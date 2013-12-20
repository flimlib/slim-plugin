/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2013, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim2.process.interactive.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import loci.slim2.process.Excitation;
import loci.slim2.process.interactive.cursor.FittingCursor;
import loci.slim2.heuristics.ExcitationScaler;

/**
 * Panel that holds the excitation graph.
 * 
 * @author Aivar Grislis
 */
public class ExcitationPanel extends JFrame {
    private static final String TITLE = "Instrument Response Function";
    private Excitation excitation;
    private int bins;
    private double timeInc;

    public ExcitationPanel(Excitation excitationParam, final FittingCursor fittingCursor) {

        excitation = excitationParam; //NB can't do "this.excitation = excitation": method variable will hide class variable in ActionListener

        setTitle(TITLE);
        
        double start = fittingCursor.getPromptStartTime();
        double stop  = fittingCursor.getPromptStopTime();
        double base = fittingCursor.getPromptBaselineValue();

        double[] values = excitation.getValues();
        bins = values.length;
        timeInc = excitation.getTimeInc();
		ExcitationGraph excitationGraph = new DefaultExcitationGraph(start, stop, base,
                bins, values, timeInc);
        excitationGraph.setFittingCursor(fittingCursor);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", createTopPanel());
        panel.add("Center", excitationGraph.getComponent());
		
		// Experimental IRF stuff
		boolean experiment = false;
		if (experiment) {
			JPanel panelX = new JPanel();
			JLabel label = new JLabel("EXPERIMENTAL:");
			panelX.add(label);

			JButton button1 = new JButton("Square IRF");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int start = fittingCursor.getPromptStartIndex();
					int stop = fittingCursor.getPromptStopIndex();
					double baseline = fittingCursor.getPromptBaselineValue();
					System.out.println("start " + start + " stop " + stop + " baseline " + baseline);
					double[] values = new double[bins];
					for (int i = 0; i < bins; ++i) {
						if (i < start || i > stop) {
							values[i] = 0.0;
						}
						else {
							values[i] = baseline;
						}
					}
					excitation = new Excitation("Square", values, timeInc);
				}});
			panelX.add(button1);

			JButton button2 = new JButton("Gaussian IRF");
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int start = fittingCursor.getPromptStartIndex();
					int stop = fittingCursor.getPromptStopIndex();
					double baseline = fittingCursor.getPromptBaselineValue();
					System.out.println("start " + start + " stop " + stop + " baseline " + baseline);
					double[] values = null;
					excitation = new Excitation("Gaussian", values, timeInc);
				}});
			panelX.add(button2);

			panel.add("South", panelX);
		}
		
		if (false) {

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
        return excitation.getValues();
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
        return ExcitationScaler.scale(excitation.getValues(),
                startIndex, stopIndex, base, timeInc, bins);
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

        panel.add(new JLabel(excitation.getFileName()));

        // rows, cols, initX, initY, xPad, yPad
        //SpringUtilities.makeCompactGrid(panel, 1, 2, 4, 4, 4, 4);

        return panel;
    }
}

