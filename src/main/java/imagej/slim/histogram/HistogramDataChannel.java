/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

/**
 * This class shadows a channel in a stack for a displayed image.  If the image
 * is not a stack there would be only one of these.
 *
 * @author aivar
 */
public class HistogramDataChannel {
    private double[][] _values;
    private double _actualMin;
    private double _actualMax;

    /**
     * Constructor, takes the array of values.
     * 
     * @param values 
     */
    public HistogramDataChannel(double[][] values) {
        _values = values;
        _actualMin = _actualMax = 0.0f;
    }

    /**
     * Finds the actual minimum and maximum values.
     * Called initially and after values change.
     * 
     * @return array of { min, max }
     */
    public double[] resetActualMinMax() {
        _actualMin = Double.MAX_VALUE;
        _actualMax = Double.MIN_VALUE;
        for (int i = 0; i < _values.length; ++i) {
            for (int j = 0; j < _values[0].length; ++j) {
                if (_values[i][j] != Double.NaN) {
                    if (_values[i][j] < _actualMin) {
                        _actualMin = _values[i][j];
                    }
                    if (_values[i][j] > _actualMax) {
                        _actualMax = _values[i][j];
                    }
                }
            }
        }
        return new double[] { _actualMin, _actualMax };
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
        double binWidth = bins / (nominalMax - nominalMin);
        for (int i = 0; i < _values.length; ++i) {
            for (int j = 0; j < _values[0].length; ++j) {
                double value = _values[i][j];
                if (value >= nominalMin && value <= nominalMax) {
                    // assign each value to a bin
                    int bin = (int)((value - nominalMin) * binWidth);
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
