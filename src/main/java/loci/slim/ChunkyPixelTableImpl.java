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

/**
 * Table used for the "chunky pixel" effect.
 *
 * @author Aivar Grislis
 */
public class ChunkyPixelTableImpl implements IChunkyPixelTable {
	static final int WIDTH  = 16;
	static final int HEIGHT = 16;
	static final ChunkyPixel[] _table = {

		// divide 16x16 square into 4 8x8 parts

		new ChunkyPixel( 0,  0, 16, 16), // means: at (0, 0), draw 16x16
		new ChunkyPixel( 8,  0,  8, 16),
		new ChunkyPixel( 0,  8,  8,  8),
		new ChunkyPixel( 8,  8,  8,  8),

		// further divide 4 8z8 parts into 16 4x4 parts

		new ChunkyPixel( 4,  0,  4,  8), // divide 8x8 at (0, 0)
		new ChunkyPixel( 0,  4,  4,  4),
		new ChunkyPixel( 4,  4,  4,  4),

		new ChunkyPixel(12,  0,  4,  8), // divide 8x8 at (8, 0)
		new ChunkyPixel( 8,  4,  4,  4),
		new ChunkyPixel(12,  4,  4,  4),

		new ChunkyPixel( 4,  8,  4,  8), // divide 8x8 at (0, 8)
		new ChunkyPixel( 0, 12,  4,  4),
		new ChunkyPixel( 4, 12,  4,  4),

		new ChunkyPixel(12,  8,  4,  8), // divide 8x8 at (8, 8)
		new ChunkyPixel( 8, 12,  4,  4),
		new ChunkyPixel(12, 12,  4,  4),

		// further divide 16 4x4 parts into 64 2x2 parts

		new ChunkyPixel( 2,  0,  2,  4), // divide 4x4 at (0, 0)
		new ChunkyPixel( 0,  2,  2,  2),
		new ChunkyPixel( 2,  2,  2,  2),

		new ChunkyPixel( 6,  0,  2,  4), // divide 4x4 at (4, 0)
		new ChunkyPixel( 4,  2,  2,  2),
		new ChunkyPixel( 6,  2,  2,  2),

		new ChunkyPixel(10,  0,  2,  4), // divide 4x4 at (8, 0)
		new ChunkyPixel( 8,  2,  2,  2),
		new ChunkyPixel(10,  2,  2,  2),

		new ChunkyPixel(14,  0,  2,  4), // divide 4x4 at (12, 0)
		new ChunkyPixel(12,  2,  2,  2),
		new ChunkyPixel(14,  2,  2,  2),

		new ChunkyPixel( 2,  4,  2,  4), // divide 4x4 at (0, 4)
		new ChunkyPixel( 0,  6,  2,  2),
		new ChunkyPixel( 2,  6,  2,  2),

		new ChunkyPixel( 6,  4,  2,  4), // divide 4x4 at (4, 4)
		new ChunkyPixel( 4,  6,  2,  2),
		new ChunkyPixel( 6,  6,  2,  2),

		new ChunkyPixel(10,  4,  2,  4), // divide 4x4 at (8, 4)
		new ChunkyPixel( 8,  6,  2,  2),
		new ChunkyPixel(10,  6,  2,  2),

		new ChunkyPixel(14,  4,  2,  4), // divide 4x4 at (12, 4)
		new ChunkyPixel(12,  6,  2,  2),
		new ChunkyPixel(14,  6,  2,  2),

		new ChunkyPixel( 2,  8,  2,  4), // divide 4x4 at (0, 8)
		new ChunkyPixel( 0, 10,  2,  2),
		new ChunkyPixel( 2, 10,  2,  2),

		new ChunkyPixel( 6,  8,  2,  4), // divide 4x4 at (4, 8)
		new ChunkyPixel( 4, 10,  2,  2),
		new ChunkyPixel( 6, 10,  2,  2),

		new ChunkyPixel(10,  8,  2,  4), // divide 4x4 at (8, 8)
		new ChunkyPixel( 8, 10,  2,  2),
		new ChunkyPixel(10, 10,  2,  2),

		new ChunkyPixel(14,  8,  2,  4), // divide 4x4 at (12, 8)
		new ChunkyPixel(12, 10,  2,  2),
		new ChunkyPixel(14, 10,  2,  2),

		new ChunkyPixel( 2, 12,  2,  4), // divide 4x4 at (0, 12)
		new ChunkyPixel( 0, 14,  2,  2),
		new ChunkyPixel( 2, 14,  2,  2),

		new ChunkyPixel( 6, 12,  2,  4), // divide 4x4 at (4, 12)
		new ChunkyPixel( 4, 14,  2,  2),
		new ChunkyPixel( 6, 14,  2,  2),

		new ChunkyPixel(10, 12,  2,  4), // divide 4x4 at (8, 12)
		new ChunkyPixel( 8, 14,  2,  2),
		new ChunkyPixel(10, 14,  2,  2),

		new ChunkyPixel(14, 12,  2,  4), // divide 4x4 at (12, 12)
		new ChunkyPixel(12, 14,  2,  2),
		new ChunkyPixel(14, 14,  2,  2),

		// further divide 64 2x2 parts into 256 1x1 parts

		new ChunkyPixel( 1,  0,  1,  2), // divide 2x2 at (0, 0)
		new ChunkyPixel( 0,  1,  1,  1),
		new ChunkyPixel( 1,  1,  1,  1),

		new ChunkyPixel( 3,  0,  1,  2), // divide 2x2 at (2, 0)
		new ChunkyPixel( 2,  1,  1,  1),
		new ChunkyPixel( 3,  1,  1,  1),

		new ChunkyPixel( 5,  0,  1,  2), // divide 2x2 at (4, 0)
		new ChunkyPixel( 4,  1,  1,  1),
		new ChunkyPixel( 5,  1,  1,  1),

		new ChunkyPixel( 7,  0,  1,  2), // divide 2x2 at (6, 0)
		new ChunkyPixel( 6,  1,  1,  1),
		new ChunkyPixel( 7,  1,  1,  1),

		new ChunkyPixel( 9,  0,  1,  2), // divide 2x2 at (8, 0)
		new ChunkyPixel( 8,  1,  1,  1),
		new ChunkyPixel( 9,  1,  1,  1),

		new ChunkyPixel(11,  0,  1,  2), // divide 2x2 at (10, 0)
		new ChunkyPixel(10,  1,  1,  1),
		new ChunkyPixel(11,  1,  1,  1),

		new ChunkyPixel(13,  0,  1,  2), // divide 2x2 at (12, 0)
		new ChunkyPixel(12,  1,  1,  1),
		new ChunkyPixel(13,  1,  1,  1),

		new ChunkyPixel(15,  0,  1,  2), // divide 2x2 at (14, 0)
		new ChunkyPixel(14,  1,  1,  1),
		new ChunkyPixel(15,  1,  1,  1),


		new ChunkyPixel( 1,  2,  1,  2), // divide 2x2 at (0, 2)
		new ChunkyPixel( 0,  3,  1,  1),
		new ChunkyPixel( 1,  3,  1,  1),

		new ChunkyPixel( 3,  2,  1,  2), // divide 2x2 at (2, 2)
		new ChunkyPixel( 2,  3,  1,  1),
		new ChunkyPixel( 3,  3,  1,  1),

		new ChunkyPixel( 5,  2,  1,  2), // divide 2x2 at (4, 2)
		new ChunkyPixel( 4,  3,  1,  1),
		new ChunkyPixel( 5,  3,  1,  1),

		new ChunkyPixel( 7,  2,  1,  2), // divide 2x2 at (6, 2)
		new ChunkyPixel( 6,  3,  1,  1),
		new ChunkyPixel( 7,  3,  1,  1),

		new ChunkyPixel( 9,  2,  1,  2), // divide 2x2 at (8, 2)
		new ChunkyPixel( 8,  3,  1,  1),
		new ChunkyPixel( 9,  3,  1,  1),

		new ChunkyPixel(11,  2,  1,  2), // divide 2x2 at (10, 2)
		new ChunkyPixel(10,  3,  1,  1),
		new ChunkyPixel(11,  3,  1,  1),

		new ChunkyPixel(13,  2,  1,  2), // divide 2x2 at (12, 2)
		new ChunkyPixel(12,  3,  1,  1),
		new ChunkyPixel(13,  3,  1,  1),

		new ChunkyPixel(15,  2,  1,  2), // divide 2x2 at (14, 2)
		new ChunkyPixel(14,  3,  1,  1),
		new ChunkyPixel(15,  3,  1,  1),


		new ChunkyPixel( 1,  4,  1,  2), // divide 2x2 at (0, 4)
		new ChunkyPixel( 0,  5,  1,  1),
		new ChunkyPixel( 1,  5,  1,  1),

		new ChunkyPixel( 3,  4,  1,  2), // divide 2x2 at (2, 4)
		new ChunkyPixel( 2,  5,  1,  1),
		new ChunkyPixel( 3,  5,  1,  1),

		new ChunkyPixel( 5,  4,  1,  2), // divide 2x2 at (4, 4)
		new ChunkyPixel( 4,  5,  1,  1),
		new ChunkyPixel( 5,  5,  1,  1),

		new ChunkyPixel( 7,  4,  1,  2), // divide 2x2 at (6, 4)
		new ChunkyPixel( 6,  5,  1,  1),
		new ChunkyPixel( 7,  5,  1,  1),

		new ChunkyPixel( 9,  4,  1,  2), // divide 2x2 at (8, 4)
		new ChunkyPixel( 8,  5,  1,  1),
		new ChunkyPixel( 9,  5,  1,  1),

		new ChunkyPixel(11,  4,  1,  2), // divide 2x2 at (10, 4)
		new ChunkyPixel(10,  5,  1,  1),
		new ChunkyPixel(11,  5,  1,  1),

		new ChunkyPixel(13,  4,  1,  2), // divide 2x2 at (12, 4)
		new ChunkyPixel(12,  5,  1,  1),
		new ChunkyPixel(13,  5,  1,  1),

		new ChunkyPixel(15,  4,  1,  2), // divide 2x2 at (14, 4)
		new ChunkyPixel(14,  5,  1,  1),
		new ChunkyPixel(15,  5,  1,  1),


		new ChunkyPixel( 1,  6,  1,  2), // divide 2x2 at (0, 6)
		new ChunkyPixel( 0,  7,  1,  1),
		new ChunkyPixel( 1,  7,  1,  1),

		new ChunkyPixel( 3,  6,  1,  2), // divide 2x2 at (2, 6)
		new ChunkyPixel( 2,  7,  1,  1),
		new ChunkyPixel( 3,  7,  1,  1),

		new ChunkyPixel( 5,  6,  1,  2), // divide 2x2 at (4, 6)
		new ChunkyPixel( 4,  7,  1,  1),
		new ChunkyPixel( 5,  7,  1,  1),

		new ChunkyPixel( 7,  6,  1,  2), // divide 2x2 at (6, 6)
		new ChunkyPixel( 6,  7,  1,  1),
		new ChunkyPixel( 7,  7,  1,  1),

		new ChunkyPixel( 9,  6,  1,  2), // divide 2x2 at (8, 6)
		new ChunkyPixel( 8,  7,  1,  1),
		new ChunkyPixel( 9,  7,  1,  1),

		new ChunkyPixel(11,  6,  1,  2), // divide 2x2 at (10, 6)
		new ChunkyPixel(10,  7,  1,  1),
		new ChunkyPixel(11,  7,  1,  1),

		new ChunkyPixel(13,  6,  1,  2), // divide 2x2 at (12, 6)
		new ChunkyPixel(12,  7,  1,  1),
		new ChunkyPixel(13,  7,  1,  1),

		new ChunkyPixel(15,  6,  1,  2), // divide 2x2 at (14, 6)
		new ChunkyPixel(14,  7,  1,  1),
		new ChunkyPixel(15,  7,  1,  1),


		new ChunkyPixel( 1,  8,  1,  2), // divide 2x2 at (0, 8)
		new ChunkyPixel( 0,  9,  1,  1),
		new ChunkyPixel( 1,  9,  1,  1),

		new ChunkyPixel( 3,  8,  1,  2), // divide 2x2 at (2, 8)
		new ChunkyPixel( 2,  9,  1,  1),
		new ChunkyPixel( 3,  9,  1,  1),

		new ChunkyPixel( 5,  8,  1,  2), // divide 2x2 at (4, 8)
		new ChunkyPixel( 4,  9,  1,  1),
		new ChunkyPixel( 5,  9,  1,  1),

		new ChunkyPixel( 7,  8,  1,  2), // divide 2x2 at (6, 8)
		new ChunkyPixel( 6,  9,  1,  1),
		new ChunkyPixel( 7,  9,  1,  1),

		new ChunkyPixel( 9,  8,  1,  2), // divide 2x2 at (8, 8)
		new ChunkyPixel( 8,  9,  1,  1),
		new ChunkyPixel( 9,  9,  1,  1),

		new ChunkyPixel(11,  8,  1,  2), // divide 2x2 at (10, 8)
		new ChunkyPixel(10,  9,  1,  1),
		new ChunkyPixel(11,  9,  1,  1),

		new ChunkyPixel(13,  8,  1,  2), // divide 2x2 at (12, 8)
		new ChunkyPixel(12,  9,  1,  1),
		new ChunkyPixel(13,  9,  1,  1),

		new ChunkyPixel(15,  8,  1,  2), // divide 2x2 at (14, 8)
		new ChunkyPixel(14,  9,  1,  1),
		new ChunkyPixel(15,  9,  1,  1),


		new ChunkyPixel( 1, 10,  1,  2), // divide 2x2 at (0, 10)
		new ChunkyPixel( 0, 11,  1,  1),
		new ChunkyPixel( 1, 11,  1,  1),

		new ChunkyPixel( 3, 10,  1,  2), // divide 2x2 at (2, 10)
		new ChunkyPixel( 2, 11,  1,  1),
		new ChunkyPixel( 3, 11,  1,  1),

		new ChunkyPixel( 5, 10,  1,  2), // divide 2x2 at (4, 10)
		new ChunkyPixel( 4, 11,  1,  1),
		new ChunkyPixel( 5, 11,  1,  1),

		new ChunkyPixel( 7, 10,  1,  2), // divide 2x2 at (6, 10)
		new ChunkyPixel( 6, 11,  1,  1),
		new ChunkyPixel( 7, 11,  1,  1),

		new ChunkyPixel( 9, 10,  1,  2), // divide 2x2 at (8, 10)
		new ChunkyPixel( 8, 11,  1,  1),
		new ChunkyPixel( 9, 11,  1,  1),

		new ChunkyPixel(11, 10,  1,  2), // divide 2x2 at (10, 10)
		new ChunkyPixel(10, 11,  1,  1),
		new ChunkyPixel(11, 11,  1,  1),

		new ChunkyPixel(13, 10,  1,  2), // divide 2x2 at (12, 10)
		new ChunkyPixel(12, 11,  1,  1),
		new ChunkyPixel(13, 11,  1,  1),

		new ChunkyPixel(15, 10,  1,  2), // divide 2x2 at (14, 10)
		new ChunkyPixel(14, 11,  1,  1),
		new ChunkyPixel(15, 11,  1,  1),


		new ChunkyPixel( 1, 12,  1,  2), // divide 2x2 at (0, 12)
		new ChunkyPixel( 0, 13,  1,  1),
		new ChunkyPixel( 1, 13,  1,  1),

		new ChunkyPixel( 3, 12,  1,  2), // divide 2x2 at (2, 12)
		new ChunkyPixel( 2, 13,  1,  1),
		new ChunkyPixel( 3, 13,  1,  1),

		new ChunkyPixel( 5, 12,  1,  2), // divide 2x2 at (4, 12)
		new ChunkyPixel( 4, 13,  1,  1),
		new ChunkyPixel( 5, 13,  1,  1),

		new ChunkyPixel( 7, 12,  1,  2), // divide 2x2 at (6, 12)
		new ChunkyPixel( 6, 13,  1,  1),
		new ChunkyPixel( 7, 13,  1,  1),

		new ChunkyPixel( 9, 12,  1,  2), // divide 2x2 at (8, 12)
		new ChunkyPixel( 8, 13,  1,  1),
		new ChunkyPixel( 9, 13,  1,  1),

		new ChunkyPixel(11, 12,  1,  2), // divide 2x2 at (10, 12)
		new ChunkyPixel(10, 13,  1,  1),
		new ChunkyPixel(11, 13,  1,  1),

		new ChunkyPixel(13, 12,  1,  2), // divide 2x2 at (12, 12)
		new ChunkyPixel(12, 13,  1,  1),
		new ChunkyPixel(13, 13,  1,  1),

		new ChunkyPixel(15, 12,  1,  2), // divide 2x2 at (14, 12)
		new ChunkyPixel(14, 13,  1,  1),
		new ChunkyPixel(15, 13,  1,  1),


		new ChunkyPixel( 1, 14,  1,  2), // divide 2x2 at (0, 14)
		new ChunkyPixel( 0, 15,  1,  1),
		new ChunkyPixel( 1, 15,  1,  1),

		new ChunkyPixel( 3, 14,  1,  2), // divide 2x2 at (2, 14)
		new ChunkyPixel( 2, 15,  1,  1),
		new ChunkyPixel( 3, 15,  1,  1),

		new ChunkyPixel( 5, 14,  1,  2), // divide 2x2 at (4, 14)
		new ChunkyPixel( 4, 15,  1,  1),
		new ChunkyPixel( 5, 15,  1,  1),

		new ChunkyPixel( 7, 14,  1,  2), // divide 2x2 at (6, 14)
		new ChunkyPixel( 6, 15,  1,  1),
		new ChunkyPixel( 7, 15,  1,  1),

		new ChunkyPixel( 9, 14,  1,  2), // divide 2x2 at (8, 14)
		new ChunkyPixel( 8, 15,  1,  1),
		new ChunkyPixel( 9, 15,  1,  1),

		new ChunkyPixel(11, 14,  1,  2), // divide 2x2 at (10, 14)
		new ChunkyPixel(10, 15,  1,  1),
		new ChunkyPixel(11, 15,  1,  1),

		new ChunkyPixel(13, 14,  1,  2), // divide 2x2 at (12, 14)
		new ChunkyPixel(12, 15,  1,  1),
		new ChunkyPixel(13, 15,  1,  1),

		new ChunkyPixel(15, 14,  1,  2), // divide 2x2 at (14, 14)
		new ChunkyPixel(14, 15,  1,  1),
		new ChunkyPixel(15, 15,  1,  1)
	};

	@Override
	public int size() {
		return WIDTH * HEIGHT;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public ChunkyPixel getChunkyPixel(int index) {
		ChunkyPixel chunkyPixel = null;
		if (index < _table.length) {
			chunkyPixel = _table[index];
		}
		return chunkyPixel;
	}
}
