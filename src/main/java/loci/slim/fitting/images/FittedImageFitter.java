/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;

import loci.slim.IGrayScaleImage;
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
			String file,
            FittedImageType[] imageTypes,
			int channel,
			int ordinal,
            int[] dimension,
            IndexColorModel indexColorModel,
            int components,
            boolean colorizeGrayScale,
            IGrayScaleImage grayScaleImage)
    {
        // create MaskGroup for each channel
		int fittedChannels = dimension[2];
        IMaskGroup[] maskGroup = new MaskGroup[fittedChannels];
        for (int i = 0; i < maskGroup.length; ++i) {
            maskGroup[i] = new MaskGroup();
        }
		//TODO fitted image channels & grayscale image channels c/b out of synch!!!
		// grayscale image is hooked up to first MaskGroup for channel 0
		grayScaleImage.listenToMaskGroup(maskGroup[0]);
        
        _fittedImages.clear();
        for (FittedImageType imageType : imageTypes) {
            IFittedImage fittedImage =
                    FittedImageFactory.getInstance().createImage(
					        file,
                            imageType,
							channel,
							ordinal,
                            dimension,
                            indexColorModel,
                            components,
                            colorizeGrayScale,
                            grayScaleImage,
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
    public void updateLUTRange() {    
        for (IFittedImage fittedImage : _fittedImages) {
            fittedImage.updateRanges();
        }
    }    

}
