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

package loci.slim.fitting.images;

import ij.process.ImageProcessor;

import java.awt.image.IndexColorModel;

/**
 * This interface is for a 2D slice of the fitted image.
 *
 * @author Aivar Grislis
 */
public interface IFittedImageSlice {

	/**
	 * Initializes a slice.
	 *
	 */
	public void init(int width, int height, int channel,
		IndexColorModel indexColorModel);

	/**
	 * Changes LUT.
	 *
	 */
	public void setColorModel(IndexColorModel indexColorModel);

	/**
	 * Gets the underlying IJ image processor.
	 *
	 */
	public ImageProcessor getImageProcessor();

	/**
	 * Sets the minimum and maximum values for the LUT range.
	 *
	 */
	public void setMinAndMax(double min, double max);

	/**
	 * Draws a single pixel with current LUT and LUT range.
	 *
	 */
	public void draw(int x, int y, double value);
}
