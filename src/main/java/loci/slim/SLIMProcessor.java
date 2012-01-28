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
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import loci.curvefitter.CurveFitData;
import loci.curvefitter.GrayCurveFitter;
import loci.curvefitter.GrayNRCurveFitter;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;
import loci.curvefitter.JaolhoCurveFitter;
import loci.curvefitter.MarkwardtCurveFitter;
import loci.curvefitter.SLIMCurveFitter;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.slim.analysis.SLIMAnalysis;
import loci.slim.binning.SLIMBinning;
import loci.slim.colorizer.DataColorizer;
 import loci.slim.colorizer.DataColorizer2;

import loci.slim.ui.ExcitationPanel;
import loci.slim.ui.IStartStopListener;
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
import imagej.slim.fitting.FitInfo;
import imagej.slim.fitting.IDecayImage;
import imagej.slim.fitting.IFittedImage;
import imagej.slim.fitting.images.ColorizedImageParser;
import imagej.slim.fitting.params.IGlobalFitParams;
import imagej.slim.fitting.params.LocalFitParams;
import imagej.slim.fitting.params.GlobalFitParams;
import imagej.slim.fitting.engine.IFittingEngine;
import imagej.slim.fitting.params.ILocalFitParams;
import imagej.slim.fitting.params.IFitResults;
import imagej.slim.fitting.config.Configuration;
import imagej.slim.fitting.FitInfo;
import imagej.slim.fitting.images.ColorizedImageFitter;
import imagej.slim.fitting.images.ColorizedImageFitter.ColorizedImageType;
import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;
import loci.curvefitter.ICurveFitter.NoiseModel;


//TODO tidy up SLIMProcessor
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
    
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String LIFETIME = "Lifetime";
    private static final String CHANNELS = "Channels";
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

    private static final double[] DEFAULT_SINGLE_EXP_PARAMS  = { 0.0, 0.5, 100.0, 0.5 };                      // 0 C A T
    private static final double[] DEFAULT_DOUBLE_EXP_PARAMS  = { 0.0, 0.5, 50.0, 0.5, 50, 0.25 };             // 0 C A1 T1 A2 T2
    private static final double[] DEFAULT_TRIPLE_EXP_PARAMS  = { 0.0, 0.5, 40.0, 0.5, 30.0, 0.25, 30, 0.10 }; // 0 C A1 T1 A2 T2 A3 T3
    private static final double[] DEFAULT_STRETCH_EXP_PARAMS = { 0.0, 0.5, 100.0, 0.5, 0.5 };                 // 0 C A T H

    private Object m_synchFit = new Object();
    private volatile boolean m_quit;
    private volatile boolean m_cancel;
    private volatile boolean m_fitInProgress;
    private volatile boolean m_fitted;

    private static final String FILE_KEY = "file";
    private static final String PATH_KEY = "path";
    private String m_file;
    private String m_path;

    IFormatReader m_reader;

    private Image<T> m_image;
    private LocalizableByDimCursor<T> m_cursor;

    private Image<DoubleType> m_fittedImage = null;
    private int m_fittedParameterCount = 0;
    boolean m_visibleFit = true;

    // data parameters
    private boolean m_hasChannels;
    private int m_channels;
    private int m_channelIndex;
    private int m_width;
    private int m_height;
    private int[] m_cLengths;
    private int m_bins;
    private int m_binIndex;

    private boolean m_little;
    private int m_pixelType;
    private int m_bpp;
    private boolean m_floating;
    private float m_timeRange;
    private int m_minWave, m_waveStep; //, m_maxWave;

    private FitRegion m_region;
    private FitAlgorithm m_algorithm;
    private FitFunction m_function;

    private SLIMAnalysis m_analysis;
    private SLIMBinning m_binning;

    private ExcitationPanel m_excitationPanel = null;
    private IGrayScaleImage m_grayScaleImage;
    // user sets these from the grayScalePanel
    private int m_channel;
    private boolean m_fitAllChannels;

    // current channel, x, y
    private int m_xchannel; // channel to fit; -1 means fit all channels
    private int m_x;
    private int m_y;
    private int m_xvisibleChannel; // channel being displayed; -1 means none

    private double[] m_param = new double[7];
    private boolean[] m_free = { true, true, true, true, true, true, true };

    private int m_startBin;
    private int m_stopBin;
    private int m_startX;
    private int m_threshold;
    private float m_chiSqTarget;
    
    private FitInfo m_fitInfo;

    private int m_debug = 0;

    public SLIMProcessor() {
        m_analysis = new SLIMAnalysis();
        m_binning = new SLIMBinning();
        m_quit = false;
        m_cancel = false;
        m_fitInProgress = false;
        m_fitted = false;
    }

    public void processImage(Image<T> image) {
        boolean success = false;

        m_image = image;
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
            m_image = loadImage(m_path, m_file);
            if (null == m_image) {
                System.out.println("image is null");
            }
            if (getImageInfo(m_image)) {
                savePathAndFileInPreferences(m_path, m_file);
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
     * sets the global m_fitInProgress.
     *
     * @param uiPanel
     */
    private void doFits() {
        // show the UI; do fits
        final IUserInterfacePanel uiPanel = new UserInterfacePanel(USE_TAU, m_analysis.getChoices(), m_binning.getChoices());
        uiPanel.setX(0);
        uiPanel.setY(0);
        uiPanel.setStart(m_bins / 2, false); //TODO hokey
        uiPanel.setStop(m_bins - 1, false);
        uiPanel.setThreshold(100);
        uiPanel.setChiSquareTarget(1.5);
        uiPanel.setFunctionParameters(0, DEFAULT_SINGLE_EXP_PARAMS);
        uiPanel.setFunctionParameters(1, DEFAULT_DOUBLE_EXP_PARAMS);
        uiPanel.setFunctionParameters(2, DEFAULT_TRIPLE_EXP_PARAMS);
        uiPanel.setFunctionParameters(3, DEFAULT_STRETCH_EXP_PARAMS);
        uiPanel.setListener(
            new IUserInterfacePanelListener() {
                /**
                 * Triggers a fit.
                 */
                public void doFit() {
                    m_cancel = false;
                    m_fitInProgress = true;
                }

                /**
                 * Cancels ongoing fit.
                 */
                public void cancelFit() {
                    m_cancel = true;
                    if (null != m_fitInfo) {
                        m_fitInfo.setCancel(true);
                    }
                }
                
                /**
                 * Triggers refit of current pixel.
                 */
                public void doPixelFit() {
                    // ordinarily these fits take place in the thread at the end 
                    // of this method.
                    // this is a hack until I refactor out a FittingEngine.
                    fitPixel(uiPanel);
                }
                
                /**
                 * Quits running plugin.
                 */
                public void quit() {
                    m_quit = true;
                }

                /**
                 * Loads an excitation curve from file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                public boolean loadExcitation(String fileName) {
                    Excitation excitation = ExcitationFileHandler.getInstance().loadExcitation(fileName, m_timeRange);
                    return updateExcitation(uiPanel, excitation);
                }

                /**
                 * Creates an excitation curve from currrent X, Y and saves to file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                public boolean createExcitation(String fileName) {
                    int channel = 0;
                    if (null != m_grayScaleImage) {
                        channel = m_grayScaleImage.getChannel();
                    }
                    int x = uiPanel.getX();
                    int y = uiPanel.getY();
                    float[] values = new float[m_bins];
                    for (int b = 0; b < m_bins; ++b) {
                        values[b] = (float) getData(m_cursor, channel, x, y, b);
                    }
                    Excitation excitation = ExcitationFileHandler.getInstance().createExcitation(fileName, values, m_timeRange);
                    return updateExcitation(uiPanel, excitation);
                }

                /**
                 * Cancels the current excitation curve, if any.
                 *
                 */
                public void cancelExcitation() {
                    if (null != m_excitationPanel) {
                        m_excitationPanel.quit();
                        m_excitationPanel = null;
                        //TODO redo stop/start cursors on decay curve?
                    }
                }
            }
        );
        uiPanel.getFrame().setLocationRelativeTo(null);
        uiPanel.getFrame().setVisible(true);

        // create a grayscale image from the data
        m_grayScaleImage = new GrayScaleImage(m_image);
        m_grayScaleImage.setListener(
            new ISelectListener() {
                public void selected(int channel, int x, int y) {
                    // just ignore clicks during a fit
                    if (!m_fitInProgress) {
                        synchronized (m_synchFit) {
                            float zoomFactor = ((GrayScaleImage)m_grayScaleImage).getZoomFactor();
                            x *= zoomFactor;
                            y *= zoomFactor;
                            uiPanel.setX(x);
                            uiPanel.setY(y);
                            getFitSettings(m_grayScaleImage, uiPanel);
                            // fit on the pixel clicked
                            fitPixel(uiPanel);
                        }
                    }
                }
            }
        );

        // set start and stop for now; will be updated if we load an excitation curvce
        updateDecayCursors(uiPanel);

        // processing loop; waits for UI panel input
        while (!m_quit) {
            while (!m_fitInProgress) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {

                }
                if (m_quit) {
                    hideUIPanel(uiPanel);
                    return;
                }
            }

            //uiPanel.enable(false); //TODO this might be better to be same as grayScalePanel
            m_grayScaleImage.enable(false);

            // get settings of requested fit
            getFitSettings(m_grayScaleImage, uiPanel);

            // do the fit
            fitData(uiPanel);

            m_fitInProgress = false;
            //uiPanel.enable(true);
            m_grayScaleImage.enable(true);
            uiPanel.reset();
        }
        hideUIPanel(uiPanel);
    }

    private void hideUIPanel(IUserInterfacePanel uiPanel) {
        m_grayScaleImage.setListener(null);
        //TODO uiPanel is still hooked up as start stop listeners to decay curves!
        uiPanel.getFrame().setVisible(false);
    }

    private void updateDecayCursors(IUserInterfacePanel uiPanel) {
        // sum selected channel
        double[] decay = new double[m_bins];
        for (int i = 0; i < decay.length; ++i) {
            decay[i] = 0.0;
        }
        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                for (int b = 0; b < m_bins; ++b) {
                    decay[b] += getData(m_cursor, m_channel, x, y, b);
                }
            }
        }

        int[] results = CursorHelper.estimateDecayCursors(m_timeRange, decay);
        uiPanel.setStart(results[0], false);
        uiPanel.setStop(results[1], false);
    }

    private boolean updateExcitation(IUserInterfacePanel uiPanel, Excitation excitation) {
        boolean success = false;
        if (null != excitation) {
            if (null != m_excitationPanel) {
                m_excitationPanel.quit();
            }

            // sum selected channel
            int channel = 0;
            if (null != m_grayScaleImage) {
                channel = m_grayScaleImage.getChannel();
            }
            double[] decay = new double[m_bins];
            for (int i = 0; i < decay.length; ++i) {
                decay[i] = 0.0;
            }
            for (int y = 0; y < m_height; ++y) {
                for (int x = 0; x < m_width; ++x) {
                    for (int b = 0; b < m_bins; ++b) {
                        decay[b] += getData(m_cursor, channel, x, y, b);
                    }
                }
            }

            float[] results = CursorHelper.estimateCursors(m_timeRange, excitation.getValues(), decay);
            excitation.setStart((int) results[0]);
            excitation.setStop((int) results[1]);
            excitation.setBase(results[2]);

            uiPanel.setStart((int) results[3], false);
            uiPanel.setStop((int) results[4], false);

            m_excitationPanel = new ExcitationPanel(excitation);

            success = true;
        }
        return success;
    }

    private void getFitSettings(IGrayScaleImage grayScalePanel, IUserInterfacePanel uiPanel) {
        m_channel        = grayScalePanel.getChannel();

        m_region         = uiPanel.getRegion();
        m_algorithm      = uiPanel.getAlgorithm();
        m_function       = uiPanel.getFunction();
        m_fitAllChannels = uiPanel.getFitAllChannels();

        m_x              = uiPanel.getX();
        m_y              = uiPanel.getY();
        m_startBin       = uiPanel.getStart();
        m_stopBin        = uiPanel.getStop();
        m_threshold      = uiPanel.getThreshold();

        m_param          = uiPanel.getParameters();
        m_free           = uiPanel.getFree();
    }

    /**
     * Prompts for a .sdt file.
     *
     * @param defaultFile
     * @return
     */
    private boolean showFileDialog(String[] defaultPathAndFile) {
        OpenDialog dialog = new OpenDialog("Load Data", m_path, m_file);
        //if (dialog.wasCanceled()) return false;
        m_path = dialog.getDirectory();
        m_file = dialog.getFileName();
        System.out.println("directory is " + dialog.getDirectory());
        System.out.println("file is " + dialog.getFileName());
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
        m_width = ImageUtils.getWidth(image);
        m_height = ImageUtils.getHeight(image);
        m_channels = ImageUtils.getNChannels(image);
        //TODO this is broken; returns 1 when there are 16 channels; corrected below
        System.out.println("ImageUtils.getNChannels returns " + m_channels);
        m_hasChannels = false;
        if (dimensions.length > 3) {
            m_hasChannels = true;
            m_channelIndex = 3;
            m_channels = dimensions[m_channelIndex];
        }
        System.out.println("corrected to " + m_channels);
        m_bins = ImageUtils.getDimSize(image, FormatTools.LIFETIME);
        m_binIndex = 2;
        System.out.println("width " + m_width + " height " + m_height + " timeBins " + m_bins + " channels " + m_channels);
        m_cursor = image.createLocalizableByDimCursor();
        /*
        int index = 0;
        xIndex = index++;
        yIndex = index++;
        lifetimeIndex = index++;
        if (m_channels > 1) {
            channelIndex = index;
        }
        else {
            channelIndex = null;
        }


        m_data = new int[m_channels][m_height][m_width][m_timeBins];
        final LocalizableByDimCursor<T> cursor = image.createLocalizableByDimCursor();
        int x, y, bin, channel;
        for (channel = 0; channel < m_channels; ++channel) {
            if (null != channelIndex) {
                dimensions[channelIndex] = channel;
            }
            for (y = 0; y < m_height; ++y) {
                dimensions[yIndex] = y;
                for (x = 0; x < m_width; ++x) {
                    dimensions[xIndex] = x;
                    for (bin = 0; bin < m_timeBins; ++bin) {
                        dimensions[lifetimeIndex] = bin;
                        cursor.moveTo(dimensions);
                        m_data[channel][y][x][bin] = (int) cursor.getType().getRealFloat();
                        //TODO don't do this, screws up low photon count images...  m_data[channel][y][x][bin] /= 10.0f; //TODO in accordance with TRI2; HOLY COW!!!  ALSO int vs float??? why?
                    }
                }
            }
        }
        cursor.close();*/
        // print out some useful information about the image
        //System.out.println(image);
        //final Cursor<T> cursor = image.createCursor();
        //cursor.fwd();
        //System.out.println("\tType = " + cursor.getType().getClass().getName());
        //cursor.close();

        //TODO from a former version:
     //TODO won't compile with my version of the jar: Number timeBase = (Number) m_reader.getGlobalMetadata().get("time base");
     //TODO fix:
         //   Number timeBase = null;
         //   m_timeRange = timeBase == null ? Float.NaN : timeBase.floatValue();
         ////   if (m_timeRange != m_timeRange) m_timeRange = 10.0f;
         //   m_minWave = 400;
         //   m_waveStep = 10;
            //m_binRadius = 3;


        // patch things up
        m_timeRange = 10.0f / 64.0f; //TODO ARG this patches things up in accord with TRI2 for brian/gpl1.sdt; very odd value here NOTE this was with photon counts all divided by 10.0f above! might have cancelled out.
                   //TODO the patch above worked when I was also dividing the photon count by 10.0f!!  should be 1/64?
        m_minWave = 400;
        m_waveStep = 10;

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
        synchronized (m_synchFit) {
            
            switch (m_region) {
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
                    fittedImage = fitPixel(uiPanel, m_x, m_y);
                    break;
                case EACH:
                    // fit every pixel
                    fittedImage = fitImage(uiPanel); ///TODO WAS OLD VERSION: fitAllPixels(uiPanel);
                    break;
            }
        }
        System.out.println("fitted iage is " + fittedImage);
        if (null != fittedImage) {
            System.out.println("not null");
            for (String analysis : uiPanel.getAnalysisList()) {
                System.out.println("analsysis " + analysis);
                m_analysis.doAnalysis(analysis, fittedImage, uiPanel.getRegion(), uiPanel.getFunction()); //TODO get from uiPanel or get from global?  re-evaluate approach here
            }
        }
    }
    
    private FitInfo getFitInfo(IGrayScaleImage grayScalePanel, IUserInterfacePanel uiPanel) {
        FitInfo fitInfo = new FitInfo();
        fitInfo.setChannel(grayScalePanel.getChannel());
        fitInfo.setRegion(uiPanel.getRegion());
        fitInfo.setAlgorithm(uiPanel.getAlgorithm());
        fitInfo.setFunction(uiPanel.getFunction());
        fitInfo.setNoiseModel(uiPanel.getNoiseModel());
        fitInfo.setFittedImages(uiPanel.getFittedImages());
        fitInfo.setAnalysisList(uiPanel.getAnalysisList());
        fitInfo.setFitAllChannels(uiPanel.getFitAllChannels());
        fitInfo.setStartDecay(uiPanel.getStart());
        fitInfo.setStopDecay(uiPanel.getStop());
        fitInfo.setThreshold(uiPanel.getThreshold());
        fitInfo.setChiSquareTarget(uiPanel.getChiSquareTarget());
        fitInfo.setBinning(uiPanel.getBinning());   
        fitInfo.setX(uiPanel.getX());
        fitInfo.setY(uiPanel.getY());
        fitInfo.setParameterCount(uiPanel.getParameterCount());
        fitInfo.setParameters(uiPanel.getParameters());
        fitInfo.setFree(uiPanel.getFree());
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
        FitInfo fitInfo = getFitInfo(m_grayScaleImage, uiPanel);
        fitInfo.setXInc(m_timeRange);
        if (null != m_excitationPanel) {
            fitInfo.setPrompt(m_excitationPanel.getValues(1));
            fitInfo.setStartPrompt(0);
            fitInfo.setStopPrompt(100);
        }
        m_fitInfo = fitInfo;
        
        // set up images
        IDecayImage decayImage = new DecayImageWrapper(m_image, m_width, m_height, m_channels, m_channelIndex, m_bins, m_binIndex);
        IFittedImage previousImage = null;
        int width = decayImage.getWidth();
        int height = decayImage.getHeight();
        int channels = 1;
        if (fitInfo.getFitAllChannels()) {
            channels = decayImage.getChannels();
        }
        int parameters = fitInfo.getParameterCount();
        IFittedImage newImage = new OutputImageWrapper(width, height, channels, parameters);
        
        // create a fitting engine to use
        IFittingEngine fittingEngine = Configuration.getInstance().getFittingEngine();
        ICurveFitter curveFitter = getCurveFitter(uiPanel); //TODO ARG shouldn't all UI panel info go into FitInfo???
        fittingEngine.setCurveFitter(curveFitter);
        
        return fitImage(fittingEngine, fitInfo, decayImage, previousImage, newImage);

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
            IFittedImage previousImage,
            IFittedImage newImage) {
 
        // get commonly-used items in local variables
        int width = /*new*/decayImage.getWidth();
        int height = /*new*/decayImage.getHeight();
        int channels = /*new*/decayImage.getChannels();
        int[] dimension = new int[] { width, height, channels };
        int channel = fitInfo.getChannel();
        boolean fitAllChannels = fitInfo.getFitAllChannels();

        // needed to display progress bar
        int pixelCount = 0;
        int totalPixelCount = totalPixelCount(width, height, channels, fitAllChannels);
        int pixelsToProcessCount = 0;
 
        // handle optionally producing colorized images during the fit
        ColorizedImageFitter imageColorizer = null;
        String outputs = fitInfo.getFittedImages();
        if (null != outputs) {
            int components = fitInfo.getComponents();
            boolean stretched = fitInfo.getStretched();
            ColorizedImageParser parser = new ColorizedImageParser(outputs, components, stretched);
            ColorizedImageType[] outputImages = parser.getColorizedImages();
            imageColorizer = new ColorizedImageFitter();
            imageColorizer.setUpFit(outputImages, dimension, components);
            imageColorizer.beginFit();
        }
  
        // set up global, image-wide fit parameters
        //TODO revisit all of these
        IGlobalFitParams globalFitParams = new GlobalFitParams();
        globalFitParams.setFitAlgorithm(fitInfo.getAlgorithm());
        globalFitParams.setFitFunction(fitInfo.getFunction());
        globalFitParams.setNoiseModel(fitInfo.getNoiseModel());
        globalFitParams.setStartDecay(fitInfo.getStartDecay());
        globalFitParams.setStopDecay(fitInfo.getStopDecay());
        globalFitParams.setXInc(fitInfo.getXInc());
        globalFitParams.setPrompt(fitInfo.getPrompt());
        globalFitParams.setStartPrompt(fitInfo.getStartPrompt());
        globalFitParams.setStopPrompt(fitInfo.getStopPrompt());
        globalFitParams.setChiSquareTarget(fitInfo.getChiSquareTarget());
        globalFitParams.setFree(fitInfo.getFree()); //TODO translateFree(uiPanel.getFunction(), uiPanel.getFree()));;
     
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
                
                int x = pixel.getX();
                int y = pixel.getY();
                if (decayImage.fitThisPixel(x, y, c)) {
                    // set up local, pixel fit parameters
                    ILocalFitParams localFitParams = new LocalFitParams();
                    localFitParams.setY(decayImage.getPixel(x, y, c));
                    double[] decay = decayImage.getPixel(x, y, c);
                    localFitParams.setSig(null);
                    localFitParams.setFitStart(fitInfo.getStartDecay());
                    localFitParams.setFitStop(fitInfo.getStopDecay());
                    localFitParams.setParams(fitInfo.getParameters());
                    double[] yFitted = new double[1024]; //TODO ARG s/b based on bins
                    localFitParams.setYFitted(yFitted);
                    
                    pixel.setLocation(new int[] { x, y, c });                   
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
                System.out.println("finishing up " + pixelsToProcessCount + " pixels");
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
            ColorizedImageFitter imageColorizer,
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
            ChunkyPixel p = pixels[i];
            if (null != imageColorizer) {
                int[] location = p.getLocation();
                double[] results = result.getParams();
                imageColorizer.updatePixel(location, results);
            }
            fittedImage.setPixel(p.getLocation(), result.getParams()); //TODO ARG for a multichannel fit, location needs channel also; should ChunkyPixel keep track?
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

        // loop over all channels or just the current one
        for (int channel : getChannelIndices(m_fitAllChannels, m_channel, m_channels)) {
            curveFitData = new CurveFitData();
            curveFitData.setParams(params.clone()); //TODO NO NO NO s/b either from UI or fitted point or fitted whole image
            yCount = new double[m_bins];
            for (int b = 0; b < m_bins; ++b) {
                yCount[b] = 0.0;
            }

            // count photons and pixels
            int photons = 0;
            int pixels = 0;

            // sum this channel
            for (int y = 0; y < m_height; ++y) {
                for (int x = 0; x < m_width; ++x) {
                    for (int b = 0; b < m_bins; ++b) {
                        double count = getData(m_cursor, m_channel, x, y, b);
                        yCount[b] += count;
                        photons += (int) count;
                    }
                    ++pixels;
                }
            }
            curveFitData.setYCount(yCount);
            yFitted = new double[m_bins];
            curveFitData.setYFitted(yFitted);

            // use zero for current channel if it's the only one
            int nominalChannel = m_fitAllChannels ? channel : 0;
            curveFitData.setChannel(nominalChannel);
            curveFitData.setX(0);
            curveFitData.setY(0);
            curveFitData.setPixels(pixels);
            curveFitDataList.add(curveFitData);
        }

        // do the fit
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        getCurveFitter(uiPanel).fitData(dataArray, m_startBin, m_stopBin);

        // show decay and update UI parameters
        int visibleChannel = m_fitAllChannels ? m_channel : 0;
        double[] irf = null;
        if (null != m_excitationPanel) {
            // get the IRF curve scaled for total number of fitted pixels
            irf = m_excitationPanel.getValues(dataArray[visibleChannel].getPixels());
        }
        String title = "Summed";
        if (1 < m_channels) {
            title += " Channel " + (m_channel + 1);
        }
        showDecayGraph(title, uiPanel, irf, dataArray[visibleChannel]);
        uiPanel.setParameters(dataArray[visibleChannel].getParams());

        // get the results
        int channels = m_fitAllChannels ? m_channels : 1;
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

        // loop over all channels or just the current one
        for (int channel : getChannelIndices(m_fitAllChannels, m_channel, m_channels)) {
            int roiNumber = 1;
            for (Roi roi: getRois()) {
                curveFitData = new CurveFitData();
                curveFitData.setParams(params.clone());
                yCount = new double[m_bins];
                for (int b = 0; b < m_bins; ++b) {
                    yCount[b] = 0.0;
                }
                Rectangle bounds = roi.getBounds();
                int pixels = 0;
                for (int x = 0; x < bounds.width; ++x) {
                    for (int y = 0; y < bounds.height; ++y) {
                        if (roi.contains(bounds.x + x, bounds.y + y)) {
                            ++pixels;
                            for (int b = 0; b < m_bins; ++b) {
                                yCount[b] += getData(m_cursor, channel, x, y, b);
                            }
                        }
                    }
                }
                curveFitData.setYCount(yCount);
                yFitted = new double[m_bins];
                curveFitData.setYFitted(yFitted);

                // use zero for current channel if it's the only one
                int nominalChannel = m_fitAllChannels ? channel : 0;
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
        getCurveFitter(uiPanel).fitData(dataArray, m_startBin, m_stopBin);

        // show the decay graphs
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        int roiNumber = 1;
        for (Roi roi: getRois()) {
            int nominalChannel = m_fitAllChannels ? m_channel : 0;
            int dataIndex = nominalChannel * getRois().length + (roiNumber - 1);

            double[] irf = null;
            if (null != m_excitationPanel) {
                // get the IRF curve scaled for number of pixels in this ROI
                irf = m_excitationPanel.getValues(dataArray[dataIndex].getPixels());
            }
            String title = "Roi " + roiNumber;
            if (1 < m_channels) {
                title += " Channel " + (m_channel + 1);
            }
            showDecayGraph(title, uiPanel, irf, dataArray[dataIndex]);
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
        ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
        ImagePlus imagePlus = new ImagePlus("ROIs Fitted Lifetimes", imageProcessor);
        roiNumber = 1;
        for (Roi roi: getRois()) {
            int nominalChannel = m_fitAllChannels ? m_channel : 0;
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
        int channels = m_fitAllChannels ? m_channels : 1;
        //fittedPixels = makeImage(channels, 1, 1, uiPanel.getParameterCount()); //TODO ImgLib bug if you use 1, 1, 1, 4; see "imglibBug()" below.
        fittedPixels = makeImage(channels + 1, getRois().length + 1, 2, uiPanel.getParameterCount()); //TODO this is a workaround; unused pixels will remain NaNs
        LocalizableByDimCursor<DoubleType> resultsCursor = fittedPixels.createLocalizableByDimCursor();
        setFittedParamsFromData(resultsCursor, dataArray);
        return fittedPixels;
    }

    // added kludge to make moving cursors in DecayGraph do a refit.
    private Image<DoubleType> fitPixel(IUserInterfacePanel uiPanel) {
        int x = uiPanel.getX();
        int y = uiPanel.getY();
        m_startBin = uiPanel.getStart();
        m_stopBin = uiPanel.getStop();
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

        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        double params[] = uiPanel.getParameters(); //TODO wrong; params should possibly come from already fitted data
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];

        // loop over all channels or just the current one
        for (int channel : getChannelIndices(m_fitAllChannels, m_channel, m_channels)) {
            curveFitData = new CurveFitData();
            curveFitData.setParams(params.clone()); //TODO NO NO NO s/b either from UI or fitted point or fitted whole image
            yCount = new double[m_bins];
            for (int b = 0; b < m_bins; ++b) {
                yCount[b] = getData(m_cursor, channel, x, y, b);
            }
            curveFitData.setYCount(yCount);
            yFitted = new double[m_bins];
            curveFitData.setYFitted(yFitted);

            // use zero for current channel if it's the only one
            int nominalChannel = m_fitAllChannels ? channel : 0;
            curveFitData.setChannel(nominalChannel);
            curveFitData.setX(0);
            curveFitData.setY(0);
            curveFitData.setPixels(1);
            curveFitDataList.add(curveFitData);
        }
        
        // do the fit
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        getCurveFitter(uiPanel).fitData(dataArray, m_startBin, m_stopBin);

        // show decay graph for visible channel
        double irf[] = null;
        if (null != m_excitationPanel) {
            // get the IRF curve scaled for a single pixel
            irf = m_excitationPanel.getValues(1);
        }
        String title = "Pixel " + x + " " + y;
        if (1 < m_channels) {
            title += " Channel " + (m_channel + 1);
        }
        int visibleChannel = 0;
        if (m_fitAllChannels) {
            visibleChannel = m_channel;
        }
        showDecayGraph(title, uiPanel, irf, dataArray[visibleChannel]);

        // update UI parameters
        uiPanel.setParameters(dataArray[visibleChannel].getParams());
        
        // get the results
        int channels = m_fitAllChannels ? m_channels : 1;
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
    
    //TODO This fitImages is the new version and "fitEachPixelXYZ" is the old:
    //TODO need to fit all channels
    //TODO need to be a selfcontained function call
    //  = gives you fitted images and a histogram tool

    private Image<DoubleType> fitImagesDEFUNCT(IUserInterfacePanel uiPanel) {
        
        
        
        // loop over all channels
        int startChannel;
        int stopChannel;
        if (m_fitAllChannels) {
            startChannel = 0;
            stopChannel = m_channels;
        }
        else {
            startChannel = m_channel;
            stopChannel = m_channel + 1;
        }
        for (int channel = startChannel; channel < stopChannel; ++channel) {
            
        }
        
        long start = System.nanoTime();
        int pixelCount = 0;
        int totalPixelCount = totalPixelCount(m_width, m_height, m_channels, m_fitAllChannels);
        int pixelsToProcessCount = 0;

        Image<T> workImage = m_image;
        if (!SLIMBinning.NONE.equals(uiPanel.getBinning())) {
            workImage = m_binning.doBinning(uiPanel.getBinning(), m_image);
        }
        LocalizableByDimCursor<T> pixelCursor = workImage.createLocalizableByDimCursor();

        ICurveFitter curveFitterX = getCurveFitter(uiPanel); //TODO where is this used?
        double params[] = uiPanel.getParameters();

        boolean useFittedParams;
        LocalizableByDimCursor<DoubleType> resultsCursor = null;
        if (null == m_fittedImage || uiPanel.getParameterCount() != m_fittedParameterCount) {
            // can't use previous results
            useFittedParams = false;
            m_fittedParameterCount = uiPanel.getParameterCount();
            m_fittedImage = makeImage(m_channels, m_width, m_height, m_fittedParameterCount);
        }
        else {
            // ask UI whether to use previous results
            useFittedParams = uiPanel.getRefineFit();
        }
        resultsCursor = m_fittedImage.createLocalizableByDimCursor();
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ArrayList<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];

        ChunkyPixelEffectIterator pixelIterator = new ChunkyPixelEffectIterator(new ChunkyPixelTableImpl(), m_width, m_height);
        
        //TODO new style code starts only here:
        ColorizedImageFitter imageFitter = new ColorizedImageFitter();
        int components = 0;
        boolean stretched = false;
        switch (uiPanel.getFunction()) {
            case SINGLE_EXPONENTIAL:
                components = 1;
                break;
            case DOUBLE_EXPONENTIAL:
                components = 2;
                break;
            case TRIPLE_EXPONENTIAL:
                components = 3;
                break;
            case STRETCHED_EXPONENTIAL:
                stretched = true;
                break;
        }
        String outputs = uiPanel.getFittedImages();
        ColorizedImageParser parser = new ColorizedImageParser(outputs, components, stretched);
        
        ColorizedImageType[] outputImages = parser.getColorizedImages();
        imageFitter.setUpFit(outputImages, new int[] { m_width, m_height }, components);
        imageFitter.beginFit();
        
        IGlobalFitParams globalFitParams = new GlobalFitParams();
        globalFitParams.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA); ///uiPanel.getAlgorithm()); //loci.curvefitter.ICurveFitter.FitAlgorithm.RLD_LMA);
        globalFitParams.setFitFunction(FitFunction.SINGLE_EXPONENTIAL); //uiPanel.getFunction());
        globalFitParams.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));
        globalFitParams.setNoiseModel(NoiseModel.MAXIMUM_LIKELIHOOD); //uiPanel.getNoiseModel());
        globalFitParams.setChiSquareTarget(uiPanel.getChiSquareTarget());
        ///globalFitParams.setFitFunction(loci.curvefitter.ICurveFitter.FitFunction.SINGLE_EXPONENTIAL);
        ///globalFitParams.setNoiseModel(loci.curvefitter.ICurveFitter.NoiseModel.MAXIMUM_LIKELIHOOD);
        globalFitParams.setXInc(m_timeRange);
        globalFitParams.setPrompt(null);
        if (null != m_excitationPanel) {
            globalFitParams.setPrompt(m_excitationPanel.getValues(1));
        }
        ////globalFitParams.setChiSquareTarget(data[0].getChiSquareTarget());


        boolean[] free = { true, true, true };
        globalFitParams.setFree(free); //TODO BAD! s/n/b hardcoded here
      //TODO KLUDGE  globalFitParams.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));
        
        
        

        while (!m_cancel && pixelIterator.hasNext()) {
            IJ.showProgress(++pixelCount, totalPixelCount);
            ChunkyPixel pixel = pixelIterator.next();
            if (wantFitted(m_channel, pixel.getX(), pixel.getY())) {
                curveFitData = new CurveFitData();
                curveFitData.setChannel(m_channel);
                curveFitData.setX(pixel.getX());
                curveFitData.setY(pixel.getY());
                curveFitData.setParams(
                    useFittedParams ?
                        getFittedParams(resultsCursor, m_channel, pixel.getX(), pixel.getY(), m_fittedParameterCount) :
                            params.clone());
                yCount = new double[m_bins];
                for (int b = 0; b < m_bins; ++b) {
                    yCount[b] = getData(pixelCursor, m_channel, pixel.getX(), pixel.getY(), b); //binnedData[m_channel][pixel.getY()][pixel.getX()][b];
                }
                curveFitData.setYCount(yCount);
                yFitted = new double[m_bins];
                curveFitData.setYFitted(yFitted);
                curveFitDataList.add(curveFitData);
                pixelList.add(pixel);

                // process the pixels
                if (++pixelsToProcessCount >= PIXEL_COUNT) {
                    ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                    ChunkyPixel[] pixels = pixelList.toArray(new ChunkyPixel[0]);
                    processPixelsXYZ(data, pixels, imageFitter);
                    curveFitDataList.clear();
                    pixelList.clear();
                    pixelsToProcessCount = 0;
                }
            }
        }
        
        if (m_cancel) {
            IJ.showProgress(0, 0);
            cancelImageFit();
            imageFitter.cancelFit();
            return null;
        }
        else {
            if (pixelsToProcessCount > 0) {
                ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                ChunkyPixel[] pixels = pixelList.toArray(new ChunkyPixel[0]);
                processPixelsXYZ(data, pixels, imageFitter);
            }
            imageFitter.endFit();
        }

        
        //TODO so the results are not getting saved to an Imglib Image
        //TODO this technique of building an array of ICurveFitData, then breaking that down into
        // IGlobalFitParams and ILocalFitParams seems over-complicated
        
        return null;
    }
    
    
    /*
     * Helper function that processes an array of pixels.  Histogram and images
     * are updated at the end of this function.
     */
    private void processPixelsDEFUNCT(ICurveFitData[] data, ChunkyPixel[] pixels, ColorizedImageFitter imageFitter) {
        if (null == _fittingEngine) {
            _fittingEngine = Configuration.getInstance().getFittingEngine();
            _fittingEngine.setThreads(Configuration.getInstance().getThreads());
            _fittingEngine.setCurveFitter(Configuration.getInstance().getCurveFitter());
        }

        IGlobalFitParams globalFitParams = new GlobalFitParams();
        globalFitParams.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA);
        globalFitParams.setFitFunction(FitFunction.SINGLE_EXPONENTIAL);
        globalFitParams.setNoiseModel(NoiseModel.MAXIMUM_LIKELIHOOD);
        globalFitParams.setXInc(m_timeRange);
        globalFitParams.setPrompt(null);
        if (null != m_excitationPanel) {
            globalFitParams.setPrompt(m_excitationPanel.getValues(1));
        }
        globalFitParams.setChiSquareTarget(data[0].getChiSquareTarget());


        boolean[] free = { true, true, true };
        globalFitParams.setFree(free); //TODO BAD! s/n/b hardcoded here
      //TODO KLUDGE  globalFitParams.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));

        List<ILocalFitParams> dataList = new ArrayList<ILocalFitParams>();

        for (ICurveFitData datum : data) {
            ILocalFitParams localFitParams = new LocalFitParams();
            localFitParams.setY(datum.getYCount());
            localFitParams.setSig(datum.getSig());
            localFitParams.setParams(datum.getParams());
            localFitParams.setFitStart(m_startBin);
            localFitParams.setFitStop(m_stopBin);
            localFitParams.setYFitted(datum.getYFitted());
            dataList.add(localFitParams);
        }

        List<IFitResults> results = _fittingEngine.fit(globalFitParams, dataList);

        for (int i = 0; i < results.size(); ++i) {
            IFitResults result = results.get(i);
            ChunkyPixel p = pixels[i];
            int[] location = { p.getX(), p.getY() };
            imageFitter.updatePixel(location, result.getParams());
        }

        imageFitter.recalcHistogram();
    }
    
    //TODO This XYZ is the new version and "fitEachPixelX" is the old:
    //TODO need to fit all channels
    //TODO need to be a selfcontained function call
    //  = gives you fitted images and a histogram tool
    
    private Image<DoubleType> fitEachPixelLatestDEFUNCT(IUserInterfacePanel uiPanel) {
        
        // we need to generate the following for fitEachPixel nowadays:
        
        // note that every fit type needs these, not just images
        
        IFittingEngine fittingEngine = Configuration.getInstance().getFittingEngine(); // still have a _fittingEngine global also
            fittingEngine.setThreads(Configuration.getInstance().getThreads());
            fittingEngine.setCurveFitter(Configuration.getInstance().getCurveFitter());
        ChunkyPixel[] pixels; 
        IGlobalFitParams globalFitParams;
        List<ILocalFitParams> localFitParams = new ArrayList<ILocalFitParams>();
        ColorizedImageFitter imageFitter; //it's confusing to have both an imageFitter and a fittingEngine
      
        return null;
    }

    private Image<DoubleType> fitAllPixelsDEFUNCT(IUserInterfacePanel uiPanel) {
        long start = System.nanoTime();
        int pixelCount = 0;
        int totalPixelCount = totalPixelCount(m_width, m_height, m_channels, m_fitAllChannels);
        int pixelsToProcessCount = 0;

        Image<T> workImage = m_image;
        if (!SLIMBinning.NONE.equals(uiPanel.getBinning())) {
            workImage = m_binning.doBinning(uiPanel.getBinning(),  m_image);
        }
        LocalizableByDimCursor<T> pixelCursor = workImage.createLocalizableByDimCursor();

        ICurveFitter curveFitter = getCurveFitter(uiPanel); //TODO where is this used?
        double params[] = uiPanel.getParameters();

        boolean useFittedParams;
        LocalizableByDimCursor<DoubleType> resultsCursor = null;
        if (null == m_fittedImage || uiPanel.getParameterCount() != m_fittedParameterCount) {
            // can't use previous results
            useFittedParams = false;
            m_fittedParameterCount = uiPanel.getParameterCount();
            m_fittedImage = makeImage(m_channels, m_width, m_height, m_fittedParameterCount);
        }
        else {
            // ask UI whether to use previous results
            useFittedParams = uiPanel.getRefineFit();
        }
        resultsCursor = m_fittedImage.createLocalizableByDimCursor();
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ArrayList<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];

        ChunkyPixelEffectIterator pixelIterator = new ChunkyPixelEffectIterator(new ChunkyPixelTableImpl(), m_width, m_height);
        
        //TODO new style code starts only here:
        ColorizedImageFitter imageFitter = new ColorizedImageFitter();
        int components = 0;
        boolean stretched = false;
        switch (uiPanel.getFunction()) {
            case SINGLE_EXPONENTIAL:
                components = 1;
                break;
            case DOUBLE_EXPONENTIAL:
                components = 2;
                break;
            case TRIPLE_EXPONENTIAL:
                components = 3;
                break;
            case STRETCHED_EXPONENTIAL:
                stretched = true;
                break;
        }
        String outputs = uiPanel.getFittedImages();
        ColorizedImageParser parser = new ColorizedImageParser(outputs, components, stretched);
        
        ColorizedImageType[] colorizedImages = parser.getColorizedImages();
        imageFitter.setUpFit(colorizedImages, new int[] { m_width, m_height }, components);
        imageFitter.beginFit();       

        while (!m_cancel && pixelIterator.hasNext()) {
            IJ.showProgress(++pixelCount, totalPixelCount);
            ChunkyPixel pixel = pixelIterator.next();
            if (wantFitted(m_channel, pixel.getX(), pixel.getY())) {
                curveFitData = new CurveFitData();
                curveFitData.setChannel(m_channel);
                curveFitData.setX(pixel.getX());
                curveFitData.setY(pixel.getY());
                curveFitData.setParams(
                    useFittedParams ?
                        getFittedParams(resultsCursor, m_channel, pixel.getX(), pixel.getY(), m_fittedParameterCount) :
                            params.clone());
                yCount = new double[m_bins];
                for (int b = 0; b < m_bins; ++b) {
                    yCount[b] = getData(pixelCursor, m_channel, pixel.getX(), pixel.getY(), b); //binnedData[m_channel][pixel.getY()][pixel.getX()][b];
                }
                curveFitData.setYCount(yCount);
                yFitted = new double[m_bins];
                curveFitData.setYFitted(yFitted);
                curveFitDataList.add(curveFitData);
                pixelList.add(pixel);

                // process the pixels
                if (++pixelsToProcessCount >= PIXEL_COUNT) {
                    ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                    ChunkyPixel[] pixels = pixelList.toArray(new ChunkyPixel[0]);
                    processPixelsXYZ(data, pixels, imageFitter);
                    curveFitDataList.clear();
                    pixelList.clear();
                    pixelsToProcessCount = 0;
                }
            }
        }
        
        if (m_cancel) {
            IJ.showProgress(0, 0);
            cancelImageFit();
            imageFitter.cancelFit();
            return null;
        }
        else {
            if (pixelsToProcessCount > 0) {
                ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                ChunkyPixel[] pixels = pixelList.toArray(new ChunkyPixel[0]);
                processPixelsXYZ(data, pixels, imageFitter);
            }
            imageFitter.endFit();
        }

        
        //TODO so the results are not getting saved to an Imglib Image
        //TODO this technique of building an array of ICurveFitData, then breaking that down into
        // IGlobalFitParams and ILocalFitParams seems over-complicated
        
        return null;
    }
    /*
     * Helper function that processes an array of pixels.  Histogram and images
     * are updated at the end of this function.
     */
    
    

  
    // copied 1/12 to modify into "processPixels" above:
    private void processPixelsXYZhuhuhDEFUNCT(ICurveFitData[] data, ChunkyPixel[] pixels, ColorizedImageFitter imageFitter) {
        if (null == _fittingEngine) {
            _fittingEngine = Configuration.getInstance().getFittingEngine();
            _fittingEngine.setThreads(Configuration.getInstance().getThreads());
            _fittingEngine.setCurveFitter(Configuration.getInstance().getCurveFitter());
        }

        IGlobalFitParams globalFitParams = new GlobalFitParams();
        globalFitParams.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA);
        globalFitParams.setFitFunction(FitFunction.SINGLE_EXPONENTIAL);
        globalFitParams.setNoiseModel(NoiseModel.MAXIMUM_LIKELIHOOD);
        globalFitParams.setXInc(m_timeRange);
        globalFitParams.setPrompt(null);
        if (null != m_excitationPanel) {
            globalFitParams.setPrompt(m_excitationPanel.getValues(1));
        }
        globalFitParams.setChiSquareTarget(data[0].getChiSquareTarget());


        boolean[] free = { true, true, true };
        globalFitParams.setFree(free); //TODO BAD! s/n/b hardcoded here
      //TODO KLUDGE  globalFitParams.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));

        List<ILocalFitParams> dataList = new ArrayList<ILocalFitParams>();

        for (ICurveFitData datum : data) {
            ILocalFitParams localFitParams = new LocalFitParams();
            localFitParams.setY(datum.getYCount());
            localFitParams.setSig(datum.getSig());
            localFitParams.setParams(datum.getParams());
            localFitParams.setFitStart(m_startBin);
            localFitParams.setFitStop(m_stopBin);
            localFitParams.setYFitted(datum.getYFitted());
            dataList.add(localFitParams);
        }

        List<IFitResults> results = _fittingEngine.fit(globalFitParams, dataList);

        for (int i = 0; i < results.size(); ++i) {
            IFitResults result = results.get(i);
            ChunkyPixel p = pixels[i];
            int[] location = { p.getX(), p.getY() };
            imageFitter.updatePixel(location, result.getParams());
        }

        imageFitter.recalcHistogram();
    }
 
    /*
     * Helper function that processes an array of pixels.  Histogram and images
     * are updated at the end of this function.
     */
    // THis is only called from defunct image fitters.
    private void processPixelsXYZ(ICurveFitData[] data, ChunkyPixel[] pixels, ColorizedImageFitter imageFitter) {
        if (null == _fittingEngine) {
            _fittingEngine = Configuration.getInstance().getFittingEngine();
            _fittingEngine.setThreads(Configuration.getInstance().getThreads());
            _fittingEngine.setCurveFitter(Configuration.getInstance().getCurveFitter());
        }

        IGlobalFitParams globalFitParams = new GlobalFitParams();
        globalFitParams.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA);
        globalFitParams.setFitFunction(FitFunction.SINGLE_EXPONENTIAL);
        globalFitParams.setNoiseModel(NoiseModel.MAXIMUM_LIKELIHOOD);
        globalFitParams.setXInc(m_timeRange);
        globalFitParams.setPrompt(null);
        if (null != m_excitationPanel) {
            globalFitParams.setPrompt(m_excitationPanel.getValues(1));
        }
        globalFitParams.setChiSquareTarget(data[0].getChiSquareTarget());


        boolean[] free = { true, true, true };
        globalFitParams.setFree(free); //TODO BAD! s/n/b hardcoded here
      //TODO KLUDGE  globalFitParams.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));

        List<ILocalFitParams> dataList = new ArrayList<ILocalFitParams>();

        for (ICurveFitData datum : data) {
            ILocalFitParams localFitParams = new LocalFitParams();
            localFitParams.setY(datum.getYCount());
            localFitParams.setSig(datum.getSig());
            localFitParams.setParams(datum.getParams());
            localFitParams.setFitStart(m_startBin);
            localFitParams.setFitStop(m_stopBin);
            localFitParams.setYFitted(datum.getYFitted());
            dataList.add(localFitParams);
        }

        List<IFitResults> results = _fittingEngine.fit(globalFitParams, dataList);

        for (int i = 0; i < results.size(); ++i) {
            IFitResults result = results.get(i);
            ChunkyPixel p = pixels[i];
            int[] location = { p.getX(), p.getY() };
            imageFitter.updatePixel(location, result.getParams());
        }

        imageFitter.recalcHistogram();
    }
 
    /*
     * Fits each and every pixel.  This is the most complicated fit.
     *
     * If a channel is visible it is fit first and drawn incrementally.
     *
     * Results of the fit go to VisAD for analysis.
     */
    //TODO this is the old version
    private Image<DoubleType> fitEachPixelXDEFUNCT(IUserInterfacePanel uiPanel) {
        long start = System.nanoTime();
        int pixelCount = 0;
        int totalPixelCount = totalPixelCount(m_width, m_height, m_channels, m_fitAllChannels);
        int pixelsToProcessCount = 0;

        Image<T> workImage = m_image;
        if (!SLIMBinning.NONE.equals(uiPanel.getBinning())) {
            workImage = m_binning.doBinning(uiPanel.getBinning(),  m_image);
        }
        LocalizableByDimCursor<T> pixelCursor = workImage.createLocalizableByDimCursor();

        ICurveFitter curveFitter = getCurveFitter(uiPanel);     
        double params[] = uiPanel.getParameters();

        boolean useFittedParams;
        LocalizableByDimCursor<DoubleType> resultsCursor = null;
        if (null == m_fittedImage || uiPanel.getParameterCount() != m_fittedParameterCount) {
            // can't use previous results
            useFittedParams = false;
            m_fittedParameterCount = uiPanel.getParameterCount();
            m_fittedImage = makeImage(m_channels, m_width, m_height, m_fittedParameterCount);
        }
        else {
            // ask UI whether to use previous results
            useFittedParams = uiPanel.getRefineFit();
        }
        resultsCursor = m_fittedImage.createLocalizableByDimCursor();
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ArrayList<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];

        // special handling for visible channel
        if (m_visibleFit) {
            // show colorized image
            DataColorizer2 dataColorizer = new DataColorizer2(m_width, m_height, m_algorithm + " Fitted Lifetimes");

            ChunkyPixelEffectIterator pixelIterator = new ChunkyPixelEffectIterator(new ChunkyPixelTableImpl(), m_width, m_height);

            while (!m_cancel && pixelIterator.hasNext()) {
                if (m_cancel) {
                    IJ.showProgress(0, 0); //TODO kludgy to have this here and also below; get rid of this but make the dataColorizer go away regardless
                    dataColorizer.quit();
                    cancelImageFit();
                    return null;
                }
                IJ.showProgress(++pixelCount, totalPixelCount);
                ChunkyPixel pixel = pixelIterator.next();
                if (wantFitted(m_channel, pixel.getX(), pixel.getY())) {
                    curveFitData = new CurveFitData();
                    curveFitData.setChannel(m_channel);
                    curveFitData.setX(pixel.getX());
                    curveFitData.setY(pixel.getY());
                    curveFitData.setParams(
                            useFittedParams ?
                                getFittedParams(resultsCursor, m_channel, pixel.getX(), pixel.getY(), m_fittedParameterCount) :
                                params.clone());
                    yCount = new double[m_bins];
                    for (int b = 0; b < m_bins; ++b) {
                        yCount[b] = getData(pixelCursor, m_channel, pixel.getX(), pixel.getY(), b); //binnedData[m_channel][pixel.getY()][pixel.getX()][b];
                    }
                    curveFitData.setYCount(yCount);
                    yFitted = new double[m_bins];
                    curveFitData.setYFitted(yFitted);
                    curveFitDataList.add(curveFitData);
                    pixelList.add(pixel);

                    // process the pixels
                    if (++pixelsToProcessCount >= PIXEL_COUNT) {
                        if (OLD_STYLE) {
                            pixelsToProcessCount = 0;
                            ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                            curveFitDataList.clear();
                            curveFitter.fitData(data, m_startBin, m_stopBin);
                            setFittedParamsFromData(resultsCursor, data);
                            colorizePixels(dataColorizer, m_channel, data, pixelList.toArray(new ChunkyPixel[0]));
                            pixelList.clear();
                        }
                        else {
                            pixelsToProcessCount = 0;
                            ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                            curveFitDataList.clear();
                            
                            if (null == _fittingEngine) {
                                _fittingEngine = Configuration.getInstance().getFittingEngine();
                                _fittingEngine.setThreads(Configuration.getInstance().getThreads());
                                _fittingEngine.setCurveFitter(Configuration.getInstance().getCurveFitter());
                            }
                                                        
                            
                            IGlobalFitParams globalFitParams = new GlobalFitParams();
                            globalFitParams.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA);
                            globalFitParams.setFitFunction(FitFunction.SINGLE_EXPONENTIAL);
                            globalFitParams.setXInc(m_timeRange);
                            globalFitParams.setPrompt(null);
                            if (null != m_excitationPanel) {
                                globalFitParams.setPrompt(m_excitationPanel.getValues(1));
                            }
                            globalFitParams.setChiSquareTarget(data[0].getChiSquareTarget());
                            globalFitParams.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));
                            
                            java.util.List<ILocalFitParams> dataList = new ArrayList<ILocalFitParams>();
                            
                            for (ICurveFitData datum : data) {
                                ILocalFitParams localFitParams = new LocalFitParams();
                                localFitParams.setY(datum.getYCount());
                                localFitParams.setSig(datum.getSig());
                                localFitParams.setParams(datum.getParams());
                                localFitParams.setFitStart(m_startBin);
                                localFitParams.setFitStop(m_stopBin);
                                localFitParams.setYFitted(datum.getYFitted());
                                dataList.add(localFitParams);
                            }
                            
                            java.util.List<IFitResults> results = _fittingEngine.fit(globalFitParams, dataList);
                            
                            double[] lifetimes = new double[pixelList.size()];
                            for (int i = 0; i < pixelList.size(); ++i) {
                                lifetimes[i] = results.get(i).getParams()[3];
                            }

                           //TODO ARG setFittedParamsFromData(resultsCursor, data);
                            colorizePixelsII(dataColorizer, m_channel, lifetimes, pixelList.toArray(new ChunkyPixel[0]));
                            pixelList.clear();
                            
                            //TODO so the results are not getting saved to an Imglib Image
                            //TODO this technique of building an array of ICurveFitData, then breaking that down into
                            // IGlobalFitParams and ILocalFitParams

                        }

                    }
                }
            }
            // handle any leftover pixels
            if (!m_cancel && pixelsToProcessCount > 0) {
                if (OLD_STYLE) {
                    pixelsToProcessCount = 0;
                    ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                    curveFitDataList.clear();
                    curveFitter.fitData(data, m_startBin, m_stopBin);
                    setFittedParamsFromData(resultsCursor, data);
                    colorizePixels(dataColorizer, m_channel, data, pixelList.toArray(new ChunkyPixel[0]));
 
                }
                else {
                            pixelsToProcessCount = 0;
                            ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                            curveFitDataList.clear();
                            
                            if (null == _fittingEngine) {
                                _fittingEngine = Configuration.getInstance().getFittingEngine();
                                _fittingEngine.setThreads(Configuration.getInstance().getThreads());
                                _fittingEngine.setCurveFitter(Configuration.getInstance().getCurveFitter());
                            }
                                                        
                            
                            IGlobalFitParams globalFitParams = new GlobalFitParams();
                            globalFitParams.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA);
                            globalFitParams.setFitFunction(FitFunction.SINGLE_EXPONENTIAL);
                            globalFitParams.setXInc(m_timeRange);
                            globalFitParams.setPrompt(null);
                            if (null != m_excitationPanel) {
                                globalFitParams.setPrompt(m_excitationPanel.getValues(1));
                            }
                            globalFitParams.setChiSquareTarget(data[0].getChiSquareTarget());
                            globalFitParams.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));
                            
                            java.util.List<ILocalFitParams> dataList = new ArrayList<ILocalFitParams>();
                            
                            for (ICurveFitData datum : data) {
                                ILocalFitParams localFitParams = new LocalFitParams();
                                localFitParams.setY(datum.getYCount());
                                localFitParams.setSig(datum.getSig());
                                localFitParams.setParams(datum.getParams());
                                localFitParams.setFitStart(m_startBin);
                                localFitParams.setFitStop(m_stopBin);
                                localFitParams.setYFitted(datum.getYFitted());
                                dataList.add(localFitParams);
                            }
                            
                            java.util.List<IFitResults> results = _fittingEngine.fit(globalFitParams, dataList);
                            
                            double[] lifetimes = new double[pixelList.size()];
                            for (int i = 0; i < pixelList.size(); ++i) {
                                lifetimes[i] = results.get(i).getParams()[3];
                            }

                           //TODO ARG setFittedParamsFromData(resultsCursor, data);
                            colorizePixelsII(dataColorizer, m_channel, lifetimes, pixelList.toArray(new ChunkyPixel[0]));

                }
            }
        }
        if (m_cancel) {
            IJ.showProgress(0, 0); //TODO the code below s/b showing progress also
           // dataColorizer.quit(); //TODO no longer visible in this code
            cancelImageFit();
            return null;
        }
 
        // any channels still to be fitted?
        for (int channel : channelIndexArray(m_channel, m_channels, m_visibleFit, m_fitAllChannels)) {
            for (int y = 0; y < m_height; ++y) {
                for (int x = 0; x < m_width; ++x) {
                    if (m_visibleFit) {
                        IJ.showProgress(++pixelCount, totalPixelCount);
                    }
 
                    if (wantFitted(channel, x, y)) {
                         ++pixelsToProcessCount;
                         curveFitData = new CurveFitData();
                         curveFitData.setChannel(channel);
                         curveFitData.setX(x);
                         curveFitData.setY(y);
                         curveFitData.setParams(
                             useFittedParams ?
                                 getFittedParams(resultsCursor, channel, x, y, m_fittedParameterCount) :
                                 params.clone());
                         yCount = new double[m_bins];
                         for (int b = 0; b < m_bins; ++b) {
                             yCount[b] = getData(pixelCursor, channel, x, y, b); //binnedData[channel][y][x][b];
                         }
                         curveFitData.setYCount(yCount);
                         yFitted = new double[m_bins];
                         curveFitData.setYFitted(yFitted);
                         curveFitDataList.add(curveFitData);
                    }
                    
                    if (m_cancel) {
                        cancelImageFit();
                        return null;
                    }
                }
                // every row, process pixels as needed
                if (pixelsToProcessCount >= PIXEL_COUNT) {
                    pixelsToProcessCount = 0;
                    ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                    curveFitDataList.clear();
                    curveFitter.fitData(data, m_startBin, m_stopBin);
                    setFittedParamsFromData(resultsCursor, data);
                }
            }
        }
        // handle any leftover pixels
        if (pixelsToProcessCount > 0) {
            ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
            curveFitter.fitData(data, m_startBin, m_stopBin);
            setFittedParamsFromData(resultsCursor, data);
        }


        uiPanel.setFittedParameterCount(m_fittedParameterCount); //TODO kind of strange since I got that info from uiPanel earlier...  This s/b reset(true) or something Also, it doesn't really do anything in uiPanel

        long elapsed = System.nanoTime() - start;
        System.out.println("nanoseconds " + elapsed);
        showRunningAverage(elapsed);

        return m_fittedImage;
    }

    static int s_avgIndex;
    static ArrayList<Long> s_list = new ArrayList<Long>();

    private void showRunningAverage(long elapsed) {
        s_list.add(elapsed);
        long total = 0;
        for (long time : s_list) {
            total += time;
        }
        total /= s_list.size();
        System.out.println("average " + total + " after " + s_list.size() + " trials");
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
        if (m_hasChannels) {
            dim = new int[] { x, y, bin, channel }; //TODO ARG is this order guaranteed?
        }
        else {
            dim = new int[] { x, y, bin };
        }
        cursor.moveTo(dim);
        return cursor.getType().getRealFloat();
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

        System.out.println("channels width height params " + channels + " " + width + " " + height + " " + parameters);
        
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
    
    private void setFittedParamsFromDataII(LocalizableByDimCursor<DoubleType> cursor, ICurveFitData dataArray[]) {
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
        m_fittedImage = null;
        m_fittedParameterCount = 0;
    }

    /**
     * Visibly processes a batch of pixels.
     *
     * @param dataColorizer automatically sets colorization range and updates colorized image
     * @param height passed in to fix a vertical orientation problem
     * @channel current channel
     * @param data list of data corresponding to pixels to be fitted
     * @param pixels parallel list of rectangles with which to draw the fitted pixel
     */
    void colorizePixels(DataColorizer2 dataColorizer, int channel, ICurveFitData data[], ChunkyPixel pixels[]) {

        // draw as you go; 'chunky' pixels get smaller as the overall fit progresses
        for (int i = 0; i < pixels.length; ++i) {
            ChunkyPixel pixel = pixels[i];
            //TODO tau is 3, 1 is C double lifetime = data[i].getParams()[1];
            double lifetime = data[i].getParams()[3];

            //TODO quick fix
            if (lifetime < 0.0) {
                System.out.println("negative lifetime " + lifetime + " at " + pixel.getX() + " " + pixel.getY());
                return;
            }

            //TODO debugging:
            //if (lifetime > 2 * m_param[1]) {
            //    System.out.println("BAD FIT??? x " + pixel.getX() + " y " + pixel.getY() + " fitted lifetime " + lifetime);
            //}

            //TODO BUG:
            // With the table as is, you can get
            //   x   y   w   h
            //   12  15  2   1
            //   14  15  2   1
            // all within the same drawing cycle.
            // So it looks like a 4x1 slice gets drawn (it
            // is composed of two adjacent 2x1 slices with
            // potentially two different colors).
            //if (pixel.getWidth() == 2) {
            //    System.out.println("x " + pixel.getX() + " y " + pixel.getY() + " w " + pixel.getWidth() + " h " + pixel.getHeight());
            //}
            //System.out.println("w " + pixel.getWidth() + " h " + pixel.getHeight());
            //System.out.println("lifetime is " + lifetime);
            //Color color = lifetimeColorMap(MAXIMUM_LIFETIME, lifetime);
            //imageProcessor.setColor(color);
            boolean firstTime = true;
            for (int x = pixel.getX(); x < pixel.getX() + pixel.getWidth(); ++x) {
                for (int y = pixel.getY(); y < pixel.getY() + pixel.getHeight(); ++y) {
                    if (wantFitted(channel, x, y)) {
                        dataColorizer.setData(firstTime, x, y , lifetime);
                        firstTime = false;
                    }
                }
            }
        }
        dataColorizer.update();
    }
    
    void colorizePixelsII(DataColorizer2 dataColorizer, int channel, double[] lifetime, ChunkyPixel pixels[]) {

        // draw as you go; 'chunky' pixels get smaller as the overall fit progresses
        for (int i = 0; i < pixels.length; ++i) {
            ChunkyPixel pixel = pixels[i];
            //TODO tau is 3, 1 is C double lifetime = data[i].getParams()[1];


            //TODO quick fix
            if (lifetime[i] < 0.0) {
                System.out.println("negative lifetime " + lifetime + " at " + pixel.getX() + " " + pixel.getY());
                return;
            }

            //TODO debugging:
            //if (lifetime > 2 * m_param[1]) {
            //    System.out.println("BAD FIT??? x " + pixel.getX() + " y " + pixel.getY() + " fitted lifetime " + lifetime);
            //}

            //TODO BUG:
            // With the table as is, you can get
            //   x   y   w   h
            //   12  15  2   1
            //   14  15  2   1
            // all within the same drawing cycle.
            // So it looks like a 4x1 slice gets drawn (it
            // is composed of two adjacent 2x1 slices with
            // potentially two different colors).
            //if (pixel.getWidth() == 2) {
            //    System.out.println("x " + pixel.getX() + " y " + pixel.getY() + " w " + pixel.getWidth() + " h " + pixel.getHeight());
            //}
            //System.out.println("w " + pixel.getWidth() + " h " + pixel.getHeight());
            //System.out.println("lifetime is " + lifetime);
            //Color color = lifetimeColorMap(MAXIMUM_LIFETIME, lifetime);
            //imageProcessor.setColor(color);
            boolean firstTime = true;
            for (int x = pixel.getX(); x < pixel.getX() + pixel.getWidth(); ++x) {
                for (int y = pixel.getY(); y < pixel.getY() + pixel.getHeight(); ++y) {
                    if (wantFitted(channel, x, y)) {
                        dataColorizer.setData(firstTime, x, y , lifetime[i]);
                        firstTime = false;
                    }
                }
            }
        }
        dataColorizer.update();
    }
    
    /**
     * Checks criterion for whether this pixel needs to get fitted or drawn.
     *
     * @param channel
     * @param x
     * @param y
     * @return whether to include or ignore this pixel
     */
    boolean wantFitted(int channel, int x, int y) {
        return (aboveThreshold(channel, x, y) & isInROIs(x, y));
    }

    /**
     * Checks whether a given pixel is above threshold photon count value.
     *
     * @param channel
     * @param x
     * @param y
     * @return whether above threshold
     */
    boolean aboveThreshold(int channel, int x, int y) {
        return (m_threshold <= m_grayScaleImage.getPixel(channel, x, y));
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
        curveFitter.setFitFunction(fitFunction);
        curveFitter.setNoiseModel(uiPanel.getNoiseModel());
        curveFitter.setXInc(m_timeRange);
        curveFitter.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));
        if (null != m_excitationPanel) {
            // get the raw, unscaled IRF curve (will get scaled for number of pixels later)
            curveFitter.setInstrumentResponse(m_excitationPanel.getValues(1));
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
    private void showDecayGraph(final String title, final IUserInterfacePanel uiPanel, final double irf[], final ICurveFitData data) {
        loci.slim.refactor.ui.charts.IDecayGraph decayGraph = loci.slim.refactor.ui.charts.DecayGraph.getInstance();
        JFrame frame = decayGraph.init(uiPanel.getFrame(), m_bins, m_timeRange);
        decayGraph.setTitle(title);
        int start = uiPanel.getStart();
        int stop = uiPanel.getStop();
        decayGraph.setStartStop(start, stop);
        decayGraph.setData(irf, data);
        decayGraph.setStartStopListener(
            new IStartStopListener() {
                public void setStartStop(int start, int stop) {
                    uiPanel.setStart(start, false);
                    uiPanel.setStop(stop, true); // do a refit
                }
            }
        );
    }
}
