/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process.interactive.ui;

/**
 *
 * @author Aivar Grislis
 */
public interface UserInterfacePanelListener {

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

