//
// Mask.java
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
    * Neither the names of the ImageJDev.org developers nor the
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

package loci.slim.mask;

import java.util.Collection; 

/**
 * Class for keeping track of exclusion masks.
 * 
 * Similar to a ROI, but these masks are used while examining fitted results.
 *
 * @author Aivar Grislis
 */
public class Mask {
    private boolean[][] _bits;
    
    public Mask(boolean[][] bits) {
        _bits = bits;
    }
    
    public Mask(int width, int height) {
        // create array of FALSE
        _bits = new boolean[width][height];
    }

    /**
     * Gets the boolean switches.
     * 
     * @return 
     */
    public boolean[][] getBits() {
        return _bits;
    }

    /**
     * Sets the boolean switches.
     * 
     * @param bits 
     */
    public void setBits(boolean[][] bits) {
        _bits = bits;
    }

    /**
     * Test whether a given x and y is masked.
     * 
     * @param x
     * @param y
     * @return 
     */
    public boolean test(int x, int y) {
        boolean result = true;
        if (null != _bits) {
            result = _bits[x][y];
        }
        return result;
    }

    /**
     * Sets a masked x and y.
     * 
     * @param x
     * @param y 
     */
    public void set(int x, int y) {
        if (null == _bits) {
            _bits[x][y] = true;
        }
    }

    /**
     * Adds given mask to current mask, generating a new mask.
     * 
     * @param mask
     * @return 
     */
    public Mask add(Mask mask) {
        if (null == mask) {
            return this;
        }
        
        boolean[][] bits = mask.getBits();
        int width = bits[0].length;
        int height = bits.length;
        boolean[][] result = new boolean[width][height];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                result[x][y] = _bits[x][y] && bits[x][y];
            }
        }
        return new Mask(result);
    }
    
    /**
     * Given a collection of masks, adds them all together except for one mask
     * specified to be excluded.
     * 
     * Having this be part of the Mask class hides implementation details.
     * 
     * @param excludedMask
     * @param masks
     * @return mask or null
     */
    public static Mask addOtherMasks(Mask excludedMask, Collection<Mask> masks) {
        Mask returnValue = null;
        if (!masks.isEmpty()) {
            boolean[][] result = null;
            int width = 0;
            int height = 0;
            for (Mask mask : masks) {
                if (null != mask) {
                    if (mask != excludedMask) {
                        boolean[][] addition = mask.getBits();
                        if (null != addition) {
                            // lazy initialization of results
                            if (null == result) {
                                result = mask.getBits().clone();
                                width  = result[0].length;
                                height = result.length;
                            }
                            for (int x = 0; x < width; ++x) {
                                for (int y = 0; y < height; ++y) {
                                    result[x][y] = result[x][y] && addition[x][y];
                                }
                            }
                        }
                    }
                }
            }
            if (null != result) {
                returnValue = new Mask(result);
            }
        }
        else {
            System.out.println("masks is empty");
        }
        return returnValue;
    }
}
