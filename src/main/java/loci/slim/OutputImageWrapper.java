/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim;

import imagej.slim.fitting.IFittedImage;

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
    private LocalizableByDimCursor<DoubleType> _cursor;
    
    public OutputImageWrapper(Image<DoubleType> image) {
        _image = image;
        int[] dimensions = image.getDimensions();
        if (dimensions.length >= 4) {
            _width = dimensions[0];
            _height = dimensions[1];
            _channels = dimensions[2];
            _parameters = dimensions[3];
            _cursor = _image.createLocalizableByDimCursor();
        }
    }
    
    public OutputImageWrapper(int width, int height, int channels, int parameters) {
        Image<DoubleType> _image = null;
        _width = width;
        _height = height;
        _channels = channels;
        _parameters = parameters;

        // avoid a problem with ImgLib:
        if (1 == width) ++width;
        if (1 == height) ++height;
        if (1 == channels) ++channels;
        
        int[] dimensions = { width, height, channels, parameters };
        _image = new ImageFactory<DoubleType>
                (new DoubleType(),
                 new PlanarContainerFactory()).createImage(dimensions, "Fitted");
        
        // initialize image
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
        int[] dimension = null;
        if (_channels > 1) {
            dimension = new int[] { _width, _height, _channels, _parameters };
        }
        else {
            dimension = new int[] { _width, _height, _parameters };
        }
        return dimension;
    }

    public double[] getPixel(int[] location) {
        double[] parameters = new double[_parameters];
        int parameterIndex = location.length - 1;
        for (int i = 0; i < _parameters; ++i) {
            location[parameterIndex] = i;
            _cursor.moveTo(location);
            parameters[i] = _cursor.getType().getRealFloat();
        }
        return parameters;
    }

    public void setPixel(int[] location, double[] value) {
        int parameterIndex = location.length - 1;
        for (int i = 0; i < _parameters; ++i) {
            location[parameterIndex] = i;
            _cursor.moveTo(location);
            _cursor.getType().set(value[i]);
        }
    }

    /**
     * Gets fitted pixel value.
     * 
     * @param x
     * @param y
     * @param channel
     * @return
     */
    @Override
    public double[] getPixel(int x, int y, int channel) {
        double[] parameters = new double[_parameters];
        int[] location = new int[] { x, y, channel, 0 };
        for (int i = 0; i < _parameters; ++i) {
            location[3] = i;
            _cursor.moveTo(location);
            parameters[i] = _cursor.getType().getRealFloat();
        }
        return parameters;
    }

    /**
     * Puts fitted pixel value.
     * 
     * @param x
     * @param y
     * @param channel
     * @param pixel 
     */
    @Override
    public void setPixel(int x, int y, int channel, double[] parameters) {
        int[] location = new int[] { x, y, channel, 0 };
        for (int i = 0; i < _parameters; ++i) {
            location[3] = i;
            _cursor.moveTo(location);
            _cursor.getType().set(parameters[i]);
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
