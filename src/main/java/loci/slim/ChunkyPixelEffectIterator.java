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

import java.util.Iterator;

/**
 * Iterator that supplies a series of chunky pixels.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ChunkyPixelEffectIterator.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ChunkyPixelEffectIterator.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class ChunkyPixelEffectIterator implements Iterator {
    IChunkyPixelTable _table;
    int _width;
    int _height;
    int _index;
    int _x;
    int _y;
    ChunkyPixel _chunkyPixel;

    /**
     * Constructor, sets up the chunky pixel iterator with a table of chunky
     * pixels and the width and height of the image being processed.
     * 
     * @param table
     * @param width
     * @param height 
     */
    public ChunkyPixelEffectIterator(IChunkyPixelTable table, int width, int height) {
        _table = table;
        _width = width;
        _height = height;

        // initialize
        _index = 0;
        _x = 0;
        _y = 0;

        // get first chunky pixel
        _chunkyPixel = getNextChunkyPixel();
    }

    @Override
    public boolean hasNext() {
        return _chunkyPixel != null;
    }

    @Override
    public ChunkyPixel next() {
        ChunkyPixel chunkyPixel = _chunkyPixel;
        _chunkyPixel = getNextChunkyPixel();
        return chunkyPixel;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /*
     * Gets the next chunky pixel from the table.
     * 
     */
    ChunkyPixel getNextChunkyPixel() {
        // get the relative chunky pixel from the table
        ChunkyPixel relChunkyPixel = _table.getChunkyPixel(_index);

        if (_x + relChunkyPixel.getX() >= _width) {
            // start next row
            _x = 0;
            _y += _table.getHeight();

            if (_y + relChunkyPixel.getY() >= _height) {
                // use next table entry, are we done?
                if (++_index >= _table.size()) {
                    return null;
                }

                // start from the top
                _y = 0;

                // update relative chunky pixel
                relChunkyPixel = _table.getChunkyPixel(_index);
            }
        }
        
        // convert relative to absolute
        int x = _x + relChunkyPixel.getX();
        int y = _y + relChunkyPixel.getY();
        ChunkyPixel absChunkyPixel = new ChunkyPixel(x, y,
                Math.min(relChunkyPixel.getWidth(), _width - x),
                Math.min(relChunkyPixel.getHeight(), _height - y));

        // set up for next call
        _x += _table.getWidth();

        return absChunkyPixel;
    }
}
