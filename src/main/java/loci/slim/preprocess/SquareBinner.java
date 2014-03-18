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

package loci.slim.preprocess;

/**
 * This class bins the image.
 * 
 * @author Aivar Grislis
 */
public class SquareBinner implements IProcessor {
	private int _size;
	private int _width;
	private int _height;
	private IProcessor _processor;

	/**
	 * Initializes the binner.  Must be called once after instantiation and
	 * before use.
	 * 
	 * @param size
	 * @param width
	 * @param height 
	 */
	public void init(int size, int width, int height) {
		_size   = size;
		_width  = width;
		_height = height;
	}

	/**
	 * Specifies a source IProcessor to be chained to this one.
	 * 
	 * @param processor 
	 */
	public void chain(IProcessor processor) {
		_processor = processor;
	}

	/**
	 * Gets input pixel value.
	 * 
	 * @param location
	 * @return pixel value
	 */
	public double[] getPixel(int[] location) {
		double[] sum = _processor.getPixel(location);
		if (null != sum) {
			// keep a running sum; don't change source pixel
			sum = sum.clone();

			int x = location[0];
			int y = location[1];

			int startX = x - _size;
			if (startX < 0) {
				startX = 0;
			}
			int stopX  = x + _size;
			if (stopX >= _width) {
				stopX = _width - 1;
			}
			int startY = y - _size;
			if (startY < 0) {
				startY = 0;
			}
			int stopY  = y + _size;
			if (stopY >= _height) {
				stopY = _height - 1;
			}

			for (int j = startY; j <= stopY; ++j) {
				for (int i = startX; i <= stopX; ++i) {
					if (j != y || i != x) {
						location[0] = i;
						location[1] = j;
						double[] pixel = _processor.getPixel(location);

						if (null != pixel) {
							add(sum, pixel);
						}
					}
				}
			}
		}
		return sum;
	}

	/*
	 * Adds together two decays.
	 */
	private void add(double[] sum, double[] pixel) {
		for (int i = 0; i < sum.length; ++i) {
			sum[i] += pixel[i];
		}
	}
}
