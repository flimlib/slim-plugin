//
// DecayGraph.java
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

package loci;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import loci.curvefitter.ICurveFitData;

//import org.jdesktop.jxlayer.JXLayer;
//import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/DecayGraph.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/DecayGraph.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class DecayGraph implements IStartStopListener, IStartStopProportionListener {
    static final Color DECAY_COLOR = Color.BLUE;
    static final Color FITTED_COLOR = Color.MAGENTA;
    int m_start;
    int m_stop;
    int m_bins;
    StartStopDraggingUI<JComponent> m_startStopDraggingUI;
    IStartStopListener m_startStopListener;
    boolean m_headless = false;
    boolean m_logarithmic = true;
    MyXYPlot m_decaySubPlot;
    XYSeriesCollection m_decayDataset;
    XYSeriesCollection m_residualDataset;
    ChartPanel m_panel;

    JFreeChart m_decayChart;
    JFreeChart m_residualsChart;

    DecayGraph(double timeInc, int start, int stop, ICurveFitData data) {
        m_bins = data.getYData().length;
        JFreeChart chart = createCombinedChart(m_bins, timeInc, start, stop, data); //TODO got ugly; rethink params, globals etc.
        setStartStop(start, stop);
        m_panel = new ChartPanel(chart, true, true, true, false, true);
        m_panel.setDomainZoomable(false);
        m_panel.setRangeZoomable(false);
        m_panel.setPreferredSize(new java.awt.Dimension(500, 270));
        //try {
        //ChartUtilities.saveChartAsPNG(new File("CHART_FILE"), chart, 500, 270);
        //}
        //catch (Exception e) {
        //    System.out.println("exception " + e);
        //}
    }

    void setLogarithmic(boolean logarithmic) {
        m_logarithmic = logarithmic;
    }

    /**
     * Registers an external start/stop listener.
     *
     * @param startStopListener
     */
    void setStartStopListener(IStartStopListener startStopListener) {
        m_startStopListener = startStopListener;
    }

    /**
     * Handles start and stop being set externally.  Passes it on to the start/stop dragging UI layer.
     *
     * @param start index
     * @param stop index, inclusive
     */
    public void setStartStop(int start, int stop){
        m_start = start;
        m_stop = stop;
        if (null != m_startStopDraggingUI) {
            System.out.println("start comes in " + start + " stop " + stop);
            double startProportion = (double) start / m_bins;
            double stopProportion = (double) stop / m_bins;
            System.out.println("startP " + startProportion + " stopP " + stopProportion);

            m_startStopDraggingUI.setStartStopProportions(startProportion, stopProportion);
        }
    }

    /**
     * Handles start and stop being set internally, from the start/stop dragging UI layer.  Validates
     * and passes changes on to external listener.
     *
     * @param startProportion
     * @param stopProportion
     */
    public void setStartStopProportion(double startProportion, double stopProportion) {
        //System.out.println("getting notification of " + startProportion + " " + stopProportion);
        int start = (int) (startProportion * m_bins + 0.5);
        int stop = (int) (stopProportion * m_bins + 0.5);
        //System.out.println("start " + start + " stop " + stop);
        if (start != m_start || stop != m_stop) {
            // redraw UI on bin boundaries
            setStartStop(start, stop);
            if (null != m_startStopListener) {
                //System.out.println("NOTIFY LISTENER");
                m_startStopListener.setStartStop(start, stop);
            }
        }
    }

    JFreeChart createCombinedChart(int bins, double timeInc, int start, int stop, ICurveFitData data) {

        createDatasets(timeInc, start, stop, bins, data);

        // make a common horizontal axis for both sub-plots
        NumberAxis timeAxis = new NumberAxis("Time");
        timeAxis.setLabel("nanoseconds");
        timeAxis.setRange(0.0, (bins - 1) * timeInc);

        // make a vertically combined plot
        CombinedDomainXYPlot parent = new CombinedDomainXYPlot(timeAxis);

        // create decay sub-plot
        NumberAxis photonAxis;
        if (m_logarithmic) {
            photonAxis = new LogarithmicAxis("Photons");
        }
        else {
            photonAxis = new NumberAxis("Photons");
        }
        photonAxis.setRange(0.0, 2000000.0);
        XYSplineRenderer decayRenderer = new XYSplineRenderer();
        decayRenderer.setSeriesShapesVisible(0, false);
        decayRenderer.setSeriesShapesVisible(1, false);
        decayRenderer.setSeriesLinesVisible(2, false);
        decayRenderer.setSeriesShape(2, new Ellipse2D.Float(2.0f, 2.0f, 2.0f, 2.0f)); // 1.5, 3.0 look ugly!



        decayRenderer.setSeriesPaint(0, Color.green);
        decayRenderer.setSeriesPaint(1, Color.red);
        decayRenderer.setSeriesPaint(2, Color.blue);

        m_decaySubPlot = new MyXYPlot(m_decayDataset, null, photonAxis, decayRenderer);
        m_decaySubPlot.setDomainCrosshairVisible(true);
        m_decaySubPlot.setRangeCrosshairVisible(true);

        // add decay sub-plot to parent
        parent.add(m_decaySubPlot, 4);

        // create residual sub-plot
        NumberAxis residualAxis = new NumberAxis("Residual");
        residualAxis.setRange(-100.0, 100.0);
        XYSplineRenderer residualRenderer = new XYSplineRenderer();
        residualRenderer.setSeriesShapesVisible(0, false);
        residualRenderer.setSeriesPaint(0, Color.black);
        XYPlot residualSubPlot = new XYPlot(m_residualDataset, null, residualAxis, residualRenderer);
        residualSubPlot.setDomainCrosshairVisible(true);
        residualSubPlot.setRangeCrosshairVisible(true);
        residualSubPlot.setFixedLegendItems(null);

        // add residual sub-plot to parent
        parent.add(residualSubPlot, 1);

        // now make the top level JFreeChart
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, parent, true);
        //chart.setLegend(null);
        chart.removeLegend();
        return chart;
    }

    private void createDatasets(double timeInc, int start, int stop, int bins, ICurveFitData data) {
        //TODO lamp function; comes from where?
        XYSeries series1 = new XYSeries("IRF");
        series1.add(1.0, 1.0);
        series1.add(2.0, 4.0);
        series1.add(3.0, 3.0);
        series1.add(4.0, 5.0);
        series1.add(5.0, 5.0);
        series1.add(6.0, 7.0);
        series1.add(7.0, 7.0);
        series1.add(8.0, 8.0);

        XYSeries series2 = new XYSeries("Fitted");
        XYSeries series3 = new XYSeries("Data");
        XYSeries series4 = new XYSeries("Residuals");

     //// XYSeries series5 = new XYSeries("Sanity Check");
      double params[] = data.getParams();

        double yData, yFitted;
        double xCurrent = 0;
        for (int i = 0; i < bins; ++i) {
            yData = data.getYData()[i];
            series3.add(xCurrent, yData);
            // are we in fitted region?
            if (start <= i && i <= stop) {
                // yes, show fitted curve and residuals
                yFitted = data.getYFitted()[i];
                System.out.println("yFitted[" + i + "] " + yFitted);
                if (yFitted > 0.0) {
                series2.add(xCurrent, yFitted);
                series4.add(xCurrent, yData - yFitted);
                }

////                double newlyCalculated;
////series5.add(xCurrent, newlyCalculated = params[0] * Math.exp(-params[1] * xCurrent) + params[2]);
////System.out.println(" i " + i + " " + newlyCalculated);
                //series5.add(xCurrent, Math.exp(-params[1] * xCurrent) + params[2]);
            }
            else {
                series2.add(xCurrent, null);
                //series2.add(xCurrent, data.getYFitted()[i]);
                series4.add(xCurrent, null);
            }
            xCurrent += timeInc;
        }

        m_decayDataset = new XYSeriesCollection();
        m_decayDataset.addSeries(series1);
        m_decayDataset.addSeries(series2);
        m_decayDataset.addSeries(series3);
 ////   m_decayDataset.addSeries(series5);

        m_residualDataset = new XYSeriesCollection();
        m_residualDataset.addSeries(series4);
    }

    /**
     * Gets a JComponent containing the chart.  Uses JXLayer to add
     * the capability of dragging the start/stop lines.
     *
     * @return component
     */
    JComponent getComponent() {
        JXLayer<JComponent> layer = new JXLayer<JComponent>(m_panel);
        m_startStopDraggingUI = new StartStopDraggingUI<JComponent>(m_decaySubPlot, this);
        layer.setUI(m_startStopDraggingUI);
        return layer;
    }

    /**
     * UI which allows us to paint on top of the components, using JXLayer.
     *
     * @param <V> component
     */
    static class StartStopDraggingUI<V extends JComponent> extends AbstractLayerUI<V> {
        private static final int CLOSE_ENOUGH = 4; // pizels
        private MyXYPlot m_plot;
        private IStartStopProportionListener m_listener;
        boolean m_draggingStartMarker = false;
        boolean m_draggingStopMarker = false;
        private double m_startMarkerProportion = 0.25;
        private double m_stopMarkerProportion = 0.75;
        private int m_y0;
        private int m_y1;
        private int m_xStart;
        private int m_xStop;

        StartStopDraggingUI(MyXYPlot plot, IStartStopProportionListener listener) {
            m_plot = plot;
            m_listener = listener;
        }

        void setStartStopProportions(double startMarkerProportion, double stopMarkerProportion) {
            //System.out.println("in UI getting new proportions " + startMarkerProportion + " " + stopMarkerProportion);
            m_startMarkerProportion = startMarkerProportion;
            m_stopMarkerProportion = stopMarkerProportion;
            setDirty(true);
        }

        // override paintLayer(), not paint()
        protected void paintLayer(Graphics2D g2, JXLayer<? extends V> l) {
            // this paints layer as is
            super.paintLayer(g2, l);

            // adjust to current size
            Rectangle2D area = m_plot.getArea();
            double x = area.getX();
            m_y0 = (int) area.getY();
            m_y1 = (int) (area.getY() + area.getHeight());
            double width = area.getWidth();
            m_xStart = (int) (x + width * m_startMarkerProportion);
            m_xStop = (int) (x + width * m_stopMarkerProportion);
            System.out.println("startP " + m_startMarkerProportion + " stopP " + m_stopMarkerProportion + " xStart " + m_xStart + " xStop " + m_xStop);

            // custom painting is here
            g2.setXORMode(Color.MAGENTA);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(m_xStart, m_y0, m_xStart, m_y1);
            g2.drawLine(m_xStop, m_y0, m_xStop, m_y1);
        }

        // catch drag events
        protected void processMouseMotionEvent(MouseEvent e, JXLayer<? extends V> l) {
            super.processMouseMotionEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                //Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
                if (m_draggingStartMarker || m_draggingStopMarker) {
                    double newProportion = getDraggedProportion(e);
                    if (m_draggingStartMarker) {
                        if (newProportion <= m_stopMarkerProportion) {
                            m_startMarkerProportion = newProportion;
                        }
                    }
                    else {
                        if (newProportion >= m_startMarkerProportion) {
                            m_stopMarkerProportion = newProportion;
                        }
                    }
                    // mark the ui as dirty and needed to be repainted
                    setDirty(true);
                }
            }
        }

        /*
         * Returns a value between 0.0 and 1.0.
         */
        private double getDraggedProportion(MouseEvent e) {
            Rectangle2D area = m_plot.getArea();
            double proportion = ((double) e.getX() - area.getX()) / area.getWidth();
            if (proportion < 0.0) {
                proportion = 0.0;
            }
            else if (proportion > 1.0) {
                proportion = 1.0;
            }
            return proportion;
        }

        // catch MouseEvent.MOUSE_RELEASED
        protected void processMouseEvent(MouseEvent e, JXLayer<? extends V> l) {
            super.processMouseEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                int x = e.getX();
                int y = e.getY();
                if (y > m_y0 - CLOSE_ENOUGH && y < m_y1 + CLOSE_ENOUGH) {
                    if (Math.abs(x - m_xStart) < CLOSE_ENOUGH) {
                        // start dragging start line
                        m_draggingStartMarker = true;

                    }
                    else if (Math.abs(x - m_xStop) < CLOSE_ENOUGH) {
                        // start dragging stop line
                        m_draggingStopMarker = true;
                    }
                }
                System.out.println(" x " + e.getX() + " y " + e.getY());
            }
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                m_draggingStartMarker = m_draggingStopMarker = false;
                SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                //System.out.println("RELEASED " + m_startMarkerProportion + " " + m_stopMarkerProportion);
                                m_listener.setStartStopProportion(m_startMarkerProportion, m_stopMarkerProportion);
                            }
                });

//TODO NOTIFY SOMEBODY!!!
                // mark the ui as dirty and needed to be repainted
                //setDirty(true);
            }
        }
    }

    /**
     * Bit of a kludge.<p>
     * XYPlot & Plot don't provide a method to get the size of the
     * graph area.
     */
    private class MyXYPlot extends XYPlot {
        private Rectangle2D m_area;

        MyXYPlot(XYDataset dataset,
                        ValueAxis domainAxis,
                        ValueAxis rangeAxis,
                        XYItemRenderer renderer) {
            super(dataset, domainAxis, rangeAxis, renderer);
        }

        @Override
        public void drawOutline(Graphics2D g2, Rectangle2D area) {
            super.drawOutline(g2, area);
            m_area = area;
        }

        public Rectangle2D getArea() {
            return m_area;
        }
    }
}

/**
 * Used within DecayGraph, to communicate with StartStopDraggingUI inner class.
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
interface IStartStopProportionListener {
    public void setStartStopProportion(double startProportion, double stopProportion);
}
