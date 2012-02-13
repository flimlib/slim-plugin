/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting;

import loci.slim.process.IProcessor;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 *
 * @author Aivar Grislis
 */
public interface IDecayImage<T extends RealType<T>> extends IProcessor {
    
    /**
     * Gets width of image.
     * 
     * @return 
     */
    public int getWidth();
    
    /**
     * Gets height of image.
     * @return 
     */
    public int getHeight();
    
    /**
     * Gets number of channels of image.
     * 
     * @return 
     */
    public int getChannels();
    
    /**
     * Gets number of parameters of image.
     * 
     * @return 
     */
    public int getBins();
    
    /**
     * Specifies a source IProcessor to be chained to this one.
     * 
     * @param processor 
     */
    public void chain(IProcessor processor);
    
    /**
     * Gets input pixel value.
     * 
     * @param x
     * @param y
     * @param channel
     * @return null or pixel value
     */
    public double[] getPixel(int[] location);

    /**
     * Gets associated image.
     * 
     * @return 
     */
    public Image<DoubleType> getImage();
}
