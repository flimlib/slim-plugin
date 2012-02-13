/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;
/**
 *
 * @author Aivar Grislis
 */
public interface IFittedImage {

    /**
     * Gets width of image.
     * 
     * @return 
     */
    public int getWidth();
    
    /**
     * Gets height of image.
     * 
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
    public int getParameters();

    /**
     * Gets dimensions.
     *
     * @return
     */
    public int[] getDimension();

    /**
     * Gets pixel values at location.
     *
     * @param location
     * @return
     */
    public double[] getPixel(int[] location);

    /**
     * Sets pixel value at location.
     *
     * @param location
     * @param value
     */
    public void setPixel(int[] location, double[] value);   

    /**
     * Gets associated image.
     * 
     * @return 
     */
    public Image<DoubleType> getImage();   
}
