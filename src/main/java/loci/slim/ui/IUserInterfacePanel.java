/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
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

package loci.slim.ui;

import javax.swing.JFrame;

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.slim.IThresholdUpdate;

/**
 * Interface to the User Interface Panel
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/IUserInterfacePanel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/IUserInterfacePanel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public interface IUserInterfacePanel {

    /**
     * Gets the UI JFrame.
     *
     * @return JFrame
     */
    public JFrame getFrame();

    /**
     * Sets the listener
     */
    public void setListener(IUserInterfacePanelListener listener);

	/**
	 * Sets the threshold listener.
	 */
	public void setThresholdListener(IThresholdUpdate thresholdListener);
	
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
    public FitRegion getRegion();

    /**
     * Gets implementation & algorithm for the fit.
     *
     * @return fit algorithm.
     */
    public FitAlgorithm getAlgorithm();

    /**
     * Gets function to be fitted.
     *
     * @return fit function
     */
    public FitFunction getFunction();

    /**
     * Gets noise model for fit.
     *
     * @return
     */
    public NoiseModel getNoiseModel();

    /**
     * Returns list of fitted images to display.
     *
     * @return
     */
    public String getFittedImages();

    /**
     * Returns whether to create colorized grayscale fitted images.
     * 
     * @return 
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
     * Gets photon count threshold to fit a pixel.
     *
     * @return threshold
     */
    public int getThreshold();

    /**
     * Sets photon count threshold to fit a pixel.
     *
     * @param threshold
     */
    public void setThreshold(int threshold);
 
    /**
     * Gets chi square target for fit.
     * 
     * @return 
     */
    public double getChiSquareTarget();

    /**
     * Sets chi square target for fit.
     * 
     * @param chiSqTarget 
     */
    public void setChiSquareTarget(double chiSqTarget);   

    /**
     * Gets binning plugin name.
     *
     * @return binning plugin name
     */
    public String getBinning();

    /**
     * Gets pixel x.
     *
     * @return x
     */
    public int getX();

    /**
     * Sets pixel x.
     *
     * @param x
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
     * @param y
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
     * @param components
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
     * @param parameters
	 * @param AIC
     */
    public void setParameters(double parameters[], double AIC);

    /**
     * Sets the parameters of the fit by function.
     *
     * @param function index
     * @param parameters
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
     * @return
     */
    public boolean getRefineFit();

	/**
	 * Experimental: gets a scatter factor from UI.  SPC Image can fit an
	 * additional scatter parameter; this is an attempt to see if scatter
	 * correction helps the fit.
	 * 
	 * @return 
	 */
	public double getScatter();
}
