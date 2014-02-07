/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import loci.slim.IGrayScaleImage;
import loci.slim.mask.IMaskGroup;

/**
 * Simple image that just displays one of the input parameters.
 *
 * @author Aivar Grislis
 */
public class SimpleFittedImage extends AbstractBaseFittedImage {
	private int _parameterIndex;

	/**
	 * Constructor, giving index of input parameter to display
	 *
	 * @param title
	 * @param dimension
	 * @param indexColorModel
	 * @param parameterIndex
	 * @param colorizeGrayScale
	 * @param grayScalePixelValue;
	 * @param maskGroup;
	 */
	public SimpleFittedImage(String title, int[] dimension,
			IndexColorModel indexColorModel, int parameterIndex,
			boolean colorizeGrayScale, IGrayScaleImage grayScaleImage,
			IMaskGroup[] maskGroup) {
		super(title, dimension, indexColorModel, colorizeGrayScale,
				grayScaleImage, maskGroup);
		_parameterIndex = parameterIndex;
	}

	/**
	 * Given the array of fitted parameters, get the value for this image.
	 * 
	 * @param parameters
	 * @return 
	 */
	@Override
	public double getValue(double[] parameters) {
		return parameters[_parameterIndex];
	}
}
