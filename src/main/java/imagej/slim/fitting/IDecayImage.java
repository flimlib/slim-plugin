/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 *
 * @author Aivar Grislis
 */
public interface IDecayImage<T extends RealType<T>> {
    
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
     * Gets whether or not to fit this pixel.
     * 
     * @param x
     * @param y
     * @param channel
     * @return 
     */
    public boolean fitThisPixel(int[] location);
   
    /**
     * Gets input pixel value.
     * 
     * @param x
     * @param y
     * @param channel
     * @return 
     */
    public double[] getPixel(int[] location);

    /**
     * Gets associated image.
     * 
     * @return 
     */
    public Image<DoubleType> getImage();
}
