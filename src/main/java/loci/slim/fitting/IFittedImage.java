/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim.fitting;

import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;


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
    public ImgPlus<DoubleType> getImage();
}
