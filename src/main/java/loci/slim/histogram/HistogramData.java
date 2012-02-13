/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.histogram;

import loci.slim.fitting.images.IColorizedImage;

/**
 * Keeps an array of HistogramChannels for a given image.  Builds the 
 * histogram data as appropriate.  Handles updates as the fitted results are
 * available.  Handles optional autoranging.
 * 
 * @author Aivar Grislis
 */
public class HistogramData {
    private IColorizedImage _image;
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
    private double _minData;
    private double _maxData;
    private double _minDataCurrent;
    private double _maxDataCurrent;
    private IHistogramDataListener _listener;

    /**
     * Constructor, takes an array of HistogramChannels.
     * 
     * @param channel 
     */
    public HistogramData(IColorizedImage image, String title,
            HistogramDataChannel[] channel) {
        _image = image;
        _title = title;
        _channel = channel;
        _autoRange = true;
        _combineChannels = hasChannels();
        _displayChannels = hasChannels();
        _channelIndex = 0;
        _minView = _maxView = 0.0;
        _minLUT  = _maxLUT  = 0.0;
        _minData = _maxData = 0.0;
        _minDataCurrent = _maxDataCurrent = 0.0;
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
        update(autoRange, _combineChannels);
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
        update(_autoRange, combineChannels);
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
     * Returns whether or not histogram has channels.
     * 
     * @return 
     */
    public boolean hasChannels() {
        return _channel.length > 1;
    }

    /*
     * Helper function to update autoranging or channel combination.
     */
    private void update(boolean autoRange, boolean combineChannels) {
        if (_autoRange != autoRange || _combineChannels != combineChannels) {
            double minView;
            double maxView;
            double minLUT;
            double maxLUT;
            
            _autoRange       = autoRange;
            _combineChannels = combineChannels;
            
            if (_autoRange) {
                if (_combineChannels) {
                    // LUT and view bounded by data for all channels
                    minLUT = _minData;
                    maxLUT = _maxData;
                    
                    minView = minLUT;
                    maxView = maxLUT;
                }
                else {
                    // LUT is bounded by data for current channel
                    minLUT = _minDataCurrent;
                    maxLUT = _maxDataCurrent;
                    
                    if (_displayChannels) {
                        minView = _minData;
                        maxView = _maxData;
                    }
                    else {
                        minView = minLUT;
                        maxView = maxLUT;
                    }
                }
                
                // did anything really change?
                if (_minView != minView || _maxView != maxView
                        || _minLUT != minLUT || _maxLUT != maxLUT) {
                    _minView = minView;
                    _maxView = maxView;
                    _minLUT = minLUT;
                    _maxLUT = maxLUT;

                    // update listener, if any
                    if (null != _listener) {
                        _listener.minMaxChanged(minView, maxView, minLUT, maxLUT);
                    }
                }
            }
        }
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
        redisplay();
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
    //TODO these recalcHistogram events need to be synchronized so that they
    // don't step on other ways to change min/maxLUT/View
    // Perhaps this class should have a synch object that also has a getter.
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
        if (1 < _channel.length && (_displayChannels || _combineChannels)) {
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
            maxData = maxDataCurrent = minMaxData[1];
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
        
        _minData = minData;
        _maxData = maxData;
        
        _minDataCurrent = minDataCurrent;
        _maxDataCurrent = maxDataCurrent;

        if (null != _listener) {
            _listener.minMaxChanged(_minView, _maxView, _minLUT, _maxLUT);
        }

        return new double[] { _minLUT, _maxLUT };
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
