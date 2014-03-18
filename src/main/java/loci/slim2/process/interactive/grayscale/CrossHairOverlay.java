/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
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

import imagej.data.Dataset;
import imagej.data.overlay.PointOverlay;

import java.util.List;

import org.scijava.Context;

/**
 *
 * @author Aivar Grislis
 */
public class CrossHairOverlay extends PointOverlay {
	private final Dataset dataset;

	/**
	 * Construct a {@link CrossHairOverlay} given a {@link Context} context.
	 */
	public CrossHairOverlay(Context context, Dataset dataset)
	{
		super(context);
		this.dataset = dataset;
	}

	/**
	 * Sets the point.
	 * 
	 * @param point
	 */
	public void setPoint(double[] point) {
		List<double[]> pts = super.getPoints();
		pts.add(0, point);
		super.setPoints(pts);
		dataset.rebuild();
		dataset.update();
	}
}
