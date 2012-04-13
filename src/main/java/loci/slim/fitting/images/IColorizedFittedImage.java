/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import ij.process.ImageProcessor;

/**
 *
 * @author aivar
 */
public interface IColorizedFittedImage {
    
    public void init(int width, int height, int channel,
            IndexColorModel indexColorModel);
    
    public void setColorModel(IndexColorModel indexColorModel);
    
    public ImageProcessor getImageProcessor();
    
    public void setMinAndMax(double min, double max);
    
    public void draw(int x, int y, double value);
}
