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
import net.imglib2.RandomAccess;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
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
	public static Dataset convert(final DatasetService datasetService, final Dataset dataset, final int lifetimeDimension, final int factor) {
		ImgPlus img = dataset.getImgPlus();
		final int bins = (int) img.dimension(lifetimeDimension);
		
		// want same dimensions & axes except without lifetime
		final long[] dimensions = deleteDimension(img, lifetimeDimension);
		final AxisType[] axes = deleteAxisType(img, lifetimeDimension);

		// create grayscale image
		final int bpp = 16;
		final boolean signed = false;
		final boolean floating = false;
		final Dataset returnValue = datasetService.create(dimensions, dataset.getName(), axes, bpp, signed, floating);
		
		// iterate through grayscale image
		final double[] decay = new double[bins];
		final ImgPlus imgPlus = returnValue.getImgPlus();
		final Cursor<? extends RealType<?>> grayscaleCursor = imgPlus.localizingCursor();
		final RandomAccess<? extends RealType<?>> decayRandomAccess = dataset.getImgPlus().randomAccess();
		final long[] position = new long[dimensions.length];
		while (grayscaleCursor.hasNext()) {
			grayscaleCursor.fwd();
			grayscaleCursor.localize(position);
			getDecay(decayRandomAccess, decay, position, lifetimeDimension);
			int summed = sum(decay, factor);
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
	private static long[] deleteDimension(ImgPlus img, int dimension) {
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
	private static AxisType[] deleteAxisType(ImgPlus img, int dimension) {
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
	private static void getDecay(RandomAccess<? extends RealType<?>> randomAccess, double[] decay, long[] position, int dimension) {
		long[] expandedPosition = expandPosition(position, dimension);
		for (int i = 0; i < decay.length; ++i) {
			expandedPosition[dimension] = i;
			//dumpPosition(expandedPosition);
			randomAccess.setPosition(expandedPosition);
			decay[i] = randomAccess.get().getRealDouble();
		}
	}
	
	private static void dumpPosition(long[] position) {
		for (long p : position) {
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
	private static int sum(double[] decay, int factor) {
		long returnValue = 0;
		for (double d : decay) {
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
	private static long[] expandPosition(long[] position, int dimension) {
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
