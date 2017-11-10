/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
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

//TODO ARG copied wholesale from IJ1 version; imports and methods may be off
import javax.swing.JFrame;

import loci.curvefitter.ICurveFitter;

/**
 * UI Panel interface.
 *
 * @author Aivar Grislis
 */
public interface UserInterfacePanel {

	// Unicode special characters
	public static final Character CHI = '\u03c7', SQUARE = '\u00b2',
			TAU_CHAR = '\u03c4', LAMBDA = '\u03bb', SIGMA = '\u03c3',
			SUB_1 = '\u2081', SUB_2 = '\u2082', SUB_3 = '\u2083', SUB_M = '\u2098', // Unicode
																																							// 6.0.0
																																							// (October
																																							// 2010)
			SUB_R = '\u1d63';

	public static final String TAU = "" + TAU_CHAR, TAU1 = TAU + "1", TAU2 = TAU +
		"2", TAU3 = TAU + "3", CHISQUARE = "" + CHI + SQUARE, F_UPPER = "F ",
			F_LOWER = "f ", TAU_MEAN = "" + TAU_CHAR + "m";

	/**
	 * Gets the UI JFrame.
	 *
	 * @return JFrame
	 */
	public JFrame getFrame();

	/**
	 * Sets the listener
	 */
	public void setListener(UserInterfacePanelListener listener);

	/**
	 * Sets the threshold listener.
	 */
	public void setThresholdListener(ThresholdUpdate thresholdListener);

	/**
	 * Disables the UI.
	 */
	public void disable();

	/**
	 * Resets the UI after a fit.
	 */
	public void reset();

	/**
	 * Disables the UI buttons.
	 */
	public void disableButtons();

	/**
	 * Resets the UI buttons after a fit.
	 */
	public void resetButtons();

	/**
	 * Gets region the fit applies to.
	 *
	 * @return fit region
	 */
	public ICurveFitter.FitRegion getRegion();

	/**
	 * Gets implementation and algorithm for the fit.
	 *
	 * @return fit algorithm.
	 */
	public ICurveFitter.FitAlgorithm getAlgorithm();

	/**
	 * Gets function to be fitted.
	 *
	 * @return fit function
	 */
	public ICurveFitter.FitFunction getFunction();

	/**
	 * Gets noise model for fit.
	 *
	 */
	public ICurveFitter.NoiseModel getNoiseModel();

	/**
	 * Returns list of fitted images to display.
	 *
	 */
	public String getFittedImages();

	/**
	 * Returns whether to create colorized grayscale fitted images.
	 *
	 */
	public boolean getColorizeGrayScale();

	/**
	 * Gets analysis plugin names.
	 *
	 * @return analysis plugin names
	 */
	public String[] getAnalysisList();

	/**
	 * Gets whether or not to fit all channels.
	 *
	 * @return fit all channels
	 */
	public boolean getFitAllChannels();

	/**
	 * Gets photon count threshold minimum to fit a pixel.
	 *
	 * @return threshold
	 */
	public int getThresholdMinimum();

	/**
	 * Sets photon count threshold minimum to fit a pixel.
	 *
	 */
	public void setThresholdMinimum(int thresholdMin);

	/**
	 * Gets photon count threshold maximum to fit a pixel.
	 *
	 * @return threshold
	 */
	public int getThresholdMaximum();

	/**
	 * Sets photon count threshold maximum to fit a pixel.
	 *
	 */
	public void setThresholdMaximum(int thresholdMax);

	/**
	 * Gets chi square target for fit.
	 *
	 */
	public double getChiSquareTarget();

	/**
	 * Sets chi square target for fit.
	 *
	 */
	public void setChiSquareTarget(double chiSqTarget);

	/**
	 * Gets binning index.
	 *
	 * @return binning index
	 */
	public int getBinning();

	/**
	 * Gets pixel x.
	 *
	 * @return x
	 */
	public int getX();

	/**
	 * Sets pixel x.
	 *
	 */
	public void setX(int x);

	/**
	 * Gets pixel y.
	 *
	 * @return y
	 */
	public int getY();

	/**
	 * Sets pixel y.
	 *
	 */
	public void setY(int y);

	/**
	 * Gets number of fit parameters
	 *
	 * @return count
	 */
	public int getParameterCount();

	/**
	 * Sets number of fitted parameters after a successful fit.
	 *
	 */
	public void setFittedParameterCount(int count);

	/**
	 * Gets the parameters of the fit
	 *
	 * @return fit parameters
	 */
	public double[] getParameters();

	/**
	 * Sets the parameters of the fit
	 *
	 */
	public void setParameters(double parameters[], double AIC);

	/**
	 * Sets the parameters of the fit by function.
	 *
	 * @param function index
	 */
	public void setFunctionParameters(int function, double parameters[]);

	/**
	 * Gets which parameters aren't fixed.
	 *
	 * @return free parameters
	 */
	public boolean[] getFree();

	/**
	 * Gets whether to start next fit with results of last fit.
	 *
	 */
	public boolean getRefineFit();

	/**
	 * Experimental: gets a scatter factor from UI. SPC Image can fit an
	 * additional scatter parameter; this is an attempt to see if scatter
	 * correction helps the fit.
	 *
	 */
	public double getScatter();
}
