/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process;

import java.io.File;

/**
 *
 * @author Aivar Grislis
 */
public interface BatchProcessor {

	/**
	 * Processes list of files with current settings.
	 * 
	 * @param files
	 * @param fitSettings 
	 */
	public void process(File[] files, FitSettings fitSettings);
}
