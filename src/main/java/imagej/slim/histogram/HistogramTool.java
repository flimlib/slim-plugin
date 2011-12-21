/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

import java.awt.BorderLayout;
import java.awt.image.IndexColorModel;
import java.io.IOException;

import ij.plugin.LutLoader;
import ij.process.ByteProcessor;
import ij.ImagePlus;
import ij.process.LUT;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;


/**
 * This is the main class for the histogram tool.  It handles layout and wiring
 * of UI components and the logic of updating the histogram.
 *
 * @author Aivar Grislis
 */
public class HistogramTool {
    private final static int WIDTH = PaletteFix.getSize();
    private final static int INSET = 5;
    private final static int HISTOGRAM_HEIGHT = 140;
    private final static int COLORBAR_HEIGHT = 20;
    private final static int TASK_PERIOD = 100;
    //TODO kludged in, user s/b able to select LUTS, somewhere else:
    //TODO note that most IJ LUTs are unsuitable here, so having one standard lifetime LUT is not so bad
    private final static String HARDCODED_LUT =  "/Applications/ImageJ/luts/aivar6.lut"; // aivar6 is my five color blue/cyan/green/yellow/red spectral palette
    private static HistogramTool INSTANCE = null;
    private final Object _synchObject = new Object();
    private JFrame _frame;
    private HistogramData _histogramData;
    private HistogramPanel _histogramPanel;
    private ColorBarPanel _colorBarPanel;
    private UIPanel _uiPanel;
 
    /**
     * Constructor, handles layout and wiring.
     */
    private HistogramTool() {
        // create the histogram and color bar display panels
        _histogramPanel = new HistogramPanel(WIDTH, INSET, HISTOGRAM_HEIGHT);
        _histogramPanel.setListener(new HistogramPanelListener());
        _colorBarPanel = new ColorBarPanel(WIDTH, INSET, COLORBAR_HEIGHT);
        _colorBarPanel.setLUT(getLUT());
        _uiPanel = new UIPanel(true);
        _uiPanel.setListener(new UIPanelListener());

        _frame = new JFrame("Histogram");
        _frame.setResizable(false);
        _frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //TODO kind of lame, for now
        _frame.getContentPane().add(_histogramPanel, BorderLayout.NORTH);
        _frame.getContentPane().add(_colorBarPanel, BorderLayout.CENTER);
        _frame.getContentPane().add(_uiPanel, BorderLayout.SOUTH);
        _frame.pack();
        _frame.setVisible(true);
    }

    /**
     * Class is a singleton for convenience.
     * 
     * @return 
     */
    public static synchronized HistogramTool getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new HistogramTool();
        }
        return INSTANCE;
    }

    /**
     * Gets an IndexColorModel by loading a hardcoded LUT file.
     * Temporary expedient, belongs elsewhere.
     * 
     * @return 
     */
    public static IndexColorModel getIndexColorModel() {
        IndexColorModel colorModel = null;
        try {
            colorModel = LutLoader.open(HARDCODED_LUT);
        }
        catch (IOException e) {
            System.out.println("Error opening LUT " + e.getMessage());
        }

        // IJ converts the FloatProcessor to 8-bits and then uses this palette
        // for display.  Unfortunately values less than or greater than the LUT
        // range still get displayed with LUT colors.  To work around this, use
        // only 254 of the LUT colors.  The first and last colors will represent
        // values less than and greater than the LUT range respectively.

        colorModel = PaletteFix.fixIndexColorModel(colorModel, Color.BLACK, Color.WHITE);
        return colorModel;
    }

    /**
     * Gets a LUT.  Temporary expedient, belongs elsewhere.
     * 
     * @return 
     */
    public static LUT getLUT() {
        IndexColorModel colorModel = getIndexColorModel();
        LUT lut = new LUT(colorModel, Double.MIN_VALUE, Double.MAX_VALUE);
        return lut;
    }

    /**
     * This method should be called whenever a new set of histogram values is
     * to be displayed (i.e. when a different image gets focus).
     * 
     * @param histogramData 
     */
    public void setHistogramData(HistogramData histogramData) {
        double[] minMaxView;
        double[] minMaxLUT;
        synchronized (_synchObject) {
            _histogramData = histogramData;
            _histogramData.setListener(new HistogramDataListener());
            minMaxView = _histogramData.getMinMaxView();
            minMaxLUT  = _histogramData.getMinMaxLUT();
        }
        
        if (_frame.isVisible()) {
            _frame.setVisible(true);
        }
        _frame.setTitle(histogramData.getTitle());
        
        _histogramPanel.setBins(histogramData.binValues(WIDTH));
        //_histogramPanel.setCursors(0.0, 0.0); set cursors if not autoranging

        _uiPanel.setAutoRange(histogramData.getAutoRange());
        _uiPanel.setCombineChannels(histogramData.getCombineChannels());
        _uiPanel.setDisplayChannels(histogramData.getDisplayChannels());
        _uiPanel.setMinMaxLUT(minMaxLUT[0], minMaxLUT[1]);
        
        _colorBarPanel.setMinMax(minMaxView[0], minMaxView[1],
                minMaxLUT[0], minMaxLUT[1]);
    }

    /*
     * Converts histogram onscreen horizontal pixel amounts to image values.
     */
    private double pixelToValue(int pixel) {
        synchronized (_synchObject) {
            double[] minMaxView = _histogramData.getMinMaxView();
            double min = minMaxView[0];
            double max = minMaxView[1];
            double value = (max - min) / PaletteFix.getSize();
            return (pixel - INSET) * value;
        }
    }

    /*
     * Converts image value to histogram onscreen horizontal pixel.
     */
    private int valueToPixel(double value) {
        synchronized (_synchObject) {
            double[] minMaxView = _histogramData.getMinMaxView();
            double min = minMaxView[0];
            double max = minMaxView[1];
            int pixel = (int)(PaletteFix.getSize() * (value - min) / (max - min));
            return pixel;
        }
    }

    /*
     * Updates histogram and color bar during the fit.
     */
    private void changed(double minView, double maxView,
                double minLUT, double maxLUT) {
        synchronized (_synchObject) {
            int[] bins = _histogramData.binValues(WIDTH);
            _histogramPanel.setBins(bins);
            _colorBarPanel.setMinMax(minView, maxView, minLUT, maxLUT);
        }
    }

    /**
     * Inner class listens for changes during the fit.
     */
    private class HistogramDataListener implements IHistogramDataListener {
        
        @Override
        public void minMaxChanged(double minView, double maxView,
                double minLUT, double maxLUT) {
            changed(minView, maxView, minLUT, maxLUT);
        }
    }

    /**
     * Inner class to listen for the user moving the cursor on the histogram.
     */
    private class HistogramPanelListener implements IHistogramPanelListener {
        private Timer _timer = null;
        private volatile int _dragPixels;
 
        /**
         * Listens to the HistogramPanel, gets minimum and maximum cursor bar
         * positions in pixels.  Called when the cursor bar is moved and the
         * mouse button released.  A new LUT range has been specified.
         * 
         * @param min
         * @param max 
         */
        public void setMinMaxLUTPixels(int min, int max) {
            killTimer();

            // get new minimum and maximum values for LUT
            double minLUT = pixelToValue(min);
            double maxLUT = pixelToValue(max);
            
            // set min and max on UI panel
            _uiPanel.setMinMaxLUT(minLUT, maxLUT);

            // redraw color bar
            _colorBarPanel.setMinMaxLUT(minLUT, maxLUT);
            
            // save and redraw image
            synchronized (_synchObject) {
                _histogramData.setMinMaxLUT(minLUT, maxLUT);
            }
        }

        /**
         * Listens to the HistogramPanel, gets minimum and maximum cursor bar
         * positions in pixels.  Called during a drag operation.
         *
         * @param min
         * @param max
         */
        @Override
        public void dragMinMaxPixels(int min, int max) {
            System.out.println("dragMinMax(" + min + "," + max + ")");
            if (min < 0 || max >= PaletteFix.ADJUSTED_SIZE) {
                // cursor is out of bounds, set up a periodic task to stretch
                // the bounds, if not already running
                if (min < 0) {
                    _dragPixels = min;
                }
                else {
                    _dragPixels = max - PaletteFix.ADJUSTED_SIZE + 1;
                }
                if (null == _timer) {
                    System.out.println("Schedule");
                    _timer = new Timer();
                    _timer.schedule(new PeriodicTask(), 0, TASK_PERIOD);
                }
            }
            else {
                // dragging within bounds now, kill the periodic task, if any
                killTimer();
                _uiPanel.dragMinMaxLUT(pixelToValue(min), pixelToValue(max));
            }
        }

        @Override
        public void exited() {
            // dragged off the panel, kill the periodic task
            killTimer();
        }

        // stop our timer for animating view expansion
        private void killTimer() {
            if (null != _timer) {
                _timer.cancel();
                _timer = null;
            }
        }

        /**
         * Inner class used to animate view expansion, triggered by the initial
         * report of a mouse drag event off the edge of the histogram.
         */
        private class PeriodicTask extends TimerTask {

            @Override
            public void run() {
                // how much are we dragging, converted to our value
                double value = pixelToValue(_dragPixels);
                System.out.println("value " + value + " dp " + _dragPixels);
                synchronized (_synchObject) {
                    // get current LUT bounds
                    double[] minMaxLUT = _histogramData.getMinMaxLUT();
                    double minLUT = minMaxLUT[0];
                    double maxLUT = minMaxLUT[1];

                    // adjust the appropriate left or right side of the view
                    double[] minMaxView = _histogramData.getMinMaxView();
                    double minView = minMaxView[0];
                    double maxView = minMaxView[1];
                    if (value < 0) {
                        minView += value;
                        minLUT = minView;
                    }
                    else {
                        maxView += value;
                        maxLUT = maxView;
                    }

                    // get updated histogram data & show it
                    _histogramData.setMinMaxView(minView, maxView);
                    int[] bins = _histogramData.binValues(PaletteFix.ADJUSTED_SIZE);
                    _histogramPanel.setBins(bins);
                    _colorBarPanel.setMinMax(minView, maxView, minLUT, maxLUT);
                    System.out.println("set to " + minView + " " + maxView);
                }
            }
        }
    }

    private class UIPanelListener implements IUIPanelListener {
        @Override
        public void setAutoRange(boolean autoRange) {
            System.out.println("HistogramTool.UIPanelListener.setAutoRange(" + autoRange + ")");
            synchronized (_synchObject) {
                 _histogramData.setAutoRange(autoRange);
            }
             // turn on/off the cursors
            // they are usually at -1 & 255
            // but if you are autoranging & showing all channels
            // they need to be calculated
            // autorange off, stuff shouldn't change right away
            // autorange on, should calculate new bounds
        }
        
        @Override
        public void setCombineChannels(boolean combineChannels) {
            synchronized (_synchObject) {
                _histogramData.setCombineChannels(combineChannels);
            }
            //TODO
            System.out.println("HistogramTool.UIPanelListener.setCombineChannels(" + combineChannels + ")");
        }
        
        @Override
        public void setDisplayChannels(boolean displayChannels) {
            synchronized (_synchObject) {
                _histogramData.setDisplayChannels(displayChannels);
            }
            //TODO
            System.out.println("HistogramTool.UIPanelListener.setDisplayChannels(" + displayChannels + ")");
        }

        @Override
        public void setMinMaxLUT(double minLUT, double maxLUT) {
            boolean changed = false;
            double minView = 0;
            double maxView = 0;
            
            // silently ignores errors; c/b temporary condition such as setting
            // maximum before minimum
            if (minLUT < maxLUT) {
                changed = true;
                
                synchronized (_synchObject) {
                    // expand the view to fit the LUT
                    double[] minMaxView = _histogramData.getMinMaxView();
                    minView = Math.min(minLUT, minMaxView[0]);
                    maxView = Math.max(maxLUT, minMaxView[1]);
                    _histogramData.setMinMaxView(minView, maxView);
                    _histogramData.setMinMaxLUT(minLUT, maxLUT);
                }
            }

            if (changed) {
                System.out.println("CHANGED:" + minView + " " + maxView);
                // update histogram and color bar
                changed(minView, maxView, minLUT, maxLUT);
            }
            System.out.println("new min LUT is " + minLUT + "new max LUT is " + maxLUT);   
        }
    }
}
