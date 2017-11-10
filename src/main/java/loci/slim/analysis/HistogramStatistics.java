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

package loci.slim.analysis;

import ij.IJ;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import loci.slim.SLIMProcessor;

/**
 * A histogram statistics class used for export to text.
 *
 * @author Aivar Grislis
 */
//TODO reconcile with loci.slim.histogram.HistogramStatistics

public class HistogramStatistics {

	private static final int MIN_COUNT = 3;
	private static final String NAN = "NaN";
	private static final MathContext context = new MathContext(4,
		RoundingMode.FLOOR);
	String title;
	private long count;
	private double min;
	private double max;
	private double firstQuartile;
	private double median;
	private double thirdQuartile;
	private double mean;
	private double standardDeviation;
	private long histogramCount;
	private double minRange;
	private double maxRange;
	private long[] histogram;

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * @return the count of pixels
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @param count the count of pixels to set
	 */
	public void setCount(final long count) {
		this.count = count;
	}

	/**
	 * @return the minimum fitted value
	 */
	public double getMin() {
		return min;
	}

	/**
	 * @param min the minimum fitted value to set
	 */
	public void setMin(final double min) {
		this.min = min;
	}

	/**
	 * @return the maximum fitted value
	 */
	public double getMax() {
		return max;
	}

	/**
	 * @param max the maximum fitted value to set
	 */
	public void setMax(final double max) {
		this.max = max;
	}

	/**
	 * @return the first quartile value
	 */
	public double getFirstQuartile() {
		return firstQuartile;
	}

	/**
	 * @param firstQuartile the first quartile value to set
	 */
	public void setFirstQuartile(final double firstQuartile) {
		this.firstQuartile = firstQuartile;
	}

	/**
	 * @return the median value (second quartile)
	 */
	public double getMedian() {
		return median;
	}

	/**
	 * @param median the median value to set
	 */
	public void setMedian(final double median) {
		this.median = median;
	}

	/**
	 * @return the third quartile value
	 */
	public double getThirdQuartile() {
		return thirdQuartile;
	}

	/**
	 * @param thirdQuartile the third quartile value to set
	 */
	public void setThirdQuartile(final double thirdQuartile) {
		this.thirdQuartile = thirdQuartile;
	}

	/**
	 * @return the mean value
	 */
	public double getMean() {
		return mean;
	}

	/**
	 * @param mean the mean value to set
	 */
	public void setMean(final double mean) {
		this.mean = mean;
	}

	/**
	 * @return the standard deviation value
	 */
	public double getStandardDeviation() {
		return standardDeviation;
	}

	/**
	 * @param standardDeviation the standard deviation value to set
	 */
	public void setStandardDeviation(final double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	/**
	 * @return the count of pixels in the histogram
	 */
	public long getHistogramCount() {
		return histogramCount;
	}

	/**
	 * @param histogramCount the count of pixels in the histogram to set
	 */
	public void setHistogramCount(final long histogramCount) {
		this.histogramCount = histogramCount;
	}

	/**
	 * @return the minimum value in the histogram
	 */
	public double getMinRange() {
		return minRange;
	}

	/**
	 * @param minRange the minimum value in the histogram to set
	 */
	public void setMinRange(final double minRange) {
		this.minRange = minRange;
	}

	/**
	 * @return the maximum value in the histogram
	 */
	public double getMaxRange() {
		return maxRange;
	}

	/**
	 * @param maxRange the maximum value in the histogram to set
	 */
	public void setMaxRange(final double maxRange) {
		this.maxRange = maxRange;
	}

	/**
	 * @return the histogram counts
	 */
	public long[] getHistogram() {
		return histogram;
	}

	/**
	 * @param histogram the histogram counts to set
	 */
	public void setHistogram(final long[] histogram) {
		this.histogram = histogram;
	}

	/**
	 * Exports the histogram statistics using a BufferedWriter.
	 *
	 * @throws IOException
	 */
	public boolean export(final BufferedWriter writer, final char separator)
		throws IOException
	{
		if (getCount() < MIN_COUNT) {
			writer.write("Count" + separator + getCount());
			writer.newLine();
			writer.write("Too few pixels for histograms");
			writer.newLine();
			writer.newLine();

			// don't process any more parameters; all will have same count
			return false;
		}

		if(SLIMProcessor.macroParams.useDetailStat){
			///add check for either all statisctics or just mean
			//if(meanValueOnly){}else{}
			writer.write("Parameter" + separator + getTitle());
			writer.newLine();

			// put out statistics
			writer.write("Min" + separator + showParameter(getMin()));
			writer.newLine();
			writer.write("Max" + separator + showParameter(getMax()));
			writer.newLine();
			writer.write("Count" + separator + getCount());
			writer.newLine();
			writer.write("Mean" + separator + showParameter(getMean()));
			writer.newLine();
			writer.write("Standard Deviation" + separator +
					showParameter(getStandardDeviation()));
			writer.newLine();
			writer
			.write("1st Quartile" + separator + showParameter(getFirstQuartile()));
			writer.newLine();
			writer.write("Median" + separator + showParameter(getMedian()));
			writer.newLine();
			writer
			.write("3rd Quartile" + separator + showParameter(getThirdQuartile()));
			writer.newLine();

			// put out histogram
			final long[] histo = getHistogram();
			writer.write("Histogram");
			writer.newLine();
			writer.write("Bins" + separator + histo.length);
			writer.newLine();
			writer.write("Min" + separator + showParameter(getMinRange()));
			writer.newLine();
			writer.write("Max" + separator + showParameter(getMaxRange()));
			writer.newLine();
			writer.write("Count" + separator + getHistogramCount());
			writer.newLine();

			final double[] values =
					Binning.centerValuesPerBin(histo.length, getMinRange(), getMaxRange());
			for (int j = 0; j < histo.length; ++j) {
				writer.write(showParameter(values[j]) + separator + histo[j]);
				writer.newLine();
			}
			writer.newLine();
		}
		else{
			writer.write("Mean" + separator + showParameter(getMean()));
			writer.newLine();
		}
		return true;
	}

	/**
	 * Exports a set of histograms.
	 *
	 * @throws IOException
	 */
	public static boolean export(final HistogramStatistics[] statistics,
		final BufferedWriter writer, final char separator) throws IOException
	{
		// check for degenerate case
		final long count = statistics[0].getCount();
		if (count < MIN_COUNT) {
			writer.write("Count" + separator + count);
			writer.newLine();
			writer.write("Too few pixels for histograms");
			writer.newLine();
			writer.newLine();

			// don't process any more parameters; all will have same count
			return false;
		}
		boolean firstTime = true;


		if(SLIMProcessor.macroParams.useDetailStat){///use detailed stat
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Parameter" + separator + statistic.getTitle());
				//writer.newLine();
			}
			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Min" + separator + showParameter(statistic.getMin()));
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Max" + separator + showParameter(statistic.getMax()));
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Count" + separator + statistic.getCount());
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Mean" + separator + showParameter(statistic.getMean()));
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Standard Deviation" + separator +
						showParameter(statistic.getStandardDeviation()));
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("1st Quartile" + separator +
						showParameter(statistic.getFirstQuartile()));
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Median" + separator + showParameter(statistic.getMedian()));
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("3rd Quartile" + separator +
						showParameter(statistic.getThirdQuartile()));
			}
			writer.newLine();

			// get all of the histograms into memory
			int maxHistosLength = Integer.MIN_VALUE;
			final long[][] histos = new long[statistics.length][];
			final double[][] centers = new double[statistics.length][];
			for (int i = 0; i < statistics.length; ++i) {
				histos[i] = statistics[i].getHistogram();
				if (histos[i].length > maxHistosLength) {
					maxHistosLength = histos[i].length;
				}
				centers[i] =
						Binning.centerValuesPerBin(histos[i].length, statistics[i]
								.getMinRange(), statistics[i].getMaxRange());
			}

			firstTime = true;
			for (final long[] histo : histos) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Histogram Bins" + separator + histo.length);
			}
			writer.newLine();
			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Histogram Min" + separator +
						showParameter(statistic.getMinRange()));
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Histogram Max" + separator +
						showParameter(statistic.getMaxRange()));
			}
			writer.newLine();

			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write("Histogram Count" + separator +
						statistic.getHistogramCount());
			}
			writer.newLine();

			for (int bin = 0; bin < maxHistosLength; ++bin) {
				firstTime = true;
				for (int i = 0; i < histos.length; ++i) {
					if (!firstTime) {
						writer.write(separator);
					}
					firstTime = false;
					if (bin < histos[i].length) {
						writer.write("" + showParameter(centers[i][bin]) + separator +
								histos[i][bin]);
					}
					else {
						writer.write(separator);
					}
				}
				writer.newLine();
			}
		}
		else {/// for Brief stat, concise format
			if(!SLIMProcessor.macroParams.writeParamOnce){
				for (final HistogramStatistics statistic : statistics) {
					if (!firstTime) {
						writer.write(separator);
					}
					firstTime = false;//writing parameters
					IJ.log("getting titles"+statistic.getTitle());
					writer.write(statistic.getTitle());//title is A T Z X
				}
				SLIMProcessor.macroParams.writeParamOnce=true;
			}
			writer.newLine();
			firstTime = true;
			for (final HistogramStatistics statistic : statistics) {
				if (!firstTime) {
					writer.write(separator);
				}
				firstTime = false;
				writer.write( showParameter(statistic.getMean()));
			}
			writer.newLine();

		}

		return true;
	}

	/**
	 * exports statistics only with mean values.
	 * @throws IOException
	 */
	public static boolean export(final HistogramStatistics[] statistics,
			final BufferedWriter writer, final char separator,final String name) throws IOException{

		boolean firstTime = true;
		if(!SLIMProcessor.macroParams.isAppendUsed){///titles are not written when in append mode
			for (final HistogramStatistics statistic : statistics) {

				if (!firstTime) {
					writer.write(separator);		
				}
				firstTime = false;//writing parameters
					writer.write(statistic.getTitle());
			}
			writer.newLine();
		}
		SLIMProcessor.macroParams.writeParamOnce=true;


		firstTime = true;
		SLIMProcessor.macroParams.meanStatValues= new String[statistics.length];
		int i=0;

		for (final HistogramStatistics statistic : statistics) {
			if (!firstTime) {
				writer.write(separator);
			}
			firstTime = false;
			double meanVal=statistic.getMean();
			SLIMProcessor.macroParams.meanStatValues[i++]=String.format("%.6f", meanVal);
			writer.write( showParameter(meanVal));
		}
		SLIMProcessor.macroParams.fittingDone=true;
		writer.write(separator+name);
		return true;


	}

	private static String showParameter(final double parameter) {
		String returnValue = NAN;
		if (!Double.isNaN(parameter)) {
			returnValue =
				BigDecimal.valueOf(parameter).round(context).toEngineeringString();
		}
		return returnValue;
	}
}
