/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

import java.util.ArrayList;
import java.util.List;

import imagej.slim.fitting.engine.IFittingEngine;
import imagej.slim.fitting.engine.ThreadedFittingEngine;

/**
 *
 * @author aivar
 */
public class FLIMImageFitter {
    public enum OutputImage { A1, T1, A2, T2, A3, T3, H, Z, CHISQ, F1, F2, F3, f1, f2, f3 };
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
    private IFittingEngine _fittingEngine;
    
    public FLIMImageFitter() {
        _fittedImages = new ArrayList<IFittedImage>();
        //TODO s/b configurable which fitting engine to use:
        _fittingEngine = new ThreadedFittingEngine();
    }
    
    public void setUpFit(OutputImage[] images, int[] dimension, int components) {
        _fittedImages.clear();
        for (OutputImage image : images) {
            IFittedImage fittedImage = FLIMFittedImageFactory.getInstance().createImage(image, dimension, components);
            _fittedImages.add(fittedImage);
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
     * @param parameters
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
     * @param parameters
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
