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

import javax.swing.JFrame;

import loci.slim2.fitting.FitResults;
import loci.slim2.process.interactive.cursor.FittingCursor;

/**
 * Interface for a decay chart.
 *
 * @author Aivar Grislis
 */
public interface DecayGraph {

	/**
	 * Initialize the graph and returns the containing JFrame.
	 *
	 * @return frame
	 */
	public JFrame init(final JFrame frame, final int bins, final double timeInc,
		PixelPicker pixelPicker);

	/**
	 * Changes (or initializes) the cursor (start/stop bins/values) for the fit.
	 *
	 */
	public void setFittingCursor(FittingCursor fittingCursor);

	/**
	 * Changes (or initializes) the title of the graph.
	 *
	 */
	public void setTitle(final String title);

	/**
	 * Changes (or initializes) all of the charted data.
	 *
	 */
	public void setData(int startIndex, double[] prompt, FitResults fitResults);

	/**
	 * Sets reduced chi square of fit.
	 *
	 */
	public void setChiSquare(double chiSquare);

	/**
	 * Sets number of photons in fit.
	 *
	 */
	public void setPhotons(int photons);

	/**
	 * Changes (or initializes) the start and stop vertical bars.
	 *
	 */
	public void setStartStop(double transientStart, double dataStart,
		double transientStop);
}
