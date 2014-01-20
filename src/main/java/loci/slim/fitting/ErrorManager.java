/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
