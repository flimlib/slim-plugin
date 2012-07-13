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
        System.out.println("Excitation.scale, start " + start + " stop " + stop + " base " + base);
        System.out.println("timeInc " + timeInc + " bins " + bins);

        System.out.print("{");
        for (double d : decay) {
            System.out.print(" " + d + ",");
        }
        System.out.println("}");
        
        
        int startIndex = (int) Math.ceil(start / timeInc);
        if (startIndex < 0) {
            startIndex = 0;
        }
        int stopIndex = (int) Math.floor(stop / timeInc) + 1;
        if (stopIndex > bins) {
            stopIndex = bins;
        }
        System.out.println("startIndex " + startIndex + " stopIndex " + stopIndex);
        
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

        System.out.print("{");
        for (double val : values) {
            System.out.print(" " + val + ",");
        }
        System.out.println("}");
        return values;
    }
    
}
