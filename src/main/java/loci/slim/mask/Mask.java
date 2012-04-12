/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.mask;

import java.util.Collection;

/**
 *
 * @author aivar
 */
public class Mask {

    public Mask(Mask mask) {

    }

    public static Mask combineOtherMasks(Mask excludedMask, Collection<Mask> masks) {
        // create compatible mask
        Mask returnMask = new Mask(excludedMask);

        for (Mask mask : masks) {
            
        }
        return null;
    }
}
