/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

/**
 * This class shadows a channel in a stack for a displayed image.  If the image
 * has only two dimensions there would be only one of these per HistogramData.
 *
 * @author Aivar Grislis
 */
public class HistogramDataChannel {
    private double[][] _values;
    private double _min;
    private double _max;
    private double _minLUT;
    private double _maxLUT;

    /**
     * Constructor, takes the array of values.
     * 
     * This class keeps a separate array of values, rather than just referring
     * to the image values, for two reasons:
     * 1) If the 'chunky pixel' effect is used to draw the image, histogram
     * values will be temporarily incorrect until image is complete.
     * 2) If the image shows colorized grayscale, a la SPCImage, the image values
     * will be incorrect.
     * 
     * @param values 
     */
    public HistogramDataChannel(double[][] values) {
        _values = values;
        _min = _max = 0.0f;
    }

    /**
     * This class is also a container for minimum and maximum LUT values.  These
     * only apply when the LUT is not autoranging and each channel has its own
     * values.
     * 
     * @return 
     */
    public double[] getMinMaxLUT() {
        return new double[] { _minLUT, _maxLUT };
    }
 
    /**
     * Sets the minimum and maximum LUT values.
     * 
     * @param min
     * @param max 
     */
    public void setMinMaxLUT(double min, double max) {
        _minLUT = min;
        _maxLUT = max;
    }

    /**
     * Finds the actual minimum and maximum values.
     * Called initially and after values change.
     * 
     * @return array of { min, max }
     */
    public double[] findMinMax() {
        _min = Double.MAX_VALUE;
        _max = Double.MIN_VALUE;
        for (int i = 0; i < _values.length; ++i) {
            for (int j = 0; j < _values[0].length; ++j) {
                if (_values[i][j] != Double.NaN) {
                    if (_values[i][j] < _min) {
                        _min = _values[i][j];
                    }
                    if (_values[i][j] > _max) {
                        _max = _values[i][j];
                    }
                }
            }
        }
        return new double[] { _min, _max };
    }
    
    /**
     * Creates an array of histogram values based on the current nominal min/max
     * range.
     * 
     * @param bins number of bins
     * @param nominalMin first value assigned to bin 0
     * @param nominalMax last value assigned to last bin
     * @return histogram array with counts per bin
     */
    public int[] binValues(int bins, double nominalMin, double nominalMax) {
        int[] results = new int[bins];
        for (int i = 0; i < bins; ++i) {
            results[i] = 0;
        }
        //TODOdouble binWidth = bins / (nominalMax - nominalMin); I think this might be losing the last bin //TODO no, still loses the last bin, on gpl1.sdt T1
        for (int i = 0; i < _values.length; ++i) {
            for (int j = 0; j < _values[0].length; ++j) {
                double value = _values[i][j];
                if (value >= nominalMin && value <= nominalMax) {
                    // assign each value to a bin
                    int bin = (int)((value - nominalMin) * bins / (nominalMax - nominalMin)); //TODO binWidth);
                    if (bin >= bins) {
                        --bin;
                    }
                    ++results[bin];
                }
            }
        }
        return results;
    }   
}
