/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.slim.fitting;

import ij.process.MyFloatProcessor; //TODO IJ hack; update to IJ2 ImgLib

import imagej.slim.histogram.HistogramData;
import imagej.slim.histogram.HistogramDataChannel;

/**
 *
 * @author aivar
 */
abstract public class AbstractBaseFittedImage implements IFittedImage {
    private String _title;
    private double _values[][];
    private HistogramData _histogramData;
    private MyFloatProcessor _image;
    
    public AbstractBaseFittedImage(String title, int[] dimension) {
        _title = title;
        int x = dimension[0];
        int y = dimension[1];
        _values = new double[x][y];
        HistogramDataChannel histogramDataChannel = new HistogramDataChannel(_values);
        HistogramDataChannel[] histogramDataChannels = new HistogramDataChannel[] { histogramDataChannel };
        _histogramData = new HistogramData(title, histogramDataChannels);
        _image = new MyFloatProcessor(x, y);
    }

    /**
     * Gets the title of the fitted image.
     * 
     * @return 
     */
    public String getTitle() {
        return _title;
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
        // clear the 2D slice
        clear(_values);

    }
    /**
     * Ends a fit.
     */
    public void endFit() {

    }

    /**
     * Recalculates the image histogram and resets the palette.  Called
     * periodically during the fit.
     */
    public void recalcHistogram() {
        _histogramData.getMinMax(); //TODO how about HistogramData.recalculate?
        // etc.
    }
    
    /**
     * Updates the fitted parameters for a pixel.
     *
     * @param location
     * @param parameters
     */
    public void updatePixel(int[] location, double[] parameters) {
        double value = getValue(parameters);
        int x = location[0];
        int y = location[1];
        _values[x][y] = value;
        _image.setValue(value);
        _image.drawPixel(x, y);
    }

    /**
     * Updates the fitted parameters for a pixel.  The pixel is drawn
     * outsized at first.
     *
     * @param location
     * @param dimension
     * @param parameters
     */
    public void updateChunkyPixel
            (int[] location, int[] dimension, double[] parameters)
    {
        //TODO for now, draw w/o chunkiness:
        updatePixel(location, parameters);
    }
        
    /**
     * Given the array of fitted parameters, get the value for this image.
     * 
     * @param parameters
     * @return 
     */
    abstract public double getValue(double[] parameters); 

    private void clear(double[][] values) {
        for (int y = 0; y < values[0].length; ++y) {
            for (int x = 0; x < values.length; ++x) {
                values[x][y] = Double.NaN;
            }
        }
    }
}
