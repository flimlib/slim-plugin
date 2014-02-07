/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim.fitting.images;

import ij.IJ;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;

import loci.slim.IGrayScaleImage;
import loci.slim.IGrayScalePixelValue;
import loci.slim.histogram.HistogramDataGroup;
import loci.slim.histogram.HistogramDataNode;
import loci.slim.histogram.HistogramTool;
import loci.slim.histogram.PaletteFix;
import loci.slim.mask.IMaskGroup;
import loci.slim.mask.Mask;

import loci.slim.MyStackWindow;

/**
 * Base class for the fitted images.
 * 
 * @author Aivar Grislis
 */
abstract public class AbstractBaseFittedImage implements IFittedImage {
    private static final int UNKNOWN_CHANNEL = -1;
    private String _title;
    private int _width;
    private int _height;
    private int _channels;
    private int _channel;
    private boolean _colorizeGrayScale;
	private IGrayScaleImage _grayScaleImage;
	private IMaskGroup _maskGroup[];
    private ImageStack _imageStack;
    private ImagePlus _imagePlus;
    private MyStackWindow _stackWindow;
    private double _values[][];
    private IFittedImageSlice[] _fittedImages;
    private HistogramDataNode[] _dataChannels;
    private HistogramDataGroup _histogramData;
    private IFittedImageSlice _fittedImage;
    private Mask _mask;
    
    public AbstractBaseFittedImage(
            String title,
            int[] dimension,
            IndexColorModel indexColorModel,
            boolean colorizeGrayScale,
            IGrayScaleImage grayScaleImage,
            IMaskGroup[] maskGroup) {
        _title = title;
        _width = dimension[0];
        _height = dimension[1];
        _channels = dimension[2];
        _channel = UNKNOWN_CHANNEL;
        _colorizeGrayScale = colorizeGrayScale;
		_grayScaleImage = grayScaleImage;
		_maskGroup = maskGroup;
        
        // building an image stack
        _imageStack = new ImageStack(_width, _height);
        
        // building a list of displayed images
        List<IFittedImageSlice> fittedImageList
                = new ArrayList<IFittedImageSlice>();
        
        // building a list of HistogramDataChannels
        List<HistogramDataNode> dataChannelList
                = new ArrayList<HistogramDataNode>();
        
        for (int c = 0; c < _channels; ++c) {       
            // build the histogram data
            _values = new double[_width][_height];
            clear(_values);
            HistogramDataNode histogramDataChannel
                    = new HistogramDataNode(this, _values);
            dataChannelList.add(histogramDataChannel);
            
            // build the actual displayed image
            IFittedImageSlice fittedImage = null;
            if (colorizeGrayScale) {
                fittedImage
                        = new ColorizedFittedImage(grayScaleImage, _values); 
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
				// what channel is active?
                int channelIndex = _stackWindow.getSlice() - 1;
                _histogramData.setChannelIndex(channelIndex);
				
				// show our data with histogram tool
				HistogramTool histogramTool = HistogramTool.getInstance();
                histogramTool.setHistogramData(_histogramData);

				// make sure grayscale image is hooked up also
				_grayScaleImage.listenToMaskGroup(_maskGroup[channelIndex]);
            }
            
			@Override
            public void focusLost(FocusEvent e) { }
        });
        _stackWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
				// when closing, delete all masks
				for (int i = 0; i < _dataChannels.length; ++i) {
					_dataChannels[i].rescindMask();
				}
            }
        });
        
        _fittedImages
                = fittedImageList.toArray(new IFittedImageSlice[0]);
        _dataChannels
                = dataChannelList.toArray(new HistogramDataNode[0]);
        
        for (int i = 0; i < _dataChannels.length; ++i) {
            _dataChannels[i].setMaskGroup(maskGroup[i]);
        }
        
        _histogramData = new HistogramDataGroup(this, title, _dataChannels); 
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
        for (IFittedImageSlice fittedImage : _fittedImages) {
            fittedImage.setColorModel(colorModel);
        }
    }    
    
    /**
     * Gets the associated histogram data object.
     * @return
     */
    public HistogramDataGroup getHistogramData() {
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
    public void updateRanges() {
        double[] minMaxLUT = _histogramData.updateRanges();

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

    /**
     * Redraws the entire image after a masking change.  Mask shows all pixels
	 * masked by all nodes combined, including this node.
     * 
     * @param mask
     */
    public void updateMask(Mask mask) {
		if (mask != _mask) {
            _mask = mask;
            
			// recalculate min/max data/view/LUT
			updateRanges();

			// redraw the image using the mask
            for (int y = 0; y < _values[0].length; ++y) {
                for (int x = 0; x < _values.length; ++x) {
                    double value = Double.NaN;
                    if (null == mask || mask.test(x, y)) {
                        value = _values[x][y];
                    }
                    _fittedImage.draw(x, y, value);
                }
            }

            // This forces redisplay:
            _imagePlus.setProcessor(_fittedImage.getImageProcessor().duplicate()); 
        }
    }
	
    private String debugMask(Mask mask) {
		if (null == mask) {
			return "NULL";
		}
		return "" + mask.getCount();
	}

    /*
     * Redisplay the image with new LUT range.
     */
    private void redisplay(double[] minMaxLUT) {
        minMaxLUT = PaletteFix.adjustMinMax(minMaxLUT[0], minMaxLUT[1]);
        if (null != _fittedImage) {
            if (_colorizeGrayScale) {
                // redraw all images with new LUT
                for (IFittedImageSlice fittedImage : _fittedImages) {
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
     * @param parameters null signals error
     */
    public void updatePixel(int[] location, double[] parameters) {
		double value;
		
		if (null == parameters) {
			value = Double.NaN;
		}
		else {
			// compute our displayable value from parameters
			value = getValue(parameters);
		}
        
        int x       = location[0];
        int y       = location[1];
        int channel = location[2];

        // check for channel change
        if (_channel != channel) {
			
			if (_channel > channel) {
				IJ.log("retrograde channel " + _channel + " > " + channel);
			}
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
