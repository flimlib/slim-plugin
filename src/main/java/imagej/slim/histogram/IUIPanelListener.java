/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.slim.histogram;

/**
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public interface IUIPanelListener {
    
    /**
     * User has clicked the auto checkbox.
     * 
     * @param auto
     */
    public void setAuto(boolean auto);

    /**
     * User has entered new min or max LUT values.
     *
     * @param min
     * @param max
     */
    public void setMinMaxLUT(double min, double max);
}
