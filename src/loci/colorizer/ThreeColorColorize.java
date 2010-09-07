/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.colorizer;

import java.awt.Color;

/**
 *
 * @author aivar
 */
public class ThreeColorColorize implements IColorize {
    Color m_color1;
    Color m_color2;
    Color m_color3;

    public ThreeColorColorize(Color color1, Color color2, Color color3) {
        m_color1 = color1;
        m_color2 = color2;
        m_color3 = color3;
    }

    public Color colorize(double start, double stop, double value) {
        Color returnColor = Color.BLACK;
        if (value > 0.0) {
            if (value >= start && value <= stop) {
                double range = stop - start;
                value -= start;
                if (value < (range / 2.0)) {
                    returnColor = interpolateColor(m_color1, m_color2, 2.0 * value / range);
                }
                else {
                    returnColor = interpolateColor(m_color2, m_color3, 2.0 * (value - (range / 2.0)) / range);
                }
            }

        }
        return returnColor;
    }

    private Color interpolateColor(Color start, Color end, double blend) {
        int startRed   = start.getRed();
        int startGreen = start.getGreen();
        int startBlue  = start.getBlue();
        int endRed   = end.getRed();
        int endGreen = end.getGreen();
        int endBlue  = end.getBlue();
        int red   = interpolateColorComponent(startRed, endRed, blend);
        int green = interpolateColorComponent(startGreen, endGreen, blend);
        int blue  = interpolateColorComponent(startBlue, endBlue, blend);
        Color returnColor = Color.BLACK;
        try {
            returnColor = new Color(red, green, blue);
        }
        catch (Exception e) {
            System.out.println("Exception " + e + " " + red + " " + green + " " + blue);
        }
        return returnColor;
    }

    private int interpolateColorComponent(int start, int end, double blend) {
        return (int)(blend * (end - start) + start);
    }

}
