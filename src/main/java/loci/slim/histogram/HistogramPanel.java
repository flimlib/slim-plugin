/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
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
 * @author Aivar Grislis
 */
public class HistogramPanel extends JPanel {
	public static final double DEFAULT_BANDWIDTH = 0.2;
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
	private boolean _smooth;
	private boolean _family = true;
	private boolean _style1 = false;
    private Integer _minCursor;
    private Integer _maxCursor;
    private boolean _draggingMinCursor;
    private boolean _draggingMaxCursor;
	private double[] _quartiles;
	private int[] _quartileIndices;
	private double[] _kernelDensityEstimate;
	private double[][] _kernelDensityEstimateFamily;
	private double _bandwidth = DEFAULT_BANDWIDTH;
	private double[] _bandwidthFamily = new double[] { 0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35 };
    
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
		_smooth = false;
        
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
	
	public boolean getLogarithmicDisplay() {
		return _log;
	}
	
	public void setLogarithmicDisplay(boolean log) {
		_log = log;
		repaint();
	}
	
	public boolean getSmoothing() {
		return _smooth;
	}
	
	public void setSmoothing(boolean smooth) {
		_smooth = smooth;
		repaint();
	}
	
	public double getBandwidth() {
		return _bandwidth;
	}
	
	public void setBandwidth(double bandwidth) {
		System.out.println("bandwidth is " + bandwidth);
		_family = false;
		_bandwidth = bandwidth;
		_kernelDensityEstimate = kernelDensityEstimation(_bins, _maxBinCount, _bandwidth);
		repaint();
	}
	
	public void setFamily1(boolean on) {
		if (on) {
			_family = true;
			_style1 = true;
			repaint();
		}
	}
	
	public void setFamily2(boolean on) {
		if (on) {
			_family = true;
			_style1 = false;
			repaint();
		}
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
		int count = 0;
		for (int i = 0; i < _bins.length; ++i) { //TODO if this stays, incorporate in earlier loop
			count += _bins[i];
		}
		//System.out.println("statistics.getStdDev is " + statistics.getStdDev());
		//System.out.println("Silverman's rule bandwidth is count " + count + " bandwidth " + silvermansRule(0.27, count));
		//System.out.println("estimate " + estimateBandwidth(statistics.getFences()[1] - statistics.getFences()[0], count));
		_bandwidth = 5 * estimateBandwidth(statistics.getFences()[1] - statistics.getFences()[0], count);
		_kernelDensityEstimate = kernelDensityEstimation(_bins, _maxBinCount, _bandwidth);
		
		_kernelDensityEstimateFamily = new double[_bins.length][_bandwidthFamily.length];
		for (int i = 0; i < _bandwidthFamily.length; ++i) {
			_kernelDensityEstimateFamily[i] = kernelDensityEstimation(_bins, _maxBinCount, _bandwidthFamily[i]);
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
				
				if (_smooth) {
					if (_family) {
						g.setColor(Color.DARK_GRAY);
						double max = -Double.MAX_VALUE;
						if (_style1) {
							for (double[] kernelDensityEstimate : _kernelDensityEstimateFamily) {
								for (int x = 0; x < kernelDensityEstimate.length; ++x) {
									if (kernelDensityEstimate[x] > max) {
										max = kernelDensityEstimate[x];
									}
								}
							}
						}
						for (double[] kernelDensityEstimate : _kernelDensityEstimateFamily) {
							if (!_style1) {
								max = -Double.MAX_VALUE;
								for (int x = 0; x < kernelDensityEstimate.length; ++x) {
									if (kernelDensityEstimate[x] > max) {
										max = kernelDensityEstimate[x];
									}
								}
							}
							int prevX = 0;
							int prevY = 0;
							boolean firstPixel = true;
							for (int x = 0; x < kernelDensityEstimate.length; ++x) {
								int y = _height - 1 - (int) ((kernelDensityEstimate[x] * _height) / max);
								if (firstPixel) {
									g.drawLine(_inset + x, y, _inset + x, y);
								}
								else {
									g.drawLine(_inset + prevX, prevY, _inset + x, y);
								}
								firstPixel = false;
								prevX = x;
								prevY = y;
							}
						}
					}
					else {
						if (null != _kernelDensityEstimate) {
							double max = -Double.MAX_VALUE;
							for (int x = 0; x < _kernelDensityEstimate.length; ++x) {
								if (_kernelDensityEstimate[x] > max) {
									max = _kernelDensityEstimate[x];
								}
							}
							for (int x = 0; x < _kernelDensityEstimate.length; ++x) {
								int y = _height - 1 - (int) ((_kernelDensityEstimate[x] * _height) / max);
								g.setColor(Color.WHITE);
								g.drawLine(_inset + x, 0, _inset + x, y);
								g.setColor(Color.DARK_GRAY);
								g.drawLine(_inset + x, y, _inset + x, _height);
							}
						}
					}

				}
				else {
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
	
	private double[] kernelDensityEstimation(int[] bins, int maxCount, double bandwidth) {
		double[] fitted = new double[bins.length];
		for (int i = 0; i < fitted.length; ++i) {
			fitted[i] = 0.0;
		}
		for (int center = 0; center < bins.length; ++center) {
			int count = bins[center];
			for (int x = 0; x < bins.length; ++x) {
				fitted[x] += gaussian(count, center, bandwidth, x);
			}
		}
		return fitted;
	}
	
	private double gaussian(double a, double b, double c, double x) {
		return a * Math.exp(-(x - b) * (x - b) / (2 * c * c));
	}
	
	private double silvermansRule(double standardDeviation, int n) {
		return (1.06 * standardDeviation / Math.pow(n, 0.20));
	}
	
	private double estimateBandwidth(double range, int n) {
		System.out.println("range is " + range + " count " + n);
		return range / Math.sqrt(n);
	}
}
