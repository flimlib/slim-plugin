/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci;

/**
 *
 * @author aivar
 */
public class ChunkyPixel {
    final int m_x;
    final int m_y;
    final int m_width;
    final int m_height;

    public ChunkyPixel(int x, int y, int width, int height) {
        m_x = x;
        m_y = y;
        m_width = width;
        m_height = height;
    }

    public int getX() {
        return m_x;
    }

    public int getY() {
        return m_y;
    }

    public int getWidth() {
        return m_width;
    }

    public int getHeight() {
        return m_height;
    }
}
