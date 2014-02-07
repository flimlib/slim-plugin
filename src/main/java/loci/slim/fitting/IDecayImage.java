/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

import loci.slim.preprocess.IProcessor;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;

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
	public ImgPlus<T> getImage();
}
