/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci;

import java.lang.UnsupportedOperationException;
import java.util.Iterator;

/**
 *
 * @author aivar
 */
public class ChunkyPixelEffectIterator implements Iterator {
    int m_width;
    int m_height;
    int m_index;
    int m_x;
    int m_y;
    IChunkyPixelTable m_table;
    ChunkyPixel m_chunkyPixel;
    
    public ChunkyPixelEffectIterator(IChunkyPixelTable table, int width, int height) {
        m_table = table;
        m_width = width;
        m_height = height;

        // initialize
        m_index = 0;
        m_x = 0;
        m_y = 0;

        // get first chunky pixel
        m_chunkyPixel = getNextChunkyPixel();
    }

    public boolean hasNext() {
        return m_chunkyPixel != null;
    }

    public ChunkyPixel next() {
        ChunkyPixel chunkyPixel = m_chunkyPixel;
        m_chunkyPixel = getNextChunkyPixel();
        return chunkyPixel;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    ChunkyPixel getNextChunkyPixel() {
        // get the relative chunky pixel from the table
        ChunkyPixel relChunkyPixel = m_table.getChunkyPixel(m_index);

        if (m_x + relChunkyPixel.getX() >= m_width) {
            // start next row
            m_x = 0;
            m_y += m_table.getHeight();

            if (m_y + relChunkyPixel.getY() >= m_height) {
                // use next table entry, are we done?
                if (++m_index >= m_table.size()) {
                    return null;
                }

                // start from the top
                m_y = 0;

                // update relative chunky pixel
                relChunkyPixel = m_table.getChunkyPixel(m_index);
            }
        }
        
        // convert relative to absolute
        int x = m_x + relChunkyPixel.getX();
        int y = m_y + relChunkyPixel.getY();
        ChunkyPixel absChunkyPixel = new ChunkyPixel(x, y,
                Math.min(relChunkyPixel.getWidth(), m_width - x),
                Math.min(relChunkyPixel.getHeight(), m_height - y));

        // set up for next call
        m_x += m_table.getWidth();

        return absChunkyPixel;
    }
}
