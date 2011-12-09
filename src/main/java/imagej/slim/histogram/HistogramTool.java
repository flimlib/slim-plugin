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
    private final static String HARDCODED_LUT =  "/Applications/ImageJ/luts/aivar6.lut"; // aivar6 is my five color blue/cyan/green/yellow/red spectral palette
    JFrame _frame;
    HistogramPanel _histogram;
    ColorBarPanel _colorBar;
    
    public HistogramTool() {
        //TODO fakes up a LUT
        LUT lut = null;
        IndexColorModel colorModel = null;
        try {
            colorModel = LutLoader.open(HARDCODED_LUT);
        }
        catch (IOException e) {
            System.out.println("Error opening LUT " + e.getMessage());
        }
        ByteProcessor bp = LutLoader.createImage(colorModel);
        ImagePlus ip = new ImagePlus(HARDCODED_LUT, bp);
        System.out.println("BOUT TO DRAW");
        ip.draw();
        lut = new LUT(colorModel, Double.MIN_VALUE, Double.MAX_VALUE); //TODO not sure what min/max values do here
        //TODO some LUTs, such as unionjack, do not load correctly using this technique
        
        // create the histogram and color bar display panels
        _histogram = new HistogramPanel(320, 160, 2);
        _colorBar = new ColorBarPanel(320, 20, 2);
        _colorBar.setLUT(lut);

        _frame = new JFrame("Histogram");
        //TODO closes the entire plugin:
        //m_frame.setDefaultCloseOperation(m_frame.EXIT_ON_CLOSE);
        _frame.setResizable(false);
        _frame.getContentPane().add(_histogram, BorderLayout.NORTH);
        _frame.getContentPane().add(_colorBar, BorderLayout.CENTER);
        //TODO_frame.getContentPane().add(_inputPanel, BorderLayout.SOUTH);
        _frame.pack();
        _frame.setVisible(true);
        _frame.setTitle("WOOHOO");
    }
}
