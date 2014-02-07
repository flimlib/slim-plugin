/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
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
