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
		
		// 200,000 * 24 bytes ~= 4.8Mb memory usage
		int totalBins = 100000;
		setTotalBins(totalBins);
		
		// bin z values from -50.0..+50.0
		double minRange = -50.0;
		double maxRange = 50.0;
		setMinMaxRange(minRange, maxRange);
	}
}