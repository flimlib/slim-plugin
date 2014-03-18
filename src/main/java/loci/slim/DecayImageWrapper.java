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

package loci.slim;

import loci.slim.fitting.IDecayImage;
import loci.slim.preprocess.IProcessor;
import net.imglib2.RandomAccess;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * This class wraps an image that has a decay curve for each pixel.
 * 
 * @author Aivar Grislis
 */
public class DecayImageWrapper<T extends RealType<T>> implements IDecayImage<T> {
	private ImgPlus<T> _image;
	private int _width;
	private int _height;
	private int _channels;
	private int _bins;
	private int _binIndex;
	private int _increment;
	private RandomAccess<T> _cursor;

	public DecayImageWrapper(ImgPlus<T> image, int width, int height,
			int channels, int bins, int binIndex, int increment) {
		_image    = image;
		_width    = width;
		_height   = height;
		_channels = channels;
		_bins     = bins;
		_binIndex = binIndex;
		_increment = increment;

		_cursor = image.randomAccess();
	}

	/**
	 * Gets width of image.
	 * 
	 * @return 
	 */
	@Override
	public int getWidth() {
		return _width;
	}

	/**
	 * Gets height of image.
	 * @return 
	 */
	@Override
	public int getHeight() {
		return _height;
	}

	/**
	 * Gets number of channels of image.
	 * 
	 * @return 
	 */
	@Override
	public int getChannels() {
		return _channels;
	}

	/**
	 * Gets number of bins in decay curve of image.
	 * 
	 * @return 
	 */
	@Override
	public int getBins() {
		return _bins;
	}

	/**
	 * Specifies a source IProcessor to be chained to this one.
	 * 
	 * @param processor 
	 */
	@Override
	public void chain(IProcessor processor) {
		throw new UnsupportedOperationException("Can't chain to DecayImageWrapper");
	}

	/**
	 * Gets input pixel decay curve.
	 * 
	 * @param location
	 * @return 
	 */
	@Override
	public double[] getPixel(int[] location) {
		double[] decay = new double[_bins];

		// add bins to location
		int[] innerLocation = new int[location.length + 1];
		for (int i = 0; i < _binIndex; ++i) {
			innerLocation[i] = location[i];
		}
		for (int i = _binIndex; i < location.length; ++i) {
			innerLocation[i + 1] = location[i];
		}

		for (int i = 0; i < _bins; ++i) {
			innerLocation[_binIndex] = i;
			_cursor.setPosition(innerLocation);
			decay[i] = _cursor.get().getRealFloat() / _increment;
		}
		return decay;
	}

	/**
	 * Gets underlying image.
	 */
	@Override
	public ImgPlus<T> getImage() {
		return _image;
	}
}
