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

package loci.slim2.process.interactive.ui;

import javax.swing.JFrame;

import loci.slim2.fitting.FitResults;
import loci.slim2.process.interactive.cursor.FittingCursor;
import loci.slim2.process.interactive.ui.PixelPicker;

/**
 * Interface for a decay chart.
 * 
 * @author Aivar Grislis
 */
public interface DecayGraph {
    
    /**
     * Initialize the graph and returns the containing JFrame.
     *
     * @param bins
     * @param timeInc
	 * @param grayScale
     * @return frame
     */
    public JFrame init(final JFrame frame, final int bins, final double timeInc, PixelPicker pixelPicker);

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
     * @param fitResults
     */
    public void setData(int startIndex, double[] prompt, FitResults fitResults);

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
