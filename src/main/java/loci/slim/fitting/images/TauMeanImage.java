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

import loci.slim.IGrayScaleImage;
import loci.slim.IGrayScalePixelValue;
import loci.slim.mask.IMaskGroup;

/**
 * This class builds a fitted image that shows the tau mean.
 * 
 * Tau Mean Tmi = sum of all Fi * Ti, where Fi is the Fractional Intensity.
 * 
 * Fractional Intensity Fi = Ai / sum of all Aj.
 *
 * @author Aivar Grislis
 */
public class TauMeanImage extends AbstractBaseFittedImage {
    private int _component;
    private int _components;

    /**
     * Create the fitted image.  Specifies number of components which should
     * be 2 or 3 and the current component which is ignored.
     * respectively.
     * 
     * @param title
     * @param dimension
     * @param component
     * @param components 
     */
    public TauMeanImage(String title, int[] dimension,
            IndexColorModel indexColorModel, int component, int components,
            boolean colorizeGrayScale, IGrayScaleImage grayScaleImage,
            IMaskGroup[] maskGroup) {
        super(title, dimension, indexColorModel, colorizeGrayScale,
                grayScaleImage, maskGroup);
        _component = component;
        _components = components;
    }
    
    public double getValue(double[] parameters) {
        double value = 0.0;
        double sum = 0.0;
        switch (_components) {
            case 2:
                sum = parameters[FittedImageFitter.A1_INDEX]
                        + parameters[FittedImageFitter.A2_INDEX];
                value = parameters[FittedImageFitter.A1_INDEX]
                            * parameters[FittedImageFitter.T1_INDEX]
                        + parameters[FittedImageFitter.A2_INDEX]
                            * parameters[FittedImageFitter.T2_INDEX];
                break;
            case 3:
                sum = parameters[FittedImageFitter.A1_INDEX]
                        + parameters[FittedImageFitter.A2_INDEX]
                        + parameters[FittedImageFitter.A3_INDEX];
                value = parameters[FittedImageFitter.A1_INDEX]
                            * parameters[FittedImageFitter.T1_INDEX]
                        + parameters[FittedImageFitter.A2_INDEX]
                            * parameters[FittedImageFitter.T2_INDEX]
                        + parameters[FittedImageFitter.A3_INDEX]
                            * parameters[FittedImageFitter.T3_INDEX];
                break;
        }
        return value / sum;
    }   
}
