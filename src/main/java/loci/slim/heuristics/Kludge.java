/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.heuristics;

/**
 *
 * @author aivar
 */
public class Kludge {
    private static final boolean kludge = true;
    
    public static int kludgeStart(double value, double inc, int bin) {
        if (kludge) {
            int newBin = (int) Math.ceil(value / inc);
            if (bin != newBin) {
                System.out.println("Kludge: Old start bin was " + bin + " go with " + newBin);
                bin = newBin;
            }
        }
        
        return bin;
    }
    
    public static int kludgeEnd(double value, double inc, int bin) {
        if (kludge) {
            int newBin = (int) Math.floor(value / inc) + 1;
            if (bin != newBin) {
                System.out.println("Kludge: Old end bin was " + bin + " go with " + newBin);
                bin = newBin;
            }
        }
        return bin;
    }
    
}
