/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import loci.slim.histogram.HistogramData;

/**
 *
 * @author Aivar Grislis
 */
public interface IColorizedImage {
    
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
     * Redisplays the image.
     */
    public void redisplay();

    /**
     * Given the array of fitted parameters, get the value for this image.
     * 
     * @param parameters
     * @return 
     */
    public double getValue(double[] parameters);
}
