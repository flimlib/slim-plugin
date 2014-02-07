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

package loci.slim.heuristics;

import static org.junit.Assert.*;

/**
 * Improved JUnit double assertations.
 * 
 * @author Aivar Grislis
 */
public class TestHelper {

    /**
     * Checks whether the percentage difference is within margin.
     * 
     * @param expResult
     * @param result
     * @param margin 
     */
    public static void assertComparable(double expResult, double result, double margin) {
        double percent = percentageDifference(expResult, result);
        if (percent > margin) {
            fail("results differed (by " + percent + "& > " + margin + "%); expected:<" + expResult + "> but was:<" + result + ">");
        }
    }

    /**
     * JUnit does provide a AssertEquals method to compare double arrays, i.e.:<p>
     *   "assertArrayEquals(expResult, result, 0.001)"<p>
     * Instead of taking a fixed maximum different this takes a percentage
     * difference.
     * 
     * @param expResult
     * @param result
     * @param margin 
     */
    public static void assertArrayComparable(double[] expResult, double[] result, double margin) {
        if (expResult.length != result.length) {
            fail("array lengths differed, expected.length=" + expResult.length + " actual.length=" + result.length);
        }
        for (int i = 0; i < result.length; ++i) {
            double percent = percentageDifference(expResult[i], result[i]);
            if (percent > margin) {
                fail("arrays first differed (by " + percent + "% > " + margin + "%) at element [" + i + "]; expected:<" + expResult[i] + "> but was:<" + result[i] + ">");
            }
        }
    }
    
    private static double percentageDifference(double expResult, double result) {
        // calculate percentage
        double percent = 100.0 * expResult / result;
        
        // convert to positive percentage difference
        if (percent > 100.0) {
            percent -= 100.0;
        }
        else {
            percent = 100.0 - percent;
        }
        
        return percent;
    }
}
