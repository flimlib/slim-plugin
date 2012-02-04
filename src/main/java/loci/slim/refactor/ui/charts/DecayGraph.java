/*
Combined spectral-lifetime image analysis plugin.

Copyright (c) 2011, UW-Madison LOCI
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

package loci.slim.refactor.ui.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import loci.curvefitter.ICurveFitData;
import loci.slim.ui.IStartStopListener;

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


/**
 * This is the chart that shows the transient decay data, the fitted model,
 * and the residuals.
 *
 * It also has a user interface to set the start and stop of the fit.
 *
 * @author Aivar Grislis
 */
public class DecayGraph implements IDecayGraph, IStartStopProportionListener {
    static final Dimension SIZE = new Dimension(500, 270);
    static final Dimension FRAME_SIZE = new Dimension(450, 450);
    static final String PHOTON_AXIS_LABEL = "Photons";
    static final String TIME_AXIS_LABEL = "Time";
    static final String UNITS_LABEL = "nanoseconds";
    static final String RESIDUAL_AXIS_LABEL = "Residual";
    static final int HORZ_TWEAK = 4;
    static final Color IRF_COLOR = Color.GRAY;
    static final Color DECAY_COLOR = Color.GRAY.darker();
    static final Color FITTED_COLOR = Color.RED;
    static final Color BACK_COLOR = Color.WHITE;
    static final Color START_COLOR = Color.BLUE.darker();
    static final Color STOP_COLOR = Color.RED.darker();
    static final Color BASE_COLOR = Color.GREEN.darker();
    static final Color RESIDUAL_COLOR = Color.BLACK;

    private static DecayGraph _instance;
    private JFrame _frame;
    IStartStopListener _startStopListener;

    private StartStopDraggingUI<JComponent> _startStopDraggingUI;
    private double _timeInc;
    private int _bins;
    private Integer _start;
    private Integer _stop;
    private boolean _logarithmic = true;

    XYPlot _decaySubPlot;
    XYSeriesCollection _decayDataset;
    XYSeriesCollection _residualDataset;

    JFreeChart m_decayChart;
    JFreeChart m_residualsChart;

    /**
     * Public constructor, may be used to create non-singletons.
     */
    public DecayGraph() {
        _frame = null;
    }

    /**
     * Gets the singleton instance.
     *
     * @return singleton instance
     */
    public static synchronized DecayGraph getInstance() {
        if (null == _instance) {
            _instance = new DecayGraph();
        }
        return _instance;
    }
    
    /**
     * Sets up a listener to listen for start/stop changes.
     * 
     * @param startStopListener
     */
    public void setStartStopListener(IStartStopListener startStopListener) {
        _startStopListener = startStopListener;
    }

    /**
     * Initialize the graph and returns the containing JFrame.
     *
     * @param bins
     * @param timeInc
     * @return frame
     */
    public JFrame init(final JFrame frame, final int bins, final double timeInc) {
        boolean create = false;
        if (null == _frame
                || !_frame.isVisible()
                || _bins != bins
                || _timeInc != timeInc)
        {
            // save incoming parameters
            _bins = bins;
            _timeInc = timeInc;
            
            if (null != _frame) {
                // delete existing frame
                _frame.setVisible(false);
                _frame.dispose();
            }
            
             // create the combined chart
            JFreeChart chart = createCombinedChart(bins, timeInc);
            ChartPanel panel = new ChartPanel
                    (chart, true, true, true, false, true);
            panel.setDomainZoomable(false);
            panel.setRangeZoomable(false);
            panel.setPreferredSize(SIZE);

            // add start/stop vertical bar handling
            _start = _stop = null;
            JXLayer<JComponent> layer = new JXLayer<JComponent>(panel);
            _startStopDraggingUI =
                    new StartStopDraggingUI<JComponent>
                            (panel, _decaySubPlot, this);
            layer.setUI(_startStopDraggingUI);

            // create a frame for the chart
            _frame = new JFrame();
            _frame.getContentPane().add(layer);
            _frame.setSize(FRAME_SIZE);
            _frame.pack();
            _frame.setLocationRelativeTo(frame);
            _frame.setVisible(true);            
        }
        return _frame;
    }
    
    public void setTitle(final String title) {
        _frame.setTitle(title);
    }

    /**
     * Changes (or initializes) all of the charted data.
     *
     * @param irf
     * @param data
     */
    public void setData(final double[] irf, ICurveFitData data) {
        createDatasets(10, 200, _bins, _timeInc, irf, data);

    }

    /**
     * Changes (or initializes) the start and stop vertical bars.
     *
     * @param start
     * @param stop
     */
    public void setStartStop(int start, int stop) {
        if (null == _start) {
            // initialize the vertical bars
            double startValue = start * _timeInc;
            double stopValue = stop * _timeInc;
            double maxValue = _bins * _timeInc;
            _startStopDraggingUI.setStartStopValues
                    (startValue, stopValue, maxValue);
        }

        _start = start;
        _stop = stop;

    }

    /**
     * Sets stop and start time bins, based on proportions 0.0..1.0.  This is called from
     * the UI layer that lets user drag the start and stop vertical bars.  Validates
     * and passes changes on to external listener.
     *
     * @param startProportion
     * @param stopProportion
     */
    public void setStartStopProportion
            (double startProportion, double stopProportion)
    {
        // calculate new start and stop
        int start = (int) (startProportion * _bins + 0.5);
        int stop = (int) (stopProportion * _bins + 0.5);

        // if changed, notify listener
        if (start != _start || stop != _stop) {
            if (null != _startStopListener) {
                _startStopListener.setStartStop(start, stop);
            }
        }
    }

    /**
     * Creates the chart
     *
     * @param bins number of bins
     * @param timeInc time increment per bin
     * @return the chart
     */
    JFreeChart createCombinedChart(int bins, double timeInc) {

        // create empty chart data sets
        _decayDataset = new XYSeriesCollection();;
        _residualDataset = new XYSeriesCollection();;

        // make a common horizontal axis for both sub-plots
        NumberAxis timeAxis = new NumberAxis(TIME_AXIS_LABEL);
        timeAxis.setLabel(UNITS_LABEL);
        timeAxis.setRange(0.0, (bins - 1) * timeInc);

        // make a vertically combined plot
        CombinedDomainXYPlot parent = new CombinedDomainXYPlot(timeAxis);

        // create decay sub-plot
        NumberAxis photonAxis;
        if (_logarithmic) {
            photonAxis = new LogarithmicAxis(PHOTON_AXIS_LABEL);
        }
        else {
            photonAxis = new NumberAxis(PHOTON_AXIS_LABEL);
        }
        XYSplineRenderer decayRenderer = new XYSplineRenderer();
        decayRenderer.setSeriesShapesVisible(0, false);
        decayRenderer.setSeriesShapesVisible(1, false);
        decayRenderer.setSeriesLinesVisible(2, false);
        decayRenderer.setSeriesShape
                (2, new Ellipse2D.Float(-1.0f, -1.0f, 2.0f, 2.0f));

        decayRenderer.setSeriesPaint(0, IRF_COLOR);
        decayRenderer.setSeriesPaint(1, FITTED_COLOR);
        decayRenderer.setSeriesPaint(2, DECAY_COLOR);

        _decaySubPlot = new XYPlot
                (_decayDataset, null, photonAxis, decayRenderer);
        _decaySubPlot.setDomainCrosshairVisible(true);
        _decaySubPlot.setRangeCrosshairVisible(true);

        // add decay sub-plot to parent
        parent.add(_decaySubPlot, 4);

        // create residual sub-plot
        NumberAxis residualAxis = new NumberAxis(RESIDUAL_AXIS_LABEL);
        XYSplineRenderer residualRenderer = new XYSplineRenderer();
        residualRenderer.setSeriesShapesVisible(0, false);
        residualRenderer.setSeriesPaint(0, RESIDUAL_COLOR);
        XYPlot residualSubPlot = new XYPlot
                (_residualDataset, null, residualAxis, residualRenderer);
        residualSubPlot.setDomainCrosshairVisible(true);
        residualSubPlot.setRangeCrosshairVisible(true);
        residualSubPlot.setFixedLegendItems(null);

        // add residual sub-plot to parent
        parent.add(residualSubPlot, 1);

        // now make the top level JFreeChart
        JFreeChart chart = new JFreeChart
                (null, JFreeChart.DEFAULT_TITLE_FONT, parent, true);
        chart.removeLegend();

        return chart;
    }

    /**
     * Creates the data sets for the chart
     *
     * @param start time bin
     * @param stop time bin
     * @param bins number of time bins
     * @param timeInc time increment per time bin
     * @param data from the fit
     */
    private void createDatasets(int start, int stop, int bins, double timeInc,
            double[] irf, ICurveFitData data)
    {
        XYSeries series1 = new XYSeries("IRF");
        double xCurrent = 0;
        if (null != irf) {
            for (int i = 0; i < bins; ++i) {
                // logarithmic plots can't handle <= 0
                if (irf[i] > 0.0) {
                    series1.add(xCurrent, irf[i]);
                }
                else {
                    series1.add(xCurrent, null);
                }
                xCurrent += timeInc;
            }
        }

        XYSeries series2 = new XYSeries("Fitted");
        XYSeries series3 = new XYSeries("Data");
        XYSeries series4 = new XYSeries("Residuals");

        double yData, yFitted;
        xCurrent = 0;
        for (int i = 0; i < bins; ++i) {
            yData = data.getYCount()[i];
            // logarithmic plots can't handle <= 0.0
            series3.add(xCurrent, (yData > 0.0 ? yData : null));
            // are we in fitted region?
            if (_start <= i && i <= _stop) {
                // yes, show fitted curve and residuals
                yFitted = data.getYFitted()[i];
                // logarithmic plots can't handle <= 0
                if (yFitted > 0.0) {
                    series2.add(xCurrent, yFitted);
                    series4.add(xCurrent, yData - yFitted);
                }
                else {
                    series2.add(xCurrent, null);
                    series4.add(xCurrent, null);
                }
            }
            else {
                series2.add(xCurrent, null);
                series4.add(xCurrent, null);
            }
            xCurrent += timeInc;
        }

        _decayDataset.removeAllSeries();
        _decayDataset.addSeries(series1);
        _decayDataset.addSeries(series2);
        _decayDataset.addSeries(series3);

        _residualDataset.removeAllSeries();
        _residualDataset.addSeries(series4);
    }

    /**
     * Inner class, UI which allows us to paint on top of the components,
     * using JXLayer.
     *
     * @param <V> component
     */
    static class StartStopDraggingUI<V extends JComponent>
            extends AbstractLayerUI<V>
    {
        private static final int CLOSE_ENOUGH = 4; // pizels
        private ChartPanel _panel;
        private XYPlot _plot;
        private IStartStopProportionListener _listener;
        boolean _draggingStartMarker = false;
        boolean _draggingStopMarker = false;
        private Double _startMarkerProportion;
        private Double _stopMarkerProportion;
        private int _y0;
        private int _y1;
        private int _xStart;
        private int _xStop;

        /**
         * Creates the UI.
         *
         * @param panel for the chart
         * @param plot within the chart
         * @param listener to be notified when user drags start/stop vertical bars
         */
        StartStopDraggingUI(ChartPanel panel, XYPlot plot,
                IStartStopProportionListener listener)
        {
            _panel    = panel;
            _plot     = plot;
            _listener = listener;
            _startMarkerProportion = null;
            _stopMarkerProportion = null;
        }

        void setStartStopValues
                (double startValue, double stopValue, double maxValue)
        {
            Rectangle2D area = getDataArea();
            double x = area.getX();
            double width = area.getWidth();
            if (0.1 > width) {
                _startMarkerProportion = startValue / maxValue;
                _stopMarkerProportion = stopValue / maxValue;
            }
            else {
                double minRepresentedValue = screenToValue((int) x);
                double maxRepresentedValue = screenToValue((int) (x + width));
                _startMarkerProportion =
                        (float) (startValue - minRepresentedValue) /
                            (maxRepresentedValue - minRepresentedValue);
                _stopMarkerProportion =
                        (float) (stopValue - minRepresentedValue) /
                            (maxRepresentedValue - minRepresentedValue);
            }
        }

        /**
         * Used to draw the start/stop vertical bars.
         *
         * Overrides 'paintLayer()', not 'paint()'.
         *
         * @param g2D
         * @param layer
         */
        @Override
        protected void paintLayer(Graphics2D g2D, JXLayer<? extends V> layer) {
            // this paints layer as is
            super.paintLayer(g2D, layer);
            
            if (null != _startMarkerProportion && null != _stopMarkerProportion) {
                // adjust to current size
                Rectangle2D area = getDataArea();
                double x = area.getX();
                _y0 = (int) area.getY();
                _y1 = (int) (area.getY() + area.getHeight());
                double width = area.getWidth();
                _xStart = (int) Math.round(x + width * _startMarkerProportion)
                        + HORZ_TWEAK;
                _xStop = (int) Math.round(x + width * _stopMarkerProportion)
                        + HORZ_TWEAK;

                // custom painting is here
                g2D.setStroke(new BasicStroke(2f));
                g2D.setXORMode(XORvalue(START_COLOR));
                g2D.drawLine(_xStart, _y0, _xStart, _y1);
                g2D.setXORMode(XORvalue(STOP_COLOR));
                g2D.drawLine(_xStop, _y0, _xStop, _y1);    
            }
        }

        /**
         * Mouse listener, catches drag events
         *
         * @param event
         * @param layer
         */
        protected void processMouseMotionEvent
                (MouseEvent event, JXLayer<? extends V> layer)
        {
            super.processMouseMotionEvent(event, layer);
            if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
                if (_draggingStartMarker || _draggingStopMarker) {
                    double newProportion = getDraggedProportion(event);
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
                    // mark the ui as dirty and needing to be repainted
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
        private double getDraggedProportion(MouseEvent e) {
            Rectangle2D dataArea =
                    _panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            Rectangle2D area = getDataArea();
            double proportion = (e.getX() - area.getX()) / area.getWidth();
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
            if (null != _startMarkerProportion && null != _stopMarkerProportion) {
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
                    }
                }
                if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                    _draggingStartMarker = _draggingStopMarker = false;
                    SwingUtilities.invokeLater(
                            new Runnable() {
                                public void run() {
                                    if (null != _listener) {
                                        _listener.setStartStopProportion
                                                (_startMarkerProportion,
                                                 _stopMarkerProportion);
                                    }
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
        private Rectangle2D getDataArea() {
            Rectangle2D dataArea =
                    _panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            return dataArea;
        }

        /**
         * Converts screen x to chart x value.
         *
         * @param x
         * @return chart value
         */

        private double screenToValue(int x) {
            return _plot.getDomainAxis().java2DToValue(
                    (double) x, getDataArea(), RectangleEdge.TOP);
        }
    }
}

/**
 * Used only within DecayGraph, to get results from StartStopDraggingUI inner class.
 *
 * @author Aivar Grislis
 */
interface IStartStopProportionListener {
    public void setStartStopProportion
            (double startProportion, double stopProportion);
}