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

import java.awt.image.IndexColorModel;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * 
 * @author Aivar Grislis
 */
public class FloatFittedImage implements IFittedImageSlice {
    int _width;
    int _height;
    int _channel;
    FloatProcessor _imageProcessor;
    
    public void init(int width, int height, int channel,
            IndexColorModel indexColorModel) {
        _width = width;
        _height = height;
        _channel = channel;
        
        _imageProcessor = new FloatProcessor(width, height);
        _imageProcessor.setColorModel(indexColorModel);
        
        // fill the image with a value that will be out of LUT range and
        // paint black
        _imageProcessor.setValue(Float.NaN);
        _imageProcessor.fill();
    }
    
    public void setColorModel(IndexColorModel indexColorModel) {
        _imageProcessor.setColorModel(indexColorModel);
    }
    
    public ImageProcessor getImageProcessor() {
        return _imageProcessor;
    }
    
    public void setMinAndMax(double min, double max) {
        _imageProcessor.setMinAndMax(min, max);
    }
    
    public void draw(int x, int y, double value) {
        _imageProcessor.setValue(value);
        _imageProcessor.drawPixel(x, y);
    }
}
