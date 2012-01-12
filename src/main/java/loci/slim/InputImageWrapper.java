/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim;

import imagej.slim.fitting.IInputImage;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * This class wraps an image that is being used as input for a cumulative fit.
 * 
 * @author Aivar Grislis
 */
public class InputImageWrapper implements IInputImage {
    private Image<DoubleType> _image;
    private int _width;
    private int _height;
    private int _channels;
    private int _parameters;
    private LocalizableByDimCursor<DoubleType> _cursor;
    
    public InputImageWrapper(Image<DoubleType> image) {
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
     * Gets number of parameters of image.
     * 
     * @return 
     */
    @Override
    public int getParameters() {
        return _parameters;
    }
    
    /**
     * Gets input pixel value.
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

    @Override
    public Image<DoubleType> getImage() {
        return _image;
    }
}
