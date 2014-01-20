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
