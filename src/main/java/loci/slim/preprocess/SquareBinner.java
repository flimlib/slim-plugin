/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.slim.preprocess;

/**
 * This class bins the image.
 * 
 * @author Aivar Grislis
 */
public class SquareBinner implements IProcessor {
    private int _size;
    private int _width;
    private int _height;
    private IProcessor _processor;

    /**
     * Initializes the binner.  Must be called once after instantiation and
     * before use.
     * 
     * @param size
     * @param width
     * @param height 
     */
    public void init(int size, int width, int height) {
        _size   = size;
        _width  = width;
        _height = height;
    }
    
    /**
     * Specifies a source IProcessor to be chained to this one.
     * 
     * @param processor 
     */
    public void chain(IProcessor processor) {
        _processor = processor;
    }
    
    /**
     * Gets input pixel value.
     * 
     * @param location
     * @return pixel value
     */
    public double[] getPixel(int[] location) {
        double[] sum = _processor.getPixel(location);
		if (null != sum) {
			// keep a running sum; don't change source pixel
			sum = sum.clone();
			
			int x = location[0];
			int y = location[1];

			int startX = x - _size;
			if (startX < 0) {
				startX = 0;
			}
			int stopX  = x + _size;
			if (stopX >= _width) {
				stopX = _width - 1;
			}
			int startY = y - _size;
			if (startY < 0) {
				startY = 0;
			}
			int stopY  = y + _size;
			if (stopY >= _height) {
				stopY = _height - 1;
			}

			for (int j = startY; j <= stopY; ++j) {
				for (int i = startX; i <= stopX; ++i) {
					if (j != y || i != x) {
						location[0] = i;
						location[1] = j;
						double[] pixel = _processor.getPixel(location);

						if (null != pixel) {
							add(sum, pixel);
						}
					}
				}
			}
		}
        return sum;
    }

    /*
     * Adds together two decays.
     */
    private void add(double[] sum, double[] pixel) {
        for (int i = 0; i < sum.length; ++i) {
            sum[i] += pixel[i];
        }
    }
}
