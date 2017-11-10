/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim.histogram;

import java.awt.Color;
import java.awt.image.IndexColorModel;

/**
 * This class fixes a problem in the way ImageJ assigns palette colors to
 * FloatProcessor images. Basically a FloatProcessor is converted to a 8-bit
 * image using the assigned minimum and maximum values. Anything below min or
 * above max gets scrunched into the 0 or 255 value. This class supports viewing
 * FloatProcessor images with a palette so that values below min or above max
 * are displayed in different colors. The key to this is to add the below min
 * and above max colors to the palette. Then you essentially wind up with a 254
 * color palette to display a range of FloatProcessor values.
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
	 */
	public static IndexColorModel fixIndexColorModel(IndexColorModel colorModel,
		final Color below, final Color above)
	{
		// get the RGB colors for this color model
		final byte[] reds = new byte[NATIVE_SIZE];
		final byte[] greens = new byte[NATIVE_SIZE];
		final byte[] blues = new byte[NATIVE_SIZE];
		colorModel.getReds(reds);
		colorModel.getBlues(blues);
		colorModel.getGreens(greens);

		// make the first entry the below color and the last the above color
		reds[0] = (byte) below.getRed();
		greens[0] = (byte) below.getGreen();
		blues[0] = (byte) below.getBlue();

		reds[NATIVE_SIZE - 1] = (byte) above.getRed();
		greens[NATIVE_SIZE - 1] = (byte) above.getGreen();
		blues[NATIVE_SIZE - 1] = (byte) above.getBlue();

		// make a new color model
		colorModel = new IndexColorModel(8, NATIVE_SIZE, reds, greens, blues);
		return colorModel;
	}

	/**
	 * Given a min and max specification for a 254-color palette, turns it into a
	 * 256-color palette min and max. Values below 254-color min are colored with
	 * below color and values above 254-color max are colored with above color.
	 *
	 */
	public static double[] adjustMinMax(final double min, final double max) {
		final double adjust = (max - min) / ADJUSTED_SIZE;

		// TODO ARG ueed ADJUSTED_SIZE + 1 as a kludge: it made more black dots
		// TODO ARG having - 1 appears to have the same result!
		// TODO ARG tried + or - 0.5
		return new double[] { min - adjust, max + adjust };
	}

	/**
	 * Gets the adjusted palette size.
	 *
	 */
	public static int getSize() {
		return ADJUSTED_SIZE;
	}
}
