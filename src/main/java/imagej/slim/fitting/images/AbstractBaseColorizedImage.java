/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.slim.fitting.images;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import imagej.slim.histogram.HistogramData;
import imagej.slim.histogram.HistogramDataChannel;
import imagej.slim.histogram.HistogramTool;
import imagej.slim.histogram.PaletteFix;

import loci.slim.MyStackWindow;

/**
 *
 * @author aivar
 */
abstract public class AbstractBaseColorizedImage implements IColorizedImage {
    private static final int UNKNOWN_CHANNEL = -1;
    private String _title;
    private int _width;
    private int _height;
    private int _channels;
    private int _channel;
    private ImageStack _imageStack;
    private ImagePlus _imagePlus;
    private MyStackWindow _stackWindow;
    private double _values[][];
    private HistogramDataChannel[] _histogramDataChannels;
    private HistogramData _histogramData;
    private FloatProcessor _imageProcessor;
    
    public AbstractBaseColorizedImage(String title, int[] dimension,
            IndexColorModel indexColorModel) {
        _title = title;
        _width = dimension[0];
        _height = dimension[1];
        _channels = dimension[2];
        _channel = UNKNOWN_CHANNEL;
        
        // building an image stack
        _imageStack = new ImageStack(_width, _height);
        
        // building a list of HistogramDataChannels
        List<HistogramDataChannel> list = new ArrayList<HistogramDataChannel>();
        
        for (int c = 0; c < _channels; ++c) {
            // build the actual displayed image
            FloatProcessor imageProcessor = new FloatProcessor(_width, _height);
            imageProcessor.setColorModel(indexColorModel);
            
            // fill the image with a value that will be out of LUT range and
            // paint black
            imageProcessor.setValue(Float.NaN);
            imageProcessor.fill();
            
            _imageStack.addSlice("" + c, imageProcessor);
            
            // build the histogram data
            _values = new double[_width][_height];
            clear(_values);
            HistogramDataChannel histogramDataChannel = new HistogramDataChannel(_values);
            list.add(histogramDataChannel);           
        }
        
        _imagePlus = new ImagePlus(title, _imageStack);
        _stackWindow = new MyStackWindow(_imagePlus);
        _stackWindow.setVisible(true);
        _stackWindow.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                HistogramTool.getInstance().setHistogramData(_histogramData);
            }
            
            public void focusLost(FocusEvent e) { }
        });  
        
        _histogramDataChannels
                = list.toArray(new HistogramDataChannel[0]);
        _histogramData = new HistogramData(this, title, _histogramDataChannels); 
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
        _imageProcessor.setColorModel(colorModel);
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
            redisplay(minMaxLUT);
        }
    }

    /**
     * Called from the histogram tool.  Redisplays the image after LUT ranges
     * have changed.
     */
    public void redisplay() {
        double[] minMaxLUT = _histogramData.getMinMaxLUT();
        redisplay(minMaxLUT);
    }

    /*
     * Redisplay the image with new LUT range.
     */
    private void redisplay(double[] minMaxLUT) {
        minMaxLUT = PaletteFix.adjustMinMax(minMaxLUT[0], minMaxLUT[1]);
        _imageProcessor.setMinAndMax(minMaxLUT[0], minMaxLUT[1]);
        _imagePlus.setProcessor(_imageProcessor.duplicate()); //TODO ARG OUCH!  This ImagePlus holds an ImageStack - maybe update(ImageProcessor ip) Updates this stack so its attributes such as min max calibration table and color model, are the same as 'ip'
    }

    
    /**
     * Updates the fitted parameters for a pixel.
     *
     * @param location
     * @param parameters
     */
    public void updatePixel(int[] location, double[] parameters) {
        // compute our displayable value from params
        double value = getValue(parameters);
        
        int x       = location[0];
        int y       = location[1];
        int channel = location[2];
        System.out.println("updatePixel(" + x + " " + y + " " + channel + " prev channel is " + _channel);
        
        if (_channel != channel) {
            _channel = channel;
            _stackWindow.showSlice(channel + 1);
            _values = _histogramDataChannels[channel].getValues();
            
            System.out.println("values is " + _values + " " + _values.hashCode());
            
            _imageProcessor = (FloatProcessor) _imageStack.getProcessor(channel + 1);
            System.out.println("ImageProc for " + channel + " is " + _imageProcessor.toString() + " hash " + _imageProcessor.hashCode());
        }
        
        _values[x][y] = value;
        _imageProcessor.setValue(value);
        _imageProcessor.drawPixel(x, y);
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
