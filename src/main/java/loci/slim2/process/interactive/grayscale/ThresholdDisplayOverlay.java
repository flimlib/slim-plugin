/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package loci.slim2.process.interactive.grayscale;

import imagej.data.Dataset;
import imagej.data.overlay.ThresholdOverlay;
import imagej.util.ColorRGB;
import imagej.util.Colors;
import net.imglib2.ops.pointset.PointSet;
import org.scijava.Context;

/**
 * The ThresholdDisplayOverlay
 * @author aivar
 */
public class ThresholdDisplayOverlay extends ThresholdOverlay {
	private static final ColorRGB COLOR_LESS = Colors.AQUA;
	private static final ColorRGB COLOR_WITHIN = Colors.PINK;
	private static final ColorRGB COLOR_GREATER = Colors.ORANGE;
	private final Dataset dataset;
	
	/**
	 * Construct a {@link ThresholdDisplayOverlay} on a {@link Dataset} given an
	 * {@link Context} context.
	 */
	public ThresholdDisplayOverlay(Context context, Dataset dataset, int thresholdMin, int thresholdMax)
	{
		super(context, dataset, thresholdMin, thresholdMax);
		this.dataset = dataset;
		dataset.rebuild();
		dataset.update();
		
		System.out.println("ThresholdDisplayOverlay ctor " + dataset);
		//super.setColorLess(COLOR_LESS);
		//super.setColorWithin(COLOR_WITHIN); //TODO ARG null);
		//super.setColorGreater(COLOR_GREATER);
	}

	/**
	 * Sets the threshold value.
	 * 
	 * @param thresholdMin
	 * @param thresholdMax
	 */
	public void setThreshold(int thresholdMin, int thresholdMax) {
		System.out.println("ThresholdDisplayOverlay " + thresholdMin + " " + thresholdMax);
		super.setRange(thresholdMin, thresholdMax);
		PointSet pointSet = getPointsWithin();
		System.out.println("pointSet size is " + pointSet.size());
		dataset.rebuild();
		dataset.update();
	}
}
