/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.slim.histogram;

/**
 * Interface for a listener to catch changes in the data.
 *
 * @author aivar
 */
public interface IHistogramDataListener {
    public void minMaxChanged(double minView, double maxView,
            double minLUT, double maxLUT);
}
