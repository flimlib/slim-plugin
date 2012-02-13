/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.heuristics;

/**
 * This class contains all estimates and rules of thumb.
 *
 * @author Aivar Grislis
 */
public class Estimator implements IEstimator {
    private static final double[] DEFAULT_SINGLE_EXP_PARAMS  = { 0.0, 0.5, 100.0, 0.5 };                      // 0 Z A T
    private static final double[] DEFAULT_DOUBLE_EXP_PARAMS  = { 0.0, 0.5, 50.0, 0.5, 50, 0.25 };             // 0 Z A1 T1 A2 T2
    private static final double[] DEFAULT_TRIPLE_EXP_PARAMS  = { 0.0, 0.5, 40.0, 0.5, 30.0, 0.25, 30, 0.10 }; // 0 Z A1 T1 A2 T2 A3 T3
    private static final double[] DEFAULT_STRETCH_EXP_PARAMS = { 0.0, 0.5, 100.0, 0.5, 0.5 };                 // 0 Z A T H
    
    @Override
    public int getStart(int bins) {
        return bins / 4;
    }
    
    @Override
    public int getStop(int bins) {
        return 5 * bins / 6;
    }
    
    @Override
    public int getThreshold() {
        return 1000;
    }
    
    @Override
    public double getChiSquareTarget() {
        return 1.5;
    }
    
    @Override
    public double[] getParameters(int components, boolean stretched) {
        double[] parameters;
        if (stretched) {
            // Z T A H
            parameters = DEFAULT_STRETCH_EXP_PARAMS;
        }
        else {
            switch (components) {
                case 1:
                    // Z T A
                    parameters = DEFAULT_SINGLE_EXP_PARAMS;
                    break;
                case 2:
                    // Z T1 A1 T2 A2
                    parameters = DEFAULT_DOUBLE_EXP_PARAMS;
                    break;
                case 3:
                default:
                    parameters = DEFAULT_TRIPLE_EXP_PARAMS;
                    break;
            }
        }
        return parameters;
    }
    
}
