/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.heuristics;

import ij.IJ;

//TODO remove this class once next version of TRI2 is available.
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
                IJ.log("Kludge: start bin was " + bin + " old version " + newBin);
                //bin = newBin;
            }
        }
        
        return bin;
    }
    
    public static int kludgeEnd(double value, double inc, int bin) {
        if (kludge) {
            int newBin = (int) Math.floor(value / inc) + 1;
            if (bin != newBin) {
                IJ.log("Kludge: end bin was " + bin + " old version " + newBin);
                //bin = newBin;
            }
        }
        return bin;
    }
    
}
