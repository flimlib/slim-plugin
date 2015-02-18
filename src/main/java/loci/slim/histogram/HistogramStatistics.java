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
	public void setBins(final int[] bins) {
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
	public void setBinValues(final double[] binValues) {
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
	public void setQuartiles(final double[] quartiles) {
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
	public void setQuartileIndices(final int[] quartileIndices) {
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
			final double interquartileRange = _quartiles[2] - _quartiles[0];

			// 1.5 factor is rule of thumb by John Tukey, inventor of box-and-whisker
			// plots.
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
	public void setStdDev(final double[] stdDev) {
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
	public void setStdDevIndices(final int[] stdDevIndices) {
		_stdDevIndices = stdDevIndices;
	}
}
