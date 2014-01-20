/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.slim2.process.interactive.grayscale;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.threshold.ThresholdMethod;
import imagej.data.threshold.ThresholdService;
import imagej.display.Display;
import java.util.List;
import net.imglib2.histogram.Histogram1d;
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
	CrossHairOverlay crossHairOverlay;
	
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
//TODO ARG not used	
	public double[] getThreshold() {
		double[] returnValue = null;
		ThresholdService thresholdService = context.getService(ThresholdService.class);
		System.out.println("thresholdService is " + thresholdService);
		ImageDisplayService imageDisplayService = context.getService(ImageDisplayService.class);
		System.out.println("imageDisplayService is " + imageDisplayService);
		
		ImageDisplay imageDisplay = imageDisplayService.getActiveImageDisplay();
		System.out.println("imageDisplay is " + imageDisplay);
		//TODO throws exception here NPE at DefaultThresholdService.java:95
		boolean alreadyHadOne = false;
		//boolean alreadyHadOne = thresholdService.hasThreshold(imageDisplay);
		System.out.println("alreadyHadOne is " + alreadyHadOne);
		if (!alreadyHadOne) {
			//TODO ARG this call also shows the pixels between min & max in red
			ThresholdOverlay overlay = thresholdService.getThreshold(imageDisplay);
			System.out.println("overlay is " + overlay);
			System.out.println(" ranges "  + overlay.getRangeMax() + " " + overlay.getRangeMin());
			return new double[] { overlay.getRangeMax(), overlay.getRangeMin() };
		}
		return returnValue;
	}
	
	public double[] estimateThreshold() {
	if (false) {
		//TODO ARG how to find a/default(?) threshold method
		ThresholdService thresholdService = context.getService(ThresholdService.class);
		List<String> thresholdMethodNames = thresholdService.getThresholdMethodNames();
		ThresholdMethod thresholdMethod = thresholdService.getThresholdMethod(thresholdMethodNames.get(0));

		//TODO ARG how to check for existing threshold
		ImageDisplayService imageDisplayService = context.getService(ImageDisplayService.class);
		ImageDisplay imageDisplay = imageDisplayService.getActiveImageDisplay();
		boolean alreadyHadThreshold = thresholdService.hasThreshold(imageDisplay); }
	
		double[] thresholds = new double[] { 0.0, 1000.0 };// (double) threshold, Double.MAX_VALUE }; //TODO s/b long threshold
		return thresholds;
	}
	
	public void setThreshold(int thresholdMin, int thresholdMax) {
		if (null == thresholdOverlay) {
			System.out.println("CREATING THRESHOLD OVERLAY");
			thresholdOverlay = new ThresholdDisplayOverlay(context, dataset, thresholdMin, thresholdMax);
			//display.display(thresholdOverlay);
		}
		System.out.println("setThreshold " + thresholdMin + " " + thresholdMax);
		thresholdOverlay.setThreshold(thresholdMin, thresholdMax);
	}
	
	public void setPixel(long[] position) {
		System.out.println("setPixel position length is " + position.length + " x y " + position[0] + " " + position[1]);
		double[] doublePosition = new double[position.length];
		for (int i = 0; i < position.length; ++i) {
			doublePosition[i] = position[i];
		}
		if (null == crossHairOverlay) {
			crossHairOverlay = new CrossHairOverlay(context, dataset);
			display.display(crossHairOverlay);
		}
		crossHairOverlay.setPoint(doublePosition);
	}
	
	public void close() {
		display.close();
	}
}
