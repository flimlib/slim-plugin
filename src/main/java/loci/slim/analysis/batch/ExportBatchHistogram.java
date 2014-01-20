/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
public class ExportBatchHistogram {
	private static final int BINS = 10000;
	private static final double TOTAL_MEAN = 2.845409822318876;
	private static final double IN_RANGE_MEAN = 1.8612174728965587;
	private int _paramT = 2;
	private long[] _histoT = new long[BINS];
	private long _histoTUnder = 0;
	private long _histoTOver = 0;
	private double _histoTMax = 10.0;
	private double _histoTMin = 0.0;
	private double totalSum = 0.0;
	private long totalCount = 0;
	private double totalStdDev = 0.0;
	private double sum = 0.0;
	private long count = 0;
	private double stdDev = 0.0;
	
	public void start() {
		double pixels[][] = new double[10][0];
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
						//System.out.println("--> bin " + bin);
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
	
	public void end(String fileName) {
		System.out.println("actual mean in-range is " + sum / count + " count was " + count);
		System.out.println("actual mean total is " + totalSum / totalCount + " totalCount was " + totalCount);
		System.out.println("mean from histo is " + meanFromHisto(_histoT));

		System.out.println("in-range std dev is " + stdDev / count);
		System.out.println("total std dev is " + totalStdDev / totalCount);
		
		System.out.println("std dev from histo is " + standardDeviationFromHisto(_histoT, meanFromHisto(_histoT)));
	}
	
	private double meanFromHisto(long[] histo) {
		double sum = 0.0;
		long count = 0;
		long counter;
		for (int i = 0; i < _histoT.length; ++i) {
			counter = _histoT[i];
			sum += counter * Binning.centerValuesPerBin(BINS, _histoTMin, _histoTMax)[i];
			count += counter;
		}
		System.out.println("histo count is " + count);
		return sum / count;
	}
	
	private double standardDeviationFromHisto(long[] histo, double mean) {
		double sum = 0.0;
		long count = 0;
		long counter;
		for (int i = 0; i < _histoT.length; ++i) {
			counter = _histoT[i];
			double value = Binning.centerValuesPerBin(BINS, _histoTMin, _histoTMax)[i];
			sum += counter * ((mean - value) * (mean - value));
			count += counter;
		}
		return sum / count;
	}
}
