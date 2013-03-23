/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch;

import loci.slim.fitted.FittedValue;

/**
 * A batch histogram that represents fractional contribution f values.
 * 
 * @author Aivar Grislis
 */
public class FractionalContribBatchHistogram extends AbstractBatchHistogram implements BatchHistogram {

	@Override
	public void init(FittedValue fittedValue) {
		setFittedValue(fittedValue);
		
		// 100,000 * 24 bytes ~= 2.4Mb memory usage
		int totalBins = 100000;
		setTotalBins(totalBins);
		
		// expect f values from 0.0...1.0
		double minRange = 0.0;
		double maxRange = 1.0;
		setMinMaxRange(minRange, maxRange);
	}
}
