/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim2.analysis.batch;

import ij.IJ;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.curvefitter.ICurveFitter;
import loci.slim.analysis.HistogramStatistics;
import loci.slim.analysis.batch.ABatchHistogram;
import loci.slim.analysis.batch.BatchHistogram;
import loci.slim.analysis.batch.ChiSqBatchHistogram;
import loci.slim.analysis.batch.FractionalContribBatchHistogram;
import loci.slim.analysis.batch.FractionalIntensityBatchHistogram;
import loci.slim.analysis.batch.TauBatchHistogram;
import loci.slim.analysis.batch.ZBatchHistogram;
import loci.slim.analysis.batch.ui.BatchHistogramListener;
import loci.slim.analysis.batch.ui.BatchHistogramsFrame;
import loci.slim.fitted.AFittedValue;
import loci.slim.fitted.ChiSqFittedValue;
import loci.slim.fitted.FittedValue;
import loci.slim.fitted.FractionalContributionFittedValue;
import loci.slim.fitted.FractionalIntensityFittedValue;
import loci.slim.fitted.TFittedValue;
import loci.slim.fitted.TMeanFittedValue;
import loci.slim.fitted.ZFittedValue;
import net.imglib2.RandomAccess;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Exports a summary histogram in batch mode.
 * 
 * @author Aivar Grislis
 */
public class ExportSummaryToText {
	private static final int PARAM_INDEX = 2;
	private ICurveFitter.FitFunction function;
	private BatchHistogramListener listener;
	private FittedValue[] parameters;
	private BatchHistogram[] histograms;
	private String[] titles;
	private int[] indices;
	private BatchHistogramsFrame frame;
	// combine histograms in horizontal columns
	private boolean combined = true;

	/**
	 * Initializes for given fitting function.
	 * 
	 * @param parameters
	 * @param function 
	 * @param listener
	 */
	public void init(ICurveFitter.FitFunction function, FittedValue[] parameters, BatchHistogramListener listener) {
		this.function = function;
		this.listener = listener;
		this.parameters = parameters;
		
		histograms = buildBatchHistograms(parameters);
	}

	/**
	 * Processes each image in batch job.
	 * 
	 * @param fileName
	 * @param image 
	 */
	public void process(String fileName, ImgPlus<DoubleType> image) {
		long[] dimensions = new long[image.numDimensions()];
		image.dimensions(dimensions);
		int fittedParameters = (int) dimensions[PARAM_INDEX];
		RandomAccess<DoubleType> cursor = image.randomAccess();

		// build array of BatchHistogram for this image
		BatchHistogram[] imageHistograms = buildBatchHistograms(parameters);

		// traverse all pixels
		int[] position = new int[dimensions.length];
		for (int y = 0; y < dimensions[1]; ++y) {
			for (int x = 0; x < dimensions[0]; ++x) {
				// set position
				position[0] = x;
				position[1] = y;
				// non-xy dimensions remain at zero

				// get all fitted values
				double[] values = new double[fittedParameters];
				for (int i = 0; i < fittedParameters; ++i) {
					position[PARAM_INDEX] = i;
					cursor.setPosition(position);
					values[i] = cursor.get().getRealDouble();
				}
				
				// update histograms for this image
				for (BatchHistogram histogram : imageHistograms) {
					histogram.process(values);
				}

				// update all batch histograms
				for (BatchHistogram histogram : histograms) {
					histogram.process(values);
				}
			}
		}
		
		
		// build list of histogram statistics for the current image
		List<HistogramStatistics> imageList = new ArrayList<HistogramStatistics>();
		for (BatchHistogram histogram : imageHistograms) {
			HistogramStatistics imageStatistics = histogram.getStatistics();
			imageList.add(imageStatistics);
		}
		
		// build list of summarized histogram statistics
		List<HistogramStatistics> summaryList = new ArrayList<HistogramStatistics>();
		for (BatchHistogram histogram : histograms) {
		    HistogramStatistics summaryStatistics = histogram.getStatistics();
		    summaryList.add(summaryStatistics);
		}
		
		// lazy instantiation of frame
		if (null == frame) {
		    frame = new BatchHistogramsFrame(listener);
		}
		// show new image statistics and update summary
		frame.update(
			fileName,
			imageList.toArray(new HistogramStatistics[imageList.size()]), 
			summaryList.toArray(new HistogramStatistics[summaryList.size()]));
	}

	/**
	 * Exports the summary to a file.
	 * 
	 * @param fileName
	 * @param separator
	 */
    public void export(String fileName, char separator) {
		BufferedWriter bufferedWriter = null;
		try {
            bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));
        }
		catch (IOException e) {
            IJ.log("exception opening file " + fileName);
            IJ.handleException(e);
        }
		
		if (null != bufferedWriter) {
			try {
				// title this export
				bufferedWriter.write("Export Summary Histogram");
				bufferedWriter.newLine();
				bufferedWriter.newLine();

				if (combined) {
					HistogramStatistics[] statistics = new HistogramStatistics[histograms.length];
					for (int i = 0; i < statistics.length; ++i) {
						statistics[i] = histograms[i].getStatistics();
					}
					HistogramStatistics.export(statistics, bufferedWriter, separator);
				}
				else {
					for (BatchHistogram histogram : histograms) {
						HistogramStatistics statistics = histogram.getStatistics();
						statistics.export(bufferedWriter, separator);
					}
				}

				bufferedWriter.newLine();
				bufferedWriter.close();
			}
			catch (IOException exception) {
				IJ.log("exception writing to file " + fileName);
				IJ.handleException(exception);
			}
		}
	}

	/**
	 * Given an array of FittedValue creates a corresponding array of BatchHistogram.
	 * 
	 * @param parameters
	 * @return 
	 */
	private BatchHistogram[] buildBatchHistograms(FittedValue[] parameters) {
		// go through list of fitted values and build corresponding batch histograms
		List<BatchHistogram> histogramsList = new ArrayList<BatchHistogram>();
		for (FittedValue parameter : parameters) {
			BatchHistogram histogram = null;
			
			if (parameter instanceof ChiSqFittedValue) {
				histogram = new ChiSqBatchHistogram();
			}
			else if (parameter instanceof ZFittedValue) {
				histogram = new ZBatchHistogram();
			}
			else if (parameter instanceof AFittedValue) {
				histogram = new ABatchHistogram();
			}
			else if (parameter instanceof FractionalContributionFittedValue) {
				histogram = new FractionalContribBatchHistogram();
			}
			else if (parameter instanceof FractionalIntensityFittedValue) {
				histogram = new FractionalIntensityBatchHistogram();
			}
			else if (parameter instanceof TFittedValue) {
				histogram = new TauBatchHistogram();
			}
			else if (parameter instanceof TMeanFittedValue) {
				histogram = new TauBatchHistogram();
			}
			//TODO 'h' parameter for stretched
			
			if (null != histogram) {
				histogram.init(parameter);
				histogramsList.add(histogram);
			}
		}
		
		return histogramsList.toArray(new BatchHistogram[histogramsList.size()]);
	}
}
