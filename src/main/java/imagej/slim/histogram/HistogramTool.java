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
 * This is the main class for this histogram tool.  It handles layout, wiring,
 * and logic.
 *
 * @author Aivar Grislis
 */
public class HistogramTool {
    private final static int WIDTH = 264;
    private final static int HISTOGRAM_HEIGHT = 140;
    private final static int COLORBAR_HEIGHT = 20;
    private final static int EXTRA = 4;
    private final static int TASK_PERIOD = 100;
    //TODO kludged in, user s/b able to select LUTS, somewhere else:
    private final static String HARDCODED_LUT =  "/Applications/ImageJ/luts/aivar6.lut"; // aivar6 is my five color blue/cyan/green/yellow/red spectral palette
    private static HistogramTool INSTANCE = null;
    private HistogramData _histogramData;
    private JFrame _frame;
    private HistogramPanel _histogram;
    private ColorBarPanel _colorBar;
 
    /**
     * Constructor, handles layout and wiring.
     */
    private HistogramTool() {
        // create the histogram and color bar display panels
        _histogram = new HistogramPanel(WIDTH, HISTOGRAM_HEIGHT, EXTRA);
        _histogram.setListener(new HistogramPanelListener());
        _colorBar = new ColorBarPanel(WIDTH, COLORBAR_HEIGHT, EXTRA);
        _colorBar.setLUT(getLUT());

        _frame = new JFrame("Histogram");
        //TODO closes the entire plugin:
        //m_frame.setDefaultCloseOperation(m_frame.EXIT_ON_CLOSE);
        _frame.setResizable(false);
        _frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //TODO kind of lame, for now
        _frame.getContentPane().add(_histogram, BorderLayout.NORTH);
        _frame.getContentPane().add(_colorBar, BorderLayout.CENTER);
        //TODO_frame.getContentPane().add(_inputPanel, BorderLayout.SOUTH);
        _frame.pack();
        _frame.setVisible(true);
        
       //TODO FOR A TEST:
        _histogram.setCursors(3, 44);
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
        return colorModel;
    }

    /**
     * Gets a LUT.
     * 
     * @return 
     */
    public static LUT getLUT() {
        IndexColorModel colorModel = getIndexColorModel();
        LUT lut = new LUT(colorModel, Double.MIN_VALUE, Double.MAX_VALUE);
        return lut;
    }

    /**
     * @param histogramData 
     */
    //TODO this method is called from the focus listener only???
    //  what about changes during the fit?
    public void setHistogramData(HistogramData histogramData) {
        _histogramData = histogramData;
        if (_frame.isVisible()) {
            _frame.setVisible(true);
        }
        _frame.setTitle(histogramData.getTitle());
        _histogram.setBins(histogramData.binValues(WIDTH));
    }

    /**
     * Inner class to listen for the user moving the cursor on the histogram.
     */
    private class HistogramPanelListener implements IHistogramPanelListener {
        private Timer _timer = null;
        
        private HistogramPanelListener() {
        }
 
        /**
         * Listens to the HistogramPanel, gets minimum and maximum cursor bar
         * positions in pixels.  Called when the cursor bar is moved and the
         * mouse button released.  Also called during a drag operation when the
         * cursor bar is out of bounds.
         * 
         * @param min
         * @param max 
         */
        public void setMinMax(int min, int max) {
            System.out.println("setMinMax(" + min + "," + max + ")");
            if (min < 0 || max > 255) {
                // cursor is out of bounds, set up a periodic task to stretch
                // the bounds
                if (null == _timer) {
                    System.out.println("Schedule");
                    _timer = new Timer();
                    _timer.schedule(new PeriodicTask(), 0, TASK_PERIOD);
                }
            }
            else {
                if (null != _timer) {
                    _timer.cancel();
                    _timer = null;
                }
            }
        }
        
        public void expand() { }
        
        private class PeriodicTask extends TimerTask {
            public void run() {
                System.out.println("timer task");
            }   
        }
    }
}
