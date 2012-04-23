//
// ImageUtils.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim;

import mpicbg.imglib.image.Image;

/**
 * Utility class for accessing Image dimensions.
 *
 * @author Barry DeZonia
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public class ImageUtils {

	public static final String X = "X";
	public static final String Y = "Y";
	public static final String Z = "Z";
	public static final String TIME = "Time";
	public static final String CHANNEL = "Channel";

	// -- ImageUtils methods --

	public static int getWidth(final Image<?> img) {
		return getDimSize(img, X, 0);
	}

	public static int getHeight(final Image<?> img) {
		return getDimSize(img, Y, 1);
	}

	public static int getNChannels(final Image<?> img) {
		return getDimSize(img, CHANNEL, 2);
	}

	public static int getNSlices(final Image<?> img) {
		return getDimSize(img, Z, 3);
	}

	public static int getNFrames(final Image<?> img) {
		return getDimSize(img, TIME, 4);
	}

	public static int getDimSize(final Image<?> img, final String dimType) {
		return getDimSize(img, dimType, -1);
	}

	// -- Helper methods --

	/** Converts the given image name back to a list of dimensional axis types. */
	private static String[] decodeTypes(String name) {
		final int lBracket = name.lastIndexOf(" [");
		if (lBracket < 0) return new String[0];
		final int rBracket = name.lastIndexOf("]");
		if (rBracket < lBracket) return new String[0];
		return name.substring(lBracket + 2, rBracket).split(" ");
	}

	private static int getDimSize(final Image<?> img, final String dimType,
		final int defaultIndex)
	{
		final String imgName = img.getName();
		final int[] dimensions = img.getDimensions();
		final String[] dimTypes = decodeTypes(imgName);
		int size = 1;
		if (dimTypes.length == dimensions.length) {
			for (int i = 0; i < dimTypes.length; i++) {
				if (dimType.equals(dimTypes[i])) size *= dimensions[i];
			}
		}
		else {
			// assume default ordering
			if (defaultIndex >= 0 && defaultIndex < dimensions.length) {
				size = dimensions[defaultIndex];
			}
		}
		return size;
	}

}
