/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;
/**
 *
 * @author Aivar Grislis
 */
public interface IOutputImage {

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
     * Puts output pixel value.
     * 
     * @param x
     * @param y
     * @param channel
     * @param pixel 
     */
    public void putPixel(int x, int y, int channel, double[] pixel);
    

    /**
     * Gets associated image.
     * 
     * @return 
     */
    public Image<DoubleType> getImage();   
}
