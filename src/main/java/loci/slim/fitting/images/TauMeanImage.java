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

package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import loci.slim.IGrayScaleImage;
import loci.slim.mask.IMaskGroup;

/**
 * This class builds a fitted image that shows the tau mean.
 * 
 * Tau Mean Tmi = sum of all Fi * Ti, where Fi is the Fractional Intensity.
 * 
 * Fractional Intensity Fi = Ai / sum of all Aj.
 *
 * @author Aivar Grislis
 */
public class TauMeanImage extends AbstractBaseFittedImage {
	private int _component;
	private int _components;

	/**
	 * Create the fitted image.  Specifies number of components which should
	 * be 2 or 3 and the current component which is ignored.
	 * respectively.
	 * 
	 * @param title
	 * @param dimension
	 * @param component
	 * @param components 
	 */
	public TauMeanImage(String title, int[] dimension,
			IndexColorModel indexColorModel, int component, int components,
			boolean colorizeGrayScale, IGrayScaleImage grayScaleImage,
			IMaskGroup[] maskGroup) {
		super(title, dimension, indexColorModel, colorizeGrayScale,
				grayScaleImage, maskGroup);
		_component = component;
		_components = components;
	}

	@Override
	public double getValue(double[] parameters) {
		double value = 0.0;
		double sum = 0.0;
		switch (_components) {
			case 2:
				sum = parameters[FittedImageFitter.A1_INDEX]
						+ parameters[FittedImageFitter.A2_INDEX];
				value = parameters[FittedImageFitter.A1_INDEX]
							* parameters[FittedImageFitter.T1_INDEX]
						+ parameters[FittedImageFitter.A2_INDEX]
							* parameters[FittedImageFitter.T2_INDEX];
				break;
			case 3:
				sum = parameters[FittedImageFitter.A1_INDEX]
						+ parameters[FittedImageFitter.A2_INDEX]
						+ parameters[FittedImageFitter.A3_INDEX];
				value = parameters[FittedImageFitter.A1_INDEX]
							* parameters[FittedImageFitter.T1_INDEX]
						+ parameters[FittedImageFitter.A2_INDEX]
							* parameters[FittedImageFitter.T2_INDEX]
						+ parameters[FittedImageFitter.A3_INDEX]
							* parameters[FittedImageFitter.T3_INDEX];
				break;
		}
		return value / sum;
	}
}
