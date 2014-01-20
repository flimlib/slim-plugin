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

package loci.slim2.outputset;

import loci.slim2.histogram.DataHistogramCommand;
import imagej.command.CommandService;
import imagej.data.Dataset;
import imagej.data.DatasetService;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Builds a set of related images.  In these images corresponding pixels
 * describe different attributes measured or computed at the same pixel position.
 * 
 * @author Aivar Grislis
 */
//TODO ARG Views hyperslice and addDimension are similar, but I don't want shared data
//TODO ARG recursive generic not appropriate, just want some kind of common RealType, don't all have to be the same Type
public class OutputSet <T extends RealType<T> & NativeType<T>> {
	private static final int POST_XY_DIMENSION = 2;
	private static final String DIMENSION_LABEL = "parameters";
	private String name;
	private boolean combined;
	private boolean useChannelDimension = true;
	private long[] dimensions;
	private List<OutputSetMember> indices;
	private List<Dataset> list;

	/**
	 * Creates an images set.  This variant uses {@link MemberFormula} which
	 * allows deriving values from a formula.
	 * 
	 * @param commandService
	 * @param datasetService or null if display not required
	 * @param combined whether images should be in a stack
	 * @param useChannelDimension whether to use CHANNEL dimension for combined
	 * @param type underlying Type of images
	 * @param dimensions
	 * @param name
	 * @param axes
	 * @param indices list of information per output index
	 */
	public OutputSet(CommandService commandService, DatasetService datasetService, boolean combined, boolean useChannelDimension, T type, long[] dimensions, String name, AxisType[] axes, List<OutputSetMember> indices) {
		init(commandService, datasetService, combined, useChannelDimension, type, dimensions, name, axes, indices);
	}

	/**
	 * Creates a set of images.  This variant uses an array of value labels.
	 * The index of the label is also used to derive the value.
	 * 
	 * @param commandService
	 * @param datasetService or null if display not required
	 * @param combined whether images should be in a stack
	 * @param useChannelDimension whether to use CHANNEL dimension for combined
	 * @param type underlying Type of images
	 * @param dimensions
	 * @param name
	 * @param axes
	 * @param labels array of names per output index
	 */
	public OutputSet(CommandService commandService, DatasetService datasetService, boolean combined, boolean useChannelDimension, T type, long[] dimensions, String name, AxisType[] axes, String[] labels) {
		init(commandService, datasetService, combined, useChannelDimension, type, dimensions, name, axes, labels);
	}

	/**
	 * Creates a set of images.
	 * 
	 * @param commandService
	 * @param datasetService or null if display not required
	 * @param combined whether images should be in a stack
	 * @param useChannelDimension whether to use CHANNEL dimension for combined
	 * @param type underlying {@link Type} of images
	 * @param dataset sample Dataset of same dimensionality as outputs
	 * @param labels array of names
	 */
	//TODO ARG can't you get T type from the Dataset somehow??
	public OutputSet(CommandService commandService, DatasetService datasetService, boolean combined, boolean useChannelDimension, T type, Dataset dataset, String[] labels) {
		ImgPlus<?> img = dataset.getImgPlus();
		long[] dimensions = new long[img.numDimensions()];
		img.dimensions(dimensions);
		String name = dataset.getName();
		AxisType[] axes = new AxisType[img.numDimensions()];
		for (int i=0; i<axes.length; i++) axes[i] = img.axis(i).type();
		init(commandService, datasetService, combined, useChannelDimension, type, dimensions, name, axes, labels);
	}

	/**
	 * Creates a set of images.
	 * 
	 * @param commandService
	 * @param datasetService or null if display not required
	 * @param combined is true if images should be in a stack
	 * @param type underlying {@link Type} of images
	 * @param dataset sample {@link Dataset} of same dimensionality as output
	 * @param indices list of information per output index
	 */
	public OutputSet(CommandService commandService, DatasetService datasetService, boolean combined, boolean useChannelDimension, T type, Dataset dataset, List<OutputSetMember> indices) {
		ImgPlus<?> img = dataset.getImgPlus();
		long[] dimensions = new long[img.numDimensions()];
		img.dimensions(dimensions);
		String name = dataset.getName();
		AxisType[] axes = new AxisType[img.numDimensions()];
		for (int i=0; i<axes.length; i++) axes[i] = img.axis(i).type();
		init(commandService, datasetService, combined, useChannelDimension, type, dimensions, name, axes, indices);
	}

	/**
	 * Gets list of datasets.
	 * 
	 * @return 
	 */
	public List<Dataset> getDatasets() {
		return list;
	}
	
	/**
	 * Inputs an array of values at a given position.  These values are then used
	 * singly or in some combining formula to yield the output set images pixel
	 * values.
	 * 
	 * @param values
	 * @param position 
	 */	
	public void setPixelValue(double[] values, long[] position) {
		for (OutputSetMember index : indices) {
			index.setPixelValue(values, position);
		}
	}

	/**
	 * Inputs an array of values at a given position.
	 * <p>
	 * This variant allows drawing fat pixels.  With this scheme pixels are
	 * drawn very coarse and sparse initially but later in increasingly finer
	 * detail.  This is for use when outputs are displayed and the process is
	 * very time-consuming.
	 * 
	 * @param values
	 * @param position
	 * @param chunkySize 
	 */
	public void setPixelValue(double[] values, long[] position, int[] chunkySize) {
		for (OutputSetMember index : indices) {
			index.setPixelValue(values, position, chunkySize);
		}
	}

	/**
	 * Initialization method with array of labels.
	 * 
	 * @param commandService
	 * @param datasetService
	 * @param combined
	 * @param useChannelDimension
	 * @param type
	 * @param dimensions
	 * @param name
	 * @param axes
	 * @param labels 
	 */
	private void init(CommandService commandService, DatasetService datasetService, boolean combined, boolean useChannelDimension, T type, long[] dimensions, String name, AxisType[] axes, String[] labels) {
		List<OutputSetMember> indices = new ArrayList<OutputSetMember>();
		for (int i = 0; i < labels.length; ++i) {
			OutputSetMember tupleIndex = new OutputSetMember(labels[i], i, new IndexedMemberFormula(i) );
			indices.add(tupleIndex);
		}
		init(commandService, datasetService, combined, useChannelDimension, type, dimensions, name, axes, indices);
	}

	/**
	 * Initialization method with list of indices.
	 * 
	 * @param commandService
	 * @param datasetService
	 * @param combined
	 * @param useChannelDimension
	 * @param type
	 * @param dimensions
	 * @param name
	 * @param axes
	 * @param indices 
	 */
	private void init(CommandService commandService, DatasetService datasetService, boolean combined, boolean useChannelDimension, T type, long[] dimensions, String name, AxisType[] axes, List<OutputSetMember> indices) {
		this.combined = combined;
		this.useChannelDimension = useChannelDimension;
		this.dimensions = dimensions;
		this.name = name;
		this.indices = indices;
		list = new ArrayList<Dataset>();
		for (OutputSetMember index : indices) {
			index.setCombined(combined);
			if (!combined) {
				// create separate dataset for index
				String subName = index.getLabel() + " " + name;
				Dataset dataset = datasetService.create(type, dimensions, subName, axes);
				Cursor<? extends RealType> cursor = dataset.getImgPlus().cursor();
				while (cursor.hasNext()) {
					cursor.fwd();
					cursor.get().setReal(Double.NaN);
				}
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
			for (OutputSetMember index : indices) {
				index.setRandomAccess(dataset.getImgPlus().randomAccess());
			}
		}
		
		// pop up a data histogram tool
		System.out.println("OutputSet: pop up DataHistogramCommand");
		commandService.run(DataHistogramCommand.class);
	}

	/**
	 * Adds a new dimension when combining datasets into one.
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
				if (useChannelDimension) {
					combinedAxes[i] = Axes.CHANNEL;
				}
				else {
					combinedAxes[i] = Axes.get(DIMENSION_LABEL);
				}
			}
			else {
				combinedAxes[i] = axes[i - 1];
			}
		}
		return combinedAxes;
	}
}
