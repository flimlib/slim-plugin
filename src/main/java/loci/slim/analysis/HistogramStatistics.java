//
// HistogramStatistics.java
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

package loci.slim.analysis;

import loci.slim.analysis.Binning;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * A histogram statistics class used for export to text.
 * 
 * @author Aivar Grislis
 */
public class HistogramStatistics {
    private static final char TAB = '\t';
	private static final int MIN_COUNT = 3;
	private static final String NAN = "NaN";
    private static final MathContext context = new MathContext(4, RoundingMode.FLOOR);
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
	public void setTitle(String title) {
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
	public void setCount(long count) {
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
	public void setMin(double min) {
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
	public void setMax(double max) {
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
	public void setFirstQuartile(double firstQuartile) {
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
	public void setMedian(double median) {
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
	public void setThirdQuartile(double thirdQuartile) {
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
	public void setMean(double mean) {
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
	public void setStandardDeviation(double standardDeviation) {
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
	public void setHistogramCount(long histogramCount) {
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
	public void setMinRange(double minRange) {
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
	public void setMaxRange(double maxRange) {
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
	public void setHistogram(long[] histogram) {
		this.histogram = histogram;
	}

	/**
	 * Exports the histogram statistics using a BufferedWriter.
	 * 
	 * @param writer
	 * @return
	 * @throws IOException 
	 */
	public boolean export(BufferedWriter writer) throws IOException {
		if (getCount() < MIN_COUNT) {
			writer.write("Count" + TAB + getCount());
			writer.newLine();	
			writer.write("Too few pixels for histograms");
			writer.newLine();
			writer.newLine();
							
			// don't process any more parameters; all will have same count
			return false;
		}
		else {
			writer.write("Parameter" + TAB + getTitle());
			writer.newLine();
													
			// put out statistics
			writer.write("Min" + TAB + showParameter(getMin()));
			writer.newLine();
			writer.write("Max" + TAB + showParameter(getMax()));
			writer.newLine();
			writer.write("Count" + TAB + getCount());
			writer.newLine();
			writer.write("Mean" + TAB + showParameter(getMean()));
			writer.newLine();
			writer.write("Standard Deviation" + TAB + showParameter(getStandardDeviation()));
			writer.newLine();
			writer.write("1st Quartile" + TAB + showParameter(getFirstQuartile()));
			writer.newLine();
			writer.write("Median" + TAB + showParameter(getMedian()));
			writer.newLine();
			writer.write("3rd Quartile" + TAB + showParameter(getThirdQuartile()));
			writer.newLine();

			// put out histogram
			long[] histo = getHistogram();
			writer.write("Histogram");
			writer.newLine();
			writer.write("Bins" + TAB + histo.length);
			writer.newLine();
			writer.write("Min" + TAB + showParameter(getMinRange()));
			writer.newLine();
			writer.write("Max" + TAB + showParameter(getMaxRange()));
			writer.newLine();
			writer.write("Count" + TAB + getHistogramCount());
			writer.newLine();

			double[] values = Binning.centerValuesPerBin(histo.length, getMinRange(), getMaxRange());
			for (int j = 0; j < getHistogram().length; ++j) {
				writer.write(showParameter(values[j]) + TAB + histo[j]);
				writer.newLine();
			}
			writer.newLine();
			return true;
		}
	}
	

    private String showParameter(double parameter) {
		String returnValue = NAN;
		if (!Double.isNaN(parameter)) {
			returnValue = BigDecimal.valueOf(parameter).round(context).toEngineeringString();
		}
        return returnValue;
	}
}
