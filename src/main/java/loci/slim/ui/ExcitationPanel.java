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

package loci.slim.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import loci.slim.Excitation;
import loci.slim.fitting.cursor.FittingCursor;
import loci.slim.heuristics.ExcitationScaler;

/**
 * Panel that holds the excitation graph.
 *
 * @author Aivar Grislis
 */
public class ExcitationPanel extends JFrame {

	private static final String TITLE = "Instrument Response Function";
	private final Excitation _excitation;
	private final int _bins;
	private final double _timeInc;

	public ExcitationPanel(final Excitation excitation,
		final FittingCursor fittingCursor)
	{

		_excitation = excitation;

		this.setTitle(TITLE);

		final double start = fittingCursor.getPromptStartValue();
		final double stop = fittingCursor.getPromptStopValue();
		final double base = fittingCursor.getPromptBaselineValue();

		final double[] values = excitation.getValues();
		_bins = values.length;
		_timeInc = excitation.getTimeInc();
		final ExcitationGraph excitationGraph =
			new ExcitationGraph(start, stop, base, _bins, values, _timeInc);
		excitationGraph.setFittingCursor(fittingCursor);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.add("North", createTopPanel());
		panel.add("Center", excitationGraph.getComponent());

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
	 * @param base;
	 */
	public double[] getValues(final int startIndex, final int stopIndex,
		final double base)
	{
		return ExcitationScaler.scale(_excitation.getValues(), startIndex,
			stopIndex, base, _timeInc, _bins);
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

		panel.add(new JLabel(_excitation.getFileName()));

		// rows, cols, initX, initY, xPad, yPad
		// SpringUtilities.makeCompactGrid(panel, 1, 2, 4, 4, 4, 4);

		return panel;
	}
}
