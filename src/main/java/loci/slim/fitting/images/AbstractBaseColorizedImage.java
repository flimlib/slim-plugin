/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.fitting.images;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import loci.slim.IGrayScalePixelValue;
import loci.slim.histogram.HistogramData;
import loci.slim.histogram.HistogramDataChannel;
import loci.slim.histogram.HistogramTool;
import loci.slim.histogram.PaletteFix;

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
    private boolean _colorizeGrayScale;
    private ImageStack _imageStack;
    private ImagePlus _imagePlus;
    private MyStackWindow _stackWindow;
    private double _values[][];
    private IColorizedFittedImage[] _fittedImages;
    private HistogramDataChannel[] _dataChannels;
    private HistogramData _histogramData;
    private IColorizedFittedImage _fittedImage;
    
    public AbstractBaseColorizedImage(
            String title,
            int[] dimension,
            IndexColorModel indexColorModel,
            boolean colorizeGrayScale,
            IGrayScalePixelValue grayScalePixelValue) {
        _title = title;
        _width = dimension[0];
        _height = dimension[1];
        _channels = dimension[2];
        _channel = UNKNOWN_CHANNEL;
        _colorizeGrayScale = colorizeGrayScale;
        
        // building an image stack
        _imageStack = new ImageStack(_width, _height);
        
        // building a list of displayed images
        List<IColorizedFittedImage> fittedImageList
                = new ArrayList<IColorizedFittedImage>();
        
        // building a list of HistogramDataChannels
        List<HistogramDataChannel> dataChannelList
                = new ArrayList<HistogramDataChannel>();
        
        for (int c = 0; c < _channels; ++c) {       
            // build the histogram data
            _values = new double[_width][_height];
            clear(_values);
            HistogramDataChannel histogramDataChannel
                    = new HistogramDataChannel(_values);
            dataChannelList.add(histogramDataChannel);
            
            // build the actual displayed image
            IColorizedFittedImage fittedImage = null;
            if (colorizeGrayScale) {
                fittedImage
                        = new ColorizedFittedImage(grayScalePixelValue, _values); 
            }
            else {
                fittedImage = new FloatFittedImage();
            }
            fittedImage.init(_width, _height, c, indexColorModel);
            fittedImageList.add(fittedImage);

            // add to stack
            _imageStack.addSlice("" + c, fittedImage.getImageProcessor());
        }
        
        _imagePlus = new ImagePlus(title, _imageStack);
        _stackWindow = new MyStackWindow(_imagePlus);
        _stackWindow.setVisible(true);
        _stackWindow.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                int channelIndex = _stackWindow.getSlice() - 1;
                _histogramData.setChannelIndex(channelIndex);
                HistogramTool.getInstance().setHistogramData(_histogramData);
            }
            
            public void focusLost(FocusEvent e) { }
        });
        _stackWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Closing fitted image " + _title);
            }
        });
        
        _fittedImages
                = fittedImageList.toArray(new IColorizedFittedImage[0]);
        _dataChannels
                = dataChannelList.toArray(new HistogramDataChannel[0]);
        
        _histogramData = new HistogramData(this, title, _dataChannels); 
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
     * Sets the color model used to display values.
     * 
     * @param colorModel 
     */
    public void setColorModel(IndexColorModel colorModel) {
        for (IColorizedFittedImage fittedImage : _fittedImages) {
            fittedImage.setColorModel(colorModel);
        }
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
        if (null != _fittedImage) {
            if (_colorizeGrayScale) {
                // redraw all images with new LUT
                for (IColorizedFittedImage fittedImage : _fittedImages) {
                    fittedImage.setMinAndMax(minMaxLUT[0], minMaxLUT[1]);
                }
            }
            else {
                // when using a FloatProcessor the LUT belongs to entire stack
                _fittedImage.setMinAndMax(minMaxLUT[0], minMaxLUT[1]);
            }
            
            //TODO ARG KLUDGE
            //  This is a workaround to redisplay after the LUT range changes.
            //  Hopefully it will go away in IJ2.
            //  Maybe update(ImageProcessor ip) would work.
            //  "Updates this stack so its attributes such as min max calibration table and color model, are the same as 'ip'"
            _imagePlus.setProcessor(_fittedImage.getImageProcessor().duplicate());   
        }
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

        // check for channel change
        if (_channel != channel) {
            _channel = channel;
            _stackWindow.showSlice(channel + 1);
            _values = _dataChannels[channel].getValues();
            _fittedImage = _fittedImages[channel];
        }
 
        // save our local copy
        _values[x][y] = value;
        
        // draw pixel in fitted image
        _fittedImage.draw(x, y, value);
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
