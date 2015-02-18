/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim2.process.interactive.grayscale;

import net.imagej.Dataset;
import net.imagej.overlay.ThresholdOverlay;
import net.imglib2.ops.pointset.PointSet;

import org.scijava.Context;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;

/**
 * The ThresholdDisplayOverlay
 * @author Aivar Grislis
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
