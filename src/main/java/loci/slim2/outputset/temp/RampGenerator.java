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

package loci.slim2.outputset.temp;

//TODO ARG this class exists only for testing
/**
 *
 * @author Aivar Grislis
 */
public class RampGenerator {
	public enum RampType { UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT, TOP, LEFT, RIGHT, BOTTOM }
	private final RampType rampType;
	private final long width;
	private final long height;
	private final long diagonal;

	public RampGenerator(RampType rampType, long width, long height) {
		this.rampType = rampType;
		this.height = height;
		this.width = width;
		this.diagonal = getDiagonal(width, height);
	}

	public double getValue(long[] position) {
		return getValue(position[0], position[1]);
	}

	public double getValue(long x, long y) {
		long i = 0;
		long j = diagonal;
		switch (rampType) {
			case UPPER_LEFT:
				i = getDiagonal(x, y);
				break;
			case UPPER_RIGHT:
				i = getDiagonal(width - x - 1, y);
				break;
			case LOWER_LEFT:
				i = getDiagonal(x, height - y - 1);
				break;
			case LOWER_RIGHT:
				i = getDiagonal(width - x - 1, height - y - 1);
				break;
			case TOP:
				i = y;
				j = height;
				break;
			case LEFT:
				i = x;
				j = width;
				break;
			case RIGHT:
				i = width - x - 1;
				j = width;
				break;
			case BOTTOM:
				i = height - y - 1;
				j = height;
				break;
		}
		if (RampType.BOTTOM == rampType) { //TODO ARG just checking if LUT range will vary for this plane
			return ((double) i) / (j * 2);
		}
		return ((double) i) / j;
	}

	private long getDiagonal(long width, long height) {
		long returnValue = (long) Math.sqrt(width * width + height * height);
		//System.out.println("width " + width + " HEIGHt " + height + " returnValue " + returnValue);
		return returnValue;
	}
}
