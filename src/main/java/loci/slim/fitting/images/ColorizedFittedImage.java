/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
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

package loci.slim.fitting.images;

import java.awt.Color;
import java.awt.image.IndexColorModel;

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import loci.slim.IGrayScalePixelValue;

/**
 * This class maintains a colorized grayscale fitted image, similar to those in
 * SPCImage software.
 * 
 * @author Aivar Grislis
 */
public class ColorizedFittedImage implements IFittedImageSlice {
    private static final float SATURATION = 0.75f;
    IGrayScalePixelValue _grayScalePixelValue;
    double[][] _values;
    int _width;
    int _height;
    int _channel;
    IndexColorModel _indexColorModel;
    ColorProcessor _colorProcessor;
    double _min;
    double _max;
    
    public ColorizedFittedImage(IGrayScalePixelValue grayScalePixelValue,
            double[][] values)
    {
        _grayScalePixelValue = grayScalePixelValue;
        _values = values;
    }

    @Override
    public void init(int width, int height, int channel,
            IndexColorModel indexColorModel) {
        _width = width;
        _height = height;
        _channel = channel;
        _indexColorModel = indexColorModel;
        _colorProcessor = new ColorProcessor(width, height);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int grayValue = _grayScalePixelValue.getGrayValue(channel, x, y);
                Color gray = getGrayColor(grayValue);
                _colorProcessor.setColor(gray);
                _colorProcessor.drawPixel(x, y);   
            }
        }
    }

    @Override
    public void setColorModel(IndexColorModel indexColorModel) {
        _indexColorModel = indexColorModel;
    }

    @Override
    public ImageProcessor getImageProcessor() {
        return _colorProcessor;
    }

    @Override
    public void setMinAndMax(double min, double max) {
        if (min != _min || max != _max) {
            _min = min;
            _max = max;

            // redraw the entire image
            for (int y = 0; y < _height; ++y) {
                for (int x = 0; x < _width; ++x) {
                    Color color = null;
                    int grayValue = _grayScalePixelValue.getGrayValue(_channel, x, y);
                    double value = _values[x][y];
                    if (min <= value && value <= max) {
                        color = getColorizedGrayColor(grayValue, value);
                    }
                    else {
                        color = getGrayColor(grayValue);
                    }
                    _colorProcessor.setColor(color);
                    _colorProcessor.drawPixel(x, y);
                }
            }
        }
    }

    @Override
    public void draw(int x, int y, double value) {
        int grayValue = _grayScalePixelValue.getGrayValue(_channel, x, y);
        Color colorizedGray = getColorizedGrayColor(grayValue, value);
        _colorProcessor.setColor(colorizedGray);
        _colorProcessor.drawPixel(x, y);
    }
    
    private Color getGrayColor(int grayValue) {
        return new Color(grayValue, grayValue, grayValue);
    }

    /*
     * This method does the actual colorization.  Given a gray level and
     * a value to display, returns the colorized Color.
     * 
     */
    private Color getColorizedGrayColor(int grayValue, double value) {
        if (_max == _min
                || Double.isNaN(value)
                || value < _min || value > _max) {
            return getGrayColor(grayValue);
        }
        
        // convert value to 0.0..1.0
        value = (value - _min) / (_max - _min);
        
        // convert 0.0..1.0 to 1..254 (colors 0 and 255 have a special use) //TODO ARG problem here; the only value that results in index 254 is _max.
        int index = 1 + (int) (value * 253);

        // get color
        Color color = new Color(_indexColorModel.getRGB(index));

        // decompose color
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);

        // synthesize new color, using grayValue brightness
        float hue        = hsv[0];
        float saturation = SATURATION;
        float brightness = (float) grayValue / 255.0f; // 0..255 -> 0.0..1.0
        return Color.getHSBColor(hue, saturation, brightness);
    }
}
