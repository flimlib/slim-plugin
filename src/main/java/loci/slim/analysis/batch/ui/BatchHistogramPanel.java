/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch.ui;

import java.awt.Dimension;

import javax.swing.JPanel;

/**
 *
 * @author aivar
 */
public class BatchHistogramPanel extends JPanel {
    private HistogramPanel histogramPanel;
    private StatisticsPanel statisticsPanel;
    
    public BatchHistogramPanel(HistogramPanel histogramPanel,
	    StatisticsPanel statisticsPanel)
    {
	super();
	
	this.histogramPanel = histogramPanel;
	this.add(histogramPanel);
	this.statisticsPanel = statisticsPanel;
	this.add(statisticsPanel);
    }
}
