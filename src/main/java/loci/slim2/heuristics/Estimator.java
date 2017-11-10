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

package loci.slim2.heuristics;

/**
 * Interface for a class with estimates and rules of thumb.
 *
 * @author Aivar Grislis
 */
public interface Estimator {

	/**
	 * Get a decay start estimate.
	 *
	 */
	int getStart(int bins);

	/**
	 * Get a decay stop estimate.
	 *
	 */
	int getStop(int bins);

	/**
	 * Get a default threshold amount.
	 *
	 */
	int getThreshold();

	/**
	 * Get a default chi square target.
	 *
	 */
	double getChiSquareTarget();

	/**
	 * Gets default parameters for given number of components or stretched
	 * exponential.
	 *
	 */
	double[] getParameters(int components, boolean stretched);
}
