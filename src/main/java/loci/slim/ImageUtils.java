/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
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
