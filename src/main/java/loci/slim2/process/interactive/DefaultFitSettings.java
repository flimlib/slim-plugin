/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process.interactive;

import loci.slim2.process.FitSettings;

/**
 * Fit settings.
 * 
 * @author Aivar Grislis
 */
public class DefaultFitSettings extends BaseFitSettings implements FitSettings  {
	private double chiSqTarget;

	/**
	 * Gets the target reduced chi square.
	 * 
	 * @return 
	 */
	public double getChiSqTarget() {
		return chiSqTarget;
	}

	/**
	 * Sets the target reduced chi square.
	 * 
	 * @param chiSqTarget 
	 */
	public void setChiSqTarget(double chiSqTarget) {
		this.chiSqTarget = chiSqTarget;
	}
}
