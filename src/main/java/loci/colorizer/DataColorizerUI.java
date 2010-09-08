/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.colorizer;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
public class DataColorizerUI implements IColorizeRangeListener {
    Object m_synchObject = new Object();
    IColorizeRangeListener m_listener;
    Histogram m_histogram;
    ColorBar m_colorBar;
    InputPanel m_inputPanel;
    boolean m_auto;
    double m_start;
    double m_stop;
    double m_max;

    public DataColorizerUI(IColorize colorize, IColorizeRangeListener listener) {
        m_listener = listener;
        
        m_auto = true;
        m_start = m_stop = m_max = 0.0;

        m_histogram = new Histogram(320, 160, this);
        m_colorBar = new ColorBar(320, 20, colorize);
        m_inputPanel = new InputPanel(this);

        JFrame frame = new JFrame("Colorize");
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().add(m_histogram, BorderLayout.NORTH);
        frame.getContentPane().add(m_colorBar, BorderLayout.CENTER);
        frame.getContentPane().add(m_inputPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    void updateData(double lifetime[], double max) {
        synchronized (m_synchObject) {
            m_max = max;
            m_histogram.updateData(lifetime, max);
            if (m_auto) {
                m_stop = max;
            }
            m_colorBar.setRange(m_auto, m_start, m_stop, max);
            m_inputPanel.setRange(m_auto, m_start, m_stop, max);
        }
    }

    public void setRange(boolean auto, double start, double stop, double max) {
        synchronized (m_synchObject) {
           m_auto = auto;
           m_start = start;
           m_stop = stop;
           m_max = max;

           m_histogram.setRange(auto, start, stop, max);
           m_colorBar.setRange(auto, start, stop, max);
           m_inputPanel.setRange(auto, start, stop, max);

           m_listener.setRange(auto, start, stop, max);
        }
    }
}
