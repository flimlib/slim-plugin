//
// FitterEstimator.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim.heuristics;

import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.curvefitter.IFitterEstimator;

/**
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class FitterEstimator implements IFitterEstimator {
    private static final int BINS = 256;
    private static final boolean oldWay = false;

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
        //System.out.println("FitterEstimator.getEstimateA " + yCount.length + " " + start + " " + stop);
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
        int returnValue = 0;
        if (oldWay) {
            returnValue = (int) Math.floor(value / inc) + 1;
            if (returnValue < 0) {
                System.out.println("endValueToBin bins is negative!!:" + returnValue);
            }
            else if (returnValue >= BINS) {
                System.out.println("endValueToBin bins is >= BINS " + returnValue);
            }
        }
        else {
            returnValue = roundToNearestInteger(value / inc);
        }
        return returnValue;
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
        int returnValue = 0;
        if (oldWay) {
            returnValue = (int) Math.ceil(value / inc);
            if (returnValue < 0) {
                System.out.println("valueToBin returnValue is negative!!: " + returnValue);
            }
            else if (returnValue >= BINS) {
                System.out.println("valueToBin returnValue is > bins!!:" + returnValue);
            }
        }
        else {
            returnValue = roundToNearestInteger(value / inc);
        }
        return returnValue;
    }

    /**
     * Converts bin number to time-based value.  Rounds to four decimal places.<p>
     * Note that 'binToValue' and 'valueToBin' should round-trip.
     * 
     * @param bin
     * @param inc
     * @return 
     */
    @Override
    public double binToValue(int bin, double inc) {
        double returnValue = 0;
        if (oldWay) {
            returnValue = bin * inc;
        }
        else {
            returnValue = roundToDecimalPlaces(bin * inc, 4);
        }
        return returnValue;
    }
 
    @Override
    public double roundToDecimalPlaces(double value, int decimalPlaces) {
        double decimalTerm = Math.pow(10.0, decimalPlaces);
        int tmp = roundToNearestInteger(value * decimalTerm);
        double rounded = (double) tmp / decimalTerm;
        System.out.println("value " + value + " rounds to " + rounded);
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
