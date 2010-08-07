/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci;

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
 *
 * @author aivar
 */
public class DataColorizerUI {

    public DataColorizerUI() {
            DecayGraph(double timeInc, int start, int stop, ICurveFitData data) {
        m_start = start;
        m_stop = stop;
        m_bins = data.getYData().length;
        JFreeChart chart = createCombinedChart(m_bins, timeInc, start, stop, data); //TODO got ugly; rethink params, globals etc.
        m_panel = new ChartPanel(chart, true, true, true, false, true);
        m_panel.setDomainZoomable(false);
        m_panel.setRangeZoomable(false);
        m_panel.setPreferredSize(new java.awt.Dimension(500, 270));
        try {
        ChartUtilities.saveChartAsPNG(new File("CHART_FILE"), chart, 500, 270);
        }
        catch (Exception e) {
            System.out.println("exception " + e);
        }
    }

    }

}
