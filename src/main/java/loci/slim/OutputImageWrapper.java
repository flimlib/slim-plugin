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
    private int _parameterIndex;
    private LocalizableByDimCursor<DoubleType> _cursor;
    private int[] _location;

    /**
     * Creates a wrapper for an output image (and the image itself).
     * 
     * @param width
     * @param height
     * @param channels
     * @param parameters 
     */
    public OutputImageWrapper(int width, int height, int channels, int parameters) {  
        _width = width;
        _height = height;
        _channels = channels;
        _parameters = parameters;
        
        int[] dimensions = new int[] { width, height, channels, parameters };
        _parameterIndex = 3;
        _location = new int[dimensions.length];

        _image = new ImageFactory<DoubleType>
                (new DoubleType(),
                 new PlanarContainerFactory()).createImage(dimensions, "Fitted");
        
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
            _cursor.getType().set(value[i]);
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
