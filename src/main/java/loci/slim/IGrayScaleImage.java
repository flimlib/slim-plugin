/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
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

import loci.slim.fitting.IErrorListener;
import loci.slim.mask.IMaskGroup;
import loci.slim.mask.IMaskGroupListener;


/**
 * Interface for the GrayScaleImage UI.  Allows user to click on a pixel and
 * set the current channel selection.  Used for thresholding.
 *
 * @author Aivar Grislis
 */
public interface IGrayScaleImage extends IGrayScalePixelValue, IThresholdUpdate, ICursorListener, IErrorListener, IMaskGroupListener {

	/**
	 * Closes down the grayscale image window.
	 * 
	 */
	public void close();

	/**
	 * Sets a listener for when the user clicks on the image.
	 *
	 * @param listener
	 */
	public void setListener(ISelectListener listener);

	/**
	 * Gets the channel slider selection.
	 *
	 * @return channel
	 */
	public int getChannel();

	/**
	 * Disables and enables channel selection, during and after a fit.
	 *
	 * @param enable
	 */
	public void enable(boolean enable);

	/**
	 * Gets the minimum, non-zero photon count encountered in the image.
	 * 
	 * Usually 1.0, but sometimes its 10.0 and all photon counts are multiples
	 * of 10.0.
	 * 
	 * @return 
	 */
	public double getMinNonZeroPhotonCount();

	/**
	 * Gets the photon count of the brightest point in the image.
	 * 
	 * @return 
	 */
	public double getMaxTotalPhotons();

	/**
	 * Gets the coordinates of the brightest point in the image.
	 * 
	 * @return { x, y }
	 */
	public int[] getBrightestPoint();

	/**
	 * Sets a mask group to listen for changes.
	 * 
	 * @param maskGroup 
	 */
	public void listenToMaskGroup(IMaskGroup maskGroup);
}
