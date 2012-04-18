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
    
    private static final String SUM_REGION = "Sum All Pixels";
    private static final String ROIS_REGION = "Sum Each ROI";
    private static final String PIXEL_REGION = "Single Pixel";
    private static final String ALL_REGION = "Image";
    
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

    private static final String FIT_IMAGE = "Fit Image";
    private static final String FIT_PIXEL = "Fit Pixel";
    private static final String FIT_SUMMED_PIXELS = "Fit Summed Pixels";
    private static final String FIT_SUMMED_ROIS = "Fit Summed ROIs";
    private static final String CANCEL_IMAGES = "Cancel Images";

    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    private static final Border ETCHED_BORDER = BorderFactory.createEtchedBorder();

    private static final String REGION_ITEMS[] = { SUM_REGION, ROIS_REGION, PIXEL_REGION, ALL_REGION };
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
    
    private static final String SINGLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, T_X2, T };
    private static final String DOUBLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, T_X2, T, F_UPPER, F_LOWER };
    private static final String TRIPLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, T_X2, T, F_UPPER, F_LOWER };    
    private static final String STRETCHED_FITTED_IMAGE_ITEMS[] = { A_T_H_Z_X2, A_T_H_X2, A_T_H, T_H_X2, T_H, T };    
    
    private static final String EXCITATION_ITEMS[] = { EXCITATION_NONE, EXCITATION_FILE, EXCITATION_CREATE };
    
    private FittingCursorHelper _fittingCursorHelper;
    
    public IUserInterfacePanelListener m_listener;
    private boolean m_showBins = true;
    int m_fittedParameterCount = 0;

    // UI panel
    JPanel m_COMPONENT;
    JFrame m_frame;
    JPanel m_cardPanel;

    JComboBox m_regionComboBox;
    JComboBox m_algorithmComboBox;
    JComboBox m_functionComboBox;
    JComboBox m_noiseModelComboBox;
    JComboBox m_fittedImagesComboBox;
    JCheckBox m_colorizeGrayScale;
    JCheckBox[] m_analysisCheckBoxList;
    JCheckBox m_fitAllChannels;
    
    // cursor settings
    JTextField m_promptBaselineField;
    JTextField m_transientStartField;
    JTextField m_dataStartField;
    JTextField m_transientStopField;
    JTextField m_promptDelayField;
    JTextField m_promptWidthField;
    JComboBox m_promptComboBox;
    JButton m_estimateCursorsButton;
    
    // fit settings
    JTextField m_xField;
    JTextField m_yField;
    JTextField m_thresholdField;
    JTextField m_chiSqTargetField;
    JComboBox m_binningComboBox;

    // parameter panel
    JPanel m_paramPanel;
    int m_paramPanelIndex;

    // single exponent fit
    JTextField m_aParam1;
    JCheckBox m_aFix1;
    JTextField m_tParam1;
    JCheckBox m_tFix1;
    JTextField m_zParam1;
    JCheckBox m_zFix1;
    JTextField m_chiSqParam1;
    JCheckBox m_startParam1;

    // double exponent fit
    JTextField m_a1Param2;
    JCheckBox m_a1Fix2;
    JTextField m_a2Param2;
    JCheckBox m_a2Fix2;
    JTextField m_t1Param2;
    JCheckBox m_t1Fix2;
    JTextField m_t2Param2;
    JCheckBox m_t2Fix2;
    JTextField m_zParam2;
    JCheckBox m_zFix2;
    JTextField m_chiSqParam2;
    JCheckBox m_startParam2;

    // triple exponent fit
    JTextField m_a1Param3;
    JCheckBox m_a1Fix3;
    JTextField m_a2Param3;
    JCheckBox m_a2Fix3;
    JTextField m_a3Param3;
    JCheckBox m_a3Fix3;
    JTextField m_t1Param3;
    JCheckBox m_t1Fix3;
    JTextField m_t2Param3;
    JCheckBox m_t2Fix3;
    JTextField m_t3Param3;
    JCheckBox m_t3Fix3;
    JTextField m_zParam3;
    JCheckBox m_zFix3;
    JTextField m_chiSqParam3;
    JCheckBox m_startParam3;

    // stretched exonent fit
    JTextField m_aParam4;
    JCheckBox m_aFix4;
    JTextField m_tParam4;
    JCheckBox m_tFix4;
    JTextField m_hParam4;
    JCheckBox m_hFix4;
    JTextField m_zParam4;
    JCheckBox m_zFix4;
    JTextField m_chiSqParam4;
    JCheckBox m_startParam4;

    JButton m_quitButton;
    JButton m_fitButton;
    String m_fitButtonText = FIT_IMAGE;

    public UserInterfacePanel(boolean tabbed, boolean showTau,
            String[] analysisChoices, String[] binningChoices,
            FittingCursorHelper fittingCursorHelper)
    {
        String lifetimeLabel = "" + (showTau ? TAU : LAMBDA);
        
        _fittingCursorHelper = fittingCursorHelper;
        
        m_frame = new JFrame("SLIM Plugin");

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
            m_cardPanel = new JPanel(new CardLayout());
            m_cardPanel.add(createSingleExponentialPanel(lifetimeLabel), SINGLE_EXPONENTIAL);
            m_cardPanel.add(createDoubleExponentialPanel(lifetimeLabel), DOUBLE_EXPONENTIAL);
            m_cardPanel.add(createTripleExponentialPanel(lifetimeLabel), TRIPLE_EXPONENTIAL);
            m_cardPanel.add(createStretchedExponentialPanel(lifetimeLabel), STRETCHED_EXPONENTIAL);
            tabbedPane.addTab("Params", m_cardPanel);

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
            m_cardPanel = new JPanel(new CardLayout());
            m_cardPanel.add(createSingleExponentialPanel(lifetimeLabel), SINGLE_EXPONENTIAL);
            m_cardPanel.add(createDoubleExponentialPanel(lifetimeLabel), DOUBLE_EXPONENTIAL);
            m_cardPanel.add(createTripleExponentialPanel(lifetimeLabel), TRIPLE_EXPONENTIAL);
            m_cardPanel.add(createStretchedExponentialPanel(lifetimeLabel), STRETCHED_EXPONENTIAL);
            m_cardPanel.setBorder(border("Params"));
            innerPanel.add(m_cardPanel);

            outerPanel.add(innerPanel);
        }

        //Lay out the buttons from left to right.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        m_quitButton = new JButton("Quit");
        m_quitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (null != m_listener) {
                        m_listener.quit();
                    }
                }
            }
        );
        buttonPanel.add(m_quitButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        m_fitButton = new JButton(m_fitButtonText);
        m_fitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String text = (String)e.getActionCommand();
                    if (text.equals(m_fitButtonText)) {
                        enableAll(false);
                        setFitButtonState(false);
                        if (null != m_listener) {
                            m_listener.doFit();
                        }
                    }
                    else{
                        setFitButtonState(true);
                        if (null != m_listener) {
                            m_listener.cancelFit();
                        }
                    }
                }
            }
        );
        buttonPanel.add(m_fitButton);

        outerPanel.add(buttonPanel);
        m_frame.getContentPane().add(outerPanel);

        m_frame.pack();
        final Dimension preferred = m_frame.getPreferredSize();
        m_frame.setMinimumSize(preferred);
        m_frame.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        // allow horizontal but not vertical resize
                        int width = m_frame.getWidth();
                        if (width < (int) preferred.getWidth()) {
                            width = (int) preferred.getWidth();
                        }
                        m_frame.setSize(width, (int) preferred.getHeight());
                    }

        });

        // no prompt initially
        enablePromptCursors(false);

        // set up and show initial cursors
        _fittingCursorHelper.setFittingCursorUI(this);
    }

    @Override
    public JFrame getFrame() {
        return m_frame;
    }

    @Override
    public void setListener(IUserInterfacePanelListener listener) {
        m_listener = listener;
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
        m_regionComboBox = new JComboBox(REGION_ITEMS);
        m_regionComboBox.setSelectedItem(ALL_REGION);
        m_regionComboBox.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        String item = (String) e.getItem();
                        if (SUM_REGION.equals(item)) {
                            m_fitButtonText = FIT_SUMMED_PIXELS;
                        }
                        else if (ROIS_REGION.equals(item)) {
                            m_fitButtonText = FIT_SUMMED_ROIS;
                        }
                        else if (PIXEL_REGION.equals(item)) {
                            m_fitButtonText = FIT_PIXEL;
                        }
                        else if (ALL_REGION.equals(item)) {
                            m_fitButtonText = FIT_IMAGE;
                        }
                        m_fitButton.setText(m_fitButtonText);
                    }
                }
            }
        );
        fitPanel.add(m_regionComboBox);

        JLabel algorithmLabel = new JLabel("Algorithm");
        algorithmLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(algorithmLabel);
        m_algorithmComboBox = new JComboBox(ALGORITHM_ITEMS);
        m_algorithmComboBox.setSelectedItem(SLIM_CURVE_RLD_LMA_ALGORITHM);
        fitPanel.add(m_algorithmComboBox);

        JLabel functionLabel = new JLabel("Function");
        functionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(functionLabel);
        m_functionComboBox = new JComboBox(FUNCTION_ITEMS);
        m_functionComboBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        String item = (String) e.getItem();
                        CardLayout cl = (CardLayout)(m_cardPanel.getLayout());
                        cl.show(m_cardPanel, item);
                        reconcileStartParam();
                        updateFittedImagesComboBox(FUNCTION_ITEMS, item);
                    }
                }
            }
        );
        fitPanel.add(m_functionComboBox);

        JLabel noiseModelLabel = new JLabel("Noise Model");
        noiseModelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(noiseModelLabel);
        m_noiseModelComboBox = new JComboBox(NOISE_MODEL_ITEMS);
        m_noiseModelComboBox.setSelectedItem(MAXIMUM_LIKELIHOOD);
        fitPanel.add(m_noiseModelComboBox);

        JLabel fittedImagesLabel = new JLabel("Fitted Images");
        fittedImagesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(fittedImagesLabel);
        m_fittedImagesComboBox = new JComboBox(SINGLE_FITTED_IMAGE_ITEMS);
        fitPanel.add(m_fittedImagesComboBox);

        JLabel dummyLabel = new JLabel("");
        dummyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(dummyLabel);
        m_colorizeGrayScale = new JCheckBox("Colorize grayscale");
        fitPanel.add(m_colorizeGrayScale);

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
            m_analysisCheckBoxList = checkBoxList.toArray(new JCheckBox[0]);
        }
        
        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(fitPanel, 6 + choices, 2, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", fitPanel);

        m_fitAllChannels = new JCheckBox("Fit all channels");
        m_fitAllChannels.setSelected(true);

        panel.add("South", m_fitAllChannels);
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
            updateComboBox(m_fittedImagesComboBox, SINGLE_FITTED_IMAGE_ITEMS);
        }
        else if (DOUBLE_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(m_fittedImagesComboBox, DOUBLE_FITTED_IMAGE_ITEMS);            
        }
        else if (TRIPLE_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(m_fittedImagesComboBox, TRIPLE_FITTED_IMAGE_ITEMS);           
        }
        else if (STRETCHED_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(m_fittedImagesComboBox, STRETCHED_FITTED_IMAGE_ITEMS);  
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
        m_promptBaselineField = new JTextField(9);
        m_promptBaselineField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setPromptBaseline(m_promptBaselineField.getText());
            }
        });
        m_promptBaselineField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setPromptBaseline(m_promptBaselineField.getText());
            }
        });
        
        cursorPanel.add(m_promptBaselineField); 
        JLabel transStartLabel = new JLabel("Transient Start");
        transStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(transStartLabel);
        m_transientStartField = new JTextField(9);
        m_transientStartField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setTransientStart(m_transientStartField.getText());
            }
        });
        m_transientStartField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setTransientStart(m_transientStartField.getText());
            }
        });
        cursorPanel.add(m_transientStartField);
        
        JLabel dataStartLabel = new JLabel("Data Start");
        dataStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(dataStartLabel);
        m_dataStartField = new JTextField(9);
        m_dataStartField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setDataStart(m_dataStartField.getText());
            }
        });
        m_dataStartField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setDataStart(m_dataStartField.getText());
            }
        });
        cursorPanel.add(m_dataStartField);

        JLabel transStopLabel = new JLabel("Transient End");
        transStopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(transStopLabel);
        m_transientStopField = new JTextField(9);
        m_transientStopField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setTransientStop(m_transientStopField.getText());
            }
        });
        m_transientStopField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setTransientStop(m_transientStopField.getText());
            }
        });
        cursorPanel.add(m_transientStopField);

        JLabel excitationStartLabel = new JLabel("Excitation Delay");
        excitationStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(excitationStartLabel);
        m_promptDelayField = new JTextField(9);
        m_promptDelayField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setPromptDelay(m_promptDelayField.getText());
            }
        });
        m_promptDelayField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setPromptDelay(m_promptDelayField.getText());
            }
        });
        cursorPanel.add(m_promptDelayField);       
        
        JLabel excitationStopLabel = new JLabel("Excitation Width");
        excitationStopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(excitationStopLabel);
        m_promptWidthField = new JTextField(9);
        m_promptWidthField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fittingCursorHelper.setPromptWidth(m_promptWidthField.getText());
            }
        });
        m_promptWidthField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                _fittingCursorHelper.setPromptWidth(m_promptWidthField.getText());
            }
        });
        cursorPanel.add(m_promptWidthField);
        
        JLabel excitationLabel = new JLabel("Excitation");
        excitationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(excitationLabel);
        m_promptComboBox = new JComboBox(EXCITATION_ITEMS);
        m_promptComboBox.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selectedItem = (String) m_promptComboBox.getSelectedItem();
                    boolean isExcitationLoaded = false;
                    if (EXCITATION_NONE.equals(selectedItem)) {
                        if (null != m_listener) {
                            m_listener.cancelExcitation();
                        }
                    }
                    else if (EXCITATION_FILE.equals(selectedItem)) {
                        OpenDialog dialog = new OpenDialog("Load Excitation File", "");
                        String directory = dialog.getDirectory();
                        String fileName = dialog.getFileName();
                        if (null != fileName && !fileName.equals("") && null != m_listener) {
                            isExcitationLoaded = m_listener.loadExcitation(directory + fileName);
                        }
                    }
                    else if (EXCITATION_CREATE.equals(selectedItem)) {
                        SaveDialog dialog = new SaveDialog("Save Excitation File", "", "");
                        String directory = dialog.getDirectory();
                        String fileName = dialog.getFileName();
                        if (null != fileName && !fileName.equals("") && null != m_listener) {
                            isExcitationLoaded = m_listener.createExcitation(directory + fileName);
                        }
                    }

                    if (isExcitationLoaded) {
                        m_promptComboBox.setSelectedItem(EXCITATION_FILE);
                        enablePromptCursors(true);
                    }
                    else {
                        m_promptComboBox.setSelectedItem(EXCITATION_NONE);
                        String text = _fittingCursorHelper.getShowBins() ? "0" : "0.0";
                        m_promptDelayField.setText(text);
                        m_promptWidthField.setText(text);
                        m_promptBaselineField.setText("0.0");
                        enablePromptCursors(false);
                    }
                }
            }
        );
        cursorPanel.add(m_promptComboBox);
        
        JLabel dummyLabel = new JLabel("");
        dummyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cursorPanel.add(dummyLabel);
        m_estimateCursorsButton = new JButton("Estimate Cursors");
        m_estimateCursorsButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (null != m_listener) {
                        m_listener.estimateCursors();
                    }
                }
            }
        );
        cursorPanel.add(m_estimateCursorsButton);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(cursorPanel, 8, 2, 4, 4, 4, 4);

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
        m_xField = new JTextField(9);
        controlPanel.add(m_xField);

        JLabel yLabel = new JLabel("Y");
        yLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(yLabel);
        m_yField = new JTextField(9);
        controlPanel.add(m_yField);

        JLabel thresholdLabel = new JLabel("Threshold");
        thresholdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(thresholdLabel);
        m_thresholdField = new JTextField(9);
        controlPanel.add(m_thresholdField);

        JLabel chiSqTargetLabel = new JLabel(CHI_SQ_TARGET);
        chiSqTargetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(chiSqTargetLabel);
        m_chiSqTargetField = new JTextField(9);
        controlPanel.add(m_chiSqTargetField);

        JLabel binningLabel = new JLabel("Bin");
        binningLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(binningLabel);
        m_binningComboBox = new JComboBox(binningChoices);
        controlPanel.add(m_binningComboBox);

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
        m_aParam1 = new JTextField(9);
        //m_a1Param1.setEditable(false);
        expPanel.add(m_aParam1);
        m_aFix1 = new JCheckBox("Fix");
        //m_a1Fix1.addItemListener(this);
        expPanel.add(m_aFix1);

        JLabel t1Label1 = new JLabel(lifetimeLabel);
        t1Label1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label1);
        m_tParam1 = new JTextField(9);
        //m_t1Param1.setEditable(false);
        expPanel.add(m_tParam1);
        m_tFix1 = new JCheckBox("Fix");
        //m_t1Fix1.addItemListener(this);
        expPanel.add(m_tFix1);

        JLabel zLabel1 = new JLabel("Z");
        zLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel1);
        m_zParam1 = new JTextField(9);
        //m_zParam1.setEditable(false);
        expPanel.add(m_zParam1);
        m_zFix1 = new JCheckBox("Fix");
        //m_zFix1.addItemListener(this);
        expPanel.add(m_zFix1);

        JLabel chiSqLabel1 = new JLabel("" + CHI + SQUARE);
        chiSqLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel1);
        m_chiSqParam1 = new JTextField(9);
        m_chiSqParam1.setEditable(false);
        expPanel.add(m_chiSqParam1);
        JLabel nullLabel1 = new JLabel("");
        expPanel.add(nullLabel1);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = m_a1Param1.getBackground();
        //Color floatColor = a1Label1.getBackground();
        //m_a1Param1.setBackground(floatColor);
        //m_t1Param1.setBackground(floatColor);
        //m_zParam1.setBackground(floatColor);
        //m_chiSqParam1.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 4, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        m_startParam1 = new JCheckBox("Use as starting parameters for fit");
        m_startParam1.setSelected(true);
        m_startParam1.setEnabled(false);

        panel.add("South", m_startParam1);
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
        m_a1Param2 = new JTextField(9);
        //m_a1Param2.setEditable(false);
        expPanel.add(m_a1Param2);
        m_a1Fix2 = new JCheckBox("Fix");
        //m_a1Fix2.addItemListener(this);
        expPanel.add(m_a1Fix2);

        JLabel t1Label2 = new JLabel(lifetimeLabel + SUB_1);
        t1Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label2);
        m_t1Param2 = new JTextField(9);
        //m_t1Param2.setEditable(false);
        expPanel.add(m_t1Param2);
        m_t1Fix2 = new JCheckBox("Fix");
        //m_t1Fix2.addItemListener(this);
        expPanel.add(m_t1Fix2);

        JLabel a2Label2 = new JLabel("A" + SUB_2);
        a2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a2Label2);
        m_a2Param2 = new JTextField(9);
        //m_a2Param2.setEditable(false);
        expPanel.add(m_a2Param2);
        m_a2Fix2 = new JCheckBox("Fix");
        //m_a2Fix2.addItemListener(this);
        expPanel.add(m_a2Fix2);

        JLabel t2Label2 = new JLabel(lifetimeLabel + SUB_2);
        t2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t2Label2);
        m_t2Param2 = new JTextField(9);
        //m_t2Param2.setEditable(false);
        expPanel.add(m_t2Param2);
        m_t2Fix2 = new JCheckBox("Fix");
        //m_t2Fix2.addItemListener(this);
        expPanel.add(m_t2Fix2);

        JLabel zLabel2 = new JLabel("Z");
        zLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel2);
        m_zParam2 = new JTextField(9);
        //m_zParam2.setEditable(false);
        expPanel.add(m_zParam2);
        m_zFix2 = new JCheckBox("Fix");
        //m_zFix2.addItemListener(this);
        expPanel.add(m_zFix2);

        JLabel chiSqLabel2 = new JLabel("" + CHI + SQUARE);
        chiSqLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel2);
        m_chiSqParam2 = new JTextField(9);
        //m_chiSqParam2.setEditable(false);
        expPanel.add(m_chiSqParam2);
        JLabel nullLabel2 = new JLabel("");
        expPanel.add(nullLabel2);

        //TODO:
        // From SLIMPlotter
        //Color fixColor = m_a1Param2.getBackground();
        //Color floatColor = a1Label2.getBackground();
        //m_a1Param2.setBackground(floatColor);
        //m_t1Param2.setBackground(floatColor);
        //m_a2Param2.setBackground(floatColor);
        //m_t2Param2.setBackground(floatColor);
        //m_zParam2.setBackground(floatColor);
        //m_chiSqParam2.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 6, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        m_startParam2 = new JCheckBox("Use as starting parameters for fit");
        m_startParam2.setSelected(true);
        m_startParam2.setEnabled(false);
        panel.add("South", m_startParam2);
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
        m_a1Param3 = new JTextField(9);
        //m_a1Param3.setEditable(false);
        expPanel.add(m_a1Param3);
        m_a1Fix3 = new JCheckBox("Fix");
        //m_a1Fix3.addItemListener(this);
        expPanel.add(m_a1Fix3);

        JLabel t1Label3 = new JLabel(lifetimeLabel + SUB_1);
        t1Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label3);
        m_t1Param3 = new JTextField(9);
        //m_t1Param3.setEditable(false);
        expPanel.add(m_t1Param3);
        m_t1Fix3 = new JCheckBox("Fix");
        //m_t1Fix3.addItemListener(this);
        expPanel.add(m_t1Fix3);

        JLabel a2Label3 = new JLabel("A" + SUB_2);
        a2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a2Label3);
        m_a2Param3 = new JTextField(9);
        //m_a2Param3.setEditable(false);
        expPanel.add(m_a2Param3);
        m_a2Fix3 = new JCheckBox("Fix");
        //m_a2Fix3.addItemListener(this);
        expPanel.add(m_a2Fix3);

        JLabel t2Label3 = new JLabel(lifetimeLabel + SUB_2);
        t2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t2Label3);
        m_t2Param3 = new JTextField(9);
        //m_t2Param3.setEditable(false);
        expPanel.add(m_t2Param3);
        m_t2Fix3 = new JCheckBox("Fix");
        //m_t2Fix3.addItemListener(this);
        expPanel.add(m_t2Fix3);

        JLabel a3Label3 = new JLabel("A" + SUB_3);
        a3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a3Label3);
        m_a3Param3 = new JTextField(9);
        //m_a3Param3.setEditable(false);
        expPanel.add(m_a3Param3);
        m_a3Fix3 = new JCheckBox("Fix");
        //m_a3Fix3.addItemListener(this);
        expPanel.add(m_a3Fix3);

        JLabel t3Label3 = new JLabel(lifetimeLabel + SUB_3);
        t3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t3Label3);
        m_t3Param3 = new JTextField(9);
        //m_t3Param3.setEditable(false);
        expPanel.add(m_t3Param3);
        m_t3Fix3 = new JCheckBox("Fix");
        //m_t3Fix3.addItemListener(this);
        expPanel.add(m_t3Fix3);

        JLabel zLabel3 = new JLabel("Z");
        zLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel3);
        m_zParam3 = new JTextField(9);
        //m_zParam3.setEditable(false);
        expPanel.add(m_zParam3);
        m_zFix3 = new JCheckBox("Fix");
        //m_zFix3.addItemListener(this);
        expPanel.add(m_zFix3);


        JLabel chiSqLabel3 = new JLabel("" + CHI + SQUARE);
        chiSqLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel3);
        m_chiSqParam3 = new JTextField(9);
        //m_chiSqParam3.setEditable(false);
        expPanel.add(m_chiSqParam3);
        JLabel nullLabel3 = new JLabel("");
        expPanel.add(nullLabel3);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = m_a1Param3.getBackground();
        //Color floatColor = a1Label3.getBackground();
        //m_a1Param3.setBackground(floatColor);
        //m_t1Param3.setBackground(floatColor);
        //m_a2Param3.setBackground(floatColor);
        //m_t2Param3.setBackground(floatColor);
        //m_a3Param3.setBackground(floatColor);
        //m_t3Param3.setBackground(floatColor);
        //m_zParam3.setBackground(floatColor);
        //m_chiSqParam3.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 8, 3, 4, 4, 4, 4);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        m_startParam3 = new JCheckBox("Use as starting parameters for fit");
        m_startParam3.setSelected(true);
        m_startParam3.setEnabled(false);
        panel.add("South", m_startParam3);
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
        m_aParam4 = new JTextField(9);
        //m_a1Param1.setEditable(false);
        expPanel.add(m_aParam4);
        m_aFix4 = new JCheckBox("Fix");
        //m_a1Fix1.addItemListener(this);
        expPanel.add(m_aFix4);

        JLabel tLabel4 = new JLabel(lifetimeLabel);
        tLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(tLabel4);
        m_tParam4 = new JTextField(9);
        //m_t1Param1.setEditable(false);
        expPanel.add(m_tParam4);
        m_tFix4 = new JCheckBox("Fix");
        //m_t1Fix1.addItemListener(this);
        expPanel.add(m_tFix4);

        JLabel hLabel4 = new JLabel("H");
        hLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(hLabel4);
        m_hParam4 = new JTextField(9);
        //m_hParam4.setEditable(false);
        expPanel.add(m_hParam4);
        m_hFix4 = new JCheckBox("Fix");
        //m_hFix4.addItemListener(this);
        expPanel.add(m_hFix4);

        JLabel zLabel1 = new JLabel("Z");
        zLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel1);
        m_zParam4 = new JTextField(9);
        //m_zParam1.setEditable(false);
        expPanel.add(m_zParam4);
        m_zFix4 = new JCheckBox("Fix");
        //m_zFix1.addItemListener(this);
        expPanel.add(m_zFix4);

        JLabel chiSqLabel4 = new JLabel("" + CHI + SQUARE);
        chiSqLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel4);
        m_chiSqParam4 = new JTextField(9);
        m_chiSqParam4.setEditable(false);
        expPanel.add(m_chiSqParam4);
        JLabel nullLabel1 = new JLabel("");
        expPanel.add(nullLabel1);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = m_a1Param1.getBackground();
        //Color floatColor = a1Label1.getBackground();
        //m_a1Param1.setBackground(floatColor);
        //m_t1Param1.setBackground(floatColor);
        //m_zParam1.setBackground(floatColor);
        //m_chiSqParam1.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 5, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        m_startParam4 = new JCheckBox("Use as starting parameters for fit");
        m_startParam4.setSelected(true);
        m_startParam4.setEnabled(false);

        panel.add("South", m_startParam4);
        return panel;
    }

    private Border border(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(ETCHED_BORDER, title),
                EMPTY_BORDER);
    }
    
    private void setFitButtonState(boolean on) {
        m_fitButton.setText(on ? m_fitButtonText : CANCEL_IMAGES);
    }
    
    private boolean getFitButtonState() {
        return m_fitButton.getText().equals(m_fitButtonText);
    }

    /*
     * Disables and enables UI during and after a fit.
     *
     * @param enable
     */
    private void enableAll(boolean enable) {
        // fit algorithm settings
        m_regionComboBox.setEnabled(enable);
        m_algorithmComboBox.setEnabled(enable);
        m_functionComboBox.setEnabled(enable);
        m_noiseModelComboBox.setEnabled(enable);
        m_fittedImagesComboBox.setEnabled(enable);
        m_colorizeGrayScale.setEnabled(enable);
        for (JCheckBox checkBox : m_analysisCheckBoxList) {
            checkBox.setEnabled(enable);
        }
        m_fitAllChannels.setEnabled(enable);
        
        // cursors settings
        m_transientStartField.setEditable(enable);
        m_dataStartField.setEditable(enable);
        m_transientStopField.setEditable(enable);
        boolean promptEnable = enable;
        if (enable) {
            // do we have a prompt?
            promptEnable = _fittingCursorHelper.getPrompt();
        }
        enablePromptCursors(promptEnable);
        m_promptComboBox.setEnabled(enable);

        // fit control settings
        m_xField.setEditable(enable);
        m_yField.setEditable(enable);
        m_thresholdField.setEditable(enable);
        m_chiSqTargetField.setEditable(enable);
        m_binningComboBox.setEnabled(enable);

        // single exponent fit
        m_aParam1.setEditable(enable);
        m_aFix1.setEnabled(enable);
        m_tParam1.setEditable(enable);
        m_tFix1.setEnabled(enable);
        m_zParam1.setEditable(enable);
        m_zFix1.setEnabled(enable);

        // double exponent fit
        m_a1Param2.setEditable(enable);
        m_a1Fix2.setEnabled(enable);
        m_a2Param2.setEditable(enable);
        m_a2Fix2.setEnabled(enable);
        m_t1Param2.setEditable(enable);
        m_t1Fix2.setEnabled(enable);
        m_t2Param2.setEditable(enable);
        m_t2Fix2.setEnabled(enable);
        m_zParam2.setEditable(enable);
        m_zFix2.setEnabled(enable);

        // triple exponent fit
        m_a1Param3.setEditable(enable);
        m_a1Fix3.setEnabled(enable);
        m_a2Param3.setEditable(enable);
        m_a2Fix3.setEnabled(enable);
        m_a3Param3.setEditable(enable);
        m_a3Fix3.setEnabled(enable);
        m_t1Param3.setEditable(enable);
        m_t1Fix3.setEnabled(enable);
        m_t2Param3.setEditable(enable);
        m_t2Fix3.setEnabled(enable);
        m_t3Param3.setEditable(enable);
        m_t3Fix3.setEnabled(enable);
        m_zParam3.setEditable(enable);
        m_zFix3.setEnabled(enable);

        // stretched exonent fit
        m_aParam4.setEditable(enable);
        m_aFix4.setEnabled(enable);
        m_tParam4.setEditable(enable);
        m_tFix4.setEnabled(enable);
        m_hParam4.setEditable(enable);
        m_hFix4.setEnabled(enable);
        m_zParam4.setEditable(enable);
        m_zFix4.setEnabled(enable);

        if (enable) {
            reconcileStartParam();
        }
    }

    public FitRegion getRegion() {
        FitRegion region = null;
        String selected = (String) m_regionComboBox.getSelectedItem();
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
        String selected = (String) m_algorithmComboBox.getSelectedItem();
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
        String selected = (String) m_functionComboBox.getSelectedItem();
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
        for (JCheckBox checkBox : m_analysisCheckBoxList) {
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
        String selected = (String) m_noiseModelComboBox.getSelectedItem();
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
        String selected = (String) m_fittedImagesComboBox.getSelectedItem();
        System.out.println("selected is " + selected);
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
        System.out.println("changes to " + returnValue);
        return returnValue.toString();
    }

    /**
     * Returns whether to create colorized grayscale fitted images.
     * 
     * @return 
     */
    public boolean getColorizeGrayScale() {
        return m_colorizeGrayScale.isSelected();
    }

    public boolean getFitAllChannels() {
        return m_fitAllChannels.isSelected();
    }

    public int getX() {
        return parseInt(m_xField);
    }

    public void setX(int x) {
        m_xField.setText("" + x);
    }

    public int getY() {
        return parseInt(m_yField);
    }

    public void setY(int y) {
        m_yField.setText("" + y);
    }

    public int getThreshold() {
        return parseInt(m_thresholdField);
    }

    public void setThreshold(int threshold) {
        m_thresholdField.setText("" + threshold);
    }

    public String getBinning() {
        String selected = (String) m_binningComboBox.getSelectedItem();
        return selected;
    }

    public double getChiSquareTarget() {
        return Double.valueOf(m_chiSqTargetField.getText());
    }
    
    public void setChiSquareTarget(double chiSqTarget) {
        m_chiSqTargetField.setText("" + chiSqTarget);
    }

    public int getParameterCount() {
        int count = 0;
        String function = (String) m_functionComboBox.getSelectedItem();
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
        m_fittedParameterCount = count;
    }

    public double[] getParameters() {
        double parameters[] = null;
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            parameters = new double[4];
            parameters[2] = Double.valueOf(m_aParam1.getText());
            parameters[3] = Double.valueOf(m_tParam1.getText());
            parameters[1] = Double.valueOf(m_zParam1.getText());
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            parameters = new double[6];
            parameters[2] = Double.valueOf(m_a1Param2.getText());
            parameters[3] = Double.valueOf(m_t1Param2.getText());
            parameters[4] = Double.valueOf(m_a2Param2.getText());
            parameters[5] = Double.valueOf(m_t2Param2.getText());
            parameters[1] = Double.valueOf(m_zParam2.getText());
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            parameters = new double[8];
            parameters[2] = Double.valueOf(m_a1Param3.getText());
            parameters[3] = Double.valueOf(m_t1Param3.getText());
            parameters[4] = Double.valueOf(m_a2Param3.getText());
            parameters[5] = Double.valueOf(m_t2Param3.getText());
            parameters[6] = Double.valueOf(m_a3Param3.getText());
            parameters[7] = Double.valueOf(m_t3Param3.getText());
            parameters[1] = Double.valueOf(m_zParam3.getText());
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            parameters = new double[5];
            parameters[2] = Double.valueOf(m_aParam4.getText());
            parameters[3] = Double.valueOf(m_tParam4.getText());
            parameters[4] = Double.valueOf(m_hParam4.getText());
            parameters[1] = Double.valueOf(m_zParam4.getText());
        }
        parameters[0] = 0.0;
        return parameters;
    }

    public void setParameters(double params[]) {
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            m_aParam1.setText    ("" + (float) params[2]);
            m_tParam1.setText    ("" + (float) params[3]);
            m_zParam1.setText    ("" + (float) params[1]);
            m_chiSqParam1.setText("" + (float) params[0]);
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            m_a1Param2.setText   ("" + (float) params[2]);
            m_t1Param2.setText   ("" + (float) params[3]);
            m_a2Param2.setText   ("" + (float) params[4]);
            m_t2Param2.setText   ("" + (float) params[5]);
            m_zParam2.setText    ("" + (float) params[1]);
            m_chiSqParam2.setText("" + (float) params[0]);
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            m_a1Param3.setText   ("" + (float) params[2]);
            m_t1Param3.setText   ("" + (float) params[3]);
            m_a2Param3.setText   ("" + (float) params[4]);
            m_t2Param3.setText   ("" + (float) params[5]);
            m_a3Param3.setText   ("" + (float) params[6]);
            m_t3Param3.setText   ("" + (float) params[7]);
            m_zParam3.setText    ("" + (float) params[1]);
            m_chiSqParam3.setText("" + (float) params[0]);
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            m_aParam4.setText    ("" + (float) params[2]);
            m_tParam4.setText    ("" + (float) params[3]);
            m_hParam4.setText    ("" + (float) params[4]);
            m_zParam4.setText    ("" + (float) params[1]);
            m_chiSqParam4.setText("" + (float) params[0]);
        }
    }

    public void setFunctionParameters(int function, double params[]) {
        switch (function) {
            case 0:
                m_aParam1.setText    ("" + (float) params[2]);
                m_tParam1.setText    ("" + (float) params[3]);
                m_zParam1.setText    ("" + (float) params[1]);
                m_chiSqParam1.setText("" + (float) params[0]);
                break;
            case 1:
                m_a1Param2.setText   ("" + (float) params[2]);
                m_t1Param2.setText   ("" + (float) params[3]);
                m_a2Param2.setText   ("" + (float) params[4]);
                m_t2Param2.setText   ("" + (float) params[5]);
                m_zParam2.setText    ("" + (float) params[1]);
                m_chiSqParam2.setText("" + (float) params[0]);
                break;
            case 2:
                m_a1Param3.setText   ("" + (float) params[2]);
                m_t1Param3.setText   ("" + (float) params[3]);
                m_a2Param3.setText   ("" + (float) params[4]);
                m_t2Param3.setText   ("" + (float) params[5]);
                m_a3Param3.setText   ("" + (float) params[6]);
                m_t3Param3.setText   ("" + (float) params[7]);
                m_zParam3.setText    ("" + (float) params[1]);
                m_chiSqParam3.setText("" + (float) params[0]);
                break;
            case 3:
                m_aParam4.setText    ("" + (float) params[0]);
                m_tParam4.setText    ("" + (float) params[1]);
                m_hParam4.setText    ("" + (float) params[2]);
                m_zParam4.setText    ("" + (float) params[1]);
                m_chiSqParam4.setText("" + (float) params[0]);
                break;
        }
    }

    public boolean[] getFree() {
        boolean free[] = null;
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            free = new boolean[3];
            free[0] = !m_aFix1.isSelected();
            free[1] = !m_tFix1.isSelected();
            free[2] = !m_zFix1.isSelected();
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            free = new boolean[5];
            free[0] = !m_a1Fix2.isSelected();
            free[1] = !m_t1Fix2.isSelected();
            free[2] = !m_a2Fix2.isSelected();
            free[3] = !m_t2Fix2.isSelected();
            free[4] = !m_zFix2.isSelected();
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            free = new boolean[7];
            free[0] = !m_a1Fix3.isSelected();
            free[1] = !m_t1Fix3.isSelected();
            free[2] = !m_a2Fix3.isSelected();
            free[3] = !m_t2Fix3.isSelected();
            free[4] = !m_a3Fix3.isSelected();
            free[5] = !m_t3Fix3.isSelected();
            free[6] = !m_zFix3.isSelected();

        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            free = new boolean[4];
            free[0] = !m_aFix4.isSelected();
            free[1] = !m_tFix4.isSelected();
            free[2] = !m_hFix4.isSelected();
            free[3] = !m_zFix4.isSelected();
        }
        return free;
    }

    public boolean getRefineFit() {
        JCheckBox checkBox = null;
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            checkBox = m_startParam1;
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            checkBox = m_startParam2;
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            checkBox = m_startParam3;
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            checkBox = m_startParam4; //TODO use an array of checkboxes, etc.
        }
        return !checkBox.isSelected();
    }
    
    /**
     * Gets the transient start cursor.
     * 
     * @return 
     */
    public String getTransientStart() {
        return m_transientStartField.getText();  
    }
  
    /**
     * Sets the transient start cursor.
     * 
     * @param transientStart 
     */
    public void setTransientStart(String transientStart) {
        m_transientStartField.setText(transientStart);
    }
    
    /**
     * Gets the data start cursor.
     * @return 
     */ 
    public String getDataStart() {
        return m_dataStartField.getText();
    }
    
    /**
     * Sets the data start cursor.
     * @return 
     */
    public void setDataStart(String dataStart) {
        m_dataStartField.setText(dataStart);
    }

    /**
     * Gets the transient end cursor.
     * 
     * @return 
     */
    public String getTransientStop() {
        return m_transientStopField.getText();
    }

    /**
     * Sets the transient end cursor.
     * 
     * @param transientStop 
     */
    public void setTransientStop(String transientStop) {
        m_transientStopField.setText(transientStop);
    }

    /**
     * Gets the excitation delay cursor.
     * 
     * @return 
     */
    public String getPromptDelay() {
        return m_promptDelayField.getText();
    }

    /**
     * Sets the excitation delay cursor.
     * 
     * @param promptStart 
     */
    public void setPromptDelay(String promptDelay) {
        m_promptDelayField.setText(promptDelay);
    }

    /**
     * Gets the excitation width cursor.
     * @return 
     */
    public String getPromptWidth() {
        return m_promptWidthField.getText();
    }

    /**
     * Sets the excitation width cursor.
     * 
     * @param promptWidth 
     */
    public void setPromptWidth(String promptWidth) {
        m_promptWidthField.setText(promptWidth);
    }

    /**
     * Gets the excitation baseline.
     * 
     * @return 
     */
    public String getPromptBaseline() {
        return m_promptBaselineField.getText();
    }

    /**
     * Sets the excitation baseline.
     * 
     * @param promptBaseline 
     */
    public void setPromptBaseline(String promptBaseline) {
        m_promptBaselineField.setText(promptBaseline);
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
        m_promptDelayField.setEditable(enable);
        m_promptWidthField.setEditable(enable);
        m_promptBaselineField.setEditable(enable);
    }
    
    /**
     * This decides whether the existing parameters could be used as the
     * initial values for another fit.
     */
    private void reconcileStartParam() {
        // parameter counts happen to be unique for each fit function
        boolean enable = (m_fittedParameterCount == getParameterCount());
        m_startParam1.setEnabled(enable);
        m_startParam2.setEnabled(enable);
        m_startParam3.setEnabled(enable);
        m_startParam4.setEnabled(enable);
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
