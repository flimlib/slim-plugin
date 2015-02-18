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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Builds a maximum or minimum value, rounding and formatting appropriately.
 *
 * @author Aivar Grislis
 */
public class DoubleFormatter {

	public static final char INFINITY = '\u221E';
	double _value;
	String _text;

	public DoubleFormatter(final boolean min, final int digits, final double value)
	{
		if (Double.isInfinite(value)) {
			if (value < 0.0) {
				_text = "-" + INFINITY;
			}
			else {
				_text = "" + INFINITY;
			}
		}
		else {
			try {
				final MathContext context =
					new MathContext(digits, min ? RoundingMode.FLOOR
						: RoundingMode.CEILING);
				final BigDecimal bigDecimalValue =
					BigDecimal.valueOf(value).round(context);
				_value = bigDecimalValue.doubleValue();
				_text = bigDecimalValue.toEngineeringString();
			}
			catch (final NumberFormatException e) {
				_value = 0.0;
				_text = "0.0";
			}
		}
	}

	/**
	 * Gets the rounded value.
	 *
	 * @return rounded value
	 */
	public double getValue() {
		return _value;
	}

	/**
	 * Gets the formatted string representation of value.
	 *
	 * @return formatted string
	 */
	public String getText() {
		final int index = _text.indexOf("-");
		if (index > 0) {
			if ('E' != _text.charAt(index - 1)) {
				ij.IJ.log("Funny text " + _text);
			}
		}
		return _text;
	}
}
