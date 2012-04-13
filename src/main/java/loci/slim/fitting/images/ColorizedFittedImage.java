/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.images;

import java.awt.Color;
import java.awt.color.ColorSpace;
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
public class ColorizedFittedImage implements IColorizedFittedImage {
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
            double[][] values) {
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
                int grayValue = _grayScalePixelValue.getPixel(channel, x, y);
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
        if (min != _min || max != max) {
            _min = min;
            _max = max;

            // redraw the entire image
            for (int y = 0; y < _height; ++y) {
                for (int x = 0; x < _width; ++x) {
                    Color color = null;
                    int grayValue = _grayScalePixelValue.getPixel(_channel, x, y);
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
        int grayValue = _grayScalePixelValue.getPixel(_channel, x, y);
        Color colorizedGray = getColorizedGrayColor(grayValue, value);
        _colorProcessor.setColor(colorizedGray);
        _colorProcessor.drawPixel(x, y);
    }
    
    private Color getGrayColor(int grayValue) {
        return new Color(grayValue, grayValue, grayValue);
    }
    
    private Color getColorizedGrayColor(int grayValue, double value) {
        if (_max == _min) {
            return getGrayColor(grayValue);
        }
        
        // convert value to 0.0..1.0
        value = (value - _min) / (_max - _min);
        
        // convert 0.0..1.0 to 1..254 (colors 0 and 255 have a special use)
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
