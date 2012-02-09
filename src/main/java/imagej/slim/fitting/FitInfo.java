/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

import java.awt.image.IndexColorModel;

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;
import loci.curvefitter.ICurveFitter.NoiseModel;

/**
 *
 * @author aivar
 */
public class FitInfo {
    private volatile boolean _cancel = false;
    private int _channel;
    private FitRegion _region;
    private FitAlgorithm _algorithm;
    private FitFunction _function;
    private NoiseModel _noiseModel;
    private String _fittedImages;
    private IndexColorModel _indexColorModel;
    private String[] _analysisList;
    private boolean _fitAllChannels;
    private int _startDecay;
    private int _stopDecay;
    private double _xInc;
    private int _threshold; //TODO this s/b accounted for by IInputImage
    private double _chiSquareTarget;
    private String _binning;
    private int _x;
    private int _y;
    private int _parameterCount;
    private double[] _parameters;
    private boolean[] _free;
    private boolean _refineFit;
    
    private double[] _prompt;
    private int _startPrompt;
    private int _stopPrompt;
    private int _lowerPrompt;
    
    private double[] _sig; //TODO sig s/b specified for each pixel!!
    
    private IDecayImage _inputImage; // takes care of width/height/threshold/ROI
    private IFittedImage _outputImage;
    
    //TODO private IBob _cursorMunger;

    //HANDLE SOME OTHER WAY?:
    // analysisList/fittedImages (especially analysis c/b totally post fit)

    public FitInfo() {}

    /**
     * Gets whether to cancel current fit.
     * 
     * @return 
     */
    public boolean getCancel() {
        return _cancel;
    }

    /**
     * Sets whether to cancel current fit.
     * 
     * @param cancel 
     */
    public void setCancel(boolean cancel) {
        _cancel = cancel;
    }

    /**
     * Gets current channel
     * 
     * @return 
     */
    public int getChannel() {
        return _channel;
    }
    
    /**
     * Sets current channel
     * 
     * @param channel 
     */ 
    public void setChannel(int channel) {
        _channel = channel;
    }

    /**
     * Gets region the fit applies to.
     *
     * @return fit region
     */
    public FitRegion getRegion() {
        return _region;
    }

    /**
     * Sets region the fit applies to.
     * 
     * @param region 
     */
    public void setRegion(FitRegion region) {
        _region = region;
    }

    /**
     * Gets implementation & algorithm for the fit.
     *
     * @return fit algorithm.
     */
    public FitAlgorithm getAlgorithm() {
        return _algorithm;
    }

    /**
     * Sets implementation & algorithm for the fit.
     * 
     * @param algorithm 
     */
    public void setAlgorithm(FitAlgorithm algorithm) {
        _algorithm = algorithm;
    }

    /**
     * Gets function to be fitted.
     *
     * @return fit function
     */
    public FitFunction getFunction() {
        return _function;
    }

    /**
     * Sets function to be fitted.
     * 
     * @param function 
     */
    public void setFunction(FitFunction function) {
        _function = function;
    }

    /**
     * Gets noise model for fit.
     *
     * @return
     */
    public NoiseModel getNoiseModel() {
        return _noiseModel;
    }

    /**
     * Sets noise model for fit.
     * 
     * @param noiseModel 
     */
    public void setNoiseModel(NoiseModel noiseModel) {
        _noiseModel = noiseModel;
    }

    /**
     * Returns list of fitted images to display.
     *
     * @return
     */
    public String getFittedImages() {
        return _fittedImages;
    }

    /**
     * Sets list of fitted images to display.
     * 
     * @param fittedImages 
     */
    public void setFittedImages(String fittedImages) {
        _fittedImages = fittedImages;
    }
    
    /**
     * Returns color model for fitted images.
     * 
     * @return
     */
    public IndexColorModel getIndexColorModel() {
        return _indexColorModel;
    }

    /**
     * Sets color model for fitted images.
     * 
     * @param indexColorModel 
     */
    public void setIndexColorModel(IndexColorModel indexColorModel) {
        _indexColorModel = indexColorModel;
    }

    /**
     * Gets analysis plugin names.
     *
     * @return analysis plugin names
     */
    public String[] getAnalysisList() {
        return _analysisList;
    }

    /**
     * Sets analysis plugin names.
     * 
     * @param analysisList 
     */
    public void setAnalysisList(String[] analysisList) {
        _analysisList = analysisList;
    }

    /**
     * Gets whether or not to fit all channels.
     *
     * @return fit all channels
     */
    public boolean getFitAllChannels() {
        return _fitAllChannels;
    }

    /**
     * Sets whether or not to fit all channels.
     * 
     * @param fitAllChannels 
     */
    public void setFitAllChannels(boolean fitAllChannels) {
        _fitAllChannels = fitAllChannels;
    }

    /**
     * Gets starting bin of fit.
     *
     * @return start
     */
    public int getStartDecay() {
        return _startDecay;
    }

    /**
     * Sets starting bin of fit.
     *
     * @param bin
     */
    public void setStartDecay(int bin) {
        _startDecay = bin;
    }

    /**
     * Gets stopping bin of fit (inclusive).
     *
     * @return stop
     */
    public int getStopDecay() {
        return _stopDecay;
    }

    /**
     * Sets stopping bin of fit.
     *
     * @param bin
     */
    public void setStopDecay(int bin) {
        _stopDecay = bin;
    }

    /**
     * Gets x increment for each bin.
     * 
     * @return 
     */
    public double getXInc() {
        return _xInc;
    }

    /**
     * Gets prompt or instrument response function.
     * @return 
     */
    public double[] getPrompt() {
        return _prompt;
    }
    
    /**
     * Sets prompt or instrument response function.
     * 
     * @param prompt 
     */
    public void setPrompt(double[] prompt) {
        _prompt = prompt;
    }

    /**
     * Gets start of prompt.
     * 
     * @return 
     */
    public int getStartPrompt() {
        return _startPrompt;
    }
    
    /**
     * Sets start of prompt.
     * 
     * @param startPrompt 
     */
    public void setStartPrompt(int startPrompt) {
        _startPrompt = startPrompt;
    }
    
    /**
     * Gets end of prompt.
     * 
     * @return 
     */
    public int getStopPrompt() {
        return _stopPrompt;
    }

    /**
     * Sets start of prompt.
     * 
     * @param stopPrompt 
     */
    public void setStopPrompt(int stopPrompt) {
        _stopPrompt = stopPrompt;
    }

    /**
     * Sets x increment for each bin.
     * 
     * @param xInc 
     */
    public void setXInc(double xInc) {
        _xInc = xInc;
    }

    /**
     * Gets photon count threshold to fit a pixel.
     *
     * @return threshold
     */
    public int getThreshold() {
        return _threshold;
    }

    /**
     * Sets photon count threshold to fit a pixel.
     *
     * @param threshold
     */
    public void setThreshold(int threshold) {
        _threshold = threshold;
    }
 
    /**
     * Gets chi square target for fit.
     * 
     * @return 
     */
    public double getChiSquareTarget() {
        return _chiSquareTarget;
    }

    /**
     * Sets chi square target for fit.
     * 
     * @param chiSqTarget 
     */
    public void setChiSquareTarget(double chiSquareTarget) {
        _chiSquareTarget = chiSquareTarget;
    }   

    /**
     * Gets binning plugin name.
     *
     * @return binning plugin name
     */
    public String getBinning() {
        return _binning;
    }

    /**
     * Sets binning plugin name.
     * 
     * @param binning 
     */
    public void setBinning(String binning) {
        _binning = binning;
    }

    /**
     * Gets pixel x.
     *
     * @return x
     */
    public int getX() {
        return _x;
    }

    /**
     * Sets pixel x.
     *
     * @param x
     */
    public void setX(int x) {
        _x = x;
    }

    /**
     * Gets pixel y.
     *
     * @return y
     */
    public int getY() {
        return _y;
    }

    /**
     * Sets pixel y.
     *
     * @param y
     */
    public void setY(int y) {
        _y = y;
    }

    /**
     * Gets number of fit parameters
     *
     * @return count
     */
    public int getParameterCount() {
        return _parameterCount;
    }

    /**
     * Sets number of fitted parameters after a successful fit.
     *
     * @param components
     */
    public void setParameterCount(int count) {
        _parameterCount = count;
    }

    /**
     * Gets the parameters of the fit
     *
     * @return fit parameters
     */
    public double[] getParameters() {
        return _parameters;
    }

    /**
     * Sets the parameters of the fit
     *
     * @param parameters
     */
    public void setParameters(double parameters[]) {
        _parameters = parameters;
    }

    /**
     * Gets which parameters aren't fixed.
     *
     * @return free parameters
     */
    public boolean[] getFree() {
        return _free;
    }

    /**
     * Sets which parameters aren't fixed.
     * 
     */
    public void setFree(boolean[] free) {
        _free = free;;
    }

    /**
     * Gets whether to start next fit with results of last fit.
     *
     * @return
     */
    public boolean getRefineFit() {
        return _refineFit;
    }

    /**
     * Sets whether to start next fit with results of last fit.
     * 
     * @param refineFit 
     */
    public void setRefineFit(boolean refineFit) {
        _refineFit = refineFit;
    }

    /**
     * Gets number of exponential components.
     * 
     * @return 
     */
    public int getComponents() {
        int components = 0;
        switch (_function) {
            case SINGLE_EXPONENTIAL:
            case STRETCHED_EXPONENTIAL:
                components = 1;
                break;
            case DOUBLE_EXPONENTIAL:
                components = 2;
                break;
            case TRIPLE_EXPONENTIAL:
                components = 3;
                break;
        }
        return components;
    }
    
    /**
     * Gets whether or not this is a stretched exponential fit.
     * 
     * @return 
     */
    public boolean getStretched() {
        return FitFunction.STRETCHED_EXPONENTIAL == _function;
    }
}
