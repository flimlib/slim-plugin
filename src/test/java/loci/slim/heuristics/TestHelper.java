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
