/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process.interactive.grayscale;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.threshold.ThresholdMethod;
import imagej.data.threshold.ThresholdService;
import imagej.display.Display;
import org.scijava.Context;

/**
 *
 * @author Aivar Grislis
 */
public class GrayscaleDisplay {
	Context context;
	Dataset dataset;
	Display display;
	ThresholdDisplayOverlay thresholdOverlay;
	
	public GrayscaleDisplay(Context context, Dataset dataset, Display display) {
		this.context = context;
		this.dataset = dataset;
		this.display = display;
	}
	
	//TODO ARG:
	/*
	 * I'm building the grayscale, so how could it have an existing threshold?
	 * A non-grayscale-displayed LT image has individual histogram bin photon
	 * counts, not summed together, so can't provide any sort of meaningful
	 * initial existing threshold.
	 */
/*
 * Should be able to support running the threshold tool _after_ the grayscale is
 * created.  Threshold tool needs to put out an event.  UI & processor needs to
 * accept a change in threshold from an external source.  No need to refit unless
 * summed.
 */
	
	public double[] getThreshold() {
		double[] returnValue = null;
		ThresholdService thresholdService = context.getService(ThresholdService.class);
		System.out.println("thresholdService is " + thresholdService);
		ImageDisplayService imageDisplayService = context.getService(ImageDisplayService.class);
		System.out.println("imageDisplayService is " + imageDisplayService);
		
		ImageDisplay imageDisplay = imageDisplayService.getActiveImageDisplay();
		System.out.println("imageDisplay is " + imageDisplay);
		boolean alreadyHadOne = thresholdService.hasThreshold(imageDisplay);
		System.out.println("alreadyHadOne is " + alreadyHadOne);
		if (!alreadyHadOne) {
			ThresholdOverlay overlay = thresholdService.getThreshold(imageDisplay);
			System.out.println("overlay is " + overlay);
			System.out.println(" ranges "  + overlay.getRangeMax() + " " + overlay.getRangeMin());
			return new double[] { overlay.getRangeMax(), overlay.getRangeMin() };
		}
		return returnValue;
	}
	
	public double[] estimateThreshold() {
		double[] returnValue = null;
		ThresholdService thresholdService = context.getService(ThresholdService.class);
		System.out.println("thresholdService is " + thresholdService);
		ImageDisplayService imageDisplayService = context.getService(ImageDisplayService.class);
		System.out.println("imageDisplayService is " + imageDisplayService);
		ImageDisplay imageDisplay = imageDisplayService.getActiveImageDisplay();
		System.out.println("imageDisplay is " + imageDisplay);
		boolean alreadyHadOne = thresholdService.hasThreshold(imageDisplay);
		ThresholdMethod thresholdMethod = thresholdService.getThresholdMethod(thresholdService.getThresholdMethodNames().get(0));
		long[] thresholds = new long[2];
	//	thresholdMethod.getThreshold(thresholds);
		return new double[] { (double) thresholds[0], (double) thresholds[1] };
	}
	
	public void setThreshold(int threshold) {
		if (null == thresholdOverlay) {
			System.out.println("CREATING THRESHOLD OVERLAY");
			thresholdOverlay = new ThresholdDisplayOverlay(context, dataset, threshold);
			display.display(thresholdOverlay);
		}
		else {
			//TODO ARG what?? thresholdOverlay.
		}
	}
	
}
