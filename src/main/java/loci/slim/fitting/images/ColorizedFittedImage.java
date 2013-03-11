//
// ColorizedFittedImage.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
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
 * @author Aivar Grislis grislis at wisc dot edu
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
