/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch;

import loci.slim.analysis.HistogramStatistics;
import loci.slim.fitted.FittedValue;

/**
 *
 * @author Aivar Grislis
 */
public interface BatchHistogram {

	/**
	 * Sets the fitted value converter.
	 * 
	 * @param fittedValue 
	 */
	public void init(FittedValue fittedValue);

	/**
	 * Gets the histogram title.
	 * 
	 * @return 
	 */
	public String getTitle();
	
	/**
	 * Processes fitted values for a pixel.
	 * 
	 * @param value 
	 */
	public void process(double[] value);
	
	/**
	 * Called periodically to get current statistics.
	 * 
	 * @return 
	 */
	public HistogramStatistics getStatistics();

	/**
	 * Returns scaled-down 256-bin histogram adjusted to interquartile range.
	 * 
	 * @return 
	 */
	public long[] getScaledHistogram();

	/**
	 * Returns scaled-down histogram adjusted to interquartile range.
	 * 
	 * @param binCount
	 * @return 
	 */
	public long[] getScaledHistogram(int binCount);

	/**
	 * Gets array of center values for 256-bin histogram.
	 * 
	 * @return 
	 */
	public double[] getScaledCenterValues();

	/**
	 * Gets array of center values for scaled histogram.
	 * 
	 * @param binCount
	 * @return 
	 */
	public double[] getScaledCenterValues(int binCount);
}
