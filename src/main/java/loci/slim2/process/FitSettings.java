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

package loci.slim2.process;

import loci.slim2.fitting.GlobalFitParams;

/**
 * Holds basic FLIM fit settings.
 * <p>
 * The intention here is to provide a basic set of fit settings that every
 * FLIM fitting implementation ought to need.  If it turns out some settings are
 * irrelevant to a given fitting implementation they can simply be ignored.
 * 
 * @author Aivar Grislis
 */
public interface FitSettings {
	
	public GlobalFitParams getGlobalFitParams();
	
	public void setGlobalFitParams(GlobalFitParams globalFitParams);
	
	public String getFittedImages();
	
	public void setFittedImages(String fittedImages);

	/**
	 * Get number of bins in time histogram.
	 * 
	 * @return 
	 */
	public int getBins();

	/**
	 * Get time increment per time bin.
	 * 
	 * @return 
	 */
	public double getTimeInc();

	/**
	 * Get excitation decay values.
	 * 
	 * @return 
	 */
	public double[] getExcitation();
	
	/**
	 * Gets horizontal cursor positions on excitation decay.
	 * 
	 * @return null or array of cursors
	 */
	public HorzCursor[] getExcitationHorzCursors();

	/**
	 * Gets vertical cursor position on excitation decay.
	 * 
	 * @return null or cursor
	 */
	public VertCursor getExcitationVertCursor();

	/**
	 * Gets horizontal cursor positions on decay.
	 * 
	 * @return null or array of cursors
	 */
	public HorzCursor[] getDecayHorzCursors();

	/**
	 * Gets vertical cursor position on decay.
	 * 
	 * @return null or cursor
	 */
	public VertCursor getDecayVertCursor();
	
	/**
	 * Gets binning factor.
	 * <p>
	 * 0=no binning, 1=3x3, 2=5x5, etc.
	 * 
	 * @return binning factor
	 */
	public int getBinningFactor();
}
