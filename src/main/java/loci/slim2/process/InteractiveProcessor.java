/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process;

import imagej.data.Dataset;

/**
 *
 * @author Aivar Grislis
 */
public interface InteractiveProcessor {
	
	/**
	 * Gets current fit settings.
	 * 
	 * @return 
	 */
	public FitSettings getFitSettings();

	/**
	 * Sets fit settings.
	 * 
	 * @param fitSettings 
	 */
	public void setFitSettings(FitSettings fitSettings);

	/**
	 * Processes a {@link Dataset}.
	 * <p>
	 * Returns to load a new {@link Dataset} or to quit.
	 * 
	 * @param dataset 
	 * @return whether to quit
	 */
	public boolean process(Dataset dataset);
}
