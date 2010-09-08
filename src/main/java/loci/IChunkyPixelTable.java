/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci;

/**
 *
 * @author aivar
 */
public interface IChunkyPixelTable {

    /**
     * Get table size.
     *
     * @return size
     */
    public int size();
    
    /**
     * Get pixel width.
     * 
     * @return width
     */
    public int getWidth();
    
    /**
     * Get pixel height.
     * 
     * @return height;
     */
    public int getHeight();
    
    /**
     * Get table entry at index.
     * 
     * @param index
     * @return ChunkyPixel
     */
    public ChunkyPixel getChunkyPixel(int index);
}
