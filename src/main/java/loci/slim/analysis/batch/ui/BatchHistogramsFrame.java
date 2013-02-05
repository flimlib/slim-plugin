/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch.ui;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import loci.slim.analysis.HistogramStatistics;

/**
 * UI Panel to show information from several {@link BatchHistogram}.
 * 
 * @author aivar
 */
public class BatchHistogramsFrame extends JFrame {
    private JScrollPane scrollPane;
    private BatchHistogramPanel summaryHistogramPanel;
    private HistogramStatisticsRowPanel summaryStatisticsRow;

    /**
     * Constructor with the first image histogram and the summary histogram.
     * 
     * @param imageHistogramPanel
     * @param summaryHistogramPanel 
     */
    public BatchHistogramsFrame() {
	BorderLayout borderLayout = new BorderLayout();
	setLayout(borderLayout);
	scrollPane = new JScrollPane();
	BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
	scrollPane.setLayout(boxLayout);
	add(scrollPane, BorderLayout.CENTER);
	//doLayout();
	//setVisible(true);
    }
    
    public void update(HistogramStatistics[] imageStatistics, HistogramStatistics[] summaryStatistics) {
	HistogramStatisticsRowPanel imageStatisticsRow = new HistogramStatisticsRowPanel(imageStatistics);
	scrollPane.add(imageStatisticsRow);
	scrollPane.revalidate();
	HistogramStatisticsRowPanel summaryStatisticsRow = new HistogramStatisticsRowPanel(summaryStatistics);
	this.summaryStatisticsRow = summaryStatisticsRow;
	add(summaryStatisticsRow, BorderLayout.SOUTH);
	doLayout();
	setVisible(true);
    }
}
