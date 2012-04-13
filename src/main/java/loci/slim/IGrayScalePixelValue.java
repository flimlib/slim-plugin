/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim;

/**
 *
 * @author aivar
 */
public interface IGrayScalePixelValue {
    
    /**
     * Gets a grayscale pixel value.
     *
     * @param channel
     * @param x
     * @param y
     * @return unsigned byte expressed as an integer, 0...255
     */
    public int getPixel(int channel, int x, int y);
}
