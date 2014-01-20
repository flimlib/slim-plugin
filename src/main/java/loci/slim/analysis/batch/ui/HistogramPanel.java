/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import loci.slim.analysis.HistogramStatistics;

/**
 *
 * @author aivar
 */
public class HistogramPanel extends JPanel {

	private static final int HISTO_WIDTH = 256;
	private static final int HISTO_HEIGHT = 256;
	private static final Dimension PREFERRED_SIZE = new Dimension(HISTO_WIDTH, HISTO_HEIGHT);
	private static final int SINGLE_PIXEL = 1;
	private HistogramStatistics histogramStatistics;

	public HistogramPanel(HistogramStatistics histogramStatistics) {
		super();
		this.histogramStatistics = histogramStatistics;
		repaint();
	}

	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}
	
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
		long[] histogram = histogramStatistics.getHistogram();
		if (null != histogram) {
			// find maximum count for scaling
			long maxCount = Long.MIN_VALUE;
			for (int i = 0; i < HISTO_WIDTH; ++i) {
				if (histogram[i] > maxCount) {
					maxCount = histogram[i];
				}
			}
	
			// fill with white
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, HISTO_WIDTH, HISTO_HEIGHT);
			
			// draw bars in gray
			g.setColor(Color.DARK_GRAY);

			int height;
			for (int i = 0; i < HISTO_WIDTH; ++i) {
				if (0 != histogram[i]) {
					// make sure values of one show at least a single pixel
					height = (int)((HISTO_HEIGHT - SINGLE_PIXEL) * histogram[i] / maxCount) + SINGLE_PIXEL;
					if (height > HISTO_HEIGHT) {
						height = HISTO_HEIGHT;
					}
					g.drawLine(i, HISTO_HEIGHT - height, i, HISTO_HEIGHT);
				}
			}
        }
		else {
			g.setColor(Color.ORANGE);
			g.fillRect(0, 0, HISTO_WIDTH, HISTO_HEIGHT);
			g.setColor(Color.MAGENTA);
			g.drawLine(0, 0, HISTO_WIDTH, HISTO_HEIGHT);
			g.drawLine(WIDTH, 0, 0, HISTO_HEIGHT);
		}
    }

}
