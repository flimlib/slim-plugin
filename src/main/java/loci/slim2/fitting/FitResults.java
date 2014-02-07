/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim2.fitting;

/**
 * Interface for container for local fitted results for current pixel.  Also
 * contains some inputs to the fit that are displayed with results.
 * 
 * @author Aivar Grislis
 */
public interface FitResults {

	/**
	 * Sets error code (or 0 for no error).
	 * 
	 * @param errorCode 
	 */
	public void setErrorCode(int errorCode);

	/**
	 * Gets error code.
	 * 
	 * @return 
	 */
	public int getErrorCode();

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

	/**
	 * Sets incoming transient data.
	 * 
	 * @param decay 
	 */
	public void setTransient(double[] trans);
	
	/**
	 * Gets incoming transient data.
	 * 
	 * @return 
	 */
	public double[] getTransient();
	
	/**
	 * Sets total photon count in decay.
	 * 
	 * @param photonCount
	 */
	public void setPhotonCount(int photonCount);

	/**
	 * Gets total photon count in decay.
	 * 
	 * @return 
	 */
	public int getPhotonCount();

	/**
	 * Sets start of transient (bin index in decay).
	 * 
	 * @param transStart 
	 */
	public void setTransStart(int transStart);

	/**
	 * Gets start of transient.
	 * 
	 * @return 
	 */
	public int getTransStart();

	/**
	 * Sets start of data.
	 * 
	 * @param dataStart 
	 */
	public void setDataStart(int dataStart);

	/**
	 * Gets start of data.
	 * 
	 * @return 
	 */
	public int getDataStart();

	/**
	 * Sets end of transient.
	 * 
	 * @param transStop 
	 */
	public void setTransStop(int transStop);

	/**
	 * Gets end of transient.
	 * 
	 * @return 
	 */
	public int getTransStop();
	
}
