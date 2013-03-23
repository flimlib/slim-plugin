/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch;

import loci.slim.fitted.FittedValue;

/**
 * A batch histogram that represents A values.
 * 
 * @author Aivar Grislis
 */
public class ABatchHistogram extends AbstractBatchHistogram implements BatchHistogram {
	private int component;
	private int fittedParamIndex;
	

	@Override
	public void init(FittedValue fittedValue) {
		setFittedValue(fittedValue);
		
		// 100,000 * 24 bytes ~= 2.4Mb memory usage
		int totalBins = 100000;
		setTotalBins(totalBins);
		
		// expect z values from -50.0..+50.0
		double minRange = -50.0;
		double maxRange = 50.0;
		setMinMaxRange(minRange, maxRange);
	}
}
