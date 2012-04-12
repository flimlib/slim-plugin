/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;

import loci.slim.histogram.HistogramTool;

/**
 * This class
 * @author Aivar Grislis
 */
//TODO ARG FIX this class handles the process of building up a fit.  However it
//  needs to work with post-fit pixel masking
//TODO ARG FIX this class needs to work with colorized images also
public class ColorizedImageFitter {
    public enum ColorizedImageType { A1, T1, A2, T2, A3, T3, Z, H, CHISQ, F1, F2, F3, f1, f2, f3 };
    public static final int A1_INDEX    = 2;
    public static final int T1_INDEX    = 3;
    public static final int A2_INDEX    = 4;
    public static final int T2_INDEX    = 5;
    public static final int A3_INDEX    = 6;
    public static final int T3_INDEX    = 7;
    public static final int H_INDEX     = 4;
    public static final int Z_INDEX     = 1;
    public static final int CHISQ_INDEX = 0;
    private List<IColorizedImage> _fittedImages;
    
    public ColorizedImageFitter() {
        _fittedImages = new ArrayList<IColorizedImage>();
    }
    
    public void setUpFit(ColorizedImageType[] images, int[] dimension,
            IndexColorModel indexColorModel, int components) {
        _fittedImages.clear();
        for (ColorizedImageType image : images) {
            IColorizedImage fittedImage =
                    ColorizedImageFactory.getInstance().createImage
                            (image, dimension, indexColorModel, components);
            _fittedImages.add(fittedImage);
        }
        
        // Show histogram tool for the last image created
        int lastIndex = images.length - 1;
        if (lastIndex >= 0) {
            HistogramTool.getInstance().setHistogramData
                    (_fittedImages.get(lastIndex).getHistogramData());
        }
    }
    
    /**
     * Begins a fit.
     */
    public void beginFit() {
        for (IColorizedImage fittedImage : _fittedImages) {
            fittedImage.beginFit();
        }
    }

    /**
     * Ends a fit.
     */
    public void endFit() {
        for (IColorizedImage fittedImage : _fittedImages) {
            fittedImage.endFit();
        }

    }

    /**
     * Cancels a fit.
     */
    public void cancelFit() {
        for (IColorizedImage fittedImage : _fittedImages) {
            fittedImage.cancelFit();
        }
    }

    /**
     * Updates the fitted parameters for a pixel.
     * 
     * @param location
     * @param parameters
     */
    public void updatePixel(int[] location, double[] parameters) {
        for (IColorizedImage fittedImage : _fittedImages) {
            fittedImage.updatePixel(location, parameters);
        }
    }

    /**
     * Updates the fitted parameters for a pixel.  The pixel is drawn
     * outsized at first.
     * 
     * @param location
     * @param dimension
     * @param parameters
     */
    public void updateChunkyPixel(int[] location, int[] dimension, double[] parameters) {
        for (IColorizedImage fittedImage : _fittedImages) {
            fittedImage.updateChunkyPixel(location, dimension, parameters);
        }
    }
    /**
     * Recalculates the image histogram and resets the palette.  Called 
     * periodically during the fit.
     */
    public void recalcHistogram() {    
        for (IColorizedImage fittedImage : _fittedImages) {
            fittedImage.recalcHistogram();
        }
    }    

}
