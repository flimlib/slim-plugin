/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2013, UW-Madison LOCI
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

package loci.slim2.decay;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import net.imglib2.Cursor;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;
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
