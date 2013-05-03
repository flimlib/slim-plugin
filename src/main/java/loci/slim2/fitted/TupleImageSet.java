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

package loci.slim2.fitted;

import imagej.ImageJ;
import imagej.data.DatasetService;
import java.util.ArrayList;
import java.util.List;
import loci.slim2.SLIMPlugin;
import net.imglib2.meta.AxisType;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Builds a set of tuple images.
 * 
 * In this context a 'tuple' is just a set of values of type T that each
 * describe a different aspect of the same pixel.
 * 
 * @author Aivar Grislis
 */
//TODO ARG need a better name than tuple!!
public class TupleImageSet <T extends RealType<T> & NativeType<T>> {
	private static final int POST_XY_DIMENSION = 2;
	private String name;
	private boolean combined;
	private long[] dimensions;
	private List<TupleDimensionIndex> indices;
	private List<Dataset> list;

	/**
	 * Creates an images set.
	 * 
	 * @param datasetService
	 * @param combined
	 * @param type
	 * @param dimensions
	 * @param name
	 * @param axes
	 * @param indices 
	 */
	public TupleImageSet(DatasetService datasetService, boolean combined, T type, long[] dimensions, String name, AxisType[] axes, List<TupleDimensionIndex> indices) {
		this.combined = combined;
		this.dimensions = dimensions;
		this.name = name;
		this.indices = indices;
		list = new ArrayList<Dataset>();
		for (TupleDimensionIndex index : indices) {
			index.setCombined(combined);
			if (!combined) {
				// create separate dataset for index
				String subName = index.getLabel() + " " + name;
				Dataset dataset = datasetService.create(type, dimensions, subName, axes);
				list.add(dataset);
				index.setRandomAccess(dataset.getImgPlus().randomAccess());
			}
		}
		// combine into one dataset
		if (combined) {
			long[] combinedDimensions = addCombinedDimension(dimensions, indices.size());
			AxisType[] combinedAxes = addCombinedAxis(axes);
			Dataset dataset = datasetService.create(type, combinedDimensions, name, combinedAxes);
			list.add(dataset);
			for (TupleDimensionIndex index : indices) {
				index.setRandomAccess(dataset.getImgPlus().randomAccess());
			}
		}
	}

	/**
	 * Adds a new dimension when combining images into one.
	 * 
	 * @param dimensions
	 * @param dimension
	 * @return 
	 */
	private long[] addCombinedDimension(long[] dimensions, long dimension) {
		long[] combinedDimensions = new long[dimensions.length + 1];
		for (int i = 0; i < combinedDimensions.length; ++i) {
			if (POST_XY_DIMENSION > i) {
				combinedDimensions[i] = dimensions[i];
			}
			else if (POST_XY_DIMENSION == i) {
				combinedDimensions[i] = dimension;
			}
			else {
				combinedDimensions[i] = dimensions[i - 1];
			}
		}
		return combinedDimensions;
	}

	/**
	 * Adds a new axis when combining datasets into one.
	 * 
	 * @param axes
	 * @return 
	 */
	private AxisType[] addCombinedAxis(AxisType[] axes) {
		AxisType[] combinedAxes = new AxisType[axes.length + 1];
		for (int i = 0; i < combinedAxes.length; ++i) {
			if (POST_XY_DIMENSION > i) {
				combinedAxes[i] = axes[i];
			}
			else if (POST_XY_DIMENSION == i) {
				combinedAxes[i] = new CustomAxisType("parameters");
			}
			else {
				combinedAxes[i] = axes[i - 1];
			}
		}
		return combinedAxes;
	}

	/**
	 * Factory method to create set of images.
	 * 
	 * @param dimensions
	 * @param indices
	 * @return 
	 */
	public TupleImageSet create(DatasetService datasetService, boolean combined, T type, long[] dimensions, String name, AxisType[] axes, List<TupleDimensionIndex> indices) {
		return new TupleImageSet(datasetService, combined, type, dimensions, name, axes, indices);
	}

	/**
	 * Gets list of datasets.
	 * 
	 * @return 
	 */
	public List<Dataset> getDatasets() {
		return list;
	}
	
	private void setPixelValue(List<T> values, long[] position) {
		for (TupleDimensionIndex index : indices) {
			index.setPixelValue(values, position);
		}
	}
		
	private void setPixelValue(List<T> values, long[] position, int[] chunkySize) {
		for (TupleDimensionIndex index : indices) {
			index.setPixelValue(values, position, chunkySize);
		}
	}
}
