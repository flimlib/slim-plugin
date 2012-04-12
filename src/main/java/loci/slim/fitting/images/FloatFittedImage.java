/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 *
 * @author Aivar Grislis
 */
public class FloatFittedImage implements IColorizedFittedImage {
    int _width;
    int _height;
    FloatProcessor _imageProcessor;
    
    public void init(int width, int height, IndexColorModel indexColorModel) {
        _width = width;
        _height = height;
        
        _imageProcessor = new FloatProcessor(width, height);
        _imageProcessor.setColorModel(indexColorModel);
        
        // fill the image with a value that will be out of LUT range and
        // paint black
        _imageProcessor.setValue(Float.NaN);
        _imageProcessor.fill();
    }
    
    public void setColorModel(IndexColorModel indexColorModel) {
        
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
