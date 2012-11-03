//
// HistogramStatistics.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
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

package loci.slim.histogram;

/**
 * Container class for some histogram related statistics.
 * 
 * @author Aivar Grislis
 */
public class HistogramStatistics {
	private int[] _bins;
	private double[] _binValues;
	private double[] _quartiles;
	private int[] _quartileIndices;
	private double[] _stdDev;
	private int[] _stdDevIndices;

	/**
	 * Gets the histogram bins
	 * 
	 * @return 
	 */
	public int[] getBins() {
		return _bins;
	}

	/**
	 * Sets histogram bins
	 * 
	 * @param bins 
	 */
	public void setBins(int[] bins) {
		_bins = bins;
	}

	/**
	 * Gets values characterizing bins
	 * 
	 * @return 
	 */
	public double[] getBinValues() {
		return _binValues;
	}

	/**
	 * Sets values characterizing bins
	 * 
	 * @param binValues 
	 */
	public void setBinValues(double[] binValues) {
		_binValues = binValues;
	}

	/**
	 * Gets the quartile values
	 * 
	 * @return 
	 */
	public double[] getQuartiles() {
		return _quartiles;
	}

	/**
	 * Sets the quartile values
	 * 
	 * @param quartiles 
	 */
	public void setQuartiles(double[] quartiles) {
		_quartiles = quartiles;
	}

	/**
	 * Gets the indices of the quartile values
	 * 
	 * @return 
	 */
	public int[] getQuartileIndices() {
		return _quartileIndices;
	}

	/**
	 * Sets the indices of the quartile values
	 * 
	 * @param quartileIndices 
	 */
	public void setQuartileIndices(int[] quartileIndices) {
		_quartileIndices = quartileIndices;
	}
	
	/**
	 * Gets the 'fences' that demarcate outliers.
	 * 
	 * @return 
	 */
	public double[] getFences() {
		double[] fences = null;
		if (null != _quartiles) {
			fences = new double[2];
			double interquartileRange = _quartiles[2] - _quartiles[0];
			
            // 1.5 factor is rule of thumb by John Tukey, inventor of box-and-whisker plots.
			// "Why 1.5?" JT: "Because 1 is too small and 2 is too large."
			fences[0] = _quartiles[0] - 1.5 * interquartileRange;
			fences[1] = _quartiles[2] + 1.5 * interquartileRange;
		}
		return fences;
	}

	/**
	 * Gets standard deviation values
	 * 
	 * @return 
	 */
	public double[] getStdDev() {
		return _stdDev;
	}

	/**
	 * Sets standard deviation values
	 * 
	 * @param stdDev 
	 */
	public void setStdDev(double[] stdDev) {
		_stdDev = stdDev;
	}

	/**
	 * Gets indices of standard deviation values
	 * 
	 * @return 
	 */
	public int[] getStdDevIndices() {
		return _stdDevIndices;
	}

	/**
	 * Sets indices of standard deviation values
	 * 
	 * @param stdDevIndices 
	 */
	public void setStdDevIndices(int[] stdDevIndices) {
		_stdDevIndices = stdDevIndices;
	}
}
