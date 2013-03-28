/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch;

import loci.slim.fitted.FittedValue;

/**
 * A batch histogram that represents Z baseline values.
 * 
 * @author Aivar Grislis
 */
public class ZBatchHistogram extends AbstractBatchHistogram implements BatchHistogram {

	@Override
	public void init(FittedValue fittedValue) {
		setFittedValue(fittedValue);
		
		// 100,000 * 24 bytes ~= 2.4Mb memory usage
		int totalBins = 100000;
		setTotalBins(totalBins);
		
		// expect z values from -10.0..+10.0
		double minRange = -10.0;
		double maxRange = 10.0;
		setMinMaxRange(minRange, maxRange);
	}
}