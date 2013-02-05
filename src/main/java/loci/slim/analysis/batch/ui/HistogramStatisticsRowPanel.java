/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch.ui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import loci.slim.analysis.HistogramStatistics;

/**
 *
 * @author aivar
 */
public class HistogramStatisticsRowPanel extends JPanel {
    private HistogramStatisticsPanel[] statisticsPanels;
    
    public HistogramStatisticsRowPanel(HistogramStatistics[] statistics) {
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	statisticsPanels = new HistogramStatisticsPanel[statistics.length];
	for (int i = 0; i < statistics.length; ++i) {
	    statisticsPanels[i] = new HistogramStatisticsPanel(statistics[i]);
	    this.add(statisticsPanels[i]);
	}
	this.validate();
    }
}
