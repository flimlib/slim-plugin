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
		
		// 100,000 * 24 bytes ~= 2.4Mb memory usage
		int totalBins = 100000;
		setTotalBins(totalBins);
		
		// expect chisquare values from 0.0..2.0
		double minRange = 0.0;
		double maxRange = 2.0;
		setMinMaxRange(minRange, maxRange);
	}
}
