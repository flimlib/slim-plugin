/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author aivar
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
