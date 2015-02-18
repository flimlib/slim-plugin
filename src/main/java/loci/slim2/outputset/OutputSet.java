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

package loci.slim2.outputset;

import java.util.ArrayList;
import java.util.List;

import loci.slim2.histogram.DataHistogramCommand;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;

import org.scijava.command.CommandService;

/**
 * Builds a set of related images. In these images corresponding pixels describe
 * different attributes measured or computed at the same pixel position.
 *
 * @author Aivar Grislis
 */
//TODO ARG Views hyperslice and addDimension are similar, but I don't want shared data
//TODO ARG recursive generic not appropriate, just want some kind of common RealType, don't all have to be the same Type
public class OutputSet<T extends RealType<T> & NativeType<T>> {

	private static final int POST_XY_DIMENSION = 2;
	private static final String DIMENSION_LABEL = "parameters";
	private String name;
	private boolean combined;
	private boolean useChannelDimension = true;
	private long[] dimensions;
	private List<OutputSetMember> indices;
	private List<Dataset> list;

	/**
	 * Creates an images set. This variant uses {@link MemberFormula} which allows
	 * deriving values from a formula.
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
	public OutputSet(final CommandService commandService,
		final DatasetService datasetService, final boolean combined,
		final boolean useChannelDimension, final T type, final long[] dimensions,
		final String name, final AxisType[] axes,
		final List<OutputSetMember> indices)
	{
		init(commandService, datasetService, combined, useChannelDimension, type,
			dimensions, name, axes, indices);
	}

	/**
	 * Creates a set of images. This variant uses an array of value labels. The
	 * index of the label is also used to derive the value.
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
	public OutputSet(final CommandService commandService,
		final DatasetService datasetService, final boolean combined,
		final boolean useChannelDimension, final T type, final long[] dimensions,
		final String name, final AxisType[] axes, final String[] labels)
	{
		init(commandService, datasetService, combined, useChannelDimension, type,
			dimensions, name, axes, labels);
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
	// TODO ARG can't you get T type from the Dataset somehow??
	public OutputSet(final CommandService commandService,
		final DatasetService datasetService, final boolean combined,
		final boolean useChannelDimension, final T type, final Dataset dataset,
		final String[] labels)
	{
		final ImgPlus<?> img = dataset.getImgPlus();
		final long[] dimensions = new long[img.numDimensions()];
		img.dimensions(dimensions);
		final String name = dataset.getName();
		final AxisType[] axes = new AxisType[img.numDimensions()];
		for (int i = 0; i < axes.length; i++)
			axes[i] = img.axis(i).type();
		init(commandService, datasetService, combined, useChannelDimension, type,
			dimensions, name, axes, labels);
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
	public OutputSet(final CommandService commandService,
		final DatasetService datasetService, final boolean combined,
		final boolean useChannelDimension, final T type, final Dataset dataset,
		final List<OutputSetMember> indices)
	{
		final ImgPlus<?> img = dataset.getImgPlus();
		final long[] dimensions = new long[img.numDimensions()];
		img.dimensions(dimensions);
		final String name = dataset.getName();
		final AxisType[] axes = new AxisType[img.numDimensions()];
		for (int i = 0; i < axes.length; i++)
			axes[i] = img.axis(i).type();
		init(commandService, datasetService, combined, useChannelDimension, type,
			dimensions, name, axes, indices);
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
	 * Inputs an array of values at a given position. These values are then used
	 * singly or in some combining formula to yield the output set images pixel
	 * values.
	 *
	 * @param values
	 * @param position
	 */
	public void setPixelValue(final double[] values, final long[] position) {
		for (final OutputSetMember index : indices) {
			index.setPixelValue(values, position);
		}
	}

	/**
	 * Inputs an array of values at a given position.
	 * <p>
	 * This variant allows drawing fat pixels. With this scheme pixels are drawn
	 * very coarse and sparse initially but later in increasingly finer detail.
	 * This is for use when outputs are displayed and the process is very
	 * time-consuming.
	 *
	 * @param values
	 * @param position
	 * @param chunkySize
	 */
	public void setPixelValue(final double[] values, final long[] position,
		final int[] chunkySize)
	{
		for (final OutputSetMember index : indices) {
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
	private void init(final CommandService commandService,
		final DatasetService datasetService, final boolean combined,
		final boolean useChannelDimension, final T type, final long[] dimensions,
		final String name, final AxisType[] axes, final String[] labels)
	{
		final List<OutputSetMember> indices = new ArrayList<OutputSetMember>();
		for (int i = 0; i < labels.length; ++i) {
			final OutputSetMember tupleIndex =
				new OutputSetMember(labels[i], i, new IndexedMemberFormula(i));
			indices.add(tupleIndex);
		}
		init(commandService, datasetService, combined, useChannelDimension, type,
			dimensions, name, axes, indices);
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
	private void init(final CommandService commandService,
		final DatasetService datasetService, final boolean combined,
		final boolean useChannelDimension, final T type, final long[] dimensions,
		final String name, final AxisType[] axes,
		final List<OutputSetMember> indices)
	{
		this.combined = combined;
		this.useChannelDimension = useChannelDimension;
		this.dimensions = dimensions;
		this.name = name;
		this.indices = indices;
		list = new ArrayList<Dataset>();
		for (final OutputSetMember index : indices) {
			index.setCombined(combined);
			if (!combined) {
				// create separate dataset for index
				final String subName = index.getLabel() + " " + name;
				final Dataset dataset =
					datasetService.create(type, dimensions, subName, axes);
				final Cursor<? extends RealType> cursor = dataset.getImgPlus().cursor();
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
			final long[] combinedDimensions =
				addCombinedDimension(dimensions, indices.size());
			final AxisType[] combinedAxes = addCombinedAxis(axes);
			final Dataset dataset =
				datasetService.create(type, combinedDimensions, name, combinedAxes);
			list.add(dataset);
			for (final OutputSetMember index : indices) {
				index.setRandomAccess(dataset.getImgPlus().randomAccess());
			}
		}

		// pop up a data histogram tool
		System.out.println("OutputSet: pop up DataHistogramCommand");
		commandService.run(DataHistogramCommand.class, true);
	}

	/**
	 * Adds a new dimension when combining datasets into one.
	 *
	 * @param dimensions
	 * @param dimension
	 * @return
	 */
	private long[] addCombinedDimension(final long[] dimensions,
		final long dimension)
	{
		final long[] combinedDimensions = new long[dimensions.length + 1];
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
	private AxisType[] addCombinedAxis(final AxisType[] axes) {
		final AxisType[] combinedAxes = new AxisType[axes.length + 1];
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
