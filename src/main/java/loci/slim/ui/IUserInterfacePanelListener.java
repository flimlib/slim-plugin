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

package loci.slim.ui;

/**
 * Listens for user input that triggers changes external to the ui panel.
 *
 * @author Aivar Grislis
 */
public interface IUserInterfacePanelListener {

    /**
     * Triggers a fit.
     */
    public void doFit();
	
	/**
	 * Triggers a refit.
	 */
	public void reFit();

    /**
     * Cancels ongoing fit.
     */
    public void cancelFit();

    /**
     * Quits running plugin.
     */
    public void quit();
	
	/**
	 * Opens new file(s).
	 */
	public void openFile();
	
    /**
     * Loads an excitation curve from file.
     *
     * @param fileName
     * @return whether successful
     */
    public boolean loadExcitation(String fileName);

    /**
     * Creates an excitation curve from current X, Y and saves to file.
     *
     * @param fileName
     * @return whether successful
     */
    public boolean createExcitation(String fileName);
	
    /**
     * Estimates an excitation curve from current X, Y and saves to file.
     *
     * @param fileName
     * @return whether successful
     */
    public boolean estimateExcitation(String fileName);
	
	/**
	 * Creates excitation curve from gaussian.
	 * 
	 * @param fileName
	 * @return whether successful
	 */
	public boolean gaussianExcitation(String fileName);

    /**
     * Cancels the current excitation curve, if any.
     */
    public void cancelExcitation();

    /**
     * Estimates the prompt and decay cursors.
     */
    public void estimateCursors();
}
