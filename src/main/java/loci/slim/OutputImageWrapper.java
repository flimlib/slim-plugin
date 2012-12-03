//
// OutputImageWrapper.java
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

package loci.slim;

import loci.slim.fitting.IFittedImage;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * This class wraps an image that is being used as output from a fit.
 * 
 * @author Aivar Grislis
 */
public class OutputImageWrapper implements IFittedImage {
    private Image<DoubleType> _image;
    private int _width;
    private int _height;
    private int _channels;
    private int _parameters;
    private int _parameterIndex;
    private LocalizableByDimCursor<DoubleType> _cursor;
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
        
        int[] dimensions = new int[] { width, height, channels, parameters };
        _parameterIndex = 3;
        _location = new int[dimensions.length];

        _image = new ImageFactory<DoubleType>
                (new DoubleType(),
                 new PlanarContainerFactory()).createImage(dimensions, title + " Fitted " + fitTitle);
        
        // fill image with NaNs
        Cursor<DoubleType> cursor = _image.createCursor();
        while (cursor.hasNext()) {
            cursor.fwd();
            cursor.getType().set(Double.NaN);
        }
        
        _cursor = _image.createLocalizableByDimCursor();
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
            _cursor.moveTo(_location);
            parameters[i] = _cursor.getType().getRealFloat();
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
            _cursor.moveTo(_location);
			// a pixel with an error fitting will have null value
            _cursor.getType().set(null == value ? Double.NaN : value[i]);
        }
    }

    /**
     * Gets associated image.
     * 
     * @return 
     */
    @Override
    public Image<DoubleType> getImage() {
        return _image;
    }
}
