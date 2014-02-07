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

package loci.slim.histogram;

import java.util.Arrays;
import loci.slim.fitting.images.IFittedImage;
import loci.slim.mask.IMaskGroup;
import loci.slim.mask.IMaskNode;
import loci.slim.mask.IMaskNodeListener;
import loci.slim.mask.Mask;
import loci.slim.mask.MaskNode;

/**
 * This class shadows a channel in a stack for a displayed image.  If the image
 * has only two dimensions there would be only one of these per HistogramData.
 *
 * @author Aivar Grislis
 */
public class HistogramDataNode {
	private static final int QUARTILE_MARGIN = 5;
	private static final int IMPOSSIBLE_INDEX = -999;
    private IFittedImage _fittedImage;
    private double[][] _values;
    private IMaskNode _maskNode;
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
     * @param fittedImage
     * @param values 
     */
    public HistogramDataNode(IFittedImage fittedImage, double[][] values) {
        _fittedImage = fittedImage;
        _values = values;
		_otherMask = _totalMask = null;
    }

    /**
     * Assigns a mask group.
     * 
     * @param maskGroup 
     */
    public void setMaskGroup(IMaskGroup maskGroup) {
        // create a new mask node that listens to the group
        _maskNode = new MaskNode(maskGroup, new IMaskNodeListener () {
			// listen for mask changes
			@Override
            public void updateMasks(Mask otherMask, Mask totalMask) {
				_otherMask = otherMask;
				_totalMask = totalMask;
				
                _fittedImage.updateMask(_totalMask);
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
     * Finds the actual minimum and maximum values.
     * Called initially and after values change.
     * 
     * This will exclude pixels masked by self or others.
     * 
     * @return array of { min, max }
     */
    public double[] findMinMax() {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
		
        for (int y = 0; y < _values[0].length; ++y) {
            for (int x = 0; x < _values.length; ++x) {
                if (null == _totalMask || _totalMask.test(x, y)) {
					double value = _values[x][y];
                    if (!Double.isNaN(value)) {
                        if (value < min) {
                            min = value;
                        }
                        if (value > max) {
                            max = value;
                        }
                    } 
                }
            }
        }
		if (min == max) {
			// avoid 'min equals max'
			max = 1.01 * min;
		}
        return new double[] { min, max };
    }
    
    /**
     * Creates an array of histogram values based on the current nominal min/max
     * range.
     * 
     * This histogram array should exclude pixels masked out by others but not 
     * by self.
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
        for (int y = 0; y < _values[0].length; ++y) {
            for (int x = 0; x < _values.length; ++x) {
                if (null == _otherMask || _otherMask.test(x, y)) {
                    double value = _values[x][y];
					if (!Double.isNaN(value)) {
						// assign each value to a bin
						int bin = valueToBin(value, bins, nominalMin, nominalMax);
						if (0 <= bin && bin < bins) {
							++results[bin];
						}
					} 
				}
            }
        }
        return results;
    }

	/**
	 * Finds the quartiles of the histogram distribution.  Uses the total mask,
	 * ignores any pixels masked out by anyone.
	 * 
	 * @param quartiles
	 * @param quartileIndices
	 * @param bins
	 * @param min
	 * @param max 
	 */
	public void findQuartiles(double[] quartiles, int[] quartileIndices,
			int bins, double min, double max) {

		// create an array copy of masked, non-NaN values
		int width = _values.length;
		int height = _values[0].length;
		double[] tmp = new double[width * height];
		int tmpIndex = 0;
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (null == _totalMask || _totalMask.test(x, y)) {
					double value = _values[x][y];
					if (!Double.isNaN(value)) {
					    tmp[tmpIndex++] = value;
					}
				}
			}
		}

		// sort the (partially-filled) values array and calculate quartiles
		Arrays.sort(tmp, 0, tmpIndex);
		
		int lowerTopIndex, upperBottomIndex, index;
		if (tmpIndex % 2 != 0) {
			// odd array size
			
			// take the middle value
			lowerTopIndex = upperBottomIndex = tmpIndex / 2;
			quartiles[1] = tmp[lowerTopIndex];
		}
		else {
			// even array size
			
			// take the mean of middle two values
			lowerTopIndex = tmpIndex / 2;
			upperBottomIndex = lowerTopIndex + 1;
			quartiles[1] = (tmp[lowerTopIndex] + tmp[upperBottomIndex]) / 2;
		}
		
		if (lowerTopIndex % 2 != 0) {
			// odd half size
			
			// take the middle values
			index = lowerTopIndex / 2;
			quartiles[0] = tmp[index];
			
			index = (upperBottomIndex + tmpIndex) / 2;
			quartiles[2] = tmp[index];
		}
		else {
			// even half size
			
			// take the mean of middle two values
			index = lowerTopIndex / 2;
			quartiles[0] = (tmp[index] + tmp[index + 1]) / 2;
			
			index = (upperBottomIndex + tmpIndex) / 2;
			quartiles[2] = (tmp[index] + tmp[index + 1]) / 2;
		}

		// An earlier, simpler approach:
		//quartiles[0] = tmp[tmpIndex / 4];
		//quartiles[1] = tmp[tmpIndex / 2];
		//quartiles[2] = tmp[3 * tmpIndex / 4];

		// get bin indices for quartile values
		quartileIndices[0] = getQuartileIndex(quartiles[0], bins, min, max);
		quartileIndices[1] = getQuartileIndex(quartiles[1], bins, min, max);
		quartileIndices[2] = getQuartileIndex(quartiles[2], bins, min, max);
		
		// if quartile indices are too close together don't show quartiles
		if (quartileIndices[1] - quartileIndices[0] < QUARTILE_MARGIN ||
				quartileIndices[2] - quartileIndices[1] < QUARTILE_MARGIN) {
			quartileIndices[0] = quartileIndices[1] = quartileIndices[2] = IMPOSSIBLE_INDEX;
		}
	}

	/**
	 * Given an array and inclusive start/stop indices, computes median value.
	 * 
	 * @param values
	 * @param start
	 * @param stop
	 * @return 
	 */
	private double getMedian(double[] values, int start, int stop) {
		return 0.0;
	}

	/**
	 * Given a quartile value, looks up the bin index.
	 * 
	 * @param value
	 * @param bins
	 * @param min
	 * @param max
	 * @return 
	 */
	private int getQuartileIndex(double value, int bins, double min, double max) {
		int index = valueToBin(value, bins, min, max);
		if (index < 0 || index >= bins) {
			index = IMPOSSIBLE_INDEX;
		}
		return index;
	}
	
	private int valueToBin(double value, int bins, double nominalMin, double nominalMax) {
		int bin;
		if (nominalMin != nominalMax) {
			if (value != nominalMax) {
				// convert in-range values to 0.0..1.0
				double temp = (value - nominalMin) / (nominalMax - nominalMin);
				
				// note multiply by bins, not (bins - 1)
				// note floor is needed so that small negative values go to -1
				bin = (int) Math.floor(temp * bins);
			}
			else {
				// value == max, special case, otherwise 1.0 * bins results in bins
				bin = bins - 1;
			}
		}
		else {
			// max == min, degenerate case
			bin = bins / 2;
		}
		return bin;
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
        _maskNode.updateSelfMask(selfMask);
    }

    /**
     * Deletes our mask.
     * 
     */
    public void rescindMask() {
        Mask selfMask = null;
        _maskNode.updateSelfMask(selfMask);
    }
}
