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
     * Gets implementation & algorithm for the fit.
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
     * @return
     */
    public ICurveFitter.NoiseModel getNoiseModel();

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

