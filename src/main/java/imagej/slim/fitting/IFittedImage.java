/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.slim.fitting;

import imagej.slim.histogram.HistogramData;

/**
 *
 * @author aivar
 */
public interface IFittedImage {

    /**
     * Gets the associated histogram data object.
     * @return
     */
    public HistogramData getHistogramData();

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
    public void recalcHistogram();

    /**
     * Given the array of fitted parameters, get the value for this image.
     * 
     * @param parameters
     * @return 
     */
    public double getValue(double[] parameters);
}
