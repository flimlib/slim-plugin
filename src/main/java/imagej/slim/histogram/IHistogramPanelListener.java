/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.slim.histogram;

/**
 * Interface for a listener to catch the user moving the cursor on the 
 * histogram panel.
 * 
 * @author Aivar Grislis
 */
public interface IHistogramPanelListener {
    public enum Direction { LEFT, RIGHT };
    
    /**
     * Sets a new minimum and maximum cursor on the histogram panel.
     * Note that these values are in pixels.
     * 
     * @param min
     * @param max 
     */
    public void setMinMax(int min, int max);
    
    public void expand(); //TODO direction
}
