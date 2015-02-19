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

package loci.slim;

import java.util.Iterator;

/**
 * Iterator that supplies a series of chunky pixels.
 *
 * @author Aivar Grislis
 */
public class ChunkyPixelEffectIterator implements Iterator {

	IChunkyPixelTable _table;
	int _width;
	int _height;
	int _index;
	int _x;
	int _y;
	ChunkyPixel _chunkyPixel;

	/**
	 * Constructor, sets up the chunky pixel iterator with a table of chunky
	 * pixels and the width and height of the image being processed.
	 *
	 * @param table
	 * @param width
	 * @param height
	 */
	public ChunkyPixelEffectIterator(final IChunkyPixelTable table,
		final int width, final int height)
	{
		_table = table;
		_width = width;
		_height = height;

		// initialize
		_index = 0;
		_x = 0;
		_y = 0;

		// get first chunky pixel
		_chunkyPixel = getNextChunkyPixel();
	}

	@Override
	public boolean hasNext() {
		return _chunkyPixel != null;
	}

	@Override
	public ChunkyPixel next() {
		final ChunkyPixel chunkyPixel = _chunkyPixel;
		_chunkyPixel = getNextChunkyPixel();
		return chunkyPixel;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/*
	 * Gets the next chunky pixel from the table.
	 *
	 */
	ChunkyPixel getNextChunkyPixel() {
		// get the relative chunky pixel from the table
		ChunkyPixel relChunkyPixel = _table.getChunkyPixel(_index);

		if (_x + relChunkyPixel.getX() >= _width) {
			// start next row
			_x = 0;
			_y += _table.getHeight();

			if (_y + relChunkyPixel.getY() >= _height) {
				// use next table entry, are we done?
				if (++_index >= _table.size()) {
					return null;
				}

				// start from the top
				_y = 0;

				// update relative chunky pixel
				relChunkyPixel = _table.getChunkyPixel(_index);
			}
		}

		// convert relative to absolute
		final int x = _x + relChunkyPixel.getX();
		final int y = _y + relChunkyPixel.getY();
		final ChunkyPixel absChunkyPixel =
			new ChunkyPixel(x, y, Math.min(relChunkyPixel.getWidth(), _width - x),
				Math.min(relChunkyPixel.getHeight(), _height - y));

		// set up for next call
		_x += _table.getWidth();

		return absChunkyPixel;
	}
}
