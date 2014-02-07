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

package loci.slim2.process.interactive.ui;

import ij.io.OpenDialog;
import ij.io.SaveDialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import loci.curvefitter.ICurveFitter;
import loci.curvefitter.IFitterEstimator;
import loci.slim2.process.interactive.cursor.FittingCursor;
import loci.slim2.process.interactive.cursor.FittingCursorListener;

/**
 * Default implementation of main UI panel.
 * 
 * @author Aivar Grislis
 */
public class DefaultUserInterfacePanel implements UserInterfacePanel {
	private static final String TITLE = "SLIM Plugin";
    
    private static final String SUM_REGION = "Sum All Pixels",
                                ROIS_REGION = "Sum Each ROI",
                                PIXEL_REGION = "Single Pixel",
                                ALL_REGION = "Images";
    
    private static final String JAOLHO_LMA_ALGORITHM = "Jaolho LMA",
                                SLIM_CURVE_RLD_ALGORITHM = "SLIMCurve RLD",
                                SLIM_CURVE_LMA_ALGORITHM = "SLIMCurve LMA",
                                SLIM_CURVE_RLD_LMA_ALGORITHM = "SLIMCurve RLD+LMA";

    private static final String SINGLE_EXPONENTIAL = "Single Exponential",
                                DOUBLE_EXPONENTIAL = "Double Exponential",
                                TRIPLE_EXPONENTIAL = "Triple Exponential",
                                STRETCHED_EXPONENTIAL = "Stretched Exponential";

    private static final String GAUSSIAN_FIT = "Gaussian Fit",
                                POISSON_FIT = "Poisson Fit",
                                POISSON_DATA = "Poisson Data",
                                MAXIMUM_LIKELIHOOD = "Max. Likelihood Est.";

    private static final String CHI_SQ_TARGET = "" + CHI + SQUARE + SUB_R + " Target";

    private static final String EXCITATION_NONE = "None",
                                EXCITATION_FILE = "Load from File",
                                EXCITATION_CREATE = "Use current X Y",
			                    EXCITATION_ESTIMATE = "Estimate from current X Y",
								EXCITATION_GAUSSIAN = "Gaussian";

    private static final String FIT_IMAGE = "Fit Images",
                                FIT_PIXEL = "Fit Pixel",
                                FIT_SUMMED_PIXELS = "Fit Summed Pixels",
                                FIT_SUMMED_ROIS = "Fit Summed ROIs",
                                CANCEL_FIT = "Cancel Fit";

    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(10, 10, 10, 10),
                                ETCHED_BORDER = BorderFactory.createEtchedBorder();

	//TODO ARG fitting a series of ROIs is broken, so omit that possibility, for now:
    private static final String REGION_ITEMS[] = { SUM_REGION, /*ROIS_REGION,*/ PIXEL_REGION, ALL_REGION },
                                ALGORITHM_ITEMS[] = { JAOLHO_LMA_ALGORITHM, SLIM_CURVE_RLD_ALGORITHM, SLIM_CURVE_LMA_ALGORITHM, SLIM_CURVE_RLD_LMA_ALGORITHM },
                                FUNCTION_ITEMS[] = { SINGLE_EXPONENTIAL, DOUBLE_EXPONENTIAL, TRIPLE_EXPONENTIAL, STRETCHED_EXPONENTIAL },
                                NOISE_MODEL_ITEMS[] = { GAUSSIAN_FIT, POISSON_FIT, POISSON_DATA, MAXIMUM_LIKELIHOOD };
	
    private static final String A_T_Z_X2 = "A " + TAU_CHAR + " Z " + CHISQUARE,
                                A_T_X2 = "A " + TAU_CHAR + " " + CHISQUARE,
                                A_T = "A " + TAU_CHAR,
								F_UPPER_T_Z_X2 = F_UPPER + TAU_CHAR + " Z " + CHISQUARE,
								F_UPPER_T_X2 = F_UPPER + TAU_CHAR + " " + CHISQUARE,
								F_UPPER_T = F_UPPER + TAU_CHAR,
								F_LOWER_T_Z_X2 = F_LOWER + TAU_CHAR + " Z " + CHISQUARE,
								F_LOWER_T_X2 = F_LOWER + TAU_CHAR + " " + CHISQUARE,
			                    F_LOWER_T = F_LOWER + TAU_CHAR,
			                    T_X2 = TAU_CHAR + " " + CHISQUARE,
                                T = "" + TAU_CHAR,
								TAU_MEAN_X2 = TAU_MEAN + CHISQUARE,
								A_T_H_Z_X2 = "A " + TAU_CHAR + " H Z " + CHISQUARE,
                                A_T_H_X2 = "A " + TAU_CHAR + " H " + CHISQUARE,
                                A_T_H = "A " + TAU_CHAR + " H",
                                T_H_X2 = TAU_CHAR + " H " + CHISQUARE,
                                T_H = TAU_CHAR + " H",
								NONE = " ";
	
	private static final String FITTING_ERROR = "Fitting Error",
			                    NO_FIT = "--";
    
    private static final String SINGLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, T_X2, T, NONE },
                                DOUBLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, F_UPPER_T_Z_X2, F_UPPER_T_X2, F_UPPER_T, F_LOWER_T_Z_X2, F_LOWER_T_X2, F_LOWER_T, T_X2, T, TAU_MEAN_X2, TAU_MEAN, NONE },
                                TRIPLE_FITTED_IMAGE_ITEMS[] = { A_T_Z_X2, A_T_X2, A_T, F_UPPER_T_Z_X2, F_UPPER_T_X2, F_UPPER_T, F_LOWER_T_Z_X2, F_LOWER_T_X2, F_LOWER_T, T_X2, T, TAU_MEAN_X2, TAU_MEAN, NONE },    
                                STRETCHED_FITTED_IMAGE_ITEMS[] = { A_T_H_Z_X2, A_T_H_X2, A_T_H, T_H_X2, T_H, T, NONE };    
    
    private static final String EXCITATION_ITEMS[] = { EXCITATION_NONE, EXCITATION_FILE, EXCITATION_CREATE, EXCITATION_ESTIMATE, EXCITATION_GAUSSIAN };
	
	private static final int X_VALUE = 0,
							 X_MAX = 64000,
							 X_MIN = 0,
							 X_INC = 1;
	
	private static final int Y_VALUE = 0,
							 Y_MAX = 64000,
							 Y_MIN = 0,
							 Y_INC = 1;
	
	private static final int THRESH_VALUE = 0,
							 THRESH_MAX = 64000,
							 THRESH_MIN = 0,
							 THRESH_INC = 1;
	
	private static final double CHISQ_VALUE = 1.5,
								CHISQ_MAX = 5.0,
								CHISQ_MIN = 1.0,
								CHISQ_INC = 0.25;
	
	private static final double SCATTER_VALUE = 0.0,
								SCATTER_MAX = 2.0,
								SCATTER_MIN = 0.0,
								SCATTER_INC = 0.001;
	
	private static final String CURSOR_TIME = "Time",
	                            CURSOR_BINS = "Bins";
	
    private FittingCursor fittingCursor;
	private FittingCursorListener fittingCursorListener;
    private IFitterEstimator fitterEstimator;
	private int maxBin;
	private double xInc;
    
    private UserInterfacePanelListener listener;
	private ThresholdUpdate thresholdListener;
    int fittedParameterCount = 0;
	String promptSelection;
	volatile boolean promptSelectionLock;

    // UI panel
    JPanel COMPONENT;
    JFrame frame;
    JPanel cardPanel;

    JComboBox regionComboBox;
    JComboBox algorithmComboBox;
    JComboBox functionComboBox;
    JComboBox noiseModelComboBox;
    JComboBox fittedImagesComboBox;
    JCheckBox colorizeGrayScale;
    JCheckBox[] analysisCheckBoxList;
    JCheckBox fitAllChannels;
    
	// cursor settings
	//   model A - time increments
	JSpinner promptBaselineSpinnerA;
	JSpinner transientStartSpinnerA;
	JSpinner dataStartSpinnerA;
	JSpinner transientStopSpinnerA;
	JSpinner promptDelaySpinnerA;
	JSpinner promptWidthSpinnerA;
	//  model B - bin index increments
	JSpinner promptBaselineSpinnerB;
	JSpinner transientStartSpinnerB;
	JSpinner dataStartSpinnerB;
	JSpinner transientStopSpinnerB;
	JSpinner promptDelaySpinnerB;
	JSpinner promptWidthSpinnerB;
    JCheckBox showBins;
    JComboBox promptComboBox;
    JButton estimateCursorsButton;
	
    // fit settings
	JSpinner xSpinner;
	JSpinner ySpinner;
	JSpinner thresholdMinSpinner;
	JSpinner thresholdMaxSpinner;
	JSpinner chiSqTargetSpinner;
    JComboBox binningComboBox;
	JSpinner scatterSpinner; // scatter experiment

    // parameter panel
    JPanel paramPanel;
    int paramPanelIndex;
	boolean noFit;

    // single exponential fit
    JTextField aParam1;
    JCheckBox aFix1;
    JTextField tParam1;
    JCheckBox tFix1;
    JTextField zParam1;
    JCheckBox zFix1;
    JTextField chiSqParam1;
	JTextField AICParam1;
	JLabel errorLabel1;
    JCheckBox startParam1;

    // double exponential fit
    JTextField a1Param2;
    JCheckBox a1Fix2;
    JTextField a2Param2;
    JCheckBox a2Fix2;
    JTextField t1Param2;
    JCheckBox t1Fix2;
    JTextField t2Param2;
    JCheckBox t2Fix2;
    JTextField zParam2;
    JCheckBox zFix2;
    JTextField chiSqParam2;
	JTextField AICParam2;
	JLabel errorLabel2;
    JCheckBox startParam2;

    // triple exponential fit
    JTextField a1Param3;
    JCheckBox a1Fix3;
    JTextField a2Param3;
    JCheckBox a2Fix3;
    JTextField a3Param3;
    JCheckBox a3Fix3;
    JTextField t1Param3;
    JCheckBox t1Fix3;
    JTextField t2Param3;
    JCheckBox t2Fix3;
    JTextField t3Param3;
    JCheckBox t3Fix3;
    JTextField zParam3;
    JCheckBox zFix3;
    JTextField chiSqParam3;
	JTextField AICParam3;
	JLabel errorLabel3;
    JCheckBox startParam3;

    // stretched exponential fit
    JTextField aParam4;
    JCheckBox aFix4;
    JTextField tParam4;
    JCheckBox tFix4;
    JTextField hParam4;
    JCheckBox hFix4;
    JTextField zParam4;
    JCheckBox zFix4;
    JTextField chiSqParam4;
	JTextField AICParam4;
	JLabel errorLabel4;
    JCheckBox startParam4;

	JButton openButton;
    JButton quitButton;
    JButton fitButton;
    String fitButtonText = FIT_IMAGE;

    public DefaultUserInterfacePanel(boolean tabbed, boolean showTau,
			int maxBin, double xInc,
            String[] analysisChoices, String[] binningChoices,
            FittingCursor fittingCursor,
            IFitterEstimator fitterEstimator)
    {
        String lifetimeLabel = "" + (showTau ? TAU_CHAR : LAMBDA);
        
        this.fittingCursor = fittingCursor;
        this.fitterEstimator = fitterEstimator;
		this.maxBin = maxBin;
		this.xInc = xInc;
        
        frame = new JFrame(TITLE);

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
            cardPanel = new JPanel(new CardLayout());
            cardPanel.add(createSingleExponentialPanel(lifetimeLabel), SINGLE_EXPONENTIAL);
            cardPanel.add(createDoubleExponentialPanel(lifetimeLabel), DOUBLE_EXPONENTIAL);
            cardPanel.add(createTripleExponentialPanel(lifetimeLabel), TRIPLE_EXPONENTIAL);
            cardPanel.add(createStretchedExponentialPanel(lifetimeLabel), STRETCHED_EXPONENTIAL);
            tabbedPane.addTab("Params", cardPanel);

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
            cursorPanel.setBorder(border("Cursors & Excitation"));
            innerPanel.add(cursorPanel); 

            JPanel controlPanel = createControlPanel(binningChoices);
            controlPanel.setBorder(border("Control"));
            innerPanel.add(controlPanel);

            // Create cards and the panel that contains the cards
            cardPanel = new JPanel(new CardLayout());
            cardPanel.add(createSingleExponentialPanel(lifetimeLabel), SINGLE_EXPONENTIAL);
            cardPanel.add(createDoubleExponentialPanel(lifetimeLabel), DOUBLE_EXPONENTIAL);
            cardPanel.add(createTripleExponentialPanel(lifetimeLabel), TRIPLE_EXPONENTIAL);
            cardPanel.add(createStretchedExponentialPanel(lifetimeLabel), STRETCHED_EXPONENTIAL);
            cardPanel.setBorder(border("Params"));
            innerPanel.add(cardPanel);

            outerPanel.add(innerPanel);
        }

        //Lay out the buttons from left to right.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
		openButton = new JButton("New File/Batch");
		openButton.addActionListener(
		    new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (null != listener) {
						listener.openFile();
					}
				}
		    }
		);
		buttonPanel.add(openButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        quitButton = new JButton("Quit");
        quitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (null != listener) {
                        listener.quit();
                    }
                }
            }
        );
        buttonPanel.add(quitButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        fitButton = new JButton(fitButtonText);
        fitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
					boolean summed;
					System.out.println("fitbutton event e " + e);
					if (null != listener) {
						switch (getRegion()) {
							case EACH:
								System.out.println("region is each");
								if (getFitButtonState()) {
									System.out.println("getFBS was true; fit");
									enableAll(false);
									setFitButtonState(false);
									listener.fitImages();
								}
								else {
									System.out.println("getFBS was false; cancel");
									enableAll(true);
									setFitButtonState(true);
									listener.cancelFit();
								}
								break;
							case POINT:
								summed = false;
								listener.fitSingleDecay(summed);
								break;
							case ROI:
								break;
							case SUMMED:
								summed = true;
								listener.fitSingleDecay(summed);
								break;
						}
					}
                }
            }
        );
        buttonPanel.add(fitButton);

        outerPanel.add(buttonPanel);
        frame.getContentPane().add(outerPanel);

        frame.pack();
        final Dimension preferred = frame.getPreferredSize();
        frame.setMinimumSize(preferred);
        frame.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        // allow horizontal but not vertical resize
                        int width = frame.getWidth();
                        if (width < (int) preferred.getWidth()) {
                            width = (int) preferred.getWidth();
                        }
                        frame.setSize(width, (int) preferred.getHeight());
                    }

        });

        // no prompt initially
        enablePromptCursors(false);

        // set up and show initial cursors
		fittingCursorListener = new FittingCursorListenerImpl();
		fittingCursor.addListener(fittingCursorListener);
    }

    @Override
    public JFrame getFrame() {
        return frame;
    }

    @Override
    public void setListener(UserInterfacePanelListener listener) {
        this.listener = listener;
    }
	
	public void setThresholdListener(ThresholdUpdate thresholdListener) {
		this.thresholdListener = thresholdListener;
	}
	
	public void disable() {
		enableAll(false);
	}

    @Override
    public void reset() {
        enableAll(true);
        setFitButtonState(true);
    }
	
	public void disableButtons() {
		enableButtons(false);
	}
	
	public void resetButtons() {
		enableButtons(true);
	}

    private JPanel createFitPanel(String[] analysisChoices) {
        JPanel fitPanel = new JPanel();
        fitPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        fitPanel.setLayout(new SpringLayout());

        JLabel regionLabel = new JLabel("Region");
        regionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(regionLabel);
		//TODO all these comboboxes are problematical:
		//  if you click on the containing window & move it the combo list
		//  doesn't close and remains active & drawn in original spot.
		// See:
		// http://stackoverflow.com/questions/10982273/swt-awt-new-frame-jcombobox-never-lose-focus-when-window-is-moved
		// (but in that case problem was mixing SWT/AWT/Swing; this is just a JComboBox in a JPanel in a JTabbedPane.)
        regionComboBox = new JComboBox(REGION_ITEMS);
        regionComboBox.setSelectedItem(ALL_REGION);
        regionComboBox.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        String item = (String) e.getItem();
                        if (SUM_REGION.equals(item)) {
                            fitButtonText = FIT_SUMMED_PIXELS;
                        }
                        else if (ROIS_REGION.equals(item)) {
                            fitButtonText = FIT_SUMMED_ROIS;
                        }
                        else if (PIXEL_REGION.equals(item)) {
                            fitButtonText = FIT_PIXEL;
                        }
                        else if (ALL_REGION.equals(item)) {
                            fitButtonText = FIT_IMAGE;
                        }
                        fitButton.setText(fitButtonText);
                    }
                }
            }
        );
        fitPanel.add(regionComboBox);

        JLabel algorithmLabel = new JLabel("Algorithm");
        algorithmLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(algorithmLabel);
        algorithmComboBox = new JComboBox(ALGORITHM_ITEMS);
        algorithmComboBox.setSelectedItem(SLIM_CURVE_RLD_LMA_ALGORITHM);
		refitUponStateChange(algorithmComboBox);
        fitPanel.add(algorithmComboBox);

        JLabel functionLabel = new JLabel("Function");
        functionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(functionLabel);
        functionComboBox = new JComboBox(FUNCTION_ITEMS);
        functionComboBox.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        String item = (String) e.getItem();
                        CardLayout cl = (CardLayout)(cardPanel.getLayout());
                        cl.show(cardPanel, item);
                        reconcileStartParam();
                        updateFittedImagesComboBox(FUNCTION_ITEMS, item);
                    }
                }
            }
        );
		refitUponStateChange(functionComboBox);
        fitPanel.add(functionComboBox);

        JLabel noiseModelLabel = new JLabel("Noise Model");
        noiseModelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(noiseModelLabel);
        noiseModelComboBox = new JComboBox(NOISE_MODEL_ITEMS);
        noiseModelComboBox.setSelectedItem(MAXIMUM_LIKELIHOOD);
		refitUponStateChange(noiseModelComboBox);
        fitPanel.add(noiseModelComboBox);

        JLabel fittedImagesLabel = new JLabel("Fitted Images");
        fittedImagesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(fittedImagesLabel);
        fittedImagesComboBox = new JComboBox(SINGLE_FITTED_IMAGE_ITEMS);
        fitPanel.add(fittedImagesComboBox);

        JLabel dummyLabel = new JLabel("");
        dummyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(dummyLabel);
        colorizeGrayScale = new JCheckBox("Colorize grayscale");
        fitPanel.add(colorizeGrayScale);

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
            analysisCheckBoxList = checkBoxList.toArray(new JCheckBox[0]);
        }
        
        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(fitPanel, 6 + choices, 2, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", fitPanel);

        fitAllChannels = new JCheckBox("Fit all channels");
        fitAllChannels.setSelected(true);

        panel.add("South", fitAllChannels);
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
            updateComboBox(fittedImagesComboBox, SINGLE_FITTED_IMAGE_ITEMS);
        }
        else if (DOUBLE_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(fittedImagesComboBox, DOUBLE_FITTED_IMAGE_ITEMS);            
        }
        else if (TRIPLE_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(fittedImagesComboBox, TRIPLE_FITTED_IMAGE_ITEMS);           
        }
        else if (STRETCHED_EXPONENTIAL.equals(selectedItem)) {
            updateComboBox(fittedImagesComboBox, STRETCHED_FITTED_IMAGE_ITEMS);  
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
        cursorPanel.setLayout(new BoxLayout(cursorPanel, BoxLayout.Y_AXIS));

		final CardLayout cursorCardLayout = new CardLayout();
		final JPanel cardsPanel = new JPanel(cursorCardLayout);
		JPanel subPanelA = createCursorSubPanelA();
		cardsPanel.add(subPanelA, CURSOR_TIME);
		JPanel subPanelB = createCursorSubPanelB();
		cardsPanel.add(subPanelB, CURSOR_BINS);
		cursorPanel.add(cardsPanel);
		
		JPanel lowerPanel = new JPanel(new SpringLayout());
        JLabel dummyLabel = new JLabel("");
        dummyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lowerPanel.add(dummyLabel);
        showBins = new JCheckBox("Display as indices");
        showBins.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    boolean showBins = e.getStateChange() == ItemEvent.SELECTED;
					//TODO 4/2/13
					/**
					 * I tried having two models, one for bins and one for time
					 * values and swapping them here.  Crashes when one model has
					 * integer values and the other doubles.  So, switch to doubles
					 * for all, but still doesn't work.  Same thing having a single
					 * model that gets different min/max/inc.  In 'bins' mode I
					 * could never get the spinner to work, or even type in new
					 * values.
					 * 
					 * Therefore, for now, just disable these spinners altogether
					 * when in 'bins' mode.
					 */
					System.out.println("showBins is " + showBins);
					
					cursorCardLayout.show(cardsPanel, showBins ? CURSOR_BINS : CURSOR_TIME);
					
                    fittingCursor.setShowBins(showBins);
                }
            }
        );
		//TODO 4/2/13
		// Swapping models doesn't work correctly.
		// Failing that, I could either remove the JSpinner from cursor fields or else
		// just disable the model swap.
		// Let's go with the latter; we lose the ability to show the underlying bins
		// unfortunately.
        lowerPanel.add(showBins);
        
        JLabel excitationLabel = new JLabel("Excitation");
        excitationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lowerPanel.add(excitationLabel);
        promptComboBox = new JComboBox(EXCITATION_ITEMS);
        promptComboBox.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final String selectedItem = (String) promptComboBox.getSelectedItem();
					if (null == promptSelection || promptSelection != selectedItem) {
						promptSelection = selectedItem;
						if (!promptSelectionLock) {
							promptSelectionLock = true;
							SwingUtilities.invokeLater(
								new Runnable() {
									@Override
									public void run() {
										updatePrompt(selectedItem);
									}
								}
							);
						}
					}
				}
            }
        );
        lowerPanel.add(promptComboBox);
        
        JLabel dummyLabel2 = new JLabel("");
        dummyLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        lowerPanel.add(dummyLabel2);
        estimateCursorsButton = new JButton("Estimate Cursors");
        estimateCursorsButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (null != listener) {
                        listener.estimateCursors();
                    }
                }
            }
        );
        lowerPanel.add(estimateCursorsButton);
		
        // rows, cols, initX, initY, xPad, yPad
		SpringUtilities.makeCompactGrid(lowerPanel, 3, 2, 4, 4, 4, 4);
		
		cursorPanel.add(lowerPanel);
		
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", cursorPanel);

        return panel;
    }
	
	private JPanel createCursorSubPanelA() {
        JPanel subPanelA = new JPanel();
        //subPanelB.setBorder(new EmptyBorder(0, 0, 8, 8));
        subPanelA.setLayout(new SpringLayout());

        // emulating TRI2 cursor listing order here
		JLabel excitationBaselineLabel = new JLabel("Excitation Baseline");
        excitationBaselineLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelA.add(excitationBaselineLabel);
		SpinnerNumberModel promptBaselineModel = new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1);
		promptBaselineSpinnerA = new JSpinner(promptBaselineModel);
		promptBaselineSpinnerA.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setPromptBaselineValue((Double) promptBaselineSpinnerA.getValue());
			}
		});
		subPanelA.add(promptBaselineSpinnerA);

		JLabel transStartLabel = new JLabel("Transient Start");
        transStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelA.add(transStartLabel);
		SpinnerNumberModel transientStartModel = new SpinnerNumberModel(0.0, 0.0, maxBin * xInc, xInc);
		transientStartSpinnerA = new JSpinner(transientStartModel);
		transientStartSpinnerA.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setTransientStartTime((Double) transientStartSpinnerA.getValue());
			}
		});
		subPanelA.add(transientStartSpinnerA);
		
        JLabel dataStartLabel = new JLabel("Data Start");
        dataStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelA.add(dataStartLabel);
		SpinnerNumberModel dataStartModel = new SpinnerNumberModel(0.0, 0.0, maxBin * xInc, xInc);
		dataStartSpinnerA = new JSpinner(dataStartModel);
		dataStartSpinnerA.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setDataStartTime((Double) dataStartSpinnerA.getValue());
			}
		});
		subPanelA.add(dataStartSpinnerA);
		
        JLabel transStopLabel = new JLabel("Transient End");
        transStopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelA.add(transStopLabel);
		SpinnerNumberModel transientStopModel = new SpinnerNumberModel(0.0, 0.0, maxBin * xInc, xInc);
		transientStopSpinnerA = new JSpinner(transientStopModel);
		transientStopSpinnerA.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setTransientStopTime((Double) transientStopSpinnerA.getValue());
			}
		});
		subPanelA.add(transientStopSpinnerA);
		
        JLabel excitationStartLabel = new JLabel("Excitation Delay");
        excitationStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelA.add(excitationStartLabel);
		SpinnerNumberModel promptDelayModel = new SpinnerNumberModel(0.0, -maxBin * xInc, maxBin * xInc, xInc);
		promptDelaySpinnerA = new JSpinner(promptDelayModel);
		promptDelaySpinnerA.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setPromptDelayTime((Double) promptDelaySpinnerA.getValue());
			}
		});
		subPanelA.add(promptDelaySpinnerA);
		
        JLabel excitationStopLabel = new JLabel("Excitation Width");
        excitationStopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelA.add(excitationStopLabel);
		SpinnerNumberModel promptWidthModel = new SpinnerNumberModel(0.0, 0.0, maxBin * xInc, xInc);
		promptWidthSpinnerA = new JSpinner(promptWidthModel);
		promptWidthSpinnerA.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setPromptWidthTime((Double) promptWidthSpinnerA.getValue());
			}
		});
		subPanelA.add(promptWidthSpinnerA);
		
        // rows, cols, initX, initY, xPad, yPad
		SpringUtilities.makeCompactGrid(subPanelA, 6, 2, 4, 4, 4, 4);
		
		return subPanelA;
	}

	private JPanel createCursorSubPanelB() {
        JPanel subPanelB = new JPanel();
        //subPanelA.setBorder(new EmptyBorder(0, 0, 8, 8));
        subPanelB.setLayout(new SpringLayout());

        // emulating TRI2 cursor listing order here
		JLabel excitationBaselineLabel = new JLabel("Excitation Baseline");
        excitationBaselineLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelB.add(excitationBaselineLabel);
		SpinnerNumberModel promptBaselineModel = new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1);
		promptBaselineSpinnerB = new JSpinner(promptBaselineModel);
		promptBaselineSpinnerB.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setPromptBaselineValue((Double) promptBaselineSpinnerB.getValue());
			}
		});
		subPanelB.add(promptBaselineSpinnerB);

		JLabel transStartLabel = new JLabel("Transient Start");
        transStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelB.add(transStartLabel);
		SpinnerNumberModel transientStartModel = new SpinnerNumberModel(0, 0, maxBin, 1);
		transientStartSpinnerB = new JSpinner(transientStartModel);
		transientStartSpinnerB.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setTransientStartIndex((Integer) transientStartSpinnerB.getValue());
			}
		});
		subPanelB.add(transientStartSpinnerB);
		
        JLabel dataStartLabel = new JLabel("Data Start");
        dataStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelB.add(dataStartLabel);
		SpinnerNumberModel dataStartModel = new SpinnerNumberModel(0, 0, maxBin, 1);
		dataStartSpinnerB = new JSpinner(dataStartModel);
		dataStartSpinnerB.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setDataStartIndex((Integer) dataStartSpinnerB.getValue());
			}
		});
		subPanelB.add(dataStartSpinnerB);
		
        JLabel transStopLabel = new JLabel("Transient End");
        transStopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelB.add(transStopLabel);
		SpinnerNumberModel transientStopModel = new SpinnerNumberModel(0, 0, maxBin, 1);
		transientStopSpinnerB = new JSpinner(transientStopModel);
		transientStopSpinnerB.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setTransientStopIndex((Integer) transientStopSpinnerB.getValue());
			}
		});
		subPanelB.add(transientStopSpinnerB);
		
        JLabel excitationStartLabel = new JLabel("Excitation Delay");
        excitationStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelB.add(excitationStartLabel);
		SpinnerNumberModel promptDelayModel = new SpinnerNumberModel(0, -maxBin, maxBin, 1);
		promptDelaySpinnerB = new JSpinner(promptDelayModel);
		promptDelaySpinnerB.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setPromptDelayIndex((Integer) promptDelaySpinnerB.getValue());
			}
		});
		subPanelB.add(promptDelaySpinnerB);
		
        JLabel excitationStopLabel = new JLabel("Excitation Width");
        excitationStopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        subPanelB.add(excitationStopLabel);
		SpinnerNumberModel promptWidthModel = new SpinnerNumberModel(0, 0, maxBin, 1);
		promptWidthSpinnerB = new JSpinner(promptWidthModel);
		promptWidthSpinnerB.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fittingCursor.setPromptWidthIndex((Integer) promptWidthSpinnerB.getValue());
			}
		});
		subPanelB.add(promptWidthSpinnerB);
		
        // rows, cols, initX, initY, xPad, yPad
		SpringUtilities.makeCompactGrid(subPanelB, 6, 2, 4, 4, 4, 4);
		
		return subPanelB;
	}
	
	/**
	 * Update prompt based on new selection.
	 * 
	 * @param selectedItem 
	 */
	private void updatePrompt(String selectedItem) {
		boolean isExcitationLoaded = false;
		if (EXCITATION_FILE.equals(selectedItem)) {
			OpenDialog dialog = new OpenDialog("Load Excitation File", "");
			String directory = dialog.getDirectory();
			String fileName = dialog.getFileName();
			if (null != fileName && !fileName.equals("") && null != listener) {
				isExcitationLoaded = listener.loadExcitation(directory + fileName);
			}
		}
		else if (EXCITATION_CREATE.equals(selectedItem)) {
			SaveDialog dialog = new SaveDialog("Save Excitation File", "", "");
			String directory = dialog.getDirectory();
			String fileName = dialog.getFileName();
			if (null != fileName && !fileName.equals("") && null != listener) {
				isExcitationLoaded = listener.createExcitation(directory + fileName);
			}
		}
		else if (EXCITATION_ESTIMATE.equals(selectedItem)) {
			SaveDialog dialog = new SaveDialog("Save Excitation File", "", "");
			String directory = dialog.getDirectory();
			String fileName = dialog.getFileName();
			if (null != fileName && !fileName.equals("") && null != listener) {
				isExcitationLoaded = listener.estimateExcitation(directory + fileName);
			}
		}
		else if (EXCITATION_GAUSSIAN.equals(selectedItem)) {
			SaveDialog dialog = new SaveDialog("Save Excitation File", "", "");
			String directory = dialog.getDirectory();
			String fileName = dialog.getFileName();
			if (null != fileName && !fileName.equals("") && null != listener) {
				isExcitationLoaded = listener.gaussianExcitation(directory + fileName);
			}
		}
			
		if (isExcitationLoaded) {
			promptComboBox.setSelectedItem(EXCITATION_FILE);
			enablePromptCursors(true);
		}
		else {
			promptComboBox.setSelectedItem(EXCITATION_NONE);
			promptDelaySpinnerA.setValue(0.0);
			promptDelaySpinnerB.setValue(0);
			promptWidthSpinnerA.setValue(0.0);
			promptWidthSpinnerB.setValue(0);
			promptBaselineSpinnerA.setValue(0.0);
			promptBaselineSpinnerB.setValue(0.0); // baseline is double in both cases
			enablePromptCursors(false);
			if (null != listener) {
				listener.cancelExcitation();
			}
		}
		boolean summed = false;
		listener.fitSingleDecay(summed);
		
		// done
		promptSelectionLock = false;
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
		xSpinner = new JSpinner(new SpinnerNumberModel(X_VALUE, X_MIN, X_MAX, X_INC));
		refitUponStateChange(xSpinner);
        controlPanel.add(xSpinner);

        JLabel yLabel = new JLabel("Y");
        yLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(yLabel);
		ySpinner = new JSpinner(new SpinnerNumberModel(Y_VALUE, Y_MIN, Y_MAX, Y_INC));
		refitUponStateChange(ySpinner);
        controlPanel.add(ySpinner);

        JLabel thresholdMinLabel = new JLabel("Threshold Min");
        thresholdMinLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(thresholdMinLabel);
		thresholdMinSpinner = new JSpinner(new SpinnerNumberModel(THRESH_VALUE, THRESH_MIN, THRESH_MAX, THRESH_INC));
		updateThresholdChange(thresholdMinSpinner);
        controlPanel.add(thresholdMinSpinner);

        JLabel thresholdMaxLabel = new JLabel("Threshold Max");
        thresholdMaxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(thresholdMaxLabel);
		thresholdMaxSpinner = new JSpinner(new SpinnerNumberModel(THRESH_VALUE, THRESH_MIN, THRESH_MAX, THRESH_INC));
		updateThresholdChange(thresholdMaxSpinner);
        controlPanel.add(thresholdMaxSpinner);
		
		JLabel chiSqTargetLabel = new JLabel(CHI_SQ_TARGET);
        chiSqTargetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(chiSqTargetLabel);
		chiSqTargetSpinner = new JSpinner(new SpinnerNumberModel(CHISQ_VALUE, CHISQ_MIN, CHISQ_MAX, CHISQ_INC));
		refitUponStateChange(chiSqTargetSpinner);
		controlPanel.add(chiSqTargetSpinner);

        JLabel binningLabel = new JLabel("Bin");
        binningLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(binningLabel);
        binningComboBox = new JComboBox(binningChoices);
		refitUponStateChange(binningComboBox);
        controlPanel.add(binningComboBox);
		
		JLabel scatterLabel = new JLabel("Scatter"); //SCATTER
		scatterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		controlPanel.add(scatterLabel);
		scatterSpinner = new JSpinner();
		// see http://implementsblog.com/2012/11/26/java-gotcha-jspinner-preferred-size/
		Dimension size = scatterSpinner.getPreferredSize();
		SpinnerNumberModel model = new SpinnerNumberModel(SCATTER_VALUE, SCATTER_MIN, SCATTER_MAX, SCATTER_INC);
		scatterSpinner.setModel(model);
		scatterSpinner.setPreferredSize(size);
		refitUponStateChange(scatterSpinner);
		controlPanel.add(scatterSpinner);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(controlPanel, 7, 2, 4, 4, 4, 4); //SCATTER 5 -> 6

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", controlPanel);

        return panel;
    }
	
	//TODO the fitted parameter UI could have a JSpinner that only shows up when
	// you fix a parameter.
	// See: https://forums.oracle.com/forums/thread.jspa?threadID=1357061
	// It's quite hacky though.

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
        aParam1 = new JTextField(9);
        //a1Param1.setEditable(false);
        expPanel.add(aParam1);
        aFix1 = new JCheckBox("Fix");
        //a1Fix1.addItemListener(this);
        expPanel.add(aFix1);
		refitUponStateChange(aParam1, aFix1);

        JLabel t1Label1 = new JLabel(lifetimeLabel);
        t1Label1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label1);
        tParam1 = new JTextField(9);
        //t1Param1.setEditable(false);
        expPanel.add(tParam1);
        tFix1 = new JCheckBox("Fix");
        //t1Fix1.addItemListener(this);
        expPanel.add(tFix1);
		refitUponStateChange(tParam1, tFix1);

        JLabel zLabel1 = new JLabel("Z");
        zLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel1);
        zParam1 = new JTextField(9);
        //zParam1.setEditable(false);
        expPanel.add(zParam1);
        zFix1 = new JCheckBox("Fix");
        //zFix1.addItemListener(this);
        expPanel.add(zFix1);
		refitUponStateChange(zParam1, zFix1);

        JLabel chiSqLabel1 = new JLabel("" + CHI + SQUARE + SUB_R);
        chiSqLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel1);
        chiSqParam1 = new JTextField(9);
        chiSqParam1.setEditable(false);
        expPanel.add(chiSqParam1);
        JLabel nullLabel1 = new JLabel("");
        expPanel.add(nullLabel1);
		
		JLabel AICLabel1 = new JLabel("AIC");
		AICLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
		expPanel.add(AICLabel1);
		AICParam1 = new JTextField(9);
		AICParam1.setEditable(false);
		expPanel.add(AICParam1);
		JLabel nullLabel2 = new JLabel("");
		expPanel.add(nullLabel2);
		
		JLabel nullLabel3 = new JLabel("");
		expPanel.add(nullLabel3);
		errorLabel1 = new JLabel(FITTING_ERROR);
		errorLabel1.setVisible(false);
		expPanel.add(errorLabel1);
		JLabel nullLabel4 = new JLabel("");
		expPanel.add(nullLabel4);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = a1Param1.getBackground();
        //Color floatColor = a1Label1.getBackground();
        //a1Param1.setBackground(floatColor);
        //t1Param1.setBackground(floatColor);
        //zParam1.setBackground(floatColor);
        //chiSqParam1.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 6, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        startParam1 = new JCheckBox("Use as starting parameters for fit");
        startParam1.setSelected(true);
        startParam1.setEnabled(false);

		//TODO ARG 9/21/12 disabled non-functioning UI
        //panel.add("South", startParam1);
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
        a1Param2 = new JTextField(9);
        //a1Param2.setEditable(false);
        expPanel.add(a1Param2);
        a1Fix2 = new JCheckBox("Fix");
        //a1Fix2.addItemListener(this);
        expPanel.add(a1Fix2);
		refitUponStateChange(a1Param2, a1Fix2);

        JLabel t1Label2 = new JLabel(lifetimeLabel + SUB_1);
        t1Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label2);
        t1Param2 = new JTextField(9);
        //t1Param2.setEditable(false);
        expPanel.add(t1Param2);
        t1Fix2 = new JCheckBox("Fix");
        //t1Fix2.addItemListener(this);
        expPanel.add(t1Fix2);
		refitUponStateChange(t1Param2, t1Fix2);

        JLabel a2Label2 = new JLabel("A" + SUB_2);
        a2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a2Label2);
        a2Param2 = new JTextField(9);
        //a2Param2.setEditable(false);
        expPanel.add(a2Param2);
        a2Fix2 = new JCheckBox("Fix");
        //a2Fix2.addItemListener(this);
        expPanel.add(a2Fix2);
		refitUponStateChange(a2Param2, a2Fix2);

        JLabel t2Label2 = new JLabel(lifetimeLabel + SUB_2);
        t2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t2Label2);
        t2Param2 = new JTextField(9);
        //t2Param2.setEditable(false);
        expPanel.add(t2Param2);
        t2Fix2 = new JCheckBox("Fix");
        //t2Fix2.addItemListener(this);
        expPanel.add(t2Fix2);
		refitUponStateChange(t2Param2, t2Fix2);

        JLabel zLabel2 = new JLabel("Z");
        zLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel2);
        zParam2 = new JTextField(9);
        //zParam2.setEditable(false);
        expPanel.add(zParam2);
        zFix2 = new JCheckBox("Fix");
        //zFix2.addItemListener(this);
        expPanel.add(zFix2);
		refitUponStateChange(zParam2, zFix2);

        JLabel chiSqLabel2 = new JLabel("" + CHI + SQUARE + SUB_R);
        chiSqLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel2);
        chiSqParam2 = new JTextField(9);
        chiSqParam2.setEditable(false);
        expPanel.add(chiSqParam2);
        JLabel nullLabel2 = new JLabel("");
        expPanel.add(nullLabel2);
		
		JLabel AICLabel2 = new JLabel("AIC");
		AICLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
		expPanel.add(AICLabel2);
		AICParam2 = new JTextField(9);
		AICParam2.setEditable(false);
		expPanel.add(AICParam2);
		JLabel nullLabel3 = new JLabel("");
		expPanel.add(nullLabel3);

		JLabel nullLabel4 = new JLabel("");
		expPanel.add(nullLabel4);
		errorLabel2 = new JLabel(FITTING_ERROR);
		errorLabel2.setVisible(false);
		expPanel.add(errorLabel2);
		JLabel nullLabel5 = new JLabel("");
		expPanel.add(nullLabel5);

        //TODO:
        // From SLIMPlotter
        //Color fixColor = a1Param2.getBackground();
        //Color floatColor = a1Label2.getBackground();
        //a1Param2.setBackground(floatColor);
        //t1Param2.setBackground(floatColor);
        //a2Param2.setBackground(floatColor);
        //t2Param2.setBackground(floatColor);
        //zParam2.setBackground(floatColor);
        //chiSqParam2.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 8, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        startParam2 = new JCheckBox("Use as starting parameters for fit");
        startParam2.setSelected(true);
        startParam2.setEnabled(false);
		//TODO ARG 9/21/12 disabled non-functioning UI
        //panel.add("South", startParam2);
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
        a1Param3 = new JTextField(9);
        //a1Param3.setEditable(false);
        expPanel.add(a1Param3);
        a1Fix3 = new JCheckBox("Fix");
        //a1Fix3.addItemListener(this);
        expPanel.add(a1Fix3);
		refitUponStateChange(a1Param3, a1Fix3);

        JLabel t1Label3 = new JLabel(lifetimeLabel + SUB_1);
        t1Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label3);
        t1Param3 = new JTextField(9);
        //t1Param3.setEditable(false);
        expPanel.add(t1Param3);
        t1Fix3 = new JCheckBox("Fix");
        //t1Fix3.addItemListener(this);
        expPanel.add(t1Fix3);
		refitUponStateChange(t1Param3, t1Fix3);

        JLabel a2Label3 = new JLabel("A" + SUB_2);
        a2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a2Label3);
        a2Param3 = new JTextField(9);
        //a2Param3.setEditable(false);
        expPanel.add(a2Param3);
        a2Fix3 = new JCheckBox("Fix");
        //a2Fix3.addItemListener(this);
        expPanel.add(a2Fix3);
		refitUponStateChange(a2Param3, a2Fix3);

        JLabel t2Label3 = new JLabel(lifetimeLabel + SUB_2);
        t2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t2Label3);
        t2Param3 = new JTextField(9);
        //t2Param3.setEditable(false);
        expPanel.add(t2Param3);
        t2Fix3 = new JCheckBox("Fix");
        //t2Fix3.addItemListener(this);
        expPanel.add(t2Fix3);
		refitUponStateChange(t2Param3, t2Fix3);

        JLabel a3Label3 = new JLabel("A" + SUB_3);
        a3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a3Label3);
        a3Param3 = new JTextField(9);
        //a3Param3.setEditable(false);
        expPanel.add(a3Param3);
        a3Fix3 = new JCheckBox("Fix");
        //a3Fix3.addItemListener(this);
        expPanel.add(a3Fix3);
		refitUponStateChange(a3Param3, a3Fix3);

        JLabel t3Label3 = new JLabel(lifetimeLabel + SUB_3);
        t3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t3Label3);
        t3Param3 = new JTextField(9);
        //t3Param3.setEditable(false);
        expPanel.add(t3Param3);
        t3Fix3 = new JCheckBox("Fix");
        //t3Fix3.addItemListener(this);
        expPanel.add(t3Fix3);
		refitUponStateChange(t3Param3, t3Fix3);

        JLabel zLabel3 = new JLabel("Z");
        zLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel3);
        zParam3 = new JTextField(9);
        //zParam3.setEditable(false);
        expPanel.add(zParam3);
        zFix3 = new JCheckBox("Fix");
        //zFix3.addItemListener(this);
        expPanel.add(zFix3);
		refitUponStateChange(zParam3, zFix3);

        JLabel chiSqLabel3 = new JLabel("" + CHI + SQUARE + SUB_R);
        chiSqLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel3);
        chiSqParam3 = new JTextField(9);
        chiSqParam3.setEditable(false);
        expPanel.add(chiSqParam3);
        JLabel nullLabel3 = new JLabel("");
        expPanel.add(nullLabel3);
		
		JLabel AICLabel3 = new JLabel("AIC");
		AICLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
		expPanel.add(AICLabel3);
		AICParam3 = new JTextField(9);
		AICParam3.setEditable(false);
		expPanel.add(AICParam3);
		JLabel nullLabel4 = new JLabel("");
		expPanel.add(nullLabel4);
		
		JLabel nullLabel5 = new JLabel("");
		expPanel.add(nullLabel5);
		errorLabel3 = new JLabel(FITTING_ERROR);
		errorLabel3.setVisible(false);
		expPanel.add(errorLabel3);
		JLabel nullLabel6 = new JLabel("");
		expPanel.add(nullLabel6);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = a1Param3.getBackground();
        //Color floatColor = a1Label3.getBackground();
        //a1Param3.setBackground(floatColor);
        //t1Param3.setBackground(floatColor);
        //a2Param3.setBackground(floatColor);
        //t2Param3.setBackground(floatColor);
        //a3Param3.setBackground(floatColor);
        //t3Param3.setBackground(floatColor);
        //zParam3.setBackground(floatColor);
        //chiSqParam3.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 10, 3, 4, 4, 4, 4);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        startParam3 = new JCheckBox("Use as starting parameters for fit");
        startParam3.setSelected(true);
        startParam3.setEnabled(false);
		//TODO ARG 9/21/12 disabled non-functioning UI
        //panel.add("South", startParam3);
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
        aParam4 = new JTextField(9);
        //a1Param1.setEditable(false);
        expPanel.add(aParam4);
        aFix4 = new JCheckBox("Fix");
        //a1Fix1.addItemListener(this);
        expPanel.add(aFix4);
		refitUponStateChange(aParam4, aFix4);

        JLabel tLabel4 = new JLabel(lifetimeLabel);
        tLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(tLabel4);
        tParam4 = new JTextField(9);
        //t1Param1.setEditable(false);
        expPanel.add(tParam4);
        tFix4 = new JCheckBox("Fix");
        //t1Fix1.addItemListener(this);
        expPanel.add(tFix4);
		refitUponStateChange(tParam4, tFix4);

        JLabel hLabel4 = new JLabel("H");
        hLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(hLabel4);
        hParam4 = new JTextField(9);
        //hParam4.setEditable(false);
        expPanel.add(hParam4);
        hFix4 = new JCheckBox("Fix");
        //hFix4.addItemListener(this);
        expPanel.add(hFix4);
		refitUponStateChange(hParam4, hFix4);

        JLabel zLabel1 = new JLabel("Z");
        zLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel1);
        zParam4 = new JTextField(9);
        //zParam1.setEditable(false);
        expPanel.add(zParam4);
        zFix4 = new JCheckBox("Fix");
        //zFix1.addItemListener(this);
        expPanel.add(zFix4);
		refitUponStateChange(zParam4, zFix4);

        JLabel chiSqLabel4 = new JLabel("" + CHI + SQUARE + SUB_R);
        chiSqLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel4);
        chiSqParam4 = new JTextField(9);
        chiSqParam4.setEditable(false);
        expPanel.add(chiSqParam4);
        JLabel nullLabel4 = new JLabel("");
        expPanel.add(nullLabel4);
		
		JLabel AICLabel4 = new JLabel("AIC");
		AICLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
		expPanel.add(AICLabel4);
		AICParam4 = new JTextField(9);
		AICParam4.setEditable(false);
		expPanel.add(AICParam4);
		JLabel nullLabel5 = new JLabel("");
		expPanel.add(nullLabel5);
		
		JLabel nullLabel6 = new JLabel("");
		expPanel.add(nullLabel6);
		errorLabel4 = new JLabel(FITTING_ERROR);
		errorLabel4.setVisible(false);
		expPanel.add(errorLabel4);
		JLabel nullLabel7 = new JLabel("");
		expPanel.add(nullLabel7);

        //TODO:
        // SLIMPlotter look & feel:
        //Color fixColor = a1Param1.getBackground();
        //Color floatColor = a1Label1.getBackground();
        //a1Param1.setBackground(floatColor);
        //t1Param1.setBackground(floatColor);
        //zParam1.setBackground(floatColor);
        //chiSqParam1.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 7, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        startParam4 = new JCheckBox("Use as starting parameters for fit");
        startParam4.setSelected(true);
        startParam4.setEnabled(false);
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
        fitButton.setText(on ? fitButtonText : CANCEL_FIT);
    }
    
    private boolean getFitButtonState() {
        return fitButton.getText().equals(fitButtonText);
    }

	/**
	 * Triggers fitSingleDecay if drop-down list selection changes.
	 * 
	 * @param itemSelectable 
	 */
	private void refitUponStateChange(ItemSelectable itemSelectable) {
		itemSelectable.addItemListener(
			new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED
							&& null != listener) {
						listener.fitSingleDecay(fitSummed());
					}
				}
			});
	}

	/**
	 * Triggers fitSingleDecay if text field edited.
	 * 
	 * @param textField 
	 */
	private void refitUponStateChange(final JTextField textField) {
		textField.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// trigger if just edited text
					listener.fitSingleDecay(fitSummed());
				}
			});
		textField.addFocusListener(
			new FocusListener() {
				private String text;
				
				@Override
				public void focusGained(FocusEvent e) {
					text = textField.getText();
				}
				
				@Override
				public void focusLost(FocusEvent e) {
					if (!text.equals(textField.getText())) {
						// trigger if just edited text
						listener.fitSingleDecay(fitSummed());
					}
				}
			});
	}

	/**
	 * Triggers fitSingleDecay if fitted parameter value or checkbox change.
	 * 
	 * @param textField
	 * @param checkBox 
	 */
	private void refitUponStateChange(final JTextField textField, final JCheckBox checkBox) {
        refitUponStateChange(textField);
		
		checkBox.addItemListener(
			new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					// definitely trigger if DESELECTED
					// also if SELECTED, in case text field already edited
					listener.fitSingleDecay(fitSummed());
				}
			});
	}

	/**
	 * Triggers fitSingleDecay if spinner value changes.
	 * 
	 * @param spinner 
	 */
	private void refitUponStateChange(final JSpinner spinner) {
		spinner.addChangeListener(
			new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (null != listener) {
						listener.fitSingleDecay(fitSummed());
					}
				}
			});
	}

	/**
	 * Propagates a threshold spinner value change.
	 * 
	 * @param thresholdMinSpinner 
	 */
	private void updateThresholdChange(final JSpinner thresholdSpinner) {
		thresholdSpinner.addChangeListener(
			new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					SpinnerModel spinnerModel = thresholdSpinner.getModel();
					if (spinnerModel instanceof SpinnerNumberModel) {
						int threshold = (Integer)((SpinnerNumberModel) spinnerModel).getValue();
						int thesholdMin, thresholdMax;
						if (thresholdMinSpinner == (JSpinner) e.getSource()) {
							thesholdMin = threshold;
							thresholdMax = (Integer) thresholdMaxSpinner.getValue();
							if (thesholdMin > thresholdMax) {
								thresholdMax = thesholdMin;
								thresholdMaxSpinner.setValue(thresholdMax);
							}
						}
						else {
							thesholdMin = (Integer) thresholdMinSpinner.getValue();
							thresholdMax = threshold;
							if (thresholdMax < thesholdMin) {
								thesholdMin = thresholdMax;
								thresholdMinSpinner.setValue(thesholdMin);
							}
						}
						if (null != thresholdListener) {
							thresholdListener.updateThreshold(thesholdMin, thresholdMax, fitSummed());
						}
						if (null != listener) {
							if (fitSummed()) {
								// threshold affects a summed fit, but not an ordingary single pixel fit
								listener.fitSingleDecay(true);
							}
						}
					}
				}
			});
	}

	/**
	 * Returns whether fitting summed pixels (vs. single pixel).
	 * 
	 * @return 
	 */
	private boolean fitSummed() {
		return ICurveFitter.FitRegion.SUMMED == getRegion();
	}

    /*
     * Disables and enables UI during and after a fit.
     *
     * @param enable
     */
    private void enableAll(boolean enable) {
        // fit algorithm settings
        regionComboBox.setEnabled(enable);
        algorithmComboBox.setEnabled(enable);
        functionComboBox.setEnabled(enable);
        noiseModelComboBox.setEnabled(enable);
        fittedImagesComboBox.setEnabled(enable);
        colorizeGrayScale.setEnabled(enable);
        for (JCheckBox checkBox : analysisCheckBoxList) {
            checkBox.setEnabled(enable);
        }
        fitAllChannels.setEnabled(enable);
        
        // cursors settings
		transientStartSpinnerA.setEnabled(enable);
		transientStartSpinnerB.setEnabled(enable);
		dataStartSpinnerA.setEnabled(enable);
		dataStartSpinnerB.setEnabled(enable);
		transientStopSpinnerA.setEnabled(enable);
		transientStopSpinnerB.setEnabled(enable);
        boolean promptEnable = enable;
        if (enable) {
            // do we have a prompt?
			if (null != fittingCursor) {
				promptEnable = fittingCursor.hasPrompt();
			}
        }
        enablePromptCursors(promptEnable);
        promptComboBox.setEnabled(enable);

        // fit control settings
		xSpinner.setEnabled(enable);
		ySpinner.setEnabled(enable);
		thresholdMinSpinner.setEnabled(enable);
		scatterSpinner.setEnabled(enable);
        chiSqTargetSpinner.setEnabled(enable);
        binningComboBox.setEnabled(enable);

        // single exponent fit
        aParam1.setEditable(enable);
        aFix1.setEnabled(enable);
        tParam1.setEditable(enable);
        tFix1.setEnabled(enable);
        zParam1.setEditable(enable);
        zFix1.setEnabled(enable);

        // double exponent fit
        a1Param2.setEditable(enable);
        a1Fix2.setEnabled(enable);
        a2Param2.setEditable(enable);
        a2Fix2.setEnabled(enable);
        t1Param2.setEditable(enable);
        t1Fix2.setEnabled(enable);
        t2Param2.setEditable(enable);
        t2Fix2.setEnabled(enable);
        zParam2.setEditable(enable);
        zFix2.setEnabled(enable);

        // triple exponent fit
        a1Param3.setEditable(enable);
        a1Fix3.setEnabled(enable);
        a2Param3.setEditable(enable);
        a2Fix3.setEnabled(enable);
        a3Param3.setEditable(enable);
        a3Fix3.setEnabled(enable);
        t1Param3.setEditable(enable);
        t1Fix3.setEnabled(enable);
        t2Param3.setEditable(enable);
        t2Fix3.setEnabled(enable);
        t3Param3.setEditable(enable);
        t3Fix3.setEnabled(enable);
        zParam3.setEditable(enable);
        zFix3.setEnabled(enable);

        // stretched exonent fit
        aParam4.setEditable(enable);
        aFix4.setEnabled(enable);
        tParam4.setEditable(enable);
        tFix4.setEnabled(enable);
        hParam4.setEditable(enable);
        hFix4.setEnabled(enable);
        zParam4.setEditable(enable);
        zFix4.setEnabled(enable);

        if (enable) {
            reconcileStartParam();
        }
    }

	/**
	 * Disables and enables buttons.
	 * 
	 * @param enable 
	 */
	private void enableButtons(boolean enable) {
		openButton.setEnabled(enable);
		quitButton.setEnabled(enable);
		fitButton.setEnabled(enable);
	}

    public ICurveFitter.FitRegion getRegion() {
        ICurveFitter.FitRegion region = null;
        String selected = (String) regionComboBox.getSelectedItem();
        if (selected.equals(SUM_REGION)) {
            region = ICurveFitter.FitRegion.SUMMED;
        }
        else if (selected.equals(ROIS_REGION)) {
            region = ICurveFitter.FitRegion.ROI;
        }
        else if (selected.equals(PIXEL_REGION)) {
            region = ICurveFitter.FitRegion.POINT;
        }
        else if (selected.equals(ALL_REGION)) {
            region = ICurveFitter.FitRegion.EACH;
        }
        return region;
    }

    public ICurveFitter.FitAlgorithm getAlgorithm() {
        ICurveFitter.FitAlgorithm algorithm = null;
        String selected = (String) algorithmComboBox.getSelectedItem();
        if (selected.equals(JAOLHO_LMA_ALGORITHM)) {
            algorithm = ICurveFitter.FitAlgorithm.JAOLHO;
        }
        else if (selected.equals(SLIM_CURVE_RLD_ALGORITHM)) {
            algorithm = ICurveFitter.FitAlgorithm.SLIMCURVE_RLD;
        }
        else if (selected.equals(SLIM_CURVE_LMA_ALGORITHM)) {
            algorithm = ICurveFitter.FitAlgorithm.SLIMCURVE_LMA;
        }
        else if (selected.equals(SLIM_CURVE_RLD_LMA_ALGORITHM)) {
            algorithm = ICurveFitter.FitAlgorithm.SLIMCURVE_RLD_LMA;
        }
        return algorithm;
    }

    public ICurveFitter.FitFunction getFunction() {
        ICurveFitter.FitFunction function = null;
        String selected = (String) functionComboBox.getSelectedItem();
        if (selected.equals(SINGLE_EXPONENTIAL)) {
            function = ICurveFitter.FitFunction.SINGLE_EXPONENTIAL;
        }
        else if (selected.equals(DOUBLE_EXPONENTIAL)) {
            function = ICurveFitter.FitFunction.DOUBLE_EXPONENTIAL;
        }
        else if (selected.equals(TRIPLE_EXPONENTIAL)) {
            function = ICurveFitter.FitFunction.TRIPLE_EXPONENTIAL;
        }
        else if (selected.equals(STRETCHED_EXPONENTIAL)) {
            function = ICurveFitter.FitFunction.STRETCHED_EXPONENTIAL;
        }
        return function;
    }

    public String[] getAnalysisList() {
        List<String> analysisList = new ArrayList<String>();
        for (JCheckBox checkBox : analysisCheckBoxList) {
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
    public ICurveFitter.NoiseModel getNoiseModel() {
        ICurveFitter.NoiseModel noiseModel = null;
        String selected = (String) noiseModelComboBox.getSelectedItem();
        if (selected.equals(GAUSSIAN_FIT)) {
            noiseModel = ICurveFitter.NoiseModel.GAUSSIAN_FIT;
        }
        else if (selected.equals(POISSON_FIT)) {
            noiseModel = ICurveFitter.NoiseModel.POISSON_FIT;
        }
        else if (selected.equals(POISSON_DATA)) {
            noiseModel = ICurveFitter.NoiseModel.POISSON_DATA;
        }
        else if (selected.equals(MAXIMUM_LIKELIHOOD)) {
            noiseModel = ICurveFitter.NoiseModel.MAXIMUM_LIKELIHOOD;
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
        String selected = (String) fittedImagesComboBox.getSelectedItem();
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
        return colorizeGrayScale.isSelected();
    }

    public boolean getFitAllChannels() {
        return fitAllChannels.isSelected();
    }

    public int getX() {
        return (Integer) xSpinner.getValue();
    }

    public void setX(int x) {
        xSpinner.setValue(x);
    }

    public int getY() {
        return (Integer) ySpinner.getValue();
    }

    public void setY(int y) {
        ySpinner.setValue(y);
    }

    public int getThresholdMinimum() {
		return (Integer) thresholdMinSpinner.getValue();
    }

    public void setThresholdMinimum(int thresholdMin) {
		thresholdMinSpinner.setValue(thresholdMin);
    }
	
    public int getThresholdMaximum() {
		return (Integer) thresholdMaxSpinner.getValue();
    }

    public void setThresholdMaximum(int thresholdMax) {
		thresholdMaxSpinner.setValue(thresholdMax);
    }
	
    public int getBinning() {
		return binningComboBox.getSelectedIndex();
    }

    public double getChiSquareTarget() {
		return (Double) chiSqTargetSpinner.getValue();
    }
    
    public void setChiSquareTarget(double chiSqTarget) {
		chiSqTargetSpinner.setValue(chiSqTarget);
    }
	
	public double getScatter() {
		return (Double) scatterSpinner.getValue();
	}

    public int getParameterCount() {
        int count = 0;
        String function = (String) functionComboBox.getSelectedItem();
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
        fittedParameterCount = count;
    }

    public double[] getParameters() {
        double parameters[] = null;
		if (noFit) {
			String function = (String) functionComboBox.getSelectedItem();
			if (function.equals(SINGLE_EXPONENTIAL)) {
				parameters = new double[4];
			}
			else if (function.equals(DOUBLE_EXPONENTIAL)) {
				parameters = new double[6];
			}
			else if (function.equals(TRIPLE_EXPONENTIAL)) {
				parameters = new double[8];
			}
			else if (function.equals(STRETCHED_EXPONENTIAL)) {
				parameters = new double[5];
			}
			for (int i = 0; i < parameters.length; ++i) {
				parameters[i] = Double.NaN;
			}
		}
		else {
			try {
				String function = (String) functionComboBox.getSelectedItem();
				if (function.equals(SINGLE_EXPONENTIAL)) {
					parameters = new double[4];
					parameters[2] = Double.valueOf(aParam1.getText());
					parameters[3] = Double.valueOf(tParam1.getText());
					parameters[1] = Double.valueOf(zParam1.getText());
				}
				else if (function.equals(DOUBLE_EXPONENTIAL)) {
					parameters = new double[6];
					parameters[2] = Double.valueOf(a1Param2.getText());
					parameters[3] = Double.valueOf(t1Param2.getText());
					parameters[4] = Double.valueOf(a2Param2.getText());
					parameters[5] = Double.valueOf(t2Param2.getText());
					parameters[1] = Double.valueOf(zParam2.getText());
				}
				else if (function.equals(TRIPLE_EXPONENTIAL)) {
					parameters = new double[8];
					parameters[2] = Double.valueOf(a1Param3.getText());
					parameters[3] = Double.valueOf(t1Param3.getText());
					parameters[4] = Double.valueOf(a2Param3.getText());
					parameters[5] = Double.valueOf(t2Param3.getText());
					parameters[6] = Double.valueOf(a3Param3.getText());
					parameters[7] = Double.valueOf(t3Param3.getText());
					parameters[1] = Double.valueOf(zParam3.getText());
				}
				else if (function.equals(STRETCHED_EXPONENTIAL)) {
					parameters = new double[5];
					parameters[2] = Double.valueOf(aParam4.getText());
					parameters[3] = Double.valueOf(tParam4.getText());
					parameters[4] = Double.valueOf(hParam4.getText());
					parameters[1] = Double.valueOf(zParam4.getText());
				}
			}
			catch (NumberFormatException e) {
				//TODO recover
			}

			parameters[0] = 0.0; // chiSquare
		}
        return parameters;
    }

    public void setParameters(double params[], double AIC) {
		// parameters NaN signals error
		noFit = Double.isNaN(params[0]);
		
        String function = (String) functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
			String a, t, z, chiSq, aic;
			if (noFit) {
				// fitted parameters could not be determined
				a = t = z = chiSq = aic = NO_FIT;
			}
			else {
				a = paramToString(params[2], 3);
				t = paramToString(params[3], 3);
				z = paramToString(params[1], 3);
				chiSq = paramToString(params[0], 6);
				aic = paramToString(AIC, 6);
			}
            aParam1.setText    (a);
            tParam1.setText    (t);
            zParam1.setText    (z);
            chiSqParam1.setText(chiSq);
			AICParam1.setText  (aic);
			
			// show error message as appropriate
			errorLabel1.setVisible(noFit);
		}
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
			String a1, t1, a2, t2, z, chiSq, aic;
			if (noFit) {
				// fitted parameters could not be determined
				a1 = t1 = a2 = t2 = z = chiSq = aic = NO_FIT;
			}
			else {
				a1 = paramToString(params[2], 3);
				t1 = paramToString(params[3], 3);
				a2 = paramToString(params[4], 3);
				t2 = paramToString(params[5], 3);
				z = paramToString(params[1], 3);
				chiSq = paramToString(params[0], 6);
				aic = paramToString(AIC, 6);
			}
			
            a1Param2.setText   (a1);
            t1Param2.setText   (t1);
            a2Param2.setText   (a2);
            t2Param2.setText   (t2);
            zParam2.setText    (z);
            chiSqParam2.setText(chiSq);
			AICParam2.setText  (aic);
			
			// show error message as appropriate
		    errorLabel2.setVisible(noFit);
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
			String a1, t1, a2, t2, a3, t3, z, chiSq, aic;
			if (noFit) {
				// fitted parameters could not be determined
				a1 = t1 = a2 = t2 = a3 = t3 = z = chiSq = aic = NO_FIT;
			}
			else {
				a1 = paramToString(params[2], 3);
				t1 = paramToString(params[3], 3);
				a2 = paramToString(params[4], 3);
				t2 = paramToString(params[5], 3);
				a3 = paramToString(params[6], 3);
				t3 = paramToString(params[7], 3);
				z = paramToString(params[1], 3);
				chiSq = paramToString(params[0], 6);
				aic = paramToString(AIC, 6);
			}
            a1Param3.setText   (a1);
            t1Param3.setText   (t1);
            a2Param3.setText   (a2);
            t2Param3.setText   (t2);
            a3Param3.setText   (a3);
            t3Param3.setText   (t3);
            zParam3.setText    (z);
            chiSqParam3.setText(chiSq);
			AICParam3.setText  (aic);
			
			// show error message as appropriate
			errorLabel3.setVisible(noFit);
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
			String a, t, h, z, chiSq, aic;
			if (noFit) {
				// fitted parameters could not be determined
				a = t = h = z = chiSq = aic = NO_FIT;
			}
			else {
				a = paramToString(params[2], 3);
				t = paramToString(params[3], 3);
				h = paramToString(params[4], 3);
				z = paramToString(params[1], 3);
				chiSq = paramToString(params[0], 6);
				aic = paramToString(AIC, 6);
			}
            aParam4.setText    (a);
            tParam4.setText    (t);
            hParam4.setText    (h);
            zParam4.setText    (z);
            chiSqParam4.setText(chiSq);
			AICParam4.setText  (aic);
			
			// show error message as appropriate
			errorLabel4.setVisible(noFit);
        }
    }
    
    private String paramToString(double param, int places) {
        return "" + fitterEstimator.roundToDecimalPlaces(param, places);
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
                aParam1.setText    ("" + (float) params[2]);
                tParam1.setText    ("" + (float) params[3]);
                zParam1.setText    ("" + (float) params[1]);
                chiSqParam1.setText("" + (float) params[0]);
				errorLabel1.setVisible(false);
                break;
            case 1:
                a1Param2.setText   ("" + (float) params[2]);
                t1Param2.setText   ("" + (float) params[3]);
                a2Param2.setText   ("" + (float) params[4]);
                t2Param2.setText   ("" + (float) params[5]);
                zParam2.setText    ("" + (float) params[1]);
                chiSqParam2.setText("" + (float) params[0]);
				errorLabel2.setVisible(false);
                break;
            case 2:
                a1Param3.setText   ("" + (float) params[2]);
                t1Param3.setText   ("" + (float) params[3]);
                a2Param3.setText   ("" + (float) params[4]);
                t2Param3.setText   ("" + (float) params[5]);
                a3Param3.setText   ("" + (float) params[6]);
                t3Param3.setText   ("" + (float) params[7]);
                zParam3.setText    ("" + (float) params[1]);
                chiSqParam3.setText("" + (float) params[0]);
				errorLabel3.setVisible(false);
                break;
            case 3:
                aParam4.setText    ("" + (float) params[0]);
                tParam4.setText    ("" + (float) params[1]);
                hParam4.setText    ("" + (float) params[2]);
                zParam4.setText    ("" + (float) params[1]);
                chiSqParam4.setText("" + (float) params[0]);
				errorLabel4.setVisible(false);
                break;
        }
    }

    public boolean[] getFree() {
        boolean free[] = null;
        String function = (String) functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            free = new boolean[3];
            free[0] = !aFix1.isSelected();
            free[1] = !tFix1.isSelected();
            free[2] = !zFix1.isSelected();
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            free = new boolean[5];
            free[0] = !a1Fix2.isSelected();
            free[1] = !t1Fix2.isSelected();
            free[2] = !a2Fix2.isSelected();
            free[3] = !t2Fix2.isSelected();
            free[4] = !zFix2.isSelected();
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            free = new boolean[7];
            free[0] = !a1Fix3.isSelected();
            free[1] = !t1Fix3.isSelected();
            free[2] = !a2Fix3.isSelected();
            free[3] = !t2Fix3.isSelected();
            free[4] = !a3Fix3.isSelected();
            free[5] = !t3Fix3.isSelected();
            free[6] = !zFix3.isSelected();

        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            free = new boolean[4];
            free[0] = !aFix4.isSelected();
            free[1] = !tFix4.isSelected();
            free[2] = !hFix4.isSelected();
            free[3] = !zFix4.isSelected();
        }
        return free;
    }

    public boolean getRefineFit() {
        JCheckBox checkBox = null;
        String function = (String) functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            checkBox = startParam1;
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            checkBox = startParam2;
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            checkBox = startParam3;
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            checkBox = startParam4; //TODO use an array of checkboxes, etc.
        }
        return !checkBox.isSelected();
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
        promptDelaySpinnerA.setEnabled(enable);
		promptDelaySpinnerB.setEnabled(enable);
        promptWidthSpinnerA.setEnabled(enable);
		promptWidthSpinnerB.setEnabled(enable);
        promptBaselineSpinnerA.setEnabled(enable);
		promptBaselineSpinnerB.setEnabled(enable);
    }
    
    /**
     * This decides whether the existing parameters could be used as the
     * initial values for another fit.
     */
    private void reconcileStartParam() {
        // parameter counts happen to be unique for each fit function
        boolean enable = (fittedParameterCount == getParameterCount());
        startParam1.setEnabled(enable);
        startParam2.setEnabled(enable);
        startParam3.setEnabled(enable);
        startParam4.setEnabled(enable);
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

	private class FittingCursorListenerImpl implements FittingCursorListener {

		@Override
		public void cursorChanged(FittingCursor cursor) {
			transientStartSpinnerA.setValue(cursor.getTransientStartTime());
			transientStartSpinnerB.setValue(cursor.getTransientStartIndex());
			dataStartSpinnerA.setValue(cursor.getDataStartTime());
			dataStartSpinnerB.setValue(cursor.getDataStartIndex());
			transientStopSpinnerA.setValue(cursor.getTransientStopTime());
			transientStopSpinnerB.setValue(cursor.getTransientStopIndex());
			
			promptDelaySpinnerA.setValue(cursor.getPromptDelayTime());
			promptDelaySpinnerB.setValue(cursor.getPromptDelayIndex());
			promptWidthSpinnerA.setValue(cursor.getPromptWidthTime());
			promptWidthSpinnerB.setValue(cursor.getPromptWidthIndex());
			promptBaselineSpinnerA.setValue(cursor.getPromptBaselineValue());
			promptBaselineSpinnerB.setValue(cursor.getPromptBaselineValue());
		}
	}
}
