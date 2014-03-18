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

package loci.slim.fitting.params;

/**
 * Interface for container for local fitted results for current pixel.
 * 
 * @author Aivar Grislis
 */
public interface IFitResults {

	/**
	 * Sets fitted chi square result.
	 * 
	 * @param chiSquare 
	 */
	public void setChiSquare(double chiSquare);

	/**
	 * Gets fitted chi square result.
	 * 
	 * @return 
	 */
	public double getChiSquare();

	/**
	 * Sets fitted parameters.
	 * 
	 * @param params or null
	 */
	public void setParams(double[] params);

	/**
	 * Gets fitted parameters.
	 * 
	 * @return null or fitted params
	 */
	public double[] getParams();

	/**
	 * Sets fitted curve.
	 * 
	 * @param yFitted 
	 */
	public void setYFitted(double[] yFitted);

	/**
	 * Gets fitted curve.
	 * 
	 * @return 
	 */
	public double[] getYFitted();
}
