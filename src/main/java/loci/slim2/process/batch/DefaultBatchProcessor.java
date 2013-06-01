/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process.batch;

import java.io.File;
import loci.slim2.process.BatchProcessor;
import loci.slim2.process.FitSettings;

/**
 *
 * @author Aivar Grislis
 */
public class DefaultBatchProcessor implements BatchProcessor {

	@Override
	public void process(File[] files, FitSettings fitSettings) {
		for (File f : files) {
			System.out.println("batch process " + f);
		}
	}
}
