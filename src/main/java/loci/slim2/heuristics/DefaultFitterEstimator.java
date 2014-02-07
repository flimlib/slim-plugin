/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim2.heuristics;

import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.curvefitter.IFitterEstimator;

/**
 * Assorted rules of thumb for fitting.
 * 
 * @author Aivar Grislis
 */
public class DefaultFitterEstimator implements IFitterEstimator {

    @Override
    public double getDefaultA() {
        return 1000.0;
    }

    @Override
    public double getDefaultT() {
        return 2.0;
    }

    @Override
    public double getDefaultZ() {
        return 0.0;
    }

    @Override
    public int getEstimateStartIndex(double[] yCount, int start, int stop) {
        //System.out.println("DefaultFitterEstimator.getEstimateStartIndex " + yCount.length + " " + start + " " + stop);
        if (start < 0) {
            start = 0;
        } //TODO ARG patch for an exception
        // start index changes for RLD estimate fit
        int transEstimateStartIndex = findMax(yCount, start, stop);
        return transEstimateStartIndex;
    }

    @Override
    public double getEstimateAValue(double A, double[] yCount, int start, int stop) {
        //System.out.println("DefaultFitterEstimator.getEstimateA " + yCount.length + " " + start + " " + stop);
        // A parameter estimate changes for RLD estimate fit
        int transEstimateStartIndex = findMax(yCount, start, stop);
        return yCount[transEstimateStartIndex];
    }
    
    @Override
    public NoiseModel getEstimateNoiseModel(NoiseModel noiseModel) {
        return NoiseModel.POISSON_FIT;
    }
    
    /**
     * Adjusts the mono exponential triple integral fit estimate for further
     * mono, bi, tri, and stretched exponential fitting.
     * 
     * Based on TRfitting.c from TRI2.  In TRI2 this is all table-driven, using
     * "fitType[i].preEstimateFactors[0]" etc.  Comments below give those table
     * entry values.
     * 
     * @param params
     * @param free
     * @param fitFunction
     * @param A
     * @param tau
     * @param Z 
     */
    @Override
    public void adjustEstimatedParams(double[] params, boolean[] free,
            FitFunction fitFunction, double A, double tau, double Z) {
        switch (fitFunction) {
            case SINGLE_EXPONENTIAL:
                if (free[0]) {
                    params[1] = Z;                // 1.0
                }
                if (free[1]) {
                    params[2] = A;                // 1.0
                }
                if (free[2]) {
                    params[3] = tau;              // 1.0
                }
                break;
            case DOUBLE_EXPONENTIAL:
                if (free[0]) {
                    params[1] = Z;                // 1.0
                }
                if (free[1]) {
                    params[2] = 0.75 * A;         // 0.75
                }
                if (free[2]) {
                    params[3] = tau;              // 1.0
                }
                if (free[3]) {
                    params[4] = 0.25 * A;         // 0.25
                }
                if (free[4]) {
                    params[5] = 0.6666667 * tau;  // 0.
                }
                break;
            case TRIPLE_EXPONENTIAL:
                if (free[0]) {
                    params[1] = Z;                // 1.0
                }
                if (free[1]) {
                    params[2] = 0.75 * A;         // 0.75
                }
                if (free[2]) {
                    params[3] = tau;              // 1.0
                }
                if (free[3]) {
                    params[4] = 1.0 / 6.0 * A;    // 1.0 / 6.0
                }
                if (free[4]) {
                    params[5] = 0.6666667 * tau;  // 0.6666667
                }
                if (free[5]) {
                    params[6] = 1.0 / 6.0 * A;    // 1.0 / 6.0
                }
                if (free[6]) {
                    params[7] = 0.3333333 * tau;  // 0.3333333
                }
                break;
            case STRETCHED_EXPONENTIAL:
                if (free[0]) {
                    params[1] = Z;                // 1.0
                }
                if (free[1]) {
                    params[2] = A;                // 1.0
                }
                if (free[2]) {
                    params[3] = tau;              // 1.0
                }
                if (free[3]) {
                    params[4] = 1.5;              // -1.5
                }
                break;
        }
    }
    
    int findMax(double[] value, int start, int stop) {
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

    public int endValueToBin(double value, double inc) {
        return roundToNearestInteger(value / inc);
    }


    /**
     * Converts time-based value to a bin number.<p>
     * Note that 'valueToBin' and 'binToValue' should round-trip.
     * 
     * @param value
     * @param inc
     * @return 
     */
    @Override
    public int valueToBin(double value, double inc) {
        return roundToNearestInteger(value / inc);
    }

    /**
     * Converts bin number to time-based value.  Rounds to four decimal places.<p>
     * Note that 'binToValue' and 'valueToBin' ought to round-trip.
     * 
     * @param bin
     * @param inc
     * @return 
     */
    @Override
    public double binToValue(int bin, double inc) {
        return roundToDecimalPlaces(bin * inc, 4);
    }
 
    @Override
    public double roundToDecimalPlaces(double value, int decimalPlaces) {
        double decimalTerm = Math.pow(10.0, decimalPlaces);
        int tmp = roundToNearestInteger(value * decimalTerm);
        double rounded = tmp / decimalTerm;
        //System.out.println("value " + value + " rounds to " + rounded);
        return rounded;
    }
    
    private int roundToNearestInteger(double value) {
        if (value < 0.0) {
            value -= 0.5;
        }
        else {
            value += 0.5;
        }
        return (int) value;
    }
}

