/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
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

package loci.slim2.process.interactive.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import loci.slim.Excitation;
import loci.slim2.heuristics.ExcitationScaler;
import loci.slim2.process.interactive.cursor.FittingCursor;

/**
 * Panel that holds the excitation graph.
 *
 * @author Aivar Grislis
 */
public class ExcitationPanel extends JFrame {

	private static final String TITLE = "Instrument Response Function";
	private Excitation excitation;
	private final int bins;
	private final double timeInc;

	public ExcitationPanel(final Excitation excitationParam,
		final FittingCursor fittingCursor)
	{

		excitation = excitationParam; // NB can't do "this.excitation = excitation":
																	// method variable will hide class variable in
																	// ActionListener

		setTitle(TITLE);

		final double start = fittingCursor.getPromptStartTime();
		final double stop = fittingCursor.getPromptStopTime();
		final double base = fittingCursor.getPromptBaselineValue();

		final double[] values = excitation.getValues();
		bins = values.length;
		timeInc = excitation.getTimeInc();
		final ExcitationGraph excitationGraph =
			new DefaultExcitationGraph(start, stop, base, bins, values, timeInc);
		excitationGraph.setFittingCursor(fittingCursor);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.add("North", createTopPanel());
		panel.add("Center", excitationGraph.getComponent());

		// Experimental IRF stuff
		final boolean experiment = false;
		if (experiment) {
			final JPanel panelX = new JPanel();
			final JLabel label = new JLabel("EXPERIMENTAL:");
			panelX.add(label);

			final JButton button1 = new JButton("Square IRF");
			button1.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final int start = fittingCursor.getPromptStartIndex();
					final int stop = fittingCursor.getPromptStopIndex();
					final double baseline = fittingCursor.getPromptBaselineValue();
					System.out.println("start " + start + " stop " + stop + " baseline " +
						baseline);
					final double[] values = new double[bins];
					for (int i = 0; i < bins; ++i) {
						if (i < start || i > stop) {
							values[i] = 0.0;
						}
						else {
							values[i] = baseline;
						}
					}
					excitation = new Excitation("Square", values, timeInc);
				}
			});
			panelX.add(button1);

			final JButton button2 = new JButton("Gaussian IRF");
			button2.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final int start = fittingCursor.getPromptStartIndex();
					final int stop = fittingCursor.getPromptStopIndex();
					final double baseline = fittingCursor.getPromptBaselineValue();
					System.out.println("start " + start + " stop " + stop + " baseline " +
						baseline);
					final double[] values = null;
					excitation = new Excitation("Gaussian", values, timeInc);
				}
			});
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
	public double[] getValues(final int startIndex, final int stopIndex,
		final double base)
	{
		return ExcitationScaler.scale(excitation.getValues(), startIndex,
			stopIndex, base, timeInc, bins);
	}

	/*
	 * Creates a panel with file name.
	 */
	private JPanel createTopPanel() {
		final JPanel panel = new JPanel();
		// panel.setBorder(new EmptyBorder(0, 0, 8, 8));
		// panel.setLayout(new SpringLayout());

		// JLabel fileLabel = new JLabel("File");
		// fileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		// panel.add(fileLabel);

		panel.add(new JLabel(excitation.getFileName()));

		// rows, cols, initX, initY, xPad, yPad
		// SpringUtilities.makeCompactGrid(panel, 1, 2, 4, 4, 4, 4);

		return panel;
	}
}
