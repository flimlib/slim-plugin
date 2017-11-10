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

package loci.slim2.histogram;

import net.imagej.ChannelCollection;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.DrawingTool;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.plugins.commands.display.ShowLUT;
import net.imagej.render.RenderingService;

import org.scijava.util.Colors;

/**
 * Histogram graph {@link Dataset} drawing class. Based on {@link ShowLUT} by
 * Barry DeZonia and Wayne Rasband.
 *
 * @author Aivar Grislis
 */
public class HistogramGraph {

	private static final String DEFAULT_TITLE = "Histogram";
	private static final int DEFAULT_WIDTH = 256;
	private static final int DEFAULT_HEIGHT = 128;
	private static final int X_MARGIN = 30;
	private static final int Y_MARGIN = 20;
	private static final int FRAME_WIDTH = 1;
	private static final int FRAME_HEIGHT = 1;
	private static final ChannelCollection WHITE_CHANNELS =
		new ChannelCollection(Colors.WHITE);
	private static final ChannelCollection BLACK_CHANNELS =
		new ChannelCollection(Colors.BLACK);
	private static final ChannelCollection GRAY_CHANNELS = new ChannelCollection(
		Colors.DIMGRAY);
	private static final double LOG_ONE_FACTOR = Math.log(3) / Math.log(2);
	private final DatasetService datasetService;
	private final RenderingService renderingService;
	private final String title;
	private final int width;
	private final int height;
	private final int totalWidth;
	private final int totalHeight;
	private final Dataset dataset;
	private long[] histogram;
	private boolean logarithmic;
	private boolean distinguishNonZero;

	/**
	 * Constructor
	 *
	 */
	public HistogramGraph(final DatasetService datasetService,
		final RenderingService renderingService)
	{
		this.datasetService = datasetService;
		this.renderingService = renderingService;
		title = DEFAULT_TITLE;
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		totalWidth = width + 2 * X_MARGIN + 2 * FRAME_WIDTH;
		totalHeight = height + 3 * Y_MARGIN + 2 * FRAME_HEIGHT;
		logarithmic = false;
		distinguishNonZero = false;
		dataset = createHistogramDataset();
		repaint();
	}

	/**
	 * Gets the histogram graph dataset.
	 *
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Sets whether to graph logarithms.
	 *
	 */
	public void setLogarithmic(final boolean logarithmic) {
		this.logarithmic = logarithmic;
		repaint();
	}

	/**
	 * Sets whether or not to emphasize single or low counts so they can be
	 * distinguished from a zero count.
	 *
	 */
	public void setDistinguishNonZero(final boolean distinguishNonZero) {
		this.distinguishNonZero = distinguishNonZero;
		repaint();
	}

	/**
	 * Updates the histogram data.
	 *
	 */
	// TODO ARG histogram could just be live
	// TODO ARG still need a way to swap active image
	public void updateHistogram(final long[] histogram) {
		this.histogram = histogram;
		repaint();
	}

	public long[] getHistogram() {
		return histogram;
	}

	/**
	 * Creates a {@link Dataset} to use for histogram graph.
	 * 
	 */
	private Dataset createHistogramDataset() {
		final long[] dims = new long[] { totalWidth, totalHeight, 3 };
		final AxisType[] axes = new AxisType[] { Axes.X, Axes.Y, Axes.CHANNEL };
		final int bitsPerPixel = 8;
		final boolean signed = false;
		final boolean floating = false;
		final Dataset ds =
			datasetService.create(dims, title, axes, bitsPerPixel, signed, floating);
		ds.setRGBMerged(true);
		return ds;
	}

	/**
	 * Redraws the histogram graph.
	 */
	private void repaint() {
		final DrawingTool tool = new DrawingTool(dataset, renderingService);

		// draw background and frame
		tool.setChannels(WHITE_CHANNELS);
		tool.fillRect(0, 0, totalWidth, totalHeight);
		tool.setChannels(BLACK_CHANNELS);
		tool.drawRect(X_MARGIN, Y_MARGIN, width + 2 * FRAME_WIDTH, height + 2 *
			FRAME_HEIGHT);

		// draw histogram
		if (null != histogram) {
			final int[] barHeights = getBarHeights(width, height);

			// draw bars
			tool.setChannels(GRAY_CHANNELS);
			for (int i = 0; i < barHeights.length; ++i) {
				if (0 != barHeights[i]) {
					tool.moveTo(X_MARGIN + FRAME_WIDTH + i, Y_MARGIN + FRAME_HEIGHT +
						height - 1);
					tool.lineTo(X_MARGIN + FRAME_WIDTH + i, Y_MARGIN + FRAME_HEIGHT +
						height - barHeights[i]);
				}
			}
		}
		dataset.setDirty(true);
		dataset.update();
		// TODO ARG forces the update:
		dataset.setImgPlus(dataset.getImgPlus());
	}

	/**
	 * Calculates the histogram bar heights according to the current display
	 * scheme.
	 *
	 */
	private int[] getBarHeights(final int width, final int height) {
		final int[] barHeights = new int[histogram.length];

		// find maximum count
		long maxBinCount = Long.MIN_VALUE;
		for (final long binCount : histogram) {
			if (binCount > maxBinCount) {
				maxBinCount = binCount;
			}
		}

		if (0 == maxBinCount) {
			// all bins are zeros
			for (int i = 0; i < barHeights.length; ++i) {
				barHeights[i] = 0;
			}
		}
		else {
			// calculate the bar heights
			if (logarithmic) {
				int logOneHeight = 0;
				if (distinguishNonZero) {
					// calculate a nominal height for log of one; actually the log of
					// one is zero.
					// this is just a hacky way to get some proportionality
					final double logTwoHeight =
						(height * Math.log(2)) / (Math.log(maxBinCount) + 1);
					logOneHeight = (int) (LOG_ONE_FACTOR * logTwoHeight);
				}

				final double logMaxBinCount = Math.log(maxBinCount);
				for (int i = 0; i < histogram.length; ++i) {
					if (0 == histogram[i]) {
						barHeights[i] = 0;
					}
					else if (1 == histogram[i]) {
						barHeights[i] = logOneHeight;
					}
					else {
						barHeights[i] =
							(int) ((height - logOneHeight) * Math.log(histogram[i]) / logMaxBinCount) +
								logOneHeight;
					}
				}

			}
			else {
				final int extraPixel = distinguishNonZero ? 1 : 0;
				for (int i = 0; i < histogram.length; ++i) {
					if (0 == histogram[i]) {
						barHeights[i] = 0;
					}
					else {
						// if option selected, make sure values of one show at least a
						// single pixel
						barHeights[i] =
							(int) ((height - extraPixel) * histogram[i] / maxBinCount) +
								extraPixel;
					}
				}
			}
		}
		return barHeights;
	}
}
