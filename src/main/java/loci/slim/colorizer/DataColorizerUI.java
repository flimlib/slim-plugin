//
// DataColorizerUI.java
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

package loci.slim.colorizer;

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
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/colorizer/DataColorizerUI.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/colorizer/DataColorizerUI.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class DataColorizerUI implements IColorizeRangeListener {
    Object m_synchObject = new Object();
    JFrame m_frame;
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

        m_frame = new JFrame("Colorize");
        m_frame.setDefaultCloseOperation(m_frame.EXIT_ON_CLOSE);
        m_frame.setResizable(false);
        m_frame.getContentPane().add(m_histogram, BorderLayout.NORTH);
        m_frame.getContentPane().add(m_colorBar, BorderLayout.CENTER);
        m_frame.getContentPane().add(m_inputPanel, BorderLayout.SOUTH);
        m_frame.pack();
        m_frame.setVisible(true);
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

    public void quit() {
        m_frame.dispose(); //TODO ? what should I call here
    }
}
