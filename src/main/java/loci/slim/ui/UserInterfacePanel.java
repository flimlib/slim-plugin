//
// UserInterfacePanel.java
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

package loci.slim.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.io.SaveDialog;

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.curvefitter.IFitterEstimator;
import loci.slim.fitting.cursor.FittingCursorHelper;
import loci.slim.fitting.cursor.IFittingCursorUI;

/**
 * Main user interface panel for the fit.
 *
 * @author Aivar Grislis grislis at wisc.edu
 */

public class UserInterfacePanel implements IUserInterfacePanel, IFittingCursorUI {
    // Unicode special characters
    private static final Character CHI    = '\u03c7';
    private static final Character SQUARE = '\u00b2';
    private static final Character TAU    = '\u03c4';
    private static final Character LAMBDA = '\u03bb';
    private static final Character SIGMA  = '\u03c3';
    private static final Character SUB_1  = '\u2081';
    private static final Character SUB_2  = '\u2082';
    private static final Character SUB_3  = '\u2083';
    private static final Character SUB_M  = '\u2098'; // Unicode 6.0.0 (October 2010)
    
    private static final String SUM_REGION = "Sum All Pixels";
    private static final String ROIS_REGION = "Sum Each ROI";
    private static final String PIXEL_REGION = "Single Pixel";
    private static final String ALL_REGION = "Images";
    
    private static final String JAOLHO_LMA_ALGORITHM = "Jaolho LMA";
    private static final String SLIM_CURVE_RLD_ALGORITHM = "SLIMCurve RLD";
    private static final String SLIM_CURVE_LMA_ALGORITHM = "SLIMCurve LMA";
    private static final String SLIM_CURVE_RLD_LMA_ALGORITHM = "SLIMCurve RLD+LMA";

    private static final String SINGLE_EXPONENTIAL = "Single Exponential";
    private static final String DOUBLE_EXPONENTIAL = "Double Exponential";
    private static final String TRIPLE_EXPONENTIAL = "Triple Exponential";
    private static final String STRETCHED_EXPONENTIAL = "Stretched Exponential";

    private static final String GAUSSIAN_FIT = "Gaussian Fit";
    private static final String POISSON_FIT = "Poisson Fit";
    private static final String POISSON_DATA = "Poisson Data";
    private static final String MAXIMUM_LIKELIHOOD = "Max. Likelihood Est.";

    private static final String CHI_SQ_TARGET = "" + CHI + SQUARE + " Target";

    private static final String EXCITATION_NONE = "None";
    private static final String EXCITATION_FILE = "Load from File";
    private static final String EXCITATION_CREATE = "Use current X Y";

    private static final String FIT_IMAGE = "Fit Images";
    private static final String FIT_PIXEL = "Fit Pixel";
    private static final String FIT_SUMMED_PIXELS = "Fit Summed Pixels";
    private static final String FIT_SUMMED_ROIS = "Fit Summed ROIs";
    private static final String CANCEL_FIT = "Cancel Fit";

    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    private static final Border ETCHED_BORDER = BorderFactory.createEtchedBorder();

	//TODO ARG fitting a series of ROIs is broken, so omit that possibility, for now:
    private static final String REGION_ITEMS[] = { SUM_REGION, /*ROIS_REGION,*/ PIXEL_REGION, ALL_REGION };
    private static final String ALGORITHM_ITEMS[] = { JAOLHO_LMA_ALGORITHM, SLIM_CURVE_RLD_ALGORITHM, SLIM_CURVE_LMA_ALGORITHM, SLIM_CURVE_RLD_LMA_ALGORITHM };
    private static final String FUNCTION_ITEMS[] = { SINGLE_EXPONENTIAL, DOUBLE_EXPONENTIAL, TRIPLE_EXPONENTIAL, STRETCHED_EXPONENTIAL };
    private static final String NOISE_MODEL_ITEMS[] = { GAUSSIAN_FIT, POISSON_FIT, POISSON_DATA, MAXIMUM_LIKELIHOOD };
    
    private static final String A_T_Z_X2 = "A " + TAU + " Z " + CHI + SQUARE;
    private static final String A_T_X2 = "A " + TAU + " " + CHI + SQUARE;
    private static final String A_T = "A " + TAU;
    private static final String T_X2 = TAU + " " + CHI + SQUARE;
    private static final String T = "" + TAU;
    private static final String A_T_H_Z_X2 = "A " + TAU + " H Z " + CHI + SQUARE;
    private static final String A_T_H_X2 = "A " + TAU + " H " + CHI + SQUARE;
    private static final String A_T_H = "A " + TAU + " H";
    private static final String T_H_X2 = TAU + " H " + CHI + SQUARE;
    private static final String T_H = TAU + " H";
    private static final String F_UPPER = "F";
    private static final String F_LOWER = "f";
    private static final String TAU_MEAN = "" + TAU + "m"; // SUB_M;
    
    private static final String SINGLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, T_X2, T };
    private static final String DOUBLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, T_X2, T, F_UPPER, F_LOWER, TAU_MEAN };
    private static final String TRIPLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, T_X2, T, F_UPPER, F_LOWER, TAU_MEAN };    
    private static final String STRETCHED_FITTED_IMAGE_ITEMS[] = { A_T_H_Z_X2, A_T_H_X2, A_T_H, T_H_X2, T_H, T };    
    
    private static final String EXCITATION_ITEMS[] = { EXCITATION_NONE, EXCITATION_FILE, EXCITATION_CREATE };
    
    private FittingCursorHelper _fittingCursorHelper;
    private IFitterEstimator _fitterEstimator;
    
    private IUserInterfacePanelListener _listener;
    int _fittedParameterCount = 0;

    // UI panel
    JPanel _COMPONENT;
    JFrame _frame;
    JPanel _cardPanel;

    JComboBox _regionComboBox;
    JComboBox _algorithmComboBox;
    JComboBox _functionComboBox;
    JComboBox _noiseModelComboBox;
    JComboBox _fittedImagesComboBox;
    JCheckBox _colorizeGrayScale;
    JCheckBox[] _analysisCheckBoxList;
    JCheckBox _fitAllChannels;
    
    // cursor settings
    JTextField _promptBaselineField;
    JTextField _transientStartField;
    JTextField _dataStartField;
    JTextField _transientStopField;
    JTextField _promptDelayField;
    JTextField _promptWidthField;
    JCheckBox _showBins;
    JComboBox _promptComboBox;
    JButton _estimateCursorsButton;
    
    // fit settings
    JTextField _xField;
    JTextField _yField;
    JTextField _thresholdField;
    JTextField _chiSqTargetField;
    JComboBox _binningComboBox;

    // parameter panel
    JPanel _paramPanel;
    int _paramPanelIndex;

    // single exponent fit
    JTextField _aParam1;
    JCheckBox _aFix1;
    JTextField _tParam1;
    JCheckBox _tFix1;
    JTextField _zParam1;
    JCheckBox _zFix1;
    JTextField _chiSqParam1;
    JCheckBox _startParam1;

    // double exponent fit
    JTextField _a1Param2;
    JCheckBox _a1Fix2;
    JTextField _a2Param2;
    JCheckBox _a2Fix2;
    JTextField _t1Param2;
    JCheckBox _t1Fix2;
    JTextField _t2Param2;
    JCheckBox _t2Fix2;
    JTextField _zParam2;
    JCheckBox _zFix2;
    JTextField _chiSqParam2;
    JCheckBox _startParam2;

    // triple exponent fit
    JTextField _a1Param3;
    JCheckBox _a1Fix3;
    JTextField _a2Param3;
    JCheckBox _a2Fix3;
    JTextField _a3Param3;
    JCheckBox _a3Fix3;
    JTextField _t1Param3;
    JCheckBox _t1Fix3;
    JTextField _t2Param3;
    JCheckBox _t2Fix3;
    JTextField _t3Param3;
    JCheckBox _t3Fix3;
    JTextField _zParam3;
    JCheckBox _zFix3;
    JTextField _chiSqParam3;
    JCheckBox _startParam3;

    // stretched exonent fit
    JTextField _aParam4;
    JCheckBox _aFix4;
    JTextField _tParam4;
    JCheckBox _tFix4;
    JTextField _hParam4;
    JCheckBox _hFix4;
    JTextField _zParam4;
    JCheckBox _zFix4;
    JTextField _chiSqParam4;
    JCheckBox _startParam4;

    JButton _quitButton;
    JButton _fitButton;
    String _fitButtonText = FIT_IMAGE;

    public UserInterfacePanel(boolean tabbed, boolean showTau,
            String[] analysisChoices, String[] binningChoices,
            FittingCursorHelper fittingCursorHelper,
            IFitterEstimator fitterEstimator)
    {
        String lifetimeLabel = "" + (showTau ? TAU : LAMBDA);
        
        _fittingCursorHelper = fittingCursorHelper;
        _fitterEstimator = fitterEstimator;
        
        _frame = new JFrame("SLIM Plugin");

        // create outer panel
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        
        if (tabbed) {
            JTabbedPane tabbedPane = new JTabbedPane();

            JPanel fitPanel = createFitPanel(analysisChoices);
            tabbedPane.addTab("Fit", fitPanel);

            JPanel cursorPanel = createCursorPanel();
            tabbedPane.addTab("Cursors", cursorPanel); 

            JPanel controlPanel = createControlPanel(binningChoices);
            tabbedPane.addTab("Control", controlPanel);   

            // Create cards and the panel that contains the cards
            _cardPanel = new JPanel(new CardLayout());
            _cardPanel.add(createSingleExponentialPanel(lifetimeLabel), SINGLE_EXPONENTIAL);
            _cardPanel.add(createDoubleExponentialPanel(lifetimeLabel), DOUBLE_EXPONENTIAL);
            _cardPanel.add(createTripleExponentialPanel(lifetimeLabel), TRIPLE_EXPONENTIAL);
            _cardPanel.add(createStretchedExponentialPanel(lifetimeLabel), STRETCHED_EXPONENTIAL);
            tabbedPane.addTab("Params", _cardPanel);

            outerPanel.add(tabbedPane);
        }
        else {
            // create inner panel
            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));

            JPanel fitPanel = createFitPanel(analysisChoices);
            fitPanel.setBorder(border("Fit"));
            innerPanel.add(fitPanel);

            JPanel cursorPanel = createCursorPanel();
            cursorPanel.setBorder(border("Cursors"));
            innerPanel.add(cursorPanel); 

            JPanel controlPanel = createControlPanel(binningChoices);
            controlPanel.setBorder(border("Control"));
            innerPanel.add(controlPanel);

            // Create cards and the panel that contains the cards
            _cardPanel = new JPanel(new CardLayout());
            _cardPanel.add(createSingleExponentialPanel(lifetimeLabel), SINGLE_EXPONENTIAL);
            _cardPanel.add(createDoubleExponentialPanel(lifetimeLabel), DOUBLE_EXPONENTIAL);
            _cardPanel.add(createTripleExponentialPanel(lifetimeLabel), TRIPLE_EXPONENTIAL);
            _cardPanel.add(createStretchedExponentialPanel(lifetimeLabel), STRETCHED_EXPONENTIAL);
            _cardPanel.setBorder(border("Params"));
            innerPanel.add(_cardPanel);

            outerPanel.add(innerPanel);
        }

        //Lay out the buttons from left to right.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        _quitButton = new JButton("Quit");
        _quitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (null != _listener) {
                        _listener.quit();
                    }
                }
            }
        );
        buttonPanel.add(_quitButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        _fitButton = new JButton(_fitButtonText);
        _fitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String text = (String)e.getActionCommand();
                    if (text.equals(_fitButtonText)) {
                        enableAll(false);
                        setFitButtonState(false);
                        if (null != _listener) {
                            _listener.doFit();
                        }
                    }
                    else{
                        setFitButtonState(true);
                        if (null != _listener) {
                            _listener.cancelFit();
                        }
                    }
                }
            }
        );
        buttonPanel.add(_fitButton);

        outerPanel.add(buttonPanel);
        _frame.getContentPane().add(outerPanel);

        _frame.pack();
        final Dimension preferred = _frame.getPreferredSize();
        _frame.setMinimumSize(preferred);
        _frame.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        // allow horizontal but not vertical resize
                        int width = _frame.getWidth();
                        if (width < (int) preferred.getWidth()) {
                            width = (int) preferred.getWidth();
                        }
                        _frame.setSize(width, (int) preferred.getHeight());
                    }

        });

        // no prompt initially
        enablePromptCursors(false);

        // set up and show initial cursors
        _fittingCursorHelper.setFittingCursorUI(this);
    }

    @Override
    public JFrame getFrame() {
        return _frame;
    }

    @Override
    public void setListener(IUserInterfacePanelListener listener) {
        _listener = listener;
    }

    @Override
    public void reset() {
        enableAll(true);
        setFitButtonState(true);
    }

    private JPanel createFitPanel(String[] analysisChoices) {
        JPanel fitPanel = new JPanel();
        fitPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        fitPanel.setLayout(new SpringLayout());

        JLabel regionLabel = new JLabel("Region");
        regionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(regionLabel);
        _regionComboBox = new JComboBox(REGION_ITEMS);
        _regionComboBox.setSelectedItem(ALL_REGION);
        _regionComboBox.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        String item = (String) e.getItem();
                        if (SUM_REGION.equals(item)) {
                            _fitButtonText = FIT_SUMMED_PIXELS;
                        }
                        else if (ROIS_REGION.equals(item)) {
                            _fitButtonText = FIT_SUMMED_ROIS;
                        }
                        else if (PIXEL_REGION.equals(item)) {
                            _fitButtonText = FIT_PIXEL;
                        }
                        else if (ALL_REGION.equals(item)) {
                            _fitButtonText = FIT_IMAGE;
                        }
                        _fitButton.setText(_fitButtonText);
                    }
                }
            }
        );
        fitPanel.add(_regionComboBox);

        JLabel algorithmLabel = new JLabel("Algorithm");
        algorithmLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(algorithmLabel);
        _algorithmComboBox = new JComboBox(ALGORITHM_ITEMS);
        _algorithmComboBox.setSelectedItem(SLIM_CURVE_RLD_LMA_ALGORITHM);
		refitUponStateChange(_algorithmComboBox);
        fitPanel.add(_algorithmComboBox);

        JLabel functionLabel = new JLabel("Function");
        functionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(functionLabel);
        _functionComboBox = new JComboBox(FUNCTION_ITEMS);
        _functionComboBox.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        String item = (String) e.getItem();
                        CardLayout cl = (CardLayout)(_cardPanel.getLayout());
                        cl.show(_cardPanel, item);
                        reconcileStartParam();
                        updateFittedImagesComboBox(FUNCTION_ITEMS, item);
                    }
                }
            }
        );
		refitUponStateChange(_functionComboBox);
        fitPanel.add(_functionComboBox);

        JLabel noiseModelLabel = new JLabel("Noise Model");
        noiseModelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(noiseModelLabel);
        _noiseModelComboBox = new JComboBox(NOISE_MODEL_ITEMS);
        _noiseModelComboBox.setSelectedItem(MAXIMUM_LIKELIHOOD);
		refitUponStateChange(_noiseModelComboBox);
        fitPanel.add(_noiseModelComboBox);

        JLabel fittedImagesLabel = new JLabel("Fitted Images");
        fittedImagesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(fittedImagesLabel);
        _fittedImagesComboBox = new JComboBox(SINGLE_FITTED_IMAGE_ITEMS);
        fitPanel.add(_fittedImagesComboBox);

        JLabel dummyLabel = new JLabel("");
        dummyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(dummyLabel);
        _colorizeGrayScale = new JCheckBox("Colorize grayscale");
        fitPanel.add(_colorizeGrayScale);

        int choices = analysisChoices.length;
        if (choices > 0) {
            List<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
            boolean firstChoice = true;
            for (String analysisChoice : analysisChoices) {
                String labelString = firstChoice ? "Analysis" : "";
                firstChoice = false;
                JLabel choiceLabel = new JLabel(labelString);
                choiceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                fitPanel.add(choiceLabel);
                JCheckBox checkBox = new JCheckBox(analysisChoice);
                fitPanel.add(checkBox);
                checkBoxList.add(checkBox);
            }
            _analysisCheckBoxList = checkBoxList.toArray(new JCheckBox[0]);
        }
        
        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(fitPanel, 6 + choices, 2, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", fitPanel);

        _fitAllChannels = new JCheckBox("Fit all channels");
        _fitAllChannels.setSelected(true);

        panel.add("South", _fitAllChannels);
        return panel;
    }

    /**
     * Used to build an appropriate list of fitted images, according to the
     * fit function selected.
     * 
     * @param items
     * @param selectedItem 
     */
    private void updateFittedImagesComboBox(String[] items, String selectedItem) {
        if (SINGLE_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(_fittedImagesComboBox, SINGLE_FITTED_IMAGE_ITEMS);
        }
        else if (DOUBLE_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(_fittedImagesComboBox, DOUBLE_FITTED_IMAGE_ITEMS);            
        }
        else if (TRIPLE_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(_fittedImagesComboBox, TRIPLE_FITTED_IMAGE_ITEMS);           
        }
        else if (STRETCHED_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(_fittedImagesComboBox, STRETCHED_FITTED_IMAGE_ITEMS);  
        }
    }
 
    /*
     * Updates a combo box with a new list of items.
     */
    private void updateComboBox(JComboBox comboBox, String[] items) {
        // Had problems with "comboBox.removeAll()":
        for (int i = comboBox.getItemCount() - 1; i >= 0; --i) {
            comboBox.removeItemAt(i);
        }
        for (String item : items) {
            comboBox.addItem(item);
        }
        comboBox.setSelectedIndex(0);
    }
    
    private JPanel createCursorPanel() {
        JPanel cursorPanel = new JPanel();
        cursorPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        cursorPanel.setLayout(new SpringLayout());

        // emulating TRI2 cursor listing order here
        JLabel excitationBaselineLabel = new JLabel("Excitation Baseline");
        excitationBaselineLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(excitationBaselineLabel);
        _promptBaselineField = new JTextField(9);
        _promptBaselineField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setPromptBaseline(_promptBaselineField.getText());
            }
        });
        _promptBaselineField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setPromptBaseline(_promptBaselineField.getText());
            }
        });
        
        cursorPanel.add(_promptBaselineField); 
        JLabel transStartLabel = new JLabel("Transient Start");
        transStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(transStartLabel);
        _transientStartField = new JTextField(9);
        _transientStartField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setTransientStart(_transientStartField.getText());
            }
        });
        _transientStartField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setTransientStart(_transientStartField.getText());
            }
        });
        cursorPanel.add(_transientStartField);
        
        JLabel dataStartLabel = new JLabel("Data Start");
        dataStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(dataStartLabel);
        _dataStartField = new JTextField(9);
        _dataStartField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setDataStart(_dataStartField.getText());
            }
        });
        _dataStartField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setDataStart(_dataStartField.getText());
            }
        });
        cursorPanel.add(_dataStartField);

        JLabel transStopLabel = new JLabel("Transient End");
        transStopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(transStopLabel);
        _transientStopField = new JTextField(9);
        _transientStopField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setTransientStop(_transientStopField.getText());
            }
        });
        _transientStopField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setTransientStop(_transientStopField.getText());
            }
        });
        cursorPanel.add(_transientStopField);

        JLabel excitationStartLabel = new JLabel("Excitation Delay");
        excitationStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(excitationStartLabel);
        _promptDelayField = new JTextField(9);
        _promptDelayField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setPromptDelay(_promptDelayField.getText());
            }
        });
        _promptDelayField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setPromptDelay(_promptDelayField.getText());
            }
        });
        cursorPanel.add(_promptDelayField);       
        
        JLabel excitationStopLabel = new JLabel("Excitation Width");
        excitationStopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(excitationStopLabel);
        _promptWidthField = new JTextField(9);
        _promptWidthField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setPromptWidth(_promptWidthField.getText());
            }
        });
        _promptWidthField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setPromptWidth(_promptWidthField.getText());
            }
        });
        cursorPanel.add(_promptWidthField);
        
        JLabel dummyLabel = new JLabel("");
        dummyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(dummyLabel);
        _showBins = new JCheckBox("Display as indices");
        _showBins.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    boolean showBins = e.getStateChange() == ItemEvent.SELECTED;
                    _fittingCursorHelper.setShowBins(showBins);
                }
            }
        );
        cursorPanel.add(_showBins);
        
        JLabel excitationLabel = new JLabel("Excitation");
        excitationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(excitationLabel);
        _promptComboBox = new JComboBox(EXCITATION_ITEMS);
        _promptComboBox.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selectedItem = (String) _promptComboBox.getSelectedItem();
                    boolean isExcitationLoaded = false;
                    if (EXCITATION_FILE.equals(selectedItem)) {
                        OpenDialog dialog = new OpenDialog("Load Excitation File", "");
                        String directory = dialog.getDirectory();
                        String fileName = dialog.getFileName();
                        if (null != fileName && !fileName.equals("") && null != _listener) {
                            isExcitationLoaded = _listener.loadExcitation(directory + fileName);
                        }
                    }
                    else if (EXCITATION_CREATE.equals(selectedItem)) {
                        SaveDialog dialog = new SaveDialog("Save Excitation File", "", "");
                        String directory = dialog.getDirectory();
                        String fileName = dialog.getFileName();
                        if (null != fileName && !fileName.equals("") && null != _listener) {
                            isExcitationLoaded = _listener.createExcitation(directory + fileName);
                        }
                    }

                    if (isExcitationLoaded) {
                        _promptComboBox.setSelectedItem(EXCITATION_FILE);
                        enablePromptCursors(true);
                    }
                    else {
                        _promptComboBox.setSelectedItem(EXCITATION_NONE);
                        String text = _fittingCursorHelper.getShowBins() ? "0" : "0.0";
                        _promptDelayField.setText(text);
                        _promptWidthField.setText(text);
                        _promptBaselineField.setText("0.0");
                        enablePromptCursors(false);
                        if (null != _listener) {
                            _listener.cancelExcitation();
                        }
                    }
                }
            }
        );
        cursorPanel.add(_promptComboBox);
        
        JLabel dummyLabel2 = new JLabel("");
        dummyLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(dummyLabel2);
        _estimateCursorsButton = new JButton("Estimate Cursors");
        _estimateCursorsButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (null != _listener) {
                        _listener.estimateCursors();
                    }
                }
            }
        );
        cursorPanel.add(_estimateCursorsButton);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(cursorPanel, 9, 2, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", cursorPanel);

        return panel;
    }

    /*
     * Creates a panel that has some settings that control the fit.
     */
    private JPanel createControlPanel(String[] binningChoices) {
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        controlPanel.setLayout(new SpringLayout());

        JLabel xLabel = new JLabel("X");
        xLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(xLabel);
        _xField = new JTextField(9);
		refitUponStateChange(_xField);
        controlPanel.add(_xField);

        JLabel yLabel = new JLabel("Y");
        yLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(yLabel);
        _yField = new JTextField(9);
		refitUponStateChange(_yField);
        controlPanel.add(_yField);

        JLabel thresholdLabel = new JLabel("Threshold");
        thresholdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(thresholdLabel);
        _thresholdField = new JTextField(9);
        controlPanel.add(_thresholdField);

        JLabel chiSqTargetLabel = new JLabel(CHI_SQ_TARGET);
        chiSqTargetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(chiSqTargetLabel);
        _chiSqTargetField = new JTextField(9);
		refitUponStateChange(_chiSqTargetField);
        controlPanel.add(_chiSqTargetField);

        JLabel binningLabel = new JLabel("Bin");
        binningLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(binningLabel);
        _binningComboBox = new JComboBox(binningChoices);
		refitUponStateChange(_binningComboBox);
        controlPanel.add(_binningComboBox);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(controlPanel, 5, 2, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", controlPanel);

        return panel;
    }

    /*
     * Creates panel for the single exponential version of the fit parameters.
     */
    private JPanel createSingleExponentialPanel(String lifetimeLabel) {
        JPanel expPanel = new JPanel();
        expPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        expPanel.setLayout(new SpringLayout());

        JLabel aLabel1 = new JLabel("A");
        aLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(aLabel1);
        _aParam1 = new JTextField(9);
        //_a1Param1.setEditable(false);
        expPanel.add(_aParam1);
        _aFix1 = new JCheckBox("Fix");
        //_a1Fix1.addItemListener(this);
        expPanel.add(_aFix1);
		refitUponStateChange(_aParam1, _aFix1);

        JLabel t1Label1 = new JLabel(lifetimeLabel);
        t1Label1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label1);
        _tParam1 = new JTextField(9);
        //_t1Param1.setEditable(false);
        expPanel.add(_tParam1);
        _tFix1 = new JCheckBox("Fix");
        //_t1Fix1.addItemListener(this);
        expPanel.add(_tFix1);
		refitUponStateChange(_tParam1, _tFix1);

        JLabel zLabel1 = new JLabel("Z");
        zLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel1);
        _zParam1 = new JTextField(9);
        //_zParam1.setEditable(false);
        expPanel.add(_zParam1);
        _zFix1 = new JCheckBox("Fix");
        //_zFix1.addItemListener(this);
        expPanel.add(_zFix1);
		refitUponStateChange(_zParam1, _zFix1);

        JLabel chiSqLabel1 = new JLabel("" + CHI + SQUARE);
        chiSqLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel1);
        _chiSqParam1 = new JTextField(9);
        _chiSqParam1.setEditable(false);
        expPanel.add(_chiSqParam1);
        JLabel nullLabel1 = new JLabel("");
        expPanel.add(nullLabel1);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = _a1Param1.getBackground();
        //Color floatColor = a1Label1.getBackground();
        //_a1Param1.setBackground(floatColor);
        //_t1Param1.setBackground(floatColor);
        //_zParam1.setBackground(floatColor);
        //_chiSqParam1.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 4, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        _startParam1 = new JCheckBox("Use as starting parameters for fit");
        _startParam1.setSelected(true);
        _startParam1.setEnabled(false);

		//TODO ARG 9/21/12 disabled non-functioning UI
        //panel.add("South", _startParam1);
        return panel;
    }

    /*
     * Creates panel for the double exponential version of the fit parameters.
     */
    private JPanel createDoubleExponentialPanel(String lifetimeLabel) {
        JPanel expPanel = new JPanel();
        expPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        expPanel.setLayout(new SpringLayout());

        JLabel a1Label2 = new JLabel("A" + SUB_1);
        a1Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a1Label2);
        _a1Param2 = new JTextField(9);
        //_a1Param2.setEditable(false);
        expPanel.add(_a1Param2);
        _a1Fix2 = new JCheckBox("Fix");
        //_a1Fix2.addItemListener(this);
        expPanel.add(_a1Fix2);
		refitUponStateChange(_a1Param2, _a1Fix2);

        JLabel t1Label2 = new JLabel(lifetimeLabel + SUB_1);
        t1Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label2);
        _t1Param2 = new JTextField(9);
        //_t1Param2.setEditable(false);
        expPanel.add(_t1Param2);
        _t1Fix2 = new JCheckBox("Fix");
        //_t1Fix2.addItemListener(this);
        expPanel.add(_t1Fix2);
		refitUponStateChange(_t1Param2, _t1Fix2);

        JLabel a2Label2 = new JLabel("A" + SUB_2);
        a2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a2Label2);
        _a2Param2 = new JTextField(9);
        //_a2Param2.setEditable(false);
        expPanel.add(_a2Param2);
        _a2Fix2 = new JCheckBox("Fix");
        //_a2Fix2.addItemListener(this);
        expPanel.add(_a2Fix2);
		refitUponStateChange(_a2Param2, _a2Fix2);

        JLabel t2Label2 = new JLabel(lifetimeLabel + SUB_2);
        t2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t2Label2);
        _t2Param2 = new JTextField(9);
        //_t2Param2.setEditable(false);
        expPanel.add(_t2Param2);
        _t2Fix2 = new JCheckBox("Fix");
        //_t2Fix2.addItemListener(this);
        expPanel.add(_t2Fix2);
		refitUponStateChange(_t2Param2, _t2Fix2);

        JLabel zLabel2 = new JLabel("Z");
        zLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel2);
        _zParam2 = new JTextField(9);
        //_zParam2.setEditable(false);
        expPanel.add(_zParam2);
        _zFix2 = new JCheckBox("Fix");
        //_zFix2.addItemListener(this);
        expPanel.add(_zFix2);
		refitUponStateChange(_zParam2, _zFix2);

        JLabel chiSqLabel2 = new JLabel("" + CHI + SQUARE);
        chiSqLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel2);
        _chiSqParam2 = new JTextField(9);
        //_chiSqParam2.setEditable(false);
        expPanel.add(_chiSqParam2);
        JLabel nullLabel2 = new JLabel("");
        expPanel.add(nullLabel2);

        //TODO:
        // From SLIMPlotter
        //Color fixColor = _a1Param2.getBackground();
        //Color floatColor = a1Label2.getBackground();
        //_a1Param2.setBackground(floatColor);
        //_t1Param2.setBackground(floatColor);
        //_a2Param2.setBackground(floatColor);
        //_t2Param2.setBackground(floatColor);
        //_zParam2.setBackground(floatColor);
        //_chiSqParam2.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 6, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        _startParam2 = new JCheckBox("Use as starting parameters for fit");
        _startParam2.setSelected(true);
        _startParam2.setEnabled(false);
		//TODO ARG 9/21/12 disabled non-functioning UI
        //panel.add("South", _startParam2);
        return panel;
    }

    /*
     * Creates panel for the triple exponential version of the fit parameters.
     */
    private JPanel createTripleExponentialPanel(String lifetimeLabel) {
        JPanel expPanel = new JPanel();
        expPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        expPanel.setLayout(new SpringLayout());

        JLabel a1Label3 = new JLabel("A" + SUB_1);
        a1Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a1Label3);
        _a1Param3 = new JTextField(9);
        //_a1Param3.setEditable(false);
        expPanel.add(_a1Param3);
        _a1Fix3 = new JCheckBox("Fix");
        //_a1Fix3.addItemListener(this);
        expPanel.add(_a1Fix3);
		refitUponStateChange(_a1Param3, _a1Fix3);

        JLabel t1Label3 = new JLabel(lifetimeLabel + SUB_1);
        t1Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label3);
        _t1Param3 = new JTextField(9);
        //_t1Param3.setEditable(false);
        expPanel.add(_t1Param3);
        _t1Fix3 = new JCheckBox("Fix");
        //_t1Fix3.addItemListener(this);
        expPanel.add(_t1Fix3);
		refitUponStateChange(_t1Param3, _t1Fix3);

        JLabel a2Label3 = new JLabel("A" + SUB_2);
        a2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a2Label3);
        _a2Param3 = new JTextField(9);
        //_a2Param3.setEditable(false);
        expPanel.add(_a2Param3);
        _a2Fix3 = new JCheckBox("Fix");
        //_a2Fix3.addItemListener(this);
        expPanel.add(_a2Fix3);
		refitUponStateChange(_a2Param3, _a2Fix3);

        JLabel t2Label3 = new JLabel(lifetimeLabel + SUB_2);
        t2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t2Label3);
        _t2Param3 = new JTextField(9);
        //_t2Param3.setEditable(false);
        expPanel.add(_t2Param3);
        _t2Fix3 = new JCheckBox("Fix");
        //_t2Fix3.addItemListener(this);
        expPanel.add(_t2Fix3);
		refitUponStateChange(_t2Param3, _t2Fix3);

        JLabel a3Label3 = new JLabel("A" + SUB_3);
        a3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a3Label3);
        _a3Param3 = new JTextField(9);
        //_a3Param3.setEditable(false);
        expPanel.add(_a3Param3);
        _a3Fix3 = new JCheckBox("Fix");
        //_a3Fix3.addItemListener(this);
        expPanel.add(_a3Fix3);
		refitUponStateChange(_a3Param3, _a3Fix3);

        JLabel t3Label3 = new JLabel(lifetimeLabel + SUB_3);
        t3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t3Label3);
        _t3Param3 = new JTextField(9);
        //_t3Param3.setEditable(false);
        expPanel.add(_t3Param3);
        _t3Fix3 = new JCheckBox("Fix");
        //_t3Fix3.addItemListener(this);
        expPanel.add(_t3Fix3);
		refitUponStateChange(_t3Param3, _t3Fix3);

        JLabel zLabel3 = new JLabel("Z");
        zLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel3);
        _zParam3 = new JTextField(9);
        //_zParam3.setEditable(false);
        expPanel.add(_zParam3);
        _zFix3 = new JCheckBox("Fix");
        //_zFix3.addItemListener(this);
        expPanel.add(_zFix3);
		refitUponStateChange(_zParam3, _zFix3);

        JLabel chiSqLabel3 = new JLabel("" + CHI + SQUARE);
        chiSqLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel3);
        _chiSqParam3 = new JTextField(9);
        //_chiSqParam3.setEditable(false);
        expPanel.add(_chiSqParam3);
        JLabel nullLabel3 = new JLabel("");
        expPanel.add(nullLabel3);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = _a1Param3.getBackground();
        //Color floatColor = a1Label3.getBackground();
        //_a1Param3.setBackground(floatColor);
        //_t1Param3.setBackground(floatColor);
        //_a2Param3.setBackground(floatColor);
        //_t2Param3.setBackground(floatColor);
        //_a3Param3.setBackground(floatColor);
        //_t3Param3.setBackground(floatColor);
        //_zParam3.setBackground(floatColor);
        //_chiSqParam3.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 8, 3, 4, 4, 4, 4);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        _startParam3 = new JCheckBox("Use as starting parameters for fit");
        _startParam3.setSelected(true);
        _startParam3.setEnabled(false);
		//TODO ARG 9/21/12 disabled non-functioning UI
        //panel.add("South", _startParam3);
        return panel;
    }

    /*
     * Creates panel for the stretched exponential version of the fit parameters.
     */
    private JPanel createStretchedExponentialPanel(String lifetimeLabel) {
        JPanel expPanel = new JPanel();
        expPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        expPanel.setLayout(new SpringLayout());

        JLabel a1Label4 = new JLabel("A");
        a1Label4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a1Label4);
        _aParam4 = new JTextField(9);
        //_a1Param1.setEditable(false);
        expPanel.add(_aParam4);
        _aFix4 = new JCheckBox("Fix");
        //_a1Fix1.addItemListener(this);
        expPanel.add(_aFix4);
		refitUponStateChange(_aParam4, _aFix4);

        JLabel tLabel4 = new JLabel(lifetimeLabel);
        tLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(tLabel4);
        _tParam4 = new JTextField(9);
        //_t1Param1.setEditable(false);
        expPanel.add(_tParam4);
        _tFix4 = new JCheckBox("Fix");
        //_t1Fix1.addItemListener(this);
        expPanel.add(_tFix4);
		refitUponStateChange(_tParam4, _tFix4);

        JLabel hLabel4 = new JLabel("H");
        hLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(hLabel4);
        _hParam4 = new JTextField(9);
        //_hParam4.setEditable(false);
        expPanel.add(_hParam4);
        _hFix4 = new JCheckBox("Fix");
        //_hFix4.addItemListener(this);
        expPanel.add(_hFix4);
		refitUponStateChange(_hParam4, _hFix4);

        JLabel zLabel1 = new JLabel("Z");
        zLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel1);
        _zParam4 = new JTextField(9);
        //_zParam1.setEditable(false);
        expPanel.add(_zParam4);
        _zFix4 = new JCheckBox("Fix");
        //_zFix1.addItemListener(this);
        expPanel.add(_zFix4);
		refitUponStateChange(_zParam4, _zFix4);

        JLabel chiSqLabel4 = new JLabel("" + CHI + SQUARE);
        chiSqLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel4);
        _chiSqParam4 = new JTextField(9);
        _chiSqParam4.setEditable(false);
        expPanel.add(_chiSqParam4);
        JLabel nullLabel1 = new JLabel("");
        expPanel.add(nullLabel1);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = _a1Param1.getBackground();
        //Color floatColor = a1Label1.getBackground();
        //_a1Param1.setBackground(floatColor);
        //_t1Param1.setBackground(floatColor);
        //_zParam1.setBackground(floatColor);
        //_chiSqParam1.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 5, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        _startParam4 = new JCheckBox("Use as starting parameters for fit");
        _startParam4.setSelected(true);
        _startParam4.setEnabled(false);
        //TODO ARG 9/21/12 disabled non-functioning UI
        //panel.add("South", _startParam4);
        return panel;
    }

    private Border border(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(ETCHED_BORDER, title),
                EMPTY_BORDER);
    }
    
    private void setFitButtonState(boolean on) {
        _fitButton.setText(on ? _fitButtonText : CANCEL_FIT);
    }
    
    private boolean getFitButtonState() {
        return _fitButton.getText().equals(_fitButtonText);
    }

	/**
	 * Triggers refit if dropdown list selection changes.
	 * 
	 * @param itemSelectable 
	 */
	private void refitUponStateChange(ItemSelectable itemSelectable) {
		itemSelectable.addItemListener(
			new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED
							&& null != _listener) {
						_listener.reFit();
					}
				}
			});
	}

	/**
	 * Triggers refit if text field edited.
	 * 
	 * @param textField 
	 */
	private void refitUponStateChange(final JTextField textField) {
		textField.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// trigger if just edited text
					_listener.reFit();
				}
			});
		textField.addFocusListener(
			new FocusListener() {
				private String _text;
				
				@Override
				public void focusGained(FocusEvent e) {
					_text = textField.getText();
				}
				
				@Override
				public void focusLost(FocusEvent e) {
					if (!_text.equals(textField.getText())) {
						// trigger if just edited text
						_listener.reFit();
					}
				}
			});
	}

	/**
	 * Triggers refit if fitted parameter value or checkbox change.
	 * 
	 * @param textField
	 * @param checkBox 
	 */
	private void refitUponStateChange(JTextField textField, final JCheckBox checkBox) {
		textField.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// trigger if just edited text and check box is selected.
					if (checkBox.isSelected()
							&& null != _listener) {
						_listener.reFit();
					}
				}
			});
		
		checkBox.addItemListener(
			new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					// definitely trigger if DESELECTED
					// also if SELECTED, in case text field already edited
					_listener.reFit();
				}
			});
	}

    /*
     * Disables and enables UI during and after a fit.
     *
     * @param enable
     */
    private void enableAll(boolean enable) {
        // fit algorithm settings
        _regionComboBox.setEnabled(enable);
        _algorithmComboBox.setEnabled(enable);
        _functionComboBox.setEnabled(enable);
        _noiseModelComboBox.setEnabled(enable);
        _fittedImagesComboBox.setEnabled(enable);
        _colorizeGrayScale.setEnabled(enable);
        for (JCheckBox checkBox : _analysisCheckBoxList) {
            checkBox.setEnabled(enable);
        }
        _fitAllChannels.setEnabled(enable);
        
        // cursors settings
        _transientStartField.setEditable(enable);
        _dataStartField.setEditable(enable);
        _transientStopField.setEditable(enable);
        boolean promptEnable = enable;
        if (enable) {
            // do we have a prompt?
            promptEnable = _fittingCursorHelper.getPrompt();
        }
        enablePromptCursors(promptEnable);
        _promptComboBox.setEnabled(enable);

        // fit control settings
        _xField.setEditable(enable);
        _yField.setEditable(enable);
        _thresholdField.setEditable(enable);
        _chiSqTargetField.setEditable(enable);
        _binningComboBox.setEnabled(enable);

        // single exponent fit
        _aParam1.setEditable(enable);
        _aFix1.setEnabled(enable);
        _tParam1.setEditable(enable);
        _tFix1.setEnabled(enable);
        _zParam1.setEditable(enable);
        _zFix1.setEnabled(enable);

        // double exponent fit
        _a1Param2.setEditable(enable);
        _a1Fix2.setEnabled(enable);
        _a2Param2.setEditable(enable);
        _a2Fix2.setEnabled(enable);
        _t1Param2.setEditable(enable);
        _t1Fix2.setEnabled(enable);
        _t2Param2.setEditable(enable);
        _t2Fix2.setEnabled(enable);
        _zParam2.setEditable(enable);
        _zFix2.setEnabled(enable);

        // triple exponent fit
        _a1Param3.setEditable(enable);
        _a1Fix3.setEnabled(enable);
        _a2Param3.setEditable(enable);
        _a2Fix3.setEnabled(enable);
        _a3Param3.setEditable(enable);
        _a3Fix3.setEnabled(enable);
        _t1Param3.setEditable(enable);
        _t1Fix3.setEnabled(enable);
        _t2Param3.setEditable(enable);
        _t2Fix3.setEnabled(enable);
        _t3Param3.setEditable(enable);
        _t3Fix3.setEnabled(enable);
        _zParam3.setEditable(enable);
        _zFix3.setEnabled(enable);

        // stretched exonent fit
        _aParam4.setEditable(enable);
        _aFix4.setEnabled(enable);
        _tParam4.setEditable(enable);
        _tFix4.setEnabled(enable);
        _hParam4.setEditable(enable);
        _hFix4.setEnabled(enable);
        _zParam4.setEditable(enable);
        _zFix4.setEnabled(enable);

        if (enable) {
            reconcileStartParam();
        }
    }

    public FitRegion getRegion() {
        FitRegion region = null;
        String selected = (String) _regionComboBox.getSelectedItem();
        if (selected.equals(SUM_REGION)) {
            region = FitRegion.SUMMED;
        }
        else if (selected.equals(ROIS_REGION)) {
            region = FitRegion.ROI;
        }
        else if (selected.equals(PIXEL_REGION)) {
            region = FitRegion.POINT;
        }
        else if (selected.equals(ALL_REGION)) {
            region = FitRegion.EACH;
        }
        return region;
    }

    public FitAlgorithm getAlgorithm() {
        FitAlgorithm algorithm = null;
        String selected = (String) _algorithmComboBox.getSelectedItem();
        if (selected.equals(JAOLHO_LMA_ALGORITHM)) {
            algorithm = FitAlgorithm.JAOLHO;
        }
        else if (selected.equals(SLIM_CURVE_RLD_ALGORITHM)) {
            algorithm = FitAlgorithm.SLIMCURVE_RLD;
        }
        else if (selected.equals(SLIM_CURVE_LMA_ALGORITHM)) {
            algorithm = FitAlgorithm.SLIMCURVE_LMA;
        }
        else if (selected.equals(SLIM_CURVE_RLD_LMA_ALGORITHM)) {
            algorithm = FitAlgorithm.SLIMCURVE_RLD_LMA;
        }
        return algorithm;
    }

    public FitFunction getFunction() {
        FitFunction function = null;
        String selected = (String) _functionComboBox.getSelectedItem();
        if (selected.equals(SINGLE_EXPONENTIAL)) {
            function = FitFunction.SINGLE_EXPONENTIAL;
        }
        else if (selected.equals(DOUBLE_EXPONENTIAL)) {
            function = FitFunction.DOUBLE_EXPONENTIAL;
        }
        else if (selected.equals(TRIPLE_EXPONENTIAL)) {
            function = FitFunction.TRIPLE_EXPONENTIAL;
        }
        else if (selected.equals(STRETCHED_EXPONENTIAL)) {
            function = FitFunction.STRETCHED_EXPONENTIAL;
        }
        return function;
    }

    public String[] getAnalysisList() {
        List<String> analysisList = new ArrayList<String>();
        for (JCheckBox checkBox : _analysisCheckBoxList) {
            if (checkBox.isSelected()) {
                analysisList.add(checkBox.getText());
            }
        }
        return analysisList.toArray(new String[0]);
    }

    /**
     * Get noise model for fit.
     *
     * @return
     */
    public NoiseModel getNoiseModel() {
        NoiseModel noiseModel = null;
        String selected = (String) _noiseModelComboBox.getSelectedItem();
        if (selected.equals(GAUSSIAN_FIT)) {
            noiseModel = NoiseModel.GAUSSIAN_FIT;
        }
        else if (selected.equals(POISSON_FIT)) {
            noiseModel = NoiseModel.POISSON_FIT;
        }
        else if (selected.equals(POISSON_DATA)) {
            noiseModel = NoiseModel.POISSON_DATA;
        }
        else if (selected.equals(MAXIMUM_LIKELIHOOD)) {
            noiseModel = NoiseModel.MAXIMUM_LIKELIHOOD;
        }
        return noiseModel;
    }

    /**
     * Returns list of fitted images to display.
     *
     * @return
     */
    public String getFittedImages() {
        StringBuffer returnValue = new StringBuffer();
        String selected = (String) _fittedImagesComboBox.getSelectedItem();
        //System.out.println("selected is " + selected);
        String[] fittedImages = selected.split(" ");
        for (String fittedImage : fittedImages) {
            boolean fit = true;
            if ("A".equals(fittedImage)) {
                
            }
            else if ("T".equals(fittedImage)) {
                
            }
            else if ("Z".equals(fittedImage)) {
                
            }
            if (fit) {
                returnValue.append(fittedImage);
                returnValue.append(' ');
            }
        }
        //System.out.println("changes to " + returnValue);
        return returnValue.toString();
    }

    /**
     * Returns whether to create colorized grayscale fitted images.
     * 
     * @return 
     */
    public boolean getColorizeGrayScale() {
        return _colorizeGrayScale.isSelected();
    }

    public boolean getFitAllChannels() {
        return _fitAllChannels.isSelected();
    }

    public int getX() {
        return parseInt(_xField);
    }

    public void setX(int x) {
        _xField.setText("" + x);
    }

    public int getY() {
        return parseInt(_yField);
    }

    public void setY(int y) {
        _yField.setText("" + y);
    }

    public int getThreshold() {
        return parseInt(_thresholdField);
    }

    public void setThreshold(int threshold) {
        _thresholdField.setText("" + threshold);
    }

    public String getBinning() {
        String selected = (String) _binningComboBox.getSelectedItem();
        return selected;
    }

    public double getChiSquareTarget() {
        return Double.valueOf(_chiSqTargetField.getText());
    }
    
    public void setChiSquareTarget(double chiSqTarget) {
        _chiSqTargetField.setText("" + chiSqTarget);
    }

    public int getParameterCount() {
        int count = 0;
        String function = (String) _functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            count = 4;
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            count = 6;
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            count = 8;
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            count = 5;
        }
        return count;
    }

    public void setFittedParameterCount(int count) {
        _fittedParameterCount = count;
    }

    public double[] getParameters() {
        double parameters[] = null;
        String function = (String) _functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            parameters = new double[4];
            parameters[2] = Double.valueOf(_aParam1.getText());
            parameters[3] = Double.valueOf(_tParam1.getText());
            parameters[1] = Double.valueOf(_zParam1.getText());
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            parameters = new double[6];
            parameters[2] = Double.valueOf(_a1Param2.getText());
            parameters[3] = Double.valueOf(_t1Param2.getText());
            parameters[4] = Double.valueOf(_a2Param2.getText());
            parameters[5] = Double.valueOf(_t2Param2.getText());
            parameters[1] = Double.valueOf(_zParam2.getText());
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            parameters = new double[8];
            parameters[2] = Double.valueOf(_a1Param3.getText());
            parameters[3] = Double.valueOf(_t1Param3.getText());
            parameters[4] = Double.valueOf(_a2Param3.getText());
            parameters[5] = Double.valueOf(_t2Param3.getText());
            parameters[6] = Double.valueOf(_a3Param3.getText());
            parameters[7] = Double.valueOf(_t3Param3.getText());
            parameters[1] = Double.valueOf(_zParam3.getText());
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            parameters = new double[5];
            parameters[2] = Double.valueOf(_aParam4.getText());
            parameters[3] = Double.valueOf(_tParam4.getText());
            parameters[4] = Double.valueOf(_hParam4.getText());
            parameters[1] = Double.valueOf(_zParam4.getText());
        }
        parameters[0] = 0.0;
        return parameters;
    }

    public void setParameters(double params[]) {
        String function = (String) _functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            _aParam1.setText    (paramToString(params[2], 3));
            _tParam1.setText    (paramToString(params[3], 3));
            _zParam1.setText    (paramToString(params[1], 3));
            _chiSqParam1.setText(paramToString(params[0], 6));
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            _a1Param2.setText   (paramToString(params[2], 3));
            _t1Param2.setText   (paramToString(params[3], 3));
            _a2Param2.setText   (paramToString(params[4], 3));
            _t2Param2.setText   (paramToString(params[5], 3));
            _zParam2.setText    (paramToString(params[1], 3));
            _chiSqParam2.setText(paramToString(params[0], 6));
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            _a1Param3.setText   (paramToString(params[2], 3));
            _t1Param3.setText   (paramToString(params[3], 3));
            _a2Param3.setText   (paramToString(params[4], 3));
            _t2Param3.setText   (paramToString(params[5], 3));
            _a3Param3.setText   (paramToString(params[6], 3));
            _t3Param3.setText   (paramToString(params[7], 3));
            _zParam3.setText    (paramToString(params[1], 3));
            _chiSqParam3.setText(paramToString(params[0], 6));
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            _aParam4.setText    (paramToString(params[2], 3));
            _tParam4.setText    (paramToString(params[3], 3));
            _hParam4.setText    (paramToString(params[4], 3));
            _zParam4.setText    (paramToString(params[1], 3));
            _chiSqParam4.setText(paramToString(params[0], 6));
        }
    }
    
    private String paramToString(double param, int places) {
        return "" + _fitterEstimator.roundToDecimalPlaces(param, places);
    }

    /**
     * This version is used to initialize the parameters.
     * 
     * @param function
     * @param params 
     */
    public void setFunctionParameters(int function, double params[]) {
        switch (function) {
            case 0:
                _aParam1.setText    ("" + (float) params[2]);
                _tParam1.setText    ("" + (float) params[3]);
                _zParam1.setText    ("" + (float) params[1]);
                _chiSqParam1.setText("" + (float) params[0]);
                break;
            case 1:
                _a1Param2.setText   ("" + (float) params[2]);
                _t1Param2.setText   ("" + (float) params[3]);
                _a2Param2.setText   ("" + (float) params[4]);
                _t2Param2.setText   ("" + (float) params[5]);
                _zParam2.setText    ("" + (float) params[1]);
                _chiSqParam2.setText("" + (float) params[0]);
                break;
            case 2:
                _a1Param3.setText   ("" + (float) params[2]);
                _t1Param3.setText   ("" + (float) params[3]);
                _a2Param3.setText   ("" + (float) params[4]);
                _t2Param3.setText   ("" + (float) params[5]);
                _a3Param3.setText   ("" + (float) params[6]);
                _t3Param3.setText   ("" + (float) params[7]);
                _zParam3.setText    ("" + (float) params[1]);
                _chiSqParam3.setText("" + (float) params[0]);
                break;
            case 3:
                _aParam4.setText    ("" + (float) params[0]);
                _tParam4.setText    ("" + (float) params[1]);
                _hParam4.setText    ("" + (float) params[2]);
                _zParam4.setText    ("" + (float) params[1]);
                _chiSqParam4.setText("" + (float) params[0]);
                break;
        }
    }

    public boolean[] getFree() {
        boolean free[] = null;
        String function = (String) _functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            free = new boolean[3];
            free[0] = !_aFix1.isSelected();
            free[1] = !_tFix1.isSelected();
            free[2] = !_zFix1.isSelected();
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            free = new boolean[5];
            free[0] = !_a1Fix2.isSelected();
            free[1] = !_t1Fix2.isSelected();
            free[2] = !_a2Fix2.isSelected();
            free[3] = !_t2Fix2.isSelected();
            free[4] = !_zFix2.isSelected();
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            free = new boolean[7];
            free[0] = !_a1Fix3.isSelected();
            free[1] = !_t1Fix3.isSelected();
            free[2] = !_a2Fix3.isSelected();
            free[3] = !_t2Fix3.isSelected();
            free[4] = !_a3Fix3.isSelected();
            free[5] = !_t3Fix3.isSelected();
            free[6] = !_zFix3.isSelected();

        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            free = new boolean[4];
            free[0] = !_aFix4.isSelected();
            free[1] = !_tFix4.isSelected();
            free[2] = !_hFix4.isSelected();
            free[3] = !_zFix4.isSelected();
        }
        return free;
    }

    public boolean getRefineFit() {
        JCheckBox checkBox = null;
        String function = (String) _functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            checkBox = _startParam1;
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            checkBox = _startParam2;
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            checkBox = _startParam3;
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            checkBox = _startParam4; //TODO use an array of checkboxes, etc.
        }
        return !checkBox.isSelected();
    }
    
    /**
     * Gets the transient start cursor.
     * 
     * @return 
     */
    public String getTransientStart() {
        return _transientStartField.getText();  
    }
  
    /**
     * Sets the transient start cursor.
     * 
     * @param transientStart 
     */
    public void setTransientStart(String transientStart) {
        _transientStartField.setText(transientStart);
    }
    
    /**
     * Gets the data start cursor.
     * @return 
     */ 
    public String getDataStart() {
        return _dataStartField.getText();
    }
    
    /**
     * Sets the data start cursor.
     * @return 
     */
    public void setDataStart(String dataStart) {
        _dataStartField.setText(dataStart);
    }

    /**
     * Gets the transient end cursor.
     * 
     * @return 
     */
    public String getTransientStop() {
        return _transientStopField.getText();
    }

    /**
     * Sets the transient end cursor.
     * 
     * @param transientStop 
     */
    public void setTransientStop(String transientStop) {
        _transientStopField.setText(transientStop);
    }

    /**
     * Gets the excitation delay cursor.
     * 
     * @return 
     */
    public String getPromptDelay() {
        return _promptDelayField.getText();
    }

    /**
     * Sets the excitation delay cursor.
     * 
     * @param promptStart 
     */
    public void setPromptDelay(String promptDelay) {
        _promptDelayField.setText(promptDelay);
    }

    /**
     * Gets the excitation width cursor.
     * @return 
     */
    public String getPromptWidth() {
        return _promptWidthField.getText();
    }

    /**
     * Sets the excitation width cursor.
     * 
     * @param promptWidth 
     */
    public void setPromptWidth(String promptWidth) {
        _promptWidthField.setText(promptWidth);
    }

    /**
     * Gets the excitation baseline.
     * 
     * @return 
     */
    public String getPromptBaseline() {
        return _promptBaselineField.getText();
    }

    /**
     * Sets the excitation baseline.
     * 
     * @param promptBaseline 
     */
    public void setPromptBaseline(String promptBaseline) {
        _promptBaselineField.setText(promptBaseline);
    }

    private int parseInt(JTextField field) {
        int value = 0;
        try {
            value = Integer.parseInt(field.getText());
        }
        catch (NumberFormatException e) {
            System.out.println("Error parsing " + field.getName());
        }
        return value;
    }

    /**
     * Gray out the prompt cursors if no prompt is loaded.
     * 
     * @param enable 
     */
    private void enablePromptCursors(boolean enable) {
        _promptDelayField.setEditable(enable);
        _promptWidthField.setEditable(enable);
        _promptBaselineField.setEditable(enable);
    }
    
    /**
     * This decides whether the existing parameters could be used as the
     * initial values for another fit.
     */
    private void reconcileStartParam() {
        // parameter counts happen to be unique for each fit function
        boolean enable = (_fittedParameterCount == getParameterCount());
        _startParam1.setEnabled(enable);
        _startParam2.setEnabled(enable);
        _startParam3.setEnabled(enable);
        _startParam4.setEnabled(enable);
    }

    /**
     * Gets the path and name for a prompt file.
     * 
     * @param title
     * @return 
     */
    private String getFileName(String title) {
        OpenDialog dialog = new OpenDialog(title, "");
        return dialog.getDirectory() + dialog.getFileName();
    }
}
