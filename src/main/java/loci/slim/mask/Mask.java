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

package loci.slim.mask;

import ij.IJ;

import java.util.Collection;

/**
 * Class for keeping track of exclusion masks.
 * 
 * Similar to a ROI, but these masks are used while examining fitted results.
 *
 * @author Aivar Grislis
 */
public class Mask implements Cloneable {
	private boolean[][] _bits;
	private int _width;
	private int _height;

	public Mask(boolean[][] bits) {
		_width = bits[0].length;
		_height = bits.length;
		_bits = bits;
	}

	public Mask(int width, int height) {
		_width = width;
		_height = height;
		// create array of FALSE
		_bits = new boolean[width][height];
	}

	public Mask clone() {
		boolean[][] bits = new boolean[_width][_height];
		for (int y = 0; y < _height; ++y) {
			for (int x = 0; x < _width; ++x) {
				bits[x][y] = _bits[x][y];
			}
		}
		return new Mask(bits);
	}

	/**
	 * Gets the boolean switches.
	 * 
	 * @return 
	 */
	public boolean[][] getBits() {
		return _bits;
	}

	/**
	 * Sets the boolean switches.
	 * 
	 * @param bits 
	 */
	public void setBits(boolean[][] bits) {
		_bits = bits;
	}

	/**
	 * Test whether a given x and y is masked.
	 * 
	 * @param x
	 * @param y
	 * @return 
	 */
	public boolean test(int x, int y) {
		boolean result = true;
		if (null != _bits) {
			result = _bits[x][y];
		}
		return result;
	}

	/**
	 * Sets a masked x and y.
	 * 
	 * @param x
	 * @param y 
	 */
	public void set(int x, int y) {
		if (null != _bits) {
			_bits[x][y] = true;
		}
	}

	/**
	 * Any masking going on?
	 * 
	 * @return 
	 */
	public boolean hasExcludedPixels() {
		for (int y = 0; y < _height; ++y) {
			for (int x = 0; x < _width; ++x) {
				if (!test(x, y)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Is mask equivalent to another mask?
	 * 
	 * @param mask
	 * @return 
	 */
	public boolean equals(Mask mask) {
		boolean returnValue = false;
		if (null == mask) {
			// no mask means all pixels selected
			boolean[][] bits = mask.getBits();
			for (int y = 0; y < _height; ++y) {
				for (int x = 0; x < _width; ++x) {
					if (!bits[x][y]) {
						return false;
					}
				}
			}
		}
		else {
			boolean[][] bits = mask.getBits();
			if (bits.length == _bits.length) {
				if (bits[0].length == _bits[0].length) {
					for (int y = 0; y < _height; ++y) {
						for (int x = 0; x < _width; ++x) {
							if (bits[x][y] != _bits[x][y]) {
								return false;
							}
						}
					}
					returnValue = true;
				}
			}
		}
		return returnValue;
	}

	/**
	 * Adds given mask to current mask, generating a new mask.
	 * 
	 * @param mask
	 * @return 
	 */
	public Mask add(Mask mask) {
		if (null == mask) {
			return clone();
		}
		boolean[][] bits = mask.getBits();
		boolean[][] result = new boolean[_width][_height];
		for (int x = 0; x < _width; ++x) {
			for (int y = 0; y < _height; ++y) {
				result[x][y] = _bits[x][y] && bits[x][y];
			}
		}
		return new Mask(result);
	}

	/**
	 * Given a collection of masks, adds them all together.
	 * <p>
	 * Having this be part of the Mask class hides implementation details.
	 * 
	 * @param masks
	 * @return mask or null
	 */
	public static Mask addMasks(Collection<Mask> masks) {
		Mask returnValue = null;
		if (!masks.isEmpty()) {
			boolean[][] result = null;
			int width  = 0;
			int height = 0;

			for (Mask mask : masks) {
				if (null != mask) {
					boolean[][] maskBits = mask.getBits();
					if (null == result) {
						width  = maskBits[0].length;
						height = maskBits.length;
						result = new boolean[width][height];
						for (int y = 0; y < height; ++y) {
							for (int x = 0; x < width; ++x) {
								result[x][y] = true;
							}
						}
					}
					for (int y = 0; y < height; ++y) {
						for (int x = 0; x < width; ++x) {
							result[x][y] = result[x][y] && maskBits[x][y];
						}
					}
				}
			}
			if (null != result) {
				returnValue = new Mask(result);
			}
		}
		return returnValue;
	}

	public int getCount() {
		int count = 0;
		for (int y = 0; y < _height; ++y) {
			for (int x = 0; x < _width; ++x) {
				if (_bits[x][y]) {
					++count;
				}
			}
		}
		return count;
	}

	public void debug() {
		if (null == _bits) {
			IJ.log("NONE");
		}
		else {
			final StringBuilder sb = new StringBuilder();
			for (int y = 0; y < _height; ++y) {
				for (int x = 0; x < _width; ++x) {
					sb.append(" " + _bits[x][y]);
				}
				sb.append("\n");
			}
			IJ.log(sb.toString());
		}
	}
}
