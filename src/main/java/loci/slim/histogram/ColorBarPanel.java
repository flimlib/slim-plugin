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

package loci.slim.histogram;

import ij.process.LUT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Displays a color bar with the current colorization scheme. Live, reflects
 * ongoing changes.
 *
 * @author Aivar Grislis
 */
public class ColorBarPanel extends JPanel {

	private final Object _synchObject = new Object();
	private final int _width;
	private final int _height;
	private final int _inset;
	private Color[] _color;
	double _minView;
	double _maxView;
	double _minLUT;
	double _maxLUT;

	/**
	 * Constructor. Note that for best results width should be 254, so that there
	 * is a 1:1 relationship between colors and pixels.
	 *
	 */
	public ColorBarPanel(final int width, final int inset, final int height) {
		super();

		_width = width;
		_inset = inset;
		_height = height;

		setPreferredSize(new Dimension(width + 2 * inset, height));

		_minView = _maxView = _minLUT = _maxLUT = 0.0f;
	}

	/**
	 * Changes the color look-up table and redraws.
	 *
	 */
	public void setLUT(final LUT lut) {
		synchronized (_synchObject) {
			_color = colorsFromLUT(lut);
		}
		repaint();
	}

	/**
	 * Changes the values and redraws.
	 *
	 */
	public void setMinMax(final double minView, final double maxView,
		final double minLUT, final double maxLUT)
	{
		synchronized (_synchObject) {
			_minView = minView;
			_maxView = maxView;
			_minLUT = minLUT;
			_maxLUT = maxLUT;
		}
		repaint();
	}

	/**
	 * Changes the LUT ranges and redraws.
	 *
	 */
	public void setMinMaxLUT(final double minLUT, final double maxLUT) {
		synchronized (_synchObject) {
			_minLUT = minLUT;
			_maxLUT = maxLUT;
		}
		repaint();
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (null != _color) {
			synchronized (_synchObject) {
				for (int i = 0; i < _width; ++i) {
					g.setColor(colorize(i));
					g.drawLine(_inset + i, 0, _inset + i, _height - 1);
				}
			}
		}
	}

	/*
	 * Builds a full 256 color array of Colors from a LUT.
	 *
	 */
	private Color[] colorsFromLUT(final LUT lut) {
		final byte[] bytes = lut.getBytes();
		final int numberColors = bytes.length / 3;
		// TODO make sure numberColors is 256!
		final Color[] color = new Color[numberColors];
		for (int n = 0; n < numberColors; ++n) {
			final int red = 0xff & bytes[n];
			final int green = 0xff & bytes[256 + n];
			final int blue = 0xff & bytes[512 + n];
			color[n] = new Color(red, green, blue);
		}
		return color;
	}

	/*
	 * Given a pixel value 0..253 show appropriate color.
	 *
	 */
	private Color colorize(final int i) {
		// default color
		Color color = _color[0];

		// wait till we have initial range before any colorization
		if (_minView < _maxView) {

			// what is the value for this pixel?
			final double value = _minView + (_maxView - _minView) * i / _width;

			// if value within palette range
			if (value >= _minLUT && value <= _maxLUT) {

				// compute color index
				int index =
					1 + (int) ((value - _minLUT) * _color.length / (_maxLUT - _minLUT));

				// constrain to 1..253
				index = Math.max(index, 1);
				index = Math.min(index, _color.length - 3);

				// get the color
				color = _color[index];
			}
		}
		return color;
	}
}
