/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.heuristics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test of scaling a prompt to a particular cursor.
 * 
 * @author Aivar Grislis
 */
public class ExcitationScalerTest {
    
    public ExcitationScalerTest() {
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    /**
     * Test of scale method of class ExcitationScaler.
     */
    @org.junit.Test
    public void testScale() {
        System.out.println("scale");
        double[] decay = { 1.0, 2.0, 1.0, 3.0, 2.0, 2.0, 0.0, 0.0, 0.0, 1.0, 4.0, 2.0, 1.0, 1.0, 2.0, 1.0, 2.0, 0.0, 1.0, 0.0, 0.0, 2.0, 1.0, 1.0, 2.0, 1.0, 1.0, 5.0, 9.0, 10.0, 18.0, 17.0, 17.0, 35.0, 37.0, 32.0, 33.0, 28.0, 39.0, 36.0, 29.0, 32.0, 37.0, 38.0, 27.0, 31.0, 30.0, 32.0, 26.0, 29.0, 25.0, 25.0, 25.0, 21.0, 35.0, 23.0, 13.0, 15.0, 21.0, 18.0, 8.0, 16.0, 14.0, 20.0, 12.0, 18.0, 17.0, 17.0, 13.0, 15.0, 14.0, 16.0, 12.0, 18.0, 14.0, 10.0, 8.0, 10.0, 18.0, 7.0, 10.0, 8.0, 11.0, 11.0, 12.0, 10.0, 13.0, 7.0, 15.0, 8.0, 6.0, 10.0, 8.0, 7.0, 9.0, 11.0, 15.0, 6.0, 6.0, 10.0, 3.0, 8.0, 5.0, 7.0, 9.0, 7.0, 5.0, 3.0, 5.0, 4.0, 6.0, 5.0, 6.0, 7.0, 5.0, 8.0, 3.0, 11.0, 5.0, 5.0, 7.0, 10.0, 3.0, 6.0, 11.0, 5.0, 10.0, 3.0, 5.0, 4.0, 7.0, 2.0, 3.0, 3.0, 4.0, 4.0, 4.0, 5.0, 9.0, 8.0, 5.0, 7.0, 5.0, 4.0, 2.0, 9.0, 5.0, 2.0, 3.0, 7.0, 5.0, 4.0, 4.0, 0.0, 3.0, 5.0, 6.0, 7.0, 2.0, 2.0, 0.0, 5.0, 6.0, 1.0, 7.0, 5.0, 5.0, 1.0, 8.0, 4.0, 3.0, 7.0, 3.0, 1.0, 3.0, 2.0, 0.0, 2.0, 9.0, 3.0, 3.0, 3.0, 3.0, 0.0, 3.0, 2.0, 3.0, 4.0, 5.0, 2.0, 1.0, 1.0, 1.0, 2.0, 3.0, 4.0, 2.0, 1.0, 4.0, 2.0, 3.0, 2.0, 4.0, 1.0, 1.0, 6.0, 1.0, 3.0, 0.0, 2.0, 2.0, 3.0, 1.0, 0.0, 1.0, 2.0, 1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 0.0, 2.0, 2.0, 1.0, 0.0, 0.0, 3.0, 3.0, 1.0, 0.0, 2.0, 1.0, 2.0, 2.0, 3.0, 0.0, 2.0, 1.0, 2.0, 2.0, 2.0, 2.0, 0.0, 4.0, 0.0, 2.0, 2.0, 1.0, 1.0, 1.0, 2.0, 0.0, 2.0 };
        double start = 1.318359375; // note that 1.3184 will fail with 63 bin prompt rather than 64!
        double stop = 4.39453125;
        double base = 2.4771;
        double timeInc = 0.048828125;
        int bins = 256;
        double[] expResult = { 0.002342, 0.006054, 0.006982, 0.014407, 0.013479, 0.013479, 0.030185, 0.032041, 0.027400, 0.028328, 0.023688, 0.033897, 0.031113, 0.024616, 0.027400, 0.032041, 0.032969, 0.022760, 0.026472, 0.025544, 0.027400, 0.021832, 0.024616, 0.020904, 0.020904, 0.020904, 0.017191, 0.030185, 0.019047, 0.009766, 0.011623, 0.017191, 0.014407, 0.005126, 0.012551, 0.010694, 0.016263, 0.008838, 0.014407, 0.013479, 0.013479, 0.009766, 0.011623, 0.010694, 0.012551, 0.008838, 0.014407, 0.010694, 0.006982, 0.005126, 0.006982, 0.014407, 0.004198, 0.006982, 0.005126, 0.007910, 0.007910, 0.008838, 0.006982, 0.009766, 0.004198, 0.011623, 0.005126, 0.003270 };
        double[] result = ExcitationScaler.scale(decay, start, stop, base, timeInc, bins);
        
        //JUnit assertArrayEquals is pretty useless:
        //assertArrayEquals(expResult, result, 0.001);
        
        AssertComparable.assertArrayComparable(expResult, result, 1);
    }
}
