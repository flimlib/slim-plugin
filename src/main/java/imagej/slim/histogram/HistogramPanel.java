/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/**
 *
 * @author Aivar Grislis
 */
public class HistogramPanel extends JPanel {
    static final int ONE_HEIGHT = 20;
    static final int FUDGE_FACTOR = 3;
    private IHistogramPanelListener _listener;
    private final Object _synchObject = new Object();
    private int _width;
    private int _height;
    private int _inset;
    private int[] _bins;
    private int _max;
    private Integer _minCursor;
    private Integer _maxCursor;
    private boolean _draggingMinCursor;
    private boolean _draggingMaxCursor;
    
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
        
        _minCursor = _maxCursor = null;
        
        _draggingMinCursor = _draggingMaxCursor = false;
        
        setPreferredSize(new Dimension(width, height));
        addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                synchronized (_synchObject) {
                    if (null != _minCursor && null != _maxCursor) {
                        if (Math.abs(_minCursor - e.getX()) < FUDGE_FACTOR) {
                            _draggingMinCursor = true;
                        }
                        else if (Math.abs(_maxCursor - e.getX()) < FUDGE_FACTOR) {
                            _draggingMaxCursor = true;
                        }
                    }
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                boolean changed = false;
                synchronized (_synchObject) {
                    if (_draggingMinCursor) {
                        _minCursor = e.getX();
                        if (_minCursor < _inset) {
                            _minCursor = _inset - 1;
                        }
                        _draggingMinCursor = false;
                        changed = true;
                    }
                    else if (_draggingMaxCursor) { 
                        _maxCursor = e.getX();
                        if (_maxCursor > _width - _inset) {
                            _maxCursor = _width - _inset;
                        }
                        _draggingMaxCursor = false;
                        changed = true;
                    }                    
                }
                if (changed) {
                    repaint();
                    if (null != _listener) {
                        // convert to 0..(_width - 1)
                        int min = _minCursor - _inset + 1;
                        int max = _maxCursor - _inset - 1;
                        _listener.setMinMax(min, max);
                    }
                }
            }
            
            public void mouseEntered(MouseEvent e) { }
            
            public void mouseExited(MouseEvent e) { }
            
            public void mouseClicked(MouseEvent e) { }
            
        });
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) { }
            
            public void mouseDragged(MouseEvent e) {
                synchronized (_synchObject) {
                    if (_draggingMinCursor) {
                        if (_maxCursor > e.getX()) {
                            _minCursor = e.getX();
                        }
                        repaint();
                    }
                    else if (_draggingMaxCursor) {
                        if (_minCursor < e.getX()) {
                            _maxCursor = e.getX();
                        }
                        repaint();
                    }    
                }
            }
        });
    }

    /**
     * Sets a listener for dragging minimum and maximum.
     *
     * @param listener
     */
    public void setListener(IHistogramPanelListener listener) {
        _listener = listener;
    }

    /**
     * Changes settings and redraws.
     * 
     * @param bins 
     */
    public void setBins(int[] bins) {
        System.out.println("SET BINS");
        synchronized (_synchObject) {
            _bins = bins;
            _max = Integer.MIN_VALUE;
            for (int i = 0; i < bins.length; ++i) {
                if (bins[i] > _max) {
                    _max = bins[i];
                }
            }
        }
        repaint();
    }

    /**
     * Changes cursors and redraws.
     * 
     * @param minCursor
     * @param maxCursor 
     */
    public void setCursors(Integer minCursor, Integer maxCursor) {
        synchronized (_synchObject) {
            _minCursor = minCursor;
            _maxCursor = maxCursor;
            if (null == _minCursor && null == _maxCursor) {
                _draggingMinCursor = _draggingMaxCursor = false;
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
                    // note that the log of 1 is zero; have to distinguish from 0
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
                
                if (null != _minCursor && null != _maxCursor) {
                    g.setXORMode(Color.MAGENTA);
                    g.drawLine(_minCursor, 0, _minCursor, _height - 1);
                    g.drawLine(_maxCursor, 0, _maxCursor, _height - 1);
                }
            }
        }
    }    
}
