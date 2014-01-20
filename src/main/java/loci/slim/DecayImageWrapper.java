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

import loci.slim.fitting.IDecayImage;
import loci.slim.preprocess.IProcessor;
import net.imglib2.RandomAccess;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * This class wraps an image that has a decay curve for each pixel.
 * 
 * @author Aivar Grislis
 */
public class DecayImageWrapper<T extends RealType<T>> implements IDecayImage<T> {
    private ImgPlus<T> _image;
    private int _width;
    private int _height;
    private int _channels;
    private int _bins;
    private int _binIndex;
    private int _increment;
    private RandomAccess<T> _cursor;
    
    public DecayImageWrapper(ImgPlus<T> image, int width, int height,
            int channels, int bins, int binIndex, int increment) {
        _image    = image;
        _width    = width;
        _height   = height;
        _channels = channels;
        _bins     = bins;
        _binIndex = binIndex;
        _increment = increment;

        _cursor = image.randomAccess();
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
     * Gets number of bins in decay curve of image.
     * 
     * @return 
     */
    @Override
    public int getBins() {
        return _bins;
    }
    
    /**
     * Specifies a source IProcessor to be chained to this one.
     * 
     * @param processor 
     */
    @Override
    public void chain(IProcessor processor) {
        throw new UnsupportedOperationException("Can't chain to DecayImageWrapper");
    }
    
    /**
     * Gets input pixel decay curve.
     * 
     * @param location
     * @return 
     */
    @Override
    public double[] getPixel(int[] location) {
        double[] decay = new double[_bins];
        
        // add bins to location
        int[] innerLocation = new int[location.length + 1];
        for (int i = 0; i < _binIndex; ++i) {
            innerLocation[i] = location[i];
        }
        for (int i = _binIndex; i < location.length; ++i) {
            innerLocation[i + 1] = location[i];
        }

        for (int i = 0; i < _bins; ++i) {
            innerLocation[_binIndex] = i;
            _cursor.setPosition(innerLocation);
            decay[i] = _cursor.get().getRealFloat() / _increment;
        }
        return decay;
    }

    /**
     * Gets underlying image.
     */
    @Override
    public ImgPlus<T> getImage() {
        return _image;
    }
}
