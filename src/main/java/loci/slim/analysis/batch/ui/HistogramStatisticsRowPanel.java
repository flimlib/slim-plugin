/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import loci.slim.analysis.HistogramStatistics;
/**
 *
 * @author Aivar Grislis
 */
public class HistogramStatisticsRowPanel extends JPanel {

	private HistogramStatisticsPanel[] statisticsPanels;

	public HistogramStatisticsRowPanel(final String fileName, HistogramStatistics[] statistics, final BatchHistogramListener listener) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel label = new JLabel(fileName);
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.add(label, BorderLayout.LINE_START);
		//labelPanel.setMaximumSize(label.getMinimumSize());
		this.add(labelPanel);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		if (null != statistics) {
			statisticsPanels = new HistogramStatisticsPanel[statistics.length];
			for (int i = 0; i < statistics.length; ++i) {
				statisticsPanels[i] = new HistogramStatisticsPanel(statistics[i]);
				panel.add(statisticsPanels[i]);
			}
		}

		this.add(panel);
		
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (null != listener) {
					listener.swapImage(fileName);
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) { }
			
			@Override
			public void mouseExited(MouseEvent e) { }
			
			@Override
			public void mousePressed(MouseEvent e) { }
			
			@Override
			public void mouseReleased(MouseEvent e) { }
		});
		
		this.validate();
	}
}
