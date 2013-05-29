/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * Based on {@link imagej.core.commands.display.ShowLUT} by Barry DeZonia &
 * Wayne Rasband.
 */
package loci.slim2.histogram;

import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.DrawingTool;
import imagej.data.display.ColorTables;
import imagej.data.display.DatasetView;
import imagej.render.RenderingService;
import imagej.util.ColorRGB;
import imagej.util.Colors;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
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
	private static final ChannelCollection WHITE_CHANNELS = new ChannelCollection(Colors.WHITE);
	private static final ChannelCollection BLACK_CHANNELS = new ChannelCollection(Colors.BLACK);
	private static final ChannelCollection GRAY_CHANNELS = new ChannelCollection(Colors.BLACK); //TODO ARG default contrast shows this as white 5/20/13 Colors.DARKGRAY);
	private static final double LOG_ONE_FACTOR = Math.log(3) / Math.log(2);
	private final DatasetService datasetService;
	private final RenderingService renderingService;
	private final String title;
	private final int width;
	private final int height;
	private final int totalWidth;
	private final int totalHeight;
	private final Dataset dataset;
	private long[] histogram = new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
	private boolean logarithmic;
	private boolean distinguishNonZero;

	/**
	 * Constructor
	 * 
	 * @param datasetService
	 * @param renderingService 
	 */
	public HistogramGraph(DatasetService datasetService, RenderingService renderingService) {
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
	 * @return 
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Sets whether to graph logarithms.
	 * 
	 * @param logarithmic 
	 */
	public void setLogarithmic(boolean logarithmic) {
		this.logarithmic = logarithmic;
		repaint();
	}

	/**
	 * Sets whether or not to emphasize single or low counts so they can be
	 * distinguished from a zero count.
	 * 
	 * @param distinguishNonZero 
	 */
	public void setDistinguishNonZero(boolean distinguishNonZero) {
		this.distinguishNonZero = distinguishNonZero;
		repaint();
	}

	/**
	 * Updates the histogram data.
	 * 
	 * @param histogram 
	 */
	//TODO ARG histogram could just be live
	//TODO ARG still need a way to swap active image
	public void updateHistogram(long[] histogram) {
		this.histogram = histogram;
		repaint();
	}

	/**
	 * Creates a {@link Dataset} to use for histogram graph.
	 * @return 
	 */
	private Dataset createHistogramDataset() {
		long[] dims = new long[] { totalWidth, totalHeight, 3 };
		AxisType[] axes = new AxisType[] { Axes.X, Axes.Y, Axes.CHANNEL };
		int bitsPerPixel = 8;
		boolean signed = false;
		boolean floating = false;
		Dataset ds = datasetService.create(dims, title, axes, bitsPerPixel, signed, floating);
		ds.setRGBMerged(true);
		return ds;
	}

	/**
	 * Redraws the histogram graph.
	 */
	private void repaint() {
		DrawingTool tool = new DrawingTool(dataset, renderingService);		
		
		// draw background and frame
		tool.setChannels(WHITE_CHANNELS);
		tool.fillRect(0, 0, totalWidth, totalHeight);
		tool.setChannels(BLACK_CHANNELS);
		tool.drawRect(X_MARGIN, Y_MARGIN, width + 2 * FRAME_WIDTH, height + 2 * FRAME_HEIGHT);
		//TODO ARG System.out.println("drawRect " + (X_MARGIN) + " " + (Y_MARGIN) + " " + (width + 2 * FRAME_WIDTH) + " " + (height + 2 * FRAME_HEIGHT));
		
		// draw histogram
		if (null != histogram) {
			int[] barHeights = getBarHeights(width, height);
			
			// draw bars
			tool.setChannels(GRAY_CHANNELS);
			for (int i = 0; i < barHeights.length; ++i) {
				if (0 != barHeights[i]) {
					tool.moveTo(X_MARGIN + FRAME_WIDTH + i, Y_MARGIN + FRAME_HEIGHT + height - 1);
					tool.lineTo(X_MARGIN + FRAME_WIDTH + i, Y_MARGIN + FRAME_HEIGHT + height - barHeights[i]);
				}
			}
		}
		dataset.setDirty(true);
		dataset.update();
		//TODO ARG forces the update:
		dataset.setImgPlus(dataset.getImgPlus());
	}

	/**
	 * Calculates the histogram bar heights according to the current display
	 * scheme.
	 * 
	 * @param width
	 * @param height
	 * @return 
	 */
	private int[] getBarHeights(int width, int height) {
		int[] barHeights = new int[histogram.length];
		
		// find maximum count
		long maxBinCount = Long.MIN_VALUE;
		for (long binCount : histogram) {
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
					//  one is zero.
					// this is just a hacky way to get some proportionality
					double logTwoHeight = (height * Math.log(2)) / (Math.log(maxBinCount) + 1);
					logOneHeight = (int)(LOG_ONE_FACTOR * logTwoHeight);
				}

				double logMaxBinCount = Math.log(maxBinCount);
				for (int i =0; i < histogram.length; ++i) {
					if (0 == histogram[i]) {
						barHeights[i] = 0;
					}
					else if (1 == histogram[i]) {
						barHeights[i] = logOneHeight;
					}
					else {
						barHeights[i] = (int)((height - logOneHeight) * Math.log(histogram[i]) / logMaxBinCount) + logOneHeight;
					}
				}

			}
			else {
				int extraPixel = distinguishNonZero ? 1 : 0;
				for (int i =0; i < histogram.length; ++i) {
					// if option selected, make sure values of one show at least a single pixel
					barHeights[i] = (int)((height - extraPixel) * histogram[i] / maxBinCount) + extraPixel;
				}	
			}
		}
		return barHeights;
	}
}
