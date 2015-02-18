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

package loci.slim.fitting;

import loci.slim.mask.Mask;

/**
 * This class keeps track of bit masks that define which pixels have a fitting
 * error.
 * 
 * @author Aivar Grislis
 */
public class ErrorManager {
	Mask errorMasks[];
	IErrorListener listener;

	/**
	 * Creates a handler for a set of bit masks by channel.
	 * 
	 * @param width
	 * @param height
	 * @param channels 
	 */
	public ErrorManager(int width, int height, int channels) {
		errorMasks = new Mask[channels];
		for (int c = 0; c < channels; ++c) {
			errorMasks[c] = new Mask(width, height);
		}
	}

	/**
	 * Defines an error mask listener.
	 * 
	 * @param listener 
	 */
	public void setListener(IErrorListener listener) {
		this.listener = listener;
	}

	/**
	 * Sets an error mask bit.
	 * 
	 * @param x
	 * @param y
	 * @param channel 
	 */
	public void noteError(int x, int y, int channel) {
		errorMasks[channel].set(x, y);
		if (null != listener) {
			listener.updateErrorMask(errorMasks[channel], channel);
		}
	}

	/**
	 * Updates the entire set of error masks when a new set of fitted images
	 * gets focus.
	 * 
	 */
	public void getFocus() {
		if (null != listener) {
			for (int c = 0; c < errorMasks.length; ++c) {
				ij.IJ.log("gotFocus, errorMasks " + errorMasks.length);
				listener.updateErrorMask(errorMasks[c], c);
			}
		}
	}

}
