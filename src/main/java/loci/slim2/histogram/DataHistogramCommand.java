/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package loci.slim2.histogram;

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.command.InteractiveImageCommand;
import imagej.data.display.DatasetView;
import imagej.menu.MenuConstants;
import imagej.render.RenderingService;
import imagej.widget.NumberWidget;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.Binning;
import net.imglib2.Cursor;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Plugin that sets the minimum and maximum for scaling of display values. Sets
 * the same min/max for each channel.
 * 
 * Based on {@link imagej.core.commands.display.interactive.BrightnessContrast}
 * by Curtis Rueden & Grant Harris.
 */
//TODO ARG I thought perhaps there was an image realm for histograms and a data one,
//  with the latter if you have enough measurements the distribution tends to normal
//  then I found normalizing the histogram is a good image technique

@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.ANALYZE_LABEL, weight = MenuConstants.ANALYZE_WEIGHT,
		mnemonic = MenuConstants.ANALYZE_MNEMONIC),
	@Menu(label = "Data Histogram...", accelerator = "control shift C",
		weight = 0) }, iconPath = "/icons/commands/contrast.png", headless = true, //TODO ARG use 'normal.png', which is on my Desktop
		initializer = "initValues")
public class DataHistogramCommand extends InteractiveImageCommand {
		
	@Parameter
	private DatasetService datasetService;	

	@Parameter
	private RenderingService renderingService;

	@Parameter(type = ItemIO.BOTH, callback = "viewChanged")
	private DatasetView view; // get "[WARNING] No widget found for input: view" unless a file has been loaded ahead of time

	@Parameter(label = "Minimum", persist = false, callback = "minMaxChanged")
	private double min = Double.NaN;

	@Parameter(label = "Maximum", persist = false, callback = "minMaxChanged")
	private double max = Double.NaN;

	//TODO ARG@Parameter(callback = "brightnessContrastChanged", persist = false,
	//TODO ARG	style = NumberWidget.SCROLL_BAR_STYLE, min = S_MIN, max = S_MAX)
	private int brightness;

	//TODO ARG@Parameter(callback = "brightnessContrastChanged", persist = false,
	//TODO ARG	style = NumberWidget.SCROLL_BAR_STYLE, min = S_MIN, max = S_MAX)
	private int contrast;

	@Parameter(label = "Show full range", persist = true,
		callback = "rangeChanged")
	private boolean showFullRange;
	
	@Parameter(label = "Logarithmic", persist = true,
		callback = "logarithmicChanged")
	private boolean logarithmic;
	
	@Parameter(label = "Show single counts", persist = true,
		callback = "showSingleCountsChanged")
	private boolean showSingleCounts;	

	/** The minimum and maximum values of the data itself. */
	private double dataMin, dataMax;

	/** The initial minimum and maximum values of the data view. */
	private double initialMin, initialMax;
	
	private /*final*/ HistogramGraph histogramGraph;
	
	private DatasetView saveView;
	
	private volatile boolean running;

	public DataHistogramCommand() {
		super("view");
		System.out.println("DataHistogramCommand ctor, view is " + view);
		logarithmic = false;
		showSingleCounts = false;
		running = false;
		//TODO ARG
		min = 0;
		max = 1;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		System.out.println("DataHistogramCommand.run, view is " + view);
		// 'run' gets called again after every UI change.  This plugin stays
		//   active all the time.
		if (!running) {
			running = true;
			if (null == histogramGraph) {
				histogramGraph = new HistogramGraph(datasetService, renderingService);
				Dataset dataset = histogramGraph.getDataset();
				displayService.createDisplay(dataset);
			}
			else {
				System.out.println("DHC run and histo was not null");
			}
			updateDisplay();
			System.out.println("DONE");
			running = false;
		}
	}

	// -- BrightnessContrast methods --

	public DatasetView getView() {
		return view;
	}

	public void setView(final DatasetView view) {
		this.view = view;
		saveView = view;
	}

	public double getMinimum() {
		return min;
	}

	public void setMinimum(final double min) {
		this.min = min;
	}

	public double getMaximum() {
		return max;
	}

	public void setMaximum(final double max) {
		this.max = max;
	}

	public int getBrightness() {
		return brightness;
	}

	public void setBrightness(final int brightness) {
		this.brightness = brightness;
	}

	public int getContrast() {
		return contrast;
	}

	public void setContrast(final int contrast) {
		this.contrast = contrast;
	}

	// -- Initializers --

	protected void initValues() {
		if (null != view) {
			viewChanged();
		}
	}

	// -- Callback methods --

	/** Called when view changes. Updates everything to match. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void viewChanged() {
		// did view really change?
		if (saveView == null || view != saveView) {
			saveView = view;
			System.out.println("new view " + view);
			
			final Dataset dataset = view.getData();
			final ImgPlus img = dataset.getImgPlus();
			System.out.println("Dataset is " + dataset.getName());
			System.out.println("ImgPlus is " + img.getName());
			
			//TODO ARG just autorange it for now
			computeDataMinMax(img);
			min = dataMin;
			max = dataMax;
			System.out.println("viewChanged, autorange min/max to " + min + " " + max);
			
			System.out.println("updateHistogram from viewChanged");
			updateHistogram(img);

			long[] dims = dataset.getDims();
			long channels = 1;
			if (dims.length > Axes.CHANNEL.ordinal()) {
				channels = dims[Axes.CHANNEL.ordinal()];
			}
			for (int c = 0; c < channels; ++c) {
				System.out.println("channel " + c + " min " + dataset.getChannelMinimum(c) + " max " + dataset.getChannelMaximum(c));
			}
			System.out.println("data min " + dataMin + " max " + dataMax + " min " + min + " max " + max);
			updateDisplay();
		}
	}

	//TODO ARG having default min/max increments is not ideal; also no upper/lower limits
	/** Called when min or max changes. Updates brightness and contrast. */
	protected void minMaxChanged() {
		System.out.println("min max changed " + min + " " + max);
		if (null != view) {
			final Dataset dataset = view.getData();
			final ImgPlus img = dataset.getImgPlus();
			System.out.println("updateHistogram from minMaxChanged");
			updateHistogram(img);
			updateDisplay();
			// etc?? see above
		}
	}
	
	/** Called when show full range changes. */
	protected void rangeChanged() {
		System.out.println("RANGE CHANGED");
	}
	
	/** Called when logarithmic changes. */
	protected void logarithmicChanged() {
		System.out.println("LOG CHANGED " + logarithmic);
		if (null != histogramGraph) {
			histogramGraph.setLogarithmic(logarithmic);
		}
	}

	/** Called when show single counts changes. */
	protected void showSingleCountsChanged() {
		System.out.println("SINGLE PIXEL CHANGED " + showSingleCounts);
		if (null != histogramGraph) {
			histogramGraph.setDistinguishNonZero(showSingleCounts);
		}
	}

	// -- Helper methods --
	
	private <T extends RealType<T>> void updateHistogram(final ImgPlus<T> img) {
		System.out.println("in updateHistogram and min max are " + min + " " + max);
		//TODO ARG use statistics service
		if (null != histogramGraph) {
			long[] histogram = new long[256];
			Cursor<T> cursor = img.cursor();
			while (cursor.hasNext()) {
				cursor.fwd();
				double value = cursor.get().getRealDouble();
				if (!Double.isNaN(value)) {
					// 'exclusive' means don't count values outside the min/max range
					int index = Binning.exclusiveValueToBin(256, min, max, value);
					if (index >= 0 && index < histogram.length) {
						++histogram[index];
					}
				}
			}
			long maxHistoCount = -1;
			int maxHistoIndex = 0;
			for (int i = 0; i < histogram.length; ++i) {
				if (histogram[i] > maxHistoCount) {
					maxHistoCount = histogram[i];
					maxHistoIndex = i;
				}
			}
			boolean allZero = true;
			for (int i = 0; i < histogram.length; ++i) {
				if (i != maxHistoIndex) {
					if (i == 0 || histogram[i] != 0) {
						System.out.println("count at " + i + " is " + histogram[i]);
						allZero = false;
					}
				}
			}
			System.out.println(" max is " + maxHistoCount + " at index " + maxHistoIndex + " " + (allZero ? " rest all zeros " : " rest not all zeros"));
			histogramGraph.updateHistogram(histogram);
		}
	}

	private <T extends RealType<T>> void computeDataMinMax(final ImgPlus<T> img) {
		// FIXME: Reconcile this with DefaultDatasetView.autoscale(int). There is
		// no reason to hardcode the usage of ComputeMinMax twice. Rather, there
		// should be a single entry point for obtain the channel min/maxes from
		// the metadata, and if they aren't there, then compute them. Probably
		// Dataset (not DatasetView) is a good place for it, because it is metadata
		// independent of the visualization settings.
		//TODO ARG we need 2 versions of min/max: one for the entire channel, one for current plane of channel
		final ComputeMinMax<T> computeMinMax = new ComputeMinMax<T>(img);
		computeMinMax.process();
		dataMin = computeMinMax.getMin().getRealDouble();
		dataMax = computeMinMax.getMax().getRealDouble();
		log.debug("computeDataMinMax: dataMin=" + dataMin + ", dataMax=" + dataMax);
	}

	private void computeInitialMinMax() {
		// use only first channel, for now
		initialMin = view.getChannelMin(0);
		initialMax = view.getChannelMax(0);
		log.debug("computeInitialMinMax: initialMin=" + initialMin +
			", initialMax=" + initialMax);
	}

	/** Updates the displayed min/max range to match min and max values. */
	private void updateDisplay() {
		System.out.println("updateDisplay min " + min + " max " + max);
		view.setChannelRanges(min, max);
		view.getProjector().map();
		view.update();
	}

}
