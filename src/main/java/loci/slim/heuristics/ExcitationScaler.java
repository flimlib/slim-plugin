/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.heuristics;

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
    public static double[] scale(double[] decay, double start, double stop, double base, double timeInc, int bins) {

        int startIndex = (int) Math.ceil(start / timeInc);
        if (startIndex < 0) {
            startIndex = 0;
        }
        int stopIndex = (int) Math.floor(stop / timeInc) + 1;
        if (stopIndex > bins) {
            stopIndex = bins;
        }
        
        if (stopIndex <= startIndex) {
            return null;
        }
        
        double[] values = new double[stopIndex - startIndex];
        double scaling = 0.0;
        for (int i = 0; i < values.length; ++i) {
            values[i] = Math.max(decay[startIndex + i] - base, 0.0);
            scaling += values[i];
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
            
            System.out.println("double start = " + start + ";");
            System.out.println("double stop = " + stop + ";");
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
