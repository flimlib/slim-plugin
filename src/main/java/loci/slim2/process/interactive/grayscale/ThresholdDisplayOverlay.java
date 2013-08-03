/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process.interactive.grayscale;

import imagej.data.Dataset;
import imagej.data.overlay.ThresholdOverlay;
import org.scijava.Context;

/**
 *
 * @author aivar
 */
public class ThresholdDisplayOverlay extends ThresholdOverlay {
	
	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link Context} context.
	 */
	public ThresholdDisplayOverlay(Context context, Dataset dataset)
	{
		super(context, dataset, 0.0, 100.0);
	}
	
	/**
	 * Construct a {@link ThresholdOverlay} on a {@link Dataset} given an
	 * {@link Context} context, and a numeric range within which the data values of
	 * interest exist.
	 */
	public ThresholdDisplayOverlay(Context context, Dataset ds, int threshold)
	{
		super(context, ds, 0.0, threshold);
	}
}
