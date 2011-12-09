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
abstract public class AbstractBaseFittedImage implements IFittedImage {
    private String _title;
    private HistogramData _histogramData;
    
    public void setTitle(String title) {
        _title = title;
    }
    
    public String getTitle() {
        return _title;
    }

    /**
     * Sets the histogram data object associated with this fitted image.
     *
     * @param histogramData
     */
    public void setHistogramData(HistogramData histogramData) {
        _histogramData = histogramData;
    }

    /**
     * Gets the associated histogram data object.
     * @return
     */
    public HistogramData getHistogramData() {
        return _histogramData;
    }

    /**
     * Begins a fit.
     */
    public void beginFit() {

    }
    /**
     * Ends a fit.
     */
    public void endFit() {

    }

    /**
     * Updates the fitted parameters for a pixel.
     *
     * @param location
     * @param parameters
     */
    abstract public void updatePixel(int[] location, double[] parameters);

    /**
     * Updates the fitted parameters for a pixel.  The pixel is drawn
     * outsized at first.
     *
     * @param location
     * @param dimension
     * @param parameters
     */
    abstract public void updateChunkyPixel(int[] location, int[] dimension, double[] parameters);

    private void updatePixel(int[] location, double value) {
    }

    private void updateChunkyPixel(int location[], double value) {

    }

    /**
     * Recalculates the image histogram and resets the palette.  Called
     * periodically during the fit.
     */
    public void recalcHistogram() {
        _histogramData.getMinMax(); //TODO how about HistogramData.recalculate?
        // etc.
    }
}
