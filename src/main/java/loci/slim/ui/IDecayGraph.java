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

package loci.slim.ui;

import javax.swing.JFrame;

import loci.curvefitter.ICurveFitData;
import loci.slim.ICursorListener;
import loci.slim.fitting.cursor.FittingCursor;

/**
 * Interface for a decay chart.
 * 
 * @author Aivar Grislis
 */
public interface IDecayGraph {
    
    /**
     * Initialize the graph and returns the containing JFrame.
     *
     * @param bins
     * @param timeInc
	 * @param grayScale
     * @return frame
     */
    public JFrame init(final JFrame frame, final int bins, final double timeInc, ICursorListener cursorListener);

    /**
     * Changes (or initializes) the cursor (start/stop bins/values) for the fit.
     * 
     * @param fittingCursor 
     */
    public void setFittingCursor(FittingCursor fittingCursor);

    /**
     * Changes (or initializes) the title of the graph.
     * 
     * @param title 
     */
    public void setTitle(final String title);

    /**
     * Changes (or initializes) all of the charted data.
     *
	 * @param startIndex
     * @param prompt
     * @param data
     */
    public void setData(int startIndex, double[] prompt, ICurveFitData data);

	/**
	 * Sets reduced chi square of fit.
	 * 
	 * @param chiSquare 
	 */
	public void setChiSquare(double chiSquare);
    
    /**
     * Sets number of photons in fit.
     * 
     * @param photons
     */
    public void setPhotons(int photons);
    
    /**
     * Changes (or initializes) the start and stop vertical bars.
     *
     * @param transientStart
     * @param dataStart
     * @param transientStop
     */
    public void setStartStop(double transientStart, double dataStart, double transientStop);
}
