/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
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
 * @author Aivar Grislis
 */
public class BatchHistogramsFrame extends JFrame {
	public static final String SUMMARY = "Summary";
    private static final String TITLE = "Batch Histograms";
	private static final int SCROLLBAR_WIDTH = ((Integer) UIManager.get("ScrollBar.width")).intValue();
	private BatchHistogramListener listener;
	private BorderLayout layout;
	private JPanel viewPanel;
	private JScrollPane scrollPane;
	private JPanel scrollingPanel;
	private BatchHistogramPanel summaryHistogramPanel;
	private HistogramStatisticsRowPanel summaryStatisticsRow;

	/**
	 * Constructor of invisible frame.
	 *
	 * @param listener
	 */
	public BatchHistogramsFrame(BatchHistogramListener listener) {
		super(TITLE);
		this.listener = listener;
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

	public void update(String fileName, HistogramStatistics[] imageStatistics, HistogramStatistics[] summaryStatistics) {
		HistogramStatisticsRowPanel imageStatisticsRow = new HistogramStatisticsRowPanel(fileName, imageStatistics, listener);
		viewPanel.add(imageStatisticsRow);
		viewPanel.revalidate();
		viewPanel.repaint();
		HistogramStatisticsRowPanel summaryStatisticsRow = new HistogramStatisticsRowPanel(SUMMARY, summaryStatistics, null);
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
