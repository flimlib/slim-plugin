/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.cursor;

import loci.curvefitter.IFitterEstimator;

/**
 * This is a class used when doing a fit with an RLD estimate fit combined
 * with a LMA fit.  It allows for some peculiarities of TRI2.
 *
 * @author aivar
 */
public class FitterEstimator implements IFitterEstimator {
    
    @Override
    public boolean usePrompt() {
        // no prompt for RLD estimate fit
        return false;
    }

    @Override
    public int getEstimateStartIndex(double[] yCount, int start, int stop) {
        System.out.println("FitterEstimator.getEstimateStartIndex " + yCount.length + " " + start + " " + stop);
        if (start < 0) {
            start = 0;
        } //TODO ARG patch for an exception
        // start index changes for RLD estimate fit
        int transEstimateStartIndex = findMax(yCount, start, stop);
        return transEstimateStartIndex;
    }

    @Override
    public double getEstimateAValue(double A, double[] yCount, int start, int stop) {
        System.out.println("FitterEstimator.getEstimateA " + yCount.length + " " + start + " " + stop);
        // A parameter estimate changes for RLD estimate fit
        int transEstimateStartIndex = findMax(yCount, start, stop);
        return yCount[transEstimateStartIndex];
    }

    public int findMax(double[] value, int start, int stop) {
        int index = start;
        double max = value[start];
        for (int i = start; i < stop; ++i) {
            if (value[i] > max) {
                max = value[i];
                index = i;
            }
        }
        return index;
    }
}
