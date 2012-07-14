/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
