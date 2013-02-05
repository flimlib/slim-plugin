/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch.ui;

import java.awt.Dimension;

import javax.swing.JPanel;
import loci.slim.analysis.HistogramStatistics;

/**
 *
 * @author aivar
 */
public class HistogramPanel extends JPanel {
    private static final int HISTO_WIDTH = 256;
    private static final int HISTO_HEIGHT = 100;
    private static final Dimension PREFERRED_SIZE = new Dimension(HISTO_WIDTH, HISTO_HEIGHT);
    private HistogramStatistics histogramStatistics;
    
    public HistogramPanel(HistogramStatistics histogramStatistics) {
	this.histogramStatistics = histogramStatistics;
    }
    
    public Dimension getPreferredSize() {
	return PREFERRED_SIZE;
    }
}
