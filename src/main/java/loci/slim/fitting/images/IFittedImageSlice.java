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

package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import ij.process.ImageProcessor;

/**
 * This interface is for a 2D slice of the fitted image.
 * 
 * @author Aivar Grislis
 */
public interface IFittedImageSlice {

    /**
     * Initializes a slice.
     * 
     * @param width
     * @param height
     * @param channel
     * @param indexColorModel 
     */
    public void init(int width, int height, int channel,
            IndexColorModel indexColorModel);

    /**
     * Changes LUT.
     * 
     * @param indexColorModel 
     */
    public void setColorModel(IndexColorModel indexColorModel);

    /**
     * Gets the underlying IJ image processor.
     * 
     * @return 
     */
    public ImageProcessor getImageProcessor();

    /**
     * Sets the minimum and maximum values for the LUT range.
     * 
     * @param min
     * @param max 
     */
    public void setMinAndMax(double min, double max);

    /**
     * Draws a single pixel with current LUT and LUT range.
     * 
     * @param x
     * @param y
     * @param value 
     */
    public void draw(int x, int y, double value);
}
