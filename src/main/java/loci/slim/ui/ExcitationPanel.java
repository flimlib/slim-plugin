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

package loci.slim.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import loci.slim.Excitation;
import loci.slim.fitting.cursor.FittingCursor;
import loci.slim.heuristics.ExcitationScaler;

/**
 * Panel that holds the excitation graph.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationPanel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationPanel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class ExcitationPanel extends JFrame {
    private static final String TITLE = "Instrument Response Function";
    private Excitation _excitation;
    private int _bins;
    private double _timeInc;

    public ExcitationPanel(Excitation excitation, final FittingCursor fittingCursor) {

        _excitation = excitation;

        this.setTitle(TITLE);
        
        double start = fittingCursor.getPromptStartValue();
        double stop  = fittingCursor.getPromptStopValue();
        double base = fittingCursor.getPromptBaselineValue();

        double[] values = excitation.getValues();
        _bins = values.length;
        _timeInc = excitation.getTimeInc();
        ExcitationGraph excitationGraph = new ExcitationGraph(start, stop, base,
                _bins, values, _timeInc);
        excitationGraph.setFittingCursor(fittingCursor);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", createTopPanel());
        panel.add("Center", excitationGraph.getComponent());
		
		// DEBUGGING AIDS
		if (false) {
		JPanel panelX = new JPanel();
		JLabel label = new JLabel("EXPERIMENTAL:");
		panelX.add(label);
		
		JButton button1 = new JButton("Square IRF");
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int start = fittingCursor.getPromptStartBin();
				int stop = fittingCursor.getPromptStopBin();
				double baseline = fittingCursor.getPromptBaselineValue();
				System.out.println("start " + start + " stop " + stop + " baseline " + baseline);
				double[] values = new double[_bins];
				for (int i = 0; i < _bins; ++i) {
					if (i < start || i > stop) {
						values[i] = 0.0;
					}
					else {
						values[i] = baseline;
					}
				}
				_excitation = new Excitation("Square", values, _timeInc);
			}});
		panelX.add(button1);
		
		JButton button2 = new JButton("Gaussian IRF");
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int start = fittingCursor.getPromptStartBin();
				int stop = fittingCursor.getPromptStopBin();
				double baseline = fittingCursor.getPromptBaselineValue();
				System.out.println("start " + start + " stop " + stop + " baseline " + baseline);
				double[] values = null;
				_excitation = new Excitation("Gaussian", values, _timeInc);
			}});
		panelX.add(button2);
		
		panel.add("South", panelX);
		}

        this.getContentPane().add(panel);

        this.setSize(450, 225);
        this.pack();
        this.setVisible(true);
    }
    
    public void quit() {
        this.setVisible(false);
    }
    
    public double[] getRawValues() {
        return _excitation.getValues();
    }

    /**
     * Gets the excitation values scaled for a particular start/stop/base cursor.
     * 
     * @param startIndex
     * @param stopIndex
     * @param base;
     * @return 
     */
    public double[] getValues(int startIndex, int stopIndex, double base) {
        return ExcitationScaler.scale(_excitation.getValues(),
                startIndex, stopIndex, base, _timeInc, _bins);
    }

    /*
     * Creates a panel with file name.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
       // panel.setBorder(new EmptyBorder(0, 0, 8, 8));
       // panel.setLayout(new SpringLayout());

        //JLabel fileLabel = new JLabel("File");
        //fileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        //panel.add(fileLabel);

        panel.add(new JLabel(_excitation.getFileName()));

        // rows, cols, initX, initY, xPad, yPad
        //SpringUtilities.makeCompactGrid(panel, 1, 2, 4, 4, 4, 4);

        return panel;
    }
}
