/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2013, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
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

package loci.slim2.heuristics;

/**
 * This class contains all estimates and rules of thumb.
 *
 * @author Aivar Grislis
 */
public class DefaultEstimator implements Estimator {
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
        return 100;
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
