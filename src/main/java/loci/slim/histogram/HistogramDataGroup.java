//
// HistogramData.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim.histogram;

import loci.slim.fitting.images.IFittedImage;
import loci.slim.mask.IMaskGroup;

/**
 * Keeps an array of HistogramDataNodes for a given image.  Builds the 
 * histogram data as appropriate.  Handles updates as the fitted results are
 * available.  Handles optional auto-ranging.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class HistogramDataGroup {
    private IFittedImage _image;
    private String _title;
    private HistogramDataNode[] _channel;
    private int _channelIndex;
    private boolean _autoRange;
    private boolean _excludePixels;
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
    public HistogramDataGroup(IFittedImage image, String title,
            HistogramDataNode[] channel) {
        _image = image;
        _title = title;
        _channel = channel;
        _autoRange = true;
        _excludePixels = false;
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
     * Sets whether or not we should hide out-of-range pixels.
     * 
     * @param excludePixels 
     */
    public void setExcludePixels(boolean excludePixels) {
        _excludePixels = excludePixels;
    }

    /**
     * Sets whether or not we should hide out-of-range pixels.
     * 
     * @return 
     */
    public boolean getExcludePixels() {
        return _excludePixels;
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
                        _listener.minMaxChanged(this, minView, maxView, minLUT, maxLUT);
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
	 * Gets minimum and maximum extents of the data.
	 * 
	 * @return 
	 */
	public double[] getMinMaxData() {
		return new double[] { _minData, _maxData };
	}
    
    //TODO
    // this is setting exclude pixels on/off once you are viewing the histogram
    // if you click on another image and the histogram shows data for that image
    // that doesn't necessarily trigger mask propagation.
    //
    // Note that this is just setting up pixel masking for one channel.  Shouldn't
    // this pertain to all channels?  (With different mask for each channel).
    // The other UI is not channel-specific, so neither should this checkbox be.
    // When/if we do have different LUTs for each channel this will get even more
    // complicated.
    //
    // Note also that if this is to work during the fit process it gets more
    // complicated.  As more and more pixels are drawn, we need to build newer
    // exclusion masks and propagate them.
    
    public void excludePixels(boolean excludePixels) {
        setExcludePixels(excludePixels);
        if (excludePixels) {
            _channel[_channelIndex].propagateMask(_minLUT, _maxLUT);
        }
        else {
            _channel[_channelIndex].rescindMask();
        }
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
            _listener.minMaxChanged(this, _minView, _maxView, _minLUT, _maxLUT);
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
