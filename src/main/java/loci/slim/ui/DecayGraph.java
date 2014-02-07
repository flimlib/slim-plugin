/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
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

package loci.slim.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import loci.curvefitter.ICurveFitData;
import loci.slim.ICursorListener;
import loci.slim.fitting.cursor.FittingCursor;
import loci.slim.fitting.cursor.IFittingCursorListener;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;


/**
 * This is the chart that shows the transient decay data, the fitted model,
 * and the residuals.
 *
 * It also has a user interface to set the start and stop of the fit.
 *
 * @author Aivar Grislis
 */
public class DecayGraph implements IDecayGraph, IStartStopProportionListener {
	// Unicode special characters
    private static final Character CHI    = '\u03c7',
                                   SQUARE = '\u00b2',
                                   TAU    = '\u03c4',
                                   SUB_1  = '\u2081',
                                   SUB_2  = '\u2082',
                                   SUB_3  = '\u2083',
								   SUB_R  = '\u1d63';
	static final String WIDTH_KEY = "width";
	static final String HEIGHT_KEY = "height";
    static final Dimension SIZE = new Dimension(500, 270);
    static final Dimension FRAME_SIZE = new Dimension(450, 450);
	static final Dimension MAX_SIZE = new Dimension(961, 724); // see getDataArea( ) below
	static final Dimension MIN_SIZE = new Dimension(313, 266);
    static final String PHOTON_AXIS_LABEL = "Photons";
    static final String TIME_AXIS_LABEL = "Time";
    static final String UNITS_LABEL = "nanoseconds";
    static final String RESIDUAL_AXIS_LABEL = "Residual";
	static final String CHI_SQUARE = "" + CHI + SQUARE + SUB_R;
	static final String PHOTON_COUNT = "Photons";
	static final String LOGARITHMIC = "Logarithmic";
    static final int DECAY_WEIGHT = 4;
    static final int RESIDUAL_WEIGHT = 1;
    static final int HORZ_TWEAK = 1;
	static final Color PROMPT_COLOR = Color.GRAY.brighter();
    static final Color DECAY_COLOR = Color.GRAY.darker();
    static final Color FITTED_COLOR = Color.RED;
    static final Color BACK_COLOR = Color.WHITE;
    static final Color TRANS_START_COLOR = Color.BLUE.darker();
    static final Color DATA_START_COLOR = Color.GREEN.darker();
    static final Color TRANS_STOP_COLOR = Color.RED.darker();
    static final Color BASE_COLOR = Color.GREEN.darker();
    static final Color RESIDUAL_COLOR = Color.GRAY;
    
    private static final Object _synchObject = new Object();

    private static DecayGraph _instance;
    private JFrame _frame;
    FittingCursor _fittingCursor;
    IFittingCursorListener _fittingCursorListener;

    private StartStopDraggingUI<JComponent> _startStopDraggingUI;
    private double _timeInc;
    private int _bins;
    private double _maxValue;
    private Double _transStart;
    private Double _dataStart;
    private Double _transStop;
    private boolean _logarithmic = true;

    XYPlot _decaySubPlot;
    XYSeriesCollection _decayDataset;
    XYSeriesCollection _residualDataset;

	JTextField _tau1TextField;
	JTextField _tau2TextField;
	JTextField _tau3TextField;
	JTextField _chiSqTextField;
    JTextField _photonTextField;
    JCheckBox _logCheckBox;
	
	ICursorListener _cursorListener;

    /**
     * Public constructor, may be used to create non-singletons.
     */
    public DecayGraph() {
        _frame = null;
    }

    /**
     * Gets the singleton instance.
     *
     * @return singleton instance
     */
    public static synchronized DecayGraph getInstance() {
        if (null == _instance) {
            _instance = new DecayGraph();
        }
        return _instance;
    }

    /**
     * Initialize the graph and returns the containing JFrame.
     *
     * @param bins
     * @param timeInc
     * @return frame
     */
    public JFrame init(final JFrame frame, final int bins, final double timeInc, ICursorListener cursorListener) {
        boolean create = false;
        if (null == _frame
                || !_frame.isVisible()
                || _bins != bins
                || _timeInc != timeInc)
        {
            // save incoming parameters
            _bins = bins;
            _timeInc = timeInc;
            _maxValue = timeInc * bins;
			_cursorListener = cursorListener;
            
            if (null != _frame) {
                // delete existing frame
                _frame.setVisible(false);
                _frame.dispose();
            }
            
             // create the combined chart
            JFreeChart chart = createCombinedChart(bins, timeInc);
            ChartPanel panel = new ChartPanel
                    (chart, true, true, true, false, true);
            panel.setDomainZoomable(false);
            panel.setRangeZoomable(false);
            panel.setPreferredSize(SIZE);

            // add start/stop vertical bar handling
            _dataStart = _transStop = null;
            JXLayer<JComponent> layer = new JXLayer<JComponent>(panel);
            _startStopDraggingUI =
                    new StartStopDraggingUI<JComponent>
                            (panel, _decaySubPlot, this, _maxValue);
            layer.setUI(_startStopDraggingUI);

            // create a frame for the chart
            _frame = new JFrame();
            Container container = _frame.getContentPane();
			container.setLayout(new BorderLayout());
			container.add(layer, BorderLayout.CENTER);
            
            JPanel miscPane = new JPanel();
            miscPane.setLayout(new FlowLayout());
			JLabel label1 = new JLabel(CHI_SQUARE);
			miscPane.add(label1);
			_chiSqTextField = new JTextField(7);
			_chiSqTextField.setEditable(false);
			miscPane.add(_chiSqTextField);
            JLabel label2 = new JLabel(PHOTON_COUNT);
            miscPane.add(label2);
            _photonTextField = new JTextField(7);
            _photonTextField.setEditable(false);
            miscPane.add(_photonTextField);
            _logCheckBox = new JCheckBox(LOGARITHMIC);
            _logCheckBox.setSelected(_logarithmic);
            _logCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    _logarithmic = _logCheckBox.isSelected();
                    NumberAxis photonAxis;
                    if (_logarithmic) {
                        photonAxis = new LogarithmicAxis(PHOTON_AXIS_LABEL);
                    }
                    else {
                        photonAxis = new NumberAxis(PHOTON_AXIS_LABEL);
                    }
                    _decaySubPlot.setRangeAxis(photonAxis);
                }
            });
            miscPane.add(_logCheckBox);
			container.add(miscPane, BorderLayout.SOUTH);

			System.out.println("size from prefs " + getSizeFromPreferences());
            //_frame.setSize(getSizeFromPreferences());
			//_frame.setMaximumSize(MAX_SIZE); // doesn't work; bug in Java
            _frame.pack();
            _frame.setLocationRelativeTo(frame);
            _frame.setVisible(true);
			_frame.addComponentListener(new ComponentListener() {
				@Override
				public void componentHidden(ComponentEvent e) {
					
				}
				@Override
				public void componentMoved(ComponentEvent e) {
					
				}
				@Override
				public void componentResized(ComponentEvent e) {
					// constrain maximum size
					boolean resize = false;
					Dimension size = _frame.getSize();
					System.out.println("COMPONENT RESIZED incoming size " + size);
					if (size.width > MAX_SIZE.width) {
						size.width = MAX_SIZE.width;
						resize = true;
					}
					if (size.height > MAX_SIZE.height) {
						size.height = MAX_SIZE.height;
						resize = true;
					}
					if (size.width < MIN_SIZE.width) {
						size.width = MIN_SIZE.width;
						resize = true;
					}
					if (size.height < MIN_SIZE.height) {
						size.height = MIN_SIZE.height;
						resize = true;
					}
					if (resize) {
						_frame.setSize(size);
					}
					System.out.println("save resized " + resize + " size " + size);
					saveSizeInPreferences(size);
				}
				
				@Override
				public void componentShown(ComponentEvent e) {
					
				}
			});
			_frame.addWindowListener(new WindowAdapter() {
				@Override
			    public void windowClosing(WindowEvent e) {
					_cursorListener.hideCursor();
				}
			});
			_frame.setSize(getSizeFromPreferences());
        }
        return _frame;
    }

    /**
     * Sets the fitting cursor, which keeps track of prompt and transient
     * start and stop cursors.
     * 
     * @param fittingCursor 
     */
    public void setFittingCursor(FittingCursor fittingCursor) {
        if (null == _fittingCursor) {
            _fittingCursorListener = new FittingCursorListener();
        }
        else if (_fittingCursor != fittingCursor) {
            _fittingCursor.removeListener(_fittingCursorListener);
        }
        _fittingCursor = fittingCursor;
        _fittingCursor.addListener(_fittingCursorListener);
    }

    /**
     * Set or change the title.
     * 
     * @param title 
     */
    public void setTitle(final String title) {
        _frame.setTitle(title);
    }

    /**
     * Changes (or initializes) all of the charted data.
     *
	 * @param promptIndex
     * @param prompt
     * @param data
     */
    public void setData(int promptIndex, double[] prompt, ICurveFitData data) {
        createDatasets(_bins, _timeInc, promptIndex, prompt, data);

    }

	/**
	 * Sets the reduced chi square.
	 * 
	 * @param chiSquare 
	 */
	public void setChiSquare(double chiSquare) {
		String text = "" + roundToDecimalPlaces(chiSquare, 6);
		_chiSqTextField.setText(text);
	}

    /**
     * Sets the displayed photon count.
     * 
     * @param photons 
     */
    public void setPhotons(int photons) {
        _photonTextField.setText("" + photons);
    }

    /*
     * Sets whether vertical axis should be logarithmic.
     * 
     * @param logarithmic
     */
    public void setLogarithmic(boolean logarithmic) {
        _logarithmic = logarithmic;
    }

    /**
     * Changes or initializes the start and stop vertical bars.
     *
     * @param transStart
     * @param dataStart
     * @param transStop
     */
    public void setStartStop(double transStart, double dataStart, double transStop) {
        _startStopDraggingUI.setStartStopValues(transStart, dataStart, transStop);
    }

    /**
     * Sets stop and start time bins, based on proportions 0.0..1.0.  This is called from
     * the UI layer that lets user drag the start and stop vertical bars.  Validates
     * and passes changes on to external listener.
     *
     * @param transStartProportion
     * @param dataStartProportion
     * @param transStopProportion
     */
    public void setStartStopProportion(
            double transStartProportion,
            double dataStartProportion,
            double transStopProportion)
    {
        // calculate new start and stop
        double transStart = transStartProportion * _maxValue;
        double dataStart  = dataStartProportion  * _maxValue;
        double transStop  = transStopProportion  * _maxValue;

        // if changed, notify cursor listeners
        if (null == _transStart || transStart != _transStart
                || null == _dataStart || dataStart != _dataStart
                || null == _transStop || transStop != _transStop)
        {
            _transStart = transStart;
            _dataStart  = dataStart;
            _transStop  = transStop;
            
            if (null != _fittingCursor) {
                _fittingCursor.setTransientStartValue(transStart);
                _fittingCursor.setDataStartValue(dataStart);
                _fittingCursor.setTransientStopValue(transStop);
            }
        }
    }

    /**
     * Creates the chart
     *
     * @param bins number of bins
     * @param timeInc time increment per bin
     * @return the chart
     */
    JFreeChart createCombinedChart(int bins, double timeInc) {

        // create empty chart data sets
        _decayDataset = new XYSeriesCollection();
        _residualDataset = new XYSeriesCollection();

        // make a common horizontal axis for both sub-plots
        NumberAxis timeAxis = new NumberAxis(TIME_AXIS_LABEL);
        timeAxis.setLabel(UNITS_LABEL);
        timeAxis.setRange(0.0, (bins - 1) * timeInc);

        // make a vertically combined plot
        CombinedDomainXYPlot parent = new CombinedDomainXYPlot(timeAxis);

        // create decay sub-plot
        NumberAxis photonAxis;
        if (_logarithmic) {
            photonAxis = new LogarithmicAxis(PHOTON_AXIS_LABEL);
        }
        else {
            photonAxis = new NumberAxis(PHOTON_AXIS_LABEL);
        }
        XYSplineRenderer decayRenderer = new XYSplineRenderer();
		decayRenderer.setSeriesShapesVisible(0, false);
        decayRenderer.setSeriesShapesVisible(1, false);
        decayRenderer.setSeriesLinesVisible(2, false);
        decayRenderer.setSeriesShape
                (2, new Ellipse2D.Float(-1.0f, -1.0f, 2.0f, 2.0f));

		decayRenderer.setSeriesPaint(0, PROMPT_COLOR);
        decayRenderer.setSeriesPaint(1, FITTED_COLOR);
        decayRenderer.setSeriesPaint(2, DECAY_COLOR);

        _decaySubPlot = new XYPlot
                (_decayDataset, null, photonAxis, decayRenderer);
        _decaySubPlot.setDomainCrosshairVisible(true);
        _decaySubPlot.setRangeCrosshairVisible(true);

        // add decay sub-plot to parent
        parent.add(_decaySubPlot, DECAY_WEIGHT);

        // create residual sub-plot
        NumberAxis residualAxis = new NumberAxis(RESIDUAL_AXIS_LABEL);
        XYSplineRenderer residualRenderer = new XYSplineRenderer();
        residualRenderer.setSeriesPaint(0, RESIDUAL_COLOR);
        residualRenderer.setSeriesLinesVisible(0, false);
        residualRenderer.setSeriesShape
                (0, new Ellipse2D.Float(-1.0f, -1.0f, 2.0f, 2.0f));
        
        XYPlot residualSubPlot = new XYPlot
                (_residualDataset, null, residualAxis, residualRenderer);
        residualSubPlot.setDomainCrosshairVisible(true);
        residualSubPlot.setRangeCrosshairVisible(true);
        residualSubPlot.setFixedLegendItems(null);

        // add residual sub-plot to parent
        parent.add(residualSubPlot, RESIDUAL_WEIGHT);

        // now make the top level JFreeChart
        JFreeChart chart = new JFreeChart
                (null, JFreeChart.DEFAULT_TITLE_FONT, parent, true);
        chart.removeLegend();

        return chart;
    }

    /**
     * Creates the data sets for the chart
     *
     * @param bins number of time bins
     * @param timeInc time increment per time bin
	 * @param promptIndex starting bin of prompt
	 * @param prompt prompt curve
     * @param data from the fit
     */
    private void createDatasets(int bins, double timeInc, int promptIndex, double[] prompt, ICurveFitData data)
    {
		XYSeries series1 = new XYSeries("Prompt");
        XYSeries series2 = new XYSeries("Fitted");
        XYSeries series3 = new XYSeries("Data");
        XYSeries series4 = new XYSeries("Residuals");
		double xCurrent;
		
        // show transient data; find the maximum transient data in this pass
        double yDataMax = -Double.MAX_VALUE;
        xCurrent = 0.0;
        for (int i = 0; i < bins; ++i) {
            // show transient data
            double yData = data.getTransient()[i];
            
            // keep track of maximum
            if (yData > yDataMax) {
                yDataMax = yData;
            }
            
            // logarithmic plots can't handle <= 0.0
            series3.add(xCurrent, (yData > 0.0 ? yData : null));
            
            xCurrent += timeInc;
        }
		
		// show prompt if any
		if (null != prompt) {// debugging

			double yPromptMax = -Double.MAX_VALUE;
			for (int i = 0; i < prompt.length; ++i) {
				// debugging
				System.out.println(" " + prompt[i]);
				
				// find max
				if (prompt[i] > yPromptMax) {
					yPromptMax = prompt[i];
				}
			}
			
			// add prompt data
			xCurrent = 0.0;
			for (int i = 0; i < bins; ++i) {
				Double value = null;
				if (null != prompt) {
					if (i >= promptIndex) {
						if (i - promptIndex < prompt.length) {
							// logarithmic plots can't handle <= 0
							if (prompt[i - promptIndex] > 0.0) {
								value = prompt[i - promptIndex];
								value = yDataMax * value / yPromptMax;
							}
						}
					}
				}
				series1.add(xCurrent, value);
				xCurrent += timeInc;
			}
		}

		
        
        int transStart = data.getTransStartIndex();
        int dataStart  = data.getDataStartIndex();
        int transEnd   = data.getTransEndIndex();

        // show fitted & residuals
        xCurrent = 0.0;
        for (int i = 0; i < bins; ++i) {
            // only within cursors
            if (transStart <= i && i <= transEnd) {
                // from transStart..transEnd show fitted
                double yFitted = data.getYFitted()[i - transStart];
 
                // don't allow fitted to grow the chart downward or upward
                if (1.0 <= yFitted && yFitted <= yDataMax) {
                    series2.add(xCurrent, yFitted);
                }
                else {
                    series2.add(xCurrent, null);
                }
                
                // from dataStart..transEnd show residuals
                if (dataStart <= i && 0.0 < yFitted) {
                    double yData = data.getTransient()[i];
                    series4.add(xCurrent, yData - yFitted);
                }
                else {
                    series4.add(xCurrent, null);
                }
            }
            else {
                series2.add(xCurrent, null);
                series4.add(xCurrent, null);
            }
            
            xCurrent += timeInc;
        }

        synchronized (_synchObject) {
            _decayDataset.removeAllSeries();
			_decayDataset.addSeries(series1);
            _decayDataset.addSeries(series2);
            _decayDataset.addSeries(series3);

            _residualDataset.removeAllSeries();
            _residualDataset.addSeries(series4); 
        }
    }
	
	/**
	 * Restores size from Java Preferences.
	 *
	 * @return size
	 */
	private Dimension getSizeFromPreferences() {
	   Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	   return new Dimension(prefs.getInt(WIDTH_KEY, FRAME_SIZE.width),
			   prefs.getInt(HEIGHT_KEY, FRAME_SIZE.height));
	}

	/**
	 * Saves the size to Java Preferences.
	 *
	 * @param size
	 */
	private void saveSizeInPreferences(Dimension size) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putInt(WIDTH_KEY, size.width);
		prefs.putInt(HEIGHT_KEY, size.height);
	}

    private double roundToDecimalPlaces(double value, int decimalPlaces) {
        double decimalTerm = Math.pow(10.0, decimalPlaces);
        int tmp = (int) Math.round(value * decimalTerm);
        double rounded = (double) tmp / decimalTerm;
        return rounded;
    }

    /**
     * Inner class, UI which allows us to paint on top of the components,
     * using JXLayer.
     *
     * @param <V> component
     */
    static class StartStopDraggingUI<V extends JComponent>
            extends AbstractLayerUI<V>
    {
        private static final int CLOSE_ENOUGH = 4; // pizels
        private ChartPanel _panel;
        private XYPlot _plot;
        private IStartStopProportionListener _listener;
        private double _maxValue;
        private boolean _dragTransStartMarker = false;
        private boolean _dragDataStartMarker  = false;
        private boolean _dragTransStopMarker  = false;
        private volatile Double _transStartMarkerProportion;
        private volatile Double _dataStartMarkerProportion;
        private volatile Double _transStopMarkerProportion;
        private int _y0;
        private int _y1;
        private int _xTransStart;
        private int _xDataStart;
        private int _xTransStop;

        /**
         * Creates the UI.
         *
         * @param panel for the chart
         * @param plot within the chart
         * @param listener to be notified when user drags start/stop vertical bars
         * @param maxValue used to scale cursors
         */
        StartStopDraggingUI(ChartPanel panel, XYPlot plot,
                IStartStopProportionListener listener, double maxValue)
        {
            _panel    = panel;
            _plot     = plot;
            _listener = listener;
            _maxValue = maxValue;
        }

        void setStartStopValues
                (double transStartValue, double dataStartValue,
                double transStopValue)
        {
            _transStartMarkerProportion = transStartValue / _maxValue;
            _dataStartMarkerProportion  = dataStartValue  / _maxValue;
            _transStopMarkerProportion  = transStopValue  / _maxValue;
        }


        /**
         * Used to draw the start/stop vertical bars.
         *
         * Overrides 'paintLayer()', not 'paint()'.
         *
         * @param g2D
         * @param layer
         */
        @Override
        protected void paintLayer(Graphics2D g2D, JXLayer<? extends V> layer) {
            // synchronized with chart data update
            synchronized (_synchObject) {
                // this paints chart layer as is
                super.paintLayer(g2D, layer);
            }
            
            if (null != _transStartMarkerProportion &&
                    null != _dataStartMarkerProportion &&
                    null != _transStopMarkerProportion)
            {
                // adjust to current size
                Rectangle2D area = getDataArea();
                double x = area.getX();
                _y0 = (int) area.getY();
                _y1 = (int) (area.getY() + area.getHeight());
                double width = area.getWidth();
				//System.out.println("x " + area.getX() + " y " + area.getY() + " width " + area.getWidth() + " height " + area.getHeight());
                _xTransStart = (int) Math.round(x + width * _transStartMarkerProportion)
                        + HORZ_TWEAK;
                _xDataStart = (int) Math.round(x + width * _dataStartMarkerProportion)
                        + HORZ_TWEAK;
                _xTransStop = (int) Math.round(x + width * _transStopMarkerProportion)
                        + HORZ_TWEAK;

                // custom painting is here
                g2D.setStroke(new BasicStroke(2f));
                g2D.setXORMode(XORvalue(TRANS_START_COLOR));
                g2D.drawLine(_xTransStart, _y0, _xTransStart, _y1);
                g2D.setXORMode(XORvalue(DATA_START_COLOR));
                g2D.drawLine(_xDataStart, _y0, _xDataStart, _y1);
                g2D.setXORMode(XORvalue(TRANS_STOP_COLOR));
                g2D.drawLine(_xTransStop, _y0, _xTransStop, _y1);    
            }
        }

        /**
         * Mouse listener, catches drag events
         *
         * @param event
         * @param layer
         */
        @Override
        protected void processMouseMotionEvent
                (MouseEvent event, JXLayer<? extends V> layer)
        {
            super.processMouseMotionEvent(event, layer);
            if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
                if (_dragTransStartMarker || _dragDataStartMarker
                        || _dragTransStopMarker) {
                    double newProportion = getDraggedProportion(event);
                    if (_dragTransStartMarker) {
                        if (newProportion <= _dataStartMarkerProportion) {
                            _transStartMarkerProportion = newProportion;
                        }
                        else {
                            _transStartMarkerProportion = _dataStartMarkerProportion;
                        }
                    }
                    else if (_dragDataStartMarker) {
                        if (newProportion >= _transStartMarkerProportion) {
                            if (newProportion <= _transStopMarkerProportion) {
                                _dataStartMarkerProportion = newProportion;
                            }
                            else {
                                _dataStartMarkerProportion = _transStopMarkerProportion;
                            }
                        }
                        else {
                            _dataStartMarkerProportion = _transStartMarkerProportion;
                        }
                    }
                    else {
                        if (newProportion >= _dataStartMarkerProportion) {
                            _transStopMarkerProportion = newProportion;
                        }
                        else {
                            _transStopMarkerProportion = _dataStartMarkerProportion;
                        }
                    }
                    // mark the ui as dirty and needing to be repainted
                    setDirty(true);
                }
            }
        }

        private Color XORvalue(Color color) {
            int drawRGB = color.getRGB();
            int backRGB = BACK_COLOR.getRGB();
            return new Color(drawRGB ^ backRGB);
        }

        /**
         * Gets the currently dragged horizontal value as a proportion,
         * a value between 0.0 and 1.0.
         *
         * @param e
         * @return proportion
         */
        private double getDraggedProportion(MouseEvent e) {
            Rectangle2D dataArea =
                    _panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            Rectangle2D area = getDataArea();
            double proportion = (e.getX() - area.getX()) / area.getWidth();
            if (proportion < 0.0) {
                proportion = 0.0;
            }
            else if (proportion > 1.0) {
                proportion = 1.0;
            }
            return proportion;
        }

        /**
         * Mouse listener, catches mouse button events.
         * @param e
         * @param l
         */
        @Override
        protected void processMouseEvent(MouseEvent e, JXLayer<? extends V> l) {
            super.processMouseEvent(e, l);
            if (null != _transStartMarkerProportion && null != _transStopMarkerProportion) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    int x = e.getX();
                    int y = e.getY();
                    if (y > _y0 - CLOSE_ENOUGH && y < _y1 + CLOSE_ENOUGH) {
                        if (Math.abs(x - _xTransStart) < CLOSE_ENOUGH) {
                            // check for superimposition
                            if (_xTransStart == _xDataStart) {
                                if (_xTransStart == _xTransStop) {
                                    // all three superimposed
                                    if (x < _xTransStart) {
                                        // start dragging trans start line
                                        _dragTransStartMarker = true;
                                    }
                                    else {
                                        // start dragging trans stop line
                                        _dragTransStopMarker = true;
                                    }
                                }
                                else {
                                    // trans and data start superimposed
                                    if (x < _xTransStart) {
                                        // start dragging trans start line
                                        _dragTransStartMarker = true;
                                    }
                                    else {
                                        // start dragging data start line
                                        _dragDataStartMarker = true;
                                    }
                                }
                            }
                            else {
                                // no superimposition; start dragging start line
                                _dragTransStartMarker = true;
                            }

                        }
                        else if (Math.abs(x - _xDataStart) < CLOSE_ENOUGH) {
                            // check for superimposition
                            if (_xDataStart == _xTransStop) {
                                // data start and trans stop superimposed
                                if (x < _xDataStart) {
                                    // start dragging data start line
                                    _dragDataStartMarker = true;
                                }
                                else {
                                    // start dragging trans stop line
                                    _dragTransStopMarker = true;
                                }
                            }
                            else {
                                // no superimposition; start dragging data start line
                                _dragDataStartMarker = true;
                            }
                        }
                        else if (Math.abs(x - _xTransStop) < CLOSE_ENOUGH) {
                            // possible superimpositions already checked
                            
                            // start dragging trans stop line
                            _dragTransStopMarker = true;
                        }
                    }
                }
                if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                    _dragTransStartMarker = _dragDataStartMarker = _dragTransStopMarker = false;
                    SwingUtilities.invokeLater(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (null != _listener) {
                                        _listener.setStartStopProportion
                                                (_transStartMarkerProportion,
                                                 _dataStartMarkerProportion,
                                                 _transStopMarkerProportion);
                                    }
                                }
                    });
                }  
            }
        }

        /**
         * Gets the area of the chart panel.
		 * 
		 * As you resize larger and larger the maximum value returned for height
		 * is 724 and the maximum width 961.
         *
         * @return 2D rectangle area
         */
        private Rectangle2D getDataArea() {
            Rectangle2D dataArea =
                    _panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            return dataArea;
        }

        /**
         * Converts screen x to chart x value.
         *
         * @param x
         * @return chart value
         */

        private double screenToValue(int x) {
            return _plot.getDomainAxis().java2DToValue(
                    (double) x, getDataArea(), RectangleEdge.TOP);
        }
    }  
    
    private class FittingCursorListener implements IFittingCursorListener {
        @Override
        public void cursorChanged(FittingCursor cursor) {
            double transStart = cursor.getTransientStartValue();
            double dataStart  = cursor.getDataStartValue();
            double transStop  = cursor.getTransientStopValue();
            setStartStop(transStart, dataStart, transStop);
            _frame.repaint();
        }
    }    
}

/**
 * Used only within DecayGraph, to get results from StartStopDraggingUI inner class.
 *
 * @author Aivar Grislis
 */
interface IStartStopProportionListener {
    public void setStartStopProportion(
            double transStartProportion,
            double dataStartProportion,
            double transStopProportion);
}
