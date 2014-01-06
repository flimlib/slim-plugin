/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2014, UW-Madison LOCI
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

package loci.slim2.process.interactive.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

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

import loci.slim2.process.interactive.cursor.FittingCursor;
import loci.slim2.process.interactive.cursor.FittingCursorListener;

/**
 * Graph that displays the excitation (also known as the prompt, instrument
 * response function, or lamp function).
 * 
 * @author Aivar Grislis
 */
public class DefaultExcitationGraph implements ExcitationGraph, IStartStopBaseProportionListener {
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
    Double start;
    Double stop;
    Double base;
    int bins;
    double maxHorzValue;
    double maxVertValue;
    FittingCursor fittingCursor;
    FittingCursorListener fittingCursorListener;
    StartStopBaseDraggingUI<JComponent> startStopBaseDraggingUI;
    boolean headless = false;
    boolean logarithmic = false;
    XYPlot excitationPlot;
    XYSeriesCollection excitationDataset;
    XYSeriesCollection residualDataset;
    static ChartPanel panel;
    JXLayer<JComponent> layer;

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
    DefaultExcitationGraph(final double start, final double stop, final double base,
            final int bins, double[] values, final double timeInc) {
        this.start = start;
        this.stop = stop;
        this.base = base;
        this.bins = bins;

        // compute maximum values for width and height
        maxHorzValue = timeInc * bins;
        maxVertValue = 0.0f;
        for (double value : values) {
            if (value > maxVertValue) {
                maxVertValue = value;
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
        layer = new JXLayer<JComponent>(chartPanel);
        startStopBaseDraggingUI = new StartStopBaseDraggingUI<JComponent>
                (chartPanel, excitationPlot, this, maxHorzValue, maxVertValue);
        layer.setUI(startStopBaseDraggingUI);

        // initialize the vertical bars that show start and stop time bins and
        // the horizontal bar with the base count.
        startStopBaseDraggingUI.setStartStopBaseValues(start, stop, base);
    }

	@Override
    public JComponent getComponent() {
        return layer;
    }

	@Override
    public void setFittingCursor(FittingCursor fittingCursor) {
        if (null == this.fittingCursor) {
			// first time, make a listener
            fittingCursorListener = new FittingCursorListenerImpl();
        }
        else if (this.fittingCursor != fittingCursor) {
			// changed, don't listen to old version any more
            this.fittingCursor.removeListener(fittingCursorListener);
        }
        this.fittingCursor = fittingCursor;
		
		// listen to new version
        fittingCursor.addListener(fittingCursorListener);
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
        double newStart = startProportion * maxHorzValue;
        double newStop  = stopProportion  * maxHorzValue;
        double newBase  = baseProportion  * maxVertValue;
        
        if (start != newStart || stop != newStop || base != newBase) {
            start = newStart;
            stop  = newStop;
            base  = newBase;
            
            if (null != fittingCursor) {
                fittingCursor.setPromptStartTime(start);
                fittingCursor.setPromptStopTime(stop);
                fittingCursor.setPromptBaselineValue(base);
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
        if (logarithmic) {
            photonAxis = new LogarithmicAxis(PHOTON_AXIS_LABEL);
        }
        else {
            photonAxis = new NumberAxis(PHOTON_AXIS_LABEL);
        }

        // make an excitation plot
        XYSplineRenderer excitationRenderer = new XYSplineRenderer();
        excitationRenderer.setSeriesShapesVisible(0, false);
        excitationRenderer.setSeriesPaint(0, EXCITATION_COLOR);

        excitationPlot = new XYPlot(excitationDataset, timeAxis, photonAxis, excitationRenderer);
        excitationPlot.setDomainCrosshairVisible(true);
        excitationPlot.setRangeCrosshairVisible(true);

        // now make the top level JFreeChart
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, excitationPlot, true);
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
            if (logarithmic) {
                // logarithmic plots can't handle <= 0.0
                series.add(xCurrent, (yData > 0.0 ? yData : null));
            }
            else {
                series.add(xCurrent, yData);
            }
            xCurrent += timeInc;
        }

        excitationDataset = new XYSeriesCollection();
        excitationDataset.addSeries(series);
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
        private ChartPanel panel;
        private XYPlot plot;
        private double maxHorzValue;
        private double maxVertValue;
        private IStartStopBaseProportionListener listener;
        boolean draggingStartMarker = false;
        boolean draggingStopMarker = false;
        boolean draggingBaseMarker = false;
        private volatile Double startMarkerProportion;
        private volatile Double stopMarkerProportion;
        private volatile Double baseMarkerProportion;
        private int x0;
        private int y0;
        private int x1;
        private int y1;
        private int xStart;
        private int xStop;
        private int yBase;

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
            this.panel        = panel;
            this.plot         = plot;
            this.listener     = listener;
            this.maxHorzValue = maxHorzValue;
            this.maxVertValue = maxVertValue;
        }

        void setStartStopBaseValues
                (double startValue, double stopValue, double baseValue)
        {
            startMarkerProportion = startValue / maxHorzValue;
            stopMarkerProportion  = stopValue  / maxHorzValue;
            baseMarkerProportion  = baseValue  / maxVertValue;
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
            
            if (null != startMarkerProportion
                    && null != stopMarkerProportion
                    && null != baseMarkerProportion)
            {
                // adjust to current size
                Rectangle2D area = getDataArea();
                double x = area.getX();
                double y = area.getY();
                x0 = (int) area.getX();
                y0 = (int) area.getY();
                x1 = (int) (area.getX() + area.getWidth());
                y1 = (int) (area.getY() + area.getHeight());
                double width = area.getWidth();
                double height = area.getHeight();
                xStart = (int) Math.round(x + width * startMarkerProportion)
                        + HORZ_TWEAK;
                xStop  = (int) Math.round(x + width * stopMarkerProportion)
                        + HORZ_TWEAK;
                yBase  = (int) Math.round(y + height * (1.0 - baseMarkerProportion))
                        + VERT_TWEAK;

                // custom painting is here
                g2D.setStroke(new BasicStroke(2f));
                g2D.setXORMode(XORvalue(START_COLOR));
                g2D.drawLine(xStart, y0, xStart, y1);
                g2D.setXORMode(XORvalue(STOP_COLOR));
                g2D.drawLine(xStop, y0, xStop, y1);
                g2D.setXORMode(XORvalue(BASE_COLOR));
                g2D.drawLine(x0, yBase, x1, yBase);
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
                if (draggingStartMarker || draggingStopMarker) {
                    double newProportion = getHorzDraggedProportion(e);
                    if (draggingStartMarker) {
                        if (newProportion <= stopMarkerProportion) {
                            startMarkerProportion = newProportion;
                        }
                        else {
                            startMarkerProportion = stopMarkerProportion;
                        }
                    }
                    else {
                        if (newProportion >= startMarkerProportion) {
                            stopMarkerProportion = newProportion;
                        }
                        else {
                            stopMarkerProportion = startMarkerProportion;
                        }
                    }
                    // mark the ui as dirty and needed to be repainted
                    setDirty(true);
                }
                else if (draggingBaseMarker) {
                    baseMarkerProportion = getVertDraggedProportion(e);

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
                    panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            Rectangle2D area = getDataArea();
            double proportion = ((double) e.getX() - area.getX())
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
                    panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            Rectangle2D area = getDataArea();
            //double proportion = ((double) e.getY() - area.getY()) / area.getHeight();
            double proportion =
                    ((double) area.getY() + area.getHeight() - e.getY())
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
            if (null != startMarkerProportion && null != stopMarkerProportion && null != baseMarkerProportion) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    int x = e.getX();
                    int y = e.getY();
                    if (y > y0 - CLOSE_ENOUGH && y < y1 + CLOSE_ENOUGH) {
                        if (Math.abs(x - xStart) < CLOSE_ENOUGH) {
                            // check for superimposition
                            if (xStart == xStop) {
                                // both superimposed
                                if ( x < xStart) {
                                    // start dragging start line
                                    draggingStartMarker = true;
                                }
                                else {
                                     // start dragging stop line
                                    draggingStopMarker = true;
                                }
                            }
                            else {
                                // no superimposition, start dragging start line
                                draggingStartMarker = true;
                            }
                        }
                        else if (Math.abs(x - xStop) < CLOSE_ENOUGH) {
                            // start dragging stop line
                            draggingStopMarker = true;
                        }
                        else if (Math.abs(y - yBase) < CLOSE_ENOUGH) {
                            // start dragging base line
                            draggingBaseMarker = true;
                        }
                    }
                }
                if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                    draggingStartMarker = draggingStopMarker
                            = draggingBaseMarker = false;
                    SwingUtilities.invokeLater(
                            new Runnable() {
                                @Override
                                public void run() {
                                    listener.setStartStopBaseProportion(
                                            startMarkerProportion,
                                            stopMarkerProportion,
                                            baseMarkerProportion);
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
        @Deprecated  //TODO ARG why deprecate stuff I'm using myself ?????
        private Rectangle2D getDataArea() {
            Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
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
            return plot.getDomainAxis().java2DToValue((double) x, getDataArea(), RectangleEdge.TOP);
        }

        /**
         * Converts screen y to chart y value.
         *
         * @param y
         * @return chart value
         */
        @Deprecated
        private double vertScreenToValue(int y) {
            return plot.getRangeAxis().java2DToValue((double) y, getDataArea(), RectangleEdge.LEFT);
        }
    }
    
    private class FittingCursorListenerImpl implements FittingCursorListener {
        @Override
        public void cursorChanged(FittingCursor cursor) {
            double promptStart    = cursor.getPromptStartTime();
            double promptStop     = cursor.getPromptStopTime();
            double promptBaseline = cursor.getPromptBaselineValue();
            startStopBaseDraggingUI.setStartStopBaseValues(promptStart, promptStop, promptBaseline);
            layer.repaint();
        }
    }  
}

/**
 * Used within DefaultExcitationGraph, to get results from StartStopBaseDraggingUI
 * inner class.
 *
 * @author Aivar Grislis
 */
interface IStartStopBaseProportionListener {
	
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
            double baseProportion);
}

