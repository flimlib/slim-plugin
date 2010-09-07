/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.colorizer;

import java.awt.Color;

/**
 *
 * @author aivar
 */
public interface IColorize {

    /**
     * Expresses the value as a Color interpolated between the
     * start and stop values.
     *
     * @param start
     * @param stop
     * @param value
     * @return
     */
    public Color colorize(double start, double stop, double value);

}
