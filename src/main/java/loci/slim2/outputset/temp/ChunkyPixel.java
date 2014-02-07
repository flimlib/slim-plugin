/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim2.outputset.temp;

/**
 * Used to draw large but increasingly smaller "chunky pixels", to provide better 
 * feedback during a slow process.
 * <p>
 * When a "chunky pixel" is drawn oversize only the upper left pixel is drawn
 * with the correct, final value.  All other pixels will be redrawn during
 * processing as further detail is filled in.
 * 
 * @author Aivar Grislis
 */
public class ChunkyPixel {
	private final long[] position;
	private final long width;
	private final long height;

	/**
	 * Constructor.
	 * 
	 * @param position
	 * @param width
	 * @param height 
	 */
	public ChunkyPixel(long[] position, long width, long height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}

	/**
	 * Gets position of upper left pixel.
	 * 
	 * @return 
	 */
	public long[] getPosition() {
		return position;
	}

	/**
	 * Gets width of pixel.
	 * 
	 * @return 
	 */
	public long getWidth() {
		return width;
	}

	/**
	 * Gets height of pixel.
	 * 
	 * @return 
	 */
	public long getHeight() {
		return height;
	}
}
