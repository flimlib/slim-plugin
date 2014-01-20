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

package loci.slim.histogram;

import java.awt.Color;
import java.awt.image.IndexColorModel;

/**
 * This class fixes a problem in the way ImageJ assigns palette colors to
 * FloatProcessor images.  Basically a FloatProcessor is converted to a 8-bit
 * image using the assigned minimum and maximum values.  Anything below min or
 * above max gets scrunched into the 0 or 255 value.
 *
 * This class supports viewing FloatProcessor images with a palette so that
 * values below min or above max are displayed in different colors.  The key to
 * this is to add the below min and above max colors to the palette.  Then you
 * essentially wind up with a 254 color palette to display a range of
 * FloatProcessor values.
 *
 * @author Aivar Grislis
 */
public class PaletteFix {
    public static final int NATIVE_SIZE = 256;
    public static final int ADJUSTED_SIZE = 254;

    /**
     * Given a 256-color palette, turns it into a 254-color palette, using the
     * first and last palette entries for the below and above colors.
     *
     * @param colorModel
     * @param below
     * @param above
     * @return
     */
    public static IndexColorModel fixIndexColorModel(IndexColorModel colorModel,
            Color below, Color above) {
        // get the RGB colors for this color model
        byte[] reds   = new byte[NATIVE_SIZE];
        byte[] greens = new byte[NATIVE_SIZE];
        byte[] blues  = new byte[NATIVE_SIZE];
        colorModel.getReds(reds);
        colorModel.getBlues(blues);
        colorModel.getGreens(greens);

        // make the first entry the below color and the last the above color
        reds  [0] = (byte) below.getRed();
        greens[0] = (byte) below.getGreen();
        blues [0] = (byte) below.getBlue();

        reds  [NATIVE_SIZE - 1] = (byte) above.getRed();
        greens[NATIVE_SIZE - 1] = (byte) above.getGreen();
        blues [NATIVE_SIZE - 1] = (byte) above.getBlue();

        // make a new color model
        colorModel = new IndexColorModel(8, NATIVE_SIZE, reds, greens, blues);
        return colorModel;
    }

    /**
     * Given a min and max specification for a 254-color palette, turns it into
     * a 256-color palette min and max.  Values below 254-color min are colored
     * with below color and values above 254-color max are colored with above
     * color.
     * 
     * @param min
     * @param max
     * @return
     */
    public static double[] adjustMinMax(double min, double max) {
        double adjust = (max - min) / ADJUSTED_SIZE;
        
        //TODO ARG ueed ADJUSTED_SIZE + 1 as a kludge: it made more black dots
        //TODO ARG having - 1 appears to have the same result!
        //TODO ARG tried + or - 0.5
        return new double[] { min - adjust, max + adjust };
    }

    /**
     * Gets the adjusted palette size.
     *
     * @return
     */
    public static int getSize() {
        return ADJUSTED_SIZE;
    }
}
