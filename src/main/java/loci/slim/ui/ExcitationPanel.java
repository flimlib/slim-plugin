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

package loci.slim.ui;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
					IJ.log("start " + start + " stop " + stop + " baseline " + baseline);
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
					IJ.log("start " + start + " stop " + stop + " baseline " + baseline);
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
