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
public class DecayImageWrapper<T extends RealType<T>> implements IDecayImage {
    private Image<T> _image;
    private int _width;
    private int _height;
    private int _channels;
    private int _channelIndex;
    private int _bins;
    private int _binIndex;
    private LocalizableByDimCursor<T> _cursor;
    
    public DecayImageWrapper(Image<T> image, int width, int height,
            int channels, int channelIndex, int bins, int binIndex) {
        _image = image;
        _width = width;
        _height = height;
        _channels = channels;
        _channelIndex = channelIndex;
        _bins = bins;
        _binIndex = binIndex;

        _cursor = image.createLocalizableByDimCursor();
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
    
    @Override
    public boolean fitThisPixel(int x, int y, int channel) {
        return true; //TODO FOR NOW ONLY!!
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
        int[] location;
        
        if (_channels > 1) {
            location = new int[] { x, y, 0, 0 };
            location[_channelIndex] = channel;
        }
        else {
            location = new int[] { x, y, 0 };
        }

        for (int i = 0; i < _bins; ++i) {
            location[_binIndex] = i;
            _cursor.moveTo(location);
            decay[i] = _cursor.getType().getRealFloat();
        }
        return decay;
    }

    @Override
    public Image<T> getImage() {
        return _image;
    }
}
