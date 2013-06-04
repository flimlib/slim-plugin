/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.display.DisplayService;

/**
 *
 * @author Aivar Grislis
 */
public interface InteractiveProcessor {
	
	/**
	 * Initializes with required services.
	 * 
	 * @param datasetService
	 * @param displayService 
	 */
	public void init(DatasetService datasetService, DisplayService displayService);
	
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
	 * 
	 * @param dataset 
	 * @return whether to quit (true) or load new Dataset (false)
	 */
	public boolean process(Dataset dataset);
}
