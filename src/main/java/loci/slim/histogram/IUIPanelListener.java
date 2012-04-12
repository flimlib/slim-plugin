/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.histogram;

/**
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public interface IUIPanelListener {
    
    /**
     * User has clicked the auto ranging checkbox.
     * 
     * @param autoRange
     */
    public void setAutoRange(boolean autoRange);
    
    /**
     * User has clicked the exclude pixels checkbox.
     * 
     * @param excludePixels
     */
    public void setExcludePixels(boolean excludePixels);
    
    /**
     * User has clicked the combine channels checkbox.
     */
    public void setCombineChannels(boolean combineChannels);

    /**
     * User has clicked the display channels checkbox.
     * 
     * @param displayChannels 
     */
    public void setDisplayChannels(boolean displayChannels);

    /**
     * User has entered new min/max LUT value.
     *
     * @param min
     * @param max
     */
    public void setMinMaxLUT(double min, double max);
}
