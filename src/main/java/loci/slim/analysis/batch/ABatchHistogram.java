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
		
		// 200,000 * 24 bytes ~= 4.8Mb memory usage
		int totalBins = 200000;
		setTotalBins(totalBins);
		
		// bin A values from 0.0..1000.0
		double minRange = 0.0;
		double maxRange = 1000.0;
		setMinMaxRange(minRange, maxRange);
	}
}
