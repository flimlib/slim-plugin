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

package loci.slim;

import loci.slim.fitting.IFittedImage;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;


/**
 * This class wraps an image that is being used as output from a fit.
 * 
 * @author Aivar Grislis
 */
public class OutputImageWrapper implements IFittedImage {
    private ImgPlus<DoubleType> _image;
    private int _width;
    private int _height;
    private int _channels;
    private int _parameters;
    private int _parameterIndex;
    private RandomAccess<DoubleType> _cursor;
    private int[] _location;

    /**
     * Creates a wrapper for an output image and initial image.
     * 
     * @param width
     * @param height
     * @param channels
     * @param parameters 
     */
    public OutputImageWrapper(String title, String fitTitle, int width, int height, int channels, int parameters) {  
        _width = width;
        _height = height;
        _channels = channels;
        _parameters = parameters;
        
        long[] dimensions = new long[] { width, height, channels, parameters };
        _parameterIndex = 3;
        _location = new int[dimensions.length];

        _image = new ImgPlus<DoubleType>(PlanarImgs.doubles(dimensions));
        _image.setName(title + " Fitted " + fitTitle);
        
        // fill image with NaNs
        Cursor<DoubleType> cursor = _image.cursor();
        while (cursor.hasNext()) {
            cursor.fwd();
            cursor.get().set(Double.NaN);
        }
        
        _cursor = _image.randomAccess();
    }

    /**
     * Gets width of image.
     * 
     * @return 
     */
    @Override
    public int getWidth() {
        return _width;
    }
    
    /**
     * Gets height of image.
     * 
     * @return 
     */
    @Override
    public int getHeight() {
        return _height;
    }
    
    /**
     * Gets number of channels of image.
     * 
     * @return 
     */
    @Override
    public int getChannels() {
        return _channels;
    }

    /**
     * Gets number of parameters of image.
     * 
     * @return 
     */
    @Override
    public int getParameters() {
        return _parameters;
    }

    @Override
    public int[] getDimension() {
        int[] dimension = new int[] { _width, _height, _channels, _parameters };
        return dimension;
    }

    @Override
    public double[] getPixel(int[] location) {
        for (int i = 0; i < location.length; ++i) {
            _location[i] = location[i];
        }
        double[] parameters = new double[_parameters];
        for (int i = 0; i < _parameters; ++i) {
            _location[_parameterIndex] = i;
            _cursor.setPosition(_location);
            parameters[i] = _cursor.get().getRealFloat();
        }
        return parameters;
    }

    @Override
    public void setPixel(int[] location, double[] value) {
        for (int i = 0; i < location.length; ++i) {
            _location[i] = location[i];
        }
        for (int i = 0; i < _parameters; ++i) {
            _location[_parameterIndex] = i;
            _cursor.setPosition(_location);
			// a pixel with an error fitting will have null value
            _cursor.get().set(null == value ? Double.NaN : value[i]);
        }
    }

    /**
     * Gets associated image.
     * 
     * @return 
     */
    @Override
    public ImgPlus<DoubleType> getImage() {
        return _image;
    }
}
