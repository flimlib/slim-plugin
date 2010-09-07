/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.colorizer;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Displays a color bar (typically under the histogram) with the
 * current colorization scheme.
 *
 * @author aivar
 */
public class ColorBar extends JPanel implements IColorizeRangeListener {
    final Object m_synchObject = new Object();
    int m_width;
    int m_height;
    IColorize m_colorize;
    double m_start;
    double m_stop;
    double m_max;

    /**
     * Constructor
     *
     * @param width
     * @param height
     */
    public ColorBar(int width, int height, IColorize colorize) {
        super();
        
        m_width = width;
        m_height = height;
        m_colorize = colorize;
        
        setPreferredSize(new Dimension(width, height));

        m_start = m_stop = m_max = 0.0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (m_synchObject) {
            for (int i = 0; i < m_width; ++i) {
                g.setColor(m_colorize.colorize(m_start, m_stop, pixelToValue(i)));
                g.drawLine(i, 0, i, m_height-1);

            }
        }
    }

    /**
     * Called when any of these settings change.
     *
     * @param auto (ignored)
     * @param start
     * @param stop
     * @param max
     */
    public void setRange(boolean auto, double start, double stop, double max) {
        boolean changed = false;
        synchronized (m_synchObject) {
            if (start != m_start) {
                m_start = start;
                changed = true;
            }
            if (stop != m_stop) {
                m_stop = stop;
                changed = true;
            }
            if (max != m_max) {
                m_max = max;
                changed = true;
            }
        }
        if (changed) {
            repaint();
        }
    }

    private double pixelToValue(int x) {
        return (m_max * x) / (m_width - 1);
    }
}
