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
     * Sets the histogram data object associated with this fitted image.
     *
     * @param histogramData
     */
    public void setHistogramData(HistogramData histogramData);

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
}
