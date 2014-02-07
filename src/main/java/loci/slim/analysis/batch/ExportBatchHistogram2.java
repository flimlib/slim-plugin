/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim.analysis.batch;

import loci.curvefitter.ICurveFitter;
import loci.slim.analysis.Binning;
import net.imglib2.RandomAccess;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;

/**
 *
 * @author Aivar Grislis
 */
public class ExportBatchHistogram2 {
	private static final int BINS = 10000;
	private int _paramT = 2;
	private HistoBin[] _histoT = new HistoBin[BINS];
	private long _histoTUnder = 0;
	private long _histoTOver = 0;
	private double _histoTMax = 10.0;
	private double _histoTMin = 0.0;
	private double totalSum = 0.0;
	private long totalCount = 0;
	private double sum = 0.0;
	private long count = 0;
	private double stdDev = 0.0;

	public void start() {

	}

	public void export(ImgPlus<DoubleType> image,
			ICurveFitter.FitFunction function) {
		long[] dimensions = new long[image.numDimensions()];
		image.dimensions(dimensions);
		RandomAccess<DoubleType> cursor = image.randomAccess();

		int index = 0;
		int[] position = new int[dimensions.length];
		for (int y = 0; y < dimensions[1]; ++y) {
			for (int x = 0; x < dimensions[0]; ++x) {
				// set position
				position[0] = x;
				position[1] = y;
				position[2] = 1; //TODO ARG hardcoded channel
				position[3] = _paramT;
				cursor.setPosition(position);

				// account for value
				double value = cursor.get().getRealDouble();
				//System.out.println("value is " + value);
				if (!Double.isNaN(value)) {
					if (value < _histoTMin) {
						++_histoTUnder;
					}
					else if (value > _histoTMax) {
						++_histoTOver;
					}
					else {
						int bin = Binning.valueToBin(BINS, _histoTMin, _histoTMax, value);
						HistoBin histoBin = _histoT[bin];
						histoBin.meanSum += value;
						histoBin.varianceSum += value * value;
						++histoBin.count;
						//System.out.println("--> bin " + bin);
						sum += value;
						++count;
					}
					totalSum += value;
					++totalCount;
				}
			}
		}

	}

	public void end(String fileName) {
		System.out.println("actual mean in-range is " + sum / count + " count was " + count);
		System.out.println("actual mean total is " + totalSum / totalCount + " totalCount was " + totalCount);
		//System.out.println("mean from histo is " + meanFromHisto(_histoT));

		System.out.println("in-range std dev is " + stdDev / count);
		//System.out.println("total std dev is " + totalStdDev / totalCount);

		//System.out.println("std dev from histo is " + standardDeviationFromHisto(_histoT, meanFromHisto(_histoT)));
	}

	private double meanFromHisto(long[] histo) {
		double sum = 0.0;
		long count = 0;
		long counter;
		for (int i = 0; i < _histoT.length; ++i) {
			//counter = _histoT[i];
			//sum += counter * Binning.centerValuesPerBin(BINS, _histoTMin, _histoTMax)[i];
			//count += counter;
		}
		System.out.println("histo count is " + count);
		return sum / count;
	}

	private double standardDeviationFromHisto(long[] histo, double mean) {
		double sum = 0.0;
		long count = 0;
		long counter;
		for (int i = 0; i < _histoT.length; ++i) {
			//counter = _histoT[i];
			//double value = Binning.centerValuesPerBin(BINS, _histoTMin, _histoTMax)[i];
			//sum += counter * ((mean - value) * (mean - value));
			//count += counter;
		}
		return sum / count;
	}

	private class HistoBin {
		public double meanSum;
		public double varianceSum;
		long count;
	}
}
