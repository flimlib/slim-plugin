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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import ij.IJ;
import java.util.prefs.Preferences;

/**
 * This class holds the text fields that show the current minimum and maximum
 * LUT range.  It also has checkboxes to control how the ranges are derived
 * and displayed.
 * 
 * @author Aivar Grislis
 */
public class HistogramUIPanel extends JPanel {
    private static final String AUTO_RANGING = "Adjust range to min/max values";
    private static final String EXCLUDE_PIXELS = "Hide pixels outside range";
    private static final String DISPLAY_CHANNELS = "Display all channels";
    private static final String COMBINE_CHANNELS = "Combine all channels";
	private static final String LOG_DISPLAY = "Logarithmic";
	private static final String SMOOTHING = "Smoothing";
	private static final String FAMILY1 = "Family 1";
	private static final String FAMILY2 = "Family 2";
	private static final String BANDWIDTH = "Bandwidth:";
    
    private static final int DIGITS = 5;
	
	private Preferences userPreferences = Preferences.userNodeForPackage(this.getClass());
	private static final String EXPERIMENTAL = "experimental";
	
    IUIPanelListener _listener;
    JCheckBox _autoRangeCheckBox;
    JCheckBox _excludePixelsCheckBox;
    JCheckBox _combineChannelsCheckBox;
    JCheckBox _displayChannelsCheckBox;
	JCheckBox _logarithmicDisplayCheckBox;
	JCheckBox _smoothingCheckBox;
	JRadioButton _family1RadioButton;
	JRadioButton _family2RadioButton;
	JRadioButton _bandwidthRadioButton;
    JTextField _minTextField;
    JTextField _maxTextField;
	JTextField _bandwidthTextField;
    boolean _autoRange;
    boolean _excludePixels;
    boolean _combineChannels;
    boolean _displayChannels;
	boolean _logarithmicDisplay;
	boolean _smoothing;
	boolean _family1;
	boolean _family2;
    double _minLUT;
    double _maxLUT;
	double _bandwidth;

    /**
     * Constructor.
     * 
     * @param hasChannels
     */
    public HistogramUIPanel(boolean hasChannels) {
        super();

        // initial state
        _autoRange          = true;
        _excludePixels      = false;
        _combineChannels    = false;
        _displayChannels    = false;
		_logarithmicDisplay = false;
		_smoothing          = false;
        _minLUT = _maxLUT = 0.0;
		_bandwidth = 0.25;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // make a panel for the min/max readouts
        JPanel readOutPanel = new JPanel();
        readOutPanel.setLayout(new BoxLayout(readOutPanel, BoxLayout.X_AXIS));

        _minTextField = new JTextField();
        _minTextField.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    updateMin();
                }
            }
        );
        _minTextField.addFocusListener(
            new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateMin();
                }
            }
        );
        readOutPanel.add(_minTextField);

        _maxTextField = new JTextField();
        _maxTextField.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    updateMax();
                }
            }
        );
        _maxTextField.addFocusListener(
            new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateMax();
                }
            }
        );
        readOutPanel.add(_maxTextField);
        add(readOutPanel);
 
        _autoRangeCheckBox = new JCheckBox(AUTO_RANGING, _autoRange);
        _autoRangeCheckBox.addItemListener(
            new ItemListener() {
                @Override
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
                @Override
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
                @Override
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
                @Override
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
		
		_logarithmicDisplayCheckBox =
		    new JCheckBox(LOG_DISPLAY, _logarithmicDisplay);
		_logarithmicDisplayCheckBox.addItemListener(
		    new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					_logarithmicDisplay = _logarithmicDisplayCheckBox.isSelected();
					if (null != _listener) {
						_listener.setLogarithmicDisplay(_logarithmicDisplay);
					}
				}
			}
		);
		add(_logarithmicDisplayCheckBox);
		
		boolean experimental = userPreferences.getBoolean(EXPERIMENTAL, false);
		userPreferences.putBoolean(EXPERIMENTAL, experimental);
		if (experimental) {
			JLabel experimentalLabel = new JLabel("Experimental:");
			add(experimentalLabel);

			_smoothingCheckBox =
				new JCheckBox(SMOOTHING, _smoothing);
			_smoothingCheckBox.addItemListener(
				new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						_smoothing = _smoothingCheckBox.isSelected();
						if (null != _listener) {
							_listener.setSmoothing(_smoothing);
						}
					}
				}
			);
			add(_smoothingCheckBox);

			_family1RadioButton = new JRadioButton(FAMILY1);
			_family1RadioButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						if (null != _listener) {
							if (_family1RadioButton.isSelected()) {
								_listener.setFamilyStyle1(true);
							}
						}
					}
				}
			);
			add(_family1RadioButton);

			_family2RadioButton = new JRadioButton(FAMILY2);
			_family2RadioButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						if (null != _listener) {
							if (_family2RadioButton.isSelected()) {
								_listener.setFamilyStyle2(true);
							}
						}
					}
				}
			);
			add(_family2RadioButton);

			_bandwidthRadioButton = new JRadioButton(BANDWIDTH);
			_bandwidthRadioButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						if (null != _listener) {
							if (_bandwidthRadioButton.isSelected()) {
								updateBandwidth();
							}
						}
					}
				}
			);
			add(_bandwidthRadioButton);

			ButtonGroup group = new ButtonGroup();
			group.add(_family1RadioButton);
			group.add(_family2RadioButton);
			group.add(_bandwidthRadioButton);

			_bandwidthTextField = new JTextField("" + HistogramPanel.DEFAULT_BANDWIDTH);
			_bandwidthTextField.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						updateBandwidth();
					}
				}
			);
			_bandwidthTextField.addFocusListener(
				new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						updateBandwidth();
					}
				}
			);
			add(_bandwidthTextField);
		}
		
		enableUI(_autoRange);
    }
    
    private void updateMin() {
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
    
    private void updateMax() {
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
	
	private void updateBandwidth() {
		try {
			_bandwidth = Double.parseDouble(_bandwidthTextField.getText());
			System.out.println("bandwidth parsed to " + _bandwidth);
			if (null != _listener) {
				_listener.setBandwidth(_bandwidth);
			}
		}
		catch (NumberFormatException exception) {
			// in the event of an error, just revert
			_bandwidthTextField.setText("" + _bandwidth);
		}
	}

    /**
     * Sets a listener for this UI panel.  Listener is unique.
     * 
     * @param listener 
     */
    public void setListener(IUIPanelListener listener) {
        _listener = listener;
    }

    /**
     * Sets whether to automatically set the LUT range.
     * 
     * @param autoRange 
     */
    public void setAutoRange(boolean autoRange) {
        _autoRange = autoRange;
        _autoRangeCheckBox.setSelected(autoRange);
        enableUI(autoRange);
    }

    /**
     * Sets whether to hide out of range pixels.
     * 
     * @param excludePixels 
     */
    public void setExcludePixels(boolean excludePixels) {
        _excludePixels = excludePixels;
        _excludePixelsCheckBox.setSelected(excludePixels);
    }

    /**
     * Gets whether to hide out of range pixels.
     * 
     * @return 
     */
    public boolean getExcludePixels() {
        return _excludePixels;
    }

    /**
     * Sets whether to combine all channels.
     * 
     * For IJ1 this doesn't change anything.
     * 
     * @param combineChannels 
     */
    public void setCombineChannels(boolean combineChannels) {
        _combineChannels = combineChannels;
        _combineChannelsCheckBox.setSelected(combineChannels);
    }

    /**
     * Sets whether to display all channels.
     * 
     * @param displayChannels 
     */
    public void setDisplayChannels(boolean displayChannels) {
        _displayChannels = displayChannels;
        _displayChannelsCheckBox.setSelected(displayChannels);
    }

    /**
     * Enables or disables the checkbox UI.
     * 
     * @param enable 
     */
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
		String text;
		
        DoubleFormatter minFormatter = new DoubleFormatter(true, DIGITS, min);
		text = minFormatter.getText();
        _minTextField.setText(text);
		_minLUT = parse(text);
		
        DoubleFormatter maxFormatter = new DoubleFormatter(false, DIGITS, max);
		text = maxFormatter.getText();
        _maxTextField.setText(text);
		_maxLUT = parse(text);
    }
	
	private double parse(String text) {
		double value = 0.0;
		// check for values of infinity
		int index = text.indexOf(DoubleFormatter.INFINITY);
		if (0 == index) {
			value = Double.POSITIVE_INFINITY;
			//System.out.println("parsed positive infinity");
		}
		else if (1 == index) {
			value = Double.NEGATIVE_INFINITY;
			//System.out.print("parsed negative infinity");
		}
		else {
			try {
				value = Double.parseDouble(text);
			}
			catch (NumberFormatException e) {
				IJ.log("Error parsing '" + text + "' " + e);
			}
		}
		return value;
	}
}
