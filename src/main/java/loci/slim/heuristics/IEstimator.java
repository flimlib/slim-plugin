/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.heuristics;

/**
 *
 * @author aivar
 */
public interface IEstimator {

    double getChiSquareTarget();

    double[] getParameters(int components, boolean stretched);

    int getStart(int bins);

    int getStop(int bins);

    int getThreshold();
    
}
