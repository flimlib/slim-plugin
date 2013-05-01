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
import net.imglib2.RandomAccess;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

/**
 * Wraps a Dataset that has lifetime information.
 * 
 * @author Aivar Grislis
 */
public class LifetimeDatasetWrapper {
	private static final String LIFETIME = "Lifetime";
	private static final int IMPOSSIBLE_INDEX = -1;
	private final Dataset dataset;
	private final RandomAccess<? extends RealType<?>> randomAccess;
	private int lifetimeIndex;
	private final AxisType[] externalAxes;
	private final long[] internalDimensions;
	private final long[] externalDimensions;
	private final int bins;
	private final int factor;
	private double inc;
	
	/**
	 * Constructor.
	 * 
	 * @param dataset 
	 */
	public LifetimeDatasetWrapper(Dataset dataset) {
		this.dataset = dataset;
		randomAccess = dataset.getImgPlus().randomAccess();
		
		// find lifetime axis
		lifetimeIndex = IMPOSSIBLE_INDEX;
		AxisType[] axes = dataset.getAxes();
		externalAxes = new AxisType[axes.length - 1];
		int i = 0;
		for (int j = 0; j < axes.length; ++j) {
			if (LIFETIME.equals(axes[j].getLabel())) {
				lifetimeIndex = j;
			}
			else {
				externalAxes[i++] = axes[j];
			}
		}
		assert IMPOSSIBLE_INDEX != lifetimeIndex;

		// get internal (with lifetime) and external (no lifetime) dimensions
		internalDimensions = dataset.getDims();
		externalDimensions = new long[internalDimensions.length - 1];
		i = 0;
		for (int j = 0; j < internalDimensions.length; ++j) {
			if (j != lifetimeIndex) {
				externalDimensions[i++] = internalDimensions[j];
			}
		}
		
		bins = (int) internalDimensions[lifetimeIndex];
		
		factor = 1; //TODO how to get this from metadata?
		inc = 1.0; //TODO this too is in meta data
	}

	/**
	 * Return wrapped Dataset.
	 * 
	 * @return 
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Returns external dimensions (i.e. w/o lifetime dimension).
	 * 
	 * @return 
	 */
	public long[] getDims() {
		return externalDimensions;
	}

	/**
	 * Returns external axis types (i.e. w/o lifetime dimension).
	 * @return 
	 */
	public AxisType[] getAxes() {
		return externalAxes;
	}

	/**
	 * Returns number of lifetime bins.
	 * 
	 * @return 
	 */
	public int getBins() {
		return bins;
	}

	/**
	 * Gets bin width as time.
	 * 
	 * @return 
	 */
	public double getTimeIncrement() {
		return inc;
	}

	/**
	 * Gets decay histogram at given location.
	 * 
	 * @param x
	 * @param y
	 * @param tail rest of position
	 * @return 
	 */
	public double[] getDecay(long x, long y, long[] tail) {
		long[] position = new long[tail.length + 2];
		int i = 0;
		position[i++] = x;
		position[i++] = y;
		for (long t : tail) {
			position[i++] = t;
		}
		return getDecay(position);
	}

	/**
	 * Gets decay histogram at given location.
	 * 
	 * @param position
	 * @return 
	 */
	public double[] getDecay(long[] position) {
		return getDecayFromInternalPosition(internalPosition(position));
	}

	/**
	 * Gets summed decay histogram at given location.
	 * 
	 * @param position
	 * @return 
	 */
	public int getSummedDecay(long[] position) {
		double[] decay = getDecay(position);
		long sum = 0;
		for (double d : decay) {
			sum += d;
		}
		// avoid overflow; squash highlights
		if (sum > Integer.MAX_VALUE) {
			sum = Integer.MAX_VALUE;
		}
		return (int) sum;
	}

	/**
	 * Expands external position (without lifetime dimension) to internal
	 * position (with lifetime dimension).
	 * 
	 * @param position
	 * @return 
	 */
	private long[] internalPosition(long[] position) {
		long[] internalPosition = new long[position.length + 1];
		for (int i = 0; i < lifetimeIndex; ++i) {
			internalPosition[i] = position[i];
		}
		for (int i = lifetimeIndex + 1; i < internalPosition.length; ++i) {
			internalPosition[i] = position[i - 1];
		}
		return internalPosition;
	}

	/**
	 * Gets the decay histogram at given location.
	 * 
	 * @param position
	 * @return 
	 */
	private double[] getDecayFromInternalPosition(long[] position) {
		double[] decay = new double[bins];
		for (int i = 0; i < bins; ++i) {
			position[lifetimeIndex] = i;
			randomAccess.setPosition(position);
			decay[i] = randomAccess.get().getRealDouble() / factor;
		}
		return decay;
	}
}
