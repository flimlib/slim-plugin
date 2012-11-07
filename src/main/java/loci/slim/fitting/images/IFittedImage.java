//
// IFittedImage.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import loci.slim.histogram.HistogramDataGroup;
import loci.slim.mask.Mask;

/**
 * Interface for a fitted image.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
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
     * Updates the fitted parameters for a pixel.  The pixel is drawn
     * outsized at first.
     * 
     * @param location
     * @param dimension
     * @param parameters
     */
    public void updateChunkyPixel(int[] location, int[] dimension, double[] parameters);

    /**
     * Recalculates the image histogram and resets the palette.  Called 
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
