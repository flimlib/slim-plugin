/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
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

package loci.slim.analysis.batch;

import ij.IJ;
import loci.curvefitter.ICurveFitter;
import loci.slim.analysis.Binning;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * @author Aivar Grislis
 */
public class ExportBatchHistogram {

	private static final int BINS = 10000;
	private static final double TOTAL_MEAN = 2.845409822318876;
	private static final double IN_RANGE_MEAN = 1.8612174728965587;
	private final int _paramT = 2;
	private final long[] _histoT = new long[BINS];
	private long _histoTUnder = 0;
	private long _histoTOver = 0;
	private final double _histoTMax = 10.0;
	private final double _histoTMin = 0.0;
	private double totalSum = 0.0;
	private long totalCount = 0;
	private double totalStdDev = 0.0;
	private double sum = 0.0;
	private long count = 0;
	private double stdDev = 0.0;

	public void start() {
		final double pixels[][] = new double[10][0];
	}

	public void export(final ImgPlus<DoubleType> image,
		final ICurveFitter.FitFunction function)
	{
		final long[] dimensions = new long[image.numDimensions()];
		image.dimensions(dimensions);
		final RandomAccess<DoubleType> cursor = image.randomAccess();

		final int index = 0;
		final int[] position = new int[dimensions.length];
		for (int y = 0; y < dimensions[1]; ++y) {
			for (int x = 0; x < dimensions[0]; ++x) {
				// set position
				position[0] = x;
				position[1] = y;
				position[2] = 1; // TODO ARG hardcoded channel
				position[3] = _paramT;
				cursor.setPosition(position);

				// account for value
				final double value = cursor.get().getRealDouble();
				// IJ.log("value is " + value);
				if (!Double.isNaN(value)) {
					if (value < _histoTMin) {
						++_histoTUnder;
					}
					else if (value > _histoTMax) {
						++_histoTOver;
					}
					else {
						final int bin =
							Binning.valueToBin(BINS, _histoTMin, _histoTMax, value);
						// IJ.log("--> bin " + bin);
						++_histoT[bin];
						stdDev += (IN_RANGE_MEAN - value) * (IN_RANGE_MEAN - value);
						sum += value;
						++count;
					}
					totalStdDev += (TOTAL_MEAN - value) * (TOTAL_MEAN - value);
					totalSum += value;
					++totalCount;
				}
			}
		}

	}

	public void end(final String fileName) {
		IJ.log("actual mean in-range is " + sum / count + " count was " + count);
		IJ.log("actual mean total is " + totalSum / totalCount +
			" totalCount was " + totalCount);
		IJ.log("mean from histo is " + meanFromHisto(_histoT));

		IJ.log("in-range std dev is " + stdDev / count);
		IJ.log("total std dev is " + totalStdDev / totalCount);

		IJ.log("std dev from histo is " +
			standardDeviationFromHisto(_histoT, meanFromHisto(_histoT)));
	}

	private double meanFromHisto(final long[] histo) {
		double sum = 0.0;
		long count = 0;
		long counter;
		for (int i = 0; i < _histoT.length; ++i) {
			counter = _histoT[i];
			sum +=
				counter * Binning.centerValuesPerBin(BINS, _histoTMin, _histoTMax)[i];
			count += counter;
		}
		IJ.log("histo count is " + count);
		return sum / count;
	}

	private double standardDeviationFromHisto(final long[] histo,
		final double mean)
	{
		double sum = 0.0;
		long count = 0;
		long counter;
		for (int i = 0; i < _histoT.length; ++i) {
			counter = _histoT[i];
			final double value =
				Binning.centerValuesPerBin(BINS, _histoTMin, _histoTMax)[i];
			sum += counter * ((mean - value) * (mean - value));
			count += counter;
		}
		return sum / count;
	}
}
