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
 * This is a panel that represents a histogram.  Scale is logarithmic.  Cursors
 * can be drawn and manipulated, representing the range of the LUT inside the
 * bounds of the view.  Dragging the cursor off the edge stretches those bounds.
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class HistogramPanel extends JPanel {
    private static final int ONE_HEIGHT = 20;
    private static final int FUDGE_FACTOR = 4;
    private IHistogramPanelListener _listener;
    private final Object _synchObject = new Object();
    private int _width;
    private int _height;
    private int _inset;
    private int[] _bins;
    private int _maxBinCount;
    private Integer _minCursor;
    private Integer _maxCursor;
    private boolean _draggingMinCursor;
    private boolean _draggingMaxCursor;
    
    /**
     * Constructor
     *
     * @param width
     * @param inset
     * @param height
     */
    public HistogramPanel(int width, int inset, int height) {
        super();
        
        _width = width;
        _inset = inset;
        _height = height;
        _bins = null;
        
        _minCursor = _maxCursor = null;
        
        _draggingMinCursor = _draggingMaxCursor = false;
        
        setPreferredSize(new Dimension(width + 2 * inset, height));
        addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (_synchObject) {
                    if (null != _minCursor && null != _maxCursor) {
                        // start dragging minimum or maximum cursor
                        if (Math.abs(_minCursor - e.getX()) < FUDGE_FACTOR) {
                            _draggingMinCursor = true;
                        }
                        else if (Math.abs(_maxCursor - e.getX()) < FUDGE_FACTOR) {
                            _draggingMaxCursor = true;
                        }
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                boolean changed = false;
                int min = 0; // makes the compiler happy
                int max = 0;
                synchronized (_synchObject) {
                    if (_draggingMinCursor) {
                        _minCursor = e.getX();
                        // snap to bounds of inset
                        if (_minCursor < _inset) {
                            _minCursor = _inset - 1;
                        }
                        // don't exceed maxCursor
                        if (_minCursor >= _maxCursor) {
                            _minCursor = _maxCursor - 1;
                        }
                        _draggingMinCursor = false;
                        changed = true;
                    }
                    else if (_draggingMaxCursor) { 
                        _maxCursor = e.getX();
                        // snap to bounds of inset
                        if (_maxCursor > _inset + _width) {
                            _maxCursor = _inset + _width;
                        }
                        // must be greater than minCursor
                        if (_maxCursor <= _minCursor) {
                            _maxCursor = _minCursor + 1;
                        }
                        _draggingMaxCursor = false;
                        changed = true;
                    }
                    if (changed) {
                        // convert to 0..width-1 range
                        min = _minCursor - _inset + 1;
                        max = _maxCursor - _inset - 1;
                    }
                }
                if (changed) {
                    if (null != _listener) {
                        _listener.setMinMaxLUTPixels(min, max);
                    }
                    repaint();
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) { }
            
            @Override
            public void mouseExited(MouseEvent e) {
                _listener.exited();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) { }
            
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) { }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                boolean changed = false;
                int min = 0; // makes the compiler happy
                int max = 0;
                synchronized (_synchObject) {
                    if (_draggingMinCursor) {
                        // don't drag past max cursor
                        if (_maxCursor > e.getX()) {
                            _minCursor = e.getX();
                        }
                        // don't drag out of this panel
                        if (_minCursor < 0) {
                            _minCursor = 0;
                        }
                        changed = true;
                    }
                    else if (_draggingMaxCursor) {
                        // don't drag past min cursor
                        if (_minCursor < e.getX()) {
                            _maxCursor = e.getX();
                        }
                        // don't drag out of this panel
                        if (_maxCursor >= 2 * _inset + _width) {
                            _maxCursor = 2 * _inset + _width - 1;
                        }
                        changed = true;
                    }
                    if (changed) {
                        // convert to 0..width-1 range
                        min = _minCursor - _inset + 1;
                        max = _maxCursor - _inset - 1;
                    }

                }
                if (changed) {
                    if (null != _listener) {
                        // report dragged cursor position
                        _listener.dragMinMaxPixels(min, max);
                    }
                    repaint();
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
     * Changes histogram counts and redraws.
     * 
     * @param bins 
     */
    public void setBins(int[] bins) {
        synchronized (_synchObject) {
            _bins = bins;
            _maxBinCount = Integer.MIN_VALUE;
            for (int i = 0; i < bins.length; ++i) {
                if (bins[i] > _maxBinCount) {
                    _maxBinCount = bins[i];
                }
            }
        }
        repaint();
    }

    /**
     * Changes cursors and redraws.  Note that when they are both null no
     * cursor is diaplayed.  Otherwise if one is null only the other value
     * changes.
     * 
     * @param minCursor null or minimum cursor position in pixels
     * @param maxCursor null or maximum cursor position in pixels
     */
    public void setCursors(Integer minCursor, Integer maxCursor) {
        synchronized (_synchObject) {
            if (null == minCursor) {
                if (null == maxCursor) {
                    // both null; turn off cursors
                    _draggingMinCursor = _draggingMaxCursor = false;
                    _minCursor = _maxCursor = null;
                }
                else {
                    // setting just max cursor
                    _maxCursor = maxCursor;
                }
            }
            else if (null == maxCursor) {
                // setting just min cursor
                _minCursor = minCursor;
            }
            else {
                // setting both cursors
                _minCursor = minCursor;
                _maxCursor = maxCursor;
                
                // the cursors actually bracket the specified pixels
                --_minCursor;
                ++_maxCursor;
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
                for (int i = 0; i < _width; ++i) {
                    if (0 == _bins[i]) {
                        height = 0;
                    }
                    // note that the log of 1 is zero; have to distinguish from 0
                    else if (1 == _bins[i]) {
                        height = ONE_HEIGHT;
                    }
                    else {
                        height = (int) ((_height - ONE_HEIGHT) * Math.log(_bins[i]) / Math.log(_maxBinCount)) + ONE_HEIGHT;
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
