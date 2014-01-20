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

package loci.slim;

import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Utility class for working with ImgLib2 images.
 *
 * @author Barry DeZonia
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public class ImageUtils {

	// -- ImageUtils methods --

	public static ImgPlus<DoubleType> create(String title, long... dims) {
		final ImgPlus<DoubleType> img =
			new ImgPlus<DoubleType>(PlanarImgs.doubles(dims));
		img.setName(title);
		return img;
	}

	public static long getWidth(final ImgPlus<?> img) {
		return getDimSize(img, Axes.X, 0);
	}

	public static long getHeight(final ImgPlus<?> img) {
		return getDimSize(img, Axes.Y, 1);
	}

	public static long getNChannels(final ImgPlus<?> img) {
		return getDimSize(img, Axes.CHANNEL, 2);
	}

	public static long getNSlices(final ImgPlus<?> img) {
		return getDimSize(img, Axes.Z, 3);
	}

	public static long getNFrames(final ImgPlus<?> img) {
		return getDimSize(img, Axes.TIME, 4);
	}

	public static long getDimSize(final ImgPlus<?> img, final AxisType axisType) {
		return getDimSize(img, axisType, -1);
	}

	// -- Helper methods --

	private static long getDimSize(final ImgPlus<?> img, final AxisType axisType,
		final int defaultIndex)
	{
		final int axisIndex = img.dimensionIndex(axisType);
		return axisIndex < 0 ? defaultIndex : img.dimension(axisIndex);
	}

}
