/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch.ui;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import loci.slim.analysis.HistogramStatistics;

/**
 *
 * @author aivar
 */
public class HistogramStatisticsPanel extends JPanel {

	private HistogramPanel histogramPanel;
	private StatisticsPanel statisticsPanel;

	public HistogramStatisticsPanel(HistogramStatistics histogramStatistics) {
		super();
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(boxLayout);
		histogramPanel = new HistogramPanel(histogramStatistics);
		add(histogramPanel);
		statisticsPanel = new StatisticsPanel(histogramStatistics);
		add(statisticsPanel);
		validate();
		repaint();
	}
}
