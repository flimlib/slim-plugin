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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;


/**
 *
 * @author aivar
 */
public class HistogramTool {
    private final static int WIDTH = 320;
    private final static int HISTOGRAM_HEIGHT = 160;
    private final static int COLORBAR_HEIGHT = 20;
    private final static int EXTRA = 2;
    private final static String HARDCODED_LUT =  "/Applications/ImageJ/luts/aivar6.lut"; // aivar6 is my five color blue/cyan/green/yellow/red spectral palette
    private static HistogramTool INSTANCE = null;
    private HistogramData _histogramData;
    private JFrame _frame;
    private HistogramPanel _histogram;
    private ColorBarPanel _colorBar;
    
    private HistogramTool() {
        // create the histogram and color bar display panels
        _histogram = new HistogramPanel(WIDTH, HISTOGRAM_HEIGHT, EXTRA);
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
    
    public static synchronized HistogramTool getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new HistogramTool();
        }
        return INSTANCE;
    }
    
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
    
    public static LUT getLUT() {
        IndexColorModel colorModel = getIndexColorModel();
        LUT lut = new LUT(colorModel, Double.MIN_VALUE, Double.MAX_VALUE);
        return lut;
    }
    
    public void setHistogramData(HistogramData histogramData) {
        _histogramData = histogramData;
        if (_frame.isVisible()) {
            _frame.setVisible(true);
        }
        _frame.setTitle(histogramData.getTitle());
        _histogram.setBins(histogramData.binValues(WIDTH));
    }
}
