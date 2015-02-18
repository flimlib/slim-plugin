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
import loci.slim.analysis.Binning;
import loci.slim.analysis.HistogramStatistics;
import loci.slim.fitted.FittedValue;

/**
 * Base class for a batch, cumulative histogram for a particular fitted
 * parameter.  These histograms have a large number of bins to cover the range
 * of expected values.  They accumulate statistics from any number of fitted
 * lifetime images.  At any time we can discard outliers using Tukey's Rule
 * http://www.edgarstat.com/tukeys_outliers_help.cfm
 * and zoom in and show the histogram distribution curve.
 * 
 * @author Aivar Grislis
 */
public abstract class AbstractBatchHistogram implements BatchHistogram {
	private FittedValue fittedValue;
	private String title;
	private double minRange;
	private double maxRange;
	private int totalBins;
	private long underMinCount;
	private long overMaxCount;
	private double underMinSum;
	private double overMaxSum;
	private double underMinVarianceSum;
	private double overMaxVarianceSum;
	private HistogramBin[] bins;
	private double sum;
	private long count;
	private double minValue = Double.MAX_VALUE;
	private double maxValue = -Double.MAX_VALUE;
	private HistogramStatistics statistics;

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void process(double[] values) {
		process(fittedValue.getValue(values));
	}

	/**
	 * Called periodically to get current statistics.
	 * 
	 * @return 
	 */
	@Override
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
	@Override
	public long[] getScaledHistogram() {
		return getScaledHistogram(256);
	}

	/**
	 * Returns scaled-down histogram adjusted to interquartile range.
	 * 
	 * @param totalBins
	 * @return 
	 */
	@Override
	public long[] getScaledHistogram(int binCount) {
		statistics = computeStatistics();

		double iqr = statistics.getThirdQuartile() - statistics.getFirstQuartile();
		double minSubRange = statistics.getMedian() - 1.5 * iqr;
		double maxSubRange = statistics.getMedian() + 1.5 * iqr;

		int minBin = Binning.valueToBin(totalBins, minRange, maxRange, minSubRange);
		int maxBin = Binning.valueToBin(totalBins, minRange, maxRange, maxSubRange);
		if (maxBin - minBin + 1 < binCount) {
			IJ.log("getScaledHistogram scaling up from " + (maxBin - minBin + 1) + " to " + binCount);
		}

		// character existing bins by center values
		double[] srcCenterValues = Binning.centerValuesPerBin(totalBins, minRange, maxRange);
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

	@Override
	public double[] getScaledCenterValues() {
		return getScaledCenterValues(256);
	}

	@Override
	public double[] getScaledCenterValues(int binCount) {
		statistics = computeStatistics();

		return Binning.centerValuesPerBin(binCount, statistics.getMinRange(), statistics.getMaxRange());
	}

	void setFittedValue(FittedValue fittedValue) {
		this.fittedValue = fittedValue;
		this.title = fittedValue.getTitle();
	}

	void setMinMaxRange(double minRange, double maxRange) {
		this.minRange = minRange;
		this.maxRange = maxRange;
	}

	double[] getMinMaxRange() {
		return new double[] { minRange, maxRange };
	}

	void setTotalBins(int totalBins) {
		this.totalBins = totalBins;
		bins = new HistogramBin[totalBins];
		for (int bin = 0; bin < totalBins; ++bin) {
			bins[bin] = new HistogramBin();
		}
	}

	int getTotalBins() {
		return totalBins;
	}

	/**
	 * Account for a given value in the histogram.
	 * 
	 * @param value 
	 */
	void process(double value) {
		if (Double.isNaN(value)) {
			return;
		}

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
			int bin = Binning.valueToBin(totalBins, minRange, maxRange, value);
			++bins[bin].count;
			bins[bin].meanSum += value;
			bins[bin].varianceSum += value * value;
		}
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
			//IJ.log("s0 " + s0 + " (count " + count + ") s1 " + s1 + " s2 " + s2);
			double standardDeviation = Math.sqrt(s0 * s2 - s1 * s1) / count;
			statistics.setStandardDeviation(standardDeviation);

			// quartiles
			double quartile1 = countToValue(count / 4);
			double quartile2 = countToValue(count / 2);
			double quartile3 = countToValue(3 * count / 4);

			statistics.setFirstQuartile(quartile1);
			statistics.setMedian(quartile2);
			statistics.setThirdQuartile(quartile3);

			// range to define outliers
			if (Double.isNaN(quartile1)) {
				// unable to compute first quartile, approximate
				quartile1 = binToValue(0);
			}
			if (Double.isNaN(quartile3)) {
				// unable to compute third quartile, approximate
				quartile3 = binToValue(totalBins - 1);
			}
			double iqr = quartile3 - quartile1;
			statistics.setMinRange(quartile1 - 1.5 * iqr);
			statistics.setMaxRange(quartile3 + 1.5 * iqr);

			// the histogram
			statistics.setHistogram(getScaledHistogram());

			long totalCount = 0;
			long[] histogram = statistics.getHistogram();
			for (int bin = 0; bin < histogram.length; ++bin) {
				totalCount += histogram[bin];
			}
			statistics.setHistogramCount(totalCount);
		}
		return statistics;
	}

	/**
	 * Finds bin corresponding to the n-th value if values were ordered and 
	 * returns value for that bin.
	 * 
	 * @param n
	 * @return 
	 */
	private double countToValue(long n) {
		// make sure that the bin for this count is within range
		if (n < underMinCount) {
			IJ.log("want " + n + "th value, underMinCount " + underMinCount + " count " + count + " overMaxCount " + overMaxCount);
			// computed values way out of reasonable range, report unknown statistic
			return Double.NaN;
		}
		if (n > count - overMaxCount) {
			// only (count - overMaxCount) values are actually binned
			IJ.log("want " + n + "th value, underMinCount " + underMinCount + " count " + count + " overMaxCount " + overMaxCount);
			// computed values way out of reasonable range, report unknown statistic
			return Double.NaN;
		}

		// look for appropriate bin
		int bin;
		long sumCount = 0;
		for (bin = 0; bin < totalBins; ++bin) {
			sumCount += bins[bin].count;
			if (sumCount > n) {
				break;
			}
		}
		if (0 == sumCount) {
			// degenerate case, no counts at all
			bin = 0;
		}
		else if (bin == totalBins) {
			// this shouldn't happen
			throw new RuntimeException("BatchHistogram quartile problem " + getTitle());
		}

		// return value of bin
		return binToValue(bin);
	}

	/**
	 * Computes center value of a bin.
	 * 
	 * @param bin
	 * @return 
	 */
	double binToValue(int bin) {
		return Binning.centerValuesPerBin(totalBins, minRange, maxRange)[bin];
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
