/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.UIManager;
import loci.slim.analysis.HistogramStatistics;

/**
 * UI Panel to show information from several {@link BatchHistogram}.
 *
 * @author aivar
 */
public class BatchHistogramsFrame extends JFrame {
    private static final String TITLE = "Batch Histograms";
	private static final int SCROLLBAR_WIDTH = ((Integer) UIManager.get("ScrollBar.width")).intValue();
	private BorderLayout layout;
	private JPanel viewPanel;
	private JScrollPane scrollPane;
	private JPanel scrollingPanel;
	private BatchHistogramPanel summaryHistogramPanel;
	private HistogramStatisticsRowPanel summaryStatisticsRow;

	/**
	 * Constructor with the first image histogram and the summary histogram.
	 *
	 * @param imageHistogramPanel
	 * @param summaryHistogramPanel
	 */
	public BatchHistogramsFrame() {
		super(TITLE);
		layout = new BorderLayout();
		setLayout(layout);
		viewPanel = new JPanel();
		viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
		scrollPane = new JScrollPane(viewPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane, BorderLayout.CENTER);
		setVisible(false);
	}

	public void update(HistogramStatistics[] imageStatistics, HistogramStatistics[] summaryStatistics) {
		HistogramStatisticsRowPanel imageStatisticsRow = new HistogramStatisticsRowPanel(imageStatistics);
		viewPanel.add(imageStatisticsRow);
		viewPanel.revalidate();
		viewPanel.repaint();
		HistogramStatisticsRowPanel summaryStatisticsRow = new HistogramStatisticsRowPanel(summaryStatistics);
		this.summaryStatisticsRow = summaryStatisticsRow;
		Component component = layout.getLayoutComponent(BorderLayout.SOUTH);
        if (null != component) {
			remove(component);
		}
		add(summaryStatisticsRow, BorderLayout.SOUTH);
		System.out.println("row width " + summaryStatisticsRow.getWidth() + " scrollbar width " + SCROLLBAR_WIDTH + " height " + summaryStatisticsRow.getHeight());
		setSize(new Dimension(300, 700)); //summaryStatisticsRow.getWidth() + SCROLLBAR_WIDTH, 3 * summaryStatisticsRow.getHeight()));
		validate();
		doLayout();
		setVisible(true);
		summaryStatisticsRow.repaint();
	}
}
