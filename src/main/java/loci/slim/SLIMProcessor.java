//
// SLIMProcessor.java
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

package loci.slim;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import loci.curvefitter.CurveFitData;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;
import loci.curvefitter.IFitterEstimator;
import loci.curvefitter.JaolhoCurveFitter;
import loci.curvefitter.SLIMCurveFitter;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.ImageReader;
import loci.slim.analysis.SLIMAnalysis;
import loci.slim.preprocess.ISLIMBinner;
import loci.slim.preprocess.SLIMBinning;
import loci.slim.heuristics.FitterEstimator;
import loci.slim.heuristics.CursorEstimator;
import loci.slim.preprocess.IProcessor;
import loci.slim.preprocess.Threshold;
import loci.slim.ui.DecayGraph;
import loci.slim.ui.ExcitationPanel;
import loci.slim.ui.IDecayGraph;
import loci.slim.ui.IUserInterfacePanel;
import loci.slim.ui.IUserInterfacePanelListener;
import loci.slim.ui.UserInterfacePanel;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.DoubleType;

// Kludge in the new stuff:
import loci.slim.fitting.IDecayImage;
import loci.slim.fitting.IFittedImage;
import loci.slim.fitting.config.Configuration;
import loci.slim.fitting.cursor.FittingCursor;
import loci.slim.fitting.cursor.FittingCursorHelper;
import loci.slim.fitting.cursor.IFittingCursorListener;
import loci.slim.fitting.engine.IFittingEngine;
import loci.slim.fitting.images.FittedImageParser;
import loci.slim.fitting.params.IGlobalFitParams;
import loci.slim.fitting.params.LocalFitParams;
import loci.slim.fitting.params.GlobalFitParams;
import loci.slim.fitting.params.ILocalFitParams;
import loci.slim.fitting.params.IFitResults;

import loci.slim.fitting.FitInfo;
import loci.slim.fitting.images.FittedImageFitter;
import loci.slim.fitting.images.FittedImageFitter.FittedImageType;
import loci.slim.heuristics.Estimator;
import loci.slim.heuristics.IEstimator;
import loci.slim.histogram.HistogramTool;

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;

/**
 * SLIMProcessor is the main class of the SLIM Plugin. It was originally just
 * thrown together to get something working, with some code/techniques borrowed
 * from SLIM Plotter. Parts of this code are ugly and experimental.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/SLIMProcessor.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/SLIMProcessor.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class SLIMProcessor <T extends RealType<T>> {
    private static boolean OLD_STYLE = false;
    
    private IFittingEngine _fittingEngine;
    private FittingCursor _fittingCursor;
    
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String LIFETIME = "Lifetime";
    private static final String CHANNELS = "Channels";
    private static final boolean TABBED = true;
    private static final boolean USE_TAU = true;
    private static final boolean USE_LAMBDA = false;

    // this affects how many pixels we process at once
    private static final int PIXEL_COUNT = 128;

    // Unicode special characters
    private static final Character CHI    = '\u03c7';
    private static final Character SQUARE = '\u00b2';
    private static final Character TAU    = '\u03c4';
    private static final Character LAMBDA = '\u03bb';
    private static final Character SIGMA  = '\u03c3';
    private static final Character SUB_1  = '\u2081';
    private static final Character SUB_2  = '\u2082';
    private static final Character SUB_3  = '\u2083';
    
    private IUserInterfacePanel _uiPanel;

    private final Object _synchFit = new Object();
    private volatile boolean _quit;
    private volatile boolean _cancel;
    private volatile boolean _fitInProgress;
    private volatile boolean _fitted;

    private static final String FILE_KEY = "file";
    private static final String PATH_KEY = "path";
    private String _file;
    private String _path;
    private Hashtable<String, Object> _globalMetadata;

    private Image<T> _image;
    private LocalizableByDimCursor<T> _cursor;

    private Image<DoubleType> _fittedImage = null;
    private int _fittedParameterCount = 0;
    boolean _visibleFit = true;

    // data parameters
    private boolean _hasChannels;
    private int _channels;
    private int _channelIndex;
    private int _width;
    private int _height;
    private int[] _cLengths;
    private int _bins;
    private int _binIndex;

    private double _timeRange;
    private int _increment;
    private double _minNonZeroPhotonCount;

    private FitRegion _region;
    private FitAlgorithm _algorithm;
    private FitFunction _function;

    private SLIMAnalysis _analysis;
    private SLIMBinning _binning;

    private ExcitationPanel _excitationPanel = null;
    private IGrayScaleImage _grayScaleImage;
    // user sets this from the grayScalePanel control
    private int _channel;
    private boolean _fitAllChannels;

    // current x, y
    private int _x;
    private int _y;

    private double[] _param = new double[7];
    private boolean[] _free = { true, true, true, true, true, true, true };

    private int _startBin;
    private int _stopBin;
    private int _startX;
    private int _threshold;
    private float _chiSqTarget;
    
    private FitInfo _fitInfo;

    private int _debug = 0;

    public SLIMProcessor() {
        _analysis = new SLIMAnalysis();
        _binning = new SLIMBinning();
        _quit = false;
        _cancel = false;
        _fitInProgress = false;
        _fitted = false;
    }

    public void processImage(Image<T> image) {
        boolean success = false;

        _image = image;
        if (getImageInfo(image)) {
            // show the UI; do fits
            doFits();
        }
    }

    /**
     * Run method for the plugin.  Throws up a file dialog.
     *
     * @param arg
     */
    public void process(String arg) {
        boolean success = false;
        if (showFileDialog(getFileFromPreferences())) {
            _image = loadImage(_path, _file);
            if (null == _image) {
                System.out.println("image is null");
            }
            if (getImageInfo(_image)) {
                savePathAndFileInPreferences(_path, _file);
                success = true;
            }
        }
        
        if (success) {
            // show the UI; do fits
            doFits();
        }
    }

    /**
     * Creates a user interface panel.  Shows a grayscale
     * version of the image.
     *
     * Loops until quitting time and handles fit requests.
     * Fitting is driven by a button on the UI panel which
     * sets the global _fitInProgress.
     *
     * @param uiPanel
     */
    private void doFits() {
        // heuristics
        IEstimator estimator = new Estimator();
        IFitterEstimator fitterEstimator = new FitterEstimator();
        
        // cursor support
        _fittingCursor = new FittingCursor(_timeRange, _bins, fitterEstimator);
        _fittingCursor.addListener(new FittingCursorListener());
        
        // show the UI; do fits
        FittingCursorHelper fittingCursorHelper = new FittingCursorHelper();
        fittingCursorHelper.setFittingCursor(_fittingCursor);
        final IUserInterfacePanel uiPanel = new UserInterfacePanel(TABBED,
                USE_TAU, _analysis.getChoices(), _binning.getChoices(),
                fittingCursorHelper, fitterEstimator);
        _uiPanel = uiPanel; //TODO almost got by having it just be a local variable
        uiPanel.setX(0);
        uiPanel.setY(0);
        //TODO ARG these estimates s/n/b necessary; use the EstimateCursors class
        //uiPanel.setStart(estimator.getStart(_bins));
        //uiPanel.setStop(estimator.getStop(_bins));
        uiPanel.setThreshold(estimator.getThreshold());
        uiPanel.setChiSquareTarget(estimator.getChiSquareTarget());
        uiPanel.setFunctionParameters(0, estimator.getParameters(1, false));
        uiPanel.setFunctionParameters(1, estimator.getParameters(2, false));
        uiPanel.setFunctionParameters(2, estimator.getParameters(3, false));
        uiPanel.setFunctionParameters(3, estimator.getParameters(0, true));
        uiPanel.setListener(
            new IUserInterfacePanelListener() {
                /**
                 * Triggers a fit.
                 */
                @Override
                public void doFit() {
                    _cancel = false;
                    _fitInProgress = true;
                }

                /**
                 * Cancels ongoing fit.
                 */
                @Override
                public void cancelFit() {
                    _cancel = true;
                    if (null != _fitInfo) {
                        _fitInfo.setCancel(true);
                    }
                }
                
                /**
                 * Quits running plugin.
                 */
                @Override
                public void quit() {
                    _quit = true;
                }

                /**
                 * Loads an excitation curve from file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                @Override
                public boolean loadExcitation(String fileName) {
                    Excitation excitation = ExcitationFileHandler.getInstance().loadExcitation(fileName, _timeRange);
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
                    int channel = 0;
                    if (null != _grayScaleImage) {
                        channel = _grayScaleImage.getChannel();
                    }
                    int x = uiPanel.getX();
                    int y = uiPanel.getY();
                    double[] values = new double[_bins];
                    for (int b = 0; b < _bins; ++b) {
                        values[b] = getData(_cursor, channel, x, y, b);
                    }
                    Excitation excitation = ExcitationFileHandler.getInstance().createExcitation(fileName, values, _timeRange);
                    return updateExcitation(uiPanel, excitation);
                }

                /**
                 * Cancels the current excitation curve, if any.
                 *
                 */
                @Override
                public void cancelExcitation() {
                    if (null != _excitationPanel) {
                        _excitationPanel.quit();
                        _excitationPanel = null;
                        updateExcitation(null, null);
                        //TODO redo stop/start cursors on decay curve?
                    }
                }

                /**
                 * Estimates prompt and decay cursors.
                 */
                @Override
                public void estimateCursors() {
                    double xInc = _timeRange;
                    
                    double[] prompt = null;
                    if (null != _excitationPanel) {
                        prompt = _excitationPanel.getRawValues();
                    }
                    double[] decay = new double[_bins];
                    for (int b = 0; b < _bins; ++b) {
                        decay[b] = getData(_cursor, _channel, _x, _y, b);
                    }
                    
                    double chiSqTarget = _uiPanel.getChiSquareTarget();
                    System.out.println("chiSqTarget is " + chiSqTarget);
//                    System.out.println("prompt is " + prompt + " and fitting cursor thinks prompt " + _fittingCursor.getHasPrompt());
                    if (null != prompt && _fittingCursor.getHasPrompt()) {
                        double[] results = CursorEstimator.estimateCursors
                                (xInc, prompt, decay, chiSqTarget);
                        
                        // want all the fitting cursor listeners to get everything at once
                        _fittingCursor.suspendNotifications();
                        _fittingCursor.setHasPrompt(true);
                        _fittingCursor.setPromptStartBin
                                ((int) results[CursorEstimator.PROMPT_START]);
                        _fittingCursor.setPromptStopBin
                                ((int) results[CursorEstimator.PROMPT_STOP]);
                        _fittingCursor.setPromptBaselineValue
                                (results[CursorEstimator.PROMPT_BASELINE]);
                        _fittingCursor.setTransientStartBin
                                ((int) results[CursorEstimator.TRANSIENT_START]);
                        _fittingCursor.setDataStartBin
                                ((int) results[CursorEstimator.DATA_START]);
                        _fittingCursor.setTransientStopBin
                                ((int) results[CursorEstimator.TRANSIENT_STOP]);
                        _fittingCursor.sendNotifications();
                    }
                    else
                    {
                        int[] results = CursorEstimator.estimateDecayCursors
                                (xInc, decay);
                        
                        // want all the fitting cursor listeners to get everything at once
                        _fittingCursor.suspendNotifications();
                        _fittingCursor.setHasPrompt(false);
                        _fittingCursor.setTransientStartBin(results[CursorEstimator.TRANSIENT_START]);
                        _fittingCursor.setDataStartBin(results[CursorEstimator.DATA_START]);
                        _fittingCursor.setTransientStopBin(results[CursorEstimator.TRANSIENT_STOP]);
                        _fittingCursor.sendNotifications();
                    }
                }
            }
        );
        uiPanel.getFrame().setLocationRelativeTo(null);
        uiPanel.getFrame().setVisible(true);

        // create a grayscale image from the data
        _grayScaleImage = new GrayScaleImage(_image);
        _grayScaleImage.setListener(
            new ISelectListener() {
                @Override
                public void selected(int channel, int x, int y) {
                    // just ignore clicks during a fit
                    if (!_fitInProgress) {
                        synchronized (_synchFit) {
                            float zoomFactor = ((GrayScaleImage)_grayScaleImage).getZoomFactor();
                            x *= zoomFactor;
                            y *= zoomFactor;
                            
                            _x = x;
                            _y = y; //TODO ARG 4/6/12 trying to fix my flakey bug
                            
                            uiPanel.setX(x);
                            uiPanel.setY(y);
                            getFitSettings(_grayScaleImage, uiPanel, _fittingCursor);
                            // fit on the pixel clicked
                            fitPixel(uiPanel, _fittingCursor);
                        }
                    }
                }
            }
        );
        // get a correction factor for photon counts
        _minNonZeroPhotonCount = _grayScaleImage.getMinNonZeroPhotonCount();

        // what is the brightest point in the image?
        int[] brightestPoint = _grayScaleImage.getBrightestPoint();
        _x = brightestPoint[0];
        _y = brightestPoint[1];
        uiPanel.setX(_x);
        uiPanel.setY(_y);

        // set start and stop for now; will be updated if we load an excitation curvce
        updateDecayCursors(uiPanel);
        
        // fit on the brightest pixel
        getFitSettings(_grayScaleImage, uiPanel, _fittingCursor);
        fitPixel(uiPanel, _fittingCursor); 

        // processing loop; waits for UI panel input
        while (!_quit) {
            while (!_fitInProgress) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {

                }
                if (_quit) {
                    hideUIPanel(uiPanel);
                    return;
                }
            }

            //uiPanel.enable(false); //TODO this might be better to be same as grayScalePanel
            _grayScaleImage.enable(false);

            // get settings of requested fit
            getFitSettings(_grayScaleImage, uiPanel, _fittingCursor);

            // do the fit
            System.out.println("// do the fit!!!");
            fitData(uiPanel);

            _fitInProgress = false;
            //uiPanel.enable(true);
            _grayScaleImage.enable(true);
            uiPanel.reset();
        }
        hideUIPanel(uiPanel);
    }

    private void hideUIPanel(IUserInterfacePanel uiPanel) {
        _grayScaleImage.setListener(null);
        //TODO uiPanel is still hooked up as start stop listeners to decay curves!
        uiPanel.getFrame().setVisible(false);
    }

    /**
     * This method gives an initial estimate of the decay cursors (start and
     * stop values).
     * 
     * @param uiPanel 
     */
    private void updateDecayCursors(IUserInterfacePanel uiPanel) {
        // get selected channel
        int channel = 0;
        if (null != _grayScaleImage) {
            channel = _grayScaleImage.getChannel();
        }
        double[] decay = new double[_bins];
        for (int b = 0; b < _bins; ++b) {
            decay[b] = getData(_cursor, channel, _x, _y, b);
        }
        int[] results = CursorEstimator.estimateDecayCursors(_timeRange, decay);
        int transientStart = results[CursorEstimator.TRANSIENT_START];
        int dataStart = results[CursorEstimator.DATA_START];
        int transientStop = results[CursorEstimator.TRANSIENT_STOP];

        // want to batch all of the fitting cursor notifications to listeners
        _fittingCursor.suspendNotifications();
        _fittingCursor.setTransientStartBin(transientStart);
        _fittingCursor.setDataStartBin(dataStart);
        _fittingCursor.setTransientStopBin(transientStop);
        _fittingCursor.sendNotifications();
    }

    /**
     * This method sums the decay for all channels of all pixels.
     * 
     * @return 
     */
    private double[] getSummedDecay() {
        double[] decay = new double[_bins];
        for (int i = 0; i < decay.length; ++i) {
            decay[i] = 0.0;
        }
        for (int y = 0; y < _height; ++y) {
            for (int x = 0; x < _width; ++x) {
                for (int c = 0; c < _channels; ++c) {
                    for (int b = 0; b < _bins; ++b) {
                        decay[b] += getData(_cursor, c, x, y, b);
                    }
                }
            }
        }
        return decay;
    }

    /*
     * This method is called when a new excitation is loaded.
     */
    private boolean updateExcitation(IUserInterfacePanel uiPanel, Excitation excitation) {
        boolean success = false;
        System.out.println("###update excitation####" + excitation);
        if (null != excitation) {
            if (null != _excitationPanel) {
                _excitationPanel.quit();
            }

            // get selected channel
            int channel = 0;
            if (null != _grayScaleImage) {
                channel = _grayScaleImage.getChannel();
            }
            double[] decay = new double[_bins];
            for (int b = 0; b < _bins; ++b) {
                decay[b] = getData(_cursor, channel, _x, _y, b);
            }

            double chiSqTarget = uiPanel.getChiSquareTarget();
            System.out.println("chiSqTarget is " + chiSqTarget);
            double[] results = CursorEstimator.estimateCursors
                    (_timeRange, excitation.getValues(), decay, chiSqTarget);
            
            // want all the fitting cursor listeners to get everything at once
            _fittingCursor.suspendNotifications();
            _fittingCursor.setHasPrompt(true);
            _fittingCursor.setPromptStartBin   ((int) results[CursorEstimator.PROMPT_START]);
            _fittingCursor.setPromptStopBin    ((int) results[CursorEstimator.PROMPT_STOP]);
            _fittingCursor.setPromptBaselineValue    (results[CursorEstimator.PROMPT_BASELINE]);
            _fittingCursor.setTransientStartBin((int) results[CursorEstimator.TRANSIENT_START]);
            _fittingCursor.setDataStartBin     ((int) results[CursorEstimator.DATA_START]);
            _fittingCursor.setTransientStopBin ((int) results[CursorEstimator.TRANSIENT_STOP]);
            _fittingCursor.sendNotifications();

            _excitationPanel = new ExcitationPanel(excitation, _fittingCursor); //TODO ARG excitation cursor change refit problem here; get new values before excitation ready for refit

            success = true;
        }
        else {
            _fittingCursor.setHasPrompt(false);
        }
        return success;
    }

    private void getFitSettings(IGrayScaleImage grayScalePanel, IUserInterfacePanel uiPanel, FittingCursor cursor) {
        _channel        = grayScalePanel.getChannel();

        _region         = uiPanel.getRegion();
        _algorithm      = uiPanel.getAlgorithm();
        _function       = uiPanel.getFunction();
        _fitAllChannels = uiPanel.getFitAllChannels();

        _x              = uiPanel.getX();
        _y              = uiPanel.getY();
        _threshold      = uiPanel.getThreshold();

        _param          = uiPanel.getParameters();
        _free           = uiPanel.getFree();
        
        _startBin       = cursor.getTransientStartBin();
        _stopBin        = cursor.getTransientStopBin();
    }

    /**
     * Prompts for a FLIM file.
     *
     * @param defaultFile
     * @return
     */
    private boolean showFileDialog(String[] defaultPathAndFile) {
        OpenDialog dialog = new OpenDialog("Load Data", _path, _file);
        _path = dialog.getDirectory();
        _file = dialog.getFileName();
//        System.out.println("directory is " + dialog.getDirectory());
//        System.out.println("file is " + dialog.getFileName());
        return true;
    }

    private Image<T> loadImage(String path, String file) {
        boolean threwException = false;
        ImageOpener imageOpener = new ImageOpener();
        Image<T> image = null;
        try {
            image = imageOpener.openImage(path + file);
        }
        //catch (java.io.IOException e) { }
        //catch (loci.formats.FormatException e) {
        catch (Exception e) {
            System.out.println("Exception message: " + e.getMessage());
            threwException = true;
        }
        if (null == image && !threwException) {
            System.out.println("imageOpener returned null image");
        }

        // Open the file again, just to get metadata
        ImageReader imageReader = new ImageReader();
        try {
            imageReader.setId(path + file);
            _globalMetadata = imageReader.getGlobalMetadata();
            imageReader.close();
        }
        catch (IOException e) {       
        }
        catch (FormatException e) { 
        }
        
        return image;
    }
   
    private boolean getImageInfo(Image<T> image) {
        int[] dimensions = new int[0];
        try {
            dimensions = image.getDimensions();
            System.out.println("dimensions size is " + dimensions.length);
            for (int i : dimensions) {
                System.out.print("" + i + " ");
            }
            System.out.println();
        }
        catch (NullPointerException e) {
            System.out.println("image.getDimensions throws NullPointerException " + e.getMessage());
            System.out.println("can't detect channels");
        }
        Integer xIndex, yIndex, lifetimeIndex, channelIndex;
        _width = ImageUtils.getWidth(image);
        _height = ImageUtils.getHeight(image);
        _channels = ImageUtils.getNChannels(image);
        //TODO this is broken; returns 1 when there are 16 channels; corrected below
        System.out.println("ImageUtils.getNChannels returns " + _channels);
        _hasChannels = false;
        if (dimensions.length > 3) {
            _hasChannels = true;
            _channelIndex = 3;
            _channels = dimensions[_channelIndex];
        }
        System.out.println("corrected to " + _channels);
        _bins = ImageUtils.getDimSize(image, FormatTools.LIFETIME);
        _binIndex = 2;
        System.out.println("width " + _width + " height " + _height + " timeBins " + _bins + " channels " + _channels);
        _cursor = image.createLocalizableByDimCursor();
        
        _timeRange = 10.0f;
        if (null != _globalMetadata) {
            Number timeBase = (Number) _globalMetadata.get("time base");
            if (null != timeBase) {
                _timeRange = timeBase.floatValue();
            }
            _increment = 1;
            Number increment = (Number) _globalMetadata.get("MeasureInfo.incr");
            if (null != increment) {
                _increment = increment.intValue();
                System.out.println("MeasureInfo.incr is " + _increment);
            }
        }
        _timeRange /= _bins;

        return true;
    }

    /**
     * Restores path and file name from Java Preferences.
     *
     * @return String array with path and file name
     */
    private String[] getFileFromPreferences() {
       Preferences prefs = Preferences.userNodeForPackage(this.getClass());
       String path = prefs.get(PATH_KEY, "");
       String file = prefs.get(FILE_KEY, "");
       
       System.out.println("path " + path + " file " + file);
       return new String[] { prefs.get(PATH_KEY, ""), prefs.get(FILE_KEY, "") };
    }

    /**
     * Saves the path and file names to Java Preferences.
     *
     * @param path
     * @param file
     */
    private void savePathAndFileInPreferences(String path, String file) {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put(PATH_KEY, path);
        prefs.put(FILE_KEY, file);
    }

    /*
     * Fits the data as requested by UI.
     */
    private void fitData(IUserInterfacePanel uiPanel) {
        Image<DoubleType> fittedImage = null;
        // only one fit at a time
        synchronized (_synchFit) {
            
            switch (_region) {
                case SUMMED:
                    // sum all pixels
                    fittedImage = fitSummed(uiPanel);
                    break;
                case ROI:
                    // fit summed ROIs
                    fittedImage = fitROIs(uiPanel);
                    break;
                case POINT:
                    // fit single pixel
                    fittedImage = fitPixel(uiPanel, _x, _y);
                    break;
                case EACH:
                    // fit every pixel
                    fittedImage = fitImage(uiPanel);
                    break;
            }
        }
        if (null != fittedImage) {
            for (String analysis : uiPanel.getAnalysisList()) {
                _analysis.doAnalysis(analysis, fittedImage, uiPanel.getRegion(), uiPanel.getFunction()); //TODO get from uiPanel or get from global?  re-evaluate approach here
            }
        }
    }
    
    private FitInfo getFitInfo(
            IGrayScaleImage grayScalePanel,
            IUserInterfacePanel uiPanel,
            FittingCursor fittingCursor) {
        FitInfo fitInfo = new FitInfo();
        fitInfo.setChannel(grayScalePanel.getChannel());
        fitInfo.setRegion(uiPanel.getRegion());
        fitInfo.setAlgorithm(uiPanel.getAlgorithm());
        fitInfo.setFunction(uiPanel.getFunction());
        fitInfo.setNoiseModel(uiPanel.getNoiseModel());
        fitInfo.setFittedImages(uiPanel.getFittedImages());
        fitInfo.setColorizeGrayScale(uiPanel.getColorizeGrayScale());
        fitInfo.setAnalysisList(uiPanel.getAnalysisList());
        fitInfo.setFitAllChannels(uiPanel.getFitAllChannels());
        fitInfo.setTransientStart(fittingCursor.getTransientStartBin());
        fitInfo.setDataStart(fittingCursor.getDataStartBin());
        fitInfo.setTransientStop(fittingCursor.getTransientStopBin());
        fitInfo.setThreshold(uiPanel.getThreshold());
        fitInfo.setChiSquareTarget(uiPanel.getChiSquareTarget());
        fitInfo.setBinning(uiPanel.getBinning());   
        fitInfo.setX(uiPanel.getX());
        fitInfo.setY(uiPanel.getY());
        fitInfo.setParameterCount(uiPanel.getParameterCount());
        fitInfo.setParameters(uiPanel.getParameters());
        fitInfo.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));;
        fitInfo.setRefineFit(uiPanel.getRefineFit());
        return fitInfo;
    }

    /**
     * Fits all the pixels in the image.  Gets fit settings from the UI panel
     * and various globals.
     * 
     * @param uiPanel
     * @return 
     */
    private Image<DoubleType> fitImage(IUserInterfacePanel uiPanel) {
        // get fit settings from the UI panel
        FitInfo fitInfo = getFitInfo(_grayScaleImage, uiPanel, _fittingCursor);
        fitInfo.setXInc(_timeRange);
        if (_fittingCursor.getHasPrompt() && null != _excitationPanel) {
            int startIndex = _fittingCursor.getPromptStartBin();
            int stopIndex  = _fittingCursor.getPromptStopBin();
            double base  = _fittingCursor.getPromptBaselineValue();   
            double[] values = _excitationPanel.getValues(startIndex, stopIndex, base);
            fitInfo.setPrompt(values);
        }
        fitInfo.setIndexColorModel(HistogramTool.getIndexColorModel());
        _fitInfo = fitInfo;
        
        // set up images
        IDecayImage decayImage = new DecayImageWrapper(_image, _width, _height, _channels, _bins, _binIndex, _increment);
        IFittedImage previousImage = null;
        int width = decayImage.getWidth();
        int height = decayImage.getHeight();
        int channels = 1;
        if (fitInfo.getFitAllChannels()) {
            channels = decayImage.getChannels();
        }
        int parameters = fitInfo.getParameterCount();
        IFittedImage newImage = new OutputImageWrapper(width, height, channels, parameters);
        
        // set up preprocessor chain
        IProcessor processor = decayImage;
        ISLIMBinner binner = _binning.getBinner(uiPanel.getBinning());
        if (null != binner) {
            binner.init(_width, _height);
            binner.chain(processor);
            processor = binner;
        }
        if (fitInfo.getThreshold() > 0) {
            IProcessor threshold = new Threshold(fitInfo.getDataStart(), fitInfo.getTransientStop(), fitInfo.getThreshold());
            threshold.chain(processor);
            processor = threshold;
        }
        
        // create a fitting engine to use
        IFittingEngine fittingEngine = Configuration.getInstance().getFittingEngine();
        ICurveFitter curveFitter = getCurveFitter(uiPanel); //TODO ARG shouldn't all UI panel info go into FitInfo???
        fittingEngine.setCurveFitter(curveFitter);
        
        return fitImage(fittingEngine, fitInfo, decayImage, processor, previousImage, newImage);

    }

    /**
     * Fits all the pixels in the image.
     * 
     * @param fittingEngine fitting code to use
     * @param fitInfo fit settings
     * @param decayImage contains the decay data
     * @param previousImage previous fit results, may be null
     * @param newImage results of this fit
     * @return 
     */
    private Image<DoubleType> fitImage(
            IFittingEngine fittingEngine,
            FitInfo fitInfo,
            IDecayImage decayImage,
            IProcessor processor, //TODO ARG really need both decayImage & processor?  Processor is a poor name
            IFittedImage previousImage,
            IFittedImage newImage) {
 
        // get commonly-used items in local variables
        int width = decayImage.getWidth();
        int height = decayImage.getHeight();
        int channels = decayImage.getChannels();
        int bins = decayImage.getBins();
        int channel = fitInfo.getChannel();
        boolean fitAllChannels = fitInfo.getFitAllChannels();

        // needed to display progress bar
        int pixelCount = 0;
        int totalPixelCount = totalPixelCount(width, height, channels, fitAllChannels);
        int pixelsToProcessCount = 0;
 
        // handle optionally producing colorized images during the fit
        int colorizedChannels = 1;
        if (fitAllChannels) {
            colorizedChannels = channels;
        }
        int[] dimension = new int[] { width, height, colorizedChannels };
        FittedImageFitter imageColorizer = null;
        String outputs = fitInfo.getFittedImages();
        if (null != outputs) {
            int components = fitInfo.getComponents();
            boolean stretched = fitInfo.getStretched();
            FittedImageParser parser =
                    new FittedImageParser(outputs, components, stretched,
                            fitInfo.getFree());
            FittedImageType[] outputImages = parser.getColorizedImages();
            imageColorizer = new FittedImageFitter();
            imageColorizer.setUpFit(
                    outputImages,
                    dimension,
                    fitInfo.getIndexColorModel(),
                    components,
                    fitInfo.getColorizeGrayScale(),
                    _grayScaleImage);
            imageColorizer.beginFit();
        }
  
        // set up global, image-wide fit parameters
        //TODO revisit all of these
        IGlobalFitParams globalFitParams = new GlobalFitParams();
        globalFitParams.setEstimator(new FitterEstimator());
        globalFitParams.setFitAlgorithm(fitInfo.getAlgorithm());
        globalFitParams.setFitFunction(fitInfo.getFunction());
        globalFitParams.setNoiseModel(fitInfo.getNoiseModel());
        globalFitParams.setTransientStart(fitInfo.getTransientStart());
        globalFitParams.setDataStart(fitInfo.getDataStart());
        globalFitParams.setTransientStop(fitInfo.getTransientStop());
        globalFitParams.setXInc(fitInfo.getXInc());
        globalFitParams.setPrompt(fitInfo.getPrompt());
        globalFitParams.setStartPrompt(fitInfo.getStartPrompt());
        globalFitParams.setStopPrompt(fitInfo.getStopPrompt());
        globalFitParams.setChiSquareTarget(fitInfo.getChiSquareTarget());
        globalFitParams.setFree(fitInfo.getFree());
     
        // initialize class used for 'chunky pixel' effect
        IChunkyPixelTable chunkyPixelTable = new ChunkyPixelTableImpl();
        
        List<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();       
        List<ILocalFitParams> localFitParamsList = new ArrayList<ILocalFitParams>();
       
        // loop over all channels or just the current one
        for (int c : getChannelIndices(fitAllChannels, channel, channels)) {
            // 'chunky pixel' effect: draw staggered pixels, not sequential
            ChunkyPixelEffectIterator pixelIterator =
                    new ChunkyPixelEffectIterator(chunkyPixelTable, width, height);
            
            while (!fitInfo.getCancel() && pixelIterator.hasNext()) {
                IJ.showProgress(++pixelCount, totalPixelCount);
                ChunkyPixel pixel = pixelIterator.next();

                // compute full location information
                int x = pixel.getX();
                int y = pixel.getY();
                int[] inputLocation = new int[] { x, y, c };
                int[] outputLocation = new int[] { x, y, fitAllChannels ? c : 0 };

                double[] decay = processor.getPixel(inputLocation);
                
                // fit this pixel?
                if (null != decay) {
                    // set up local, pixel fit parameters
                    ILocalFitParams localFitParams = new LocalFitParams();
                    localFitParams.setY(decay);
                    localFitParams.setSig(null);
                    localFitParams.setParams(fitInfo.getParameters());
                    double[] yFitted = new double[bins];
                    localFitParams.setYFitted(yFitted);
                    
                    pixel.setInputLocation(inputLocation);
                    pixel.setOutputLocation(outputLocation);
                    pixelList.add(pixel);
                    localFitParamsList.add(localFitParams);
                    
                    if (++pixelsToProcessCount >= PIXEL_COUNT) {
                        pixelsToProcessCount = 0;
                        
                        ChunkyPixel[] pixelArray = pixelList.toArray(new ChunkyPixel[0]);
                        pixelList.clear();
                        ILocalFitParams[] localFitParamsArray = localFitParamsList.toArray(new ILocalFitParams[0]);
                        localFitParamsList.clear();
                        
                        processPixels(fittingEngine, pixelArray, globalFitParams, localFitParamsArray, imageColorizer, newImage);
                    }
                }
            }
        }
        
        if (fitInfo.getCancel()) {
            IJ.showProgress(0, 0);
            cancelImageFit();
            if (null != imageColorizer) {
                imageColorizer.cancelFit();
            }
            return null;
        }
        else {
            if (pixelsToProcessCount > 0) {
                ChunkyPixel[] pixelArray = pixelList.toArray(new ChunkyPixel[0]);
                ILocalFitParams[] localFitParamsArray = localFitParamsList.toArray(new ILocalFitParams[0]);
                processPixels(fittingEngine, pixelArray, globalFitParams, localFitParamsArray, imageColorizer, newImage);
            }
            if (null != imageColorizer) {
                imageColorizer.endFit();
            }
        }

        return newImage.getImage();
    }

    /**
     * Helper function that processes an array of pixels.  When creating
     * colorized images from fit parameters, the histogram and images are
     * updated at the end of this function.
     *
     * @param fittingEngine
     * @param pixels
     * @param globalFitParams
     * @param localFitParams
     * @param imageColorizer
     * @param fittedImage
     */
    private void processPixels(
            IFittingEngine fittingEngine,
            ChunkyPixel[] pixels,
            IGlobalFitParams globalFitParams,
            ILocalFitParams[] localFitParams,
            FittedImageFitter imageColorizer,
            IFittedImage fittedImage) {

        //TODO use Lists or just arrays? This just converts from array to List.
        List<ILocalFitParams> localFitParamsList = new ArrayList<ILocalFitParams>();
        for (ILocalFitParams lFP : localFitParams) {
            localFitParamsList.add(lFP);
        }
        
        List<IFitResults> resultsList =
                fittingEngine.fit(globalFitParams, localFitParamsList);

        for (int i = 0; i < resultsList.size(); ++i) {
            IFitResults result = resultsList.get(i);
            double[] results = result.getParams();
            ChunkyPixel p = pixels[i];
            int[] location = p.getOutputLocation();
         
            // if producing colorized images, feed this pixel to colorizer
            if (null != imageColorizer) {
                imageColorizer.updatePixel(location, results);
            }
            fittedImage.setPixel(location, results);
        }

        if (null != imageColorizer) {
            imageColorizer.recalcHistogram();
        }
    }

    /*
     * Sums all pixels and fits the result.
     */
    private Image<DoubleType> fitSummed(IUserInterfacePanel uiPanel) {
        Image<DoubleType> fittedPixels = null;
        double params[] = uiPanel.getParameters(); //TODO go cumulative
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];
        int photons = 0;

        // loop over all channels or just the current one
        for (int channel : getChannelIndices(_fitAllChannels, _channel, _channels)) {
            curveFitData = new CurveFitData();
            curveFitData.setParams(params.clone()); //TODO NO NO NO s/b either from UI or fitted point or fitted whole image
            yCount = new double[_bins];
            for (int b = 0; b < _bins; ++b) {
                yCount[b] = 0.0;
            }

            // count photons and pixels
            int pixels = 0;

            // sum this channel
            for (int y = 0; y < _height; ++y) {
                for (int x = 0; x < _width; ++x) {
                    for (int b = 0; b < _bins; ++b) {
                        double count = getData(_cursor, _channel, x, y, b);
                        yCount[b] += count;
                        photons += (int) count;
                    }
                    ++pixels;
                }
            }
            curveFitData.setYCount(yCount);
            curveFitData.setTransStartIndex(0);
            curveFitData.setDataStartIndex(_startBin);
            curveFitData.setTransEndIndex(_stopBin);
            yFitted = new double[_bins];
            curveFitData.setYFitted(yFitted);      

            // use zero for current channel if it's the only one
            int nominalChannel = _fitAllChannels ? channel : 0;
            curveFitData.setChannel(nominalChannel);
            curveFitData.setX(0);
            curveFitData.setY(0);
            curveFitData.setPixels(pixels);
            curveFitDataList.add(curveFitData);
        }

        // do the fit
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        getCurveFitter(uiPanel).fitData(dataArray);

        // show decay and update UI parameters
        int visibleChannel = _fitAllChannels ? _channel : 0;
        String title = "Summed";
        if (1 < _channels) {
            title += " Channel " + (_channel + 1);
        }
        showDecayGraph(title, uiPanel, _fittingCursor,
                dataArray[visibleChannel], photons);
        uiPanel.setParameters(dataArray[visibleChannel].getParams());

        // get the results
        int channels = _fitAllChannels ? _channels : 1;
        //fittedPixels = makeImage(channels, 1, 1, uiPanel.getParameterCount()); //TODO ImgLib bug if you use 1, 1, 1, 4; see "imglibBug()" below.
        fittedPixels = makeImage(channels + 1, 2, 2, uiPanel.getParameterCount()); //TODO this is a workaround; unused pixels will remain NaNs
        LocalizableByDimCursor<DoubleType> resultsCursor = fittedPixels.createLocalizableByDimCursor();
        setFittedParamsFromData(resultsCursor, dataArray);
        return fittedPixels;
    }

    /*
     * Sums and fits each ROI.
     */
    private Image<DoubleType> fitROIs(IUserInterfacePanel uiPanel) {
        Image<DoubleType> fittedPixels = null;
        double params[] = uiPanel.getParameters();
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];
        int[] photons = new int[getRois().length];
        for (int i = 0; i < photons.length; ++i) {
            photons[i] = 0;
        }

        // loop over all channels or just the current one
        for (int channel : getChannelIndices(_fitAllChannels, _channel, _channels)) {
            int roiNumber = 1;
            for (Roi roi: getRois()) {
                curveFitData = new CurveFitData();
                curveFitData.setParams(params.clone());
                yCount = new double[_bins];
                for (int b = 0; b < _bins; ++b) {
                    yCount[b] = 0.0;
                }
                Rectangle bounds = roi.getBounds();
                int pixels = 0;
                for (int x = 0; x < bounds.width; ++x) {
                    for (int y = 0; y < bounds.height; ++y) {
                        if (roi.contains(bounds.x + x, bounds.y + y)) {
                            ++pixels;
                            for (int b = 0; b < _bins; ++b) {
                                double count = getData(_cursor, channel, x, y, b);
                                yCount[b] += count;
                                photons[roiNumber - 1] += count;
                            }
                        }
                    }
                }
                curveFitData.setYCount(yCount);
                curveFitData.setTransStartIndex(0);
                curveFitData.setDataStartIndex(_startBin);
                curveFitData.setTransEndIndex(_stopBin);
                
                yFitted = new double[_bins];
                curveFitData.setYFitted(yFitted);

                // use zero for current channel if it's the only one
                int nominalChannel = _fitAllChannels ? channel : 0;
                curveFitData.setChannel(nominalChannel);
                curveFitData.setX(roiNumber - 1);
                curveFitData.setY(0);
                curveFitData.setPixels(pixels);
                curveFitDataList.add(curveFitData);

                ++roiNumber;
            }
        }

        // do the fit
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        getCurveFitter(uiPanel).fitData(dataArray);

        // show the decay graphs
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        int roiNumber = 1;
        for (Roi roi: getRois()) {
            int nominalChannel = _fitAllChannels ? _channel : 0;
            int dataIndex = nominalChannel * getRois().length + (roiNumber - 1);

            String title = "Roi " + roiNumber;
            if (1 < _channels) {
                title += " Channel " + (_channel + 1);
            }
            showDecayGraph(title, uiPanel, _fittingCursor,
                    dataArray[dataIndex], photons[roiNumber - 1]);
            double lifetime = dataArray[dataIndex].getParams()[3];
            if (lifetime < min) {
                min = lifetime;
            }
            if (lifetime > max) {
                max = lifetime;
            }
            ++roiNumber;
        }
        
        // show colorized lifetimes
        ImageProcessor imageProcessor = new ColorProcessor(_width, _height);
        ImagePlus imagePlus = new ImagePlus("ROIs Fitted Lifetimes", imageProcessor);
        roiNumber = 1;
        for (Roi roi: getRois()) {
            int nominalChannel = _fitAllChannels ? _channel : 0;
            int dataIndex = nominalChannel * getRois().length + (roiNumber - 1);
            double lifetime = dataArray[dataIndex].getParams()[3];

            imageProcessor.setColor(lifetimeColorMap(min, max, lifetime));

            Rectangle bounds = roi.getBounds();
            for (int x = 0; x < bounds.width; ++x) {
                for (int y = 0; y < bounds.height; ++y) {
                    if (roi.contains(bounds.x + x, bounds.y + y)) {
                        imageProcessor.drawPixel(bounds.x + x, bounds.y + y);
                    }
                }
            }
            ++roiNumber;
        }
        imagePlus.show();  

        // update UI parameters
        uiPanel.setParameters(dataArray[0].getParams()); //TODO, just picked first ROI here!

        // get the results
        int channels = _fitAllChannels ? _channels : 1;
        //fittedPixels = makeImage(channels, 1, 1, uiPanel.getParameterCount()); //TODO ImgLib bug if you use 1, 1, 1, 4; see "imglibBug()" below.
        fittedPixels = makeImage(channels + 1, getRois().length + 1, 2, uiPanel.getParameterCount()); //TODO this is a workaround; unused pixels will remain NaNs
        LocalizableByDimCursor<DoubleType> resultsCursor = fittedPixels.createLocalizableByDimCursor();
        setFittedParamsFromData(resultsCursor, dataArray);
        return fittedPixels;
    }

    // added kludge to make moving cursors in DecayGraph do a refit. //TODO this has to change FittingCursor will know whenever cursors change.
    private Image<DoubleType> fitPixel(
            IUserInterfacePanel uiPanel,
            FittingCursor fittingCursor) {
        int x = uiPanel.getX();
        int y = uiPanel.getY();
        _startBin = fittingCursor.getDataStartBin();
        _stopBin = fittingCursor.getTransientStopBin();
//        System.out.println("_startBin is " + _startBin + " _stopBin " + _stopBin);
//        System.out.println("FYI FWIW prompt delay is " + _fittingCursor.getPromptDelay());
//        System.out.println("prompt start is " + _fittingCursor.getPromptStartValue() + " stop " + _fittingCursor.getPromptStopValue());
//        System.out.println("_fittingCursor start value " + _fittingCursor.getTransientStartValue() + " bin " + _fittingCursor.getTransientStartBin() + " stop value " + _fittingCursor.getTransientStopValue() + " bin " + _fittingCursor.getTransientStopBin());
        return fitPixel(uiPanel, x, y);
    }

    /*
     * Fits a given pixel.
     * 
     * @param x
     * @param y
     */
    private Image<DoubleType> fitPixel(IUserInterfacePanel uiPanel, int x, int y) {
        Image<DoubleType> fittedPixels = null;
        
        // set up the source
        IDecayImage decayImage = new DecayImageWrapper(_image, _width, _height, _channels, _bins, _binIndex, _increment);
        IProcessor processor = decayImage;
        ISLIMBinner binner = _binning.getBinner(uiPanel.getBinning());
        if (null != binner) {
            binner.init(_width, _height);
            binner.chain(processor);
            processor = binner;
        }
        
        // set up the location
        int[] location = new int[] { _x, _y, _channel };
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        double params[] = uiPanel.getParameters(); //TODO wrong; params should possibly come from already fitted data
        double chiSquareTarget = uiPanel.getChiSquareTarget();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];
        int photons = 0;

        // loop over all channels or just the current one
        for (int channel : getChannelIndices(_fitAllChannels, _channel, _channels)) {
            curveFitData = new CurveFitData();
            curveFitData.setParams(params.clone()); //TODO NO NO NO s/b either from UI or fitted point or fitted whole image

            location[2] = channel;
            yCount = processor.getPixel(location);
            
            curveFitData.setYCount(yCount);
            int transStartIndex = _fittingCursor.getTransientStartBin();
            int dataStartIndex = _fittingCursor.getDataStartBin();
            int transStopIndex = _fittingCursor.getTransientStopBin();
            curveFitData.setTransStartIndex(transStartIndex);
            curveFitData.setDataStartIndex(dataStartIndex);
            curveFitData.setTransEndIndex(transStopIndex);

            //TODO ARG this photon counting needs to be channel specific and also part of summed and ROI fits.
            photons = 0;
            for (int c = dataStartIndex; c < transStopIndex; ++c) {
                if (c < yCount.length) {
                    photons += yCount[c];
                }
            }
          
            yFitted = new double[_bins];
            curveFitData.setYFitted(yFitted);
            curveFitData.setChiSquareTarget(chiSquareTarget);

            // use zero for current channel if it's the only one
            int nominalChannel = _fitAllChannels ? channel : 0;
            curveFitData.setChannel(nominalChannel);
            curveFitData.setX(0);
            curveFitData.setY(0);
            curveFitData.setPixels(1);
            curveFitDataList.add(curveFitData);
        }
        
        // do the fit
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        getCurveFitter(uiPanel).fitData(dataArray);
        
        // show decay graph for visible channel
        String title = "Fitted Pixel " + x + " " + y;
        if (1 < _channels) {
            title += " Channel " + (_channel + 1);
        }
        int visibleChannel = 0;
        if (_fitAllChannels) {
            visibleChannel = _channel;
        }
        showDecayGraph(title, uiPanel, _fittingCursor,
                dataArray[visibleChannel], photons); //TODO ARG this s/b the photon count for the appropriate channel; currently it will sum all channels.

        // update UI parameters
        uiPanel.setParameters(dataArray[visibleChannel].getParams());
        
        // get the results
        int channels = _fitAllChannels ? _channels : 1;
        //fittedPixels = makeImage(channels, 1, 1, uiPanel.getParameterCount()); //TODO ImgLib bug if you use 1, 1, 1, 4; see "imglibBug()" below.
        fittedPixels = makeImage(channels + 1, 2, 2, uiPanel.getParameterCount()); //TODO this is a workaround; unused pixels will remain NaNs
        LocalizableByDimCursor<DoubleType> resultsCursor = fittedPixels.createLocalizableByDimCursor();               
        setFittedParamsFromData(resultsCursor, dataArray);
        return fittedPixels;
    }

    /*
     * Demonstrates a bug with ImgLib:
     * //TODO fix it!
     */
    private void imglibBug() {
        int dim[] = { 1, 1, 1, 4 };
        Image<DoubleType> image = new ImageFactory<DoubleType>(new DoubleType(), new PlanarContainerFactory()).createImage(dim, "Test");

        // initialize image
        Cursor<DoubleType> cursor = image.createCursor();
        while (cursor.hasNext()) {
            System.out.println("fwd");
            cursor.fwd();
            cursor.getType().set(Double.NaN);
        }
    }

    /**
     * Gets an array of channel indices to iterate over.
     *
     * @param fitAllChannels
     * @param channel current channel
     * @param channels number of channels
     * @return
     */
    private int[] getChannelIndices(boolean fitAllChannels, int channel, int channels) {
        if (fitAllChannels) {
            int[] channelIndices = new int[channels];
            for (int c = 0; c < channels; ++c) {
                channelIndices[c] = c;
            }
            return channelIndices;
        }
        else {
            return new int[] { channel };
        }
    }

    /**
     * Calculates the total number of pixels to fit.  Used for
     * progress bar.
     *
     * @param channels
     * @param fitAll
     * @return
     */
    private int totalPixelCount(int x, int y, int channels, boolean fitAll) {
        int count = x * y;
        if (fitAll) {
            count *= channels;
        }
        return count;
    }

    /**
     * Calculates an array of channel indices to iterate over.
     *
     * @param channel
     * @param channels
     * @param visibleFit
     * @param fitAll
     * @return
     */
    private int[] channelIndexArray(int channel, int channels, boolean visibleFit, boolean fitAll) {
        int returnValue[] = { };
        if (fitAll) {
            returnValue = new int[visibleFit ? channels - 1 : channels];
            int i = 0;
            for (int c = 0; c < channels; ++c) {
                // skip visible; already processed
                if (c != channel || !visibleFit) {
                    returnValue[i++] = c;
                }
            }
        }
        else if (!visibleFit) {
            // single channel, not processed yet
            returnValue = new int[1];
            returnValue[0] = channel;
        }
        return returnValue;
    }

    private double getData(LocalizableByDimCursor<T> cursor, int channel, int x, int y, int bin) {
        int dim[];
        if (_hasChannels) {
            dim = new int[] { x, y, bin, channel }; //TODO ARG is this order guaranteed?
        }
        else {
            dim = new int[] { x, y, bin };
        }
        cursor.moveTo(dim);
        return cursor.getType().getRealFloat() / _minNonZeroPhotonCount;
    }

    /**
     * Helper routine to create imglib.Image to store fitted results.
     *
     * @param width
     * @param height
     * @param components
     * @return
     */
    private Image<DoubleType> makeImage(int channels, int width, int height, int parameters) {
        Image<DoubleType> image = null;

//        System.out.println("channels width height params " + channels + " " + width + " " + height + " " + parameters);
        
        // create image object
        int dim[] = { width, height, channels, parameters }; //TODO when we keep chi square in image  ++parameters };
        image = new ImageFactory<DoubleType>(new DoubleType(), new PlanarContainerFactory()).createImage(dim, "Fitted");

        // initialize image
        Cursor<DoubleType> cursor = image.createCursor();
        while (cursor.hasNext()) {
            cursor.fwd();
            cursor.getType().set(Double.NaN);
        }

        return image;
    }

    private double[] getFittedParams(LocalizableByDimCursor<DoubleType> cursor, int channel, int x, int y, int count) {
        double params[] = new double[count];
        int position[] = new int[4];
        position[0] = x;
        position[1] = y;
        position[2] = channel;
        for (int i = 0; i < count; ++i) {
            position[3] = i;
            cursor.setPosition(position);
            params[i] = cursor.getType().getRealDouble();
        }
        return params;
    }

    private void setFittedParamsFromData(LocalizableByDimCursor<DoubleType> cursor, ICurveFitData dataArray[]) {
        int x, y;
        double[] params;
        for (ICurveFitData data : dataArray) {
            setFittedParams(cursor, data.getChannel(), data.getX(), data.getY(), data.getParams());
        }
    }
    
    private void setFittedParams(LocalizableByDimCursor<DoubleType> cursor, int channel, int x, int y, double[] params) {
        int position[] = new int[4];
        position[0] = x;
        position[1] = y;
        position[2] = channel;
        for (int i = 0; i < params.length; ++i) {
            position[3] = i;
            cursor.setPosition(position);
            cursor.getType().set(params[i]);
        }
    }

    private void cancelImageFit() {
        _fittedImage = null;
        _fittedParameterCount = 0;
    }

    /**
     * Checks whether a given pixel is included in ROIs.  If no ROIs are
     * selected then all pixels are included.
     *
     * @param x
     * @param y
     * @return whether or not included in ROIs
     */
    boolean isInROIs(int x, int y) {
        Roi[] rois = getRois();
        if (0 < rois.length) {
            for (Roi roi: rois) {
                if (roi.contains(x, y)) {
                    return true;
                }
            }
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Gets a list of ROIs (may be empty).
     *
     * @return array of ROIs.
     */
    private Roi[] getRois() {
        Roi[] rois = {};
        RoiManager manager = RoiManager.getInstance();
        if (null != manager) {
            rois = manager.getRoisAsArray();
        }
        return rois;
    }

    /**
     * Colorizes a given lifetime value.
     *
     * Note this is much cruder than the DataColorizer that is
     * used in fitEachPixel.
     *
     * @param min
     * @param max
     * @param lifetime
     * @return
     */
    //TODO make consistent with fitEachPixel's DataColorizer
    //TODO this needs to use LUTs
     private Color lifetimeColorMap(double min, double max, double lifetime) {
        // adjust for minimum
        max -= min;
        lifetime -= min;

        Color returnColor = Color.BLACK;
        if (lifetime > 0.0) {
            if (lifetime < max/2.0) {
                returnColor = interpolateColor(Color.BLUE, Color.GREEN, 2.0 * lifetime / max);
            }
            else if (lifetime < max) {
                returnColor = interpolateColor(Color.GREEN, Color.RED, 2.0 * (lifetime - max / 2.0) / max);
            }
            else returnColor = Color.RED;
        }
        else if (lifetime == 0.0) {
            returnColor = Color.BLUE;
        }
        return returnColor;
    }

     /**
      * Interpolates between two colors based on a blend factor.
      *
      * @param start color
      * @param end color
      * @param blend factor
      * @return interpolated color
      */
    private Color interpolateColor(Color start, Color end, double blend) {
        int startRed   = start.getRed();
        int startGreen = start.getGreen();
        int startBlue  = start.getBlue();
        int endRed   = end.getRed();
        int endGreen = end.getGreen();
        int endBlue  = end.getBlue();
        int red   = interpolateColorComponent(startRed, endRed, blend);
        int green = interpolateColorComponent(startGreen, endGreen, blend);
        int blue  = interpolateColorComponent(startBlue, endBlue, blend);
        return new Color(red, green, blue);
    }

    /**
     * Interpolates a single RGB component between two values based on
     * a blend factor.
     *
     * @param start component value
     * @param end component value
     * @param blend factor
     * @return interpolated component value
     */
    private int interpolateColorComponent(int start, int end, double blend) {
        return (int)(blend * (end - start) + start);
    }

    /*
     * Gets the appropriate curve fitter for the current fit.
     *
     * @param uiPanel has curve fitter selection
     */
    private ICurveFitter getCurveFitter(IUserInterfacePanel uiPanel) {
        ICurveFitter curveFitter = null;
        switch (uiPanel.getAlgorithm()) {
            case JAOLHO:
                curveFitter = new JaolhoCurveFitter();
                break;
            case SLIMCURVE_RLD:
                curveFitter = new SLIMCurveFitter();
                curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD);
                break;
            case SLIMCURVE_LMA:
                curveFitter = new SLIMCurveFitter();
                curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_LMA);
                break;
            case SLIMCURVE_RLD_LMA:
                curveFitter = new SLIMCurveFitter();
                curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA);
                break;
        }
        ICurveFitter.FitFunction fitFunction = null;
        switch (uiPanel.getFunction()) {
            case SINGLE_EXPONENTIAL:
                fitFunction = FitFunction.SINGLE_EXPONENTIAL;
                break;
            case DOUBLE_EXPONENTIAL:
                fitFunction = FitFunction.DOUBLE_EXPONENTIAL;
                break;
            case TRIPLE_EXPONENTIAL:
                fitFunction = FitFunction.TRIPLE_EXPONENTIAL;
                break;
            case STRETCHED_EXPONENTIAL:
                fitFunction = FitFunction.STRETCHED_EXPONENTIAL;
                break;
        }
        curveFitter.setEstimator(new FitterEstimator());
        curveFitter.setFitFunction(fitFunction);
        curveFitter.setNoiseModel(uiPanel.getNoiseModel());
        curveFitter.setXInc(_timeRange);
        curveFitter.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));
        if (null != _excitationPanel) {
            double[] excitation = null;
            if (null != _excitationPanel) {
                int startIndex = _fittingCursor.getPromptStartBin();
                int stopIndex  = _fittingCursor.getPromptStopBin();
                double base  = _fittingCursor.getPromptBaselineValue();
                excitation = _excitationPanel.getValues(startIndex, stopIndex, base);
            }            
            curveFitter.setInstrumentResponse(excitation);
        }
        return curveFitter;
    }

    /*
     * Handles reordering the array that describes which fit parameters are
     * free (vs. fixed).
     */
    private boolean[] translateFree(FitFunction fitFunction, boolean free[]) {
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
     * Helper function for the fit.  Shows the decay curve.
     *
     * @param title
     * @param uiPanel gets updates on dragged/start stop
     * @param data fitted data
     */
    private void showDecayGraph(final String title,
            final IUserInterfacePanel uiPanel,
            final FittingCursor fittingCursor,
            final ICurveFitData data,
            int photons)
    {
        IDecayGraph decayGraph = DecayGraph.getInstance();
        JFrame frame = decayGraph.init(uiPanel.getFrame(), _bins, _timeRange);
        decayGraph.setTitle(title);
        decayGraph.setFittingCursor(fittingCursor);
        double transStart = fittingCursor.getTransientStartValue();
        double dataStart  = fittingCursor.getDataStartValue();
        double transStop  = fittingCursor.getTransientStopValue();
        decayGraph.setStartStop(transStart, dataStart, transStop);
        decayGraph.setData(data);
        decayGraph.setPhotons(photons);
    }
    
    /**
     * Inner class that listens for changes in the cursor that should trigger
     * a refit.
     */   
    private class FittingCursorListener implements IFittingCursorListener {
        private Integer _transStart    = null;
        private Integer _dataStart     = null;
        private Integer _transStop     = null;
        private Integer _promptStart   = null;
        private Integer _promptStop    = null;
        private Double _promptBaseline = null;
        
        public void cursorChanged(FittingCursor cursor) {
            // get current cursor values
            int transStart        = cursor.getTransientStartBin();
            int dataStart         = cursor.getDataStartBin();
            int transStop         = cursor.getTransientStopBin();
            int promptStart       = cursor.getPromptStartBin();
            int promptStop        = cursor.getPromptStopBin();
            double promptBaseline = cursor.getPromptBaselineValue();
            
            // look for changes, current vs. saved cursor values
            if (null == _transStart
                    || null == _dataStart
                    || null == _transStop
                    || null == _promptStart
                    || null == _promptStop
                    || null == _promptBaseline
                    || transStart     != _transStart
                    || dataStart      != _dataStart
                    || transStop      != _transStop
                    || promptStart    != _promptStart
                    || promptStop     != _promptStop
                    || promptBaseline != _promptBaseline) {
                
                // trigger refit
                System.out.println("*** CURSOR CHANGE REFIT ***");
  
                // update saved cursor values for next time
                _transStart     = transStart;
                _dataStart      = dataStart;
                _transStop      = transStop;
                _promptStart    = promptStart;
                _promptStop     = promptStop;
                _promptBaseline = promptBaseline;

                fitPixel(_uiPanel, _fittingCursor);
            }
        }
    }

}
