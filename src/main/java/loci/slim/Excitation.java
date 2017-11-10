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

package loci.slim;

/**
 * This class is a container for values for the excitation (also known as the
 * prompt, instrument response function, or lamp function).
 *
 * @author Aivar Grislis
 */
public class Excitation {

	private final String fileName;
	private final double[] values;
	private final double timeInc;
	private int start;
	private int stop;
	private double base;

	/**
	 * Creates an excitation with given filename and values.
	 *
	 */
	public Excitation(final String fileName, final double[] values,
		final double timeInc)
	{
		this.fileName = fileName;
		this.values = values;
		this.timeInc = timeInc;
	}

	/**
	 * Gets the file name.
	 *
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Gets the values of the excitation curve.
	 *
	 */
	public double[] getValues() {
		return values;
	}

	/**
	 * Gets the horizontal time increment for the excitation curve.
	 *
	 */
	public double getTimeInc() {
		return timeInc;
	}

	/**
	 * Sets start cursor.
	 *
	 */
	public void setStart(final int start) {
		this.start = start;
	}

	/**
	 * Gets start cursor.
	 *
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Sets the stop cursor.
	 *
	 */
	public void setStop(final int stop) {
		this.stop = stop;
	}

	/**
	 * Gets the stop cursor.
	 *
	 */
	public int getStop() {
		return stop;
	}

	/**
	 * Sets the base cursor.
	 *
	 */
	public void setBase(final double base) {
		this.base = base;
	}

	/**
	 * Gets the base cursor.
	 *
	 */
	public double getBase() {
		return base;
	}
}
