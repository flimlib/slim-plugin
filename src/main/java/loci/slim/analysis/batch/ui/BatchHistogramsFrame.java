/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
