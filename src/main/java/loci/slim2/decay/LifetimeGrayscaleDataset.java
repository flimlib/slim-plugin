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
import net.imglib2.type.numeric.RealType;

/**
 * Builds a grayscale version of the lifetime decay image.
 * 
 * @author Aivar Grislis
 */
public class LifetimeGrayscaleDataset {
	private final Dataset grayscaleDataset;
	private final long[] maxPosition;

	/**
	 * Constructor.
	 * 
	 * @param datasetService
	 * @param lifetimeDatasetWrapper
	 * @param bins
	 */
	public LifetimeGrayscaleDataset(DatasetService datasetService, LifetimeDatasetWrapper lifetimeDatasetWrapper) {
		// create grayscale image
		final long[] dimensions = lifetimeDatasetWrapper.getDims();
		final String name = lifetimeDatasetWrapper.getDataset().getName();
		final AxisType[] axes = lifetimeDatasetWrapper.getAxes();
		final int bpp = 32;
		final boolean signed = true;
		final boolean floating = false;
		grayscaleDataset = datasetService.create(dimensions, name, axes, bpp, signed, floating);

		// iterate through grayscale image
		final ImgPlus imgPlus = grayscaleDataset.getImgPlus();
		final Cursor<? extends RealType<?>> grayscaleCursor = imgPlus.localizingCursor();
		final long[] position = new long[dimensions.length];
		int maxSummed = Integer.MIN_VALUE;
		maxPosition = new long[dimensions.length];
		int binSize = 0;
		while (grayscaleCursor.hasNext()) {
			grayscaleCursor.fwd();
			grayscaleCursor.localize(position);

			final int summed = lifetimeDatasetWrapper.getSummedDecay(binSize, position);
			grayscaleCursor.get().setReal(summed);

			// keep track of brightest pixel in first plane
			if (inFirstPlane(position)) {
				if (summed > maxSummed) {
					maxSummed = summed;
					System.arraycopy(position, 0, maxPosition, 0, position.length);
				}
			}
		}
	}

	/**
	 * Gets the associated dataset.
	 * 
	 * @return 
	 */
	public Dataset getDataset() {
		return grayscaleDataset;
	}

	/**
	 * Reports the full position of the brightest pixel in the first XY plane.
	 * 
	 * @return 
	 */
	public long[] getBrightestPixel() {
		return maxPosition;
	}

	/**
	 * Reports whether this position is in the first XY plane.
	 * 
	 * @param position
	 * @return 
	 */
	private boolean inFirstPlane(long[] position) {
		for (int i = 2; i < position.length; ++i) {
			if (0L != position[i]) {
				return false;
			}
		}
		return true;
	}
}
