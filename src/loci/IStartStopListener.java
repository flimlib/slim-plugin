/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci;

/**
 *
 * @author aivar
 */
public interface IStartStopListener {

    /**
     * Listens for changes to the start and stop indices of the fit.
     *
     * @param start index
     * @param stop index inclusive
     */
    public void setStartStop(int start, int stop);
}
