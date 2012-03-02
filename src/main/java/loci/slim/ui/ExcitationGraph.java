//
// ExcitationGraph.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
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

package loci.slim.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import loci.slim.fitting.cursor.FittingCursor;
import loci.slim.fitting.cursor.IFittingCursorListener;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationGraph.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationGraph.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class ExcitationGraph implements IStartStopBaseProportionListener {
    static final int HORZ_TWEAK = 0; //TODO this was necessary for the fitted decay graph: 4;
    static final Color EXCITATION_COLOR = Color.GRAY;
    static final Color BACK_COLOR = Color.WHITE;
    static final Color START_COLOR = Color.BLUE.darker();
    static final Color STOP_COLOR = Color.RED.darker();
    static final Color BASE_COLOR = Color.GREEN.darker();
    JFrame _frame;
    FittingCursor _fittingCursor;
    IFittingCursorListener _fittingCursorListener;
    int _start;
    int _stop;
    double _base;
    int _bins;
    double _count;
    StartStopBaseDraggingUI<JComponent> _startStopBaseDraggingUI;
    boolean _headless = false;
    boolean _logarithmic = false;
    XYPlot _excitationPlot;
    XYSeriesCollection _excitationDataset;
    XYSeriesCollection _residualDataset;
    static ChartPanel _panel;
    JXLayer<JComponent> _layer;

    JFreeChart _decayChart;
    JFreeChart _residualsChart;

    /**
     * Creates a JFreeChart graph showing the excitation or instrument response
     * decay curve.
     *
     * @param start time bin
     * @param stop time bin
     * @param base count
     * @param bins number of bins
     * @param timeInc time increment per bin
     * @param values
     */
    ExcitationGraph(final int start, final int stop, final double base,
            final int bins, double[] values, final double timeInc) {
        _start = start;
        _stop = stop;
        _base = base;
        _bins = bins;
        _count = 0.0f;
        // find maximum count
        for (double value : values) {
            if (value > _count) {
                _count = value;
            }
        }

        // create the chart
        JFreeChart chart = createChart(bins, timeInc, values);
        ChartPanel chartPanel = new ChartPanel(chart, true, true, true, false, true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        // Add JXLayer to draw/drag start/stop bars
        _layer = new JXLayer<JComponent>(chartPanel);
        _startStopBaseDraggingUI = new StartStopBaseDraggingUI<JComponent>(chartPanel, _excitationPlot, this);
        _layer.setUI(_startStopBaseDraggingUI);

     System.out.println("ExcitationGraph start " + start + " stop " + stop + " base " + base);
        // initialize the vertical bars that show start and stop time bins and
        // the horizontal bar with the base count.
        _startStopBaseDraggingUI.setStartStopBaseValues(
                timeInc * start, timeInc * stop, timeInc * bins,
                base, _count);
    }

    /**
     * Gets the chart JPanel
     *
     * @return JFrame
     */
    public JComponent getComponent() {
        return _layer;
    }
    
    public void setFittingCursor(FittingCursor fittingCursor) {
        if (null == _fittingCursor) {
            _fittingCursorListener = new FittingCursorListener();
        }
        else {
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
            double startProportion, double stopProportion, double baseProportion) {
        int start = (int) (startProportion * _bins + 0.5);
        int stop = (int) (stopProportion * _bins + 0.5);
        int base = (int) (baseProportion * _count + 0.5);
        //System.out.println("start " + start + " stop " + stop);
        if (start != _start || stop != _stop || base != _base) {
            _fittingCursor.setPromptStartBin(start);
            _fittingCursor.setPromptStopBin(stop);
            _fittingCursor.setPromptBaselineValue(base);
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
        NumberAxis timeAxis = new NumberAxis("Time");
        timeAxis.setLabel("nanoseconds");
        timeAxis.setRange(0.0, (bins - 1) * timeInc);

        // make a vertical axis
        NumberAxis photonAxis;
        if (_logarithmic) {
            photonAxis = new LogarithmicAxis("Photons");
        }
        else {
            photonAxis = new NumberAxis("Photons");
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
    static class StartStopBaseDraggingUI<V extends JComponent> extends AbstractLayerUI<V> {
        private static final int CLOSE_ENOUGH = 4; // pizels
        private ChartPanel _panel;
        private XYPlot _plot;
        private IStartStopBaseProportionListener _listener;
        boolean _draggingStartMarker = false;
        boolean _draggingStopMarker = false;
        boolean _draggingBaseMarker = false;
        private double _startMarkerProportion = 0.25;
        private double _stopMarkerProportion = 0.75;
        private double _baseMarkerProportion = 0.25;
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
        StartStopBaseDraggingUI(ChartPanel panel, XYPlot plot, IStartStopBaseProportionListener listener) {
            _panel    = panel;
            _plot     = plot;
            _listener = listener;
        }

        void setStartStopBaseValues(double startValue, double stopValue, double maxHorzValue, double baseValue, double maxVertValue) {
            Rectangle2D area = getDataArea();
            double x = area.getX();
            double y = area.getY();
            double width = area.getWidth();
            double height = area.getHeight();

            if (0.1 > width) {
                _startMarkerProportion = startValue / maxHorzValue;
                _stopMarkerProportion = stopValue / maxHorzValue;
            }
            else {
                double minRepresentedValue = horzScreenToValue((int) x);
                double maxRepresentedValue = horzScreenToValue((int) (x + width));
                _startMarkerProportion = (startValue - minRepresentedValue) / (maxRepresentedValue - minRepresentedValue);
                _stopMarkerProportion = (stopValue - minRepresentedValue) / (maxRepresentedValue - minRepresentedValue);
            }

            if (0.1 > height) {
                _baseMarkerProportion = baseValue / maxVertValue;
            }
            else {
                double minRepresentedValue = vertScreenToValue((int) y);
                double maxRepresentedValue = vertScreenToValue((int) (y + height));
                _baseMarkerProportion = (baseValue - minRepresentedValue) / (maxRepresentedValue - minRepresentedValue);
            }
        }

        /**
         * Used to draw the start/stop vertical bars.
         *
         * Overrides 'paintLayer()', not 'paint()'.
         *
         * @param g2
         * @param l
         */
        @Override
        protected void paintLayer(Graphics2D g2, JXLayer<? extends V> l) {
            // this paints layer as is
            super.paintLayer(g2, l);

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
            _xStart = (int) Math.round(x + width * _startMarkerProportion) + HORZ_TWEAK;
            _xStop = (int) Math.round(x + width * _stopMarkerProportion) + HORZ_TWEAK;
            _yBase = (int) Math.round(y + height * (1 - _baseMarkerProportion));

            // custom painting is here
            g2.setStroke(new BasicStroke(2f));
            g2.setXORMode(XORvalue(START_COLOR));
            g2.drawLine(_xStart, _y0, _xStart, _y1);
            g2.setXORMode(XORvalue(STOP_COLOR));
            g2.drawLine(_xStop, _y0, _xStop, _y1);
            g2.setXORMode(XORvalue(BASE_COLOR));
            g2.drawLine(_x0, _yBase, _x1, _yBase);
        }

        /**
         * Mouse listener, catches drag events
         *
         * @param e
         * @param l
         */
        protected void processMouseMotionEvent(MouseEvent e, JXLayer<? extends V> l) {
            super.processMouseMotionEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                if (_draggingStartMarker || _draggingStopMarker) {
                    double newProportion = getHorzDraggedProportion(e);
                    if (_draggingStartMarker) {
                        if (newProportion <= _stopMarkerProportion) {
                            _startMarkerProportion = newProportion;
                        }
                    }
                    else {
                        if (newProportion >= _startMarkerProportion) {
                            _stopMarkerProportion = newProportion;
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
            Rectangle2D dataArea = _panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            Rectangle2D area = getDataArea();
            double proportion = ((double) e.getX() - area.getX()) / area.getWidth();
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
            Rectangle2D dataArea = _panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            Rectangle2D area = getDataArea();
            //double proportion = ((double) e.getY() - area.getY()) / area.getHeight();
            double proportion = ((double) area.getY() + area.getHeight() - e.getY()) / area.getHeight();
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
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                int x = e.getX();
                int y = e.getY();
                if (y > _y0 - CLOSE_ENOUGH && y < _y1 + CLOSE_ENOUGH) {
                    if (Math.abs(x - _xStart) < CLOSE_ENOUGH) {
                        // start dragging start line
                        _draggingStartMarker = true;

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
                _draggingStartMarker = _draggingStopMarker = _draggingBaseMarker = false;
                SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                _listener.setStartStopBaseProportion(_startMarkerProportion, _stopMarkerProportion, _baseMarkerProportion);
                            }
                });
            }
        }

        /**
         * Gets the area of the chart panel.
         *
         * @return 2D rectangle area
         */
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
        private double horzScreenToValue(int x) {
            return _plot.getDomainAxis().java2DToValue((double) x, getDataArea(), RectangleEdge.TOP);
        }

        /**
         * Converts screen y to chart y value.
         *
         * @param y
         * @return chart value
         */
        private double vertScreenToValue(int y) {
            return _plot.getRangeAxis().java2DToValue((double) y, getDataArea(), RectangleEdge.LEFT);
        }
    }
    
    private class FittingCursorListener implements IFittingCursorListener {
        public void cursorChanged(FittingCursor cursor) {
            int promptStart       = cursor.getPromptStartBin();
            int promptStop        = cursor.getPromptStopBin();
            double promptBaseline = cursor.getPromptBaselineValue();
            //setStartStop(transientStart, dataStart, transientStop);
            _frame.repaint();
            System.out.println("PROMPT CHANGED " + promptStart + " " + promptStop + " " + promptBaseline);
        }
    }  
}

/**
 * Used within ExcitationGraph, to get results from StartStopBaseDraggingUI
 * inner class.
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
interface IStartStopBaseProportionListener {
    public void setStartStopBaseProportion(double startProportion, double stopProportion, double baseProportion);
}
