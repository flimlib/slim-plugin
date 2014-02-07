/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
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

package loci.slim.heuristics;

import loci.curvefitter.IFitterEstimator;

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
