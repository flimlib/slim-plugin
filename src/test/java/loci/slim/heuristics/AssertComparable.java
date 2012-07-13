/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.heuristics;

import static org.junit.Assert.*;

/**
 *
 * @author aivar
 */
public class AssertComparable {
    
    public static void assertArrayComparable(double[] expResult, double[] result, double margin) {
        String error = null;
        if (expResult.length != result.length) {
            fail("array lengths differed, expected.length=" + expResult.length + " actual.length=" + result.length);
        }
        for (int i = 0; i < result.length; ++i) {
            // calculate percentage
            double percent = 100.0 * expResult[i] / result[i];
            // convert to positive percentage difference
            if (percent > 100.0) {
                percent -= 100.0;
            }
            else {
                percent = 100.0 - percent;
            }
            // check for failure
            if (percent > margin) {
                fail("arrays first differed (by " + percent + "% > " + margin + "%) at element [" + i + "]; expected:<" + expResult[i] + "> but was:<" + result[i] + ">");
            }
        }
    }  
}
