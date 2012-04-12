/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.histogram;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ij.IJ;

/**
 * This class holds the text fields that show the current minimum and maximum
 * LUT range.  It also has checkboxes to control how the ranges are derived
 * and displayed.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class UIPanel extends JPanel {
    private static final String AUTO_RANGING = "Adjust range to min/max values";
    private static final String EXCLUDE_PIXELS = "Hide pixels outside range";
    private static final String DISPLAY_CHANNELS = "Display all channels";
    private static final String COMBINE_CHANNELS = "Combine all channels";
    
    private static final int DIGITS = 5;
    IUIPanelListener _listener;
    JCheckBox _autoRangeCheckBox;
    JCheckBox _excludePixelsCheckBox;
    JCheckBox _combineChannelsCheckBox;
    JCheckBox _displayChannelsCheckBox;
    JTextField _minTextField;
    JTextField _maxTextField;
    boolean _autoRange;
    boolean _excludePixels;
    boolean _combineChannels;
    boolean _displayChannels;
    double _minLUT;
    double _maxLUT;

    /**
     * Constructor.
     * 
     * @param hasChannels
     */
    public UIPanel(boolean hasChannels) {
        super();

        // initial state
        _autoRange        = true;
        _excludePixels    = false;
        _combineChannels  = false;
        _displayChannels  = false;
        _minLUT = _maxLUT = 0.0;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // make a panel for the min/max readouts
        JPanel readOutPanel = new JPanel();
        readOutPanel.setLayout(new BoxLayout(readOutPanel, BoxLayout.X_AXIS));

        _minTextField = new JTextField();
        _minTextField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        _minLUT = Double.parseDouble(_minTextField.getText());
                        if (null != _listener) {
                            _listener.setMinMaxLUT(_minLUT, _maxLUT);
                        }
                    }
                    catch (NumberFormatException exception) {
                        // in the event of an error, just revert
                        _minTextField.setText("" + _minLUT);
                    }
                }
            }
        );
        readOutPanel.add(_minTextField);

        _maxTextField = new JTextField();
        _maxTextField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        _maxLUT = Double.parseDouble(_maxTextField.getText());
                        if (null != _listener) {
                            _listener.setMinMaxLUT(_minLUT, _maxLUT);
                        }
                    }
                    catch (NumberFormatException exception) {
                        // in the event of an error, just revert
                        _maxTextField.setText("" + _maxLUT);
                    }
                }
            }
        );
        readOutPanel.add(_maxTextField);
        add(readOutPanel);
 
        _autoRangeCheckBox = new JCheckBox(AUTO_RANGING, _autoRange);
        _autoRangeCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    _autoRange = _autoRangeCheckBox.isSelected();
                    enableUI(_autoRange);
                    if (null != _listener) {
                        _listener.setAutoRange(_autoRange);
                    }
                }
            }
        );
        add(_autoRangeCheckBox);
        
        _excludePixelsCheckBox = new JCheckBox(EXCLUDE_PIXELS, _excludePixels);
        _excludePixelsCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    _excludePixels = _excludePixelsCheckBox.isSelected();
                    if (null != _listener) {
                        _listener.setExcludePixels(_excludePixels);
                    }
                }
            }
        );
        add(_excludePixelsCheckBox);
        
        _combineChannelsCheckBox =
            new JCheckBox(COMBINE_CHANNELS, _combineChannels);
        _combineChannelsCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    _combineChannels = _combineChannelsCheckBox.isSelected();
                    if (null != _listener) {
                        _listener.setCombineChannels(_combineChannels);
                    }
                }
            }
        );
        
        /**
         * IJ1 uses the same LUT for the entire stack.  It might be possible for
         * the histogram tool to set the appropriate LUT for the current channel
         * but there is no listener or event for the histogram tool to know when
         * the channel changes.
         * 
         * Perhaps this can change with IJ2.
         * 
         * ARG 2/8/12
         */
        if (false && hasChannels) {
            add(_combineChannelsCheckBox);
        }

        _displayChannelsCheckBox =
            new JCheckBox(DISPLAY_CHANNELS, _displayChannels);
        _displayChannelsCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    _displayChannels = _displayChannelsCheckBox.isSelected();
                    if (null != _listener) {
                        _listener.setDisplayChannels(_displayChannels);
                    }   
                }
            }
        );
        /**
         * Now that we're down to one extra checkbox for channels, lets always
         * display it.  (See above.)
         * 
         * ARG 2/8/12
         */
        if (true || hasChannels) {
            add(_displayChannelsCheckBox);
        }

        enableUI(_autoRange);
    }

    /**
     * Sets a listener for this UI panel.  Listener is unique.
     * 
     * @param listener 
     */
    public void setListener(IUIPanelListener listener) {
        _listener = listener;
    }

    public void setAutoRange(boolean autoRange) {
        _autoRange = autoRange;
        _autoRangeCheckBox.setSelected(autoRange);
        enableUI(autoRange);
    }
    
    public void setCombineChannels(boolean combineChannels) {
        _combineChannels = combineChannels;
        _combineChannelsCheckBox.setSelected(combineChannels);
    }
    
    public void setDisplayChannels(boolean displayChannels) {
        _displayChannels = displayChannels;
        _displayChannelsCheckBox.setSelected(displayChannels);
    }
    
    public void enableChannels(boolean enable) {
        _combineChannelsCheckBox.setEnabled(enable);
        _displayChannelsCheckBox.setEnabled(enable);
    }

    /**
     * Called when the user is dragging the cursors on the histogram panel.
     * 
     * @param min
     * @param max 
     */
    public void dragMinMaxLUT(double min, double max) {
//        System.out.println("UIPanel.dragMinMaxLUT " + min + " " + max);
        showMinMaxLUT(min, max);
    }

    /**
     * Called when the user sets new cursors on the histogram panel.
     * 
     * @param min
     * @param max 
     */
    public void setMinMaxLUT(double min, double max) {
//        System.out.println("UIPanel.setMinMaxLUT " + min + " " + max);
        showMinMaxLUT(min, max);
        //TODO anything else?  if not combine these two methods
    }

    /**
     * Enable/disable UI elements as appropriate.
     */
    private void enableUI(boolean auto) {
        _minTextField.setEnabled(!auto);
        _maxTextField.setEnabled(!auto);
        _excludePixelsCheckBox.setEnabled(!auto);
    }

    /*
     * Shows the minimum and maximum LUT readouts.  Limits number of digits
     * shown.
     */
    private void showMinMaxLUT(double min, double max) {
        DoubleFormatter minFormatter = new DoubleFormatter(true, DIGITS, min);
        _minTextField.setText(minFormatter.getText());
        try {
            _minLUT = Double.parseDouble(_minTextField.getText());
        }
        catch (NumberFormatException e) {
            IJ.log("Error parsing min '" + minFormatter.getText() + "' " + e);
        }
        DoubleFormatter maxFormatter = new DoubleFormatter(false, DIGITS, max);
        _maxTextField.setText(maxFormatter.getText());
        try {
            _maxLUT = Double.parseDouble(_maxTextField.getText());
        }
        catch (NumberFormatException e) {
            IJ.log("Error parsing max '" + maxFormatter.getText() + "' " + e);
        }
    }    
}
