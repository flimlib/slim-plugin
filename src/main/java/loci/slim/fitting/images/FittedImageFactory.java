//
// FittedImageFactory.java
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

import loci.slim.IGrayScalePixelValue;
import loci.slim.fitting.images.IFittedImage;
import loci.slim.fitting.images.SimpleFittedImage;
import loci.slim.fitting.images.FractionalIntensityImage;
import loci.slim.fitting.images.FractionalContributionImage;
import loci.slim.fitting.images.FittedImageFitter.FittedImageType;
import loci.slim.mask.IMaskGroup;

/**
 * Factory creates fitted images.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class FittedImageFactory {
    private static FittedImageFactory INSTANCE = null;
    
    private FittedImageFactory() { 
    }
    
    public static synchronized FittedImageFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new FittedImageFactory();
        }
        return INSTANCE;
    }
    
    public IFittedImage createImage(
            FittedImageType outputImageType,
            int[] dimension,
            IndexColorModel indexColorModel,
            int components,
            boolean colorizeGrayScale,
            IGrayScalePixelValue grayScalePixelValue,
            IMaskGroup[] maskGroup)
    {
        IFittedImage fittedImage = null;
        String title;
        switch (outputImageType) {
            case A1:
                title = (1 == components) ? "A" : "A1";
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.A1_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case T1:
                title = (1 == components) ? "T" : "T1";
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.T1_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case A2:
                fittedImage = new SimpleFittedImage("A2", dimension,
                        indexColorModel, FittedImageFitter.A2_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case T2:
                fittedImage = new SimpleFittedImage("T2", dimension,
                        indexColorModel, FittedImageFitter.T2_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case A3:
                fittedImage = new SimpleFittedImage("A3", dimension,
                        indexColorModel, FittedImageFitter.A2_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case T3:
                fittedImage = new SimpleFittedImage("T3", dimension,
                        indexColorModel, FittedImageFitter.T2_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case H:
                fittedImage = new SimpleFittedImage("H", dimension,
                        indexColorModel, FittedImageFitter.H_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case Z:
                fittedImage = new SimpleFittedImage("Z", dimension,
                        indexColorModel, FittedImageFitter.Z_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case CHISQ:
                fittedImage = new SimpleFittedImage("X2", dimension,
                        indexColorModel, FittedImageFitter.CHISQ_INDEX,
                        colorizeGrayScale, grayScalePixelValue, maskGroup);
                break;
            case F1:
                fittedImage = new FractionalIntensityImage("F1", dimension,
                        indexColorModel, 0, components, colorizeGrayScale,
                        grayScalePixelValue, maskGroup);
                break;
            case F2:
                fittedImage = new FractionalIntensityImage("F2", dimension,
                        indexColorModel, 1, components, colorizeGrayScale,
                        grayScalePixelValue, maskGroup);
                break;
            case F3:
                fittedImage = new FractionalIntensityImage("F3", dimension,
                        indexColorModel, 2, components, colorizeGrayScale,
                        grayScalePixelValue, maskGroup);
                break;
            case f1:
                fittedImage = new FractionalContributionImage("f1", dimension,
                        indexColorModel, 0, components, colorizeGrayScale,
                        grayScalePixelValue, maskGroup);
                break;
            case f2:
                fittedImage = new FractionalContributionImage("f2", dimension,
                        indexColorModel, 1, components, colorizeGrayScale,
                        grayScalePixelValue, maskGroup);
                break;
            case f3:
                fittedImage = new FractionalContributionImage("f3", dimension,
                        indexColorModel, 2, components, colorizeGrayScale,
                        grayScalePixelValue, maskGroup);
                break;
            case Tm:
     System.out.println("in FittedImageFactory for Tm");
                fittedImage = new TauMeanImage("Tm", dimension,
                        indexColorModel, 0, components, colorizeGrayScale,
                        grayScalePixelValue, maskGroup);
                break;
        }
        return fittedImage;
    }
}
