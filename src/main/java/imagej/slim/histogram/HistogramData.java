/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

/**
 * Keeps an array of HistogramChannels for a given image.  Builds the 
 * histogram data as appropriate.
 * 
 * @author aivar
 */
public class HistogramData {
    private String _title;
    private HistogramDataChannel[] _channel;
    private int _channelIndex;
    private boolean _auto;
    private boolean _combine;
    private boolean _showAll;
    private double _minView;
    private double _maxView;
    private double _minLUT;
    private double _maxLUT;

    /**
     * Constructor, takes an array of HistogramChannels.
     * 
     * @param channel 
     */
    public HistogramData(String title, HistogramDataChannel[] channel) {
        _title = title;
        _channel = channel;
        _channelIndex = 0;
        _minView = _maxView = 0.0f;
        _minLUT = _maxLUT = 0.0f;
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
     * Gets whether or not histogram should combine all the channels.
     * 
     * @return whether to combine all the channels
     */
    public boolean getCombine() {
        return _combine;
    }

    /**
     * Sets whether or not histogram should combine all the channels.
     * 
     * @param combine 
     */
    public void setCombine(boolean combine) {
        _combine = combine;
    }

    /**
     * Gets whether or not histogram should automatically scale to values.
     * 
     * @return whether automatically scales
     */
    public boolean getAuto() {
        return _auto;
    }
    
    /**
     * Sets whether or not histogram should automatically scale to values.
     * 
     * @param auto whether automatically scales
     */
    public void setAuto(boolean auto) {
        _auto = auto;
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
     * Gets minimum and maximum extents of the LUT.
     * 
     * @return 
     */
    public double[] getMinMaxLUT() {
        return new double[] { _minLUT, _maxLUT };
    }

    /**
     * Sets the current min and max automatically if need be.
     * 
     * @return min and max
     */
    public double[] getMinMax() {
        double[] minMax = null;
        
        if (_auto) {
            if (_combine) {
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;
                
                // calculate actual minimum and maximum for all channels
                for (int i = 0; i < _channel.length; ++i) {
                    minMax = _channel[i].resetActualMinMax();
                    if (minMax[0] < min) {
                        min = minMax[0];
                    }
                    if (minMax[1] > max) {
                        max = minMax[1];
                    }
                }
                minMax = new double[] { min, max };  
            }
            else {
                // calculate actual minimum and maximum for current channel
                minMax = _channel[_channelIndex].resetActualMinMax();
            }
            _minView = _minLUT = minMax[0];
            _maxView = _maxLUT = minMax[1]; //TODO kludgy
        }
        return minMax; //TODO returns null if not automatically ranging
    }
    
    public int[] binValues(int bins) {
        // start new histogram bins           
        int[] bin = new int[bins];
        System.out.println("bin[3] is " + bin[3] + " bin[33] " + bin[33]);
        System.out.println("_minView is " + _minView + " max " + _maxView);
        System.out.println("_minLUT is " + _minLUT + " maxLUT " + _maxLUT);
        
        if (_showAll) {
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
