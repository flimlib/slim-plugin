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
