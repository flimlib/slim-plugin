/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch;

import loci.slim.fitted.FittedValue;

/**
 * A batch histogram that represents tau lifetime values.
 * 
 * @author Aivar Grislis
 */
public class TauBatchHistogram extends AbstractBatchHistogram implements BatchHistogram {
	
	public void init(FittedValue fittedValue) {
		setFittedValue(fittedValue);
		
		// 200,000 * 24 bytes ~= 4.8Mb memory usage
		int totalBins = 200000;
		setTotalBins(totalBins);
		
		// expect tau values from 0.0..20.0 nanoseconds
		double minRange = 0.0;
		double maxRange = 20.0;
		setMinMaxRange(minRange, maxRange);
	}
}
