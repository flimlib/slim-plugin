/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process.interactive.ui;

//TODO ARG copied wholesale from IJ1 version; imports and methods may be off
import javax.swing.JFrame;
import loci.curvefitter.ICurveFitter;
import loci.slim.IThresholdUpdate;
import loci.slim.ui.IUserInterfacePanelListener;

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

