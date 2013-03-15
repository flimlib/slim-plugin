//
// BatchHistogram.java
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

import loci.slim.analysis.Binning;
import loci.slim.analysis.HistogramStatistics;

/**
 *
 * @author Aivar Grislis grislis @ wisc.edu
 */
public class BatchHistogram {
	private static final int BINS = 200000; // 200,000 * 24 bytes ~= 4.8Mb
	private String title;
	private int fittedParamIndex;
	private double minRange; //TODO in statistics
	private double maxRange; //TODO in statistics
	private long underMinCount;
	private long overMaxCount;
	private double underMinSum;
	private double overMaxSum;
	private double underMinVarianceSum;
	private double overMaxVarianceSum;
	private HistogramBin[] bins;
	private double sum;
	private long count;
	private double minValue;
	private double maxValue;
	private HistogramStatistics statistics;
	
	public BatchHistogram(String title, int fittedParamIndex) {
		this.title = title;
		this.fittedParamIndex = fittedParamIndex;
		
		// expect values from 0.0..20.0 nanoseconds
		minRange = 0.0;
		maxRange = 20.0;
		
		minValue = Double.MAX_VALUE;
		maxValue = -Double.MAX_VALUE;
		bins = new HistogramBin[BINS];
		for (int bin = 0; bin < BINS; ++bin) {
			bins[bin] = new HistogramBin();
		}
	}
	
	public String getTitle() { // 0 usages
		return title;
	}
	
	public int getFittedParamIndex() {
		return fittedParamIndex;
	}

	/**
	 * Account for a given value in the histogram.
	 * 
	 * @param value 
	 */
	public void process(double value) {
		// check for count overflow
		if (count == Long.MAX_VALUE) {
			throw new RuntimeException("BatchHistogram count overflow");
		}
		++count;
		sum += value;

		// keep track of min/max
		if (value < minValue) {
			minValue = value;
		}
		if (value > maxValue) {
			maxValue = value;
		}

		// bin the value
		if (value < minRange) {
			++underMinCount;
			underMinSum += value;
			underMinVarianceSum += value * value;
			
		}
		else if (value > maxRange) {
			++overMaxCount;
			overMaxSum += value;
			overMaxVarianceSum += value * value;
		}
		else {
			int bin = Binning.valueToBin(BINS, minRange, maxRange, value);
			//System.out.println("value was " + value + " bin is " + bin + " BINS " + BINS);
			++bins[bin].count;
			bins[bin].meanSum += value;
			bins[bin].varianceSum += value * value;
		}
	}

	/**
	 * Called periodically to get current statistics.
	 * 
	 * @return 
	 */
	public HistogramStatistics getStatistics() {
		// build fresh statistics
		statistics = null;
		
		return computeStatistics();
	}

	/**
	 * Returns scaled-down 256-bin histogram adjusted to interquartile range.
	 * 
	 * @return 
	 */
	public long[] getScaledHistogram() {
		return getScaledHistogram(256);
	}

	/**
	 * Returns scaled-down histogram adjusted to interquartile range.
	 * 
	 * @param binCount
	 * @return 
	 */
	public long[] getScaledHistogram(int binCount) {
		statistics = computeStatistics();

		double iqr = statistics.getThirdQuartile() - statistics.getFirstQuartile();
		double minSubRange = statistics.getMedian() - 1.5 * iqr;
		double maxSubRange = statistics.getMedian() + 1.5 * iqr;
		//System.out.println("getScaledHistogram new range " + minSubRange + " " + maxSubRange);
		
		int minBin = Binning.valueToBin(BINS, minRange, maxRange, minSubRange);
		int maxBin = Binning.valueToBin(BINS, minRange, maxRange, maxSubRange);
		if (maxBin - minBin + 1 < binCount) {
			System.out.println("getScaledHistogram scaling up from " + (maxBin - minBin + 1) + " to " + binCount);
		}

		// character existing bins by center values
		double[] srcCenterValues = Binning.centerValuesPerBin(BINS, minRange, maxRange);
		// allocate to destination bins by edge values
		double[] dstEdgeValues = Binning.edgeValuesPerBin(binCount, minSubRange, maxSubRange);
		long[] dstBins = new long[binCount];
		int dstBin = 0;
		for (int srcBin = minBin; srcBin <= maxBin; ++srcBin) {
			// special handling for last destination bin
			if (dstBin == binCount - 1) {
				if (srcCenterValues[srcBin] > maxSubRange) {
					break;
				}
			}
			else {
				// need a new destination bin?
				if (srcCenterValues[srcBin] > dstEdgeValues[dstBin + 1]) {
					// time for a new bin
					++dstBin;
				}
			}
			dstBins[dstBin] += bins[srcBin].count;
		}
		return dstBins;
	}
	
	public double[] getScaledCenterValues() {
		return getScaledCenterValues(256);
	}
	
	public double[] getScaledCenterValues(int binCount) {
		statistics = computeStatistics();
		
		return Binning.centerValuesPerBin(binCount, statistics.getMinRange(), statistics.getMaxRange());
	}
	
	private HistogramStatistics computeStatistics() {
		// only compute if not already computed
		if (null == statistics) {
			statistics = new HistogramStatistics();
			
			// title
			statistics.setTitle(title);

			// count, min, max
			statistics.setCount(count);
			statistics.setMin(minValue);
			statistics.setMax(maxValue);
			
			// mean
			statistics.setMean(sum / count);
			
			// calculate running standard deviation
			// https://en.wikipedia.org/wiki/Standard_deviation#Rapid_calculation_methods
			double s0 = 0.0;
			double s1 = 0.0;
			double s2 = 0.0;
			for (HistogramBin histogramBin : bins) {
				s0 += histogramBin.count;
				s1 += histogramBin.meanSum;
				s2 += histogramBin.varianceSum;
			}
			s0 += underMinCount;
			s0 += overMaxCount;
			s1 += underMinSum;
			s1 += overMaxSum;
			s2 += underMinVarianceSum;
			s2 += overMaxVarianceSum;
			//System.out.println("s0 " + s0 + " (count " + count + ") s1 " + s1 + " s2 " + s2);
			double standardDeviation = Math.sqrt(s0 * s2 - s1 * s1) / count;
			statistics.setStandardDeviation(standardDeviation);
			
			// quartiles
			statistics.setFirstQuartile(countToValue(count / 4));
			statistics.setMedian(countToValue(count / 2));
			statistics.setThirdQuartile(countToValue(3 * count / 4));
			
			// range to define outliers
			double iqr = statistics.getThirdQuartile() - statistics.getFirstQuartile();
			statistics.setMinRange(statistics.getFirstQuartile() - 1.5 * iqr);
			statistics.setMaxRange(statistics.getThirdQuartile() + 1.5 * iqr);

			// the histogram
			statistics.setHistogram(getScaledHistogram());

			long totalCount = 0;
			long[] histogram = statistics.getHistogram();
			for (int bin = 0; bin < histogram.length; ++bin) {
				totalCount += histogram[bin];
			}
			statistics.setHistogramCount(totalCount);
			
			// experimental
			double meanFromHistogram = 0.0;
			long counter = 0;
			double[] centerValues = Binning.centerValuesPerBin(BINS, minRange, maxRange);
			for (int bin = 0; bin < BINS; ++bin) {
				meanFromHistogram += centerValues[bin] * bins[bin].count;
				counter += bins[bin].count;
			}		
			meanFromHistogram += underMinSum;
			counter += underMinCount;
			meanFromHistogram += overMaxSum;
			counter += overMaxCount;
			meanFromHistogram /= counter;
			//System.out.println("computeStatistics mean from histogram " + meanFromHistogram);
		}
		return statistics;
	}

	/**
	 * Finds bin corresponding to the n-th value if values were ordered.
	 * Returns value for that bin.
	 * 
	 * @param n
	 * @return 
	 */
	private double countToValue(long n) {
		// make sure that the bin for this count is within range
		if (n < underMinCount) {
			System.out.println("want " + n + "th value, underMinCount " + underMinCount + " count " + count + " overMaxCount " + overMaxCount);
			throw new RuntimeException("BatchHistogram quartile underflow");
		}
		if (n > count - overMaxCount) {
			System.out.println("want " + n + "th value, underMinCount " + underMinCount + " count " + count + " overMaxCount " + overMaxCount);
			throw new RuntimeException("BatchHistogram quartile overflow");
		}

		// look for appropriate bin
		long sumCount = 0;
		for (int bin = 0; bin < BINS; ++bin) {
			sumCount += bins[bin].count;
			if (sumCount > n) {
				// return value of bin
				return Binning.centerValuesPerBin(BINS, minRange, maxRange)[bin];
			}
		}
		// can't happen
		throw new RuntimeException("BatchHistogram quartile problem");
	}

	/*
	 * Inner class to keep track of count and also some sums.
	 * 
	 * 24 bytes in size.
	 */
	private class HistogramBin {
		public double meanSum;
		public double varianceSum;
		long count;
	}
}
