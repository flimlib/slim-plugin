//
// HistogramDataChannel.java
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

/**
 * This class shadows a channel in a stack for a displayed image.  If the image
 * has only two dimensions there would be only one of these per HistogramData.
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class HistogramDataChannel {
    private double[][] _values;
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
    }

    /**
     * Gets the values array.
     * 
     * @return 
     */
    public double[][] getValues() {
        return _values;
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
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < _values.length; ++i) {
            for (int j = 0; j < _values[0].length; ++j) {
                if (!Double.isNaN(_values[i][j])) {
                    if (_values[i][j] < min) {
                        min = _values[i][j];
                    }
                    if (_values[i][j] > max) {
                        max = _values[i][j];
                    }
                }
            }
        }
        return new double[] { min, max };
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
