/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.slim.fitting;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.IndexColorModel;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import imagej.slim.histogram.HistogramData;
import imagej.slim.histogram.HistogramDataChannel;
import imagej.slim.histogram.HistogramTool;
import imagej.slim.histogram.PaletteFix;

/**
 *
 * @author aivar
 */
abstract public class AbstractBaseFittedImage implements IFittedImage {
    private String _title;
    private double _values[][];
    private HistogramData _histogramData;
    private FloatProcessor _image;
    private ImagePlus _imagePlus;
    
    public AbstractBaseFittedImage(String title, int[] dimension) {
        _title = title;
        int x = dimension[0];
        int y = dimension[1];
        _values = new double[x][y];
        //TODO need to handle multiple channels:
        // _values c/b slice being drawn only; refer to Image for other slices; this scheme would fall apart if Image is colorized grayscale like SPCImage
        HistogramDataChannel histogramDataChannel = new HistogramDataChannel(_values);
        HistogramDataChannel[] histogramDataChannels = new HistogramDataChannel[] { histogramDataChannel };
        _histogramData = new HistogramData(this, title, histogramDataChannels);
        _image = new FloatProcessor(x, y);
        _image.setColorModel(imagej.slim.histogram.HistogramTool.getIndexColorModel());
        // fill the image with a value that will be out of LUT range and paint black.
        _image.setValue(Float.NaN);
        _image.fill();
        _imagePlus = new ImagePlus(title, _image);
        _imagePlus.show();
        _imagePlus.getWindow().addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                HistogramTool.getInstance().setHistogramData(_histogramData);
            }
            
            public void focusLost(FocusEvent e) { }
        });  
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
     * Sets the color model used to display float values.
     * 
     * @param colorModel 
     */   
    public void setColorModel(IndexColorModel colorModel) {
        _image.setColorModel(colorModel);
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
     * Cancels a fit.
     */
    public void cancelFit() {
       _imagePlus.close();
       _imagePlus.hide();
    }

    /**
     * Recalculates the image histogram and resets the palette.  Called
     * periodically during the fit.  Redisplays the image.
     */
    public void recalcHistogram() {
        double[] minMaxLUT = _histogramData.recalcHistogram();

        if (null != minMaxLUT) {
            System.out.println("Internal redisplay " + minMaxLUT[0] + " " + minMaxLUT[1]);
            //TODO horrible kludge here!!!  But why on earth would these be zero?
            //TODO #2 wound up enabling this again, otherwise you don't get any images except current image
            if (true) { // 0 != minMaxLUT[0] && 0 != minMaxLUT[1]) {
                redisplay(minMaxLUT);
            }
        }
        else System.out.println("min max null");
        
        System.out.println("RECALC " + numInvalid(_values));
    }

    /**
     * Called from the histogram tool.  Redisplays the image after LUT ranges
     * have changed.
     */
    public void redisplay() {
        System.out.println("public redisplay");
        double[] minMaxLUT = _histogramData.getMinMaxLUT();
        redisplay(minMaxLUT);
    }

    /*
     * Redisplay the image with new LUT range.
     */
    private void redisplay(double[] minMaxLUT) {
        minMaxLUT = PaletteFix.adjustMinMax(minMaxLUT[0], minMaxLUT[1]);
        _image.setMinAndMax(minMaxLUT[0], minMaxLUT[1]);
        System.out.println("SETTING MIN AND MAX LUT TO " + minMaxLUT[0] + " " + minMaxLUT[1]);
        _imagePlus.setProcessor(_image.duplicate());
    }
    
    private int numInvalid(double[][] values) {
        int count = 0;
        int num = 0;
        for (int y = 0; y < values[0].length; ++y) {
            for (int x = 0; x < values.length; ++x) {
                if (Double.isNaN(values[x][y])) {
                    ++num;
                }
                ++count;
            }
        }
        System.out.println("checked " + count + " pixels,  found invalid " + num);
        return num;
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

    /*
     * Clears the values for this slice.
     * 
     * @param values 
     */
    private void clear(double[][] values) {
        for (int y = 0; y < values[0].length; ++y) {
            for (int x = 0; x < values.length; ++x) {
                values[x][y] = Double.NaN;
            }
        }
    }
}
