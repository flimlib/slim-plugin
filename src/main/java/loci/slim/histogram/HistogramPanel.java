//
// HistogramPanel.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim.histogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

/**
 * This is a panel that represents a histogram.  Scale is logarithmic.  Cursors
 * can be drawn and manipulated, representing the range of the LUT inside the
 * bounds of the view.  Dragging the cursor off the edge stretches those bounds.
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class HistogramPanel extends JPanel {
	private static final double LOG_ONE_FACTOR = Math.log(3) / Math.log(2);
	private static final int SINGLE_PIXEL = 1;
    private static final int FUDGE_FACTOR = 4;
	private static final String QUARTILE_1 = "Q\u2081 ";
	private static final String QUARTILE_2 = "Q\u2082 (median)";
	private static final String QUARTILE_3 = "Q\u2083 ";
	private static final Color DASHED_LINE_COLOR = new Color(10, 10, 10);
    private IHistogramPanelListener _listener;
    private final Object _synchObject = new Object();
    private int _width;
    private int _height;
    private int _inset;
    private int[] _bins;
	private double[] _binValues;
    private int _maxBinCount;
	private boolean _log;
    private Integer _minCursor;
    private Integer _maxCursor;
    private boolean _draggingMinCursor;
    private boolean _draggingMaxCursor;
	private double[] _quartiles;
	private int[] _quartileIndices;
    
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
		
		_log = false;
        
        _minCursor = _maxCursor = null;
        
        _draggingMinCursor = _draggingMaxCursor = false;
		
		ToolTipManager.sharedInstance().registerComponent(this);
        
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
                int min = 0;
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

	@Override
	public String getToolTipText(MouseEvent e) {
		String suffix = null;
		Double value = null;
		int bin = e.getX() - _inset - 1;
		if (0 <= bin && bin < _bins.length) {
			StringBuilder sb = new StringBuilder();
			if (null != _quartileIndices && null != _quartiles) {
				if (_quartileIndices[0] == bin) {
					suffix = QUARTILE_1;
					value = _quartiles[0];
				}
				else if (_quartileIndices[1] == bin) {
					suffix = QUARTILE_2;
					value = _quartiles[1];
				}
				else if (_quartileIndices[2] == bin) {
					suffix = QUARTILE_3;
					value = _quartiles[2];
				}
			}
			if (null == value) {
				value = _binValues[bin];
			}
			sb.append(round(value));
			sb.append(' ');
			sb.append(_bins[bin]);
			if (null != suffix) {
				sb.append(' ');
				sb.append(suffix);
			}
			return sb.toString();
		}
		return null;
	}
	
	private double round(double value) {
		double result = value * 1000;
		result = Math.round(result);
		return result / 1000;
	}
	
	public boolean getLog() {
		return _log;
	}
	
	public void setLogarithmicDisplay(boolean log) {
		_log = log;
		repaint();
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
    public void setBinValues(int[] bins) {
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
	
	public void setStatistics(HistogramStatistics statistics) {
		synchronized (_synchObject) {
			_bins = statistics.getBins();
            _maxBinCount = Integer.MIN_VALUE;
            for (int i = 0; i < _bins.length; ++i) {
                if (_bins[i] > _maxBinCount) {
                    _maxBinCount = _bins[i];
                }
            }
			_binValues = statistics.getBinValues();
			_quartileIndices = statistics.getQuartileIndices();
			_quartiles = statistics.getQuartiles();
		}
		repaint();
	}
	
    /**
     * Changes cursors and redraws.  Note that when they are both null no
     * cursor is displayed.  Otherwise if one is null only the other value
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

	/**
	 * Resets the cursors.
	 */
	public void resetCursors() {
        _minCursor = _inset - 1;
        _maxCursor = _inset + _width;
		repaint();
	}

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (null != _bins) {
            synchronized (_synchObject) {

				// allow resize
				Dimension size = getSize();
				_height = size.height;
				
				// calculate a nominal height for log of one; actually log one is zero
				int logOneHeight = 0;
				if (_log) {
					// this is a hack just to get some proportionality
					double logTwoHeight = (_height * Math.log(2)) / (Math.log(_maxBinCount) + 1);
					logOneHeight = (int)(LOG_ONE_FACTOR * logTwoHeight);
				}
				
                int height;
                for (int i = 0; i < _width; ++i) {
                    if (0 == _bins[i]) {
                        height = 0;
                    }
					else if (_log) {
						if (1 == _bins[i]) {
							height = logOneHeight;
						}
						else {
							height = (int)((_height - logOneHeight) * Math.log(_bins[i]) / Math.log(_maxBinCount)) + logOneHeight;
						}
					}
					else {
						// make sure values of one show at least a single pixel
						height = (int)((_height - SINGLE_PIXEL) * _bins[i] / _maxBinCount) + SINGLE_PIXEL;
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
				
				if (null != _quartileIndices) {
					for (int quartileIndex : _quartileIndices) {
						drawDashedLine(g, _inset + quartileIndex, 0, _height);
					}
				}
            }
        }
    }
	
	private void drawDashedLine(Graphics g, int x, int y0, int y1) {
		g.setXORMode(DASHED_LINE_COLOR);
		for (int y = y0; y < y1; y += 7) {
			int yEnding = Math.min(y + 2, y1);
			g.drawLine(x, y, x, yEnding);
		}
	}
}
