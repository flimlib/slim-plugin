/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim;

import imagej.slim.fitting.IDecayImage;

import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * This class wraps an image that has a decay curve for each pixel.
 * 
 * @author Aivar Grislis
 */
public class InputImageWrapper implements IDecayImage {
    private Image<DoubleType> _image;
    private int _width;
    private int _height;
    private int _channels;
    private int _bins;
    private LocalizableByDimCursor<DoubleType> _cursor;
    
    public InputImageWrapper(Image<DoubleType> image) {
        _image = image;
        int[] dimensions = image.getDimensions();
        if (dimensions.length >= 4) {
            _width = dimensions[0];
            _height = dimensions[1];
            _channels = dimensions[2];
            _bins = dimensions[3];
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
     * Gets number of bins in decay curve of image.
     * 
     * @return 
     */
    @Override
    public int getBins() {
        return _bins;
    }
    
    /**
     * Gets input pixel decay curve.
     * 
     * @param x
     * @param y
     * @param channel
     * @return 
     */
    @Override
    public double[] getPixel(int x, int y, int channel) {
        double[] decay = new double[_bins];
        int[] location = new int[] { x, y, channel, 0 };
        for (int i = 0; i < _bins; ++i) {
            location[3] = i;
            _cursor.moveTo(location);
            decay[i] = _cursor.getType().getRealFloat();
        }
        return decay;
    }

    @Override
    public Image<DoubleType> getImage() {
        return _image;
    }
}
