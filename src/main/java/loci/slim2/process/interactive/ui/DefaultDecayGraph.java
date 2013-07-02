/*
Combined spectral-lifetime image analysis plugin.

Copyright (c) 2010-2013, UW-Madison LOCI
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

package loci.slim2.process.interactive.ui;

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

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import loci.curvefitter.ICurveFitData;
import loci.slim2.fitting.FitResults;
import loci.slim2.process.interactive.cursor.FittingCursor;
import loci.slim2.process.interactive.cursor.FittingCursorListener;

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
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class DefaultDecayGraph implements DecayGraph, IStartStopProportionListener {
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
    
    private static final Object synchObject = new Object();

    private JFrame frame;
    FittingCursor fittingCursor;
    FittingCursorListenerImpl fittingCursorListener;
	PixelPicker picker;

    private StartStopDraggingUI<JComponent> startStopDraggingUI;
    private double timeInc;
    private int bins;
    private double maxValue;
    private Double transStart;
    private Double dataStart;
    private Double transStop;
    private boolean logarithmic = true;

    XYPlot decaySubPlot;
    XYSeriesCollection decayDataset;
    XYSeriesCollection residualDataset;

	JTextField tau1TextField;
	JTextField tau2TextField;
	JTextField tau3TextField;
	JTextField chiSqTextField;
    JTextField photonTextField;
    JCheckBox logCheckBox;

    @Override
    public JFrame init(final JFrame parentFrame, final int bins,
			final double timeInc, PixelPicker pixelPicker)
	{
        if (null == frame
                || !frame.isVisible()
                || this.bins != bins
                || this.timeInc != timeInc)
        {
            // save incoming parameters
            this.bins = bins;
            this.timeInc = timeInc;
            maxValue = timeInc * bins;
			this.picker = pixelPicker;
            
            if (null != frame) {
                // delete existing frame
                frame.setVisible(false);
                frame.dispose();
            }
            
             // create the combined chart
            JFreeChart chart = createCombinedChart(bins, timeInc);
            ChartPanel panel = new ChartPanel
                    (chart, true, true, true, false, true);
            panel.setDomainZoomable(false);
            panel.setRangeZoomable(false);
            panel.setPreferredSize(SIZE);

            // add start/stop vertical bar handling
            dataStart = transStop = null;
            JXLayer<JComponent> layer = new JXLayer<JComponent>(panel);
            startStopDraggingUI =
                    new StartStopDraggingUI<JComponent>
                            (panel, decaySubPlot, this, maxValue);
            layer.setUI(startStopDraggingUI);

            // create a frame for the chart
            frame = new JFrame();
            Container container = frame.getContentPane();
			container.setLayout(new BorderLayout());
			container.add(layer, BorderLayout.CENTER);
            
            JPanel miscPane = new JPanel();
            miscPane.setLayout(new FlowLayout());
			JLabel label1 = new JLabel(CHI_SQUARE);
			miscPane.add(label1);
			chiSqTextField = new JTextField(7);
			chiSqTextField.setEditable(false);
			miscPane.add(chiSqTextField);
            JLabel label2 = new JLabel(PHOTON_COUNT);
            miscPane.add(label2);
            photonTextField = new JTextField(7);
            photonTextField.setEditable(false);
            miscPane.add(photonTextField);
            logCheckBox = new JCheckBox(LOGARITHMIC);
            logCheckBox.setSelected(logarithmic);
            logCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    logarithmic = logCheckBox.isSelected();
                    NumberAxis photonAxis;
                    if (logarithmic) {
                        photonAxis = new LogarithmicAxis(PHOTON_AXIS_LABEL);
                    }
                    else {
                        photonAxis = new NumberAxis(PHOTON_AXIS_LABEL);
                    }
                    decaySubPlot.setRangeAxis(photonAxis);
                }
            });
            miscPane.add(logCheckBox);
			container.add(miscPane, BorderLayout.SOUTH);

			System.out.println("size from prefs " + getSizeFromPreferences());
            //_frame.setSize(getSizeFromPreferences());
			//_frame.setMaximumSize(MAX_SIZE); // doesn't work; bug in Java
            frame.pack();
            frame.setLocationRelativeTo(parentFrame);
            frame.setVisible(true);
			frame.addComponentListener(new ComponentListener() {
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
					Dimension size = frame.getSize();
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
						frame.setSize(size);
					}
					System.out.println("save resized " + resize + " size " + size);
					saveSizeInPreferences(size);
				}
				
				@Override
				public void componentShown(ComponentEvent e) {
					
				}
			});
			this.frame.addWindowListener(new WindowAdapter() {
				@Override
			    public void windowClosing(WindowEvent e) {
					if (null != picker) {
						picker.hideCursor(); //TODO ARG does not work if you use the name 'pixelPicker' (collides with local var; this.pP won't work).
					}
				}
			});
			this.frame.setSize(getSizeFromPreferences());
        }
        return this.frame;
    }

	@Override
    public void setFittingCursor(FittingCursor fittingCursor) {
		System.out.println("DefaultDecayGraph.setFittingCursor " + fittingCursor);
        if (null == this.fittingCursor) {
			// first time, create a listener
            fittingCursorListener = new FittingCursorListenerImpl();
        }
        else if (this.fittingCursor != fittingCursor) {
			// fitting cursor changed, remove listener from old version
            this.fittingCursor.removeListener(fittingCursorListener);
        }
        this.fittingCursor = fittingCursor;
        this.fittingCursor.addListener(fittingCursorListener);
    }

    @Override
    public void setTitle(final String title) {
        frame.setTitle(title);
    }

    @Override
    public void setData(int promptIndex, double[] prompt, FitResults fitResults) {
        createDatasets(bins, timeInc, promptIndex, prompt, fittingCursor, fitResults);

    }

	@Override
	public void setChiSquare(double chiSquare) {
		String text = "" + roundToDecimalPlaces(chiSquare, 6);
		chiSqTextField.setText(text);
	}

    @Override
    public void setPhotons(int photons) {
        photonTextField.setText("" + photons);
    }

    /*
     * Sets whether vertical axis should be logarithmic.
     * 
     * @param logarithmic
     */
    public void setLogarithmic(boolean logarithmic) {
        this.logarithmic = logarithmic;
    }

    @Override
    public void setStartStop(double transStart, double dataStart, double transStop) {
        startStopDraggingUI.setStartStopValues(transStart, dataStart, transStop);
    }

    @Override
    public void setStartStopProportion(
            double transStartProportion,
            double dataStartProportion,
            double transStopProportion)
    {
        // calculate new start and stop
        double transStartCalc = transStartProportion * maxValue;
        double dataStartCalc  = dataStartProportion  * maxValue;
        double transStopCalc  = transStopProportion  * maxValue;

        // if changed, notify cursor listeners
        if (null == transStart || transStart != transStartCalc
                || null == dataStart || dataStart != dataStartCalc
                || null == transStop || transStop != transStopCalc)
        {
            transStart = transStartCalc;
            dataStart  = dataStartCalc;
            transStop  = transStopCalc;
            
            if (null != fittingCursor) {		
                fittingCursor.setTransientStartValue(transStart);
                fittingCursor.setDataStartValue(dataStart);
                fittingCursor.setTransientStopValue(transStop);
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
        decayDataset = new XYSeriesCollection();
        residualDataset = new XYSeriesCollection();

        // make a common horizontal axis for both sub-plots
        NumberAxis timeAxis = new NumberAxis(TIME_AXIS_LABEL);
        timeAxis.setLabel(UNITS_LABEL);
        timeAxis.setRange(0.0, (bins - 1) * timeInc);

        // make a vertically combined plot
        CombinedDomainXYPlot parent = new CombinedDomainXYPlot(timeAxis);

        // create decay sub-plot
        NumberAxis photonAxis;
        if (logarithmic) {
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

        decaySubPlot = new XYPlot
                (decayDataset, null, photonAxis, decayRenderer);
        decaySubPlot.setDomainCrosshairVisible(true);
        decaySubPlot.setRangeCrosshairVisible(true);

        // add decay sub-plot to parent
        parent.add(decaySubPlot, DECAY_WEIGHT);

        // create residual sub-plot
        NumberAxis residualAxis = new NumberAxis(RESIDUAL_AXIS_LABEL);
        XYSplineRenderer residualRenderer = new XYSplineRenderer();
        residualRenderer.setSeriesPaint(0, RESIDUAL_COLOR);
        residualRenderer.setSeriesLinesVisible(0, false);
        residualRenderer.setSeriesShape
                (0, new Ellipse2D.Float(-1.0f, -1.0f, 2.0f, 2.0f));
        
        XYPlot residualSubPlot = new XYPlot
                (residualDataset, null, residualAxis, residualRenderer);
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
	 * @param fittingCursor cursor information
     * @param fitResults from the fit
     */
    private void createDatasets(int bins, double timeInc, int promptIndex, double[] prompt, FittingCursor fittingCursor, FitResults fitResults)
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
            double yData = fitResults.getTransient()[i];

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
       
        int transStart = fittingCursor.getTransientStartBin();
        int dataStart  = fittingCursor.getDataStartBin();
        int transEnd   = fittingCursor.getTransientStopBin();
		
		System.out.println("graphing cursors indices are " + transStart + " " + dataStart + " " + transEnd);
		if (0 == transStart && 0 == dataStart && 0 == transEnd) {
			transEnd = bins - 1;
		}

        // show fitted & residuals
        xCurrent = 0.0;
        for (int i = 0; i < bins; ++i) {
            // only within cursors
            if (transStart <= i && i <= transEnd) {
                // from transStart..transEnd show fitted
                double yFitted = fitResults.getYFitted()[i - transStart];
 
                // don't allow fitted to grow the chart downward or upward
                if (1.0 <= yFitted && yFitted <= yDataMax) {
                    series2.add(xCurrent, yFitted);
                }
                else {
                    series2.add(xCurrent, null);
                }
                
                // from dataStart..transEnd show residuals
                if (dataStart <= i && 0.0 < yFitted) {
                    double yData = fitResults.getTransient()[i];
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

        synchronized (synchObject) {
            decayDataset.removeAllSeries();
			decayDataset.addSeries(series1);
            decayDataset.addSeries(series2);
            decayDataset.addSeries(series3);

            residualDataset.removeAllSeries();
            residualDataset.addSeries(series4); 
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
        private ChartPanel panel;
        private XYPlot plot;
        private IStartStopProportionListener listener;
        private double maxValue;
        private boolean dragTransStartMarker = false;
        private boolean dragDataStartMarker  = false;
        private boolean dragTransStopMarker  = false;
        private volatile Double transStartMarkerProportion;
        private volatile Double dataStartMarkerProportion;
        private volatile Double transStopMarkerProportion;
        private int y0;
        private int y1;
        private int xTransStart;
        private int xDataStart;
        private int xTransStop;

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
            this.panel    = panel;
            this.plot     = plot;
            this.listener = listener;
            this.maxValue = maxValue;
        }

        void setStartStopValues
                (double transStartValue, double dataStartValue,
                double transStopValue)
        {
            transStartMarkerProportion = transStartValue / maxValue;
            dataStartMarkerProportion  = dataStartValue  / maxValue;
            transStopMarkerProportion  = transStopValue  / maxValue;
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
            synchronized (synchObject) {
                // this paints chart layer as is
                super.paintLayer(g2D, layer);
            }
            
            if (null != transStartMarkerProportion &&
                    null != dataStartMarkerProportion &&
                    null != transStopMarkerProportion)
            {
                // adjust to current size
                Rectangle2D area = getDataArea();
                double x = area.getX();
                y0 = (int) area.getY();
                y1 = (int) (area.getY() + area.getHeight());
                double width = area.getWidth();
				//System.out.println("x " + area.getX() + " y " + area.getY() + " width " + area.getWidth() + " height " + area.getHeight());
                xTransStart = (int) Math.round(x + width * transStartMarkerProportion)
                        + HORZ_TWEAK;
                xDataStart = (int) Math.round(x + width * dataStartMarkerProportion)
                        + HORZ_TWEAK;
                xTransStop = (int) Math.round(x + width * transStopMarkerProportion)
                        + HORZ_TWEAK;

                // custom painting is here
                g2D.setStroke(new BasicStroke(2f));
                g2D.setXORMode(XORvalue(TRANS_START_COLOR));
                g2D.drawLine(xTransStart, y0, xTransStart, y1);
                g2D.setXORMode(XORvalue(DATA_START_COLOR));
                g2D.drawLine(xDataStart, y0, xDataStart, y1);
                g2D.setXORMode(XORvalue(TRANS_STOP_COLOR));
                g2D.drawLine(xTransStop, y0, xTransStop, y1);    
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
                if (dragTransStartMarker || dragDataStartMarker
                        || dragTransStopMarker) {
                    double newProportion = getDraggedProportion(event);
                    if (dragTransStartMarker) {
                        if (newProportion <= dataStartMarkerProportion) {
                            transStartMarkerProportion = newProportion;
                        }
                        else {
                            transStartMarkerProportion = dataStartMarkerProportion;
                        }
                    }
                    else if (dragDataStartMarker) {
                        if (newProportion >= transStartMarkerProportion) {
                            if (newProportion <= transStopMarkerProportion) {
                                dataStartMarkerProportion = newProportion;
                            }
                            else {
                                dataStartMarkerProportion = transStopMarkerProportion;
                            }
                        }
                        else {
                            dataStartMarkerProportion = transStartMarkerProportion;
                        }
                    }
                    else {
                        if (newProportion >= dataStartMarkerProportion) {
                            transStopMarkerProportion = newProportion;
                        }
                        else {
                            transStopMarkerProportion = dataStartMarkerProportion;
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
                    panel.getChartRenderingInfo().getPlotInfo().getDataArea();
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
            if (null != transStartMarkerProportion && null != transStopMarkerProportion) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    int x = e.getX();
                    int y = e.getY();
                    if (y > y0 - CLOSE_ENOUGH && y < y1 + CLOSE_ENOUGH) {
                        if (Math.abs(x - xTransStart) < CLOSE_ENOUGH) {
                            // check for superimposition
                            if (xTransStart == xDataStart) {
                                if (xTransStart == xTransStop) {
                                    // all three superimposed
                                    if (x < xTransStart) {
                                        // start dragging trans start line
                                        dragTransStartMarker = true;
                                    }
                                    else {
                                        // start dragging trans stop line
                                        dragTransStopMarker = true;
                                    }
                                }
                                else {
                                    // trans and data start superimposed
                                    if (x < xTransStart) {
                                        // start dragging trans start line
                                        dragTransStartMarker = true;
                                    }
                                    else {
                                        // start dragging data start line
                                        dragDataStartMarker = true;
                                    }
                                }
                            }
                            else {
                                // no superimposition; start dragging start line
                                dragTransStartMarker = true;
                            }

                        }
                        else if (Math.abs(x - xDataStart) < CLOSE_ENOUGH) {
                            // check for superimposition
                            if (xDataStart == xTransStop) {
                                // data start and trans stop superimposed
                                if (x < xDataStart) {
                                    // start dragging data start line
                                    dragDataStartMarker = true;
                                }
                                else {
                                    // start dragging trans stop line
                                    dragTransStopMarker = true;
                                }
                            }
                            else {
                                // no superimposition; start dragging data start line
                                dragDataStartMarker = true;
                            }
                        }
                        else if (Math.abs(x - xTransStop) < CLOSE_ENOUGH) {
                            // possible superimpositions already checked
                            
                            // start dragging trans stop line
                            dragTransStopMarker = true;
                        }
                    }
                }
                if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                    dragTransStartMarker = dragDataStartMarker = dragTransStopMarker = false;
                    SwingUtilities.invokeLater(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (null != listener) {
                                        listener.setStartStopProportion
                                                (transStartMarkerProportion,
                                                 dataStartMarkerProportion,
                                                 transStopMarkerProportion);
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
                    panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            return dataArea;
        }

        /**
         * Converts screen x to chart x value.
         *
         * @param x
         * @return chart value
         */

        private double screenToValue(int x) {
            return plot.getDomainAxis().java2DToValue(
                    (double) x, getDataArea(), RectangleEdge.TOP);
        }
    }  
    
    private class FittingCursorListenerImpl implements FittingCursorListener {
        @Override
        public void cursorChanged(FittingCursor cursor) {
            double transStart = cursor.getTransientStartValue();
            double dataStart  = cursor.getDataStartValue();
            double transStop  = cursor.getTransientStopValue();
            setStartStop(transStart, dataStart, transStop);
            frame.repaint();
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