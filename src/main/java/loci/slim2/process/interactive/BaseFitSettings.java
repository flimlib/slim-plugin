/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
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

package loci.slim2.process.interactive;

import loci.slim2.fitting.GlobalFitParams;
import loci.slim2.process.FitSettings;
import loci.slim2.process.HorzCursor;
import loci.slim2.process.VertCursor;

/**
 *
 * @author Aivar Grislis
 */
public class BaseFitSettings implements FitSettings {
	private String fittedImages;
	private int bins;
	private double timeInc;
	private double[] excitation;
	private HorzCursor[] excitationHorzCursors;
	private VertCursor excitationVertCursor;
	private HorzCursor[] decayHorzCursors;
	private VertCursor decayVertCursor;
	private int binningFactor;

	public GlobalFitParams globalFitParams;

	@Override
	public GlobalFitParams getGlobalFitParams() {
		return globalFitParams;
	}

	@Override
	public void setGlobalFitParams(GlobalFitParams globalFitParams) {
		this.globalFitParams = globalFitParams;
	}

	@Override
	public String getFittedImages() {
		return fittedImages;
	}

	@Override
	public void setFittedImages(String fittedImages) {
		this.fittedImages = fittedImages;
	}

	@Override
	public int getBins() {
		return bins;
	}

	/**
	 * Set number of time bins.
	 * 
	 * @param bins 
	 */
	public void setBins(int bins) {
		this.bins = bins;
	}

	@Override
	public double getTimeInc() {
		return timeInc;
	}

	/**
	 * Set time increment per time bin.
	 * 
	 * @param timeInc 
	 */
	public void setTimeInc(double timeInc) {
		this.timeInc = timeInc;
	}

	@Override
	public double[] getExcitation() {
		return excitation;
	}

	/**
	 * Set excitation decay values.
	 * 
	 * @param excitation 
	 */
	public void setExcitation(double[] excitation) {
		this.excitation = excitation;
	}

	@Override
	public HorzCursor[] getExcitationHorzCursors() {
		return excitationHorzCursors;
	}

	/**
	 * Sets horizontal cursor positions on excitation decay.
	 * 
	 * @param excitationHorzCursors
	 */
	public void setExcitationHorzCursors(HorzCursor[] excitationHorzCursors) {
		this.excitationHorzCursors = excitationHorzCursors;
	}

	@Override
	public VertCursor getExcitationVertCursor() {
		return excitationVertCursor;
	}

	/**
	 * Sets vertical cursor position on excitation decay.
	 * 
	 * @parameter excitationVertCursor
	 */
	public void setExcitationVertCursor(VertCursor excitationVertCursor) {
		this.excitationVertCursor = excitationVertCursor;
	}

	@Override
	public HorzCursor[] getDecayHorzCursors() {
		return decayHorzCursors;
	}

	/**
	 * Gets horizontal cursor positions on decay.
	 * 
	 * @param decayHorzCursors 
	 */
	public void setDecayCursors(HorzCursor[] decayHorzCursors) {
		this.decayHorzCursors = decayHorzCursors;
	}

	@Override
	public VertCursor getDecayVertCursor() {
		return decayVertCursor;
	}

	/**
	 * Sets vertical cursor position on decay.
	 * 
	 * @param decayVertCursor 
	 */
	public void setDecayVertCursor(VertCursor decayVertCursor) {
		this.decayVertCursor = decayVertCursor;
	}

	@Override
	public int getBinningFactor() {
		return binningFactor;
	}

	/**
	 * Sets binning factor.
	 * <p>
	 * 0=no binning, 1=3x3, 2=5x5, etc.
	 * 
	 * @param binningFactor
	 */
	public void setBinningFactor(int binningFactor) {
		this.binningFactor = binningFactor;
	}
}
