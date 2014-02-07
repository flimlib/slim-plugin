/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import loci.slim.fitting.cursor.FittingCursor;
import loci.slim.fitting.cursor.IFittingCursorListener;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

/**
 * Graph that displays the excitation or prompt.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationGraph.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationGraph.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class ExcitationGraph implements IStartStopBaseProportionListener {
	static final Dimension SIZE = new java.awt.Dimension(500, 270);
	static final String PHOTON_AXIS_LABEL = "Photons";
	static final String TIME_AXIS_LABEL = "Time";
	static final String UNITS_LABEL = "nanoseconds";
	static final int HORZ_TWEAK = 1;
	static final int VERT_TWEAK = 1;
	static final Color EXCITATION_COLOR = Color.GRAY;
	static final Color BACK_COLOR = Color.WHITE;
	static final Color START_COLOR = Color.BLUE.darker();
	static final Color STOP_COLOR = Color.RED.darker();
	static final Color BASE_COLOR = Color.GREEN.darker();
	Double _start;
	Double _stop;
	Double _base;
	int _bins;
	double _maxHorzValue;
	double _maxVertValue;
	FittingCursor _fittingCursor;
	IFittingCursorListener _fittingCursorListener;
	StartStopBaseDraggingUI<JComponent> _startStopBaseDraggingUI;
	boolean _headless = false;
	boolean _logarithmic = false;
	XYPlot _excitationPlot;
	XYSeriesCollection _excitationDataset;
	XYSeriesCollection _residualDataset;
	static ChartPanel _panel;
	JXLayer<JComponent> _layer;

	/**
	 * Creates a JFreeChart graph showing the excitation or instrument response
	 * decay curve.
	 *
	 * @param start time value
	 * @param stop time value
	 * @param base value
	 * @param bins number of bins
	 * @param timeInc time increment per bin
	 * @param values
	 */
	ExcitationGraph(final double start, final double stop, final double base,
			final int bins, double[] values, final double timeInc) {
		_start = start;
		_stop = stop;
		_base = base;
		_bins = bins;

		// compute maximum values for width and height
		_maxHorzValue = timeInc * bins;
		_maxVertValue = 0.0f;
		for (double value : values) {
			if (value > _maxVertValue) {
				_maxVertValue = value;
			}
		}

		// create the chart
		JFreeChart chart = createChart(bins, timeInc, values);
		ChartPanel chartPanel = new ChartPanel
				(chart, true, true, true, false, true);
		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);
		chartPanel.setPreferredSize(SIZE);

		// Add JXLayer to draw/drag start/stop bars
		_layer = new JXLayer<JComponent>(chartPanel);
		_startStopBaseDraggingUI = new StartStopBaseDraggingUI<JComponent>
				(chartPanel, _excitationPlot, this, _maxHorzValue, _maxVertValue);
		_layer.setUI(_startStopBaseDraggingUI);

	 //System.out.println("ExcitationGraph start " + start + " stop " + stop + " base " + base);
		// initialize the vertical bars that show start and stop time bins and
		// the horizontal bar with the base count.
		_startStopBaseDraggingUI.setStartStopBaseValues(_start, _stop, _base);
	}

	/**
	 * Gets the chart JPanel
	 *
	 * @return JFrame
	 */
	public JComponent getComponent() {
		return _layer;
	}

	/**
	 * Sets the fitting cursor.
	 * 
	 * @param fittingCursor 
	 */
	public void setFittingCursor(FittingCursor fittingCursor) {
		if (null == _fittingCursor) {
			_fittingCursorListener = new FittingCursorListener();
		}
		else if (_fittingCursor != fittingCursor) {
			_fittingCursor.removeListener(_fittingCursorListener);
		}
		_fittingCursor = fittingCursor;
		_fittingCursor.addListener(_fittingCursorListener);
	}

	/**
	 * Sets stop and start time bins, based on proportions 0.0..1.0.  This is called from
	 * the UI layer that lets user drag the start and stop vertical bars.  Validates
	 * and passes changes on to external listener.
	 *
	 * @param startProportion
	 * @param stopProportion
	 */
	public void setStartStopBaseProportion(
			double startProportion,
			double stopProportion,
			double baseProportion)
	{
		// calculate new start, stop and base
		double start = startProportion * _maxHorzValue;
		double stop  = stopProportion  * _maxHorzValue;
		double base  = baseProportion  * _maxVertValue;

		if (start != _start || stop != _stop || base != _base) {
			_start = start;
			_stop  = stop;
			_base  = base;

			if (null != _fittingCursor) {
				_fittingCursor.setPromptStartValue(start);
				_fittingCursor.setPromptStopValue(stop);
				_fittingCursor.setPromptBaselineValue(base);
			}
		}
	}

	/**
	 * Creates the chart
	 *
	 * @param bins number of bins
	 * @param timeInc time increment per bin
	 * @param data fitted data
	 * @return the chart
	 */
	JFreeChart createChart(int bins, double timeInc, double[] values) {

		// create chart data
		createDataset(bins, timeInc, values);

		// make a horizontal axis
		NumberAxis timeAxis = new NumberAxis(TIME_AXIS_LABEL);
		timeAxis.setLabel(UNITS_LABEL);
		timeAxis.setRange(0.0, (bins - 1) * timeInc);

		// make a vertical axis
		NumberAxis photonAxis;
		if (_logarithmic) {
			photonAxis = new LogarithmicAxis(PHOTON_AXIS_LABEL);
		}
		else {
			photonAxis = new NumberAxis(PHOTON_AXIS_LABEL);
		}

		// make an excitation plot
		XYSplineRenderer excitationRenderer = new XYSplineRenderer();
		excitationRenderer.setSeriesShapesVisible(0, false);
		excitationRenderer.setSeriesPaint(0, EXCITATION_COLOR);

		_excitationPlot = new XYPlot(_excitationDataset, timeAxis, photonAxis, excitationRenderer);
		_excitationPlot.setDomainCrosshairVisible(true);
		_excitationPlot.setRangeCrosshairVisible(true);

		// now make the top level JFreeChart
		JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, _excitationPlot, true);
		chart.removeLegend();

		return chart;
	}

	/**
	 * Creates the data set for the chart
	 *
	 * @param bins number of time bins
	 * @param timeInc time increment per time bin
	 * @param data from the fit
	 */
	private void createDataset(int bins, double timeInc, double[] values) {
		XYSeries series = new XYSeries("Data");
		double yData, yFitted;
		double xCurrent = 0;
		for (int i = 0; i < bins; ++i) {
			yData = values[i];
			if (_logarithmic) {
				// logarithmic plots can't handle <= 0.0
				series.add(xCurrent, (yData > 0.0 ? yData : null));
			}
			else {
				series.add(xCurrent, yData);
			}
			xCurrent += timeInc;
		}

		_excitationDataset = new XYSeriesCollection();
		_excitationDataset.addSeries(series);
	}

	/**
	 * UI which allows us to paint on top of the components, using JXLayer.
	 *
	 * @param <V> component
	 */
	static class StartStopBaseDraggingUI<V extends JComponent>
			extends AbstractLayerUI<V>
	{
		private static final int CLOSE_ENOUGH = 4; // pizels
		private ChartPanel _panel;
		private XYPlot _plot;
		private double _maxHorzValue;
		private double _maxVertValue;
		private IStartStopBaseProportionListener _listener;
		boolean _draggingStartMarker = false;
		boolean _draggingStopMarker = false;
		boolean _draggingBaseMarker = false;
		private volatile Double _startMarkerProportion;
		private volatile Double _stopMarkerProportion;
		private volatile Double _baseMarkerProportion;
		private int _x0;
		private int _y0;
		private int _x1;
		private int _y1;
		private int _xStart;
		private int _xStop;
		private int _yBase;

		/**
		 * Creates the UI.
		 *
		 * @param panel for the chart
		 * @param plot within the chart
		 * @param listener to be notified when user drags start/stop/base bars
		 */
		StartStopBaseDraggingUI(ChartPanel panel, XYPlot plot,
				IStartStopBaseProportionListener listener,
				double maxHorzValue, double maxVertValue)
		{
			_panel        = panel;
			_plot         = plot;
			_listener     = listener;
			_maxHorzValue = maxHorzValue;
			_maxVertValue = maxVertValue;
		}

		void setStartStopBaseValues
				(double startValue, double stopValue, double baseValue)
		{
			_startMarkerProportion = startValue / _maxHorzValue;
			_stopMarkerProportion  = stopValue  / _maxHorzValue;
			_baseMarkerProportion  = baseValue  / _maxVertValue;
		}

		/**
		 * Used to draw the start/stop vertical bars.
		 *
		 * Overrides 'paintLayer()', not 'paint()'.
		 *
		 * @param g2D
		 * @param l
		 */
		@Override
		protected void paintLayer(Graphics2D g2D, JXLayer<? extends V> l) {
			// this paints layer as is
			super.paintLayer(g2D, l);

			if (null != _startMarkerProportion
					&& null != _stopMarkerProportion
					&& null != _baseMarkerProportion)
			{
				// adjust to current size
				Rectangle2D area = getDataArea();
				double x = area.getX();
				double y = area.getY();
				_x0 = (int) area.getX();
				_y0 = (int) area.getY();
				_x1 = (int) (area.getX() + area.getWidth());
				_y1 = (int) (area.getY() + area.getHeight());
				double width = area.getWidth();
				double height = area.getHeight();
				_xStart = (int) Math.round(x + width * _startMarkerProportion)
						+ HORZ_TWEAK;
				_xStop  = (int) Math.round(x + width * _stopMarkerProportion)
						+ HORZ_TWEAK;
				_yBase  = (int) Math.round(y + height * (1.0 - _baseMarkerProportion))
						+ VERT_TWEAK;

				// custom painting is here
				g2D.setStroke(new BasicStroke(2f));
				g2D.setXORMode(XORvalue(START_COLOR));
				g2D.drawLine(_xStart, _y0, _xStart, _y1);
				g2D.setXORMode(XORvalue(STOP_COLOR));
				g2D.drawLine(_xStop, _y0, _xStop, _y1);
				g2D.setXORMode(XORvalue(BASE_COLOR));
				g2D.drawLine(_x0, _yBase, _x1, _yBase);
			}

		}

		/**
		 * Mouse listener, catches drag events
		 *
		 * @param e
		 * @param l
		 */
		@Override
		protected void processMouseMotionEvent(MouseEvent e, JXLayer<? extends V> l) {
			super.processMouseMotionEvent(e, l);
			if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
				if (_draggingStartMarker || _draggingStopMarker) {
					double newProportion = getHorzDraggedProportion(e);
					if (_draggingStartMarker) {
						if (newProportion <= _stopMarkerProportion) {
							_startMarkerProportion = newProportion;
						}
						else {
							_startMarkerProportion = _stopMarkerProportion;
						}
					}
					else {
						if (newProportion >= _startMarkerProportion) {
							_stopMarkerProportion = newProportion;
						}
						else {
							_stopMarkerProportion = _startMarkerProportion;
						}
					}
					// mark the ui as dirty and needed to be repainted
					setDirty(true);
				}
				else if (_draggingBaseMarker) {
					_baseMarkerProportion = getVertDraggedProportion(e);

					// mark the ui as dirty and needed to be repainted
					setDirty(true);
				}
			}
		}

		private Color XORvalue(Color color) {
			int drawRGB = color.getRGB();
			int backRGB = BACK_COLOR.getRGB();
			return new Color(drawRGB ^ backRGB);
		}

		/**
		 * Gets the currently dragged horizontal value as a proportion,
		 * a value between 0.0 and 1.0.
		 *
		 * @param e
		 * @return proportion
		 */
		private double getHorzDraggedProportion(MouseEvent e) {
			Rectangle2D dataArea =
					_panel.getChartRenderingInfo().getPlotInfo().getDataArea();
			Rectangle2D area = getDataArea();
			double proportion = (e.getX() - area.getX())
					/ area.getWidth();
			if (proportion < 0.0) {
				proportion = 0.0;
			}
			else if (proportion > 1.0) {
				proportion = 1.0;
			}
			return proportion;
		}

		/**
		 * Gets the currently dragged vertical value as a proportion,
		 * a value between 0.0 and 1.0.
		 *
		 * @param e
		 * @return proportion
		 */
		private double getVertDraggedProportion(MouseEvent e) {
			Rectangle2D dataArea =
					_panel.getChartRenderingInfo().getPlotInfo().getDataArea();
			Rectangle2D area = getDataArea();
			//double proportion = ((double) e.getY() - area.getY()) / area.getHeight();
			double proportion =
					(area.getY() + area.getHeight() - e.getY())
						/ area.getHeight();
			if (proportion < 0.0) {
				proportion = 0.0;
			}
			else if (proportion > 1.0) {
				proportion = 1.0;
			}
			return proportion;
		}

		/**
		 * Mouse listener, catches mouse button events.
		 * @param e
		 * @param l
		 */
		protected void processMouseEvent(MouseEvent e, JXLayer<? extends V> l) {
			super.processMouseEvent(e, l);
			if (null != _startMarkerProportion && null != _stopMarkerProportion && null != _baseMarkerProportion) {
				if (e.getID() == MouseEvent.MOUSE_PRESSED) {
					int x = e.getX();
					int y = e.getY();
					if (y > _y0 - CLOSE_ENOUGH && y < _y1 + CLOSE_ENOUGH) {
						if (Math.abs(x - _xStart) < CLOSE_ENOUGH) {
							// check for superimposition
							if (_xStart == _xStop) {
								// both superimposed
								if ( x < _xStart) {
									// start dragging start line
									_draggingStartMarker = true;
								}
								else {
									 // start dragging stop line
									_draggingStopMarker = true;
								}
							}
							else {
								// no superimposition, start dragging start line
								_draggingStartMarker = true;
							}
						}
						else if (Math.abs(x - _xStop) < CLOSE_ENOUGH) {
							// start dragging stop line
							_draggingStopMarker = true;
						}
						else if (Math.abs(y - _yBase) < CLOSE_ENOUGH) {
							// start dragging base line
							_draggingBaseMarker = true;
						}
					}
				}
				if (e.getID() == MouseEvent.MOUSE_RELEASED) {
					_draggingStartMarker = _draggingStopMarker
							= _draggingBaseMarker = false;
					SwingUtilities.invokeLater(
							new Runnable() {
								@Override
								public void run() {
									_listener.setStartStopBaseProportion(
											_startMarkerProportion,
											_stopMarkerProportion,
											_baseMarkerProportion);
								}
					});
				}
			}
		}

		/**
		 * Gets the area of the chart panel.
		 *
		 * @return 2D rectangle area
		 */
		@Deprecated
		private Rectangle2D getDataArea() {
			Rectangle2D dataArea = _panel.getChartRenderingInfo().getPlotInfo().getDataArea();
			return dataArea;
		}

		/**
		 * Converts screen x to chart x value.
		 *
		 * @param x
		 * @return chart value
		 */
		@Deprecated
		private double horzScreenToValue(int x) {
			return _plot.getDomainAxis().java2DToValue(x, getDataArea(), RectangleEdge.TOP);
		}

		/**
		 * Converts screen y to chart y value.
		 *
		 * @param y
		 * @return chart value
		 */
		@Deprecated
		private double vertScreenToValue(int y) {
			return _plot.getRangeAxis().java2DToValue(y, getDataArea(), RectangleEdge.LEFT);
		}
	}

	private class FittingCursorListener implements IFittingCursorListener {
		@Override
		public void cursorChanged(FittingCursor cursor) {
			double promptStart    = cursor.getPromptStartValue();
			double promptStop     = cursor.getPromptStopValue();
			double promptBaseline = cursor.getPromptBaselineValue();
			_startStopBaseDraggingUI.setStartStopBaseValues(promptStart, promptStop, promptBaseline);
			_layer.repaint();
		}
	}
}

/**
 * Used within ExcitationGraph, to get results from StartStopBaseDraggingUI
 * inner class.
 *
 * @author Aivar Grislis
 */
interface IStartStopBaseProportionListener {
	public void setStartStopBaseProportion(
			double startProportion,
			double stopProportion,
			double baseProportion);
}
