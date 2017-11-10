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

package loci.slim2.fitting;

/**
 * Interface for container for local fitted results for current pixel. Also
 * contains some inputs to the fit that are displayed with results.
 *
 * @author Aivar Grislis
 */
public interface FitResults {

	/**
	 * Sets error code (or 0 for no error).
	 *
	 */
	public void setErrorCode(int errorCode);

	/**
	 * Gets error code.
	 *
	 */
	public int getErrorCode();

	/**
	 * Sets fitted chi square result.
	 *
	 */
	public void setChiSquare(double chiSquare);

	/**
	 * Gets fitted chi square result.
	 *
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
	 */
	public void setYFitted(double[] yFitted);

	/**
	 * Gets fitted curve.
	 *
	 */
	public double[] getYFitted();

	/**
	 * Sets incoming transient data.
	 *
	 */
	public void setTransient(double[] trans);

	/**
	 * Gets incoming transient data.
	 *
	 */
	public double[] getTransient();

	/**
	 * Sets total photon count in decay.
	 *
	 */
	public void setPhotonCount(int photonCount);

	/**
	 * Gets total photon count in decay.
	 *
	 */
	public int getPhotonCount();

	/**
	 * Sets start of transient (bin index in decay).
	 *
	 */
	public void setTransStart(int transStart);

	/**
	 * Gets start of transient.
	 *
	 */
	public int getTransStart();

	/**
	 * Sets start of data.
	 *
	 */
	public void setDataStart(int dataStart);

	/**
	 * Gets start of data.
	 *
	 */
	public int getDataStart();

	/**
	 * Sets end of transient.
	 *
	 */
	public void setTransStop(int transStop);

	/**
	 * Gets end of transient.
	 *
	 */
	public int getTransStop();

}
