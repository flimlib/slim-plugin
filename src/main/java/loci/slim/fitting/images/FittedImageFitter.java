//
// AbstractBaseFittedImage.java
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
import java.util.ArrayList;
import java.util.List;

import loci.slim.IGrayScalePixelValue;
import loci.slim.histogram.HistogramTool;
import loci.slim.mask.IMaskGroup;
import loci.slim.mask.MaskGroup;

/**
 * This class handles the fitted image fitting process.
 * 
 * @author Aivar Grislis
 */
public class FittedImageFitter {
    public enum FittedImageType { A1, T1, A2, T2, A3, T3, Z, H, CHISQ, F1, F2, F3, f1, f2, f3, Tm };
    public static final int A1_INDEX    = 2;
    public static final int T1_INDEX    = 3;
    public static final int A2_INDEX    = 4;
    public static final int T2_INDEX    = 5;
    public static final int A3_INDEX    = 6;
    public static final int T3_INDEX    = 7;
    public static final int H_INDEX     = 4;
    public static final int Z_INDEX     = 1;
    public static final int CHISQ_INDEX = 0;
    private List<IFittedImage> _fittedImages;
    
    public FittedImageFitter() {
        _fittedImages = new ArrayList<IFittedImage>();
    }
    
    public void setUpFit(
            FittedImageType[] imageTypes,
            int[] dimension,
            IndexColorModel indexColorModel,
            int components,
            boolean colorizeGrayScale,
            IGrayScalePixelValue grayScalePixelValue)
    {
        // create shared MaskGroup for each channel
        IMaskGroup[] maskGroup = new MaskGroup[imageTypes.length];
        for (int i = 0; i < maskGroup.length; ++i) {
            maskGroup[i] = new MaskGroup();
        }
        
        _fittedImages.clear();
        for (FittedImageType imageType : imageTypes) {
            IFittedImage fittedImage =
                    FittedImageFactory.getInstance().createImage
                            (imageType,
                            dimension,
                            indexColorModel,
                            components,
                            colorizeGrayScale,
                            grayScalePixelValue,
                            maskGroup);
            _fittedImages.add(fittedImage);
        }
        
        // Show histogram tool for the last image created
        int lastIndex = imageTypes.length - 1;
        if (lastIndex >= 0) {
			HistogramTool histogramTool = HistogramTool.getInstance();
			histogramTool.show(false); //TODO ARG hardcoded hasChannels parameter
            histogramTool.setHistogramData
                    (_fittedImages.get(lastIndex).getHistogramData());
        }
    }
    
    /**
     * Begins a fit.
     */
    public void beginFit() {
        for (IFittedImage fittedImage : _fittedImages) {
            fittedImage.beginFit();
        }
    }

    /**
     * Ends a fit.
     */
    public void endFit() {
        for (IFittedImage fittedImage : _fittedImages) {
            fittedImage.endFit();
        }

    }

    /**
     * Cancels a fit.
     */
    public void cancelFit() {
        for (IFittedImage fittedImage : _fittedImages) {
            fittedImage.cancelFit();
        }
    }

    /**
     * Updates the fitted parameters for a pixel.
     * 
     * @param location
     * @param parameters may be null
     */
    public void updatePixel(int[] location, double[] parameters) {
        for (IFittedImage fittedImage : _fittedImages) {
            fittedImage.updatePixel(location, parameters);
        }
    }

    /**
     * Updates the fitted parameters for a pixel.  The pixel is drawn
     * outsized at first.
     * 
     * @param location
     * @param dimension
     * @param parameters may be null
     */
    public void updateChunkyPixel(int[] location, int[] dimension, double[] parameters) {
        for (IFittedImage fittedImage : _fittedImages) {
            fittedImage.updateChunkyPixel(location, dimension, parameters);
        }
    }
    /**
     * Recalculates the image histogram and resets the palette.  Called 
     * periodically during the fit.
     */
    public void recalcHistogram() {    
        for (IFittedImage fittedImage : _fittedImages) {
            fittedImage.recalcHistogram();
        }
    }    

}
