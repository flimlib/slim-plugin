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
