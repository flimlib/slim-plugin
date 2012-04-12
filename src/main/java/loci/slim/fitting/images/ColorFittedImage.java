/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import ij.process.ImageProcessor;

/**
 * This class maintains a colorized grayscale fitted image, similar to those in
 * SPCImage software.
 * 
 * @author Aivar Grislis
 */
public class ColorFittedImage implements IColorizedFittedImage {

    public void init(int width, int height, IndexColorModel indexColorModel) {
    }
    
    public void setColorModel(IndexColorModel indexColorModel) {
        
    }
    
    public ImageProcessor getImageProcessor() {
        return null;
    }
    
    public void setMinAndMax(double min, double max) {
    }
    
    public void draw(int x, int y, double value) {
        
    }
}
