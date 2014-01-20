/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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

    public DoubleFormatter(boolean min, int digits, double value) {
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
				MathContext context = new MathContext(digits, min ? RoundingMode.FLOOR : RoundingMode.CEILING);
				BigDecimal bigDecimalValue = BigDecimal.valueOf(value).round(context);
				_value = bigDecimalValue.doubleValue();
				_text = bigDecimalValue.toEngineeringString();
			}
			catch (NumberFormatException e) {
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
        int index = _text.indexOf("-");
        if (index > 0) {
            if ('E' != _text.charAt(index - 1)) {
                ij.IJ.log("Funny text " + _text);
            }
        }
        return _text;
    }
}
