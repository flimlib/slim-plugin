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

import java.awt.image.IndexColorModel;

import loci.slim.histogram.HistogramDataGroup;
import loci.slim.mask.Mask;

/**
 * Interface for a fitted image.
 *
 * @author Aivar Grislis
 */
public interface IFittedImage {

	/**
	 * Gets the title of this image.
	 *
	 * @return title
	 */
	public String getTitle();

	/**
	 * Sets the color model used to display float values.
	 *
	 * @param colorModel
	 */
	public void setColorModel(IndexColorModel colorModel);

	/**
	 * Gets the associated histogram data object.
	 *
	 * @return
	 */
	public HistogramDataGroup getHistogramData();

	/**
	 * Begins a fit.
	 */
	public void beginFit();

	/**
	 * Ends a fit.
	 */
	public void endFit();

	/**
	 * Cancels a fit
	 */
	public void cancelFit();

	/**
	 * Updates the fitted parameters for a pixel.
	 *
	 * @param location
	 * @param parameters
	 */
	public void updatePixel(int[] location, double[] parameters);

	/**
	 * Updates the fitted parameters for a pixel. The pixel is drawn outsized at
	 * first.
	 *
	 * @param location
	 * @param dimension
	 * @param parameters
	 */
	public void updateChunkyPixel(int[] location, int[] dimension,
		double[] parameters);

	/**
	 * Recalculates the image histogram and resets the palette. Called
	 * periodically during the fit.
	 */
	public void updateRanges();

	/**
	 * Redisplays the image after a LUT change.
	 */
	public void redisplay();

	/**
	 * Redisplays the image after masking.
	 *
	 * @param mask
	 */
	public void updateMask(Mask mask);

	/**
	 * Given the array of fitted parameters, get the value for this image.
	 *
	 * @param parameters
	 * @return
	 */
	public double getValue(double[] parameters);
}
