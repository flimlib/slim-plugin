/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.slim2.heuristics;

/**
 * A class with static functions for working with the excitation.
 * 
 * @author Aivar Grislis
 */
public class ExcitationScaler {
    // use to create data for ExcitationScalerTest
    private static final boolean createTestData = false;

    /**
     * This is based on TRCursors.c UpdatePrompt in TRI2.
     * 
     * @param decay
     * @param start
     * @param stop
     * @param base
     * @param timeInc
     * @param bins
     * @return 
     */
    public static double[] scale(double[] decay, int startIndex, int stopIndex, double base, double timeInc, int bins) {

        // stop index is exclusive
        double[] values = new double[stopIndex - startIndex];
        double scaling = 0.0;
        for (int i = 0; i < values.length; ++i) {
			if (startIndex + i < decay.length) {
				values[i] = Math.max(decay[startIndex + i] - base, 0.0);
				scaling += values[i];
			}
			else {
				values[i] = 0.0;
			}
        }
        
        if (0.0 == scaling) {
            return null;
        }
        
        for (int i = 0; i < values.length; ++i) {
            values[i] /= scaling;
        }
        
        // Can be used to generate data to cut & paste into test methods
        if (createTestData) {
            System.out.println("----8<-----");
            System.out.print("double[] decay = {");
            for (int i = 0; i < decay.length; ++i) {
                if (i > 0) {
                    System.out.print(",");
                }
                System.out.print(" " + decay[i]);
            }
            System.out.println(" };");
            
            System.out.println("int startIndex = " + startIndex + ";");
            System.out.println("int stopIndex = " + stopIndex + ";");
            System.out.println("double base = " + base + ";");
            System.out.println("double timeInc " + timeInc + ";");
            System.out.println("int bins = " + bins + ";");
            
            System.out.print("double[] expResult = {");
            for (int i = 0; i < values.length; ++i) {
                if (i > 0) {
                    System.out.print(",");
                }
                System.out.print(" " + values[i]);
            }
            System.out.println(" };");
            System.out.println("-----------");
        }
        
        return values;
    }   
}
