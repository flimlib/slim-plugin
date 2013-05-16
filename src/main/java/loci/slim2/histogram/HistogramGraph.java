/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * Based on {@link imagej.core.commands.display.ShowLUT} by Barry DeZonia &
 * Wayne Rasband.
 */
package loci.slim2.histogram;

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
 * @author aivar
 */
public class HistogramGraph {
	private static final String DEFAULT_TITLE = "Histogram";
	private static final long DEFAULT_WIDTH = 326;
	private static final long DEFAULT_HEIGHT = 188;
	private final DatasetService datasetService;
	private final RenderingService renderingService;
	private final String title;
	private final long width;
	private final long height;
	private final Dataset dataset;
	private long[] histogram;
	
	public HistogramGraph(DatasetService datasetService, RenderingService renderingService) {
		this.datasetService = datasetService;
		this.renderingService = renderingService;
		title = DEFAULT_TITLE;
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		dataset = createDataset();
	}
	
	public Dataset getDataset() {
		return dataset;
	}
	
	public void updateHistogram(long[] histogram) {
		this.histogram = histogram;
		repaint();
	}
	
	private Dataset createDataset() {
		long[] dims = new long[] { width, height, 3 };
		AxisType[] axes = new AxisType[] { Axes.X, Axes.Y, Axes.CHANNEL };
		int bitsPerPixel = 8;
		boolean signed = false;
		boolean floating = false;
		Dataset dataset = datasetService.create(dims, title, axes, bitsPerPixel, signed, floating);
		return dataset;
	}
	
	private void repaint() {
		
	}
}
