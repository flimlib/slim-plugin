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

package loci.slim2.decay;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;

/**
 * Utility class to convert lifetime images to grayscale by summing the photons.
 *
 * @author Aivar Grislis
 */
public class DecayDatasetUtility {

	/**
	 * Converts lifetime image to grayscale.
	 *
	 * @param datasetService
	 * @param dataset
	 * @param lifetimeDimension
	 * @return grayscale version
	 */
	public static Dataset convert(final DatasetService datasetService,
		final Dataset dataset, final int lifetimeDimension, final int factor)
	{
		final ImgPlus img = dataset.getImgPlus();
		final int bins = (int) img.dimension(lifetimeDimension);

		// want same dimensions & axes except without lifetime
		final long[] dimensions = deleteDimension(img, lifetimeDimension);
		final AxisType[] axes = deleteAxisType(img, lifetimeDimension);

		// create grayscale image
		final int bpp = 16;
		final boolean signed = false;
		final boolean floating = false;
		final Dataset returnValue =
			datasetService.create(dimensions, dataset.getName(), axes, bpp, signed,
				floating);

		// iterate through grayscale image
		final double[] decay = new double[bins];
		final ImgPlus imgPlus = returnValue.getImgPlus();
		final Cursor<? extends RealType<?>> grayscaleCursor =
			imgPlus.localizingCursor();
		final RandomAccess<? extends RealType<?>> decayRandomAccess =
			dataset.getImgPlus().randomAccess();
		final long[] position = new long[dimensions.length];
		while (grayscaleCursor.hasNext()) {
			grayscaleCursor.fwd();
			grayscaleCursor.localize(position);
			getDecay(decayRandomAccess, decay, position, lifetimeDimension);
			final int summed = sum(decay, factor);
			grayscaleCursor.get().setReal(summed);
		}
		return returnValue;
	}

	/**
	 * Deletes a dimension at given dimensional index.
	 *
	 * @param dimensions
	 * @param dimension
	 * @return
	 */
	private static long[] deleteDimension(final ImgPlus img, final int dimension)
	{
		final long[] returnValue = new long[img.numDimensions() - 1];
		int i = 0;
		for (int j = 0; j < img.numDimensions(); ++j) {
			if (j != dimension) {
				returnValue[i++] = img.dimension(j);
			}
		}
		return returnValue;
	}

	/**
	 * Deletes an {@link AxisType} at given dimensional index.
	 *
	 * @param axes
	 * @param dimension
	 * @return
	 */
	private static AxisType[] deleteAxisType(final ImgPlus<?> img,
		final int dimension)
	{
		final AxisType[] returnValue = new AxisType[img.numDimensions() - 1];
		int i = 0;
		for (int j = 0; j < img.numDimensions(); ++j) {
			if (j != dimension) {
				returnValue[i++] = img.axis(j).type();
			}
		}
		return returnValue;
	}

	/**
	 * Gets the lifetime decay histogram.
	 *
	 * @param randomAccess
	 * @param decay
	 * @param position
	 * @param dimension
	 */
	private static void getDecay(
		final RandomAccess<? extends RealType<?>> randomAccess,
		final double[] decay, final long[] position, final int dimension)
	{
		final long[] expandedPosition = expandPosition(position, dimension);
		for (int i = 0; i < decay.length; ++i) {
			expandedPosition[dimension] = i;
			// dumpPosition(expandedPosition);
			randomAccess.setPosition(expandedPosition);
			decay[i] = randomAccess.get().getRealDouble();
		}
	}

	private static void dumpPosition(final long[] position) {
		for (final long p : position) {
			System.out.print(" " + p);
		}
		System.out.println();
	}

	/**
	 * Sums up the decay photons at a pixel.
	 *
	 * @param decay
	 * @param factor
	 * @return
	 */
	private static int sum(final double[] decay, final int factor) {
		long returnValue = 0;
		for (final double d : decay) {
			returnValue += (d / factor);
		}
		if (returnValue > Integer.MAX_VALUE) {
			returnValue = Integer.MAX_VALUE;
		}
		return (int) returnValue;
	}

	/**
	 * Expands the position at a given dimensional index.
	 *
	 * @param position
	 * @param dimension
	 * @return
	 */
	private static long[] expandPosition(final long[] position,
		final int dimension)
	{
		final long[] returnValue = new long[position.length + 1];
		int i = 0;
		for (int j = 0; j < position.length; ++j) {
			if (j == dimension) {
				// make a space
				++i;
			}
			returnValue[i++] = position[j];
		}
		return returnValue;
	}
}
