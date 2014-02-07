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

package loci.slim;

/**
 * Class that handles drawing the image using progressively smaller "chunky"
 * (larger than single pixel) pixels.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ChunkyPixel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ChunkyPixel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class ChunkyPixel {
	final int _x;
	final int _y;
	final int _width;
	final int _height;
	int[] _inputLocation;
	int[] _outputLocation;

	/**
	 * Constructor.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height 
	 */
	public ChunkyPixel(int x, int y, int width, int height) {
		_x = x;
		_y = y;
		_width = width;
		_height = height;
		_inputLocation = null;
		_outputLocation = null;
	}

	/**
	 * Gets the x location.
	 * 
	 * @return 
	 */
	public int getX() {
		return _x;
	}

	/**
	 * Gets the y location.
	 * 
	 * @return 
	 */
	public int getY() {
		return _y;
	}

	/**
	 * Gets the width to draw the pixel.
	 * 
	 * @return 
	 */
	public int getWidth() {
		return _width;
	}

	/**
	 * Gets the height to draw the pixel.
	 * 
	 * @return 
	 */
	public int getHeight() {
		return _height;
	}

	/**
	 * Sets the location of the pixel in the input image.
	 * 
	 * @param location 
	 */
	public void setInputLocation(int[] location) {
		_inputLocation = location;
	}

	/**
	 * Gets the location of the pixel in the input image.
	 * 
	 * @return 
	 */
	public int[] getInputLocation() {
		return _inputLocation;
	}

	/**
	 * Sets the location of the pixel in the output image.
	 * 
	 * @param location 
	 */
	public void setOutputLocation(int[] location) {
		_outputLocation = location;
	}

	/**
	 * Gets the location of the pixel in the output image.
	 * 
	 * @return 
	 */
	public int[] getOutputLocation() {
		return _outputLocation;
	}
}
