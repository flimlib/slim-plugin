/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    double _value;
    String _text;

    public DoubleFormatter(boolean min, int digits, double value) {
        try {
            MathContext context = new MathContext(digits, min ? RoundingMode.FLOOR : RoundingMode.CEILING);
            BigDecimal bigDecimalValue = BigDecimal.valueOf(value).round(context);
            _value = bigDecimalValue.doubleValue();
            _text = bigDecimalValue.toEngineeringString();
        }
        catch (NumberFormatException e) {
            System.out.println("NumberFormatException " + e.getMessage());
            _value = 0.0;
            _text = "0.0";
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
