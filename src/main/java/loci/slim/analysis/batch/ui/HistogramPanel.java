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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import loci.slim.analysis.HistogramStatistics;

/**
 *
 * @author Aivar Grislis
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
