/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

import imagej.slim.fitting.IFittedImage;

/**
 * Keeps an array of HistogramChannels for a given image.  Builds the 
 * histogram data as appropriate.
 * 
 * @author aivar
 */
public class HistogramData {
    private IFittedImage _image;
    private String _title;
    private HistogramDataChannel[] _channel;
    private int _channelIndex;
    private boolean _autoRange;
    private boolean _combineChannels;
    private boolean _displayChannels;
    private double _minView;
    private double _maxView;
    private double _minLUT;
    private double _maxLUT;
    private IHistogramDataListener _listener;

    /**
     * Constructor, takes an array of HistogramChannels.
     * 
     * @param channel 
     */
    public HistogramData(IFittedImage image, String title,
            HistogramDataChannel[] channel) {
        _image = image;
        _title = title;
        _channel = channel;
        _autoRange = true;
        _combineChannels = true;
        _displayChannels = true;
        _channelIndex = 0;
        _minView = _maxView = 0.0f;
        _minLUT = _maxLUT = 0.0f;
        _listener = null;
    }

    /**
     * Sets a listener for histogram data changes.  Listener is unique.
     * 
     * @param listener 
     */
    public void setListener(IHistogramDataListener listener) {
        _listener = listener;
    }
    
    /**
     * Gets a descriptive title to display on histogram UI for this data.
     * 
     * @return 
     */
    public String getTitle() {
        return _title;
    }
    
    /**
     * Gets current channel index.
     * 
     * @return 
     */
    public int getChannelIndex() {
        return _channelIndex;
    }

    /**
     * Sets current channel index.
     * 
     * @param channelIndex 
     */
    public void setChannelIndex(int channelIndex) {
        _channelIndex = channelIndex;
    }

    /**
     * Gets whether or not histogram should automatically scale to values.
     * 
     * @return whether automatically scales
     */
    public boolean getAutoRange() {
        return _autoRange;
    }
    
    /**
     * Sets whether or not histogram should automatically scale to values.
     * 
     * @param auto whether automatically scales
     */
    public void setAutoRange(boolean autoRange) {
        _autoRange = autoRange;
    }

    /**
     * Gets whether or not histogram should combine all the channels.
     * 
     * @return whether to combine all the channels
     */
    public boolean getCombineChannels() {
        return _combineChannels;
    }

    /**
     * Sets whether or not histogram should combine all the channels.
     * 
     * @param combineChannels
     */
    public void setCombineChannels(boolean combineChannels) {
        _combineChannels = combineChannels;
    }
    
    /**
     * Gets whether or not histogram should display all channels.
     * 
     * @return 
     */
    public boolean getDisplayChannels() {
        return _displayChannels;
    }

    /**
     * Sets whether or not histogram should display all channels.
     * 
     * @param displayChannels 
     */
    public void setDisplayChannels(boolean displayChannels) {
        _displayChannels = displayChannels;
    }

    /**
     * Gets minimum and maximum extents of the view.
     * 
     * @return 
     */
    public double[] getMinMaxView() {
        return new double[] { _minView, _maxView };
    }

    /**
     * Sets minimum and maximum extents of the view.
     *
     * @param min
     * @param max
     */
    public void setMinMaxView(double min, double max) {
        _minView = min;
        _maxView = max;
    }

    /**
     * Gets minimum and maximum extents of the LUT.
     * 
     * @return 
     */
    public double[] getMinMaxLUT() {
        return new double[] { _minLUT, _maxLUT };
    }

    /**
     * Sets minimum and maximum extents of the LUT.
     *
     * @param min
     * @param max
     */
    public void setMinMaxLUT(double min, double max) {
        _minLUT = min;
        _maxLUT = max;
    }

    /**
     * Sets the current min and max automatically if need be.
     *
     * Called periodically during the fit process.
     *
     * Updates listener as a side effect.
     * 
     * @return min and max of the LUT
     */
    public double[] recalcHistogram() {
        double minData;
        double maxData;
        double minDataCurrent;
        double maxDataCurrent;
        double minLUT;
        double maxLUT;
        double minView;
        double maxView;
        double[] minMaxData;

        minData = maxData = 0.0;
        minDataCurrent = maxDataCurrent = 0.0;
        minLUT = maxLUT = 0.0;
        minView = maxView = 0.0;
        if (_displayChannels || _combineChannels) {
            minData = Double.MAX_VALUE;
            maxData = Double.MIN_VALUE;
                
            // calculate actual minimum and maximum for all channels
            for (int i = 0; i < _channel.length; ++i) {
                minMaxData = _channel[i].findMinMax();
                if (minMaxData[0] < minData) {
                    minData = minMaxData[0];
                }
                if (minMaxData[1] > maxData) {
                    maxData = minMaxData[1];
                }
                if (i == _channelIndex) {
                    minDataCurrent = minMaxData[0];
                    maxDataCurrent = minMaxData[1];
                }
            }
        }
        else {
            // calculate actual minimum and maximum for current channel
            minMaxData = _channel[_channelIndex].findMinMax();
            minData = minDataCurrent = minMaxData[0];
            maxData = minDataCurrent = minMaxData[1];
        }

        if (_autoRange) {
            if (_combineChannels) {
                // LUT and view bounded by data for all channels
                _minLUT = minData;
                _maxLUT = maxData;
            }
            else {
                // LUT is bounded by data for current channel
                _minLUT = minDataCurrent;
                _maxLUT = maxDataCurrent;
            }
        }

        _minView = minData;
        _maxView = maxData;

        if (null != _listener) {
            System.out.println("tell listener " + _minView + " " + _maxView + "," + _minLUT + " " + _maxLUT);
            _listener.minMaxChanged(_minView, _maxView, _minLUT, _maxLUT);
        }

        return new double[] { minLUT, maxLUT };
    }

    public void redisplay() {
        _image.redisplay();
    }
    
    public int[] binValues(int bins) {
        // start new histogram bins           
        int[] bin = new int[bins];
        for (int i = 0; i < bins; ++i) {
            bin[i] = 0;
        }
        System.out.println("bin[3] is " + bin[3] + " bin[33] " + bin[33]);
        System.out.println("_minView is " + _minView + " max " + _maxView);
        System.out.println("_minLUT is " + _minLUT + " maxLUT " + _maxLUT);
        
        if (_displayChannels) {
            // add all channels
            for (int i = 0; i < _channel.length; ++i) {
                int[] channelBin = _channel[i].binValues(bins, _minView, _maxView);
                for (int j = 0; j < bins; ++j) {
                    bin[j] += channelBin[j];
                }
            }
        }
        else {
            // just show current channel
            return _channel[_channelIndex].binValues(bins, _minView, _maxView);
        }
            
        return bin;
    }
}
