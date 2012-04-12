/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    public boolean[][] getBits() {
        return _bits;
    }
    
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
     * @param excludedMask
     * @param masks
     * @return 
     */
    public static Mask addOtherMasks(Mask excludedMask, Collection<Mask> masks) {
        boolean[][] result = null;
        int width = 0;
        int height = 0;
        for (Mask mask : masks) {
            if (mask != excludedMask) {
                boolean[][] addition = mask.getBits();
                if (null != addition) {
                    if (null == result) {
                        result = mask.getBits().clone();
                        width = result[0].length;
                        height = result.length;
                    }
                    else {
                        for (int x = 0; x < width; ++x) {
                            for (int y = 0; y < height; ++y) {
                                result[x][y] = result[x][y] && addition[x][y];
                            }
                        }
                    }
                }
            }
        }
        return new Mask(result);
    }
}
