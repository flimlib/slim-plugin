/*
SLIMPlugin for combined spectral-lifetime image analysis.

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

package loci.slim2.process.interactive;

import imagej.display.Display;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.threshold.ThresholdMethod;
import imagej.data.threshold.ThresholdService;
import imagej.display.DisplayService;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;

import javax.swing.JFrame;

import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;
import loci.curvefitter.ICurveFitter.FitRegion;
import loci.curvefitter.IFitterEstimator;
import loci.curvefitter.JaolhoCurveFitter;
import loci.curvefitter.SLIMCurveFitter;

import loci.slim2.fitting.DefaultGlobalFitParams;
import loci.slim2.fitting.DefaultLocalFitParams;
import loci.slim2.fitting.FitResults;
import loci.slim2.fitting.FittingEngine;
import loci.slim2.fitting.GlobalFitParams;
import loci.slim2.fitting.LocalFitParams;
import loci.slim2.fitting.ThreadedFittingEngine;
import loci.slim2.decay.LifetimeDatasetWrapper;
import loci.slim2.decay.LifetimeGrayscaleDataset;
import loci.slim2.decay.NoLifetimeAxisFoundException;
import loci.slim2.heuristics.CursorEstimator;
import loci.slim2.heuristics.DefaultFitterEstimator;
import loci.slim2.heuristics.Estimator;
import loci.slim2.process.Excitation;
import loci.slim2.process.ExcitationFileUtility;
import loci.slim2.process.FitSettings;
import loci.slim2.process.InteractiveProcessor;
import loci.slim2.process.interactive.cursor.FittingCursor;
import loci.slim2.process.interactive.cursor.FittingCursorListener;
import loci.slim2.process.interactive.grayscale.GrayscaleDisplay;
import loci.slim2.process.interactive.ui.DecayGraph;
import loci.slim2.process.interactive.ui.DefaultDecayGraph;
import loci.slim2.process.interactive.ui.DefaultUserInterfacePanel;
import loci.slim2.process.interactive.ui.ExcitationGraph;
import loci.slim2.process.interactive.ui.ExcitationPanel;
import loci.slim2.process.interactive.ui.ThresholdUpdate;
import loci.slim2.process.interactive.ui.UserInterfacePanel;
import loci.slim2.process.interactive.ui.UserInterfacePanelListener;
import org.scijava.Context;

/**
 * Fits FLIM data in an interactive manner.
 * 
 * @author Aivar Grislis
 */
public class DefaultInteractiveProcessor implements InteractiveProcessor {
	private Context context;
	private DatasetService datasetService;
	private DisplayService displayService;
	private UIService uiService;
	private Estimator estimator;
	private FittingEngine fittingEngine;
	private IFitterEstimator fitterEstimator;
	private FittingCursor fittingCursor;
	private LifetimeDatasetWrapper lifetimeDatasetWrapper;
	private LifetimeGrayscaleDataset lifetimeGrayscaleDataset;
	private GrayscaleDisplay grayscaleDisplay;
	private DecayGraph decayGraph;
	private ExcitationGraph excitationGraph;
	private ExcitationPanel excitationPanel;
	private int bins;
	private double timeInc;
	private UserInterfacePanel uiPanel;
	private long[] position;
	private int thresholdMin;
	private int thresholdMax;
	private volatile boolean quit;
	private volatile boolean openFile;
	private volatile boolean fitImages;
	private volatile boolean cancel;
	private volatile boolean fitPixel;
	private volatile boolean fitSummed;
	
	@Override
	public void init(Context context, DatasetService datasetService, DisplayService displayService, Estimator estimator) {
		this.context        = context;
		this.datasetService = datasetService;
		this.displayService = displayService;
		this.uiService      = uiService;
		this.estimator      = estimator;
	}
	
	@Override
	public FitSettings getFitSettings() {
		DefaultFitSettings fitSettings = new DefaultFitSettings();
		fitSettings.setGlobalFitParams(getGlobalFitParams(uiPanel, fittingCursor));
		String fittedImages = uiPanel.getFittedImages();
		fitSettings.setFittedImages(fittedImages);
		fitSettings.setBins(bins);
		return fitSettings;
	}
	
	@Override
	public boolean process(final LifetimeDatasetWrapper lifetime) {
		lifetimeDatasetWrapper = lifetime;
		quit = openFile = fitImages = cancel = fitPixel = fitSummed = false;
		
		// create the clickable grayscale representation
		if (null != grayscaleDisplay) {
			grayscaleDisplay.close();
		}
		grayscaleDisplay = createGrayscale();
		
		// create cursors to clip transient and prompt decays
		bins = lifetimeDatasetWrapper.getBins();
		timeInc = lifetimeDatasetWrapper.getTimeIncrement();
		fitterEstimator = new DefaultFitterEstimator();
		fittingCursor = new FittingCursor(timeInc, bins, fitterEstimator);
		fittingCursor.addListener(
			new FittingCursorListener() {
				private Integer saveTransStart    = null;
				private Integer saveDataStart     = null;
				private Integer saveTransStop     = null;
				private Integer savePromptStart   = null;
				private Integer savePromptStop    = null;
				private Double savePromptBaseline = null;
				
				@Override
				public void cursorChanged(FittingCursor cursor) {
					// get current cursor values
					int transStart        = cursor.getTransientStartIndex();
					int dataStart         = cursor.getDataStartIndex();
					int transStop         = cursor.getTransientStopIndex();
					int promptStart       = cursor.getPromptStartIndex();
					int promptStop        = cursor.getPromptStopIndex();
					double promptBaseline = cursor.getPromptBaselineValue();
					
					// look for changes, current vs. saved cursor values
					if (null == saveTransStart
							|| null == saveDataStart
							|| null == saveTransStop
							|| null == savePromptStart
							|| null == savePromptStop
							|| null == savePromptBaseline
							|| transStart     != saveTransStart
							|| dataStart      != saveDataStart
							|| transStop      != saveTransStop
							|| promptStart    != savePromptStart
							|| promptStop     != savePromptStop
							|| promptBaseline != savePromptBaseline) {
						
						// update saved cursor values for next time
						saveTransStart     = transStart;
						saveDataStart      = dataStart;
						saveTransStop      = transStop;
						savePromptStart    = promptStart;
						savePromptStop     = promptStop;
						savePromptBaseline = promptBaseline;
						
						if (null != uiPanel) {
							if (FitRegion.SUMMED == uiPanel.getRegion()) {
								fitSummed(position);
							}
							else {
								fitPixel(position);
							}
						}
					}
				}
			}
		);
		
		// display the UI
		if (null == uiPanel) {
			boolean tabbed = false;
			boolean showTau = true;
			String[] binning = new String[] { "none", "3x3", "5x5", "7x7", "9x9", "11x11" };
			uiPanel = new DefaultUserInterfacePanel(tabbed, showTau, bins, timeInc, new String[] { "one", "two" }, binning, fittingCursor, fitterEstimator);
		}
		//TODO ARG calling this shows the red threshold as a side effect
		//threshold = estimator.getThreshold();
	//	double[] t = grayscaleDisplay.getThreshold();
	//	System.out.println("threshold " + t[0] + " " + t[1]);
		
		double[] t = grayscaleDisplay.estimateThreshold();
		System.out.println("estimate as " + t[0] + " " + t[1]);
		thresholdMin = (int) t[0];
		thresholdMax = (int) t[1];
				
		grayscaleDisplay.setThreshold(thresholdMin, thresholdMax);
        uiPanel.setThresholdMinimum(thresholdMin);
		uiPanel.setThresholdMaximum(thresholdMax);
        uiPanel.setChiSquareTarget(estimator.getChiSquareTarget());
       // uiPanel.setFunctionParameters(0, estimator.getParameters(1, false));
       // uiPanel.setFunctionParameters(1, estimator.getParameters(2, false));
       // uiPanel.setFunctionParameters(2, estimator.getParameters(3, false));
       // uiPanel.setFunctionParameters(3, estimator.getParameters(0, true));
		uiPanel.setThresholdListener(
			new ThresholdUpdate() {
				@Override
				public void updateThreshold(int min, int max, boolean summed){
					thresholdMin = min;
					thresholdMax = max;
					grayscaleDisplay.setThreshold(min, max);
					if (summed) {
						fitSummed = true;
						cancel = false;
					}
				}
			}
		);

        uiPanel.setListener(
            new UserInterfacePanelListener() {
                /**
                 * Triggers a fit, creating fitted images.
                 */
                @Override
                public void fitImages() {
					fitImages = true;
					cancel = false;
					System.out.println("fitImages");
                }
				
				/**
				 * Triggers a fitSingleDecay.
				 */
				@Override
				public void fitSingleDecay(boolean summed) {
					if (summed) {
						fitSummed = true;
					}
					else {
						fitPixel = true;
					}
					cancel = false;
				}

                /**
                 * Cancels ongoing fit.
                 */
                @Override
                public void cancelFit() {
                    cancel = true;
                }
                
                /**
                 * Quits running plugin.
                 */
                @Override
                public void quit() {
					// hide the UI, quit
					uiPanel.getFrame().setVisible(false);
					quit = true;
                }
				
				/**
				 * Open new file(s).
				 */
				@Override
				public void openFile() {
					// open a different file
					openFile = true;
				}

                /**
                 * Loads an excitation curve from file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                @Override
				public boolean loadExcitation(String fileName) {
                    Excitation excitation = ExcitationFileUtility.loadExcitation(fileName, timeInc);
                    return updateExcitation(uiPanel, excitation);
                }

                /**
                 * Creates an excitation curve from current X, Y and saves to file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                @Override
                public boolean createExcitation(String fileName) {
					int binSize = uiPanel.getBinning();
					double[] decay = lifetimeDatasetWrapper.getBinnedDecay(binSize, position);
                    Excitation excitation = ExcitationFileUtility.createExcitation(fileName, decay, timeInc);
                    return updateExcitation(uiPanel, excitation);
                }

                /**
                 * Estimates an excitation curve from current X, Y and saves to file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                @Override
                public boolean estimateExcitation(String fileName) {
					/*// get the data
                    int channel = 0;
                    if (null != _grayScaleImage) {
                        channel = _grayScaleImage.getChannel();
                    }
                    int x = uiPanel.getX();
                    int y = uiPanel.getY();
                    double[] inValues = new double[_bins];
                    for (int b = 0; b < _bins; ++b) {
                        inValues[b] = getData(_cursor, channel, x, y, b);
                    }
					
					// find the peak value and bin
					double peak = -Double.MAX_VALUE;
					int peakBin = 0;
					for (int b = 0; b < _bins; ++b) {
						if (inValues[b] > peak) {
							peak = inValues[b];
							peakBin = b;
						}
					}
					
					double maxSlope = -Double.MAX_VALUE;
					int maxSlopeBin = 0;
					double[] firstDerivative = new double[_bins];
					for (int b = 0; b < peakBin; ++b) {
						firstDerivative[b] = inValues[b + 1] - inValues[b];
						if (firstDerivative[b] > maxSlope) {
							maxSlope = firstDerivative[b];
							maxSlopeBin = b;
						}
					}
					
					//TODO WHY? _fittingCursor = null;
					
					double a = peak;
					double b = (double) peakBin;
					double c = (double) (peakBin - maxSlopeBin) / 2;
					
					System.out.println("max slope estimated GAUSSIAN a " + a + " b " + b + " c " + c);
					
					double[] outValues = new double[_bins];
					for (int i = 0; i < _bins; ++i) {
						outValues[i] = gaussian(a, b, c, i);
					}
					
					System.out.println("PEAK VALUE " + peak + " BIN " + peakBin);
					System.out.println("MAX SLOPE " + maxSlope + " BIN " + maxSlopeBin);
					System.out.println("GAUSSIAN a " + a + " b " + b + " c " + c);
					//TODO END EXPERIMENTAL
					for (double oV : outValues) {
						if (0.0 != oV) System.out.println(" " + oV);
					}
					
                    Excitation excitation = ExcitationFileHandler.getInstance().createExcitation(fileName, outValues, _timeRange);
                    return updateExcitation(uiPanel, excitation);*/
					System.out.println("estimateExcitation");
					return true;
                }

				@Override
				public boolean gaussianExcitation(String fileName) {
					/*Preferences prefs = Preferences.userNodeForPackage(this.getClass());
					double a = prefs.getDouble(GAUSSIAN_A_KEY, 30.0);
					double b = prefs.getDouble(GAUSSIAN_B_KEY, 20.0);
					double c = prefs.getDouble(GAUSSIAN_C_KEY, 2.0);
					
					GenericDialog dialog = new GenericDialog("Gaussian Excitation");
					dialog.addNumericField("height", a, 5);
					dialog.addNumericField("position", b, 5);
					dialog.addNumericField("width", c, 5);
					dialog.showDialog();
					if (dialog.wasCanceled()) {
						return false;
					}
					a = dialog.getNextNumber();
					b = dialog.getNextNumber();
					c = dialog.getNextNumber();
					
					prefs.putDouble(GAUSSIAN_A_KEY, a);
					prefs.putDouble(GAUSSIAN_B_KEY, b);
					prefs.putDouble(GAUSSIAN_C_KEY, c);
					
					double[] outValues = new double[_bins];
					for (int i = 0; i < _bins; ++i) {
						outValues[i] = gaussian(a, b, c, i);
					}
					
					Excitation excitation = ExcitationFileHandler.getInstance().createExcitation(fileName, outValues, _timeRange);
					return updateExcitation(uiPanel, excitation);*/
					System.out.println("gaussianExcitation");
					return true;
				}
				
				/**
                 * Cancels the current excitation curve, if any.
                 *
                 */
                @Override
                public void cancelExcitation() {
                    if (null != excitationPanel) {
                        excitationPanel.quit();
                        excitationPanel = null;
                        updateExcitation(null, null);
                        //TODO ARG redo stop/start cursors on decay curve?
                    }
					System.out.println("cancelExcitation");
                }

                /**
                 * Estimates prompt and decay cursors.
                 */
                @Override
                public void estimateCursors() {
                    double xInc = timeInc;
					double chiSqTarget = uiPanel.getChiSquareTarget();
                    
                    double[] prompt = null;
                    if (null != excitationPanel) {
                        prompt = excitationPanel.getRawValues();
                    }
					int binSize = uiPanel.getBinning();
					double[] decay = lifetimeDatasetWrapper.getBinnedDecay(binSize, position);
    
//                    System.out.println("chiSqTarget is " + chiSqTarget);
//                    System.out.println("prompt is " + prompt + " and fitting cursor thinks prompt " + _fittingCursor.hasPrompt());
                    if (null != prompt && fittingCursor.hasPrompt()) {
                        double[] results = CursorEstimator.estimateCursors
                                (xInc, prompt, decay, chiSqTarget);
                        
                        // want all the fitting cursor listeners to get everything at once
                        fittingCursor.suspendNotifications();
                        fittingCursor.setHasPrompt(true);
                        fittingCursor.setPromptStartIndex
                                ((int) results[CursorEstimator.PROMPT_START]);
                        fittingCursor.setPromptStopIndex
                                ((int) results[CursorEstimator.PROMPT_STOP]);
                        fittingCursor.setPromptBaselineValue
                                (results[CursorEstimator.PROMPT_BASELINE]);
                        fittingCursor.setTransientStartIndex
                                ((int) results[CursorEstimator.TRANSIENT_START]);
                        fittingCursor.setDataStartIndex
                                ((int) results[CursorEstimator.DATA_START]);
                        fittingCursor.setTransientStopIndex
                                ((int) results[CursorEstimator.TRANSIENT_STOP]);
                        fittingCursor.sendNotifications();
                    }
                    else
                    {
                        int[] results = CursorEstimator.estimateDecayCursors
                                (xInc, decay);
                        
                        // want all the fitting cursor listeners to get everything at once
                        fittingCursor.suspendNotifications();
                        fittingCursor.setHasPrompt(false);
                        fittingCursor.setTransientStartIndex(results[CursorEstimator.TRANSIENT_START]);
                        fittingCursor.setDataStartIndex(results[CursorEstimator.DATA_START]);
                        fittingCursor.setTransientStopIndex(results[CursorEstimator.TRANSIENT_STOP]);
                        fittingCursor.sendNotifications();
                    }
                }
            }
        );
        uiPanel.getFrame().setLocationRelativeTo(null);
        uiPanel.getFrame().setVisible(true);
		
		// get the brightest pixel decay, current plane
		position = lifetimeGrayscaleDataset.getBrightestPixel();
		int binSize = uiPanel.getBinning();
		double[] decay = lifetimeDatasetWrapper.getBinnedDecay(binSize, position);
		
		// initial cursor estimate
		initCursors(decay);
		
		// fit brightest pixel
		fitPixel(position);
		
		do {
			// wait for user input
			while (!fitImages && !fitPixel && !fitSummed && !quit && !openFile) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {

				}
			}
			
			if (cancel) {
				cancel = false;
				fitImages = false;
			}
			else if (fitImages) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
					
				}
			}
			else if (fitPixel) {
				fitPixel();
				fitPixel = false;
			}
			else if (fitSummed) {
				// uses last known position for which plane to sum
				fitSummed(position);
				fitSummed = false;
			}
		}
		while (!quit && !openFile);

		// return whether to quit
		return(quit);
	}

    /*
     * This method is called whenever a new excitation is loaded.
     */
    private boolean updateExcitation(UserInterfacePanel uiPanel, Excitation excitation) {
        boolean success = false;
        if (null != excitation) {
            if (null != excitationPanel) {
				excitationPanel.quit();
            }

            // get decay at current pixel
			int binSize = uiPanel.getBinning();
			double[] decay = lifetimeDatasetWrapper.getBinnedDecay(binSize, position);

			// estimate cursors for excitation and current pixel
            double chiSqTarget = uiPanel.getChiSquareTarget();
            double[] results = CursorEstimator.estimateCursors
                    (timeInc, excitation.getValues(), decay, chiSqTarget);
            
            // want all the fitting cursor listeners to get everything at once
            fittingCursor.suspendNotifications();
            fittingCursor.setHasPrompt(true);
            fittingCursor.setPromptStartIndex   ((int) results[CursorEstimator.PROMPT_START]);
            fittingCursor.setPromptStopIndex    ((int) results[CursorEstimator.PROMPT_STOP]);
            fittingCursor.setPromptBaselineValue      (results[CursorEstimator.PROMPT_BASELINE]);
            fittingCursor.setTransientStartIndex((int) results[CursorEstimator.TRANSIENT_START]);
            fittingCursor.setDataStartIndex     ((int) results[CursorEstimator.DATA_START]);
            fittingCursor.setTransientStopIndex ((int) results[CursorEstimator.TRANSIENT_STOP]);
            fittingCursor.sendNotifications();

            excitationPanel = new ExcitationPanel(excitation, fittingCursor); //TODO ARG excitation cursor change refit problem here; get new values before excitation ready for refit

            success = true;
        }
        else {
            fittingCursor.setHasPrompt(false);
        }
        return success;
    }

	/**
	 * Does initial estimate of cursors.
	 * 
	 * @param decay 
	 */
	private void initCursors(double[] decay) {
        int[] results = CursorEstimator.estimateDecayCursors(timeInc, decay);
        int transientStart = results[CursorEstimator.TRANSIENT_START];
        int dataStart = results[CursorEstimator.DATA_START];
        int transientStop = results[CursorEstimator.TRANSIENT_STOP];

        // send fitting cursor notifications to listeners
        fittingCursor.suspendNotifications(); // start batch
        fittingCursor.setTransientStartIndex(transientStart);
        fittingCursor.setDataStartIndex(dataStart);
        fittingCursor.setTransientStopIndex(transientStop);
        fittingCursor.sendNotifications(); // send batch
	}
	
	private GrayscaleDisplay createGrayscale() {	
		// make a grayscale version of lifetime dataset
		lifetimeGrayscaleDataset = new LifetimeGrayscaleDataset(datasetService, lifetimeDatasetWrapper);
		//fittingContext.setGrayscaleDataset(lifetimeGrayscaleDataset);
		
		// display grayscale version
		Display<?> display = displayService.createDisplay(lifetimeGrayscaleDataset.getDataset());
		
		return new GrayscaleDisplay(context, lifetimeGrayscaleDataset.getDataset(), display);
		
		//TODO ARG no way of getting current position from Display; can get by w/o it
		//TODO ARG how to draw overlays on top of this display???
		//fittingContext.setGrayscaleDisplay(display);
	}

	/**
	 * This version handles pixel fits driven by changing X, Y in the UI Panel
	 * or by changing some other fit settings that require fitSingleDecay.
	 */
	private void fitPixel() {
		// update last position with current X, Y from UI Panel
		position[0] = uiPanel.getX();
		position[1] = uiPanel.getY();

		// fit pixel at position
		fitPixel(position);
	}

	/**
	 * Pixel fitting.
	 * 
	 * @param position
	 */
	private void fitPixel(long[] position) {
		// make sure displayed UI panel X Y is up to date
		int x = (int) position[0];
		int y = (int) position[1];
        uiPanel.setX(x);
        uiPanel.setY(y);
		
		// update grayscale cursor
		grayscaleDisplay.setPixel(position);

		// do the fit
		int binSize = uiPanel.getBinning();
		double[] decay = lifetimeDatasetWrapper.getBinnedDecay(binSize, position);
		FitResults fitResults = fitDecay(decay);

		// show fitted parameters
		uiPanel.setParameters(fitResults.getParams(), fitResults.getChiSquare());

		// show decay, fitted, and residuals
		showDecayGraph("Pixel " + x + " " + y, uiPanel, fittingCursor, fitResults);
	}

	/**
	 * Combined, summed decay fitting, per plane. //TODO ARG should there be option to sum all planes???
	 *
	 * @param position X & Y are ignored
	 * @return fit results
	 */
	private void fitSummed(long[] position) {
		double[] decay = lifetimeDatasetWrapper.getCombinedPlaneDecay(thresholdMin, thresholdMax, position);
		FitResults fitResults = fitDecay(decay);

		// show fitted parameters
		uiPanel.setParameters(fitResults.getParams(), fitResults.getChiSquare());

		// show decay, fitted, and residuals
		showDecayGraph("Summed Pixels", uiPanel, fittingCursor, fitResults);
	}

	/**
	 * Helper routine to do the fit.
	 * 
	 * @param decay
	 * @return 
	 */
	private FitResults fitDecay(double[] decay) {
		GlobalFitParams params = getGlobalFitParams(uiPanel, fittingCursor);
		
		LocalFitParams data = new DefaultLocalFitParams();
		data.setY(decay);
		data.setSig(null);
		data.setParams(uiPanel.getParameters());
		double[] yFitted = new double[bins];
		data.setYFitted(yFitted);
		
		return getFittingEngine(uiPanel).fit(params, data);
	}

	/**
	 * Helper routine to get fit parameters for a group of pixels.
	 * 
	 * @param ui
	 * @param fittingCursor
	 * @return 
	 */
	private GlobalFitParams getGlobalFitParams(UserInterfacePanel ui, FittingCursor fittingCursor) {
		GlobalFitParams params = new DefaultGlobalFitParams();
		params.setEstimator(fitterEstimator);
		params.setFitAlgorithm(ui.getAlgorithm());
		params.setFitFunction(ui.getFunction());
		params.setNoiseModel(ui.getNoiseModel());
		params.setXInc(timeInc);
		double[] promptValues = null;
        if (fittingCursor.hasPrompt() && null != excitationPanel) {
            int startIndex = fittingCursor.getPromptStartIndex();
            int stopIndex  = fittingCursor.getPromptStopIndex();
            double base  = fittingCursor.getPromptBaselineValue();   
            promptValues = excitationPanel.getValues(startIndex, stopIndex, base);
		}
		params.setPrompt(promptValues);
		params.setChiSquareTarget(ui.getChiSquareTarget());
		params.setFree(translateFree(ui.getFunction(), ui.getFree()));
		params.setStartPrompt(fittingCursor.getPromptStartIndex());
		params.setStopPrompt(fittingCursor.getPromptStopIndex());
		params.setTransientStart(fittingCursor.getTransientStartIndex());
		params.setDataStart(fittingCursor.getDataStartIndex());
		params.setTransientStop(fittingCursor.getTransientStopIndex());
		return params;
	}

	/**
	 * Helper routine to get and set up fitting engine.
	 * 
	 * @param ui
	 * @return 
	 */
	private FittingEngine getFittingEngine(UserInterfacePanel ui) {
		if (null == fittingEngine) {
			fittingEngine = new ThreadedFittingEngine();
		}
		fittingEngine.setCurveFitter(getCurveFitter(ui));
		uiPanel.getAlgorithm();
		return fittingEngine;
	}
	
    /*
     * Gets the appropriate curve fitter for the current fit.
     *
     * @param uiPanel has curve fitter selection
     */
    private ICurveFitter getCurveFitter(UserInterfacePanel ui) {
        ICurveFitter curveFitter = null;
        switch (ui.getAlgorithm()) {
            case JAOLHO:
                curveFitter = new JaolhoCurveFitter();
                break;
            case SLIMCURVE_RLD:
                curveFitter = new SLIMCurveFitter();
                curveFitter.setFitAlgorithm(ICurveFitter.FitAlgorithm.SLIMCURVE_RLD);
                break;
            case SLIMCURVE_LMA:
                curveFitter = new SLIMCurveFitter();
                curveFitter.setFitAlgorithm(ICurveFitter.FitAlgorithm.SLIMCURVE_LMA);
                break;
            case SLIMCURVE_RLD_LMA:
                curveFitter = new SLIMCurveFitter();
                curveFitter.setFitAlgorithm(ICurveFitter.FitAlgorithm.SLIMCURVE_RLD_LMA);
                break;
        }
        ICurveFitter.FitFunction fitFunction = null;
        switch (ui.getFunction()) {
            case SINGLE_EXPONENTIAL:
                fitFunction = ICurveFitter.FitFunction.SINGLE_EXPONENTIAL;
                break;
            case DOUBLE_EXPONENTIAL:
                fitFunction = ICurveFitter.FitFunction.DOUBLE_EXPONENTIAL;
                break;
            case TRIPLE_EXPONENTIAL:
                fitFunction = ICurveFitter.FitFunction.TRIPLE_EXPONENTIAL;
                break;
            case STRETCHED_EXPONENTIAL:
                fitFunction = ICurveFitter.FitFunction.STRETCHED_EXPONENTIAL;
                break;
        }
        curveFitter.setEstimator(new DefaultFitterEstimator());
        curveFitter.setFitFunction(fitFunction);
        curveFitter.setNoiseModel(ui.getNoiseModel());
        curveFitter.setXInc(timeInc);
        curveFitter.setFree(translateFree(ui.getFunction(), ui.getFree()));
		//TODO ARG PROMPT get prompt working again:
        /* if (null != _excitationPanel) {
            double[] excitation = null;
            int startIndex = _fittingCursor.getPromptStartBin();
            int stopIndex  = _fittingCursor.getPromptStopBin();
            double base    = _fittingCursor.getPromptBaselineValue();
            excitation = _excitationPanel.getValues(startIndex, stopIndex, base);          
            curveFitter.setInstrumentResponse(excitation);
        } */
        return curveFitter;
    }
	
    /*
     * Handles reordering the array that describes which fit parameters are
     * free (vs. fixed).
     */
    private boolean[] translateFree(ICurveFitter.FitFunction fitFunction, boolean free[]) {
        boolean translated[] = new boolean[free.length];
        switch (fitFunction) {
            case SINGLE_EXPONENTIAL:
                // incoming UI order is A, T, Z
                // SLIMCurve wants Z, A, T
                translated[0] = free[2];
                translated[1] = free[0];
                translated[2] = free[1];
                break;
            case DOUBLE_EXPONENTIAL:
                // incoming UI order is A1 T1 A2 T2 Z
                // SLIMCurve wants Z A1 T1 A2 T2
                translated[0] = free[4];
                translated[1] = free[0];
                translated[2] = free[1];
                translated[3] = free[2];
                translated[4] = free[3];
                break;
            case TRIPLE_EXPONENTIAL:
                // incoming UI order is A1 T1 A2 T2 A3 T3 Z
                // SLIMCurve wants Z A1 T1 A2 T2 A3 T3
                translated[0] = free[6];
                translated[1] = free[0];
                translated[2] = free[1];
                translated[3] = free[2];
                translated[4] = free[3];
                translated[5] = free[4];
                translated[6] = free[5];
                break;
            case STRETCHED_EXPONENTIAL:
                // incoming UI order is A T H Z
                // SLIMCurve wants Z A T H
                translated[0] = free[3];
                translated[1] = free[0];
                translated[2] = free[1];
                translated[3] = free[2];
                break;
        }
        return translated;
    }

    /*
     * Shows the decay curve graph, with fitted results and residuals.
     *
     * @param title
     * @param uiPanel gets updates on dragged/start stop
     * @param data fitted data
     */
    private void showDecayGraph(final String title,
            final UserInterfacePanel uiPanel,
			final FittingCursor fittingCursor,
			final FitResults fitResults)
    {
		if (null == decayGraph) {
			decayGraph = new DefaultDecayGraph();
		}
        JFrame frame = decayGraph.init(uiPanel.getFrame(), bins, timeInc, null); //TODO ARG need grayscale image as 'pixel picker'
		frame.toFront();
        decayGraph.setTitle(title);
        decayGraph.setFittingCursor(fittingCursor);
        double transStart = fittingCursor.getTransientStartTime();
        double dataStart  = fittingCursor.getDataStartTime();
        double transStop  = fittingCursor.getTransientStopTime();
		
        decayGraph.setStartStop(transStart, dataStart, transStop);
		double[] prompt = null;
		int startIndex = 0;
		//TODO ARG PROMPT
		/*
		if (null != _excitationPanel) {
            startIndex = _fittingCursor.getPromptStartBin();
            int stopIndex  = _fittingCursor.getPromptStopBin();
            double base  = _fittingCursor.getPromptBaselineValue();
			prompt = _excitationPanel.getValues(startIndex, stopIndex, base);
		}*/
        decayGraph.setData(startIndex, prompt, fitResults);
		decayGraph.setChiSquare(fitResults.getParams()[0]);
        decayGraph.setPhotons(fitResults.getPhotonCount());
    }
}
