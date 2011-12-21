/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

/**
 * This class is used to assign and test invalid double values.
 * 
 * Note that "double crap = Double.NaN; if (Double.NaN == crap) System.out.println("OK");"
 * won't print!
 *
 * Just a temporary experimental class that will go away.
 * 
 * @author aivar
 */
public class InvalidDouble {
    private static final Double invalidDouble = Double.NaN;
    
    public static Double value() {
        return invalidDouble;
    }
    
    public static boolean isValue(Double value) {
        if (Double.isNaN(invalidDouble)) {
            return Double.isNaN(value);
        }
        else if (Double.isInfinite(invalidDouble)) {
            return Double.isInfinite(value);
        }
        else {
            return value == invalidDouble;
        }
    }
}
