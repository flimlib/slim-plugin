/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
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

package loci.slim.histogram;

import ij.IJ;
import ij.plugin.LutLoader;
import ij.process.LUT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * This is the main class for the histogram tool.  It handles layout and wiring
 * of UI components and the logic of updating the histogram.
 *
 * @author Aivar Grislis
 */
public class HistogramTool {
	private final static String WIDTH_KEY = "width";
	private final static String HEIGHT_KEY = "height";
	private final static int SET_WIDTH = 263;
	private final static int SET_HEIGHT = 302;
	private final static int MAX_HEIGHT = 500;
	private final static int MIN_HEIGHT = 200;
	private final static int WIDTH = PaletteFix.getSize();
	private final static int INSET = 5;
	private final static int HISTOGRAM_HEIGHT = 140;
	private final static int COLORBAR_HEIGHT = 20;
	private final static int TASK_PERIOD = 100;
	private final static String LUT = "lifetime.lut"; // use a specific LUT; most IJ LUTs unsuitable
	private static HistogramTool INSTANCE = null;
	private final Object _synchObject = new Object();
	private JFrame _frame;
	private HistogramDataGroup _histogramDataGroup;
	private HistogramPanel _histogramPanel;
	private ColorBarPanel _colorBarPanel;
	private HistogramUIPanel _uiPanel;
	private boolean _excludePixels;

	/**
	 * Class is a singleton for convenience.
	 * 
	 * @return 
	 */
	public static synchronized HistogramTool getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new HistogramTool();
		}
		return INSTANCE;
	}

	/**
	 * Initializer, handles layout and wiring.  Called when set of images is 
	 * created.
	 * 
	 * @param hasChannels 
	 */
	public void show(boolean hasChannels) {
		if (null == _frame || !_frame.isShowing()) {
			// create the histogram and color bar display panels
			_histogramPanel = new HistogramPanel(WIDTH, INSET, HISTOGRAM_HEIGHT);
			_histogramPanel.setListener(new HistogramPanelListener());
			_colorBarPanel = new ColorBarPanel(WIDTH, INSET, COLORBAR_HEIGHT);
			_colorBarPanel.setLUT(getLUT());

			_uiPanel = new HistogramUIPanel(hasChannels);
			_uiPanel.setListener(new UIPanelListener());

			_frame = new JFrame("Histogram");
			//_frame.setResizable(false);
			_frame.setLayout(new BorderLayout());

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(_colorBarPanel);
			panel.add(_uiPanel);
			_frame.getContentPane().add(_histogramPanel, BorderLayout.CENTER);
			_frame.getContentPane().add(panel, BorderLayout.SOUTH);
			_frame.pack();
			_frame.setVisible(true);
			//IJ.log("initial size " + _frame.getSize());
			_frame.addComponentListener(new ComponentListener() {
				@Override
				public void componentHidden(ComponentEvent e) {

				}
				@Override
				public void componentMoved(ComponentEvent e) {

				}
				@Override
				public void componentResized(ComponentEvent e) {
					// constrain maximum size
					boolean resize = false;
					Dimension size = _frame.getSize();
					if (size.width != (SET_WIDTH)) {
						size.width = SET_WIDTH;
						resize = true;
					}
					if (size.height > MAX_HEIGHT) {
						size.height = MAX_HEIGHT;
						resize = true;
					}
					if (size.height < MIN_HEIGHT) {
						size.height = MIN_HEIGHT;
						resize = true;
					}
					if (resize) {
						_frame.setSize(size);
					}
					saveSizeInPreferences(size);
				}

				@Override
				public void componentShown(ComponentEvent e) {

				}
			});
			_frame.setSize(getSizeFromPreferences());
		}
	}

	/**
	 * Gets an IndexColorModel by loading a hardcoded LUT file.
	 * This is just a temporary expedient, really belongs elsewhere.
	 * 
	 * @return null or color model
	 */
	public static IndexColorModel getIndexColorModel() {
		IndexColorModel colorModel = null;

		// 'getDirectory("luts")' works in IJ but not in NetBeans development
		String lutPath = IJ.getDirectory("luts");
		if (null == lutPath) {
			// when you run from a shortcut in Linux 'getDirectory("startup")'
			//   gives you the directory of the link!
			String startupPath = IJ.getDirectory("startup");
			lutPath = addSeparator(startupPath) + "luts";
		}
		lutPath = addSeparator(lutPath) + LUT;
		try {
			colorModel = LutLoader.open(lutPath);
		}
		catch (IOException e) {
			IJ.showMessage("Missing LUT", "Install lifetime.lut from LOCI FIJI update site.");
			IJ.log("Problem loading LUT " + lutPath);
			return null;
		}
		// IJ converts the FloatProcessor to 8-bits and then uses this palette
		// for display.  Unfortunately values less than or greater than the LUT
		// range still get displayed with LUT colors.  To work around this, use
		// only 254 of the LUT colors.  The first and last colors will represent
		// values less than and greater than the LUT range respectively.

		colorModel = PaletteFix.fixIndexColorModel(colorModel, Color.BLACK, Color.BLACK);
		return colorModel;
	}

	private static String addSeparator(String path) {
		if (!path.endsWith(File.separator)) {
			path += File.separatorChar;
		}
		return path;
	}

	/**
	 * Gets a LUT.  Temporary expedient, belongs elsewhere.
	 * 
	 * @return 
	 */
	public static LUT getLUT() {
		IndexColorModel colorModel = getIndexColorModel();
		LUT lut = new LUT(colorModel, -Double.MAX_VALUE, Double.MAX_VALUE);
		return lut;
	}

	/**
	 * This method should be called whenever a new set of histogram values is
	 * to be displayed (i.e. when a different image gets focus).
	 * 
	 * @param histogramData 
	 */
	public void setHistogramData(HistogramDataGroup histogramData) {
		double[] minMaxView;
		double[] minMaxLUT;
		synchronized (_synchObject) {
			_histogramDataGroup = histogramData;
			_histogramDataGroup.setListener(new HistogramDataListener());
			minMaxView = _histogramDataGroup.getMinMaxView();
			minMaxLUT  = _histogramDataGroup.getMinMaxLUT();
		}

//		IJ.log("----");
//		IJ.log("setHistogramData");
//		IJ.log("view " + minMaxView[0] + " "  + minMaxView[1] + " lut " + minMaxLUT[0] + " " + minMaxLUT[1]);
//		IJ.log("----");

		if (null != _frame) {
			if (_frame.isVisible()) {
				_frame.setVisible(true);
			}
			_frame.setTitle(histogramData.getTitle());

			_histogramPanel.setStatistics(histogramData.getStatistics(WIDTH));

			boolean autoRange = histogramData.getAutoRange();
			if (autoRange) {
				// turn cursors off
				_histogramPanel.setCursors(null, null);
			}
			else {
				// set cursors to edges
				_histogramPanel.setCursors(INSET, INSET + WIDTH - 1);
			}
			_uiPanel.setAutoRange(autoRange);
			_uiPanel.setExcludePixels(histogramData.getExcludePixels());
			_uiPanel.setCombineChannels(histogramData.getCombineChannels());
			_uiPanel.setDisplayChannels(histogramData.getDisplayChannels());
			_uiPanel.enableChannels(histogramData.hasChannels());
			_uiPanel.setMinMaxLUT(minMaxLUT[0], minMaxLUT[1]);

			_colorBarPanel.setMinMax(minMaxView[0], minMaxView[1],
				minMaxLUT[0], minMaxLUT[1]);
		}

	}

	/*
	 * Converts histogram onscreen horizontal pixel amounts to image values.
	 */
	private double pixelToValue(int pixel) {
		synchronized (_synchObject) {
			double[] minMaxView = _histogramDataGroup.getMinMaxView();
			double min = minMaxView[0];
			return min + pixelToValueRelative(pixel);
		}
	}

	private double pixelToValueRelative(int pixel) {
		synchronized (_synchObject) {
			double[] minMaxView = _histogramDataGroup.getMinMaxView();
			double min = minMaxView[0];
			double max = minMaxView[1];
			double valuePerPixel = (max - min) / PaletteFix.getSize();
			return pixel * valuePerPixel;
		}
	}

	/*
	 * Converts image value to histogram onscreen horizontal pixel.
	 */
	private int valueToPixel(double value) {
		synchronized (_synchObject) {
			double[] minMaxView = _histogramDataGroup.getMinMaxView();
			double min = minMaxView[0];
			double max = minMaxView[1];
			int pixel = (int)(PaletteFix.getSize() * (value - min) / (max - min));
			return pixel;
		}
	}

	/*
	 * Updates histogram and color bar during the fit.
	 */
	private void changed(double minView, double maxView,
		double minLUT, double maxLUT) {
		synchronized (_synchObject) {
			_histogramDataGroup.setMinMaxView(minView, maxView); //TODO ARG added this 9/27 to change hDG internal minMaxViews
			_histogramPanel.setStatistics(_histogramDataGroup.getStatistics(WIDTH));
			_colorBarPanel.setMinMax(minView, maxView, minLUT, maxLUT);
			//TODO changed is currently called from two places:
			// i) the HistogramData listener will call it periodically during the
			// fit.
			// ii) if the user types in a new LUT range this gets called.
			// iii) in the future more UI interactions will wind up here
			//
			//TODO if the user drags a new LUT range this doesn't get called!

			//IJ.log("changed min/maxView " + minView + " " + maxView + " min/maxLUT " + minLUT + " " + maxLUT);


			_histogramDataGroup.redisplay();
		}
	}

	/*
	 * Given a value representing the minimum or maximum cursor bound,
	 * calculates a pixel location for the cursor.
	 * 
	 * @param max whether this is the maximum cursor or not
	 */
	private int cursorPixelFromValue(boolean max, double value) {
		int pixel = INSET + valueToPixel(value);
		if (max) {
			// cursor brackets the value
			++pixel;
		}
		return pixel;
	}

	//TODO TIDY THIS UP  do we need "reset cursor positions"
	private void zoomToLUT() {
		double minMaxLUT[]  = _histogramDataGroup.getMinMaxLUT();
		double minLUT = minMaxLUT[0];
		double maxLUT = minMaxLUT[1];

		// zoom to fit current min/max LUT settings
		double minView = minLUT;
		double maxView = maxLUT;


		changed(minView, maxView, minLUT, maxLUT);

		// reset cursor positions
		_histogramPanel.resetCursors();
	}

	/**
	 * Inner class listens for changes during the fit.
	 */
	private class HistogramDataListener implements IHistogramDataListener {

		@Override
		public void minMaxChanged(
			HistogramDataGroup histogramDataGroup,
			double minView, double maxView,
			double minLUT, double maxLUT) {
			// make sure it's the same current fitted image
			if (_histogramDataGroup == histogramDataGroup) {
				changed(minView, maxView, minLUT, maxLUT);
				_uiPanel.setMinMaxLUT(minLUT, maxLUT);
			}
		}
	}

	/**
	 * Inner class to listen for the user moving the cursor on the histogram.
	 */
	private class HistogramPanelListener implements IHistogramPanelListener {
		private Timer _timer = null;
		private volatile int _dragPixels;

		/**
		 * Listens to the HistogramPanel, gets minimum and maximum cursor bar
		 * positions in pixels.  Called when the cursor bar is moved and the
		 * mouse button released.  A new LUT range has been specified.
		 * 
		 * @param min
		 * @param max 
		 */
		@Override
		public void setMinMaxLUTPixels(int min, int max) {
			killTimer();

			// get new minimum and maximum values for LUT
			double minLUT = pixelToValue(min);
			double maxLUT = pixelToValue(max + 1);

			// set min and max on UI panel
			_uiPanel.setMinMaxLUT(minLUT, maxLUT);

			// redraw color bar
			_colorBarPanel.setMinMaxLUT(minLUT, maxLUT);

			// save and redraw image
			synchronized (_synchObject) {
				_histogramDataGroup.setMinMaxLUT(minLUT, maxLUT);
				if (_excludePixels) {
					_histogramDataGroup.excludePixels(true);
				}
				zoomToLUT();
			}
		}

		/**
		 * Listens to the HistogramPanel, gets minimum and maximum cursor bar
		 * positions in pixels.  Called during a drag operation.
		 *
		 * @param min
		 * @param max
		 */
		@Override
		public void dragMinMaxPixels(int min, int max) {
			if (min < 0 || max >= PaletteFix.getSize()) {
				// cursor is out of bounds, set up a periodic task to stretch
				// the bounds, if not already running
				if (min < 0) {
					_dragPixels = min;
				}
				else {
					_dragPixels = max - PaletteFix.getSize() + 1;
				}
				if (null == _timer) {
					_timer = new Timer();
					_timer.schedule(new PeriodicTask(), 0, TASK_PERIOD);
				}
			}
			else {
				// dragging within bounds now, kill the periodic task, if any
				killTimer();
				double minLUT = pixelToValue(min);
				double maxLUT = pixelToValue(max + 1);
				_uiPanel.dragMinMaxLUT(minLUT, maxLUT);
			}
		}

		@Override
		public void exited() {
			// dragged off the panel, kill the periodic task
			killTimer();
		}

		// stop our timer for animating view expansion
		private void killTimer() {
			if (null != _timer) {
				_timer.cancel();
				_timer = null;
			}
		}

		/**
		 * Inner class used to animate view expansion, triggered by the initial
		 * report of a mouse drag event off the edge of the histogram.
		 */
		private class PeriodicTask extends TimerTask {

			@Override
			public void run() {
				// how much are we dragging, converted to our value
				double value = pixelToValueRelative(_dragPixels);
				synchronized (_synchObject) {
					// get current LUT bounds
					double[] minMaxLUT = _histogramDataGroup.getMinMaxLUT();
					double minLUT = minMaxLUT[0];
					double maxLUT = minMaxLUT[1];

					// adjust the appropriate left or right side of the view
					double[] minMaxView = _histogramDataGroup.getMinMaxView();
					double minView = minMaxView[0];
					double maxView = minMaxView[1];
					if (value < 0) {
						minView += value;
						minLUT = minView;
					}
					else {
						maxView += value;
						maxLUT = maxView;
					}

					// update histogram data min and max view
					_histogramDataGroup.setMinMaxView(minView, maxView);

					// keep other cursor fixed
					int pixel;
					if (value < 0) {
						if (maxView != maxLUT) {
							pixel = cursorPixelFromValue(true, maxLUT);
							//pixel = valueToPixel(maxLUT);
							//IJ.log("PeriodicTask.run maxLUT is " + maxLUT + " cursor is at " + pixel);
							//_histogramPanel.setCursors(null, INSET + pixel + 1); //TODO ARG this is still a little bit off
							_histogramPanel.setCursors(null, pixel); //TODO ARG this is still a little off
						}
					}
					else {
						if (minView != minLUT) {
							pixel = cursorPixelFromValue(false, minLUT);
							//pixel = valueToPixel(minLUT);
							//IJ.log("PeriodicTask.run minLUT is " + minLUT + " cursor is at " + pixel);
							//_histogramPanel.setCursors(INSET + pixel, null); //TODO ARG this is still a little bit off
							_histogramPanel.setCursors(pixel, null); //TODO ARG this is still a little off

						}
					}

					// get updated histogram data and show it
					_histogramPanel.setStatistics(_histogramDataGroup.getStatistics(WIDTH));
					_colorBarPanel.setMinMax(minView, maxView, minLUT, maxLUT);

					// update numbers in UI panel
					_uiPanel.dragMinMaxLUT(minLUT, maxLUT);
				}
			}
		}
	}


	/**
	 * Restores size from Java Preferences.
	 *
	 * @return size
	 */
	private Dimension getSizeFromPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return new Dimension(prefs.getInt(WIDTH_KEY, SET_WIDTH),
			prefs.getInt(HEIGHT_KEY, SET_HEIGHT));
	}

	/**
	 * Saves the size to Java Preferences.
	 *
	 * @param size
	 */
	private void saveSizeInPreferences(Dimension size) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putInt(WIDTH_KEY, size.width);
		prefs.putInt(HEIGHT_KEY, size.height);
	}

	/**
	 * Inner class listens to the UI panel
	 */
	private class UIPanelListener implements IUIPanelListener {
		@Override
		public void setAutoRange(boolean autoRange) {
			synchronized (_synchObject) {
				_histogramDataGroup.setAutoRange(autoRange); //TODO shows up twice now!!!!TRY CALLING AFTER

				if (autoRange) {
					//TODO ARG:
					// It is not always true that the cursors will be (null, null)
					// [same as (0, 254)], the exception happens if you are showing
					// all channels but only autoranging your channel.
					_histogramPanel.setCursors(null, null);
				}
				else {
					_histogramPanel.setCursors(INSET, INSET + WIDTH - 1);
				}

				// this will propagate min/max changes
				// _histogramDataGroup.setAutoRange(autoRange);
			}
		}

		@Override
		public void setExcludePixels(boolean excludePixels) {
			_excludePixels = excludePixels;
			synchronized (_synchObject) {
				// pass along the changes
				_histogramDataGroup.excludePixels(excludePixels);

				// get updated histogram data and show it
				_histogramPanel.setStatistics(_histogramDataGroup.getStatistics(WIDTH));
			}
		}

		@Override
		public void setCombineChannels(boolean combineChannels) {
			synchronized (_synchObject) {
				_histogramDataGroup.setCombineChannels(combineChannels);
			}
		}

		@Override
		public void setDisplayChannels(boolean displayChannels) {
			synchronized (_synchObject) {
				_histogramDataGroup.setDisplayChannels(displayChannels);

				// get updated histogram data and show it
				_histogramPanel.setStatistics(_histogramDataGroup.getStatistics(WIDTH));
			}
		}

		@Override
		public void setLogarithmicDisplay(boolean log) {
			_histogramPanel.setLogarithmicDisplay(log);
		}

		@Override
		public void setSmoothing(boolean smooth) {
			_histogramPanel.setSmoothing(smooth);
		}

		@Override
		public void setBandwidth(double bandwidth) {
			_histogramPanel.setBandwidth(bandwidth);
		}

		@Override
		public void setFamilyStyle1(boolean on) {
			_histogramPanel.setFamily1(on);
		}

		@Override
		public void setFamilyStyle2(boolean on) {
			_histogramPanel.setFamily2(on);
		}

		@Override
		public void setMinMaxLUT(double minLUT, double maxLUT) {
			boolean changed = false;
			double minView = 0;
			double maxView = 0;

			// silently ignores errors; c/b temporary condition such as setting
			// maximum before minimum
			if (minLUT < maxLUT) {
				changed = true;

				synchronized (_synchObject) { //TODO redo half-assed synchronization in 1.2
					// adjust view range to LUT range
					minView = minLUT;
					maxView = maxLUT;
					_histogramDataGroup.setMinMaxView(minView, maxView);
					_histogramDataGroup.setMinMaxLUT(minLUT, maxLUT);
				}
			}

			if (changed) {
				// update histogram and color bar
				changed(minView, maxView, minLUT, maxLUT);

				if (_excludePixels) {
					_histogramDataGroup.excludePixels(true);
				}
			}
		}
	}
}
