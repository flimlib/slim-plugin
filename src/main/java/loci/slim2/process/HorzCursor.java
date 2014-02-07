/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
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

package loci.slim2.process;

/**
 * Horizontal decay cursor.  Divides decay into two regions for fitting.
 * 
 * @author Aivar Grislis
 */
public class HorzCursor {
	private final double timeInc;
	private int bin;
	private double time;

	/**
	 * Constructor, specifies time increment per bin.
	 * 
	 * @param timeInc 
	 */
	public HorzCursor(double timeInc) {
		this.timeInc = timeInc;
	}

	/**
	 * Get bin number.
	 * 
	 * @return 
	 */
	public int getBin() {
		return bin;
	}

	/**
	 * Set bin number.
	 * 
	 * @param bin 
	 */
	public void setBin(int bin) {
		this.bin = bin;
		time = bin * timeInc;
	}

	/**
	 * Get time value.
	 * 
	 * @return 
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Set time value.
	 * 
	 * @param time 
	 */
	public void setTime(double time) {
		this.time = time;
		bin = (int) (time / timeInc);
	}
}
