/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch;

import loci.slim.fitted.FittedValue;

/**
 * A batch histogram that represents chi square values.
 * 
 * @author Aivar Grislis
 */
public class ChiSqBatchHistogram extends AbstractBatchHistogram implements BatchHistogram {

	@Override
	public void init(FittedValue fittedValue) {
		setFittedValue(fittedValue);
		
		// 200,000 * 24 bytes ~= 4.8Mb memory usage
		int totalBins = 200000;
		setTotalBins(totalBins);
		
		// bin chisquare values from 0.0..50.0
		double minRange = 0.0;
		double maxRange = 50.0;
		setMinMaxRange(minRange, maxRange);
	}
}
