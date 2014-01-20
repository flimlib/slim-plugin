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
