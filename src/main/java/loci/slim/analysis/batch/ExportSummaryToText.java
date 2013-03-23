//
// ExportSummaryToText.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2013, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim.analysis.batch;

import loci.slim.analysis.HistogramStatistics;
import loci.slim.analysis.batch.AbstractBatchHistogram;
import ij.IJ;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.curvefitter.ICurveFitter;
import loci.slim.analysis.batch.ui.BatchHistogramsFrame;
import loci.slim.analysis.batch.ui.BatchHistogramListener;
import loci.slim.analysis.plugins.ExportHistogramsToText;
import loci.slim.fitted.AFittedValue;
import loci.slim.fitted.ChiSqFittedValue;
import loci.slim.fitted.FittedValue;
import loci.slim.fitted.FractionalContributionFittedValue;
import loci.slim.fitted.FractionalIntensityFittedValue;
import loci.slim.fitted.TFittedValue;
import loci.slim.fitted.TMeanFittedValue;
import loci.slim.fitted.ZFittedValue;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * Exports a summary histogram in batch mode.
 * 
 * @author Aivar Grislis
 */
public class ExportSummaryToText {
	private ICurveFitter.FitFunction function;
	private BatchHistogramListener listener;
	private BatchHistogram[] histograms;
	private String[] titles;
	private int[] indices;
	private BatchHistogramsFrame frame;

	/**
	 * Initializes for given fitting function.
	 * 
	 * @param parameters
	 * @param function 
	 * @param listener
	 */
	public void init(FittedValue[] parameters, ICurveFitter.FitFunction function, BatchHistogramListener listener) {
		this.function = function;
		this.listener = listener;

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
				//TODO
			}
			
			if (null != histogram) {
				histogram.init(parameter);
				histogramsList.add(histogram);
			}
		}
		
		histograms = histogramsList.toArray(new BatchHistogram[histogramsList.size()]);
	}

	/**
	 * Processes each image in batch job.
	 * 
	 * @param fileName
	 * @param image 
	 */
	public void process(String fileName, Image<DoubleType> image) {
		System.out.println("process " + image.getName());
		int[] dimensions = image.getDimensions();
		int fittedParameters = dimensions[3];
		System.out.println("process fittedparams is " + fittedParameters);
		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();

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
					position[3] = i;
					cursor.setPosition(position);
					values[i] = cursor.getType().getRealDouble();
				}

				// update all batch histograms
				for (BatchHistogram histogram : histograms) {
					histogram.process(values);
				}
			}
		}
		
		
		// build list of histogram statistics for this new image
/*		List<HistogramStatistics> imageList = new ArrayList<HistogramStatistics>();
		ExportHistogramsToText export = new ExportHistogramsToText();
		for (int i = 0; i < titles.length; ++i) {
		    HistogramStatistics imageStatistics = export.getStatistics(titles[i], image, 0, indices[i]);
		    imageList.add(imageStatistics);
		} //TODO this works only if titles/indices initialized; need a new mechanism for ExportHistogramsToText to work
		*/
		
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
			null, //imageList.toArray(new HistogramStatistics[imageList.size()]), 
			summaryList.toArray(new HistogramStatistics[summaryList.size()]));
	}

	/**
	 * Exports the summary to a file.
	 * 
	 * @param fileName 
	 */
    public void export(String fileName) {
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
							
				for (BatchHistogram histogram : histograms) {
					HistogramStatistics statistics = histogram.getStatistics();
					statistics.export(bufferedWriter);
				}
				bufferedWriter.close();
			}
			catch (IOException exception) {
				IJ.log("exception writing to file " + fileName);
				IJ.handleException(exception);
			}
		}
	}
}
