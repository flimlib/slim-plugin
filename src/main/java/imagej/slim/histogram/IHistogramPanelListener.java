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
    
    /**
     * Sets a new minimum and maximum cursor on the histogram panel.
     * Note that these values are in pixels.
     * 
     * @param min
     * @param max 
     */
    public void setMinMaxLUTPixels(int min, int max);

    /**
     * Called during a mouse drag.  Values in pixels.
     *
     * @param min
     * @param max
     */
    public void dragMinMaxPixels(int min, int max);

    /**
     * Mouse has exited the HistogramPanel.
     */
    public void exited();
}
