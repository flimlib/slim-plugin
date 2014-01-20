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

package loci.slim;

/**
 * Class that handles drawing the image using progressively smaller "chunky"
 * (larger than single pixel) pixels.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ChunkyPixel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ChunkyPixel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class ChunkyPixel {
    final int _x;
    final int _y;
    final int _width;
    final int _height;
    int[] _inputLocation;
    int[] _outputLocation;

    /**
     * Constructor.
     * 
     * @param x
     * @param y
     * @param width
     * @param height 
     */
    public ChunkyPixel(int x, int y, int width, int height) {
        _x = x;
        _y = y;
        _width = width;
        _height = height;
        _inputLocation = null;
        _outputLocation = null;
    }

    /**
     * Gets the x location.
     * 
     * @return 
     */
    public int getX() {
        return _x;
    }

    /**
     * Gets the y location.
     * 
     * @return 
     */
    public int getY() {
        return _y;
    }

    /**
     * Gets the width to draw the pixel.
     * 
     * @return 
     */
    public int getWidth() {
        return _width;
    }

    /**
     * Gets the height to draw the pixel.
     * 
     * @return 
     */
    public int getHeight() {
        return _height;
    }

    /**
     * Sets the location of the pixel in the input image.
     * 
     * @param location 
     */
    public void setInputLocation(int[] location) {
        _inputLocation = location;
    }

    /**
     * Gets the location of the pixel in the input image.
     * 
     * @return 
     */
    public int[] getInputLocation() {
        return _inputLocation;
    }

    /**
     * Sets the location of the pixel in the output image.
     * 
     * @param location 
     */
    public void setOutputLocation(int[] location) {
        _outputLocation = location;
    }

    /**
     * Gets the location of the pixel in the output image.
     * 
     * @return 
     */
    public int[] getOutputLocation() {
        return _outputLocation;
    }
}
