/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import ij.process.LUT;

/**
 *
 * @author aivar
 */
public class HistogramPanel extends JPanel {
    static final int ONE_HEIGHT = 20;
    private final Object _synchObject = new Object();
    private int _width;
    private int _height;
    private int _inset;
    private int[] _bins;
    private int _max;
    
    /**
     * Constructor
     *
     * @param width
     * @param height
     */
    public HistogramPanel(int width, int height, int inset) {
        super();
        
        _width = width;
        _height = height;
        _inset = inset;
        _bins = null;
        
        setPreferredSize(new Dimension(width, height));
    }

    /**
     * Changes settings and redraws.
     * 
     * @param bins 
     */
    public void setBins(int[] bins) {
        synchronized (_synchObject) {
            _bins = bins;
            for (int i = 0; i < bins.length; ++i) {
                if (bins[i] > _max) {
                    _max = bins[i];
                }
            }
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (null != _bins) {
            synchronized (_synchObject) {
                int height;
                for (int i = 0; i < _width - 2 * _inset; ++i) {
                    if (0 == _bins[i]) {
                        height = 0;
                    }
                    else if (1 == _bins[i]) {
                        height = ONE_HEIGHT;
                    }
                    else {
                        height = (int) ((_height - ONE_HEIGHT) * Math.log(_bins[i]) / Math.log(_max)) + ONE_HEIGHT;
                    }
                    if (height > _height) {
                        height = _height;
                    }
                    g.setColor(Color.WHITE);
                    g.drawLine(_inset + i, 0, _inset + i, _height - height);
                    g.setColor(Color.DARK_GRAY);
                    g.drawLine(_inset + i, _height - height, _inset + i, _height);
                }
                /*
                int x;
                g.setXORMode(Color.MAGENTA);
                x = valueToPixel(_start);
                g.drawLine(x, 0, x, _height - 1);
                x = valueToPixel(_stop);
                g.drawLine(x, 0, x, _height - 1);
                */
            }
        }
    }    
}
