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

import loci.slim.analysis.HistogramStatistics;
import loci.slim.fitted.FittedValue;

/**
 * @author Aivar Grislis
 */
public interface BatchHistogram {

	/**
	 * Sets the fitted value converter.
	 *
	 */
	public void init(FittedValue fittedValue);

	/**
	 * Gets the histogram title.
	 *
	 */
	public String getTitle();

	/**
	 * Processes fitted values for a pixel.
	 *
	 */
	public void process(double[] value);

	/**
	 * Called periodically to get current statistics.
	 *
	 */
	public HistogramStatistics getStatistics();

	/**
	 * Returns scaled-down 256-bin histogram adjusted to interquartile range.
	 *
	 */
	public long[] getScaledHistogram();

	/**
	 * Returns scaled-down histogram adjusted to interquartile range.
	 *
	 */
	public long[] getScaledHistogram(int binCount);

	/**
	 * Gets array of center values for 256-bin histogram.
	 *
	 */
	public double[] getScaledCenterValues();

	/**
	 * Gets array of center values for scaled histogram.
	 *
	 */
	public double[] getScaledCenterValues(int binCount);
}
