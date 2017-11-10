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

package loci.slim2.process.interactive;

import loci.slim2.process.FitSettings;

/**
 * Fit settings.
 *
 * @author Aivar Grislis
 */
public class DefaultFitSettings extends BaseFitSettings implements FitSettings {

	private double chiSqTarget;

	/**
	 * Gets the target reduced chi square.
	 *
	 */
	public double getChiSqTarget() {
		return chiSqTarget;
	}

	/**
	 * Sets the target reduced chi square.
	 *
	 */
	public void setChiSqTarget(final double chiSqTarget) {
		this.chiSqTarget = chiSqTarget;
	}
}
