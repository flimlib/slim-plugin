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

import loci.slim.mask.IMaskGroup;
import loci.slim.mask.IMaskNode;
import loci.slim.mask.IMaskNodeListener;
import loci.slim.mask.Mask;
import loci.slim.mask.MaskGroup;
import loci.slim.mask.MaskNode;

/**
 * This class shadows a channel in a stack for a displayed image.  If the image
 * has only two dimensions there would be only one of these per HistogramData.
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class HistogramDataNode {
    private double[][] _values;
    private IMaskNode _maskNode;
    private Mask _selfMask;
    private Mask _otherMask;
    private Mask _totalMask;

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
    public HistogramDataNode(double[][] values) {
        _values = values;
    }
    
    public void setMaskGroup(IMaskGroup maskGroup) {
        _maskNode = new MaskNode(maskGroup, new IMaskNodeListener () {
            public void updateMask(Mask mask) {
                System.out.println("HistogramDataNode.setMaskGroup IMaskNodeListener.updateMask " + mask);
                setOtherMask(mask);
                //TODO redraw, here or in setOtherMask()
            }
        });
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
     * Gets the current other mask, which masks pixels hidden by others.
     * 
     * @return mask or null
     */
    public Mask getOtherMask() {
        return _otherMask;
    }

    /**
     * Sets the current other mask, which masks pixels hidden by others.
     * 
     * @param mask or null
     */
    public void setOtherMask(Mask mask) {
        System.out.print("incoming other mask " + mask);
        if (null != mask) System.out.print(" excludes " + countBits(mask.getBits()));
        System.out.println();
        
        _otherMask = mask;
        if (null == _selfMask) {
            _totalMask = mask;
        }
        else {
            _totalMask = _selfMask.add(mask);
        }
        
        System.out.print("I am " + this + " setOtherMask, total mask " + _totalMask);
        if (null != _totalMask) System.out.print(" excludes " + countBits(_totalMask.getBits()));
        System.out.println();
    }

    /**
     * Gets the current self mask, which masks pixels hidden by self.
     * 
     * @return mask or null
     */
    public Mask getSelfMask() {
        return _selfMask;
    }

    /**
     * Sets the current self mask, which masks pixels hidden by self.
     * 
     * @param mask or null
     */
    public void setSelfMask(Mask mask) {
        _selfMask = mask;
        if (null == mask) {
            _totalMask = _otherMask;
        }
        else {
            _totalMask = mask.add(_otherMask);
        }

        System.out.println("I am " + this + " setSelfMask, total mask excludes " + countBits(_totalMask.getBits()));
    }
    
    public int countBits(boolean[][] bits) {
        int counter = 0;
        if (null != bits) {
            for (int y = 0; y < bits[0].length; ++y) {
                for (int x = 0; x < bits.length; ++x) {
                    if (!bits[x][y]) ++counter;
                }
            }
        }
        return counter;
    }

    /**
     * Finds the actual minimum and maximum values.
     * Called initially and after values change.
     * 
     * This will exclude pixels masked by self or others.
     * 
     * @return array of { min, max }
     */
    public double[] findMinMax() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < _values.length; ++i) {
            for (int j = 0; j < _values[0].length; ++j) {
                if (null == _totalMask || _totalMask.test(i, j)) {
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
        }
        return new double[] { min, max };
    }
    
    /**
     * Creates an array of histogram values based on the current nominal min/max
     * range.
     * 
     * This histogram array should exclude pixels masked by others but not by
     * self.
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
        double inverseBinWidth = bins / (nominalMax - nominalMin);
        for (int i = 0; i < _values.length; ++i) {
            for (int j = 0; j < _values[0].length; ++j) {
                if (null == _otherMask || _otherMask.test(i, j)) {
                    double value = _values[i][j];
                    if (value >= nominalMin && value <= nominalMax) {
                        // assign each value to a bin
                        int bin = (int)((value - nominalMin) * inverseBinWidth);
                        if (bin >= bins) {
                            --bin;
                        }
                        ++results[bin];
                    } 
                }
            }
        }
        return results;
    }

    /**
     * Builds a mask based on which values are within the LUT range and sends it
     * out to peer nodes.
     * 
     * @return 
     */
    public void propagateMask(double minLUT, double maxLUT) {
        boolean masked = false;
        int width = _values.length;
        int height = _values[0].length;
        boolean[][] bits = new boolean[width][height];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Double value = _values[x][y];
                bits[x][y] = false;
                if (!value.isNaN()) {
                    if (minLUT <= value && value <= maxLUT) {
                        // in range, include this pixel
                        bits[x][y] = true;
                    }
                    else {
                        // we did just mask out a pixel
                        masked = true;
                    }
                }
            }
        }
        Mask selfMask = null;
        if (masked) {
            selfMask = new Mask(bits);
        }

        System.out.println("HistogramDataNode.propagateMask " + selfMask);
        setSelfMask(selfMask);
        _maskNode.updateSelfMask(selfMask);
    }

    /**
     * Deletes our mask.
     * 
     */
    public void rescindMask() {
        Mask selfMask = null;
        
        System.out.println("HistogramDataNode.rescindMask");
        setSelfMask(selfMask);
        _maskNode.updateSelfMask(selfMask);
    }
}
