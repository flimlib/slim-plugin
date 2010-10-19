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
import ij.gui.ImageWindow;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import imagej.process.ImageUtils;

import imagej.io.ImageOpener;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import loci.slim.colorizer.DataColorizer;
import loci.slim.ui.IStartStopListener;
import loci.slim.ui.IUserInterfacePanel;
import loci.slim.ui.UserInterfacePanel;
import loci.common.DataTools;
import loci.curvefitter.CurveFitData;
import loci.curvefitter.GrayCurveFitter;
import loci.curvefitter.GrayNRCurveFitter;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;
import loci.curvefitter.JaolhoCurveFitter;
import loci.curvefitter.MarkwardtCurveFitter;
import loci.curvefitter.SLIMCurveFitter;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/SLIMProcessor.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/SLIMProcessor.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class SLIMProcessor <T extends RealType<T>> implements MouseListener {
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String LIFETIME = "Lifetime";
    private static final String CHANNELS = "Channels";

    // this affects how lifetimes are colorized:
    private static final double MAXIMUM_LIFETIME = 0.075; // for fitting fake with Jaolho // for fitting brian with barber triple integral 100.0f X tau vs lambda issue here

    // this affects how many pixels we process at once
    private static final int PIXEL_COUNT = 128; //32;//16;

    // Unicode special characters
    private static final Character CHI    = '\u03c7';
    private static final Character SQUARE = '\u00b2';
    private static final Character TAU    = '\u03c4';
    private static final Character LAMBDA = '\u03bb';
    private static final Character SIGMA  = '\u03c3';
    private static final Character SUB_1  = '\u2081';
    private static final Character SUB_2  = '\u2082';
    private static final Character SUB_3  = '\u2083';

    private Object m_synchFit = new Object();
    private volatile boolean m_fitInProgress;
    private volatile boolean m_fitted;

    //TODO total kludge; just to get started
    private boolean m_fakeData = false;

    private static final String FILE_KEY = "file";
    private String m_file;

    IFormatReader m_reader;

    // Actual data values, dimensioned [channel][row][column][bin]
    protected int[][][][] m_data;

    private ImageProcessor m_grayscaleImageProcessor;
    private Canvas m_grayscaleCanvas;

    // data parameters //TODO Curtis has these as protected
    private int m_width;
    private int m_height;
    private int[] m_cLengths;
    private int m_timeBins;
    private int m_channels;
    private int m_lifetimeIndex;
    private int m_spectraIndex;

    private boolean m_little;
    private int m_pixelType;
    private int m_bpp;
    private boolean m_floating;
    private float m_timeRange;
    private int m_minWave, m_waveStep; //, m_maxWave;

    public enum FitRegion {
        SUMMED, ROI, POINT, EACH
    }

    public enum FitAlgorithm { //TODO not really algorithm, usu. LMA
       JAOLHO, /*AKUTAN,*/ BARBER_RLD, BARBER_LMA, MARKWARDT, BARBER2_RLD, BARBER2_LMA, SLIMCURVE_RLD, SLIMCURVE_LMA
    }

    public enum FitFunction {
        SINGLE_EXPONENTIAL, DOUBLE_EXPONENTIAL, TRIPLE_EXPONENTIAL, STRETCHED_EXPONENTIAL
    }

    private FitRegion m_region;
    private FitAlgorithm m_algorithm;
    private FitFunction m_function;

    private int m_x;
    private int m_y;
    private int m_channel;

    private double[] m_param = new double[7];
    private boolean[] m_free = { true, true, true, true, true, true, true };

    private int m_startBin;
    private int m_stopBin;
    private int m_startX;
    private int m_threshold;
    private float m_chiSqTarget;

    private int m_debug = 0;

    public SLIMProcessor() {
        m_fitInProgress = false;
        m_fitted = false;
    }

    public void processImage(Image<T> image) {
        boolean success = false;
System.out.println("processImage " + image);
        if (newLoadData(image)) {
            // create a grayscale image from the data
            createGlobalGrayScale();
            while (true) {
                // ask what kind fo fit
                if (!showFitDialog()) {
                    break;
                }
                // ask for fit parameters
                if (!showFitParamsDialog()) {
                    break;
                }
                fitData();
            }
        }
        /*
        m_width = ImageUtils.getWidth(image);
        m_height = ImageUtils.getHeight(image);
        m_timeBins = ImageUtils.getDimSize(image, FormatTools.LIFETIME, 0);
        
        m_data = new int[m_channels][m_height][m_width][m_timeBins];
        final LocalizableByDimCursor<?> cursor = image.createLocalizableByDimCursor();
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
                        if (m_data[channel][y][x][bin] > 100 && ++m_debug < 100) {
                            System.out.println("m_data " + m_data[channel][y][x][bin] + " cursor " + cursor.getType().getRealFloat());
                        }
                    }
                }
            }
        }

        // patch things up
        m_timeRange = 10.0f;
        m_minWave = 400;
        m_waveStep = 10;
        */
    }

    /**
     * Run method for the plugin.  Throws up a file dialog.
     *
     * @param arg
     */
    public void process(String arg) {
        //IUserInterfacePanel uiPanel = new UserInterfacePanel(true);
        //JPanel panel = uiPanel.getPanel();
        
        m_channel = 0; //TODO s/b a JSlider that controls current channel
        
        boolean success = false;
        if (showFileDialog(getFileFromPreferences())) {
            if (m_fakeData) {
                fakeData();
                success = true;
            }
            else {
                if (newLoadData(loadImage(m_file))) {
                    saveFileInPreferences(m_file);
                    success = true;
                }
            }
        }
        
        if (success) {
            // create a grayscale image from the data
            createGlobalGrayScale();
            while (true) {
                // ask what kind fo fit
                if (!showFitDialog()) {
                    break;
                }
                // ask for fit parameters
                if (!showFitParamsDialog()) {
                    break;
                }
                fitData();
            }
        }
    }

    /**
     * Prompts for a .sdt file.
     *
     * @param defaultFile
     * @return
     */
    private boolean showFileDialog(String defaultFile) {
        //TODO shouldn't UI be in separate class?
        //TODO need to include fiji-lib.jar in repository:
        //GenericDialogPlus dialog = new GenericDialogPlus("Load Data");
        GenericDialog dialog = new GenericDialog("Load Data");
        //TODO works with GenericDialogPlus, dialog.addFileField("File:", defaultFile, 24);
        dialog.addStringField("File", defaultFile);
        dialog.addCheckbox("Fake data", m_fakeData);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }

        m_file = dialog.getNextString();
        m_fakeData = dialog.getNextBoolean();

        return true;
    }

    private Image<T> loadImage(String file) {
        ImageOpener imageOpener = new ImageOpener();
        Image<T> image = null;
        try {
            image = imageOpener.openImage(file);
        }
        catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
        return image;
    }
   
    private boolean newLoadData(Image<T> image) {
        int[] dimensions = image.getDimensions();
        System.out.println("dimensions size is " + dimensions.length);
        /*
        Map map = dimensionMap(image.getName());
        Integer xIndex, yIndex, lifetimeIndex, channelIndex;
        xIndex = (Integer) map.get(X);
        yIndex = (Integer) map.get(Y);
        lifetimeIndex = (Integer) map.get(LIFETIME);
        if (null != xIndex && null != yIndex && null != lifetimeIndex) {
            m_width = dimensions[xIndex];
            m_height = dimensions[yIndex];
            m_timeBins = dimensions[lifetimeIndex];
        }
        else {
            System.out.println("Can't find dimensions of .sdt Image " + image.getName());
            return false;
        }
        m_channels = 1;
        channelIndex = (Integer) map.get(CHANNELS);
        if (null != channelIndex) {
            System.out.println("Do have channel dimension");
            m_channels = dimensions[channelIndex];
        }
        else System.out.println("Don't have channel dimension");
        */
        Integer xIndex, yIndex, lifetimeIndex, channelIndex;
        m_width = ImageUtils.getWidth(image);
        m_height = ImageUtils.getHeight(image);
        m_channels = ImageUtils.getNChannels(image);
        m_timeBins = ImageUtils.getDimSize(image, FormatTools.LIFETIME);
        System.out.println("timeBins is " + m_timeBins);
        int index = 0;
        xIndex = index++;
        yIndex = index++;
        if (m_channels > 1) {
            channelIndex = index++;
        }
        else {
            channelIndex = null;
        }
        lifetimeIndex = index;

        System.out.println("width " + m_width + " height " + m_height + " timeBins " + m_timeBins + " channels " + m_channels);
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
                    }
                }
            }
        }
        cursor.close();
        // print out some useful information about the image
        //System.out.println(image);
        //final Cursor<T> cursor = image.createCursor();
        //cursor.fwd();
        //System.out.println("\tType = " + cursor.getType().getClass().getName());
        //cursor.close();

        // patch things up
        m_timeRange = 10.0f;
        m_minWave = 400;
        m_waveStep = 10;

        return true;
    }

    /*
     * This method parses a string of the format:
     * "Name [X Y Timebins]" and builds a map with
     * the dimensions 'X', 'Y', and 'Timebins' mapped
     * to the dimension indices.
     *
     * Temporary kludge.
     */
    private Map<String, Integer> dimensionMap(String name) {
        System.out.println("name is " + name);
        Map<String, Integer> map = new HashMap<String, Integer>();
        int startIndex = name.indexOf('[') + 1;
        int endIndex = name.indexOf(']');
        String coded = name.substring(startIndex, endIndex);
        String dimensions[] = coded.split(" ");
        int index = 0;
        for (String dimension : dimensions) {
            map.put(dimension, index++);
        }
        return map;
    }

    /**
     * Loads the .sdt file.
     * Based on the loci.slim.SlimData constructor.
     *
     * @param file
     * @return whether successful
     */
    private boolean loadFile(String file) {
        if (m_fakeData) return true;
        boolean status = false;
        try {
            // read file header
            m_reader = new ChannelSeparator();
            m_reader.setId(file);
            m_width = m_reader.getSizeX();
            m_height = m_reader.getSizeY();
            m_cLengths = m_reader.getChannelDimLengths();
            String[] cTypes = m_reader.getChannelDimTypes();
            m_timeBins = m_channels = 1;
            m_lifetimeIndex = m_spectraIndex = -1;
            for (int i=0; i<cTypes.length; i++) {
                if (cTypes[i].equals(FormatTools.LIFETIME)) {
                    m_timeBins = m_cLengths[i];
                    m_lifetimeIndex = i;
                }
                else if (cTypes[i].equals(FormatTools.SPECTRA)) {
                    m_channels = m_cLengths[i];
                    m_spectraIndex = i;
                }
                else if (m_lifetimeIndex < 0 && cTypes[i].equals(FormatTools.CHANNEL)) {
                    m_timeBins = m_cLengths[i];
                    m_lifetimeIndex = i;
                }
            }
            m_little = m_reader.isLittleEndian();
            m_pixelType = m_reader.getPixelType();
            m_bpp = FormatTools.getBytesPerPixel(m_pixelType);
            m_floating = FormatTools.isFloatingPoint(m_pixelType);

     //TODO won't compile with my version of the jar: Number timeBase = (Number) m_reader.getGlobalMetadata().get("time base");
     //TODO fix:
            Number timeBase = null;
            m_timeRange = timeBase == null ? Float.NaN : timeBase.floatValue();
            if (m_timeRange != m_timeRange) m_timeRange = 10.0f;
            m_minWave = 400;
            m_waveStep = 10;
            //m_binRadius = 3;
            status = true;
        }
        catch (Exception e) {

        }
        return status;
    }

    /**
     * This routine creates an artificial set of data that is useful to test fitting.
     *
     * @return whether successful
     */
    private boolean fakeData() {
        m_width = 50;
        m_height = 50;
        m_timeBins = 20;
        m_channels = 1;
        m_timeRange = 10.0f;
        m_minWave = 400;
        m_waveStep = 10;

        double A;
        double lambda;
        double b = 1.0;

        // show colorized lifetimes
        DataColorizer dataColorizer = new DataColorizer(m_width, m_height, "Fake Data");
        //ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
        //ImagePlus imagePlus = new ImagePlus("Fake Data", imageProcessor);

        m_data = new int[m_channels][m_height][m_width][m_timeBins];
        for (int y = 0; y < m_height; ++y) {
            A = 1000.0 + y  * 10000.0; // was 1000.0; bumped up Tuesday July 27 trying to get Barber LMA to work - didn't help.
            for (int x = 0; x < m_width; ++x) {
                double tmpX = x;
                lambda = 0.05 + x * 0.0005d; //0.0001 + x * .001; //0.5 + x * 0.01; // .002500 + x * .01;
                //System.out.println("lambda " + lambda + " color " + lambdaColorMap(MAXIMUM_LAMBDA, lambda));
                dataColorizer.setData(x, y, lambda);
                //imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lambda));
                //imageProcessor.drawPixel(x, y);
                for (int t = 0; t < m_timeBins; ++t) {
                    m_data[0][y][x][t] = (int)(A * Math.exp(-lambda * m_timeRange * t) + b);
                }
                //System.out.print(" " + m_data[0][y][x][0]);
                if (5 == x && 5 == y) System.out.println("at (5, 5) A is " + A + " lambda " + lambda + " b " + b);
                if (10 == x && 10 == y) System.out.println("at (10, 10) A is " + A + " lambda " + lambda + " b " + b);
                if (49 == x && 49 == y) System.out.println("at (49, 49) A is " + A + " lambda " + lambda + " b " + b);
            }
            //System.out.println();
        }
        dataColorizer.update();
        return true;
    }

    /**
     * Restores file name from Java Preferences.
     *
     * @return file name String
     */
    private String getFileFromPreferences() {
       Preferences prefs = Preferences.userNodeForPackage(this.getClass());
       return prefs.get(FILE_KEY, "");
    }

    /**
     * Saves the file name to Java Preferences.
     *
     * @param file
     */
    private void saveFileInPreferences(String file) {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put(FILE_KEY, file);
    }

    /**
     * This dialog shows the parameters from the .sdt file and allows user
     * to edit
     *
     * @return whether successful
     */
    private boolean showParamsDialog() {
        //TODO shouldn't UI be in separate class?
        GenericDialog dialog = new GenericDialog("Parameters");
        dialog.addNumericField("Image width: ",         m_width,     0, 8, "pixels");
        dialog.addNumericField("Image height: ",        m_height,    0, 8, "pixels");
        dialog.addNumericField("Time bins: ",           m_timeBins,  0, 8, "");
        dialog.addNumericField("Channel count: ",       m_channels,  0, 8, "");
        dialog.addNumericField("Time range: ",          m_timeRange, 0, 8, "nanoseconds");
        dialog.addNumericField("Starting wavelength: ", m_minWave,   0, 8, "nanometers");
        dialog.addNumericField("Channel width: ",       m_waveStep,  0, 8, "nanometers");
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }
        m_width     = (int) dialog.getNextNumber();
        m_height    = (int) dialog.getNextNumber();
        m_timeBins  = (int) dialog.getNextNumber();
        m_channels  = (int) dialog.getNextNumber();
        m_timeRange = (int) dialog.getNextNumber();
        m_minWave   = (int) dialog.getNextNumber();
        m_waveStep  = (int) dialog.getNextNumber();
        return true;
    }

    /**
     * Loads the data from the .sdt file.
     * Based on loci.slim.SlimData constructor.
     *
     * @return whether successful
     */
    private boolean loadData() {
        boolean success = false;
        try {
            byte[] plane = new byte[m_bpp * m_height * m_width];
            m_data = new int[m_channels][m_height][m_width][m_timeBins];
            int imageCount = m_reader.getImageCount();
            for (int i=0; i<imageCount; i++) {
                int[] zct = m_reader.getZCTCoords(i);
                if (zct[0] != 0 || zct[2] != 0) {
                    continue; // process only first Z and T
                }
                int[] sub = FormatTools.rasterToPosition(m_cLengths, zct[1]);
                int c = m_spectraIndex < 0 ? 0 : sub[m_spectraIndex];
                int t = m_lifetimeIndex < 0 ? 0 : sub[m_lifetimeIndex];
                m_reader.openBytes(i, plane, 0, 0, m_width, m_height);
                for (int y=0; y<m_height; y++) {
                    for (int x=0; x<m_width; x++) {
                        int index = m_bpp * (y * m_width + x);
                        int val;
                        if (m_pixelType == FormatTools.FLOAT) {
                            val = (int) DataTools.bytesToFloat(plane, index, m_bpp, m_little);
                        }
                        else if (m_pixelType == FormatTools.DOUBLE) {
                            val = (int) DataTools.bytesToDouble(plane, index, m_bpp, m_little);
                        }
                        else if (!m_floating) {
                            val = DataTools.bytesToInt(plane, index, m_bpp, m_little);
                        }
                        else {
                            throw new FormatException("Unsupported pixel type: " +
                                FormatTools.getPixelTypeString(m_pixelType));
                        }
                        m_data[c][y][x][t] = val;
                    }
                }
            }
            m_reader.close();
            success = true;
        }
        catch (Exception e) {
        }
        return success;
    }

    /**
     * This routine sums all of the photon data and creates a grayscale
     * image for the data.
     *
     * @return whether successful
     */
    private boolean createGlobalGrayScale() {
        int[][] pixels = new int[m_width][m_height];

        int maxPixel = 0;
        for (int x = 0; x < m_width; ++x) {
            for (int y = 0; y < m_height; ++y) {
                pixels[x][y] = 0;
                for (int c = 0; c < m_channels; ++c) {
                    for (int b = 0; b < m_timeBins; ++b) {
                        //System.out.println("x " + x + " y " + y + " c " + c + " b " + b);
                        pixels[x][y] += m_data[c][y][x][b];
                    }
                }
                if (pixels[x][y] > maxPixel) {
                    maxPixel = pixels[x][y];
                }
            }
        }

        ImageProcessor imageProcessor = new ByteProcessor(m_width, m_height);
        ImagePlus imagePlus = new ImagePlus("Global GrayScale", imageProcessor);
        byte[] outPixels = (byte[]) imageProcessor.getPixels();
        for (int x = 0; x < m_width; ++x) {
            for (int y = 0; y < m_height; ++y) {
                // flip y axis to correspond with Slim Plotter image
                outPixels[y * m_width + x] = (byte) (pixels[x][m_height - y - 1] * 255 / maxPixel);
            }
        }
        imagePlus.show();
        m_grayscaleImageProcessor = imageProcessor; //TODO for now

        // hook up mouse listener
        ImageWindow imageWindow = imagePlus.getWindow();
        m_grayscaleCanvas = imageWindow.getCanvas();
        m_grayscaleCanvas.addMouseListener(this);
        return true;
    }

    /**
     * This dialog box collects settings relating to the fit.  Pressing OK
     * starts the fit, Cancel cancels the whole plugin.
     *
     * @return
     */
    private boolean showFitDialog() {
        NonBlockingGenericDialog dialog = new NonBlockingGenericDialog("Fit Type");
        dialog.addChoice(
            "Region",
            new String[] { "Sum all", "Sum each ROI", "Single pixel", "Each pixel" },
            "Each pixel");
        dialog.addChoice(
            "Algorithm",
            new String[] { "Jaolho", /*"Akutan",*/ "old Barber RLD", "old Barber LMA", "Markwardt", "Barber NR RLD", "Barber NR LMA", "SLIMCurve RLD", "SLIMCurve LMA" },
            "Jaolho");
        dialog.addChoice(
            "Function",
            new String[] { "Single Exponential", "Double Exponential", "Triple Exponential", "Stretched Exponential" },
            "Single Exponential");
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }
        m_region = FitRegion.class.getEnumConstants()[dialog.getNextChoiceIndex()];
        m_algorithm = FitAlgorithm.class.getEnumConstants()[dialog.getNextChoiceIndex()];
        m_function = FitFunction.class.getEnumConstants()[dialog.getNextChoiceIndex()];
        return true;
    }

    // The following was an attempt to drive the fitting process from the OK button on the
    // Fit Dialog.
/*    private class MyDialogListener implements DialogListener {
        public boolean dialogItemChanged(GenericDialog dialog, AWTEvent e) {
            if (dialog.equals(m_fitDialog)) {
                if (dialog.wasCanceled()) {
                    System.out.println("Dialgo canceled");
                    m_done = true;
                }
                else if (dialog.wasOKed()) {
                    System.out.println("Dialog okayed");
                    m_region = FitRegion.class.getEnumConstants()[dialog.getNextChoiceIndex()];
                    m_algorithm = FitAlgorithm.class.getEnumConstants()[dialog.getNextChoiceIndex()];
                    m_function = FitFunction.class.getEnumConstants()[dialog.getNextChoiceIndex()];

                    // ask for fit parameters
                    if (showFitParamsDialog()) {
                        System.out.println("ABOUT TO FIT");
                        fitData();
                        showFitDialog(); // probably not the best way to do this
                    }
                    else System.out.println("FIT PARAMS DIALOG CACNELLDE");
                }
                else System.out.println("NOT CANCELLED OR OKAYED<, SOMETHING ELSE");
            }
            return true;
        }
    }*///TODO unfortunately NonBlockingGenericDialog doesn't call this for OK or Cancel button!

    /**
     * This dialog box collects settings for the current fit.
     *
     * @return
     */
    private boolean showFitParamsDialog() {
        GenericDialog dialog = new GenericDialog("Fit Params");
        if (FitRegion.POINT == m_region) {
            dialog.addNumericField("X", m_x, 0);
            dialog.addNumericField("Y", m_y, 0);
        }
        switch (m_function) {
            /*
            case SINGLE_EXPONENTIAL:
                dialog.addNumericField("A", m_fitA1, 5);
                dialog.addCheckbox("Fix", m_fitA1fixed);
                dialog.addNumericField("" + LAMBDA, m_fitT1, 5);
                dialog.addCheckbox("Fix", m_fitT1fixed);
                dialog.addNumericField("C", m_fitC, 5);
                dialog.addCheckbox("Fix", m_fitCfixed);
                break;
            case DOUBLE_EXPONENTIAL:
                dialog.addNumericField("A1", m_fitA1, 5);
                dialog.addCheckbox("Fix", m_fitA1fixed);
                dialog.addNumericField("" + LAMBDA + "1", m_fitT1, 5);
                dialog.addCheckbox("Fix", m_fitT1fixed);
                dialog.addNumericField("A2", m_fitA2, 5);
                dialog.addCheckbox("Fix", m_fitA2fixed);
                dialog.addNumericField("" + LAMBDA + "2", m_fitT2, 5);
                dialog.addCheckbox("Fix", m_fitT2fixed);
                dialog.addNumericField("C", m_fitC, 5);
                dialog.addCheckbox("Fix", m_fitCfixed);
                break;
            case TRIPLE_EXPONENTIAL:
                dialog.addNumericField("A1", m_fitA1, 5);
                dialog.addCheckbox("Fix", m_fitA1fixed);
                dialog.addNumericField("" + LAMBDA + "1", m_fitT1, 5);
                dialog.addCheckbox("Fix", m_fitT1fixed);
                dialog.addNumericField("A2", m_fitA2, 5);
                dialog.addCheckbox("Fix", m_fitA2fixed);
                dialog.addNumericField("" + LAMBDA + "2", m_fitT2, 5);
                dialog.addCheckbox("Fix", m_fitT2fixed);
                dialog.addNumericField("A3", m_fitA3, 5);
                dialog.addCheckbox("Fix", m_fitA3fixed);
                dialog.addNumericField("" + LAMBDA + "3", m_fitT3, 5);
                dialog.addCheckbox("Fix", m_fitT3fixed);
                dialog.addNumericField("C", m_fitC, 5);
                dialog.addCheckbox("Fix", m_fitCfixed);
                break;
            case STRETCHED_EXPONENTIAL:
                break;
            */
            case SINGLE_EXPONENTIAL:
                dialog.addNumericField("A",
                        m_param[0], 5);
                dialog.addCheckbox("Fix",
                        !m_free[0]);
                dialog.addNumericField("" + LAMBDA,
                        m_param[1], 5);
                dialog.addCheckbox("Fix",
                        !m_free[1]);
                dialog.addNumericField("C",
                        m_param[2], 5);
                dialog.addCheckbox("Fix",
                        !m_free[2]);
                break;
            case DOUBLE_EXPONENTIAL:
                dialog.addNumericField("A" + SUB_1,
                        m_param[0], 5);
                dialog.addCheckbox("Fix",
                        !m_free[0]);
                dialog.addNumericField("" + LAMBDA + SUB_1,
                        m_param[1], 5);
                dialog.addCheckbox("Fix",
                        !m_free[1]);
                dialog.addNumericField("A" + SUB_2,
                        m_param[2], 5);
                dialog.addCheckbox("Fix",
                        !m_free[2]);
                dialog.addNumericField("" + LAMBDA + SUB_2,
                        m_param[3], 5);
                dialog.addCheckbox("Fix",
                        !m_free[3]);
                dialog.addNumericField("C",
                        m_param[4], 5);
                dialog.addCheckbox("Fix",
                        !m_free[4]);
                break;
            case TRIPLE_EXPONENTIAL:
                dialog.addNumericField("A" + SUB_1,
                        m_param[0], 5);
                dialog.addCheckbox("Fix",
                        !m_free[0]);
                dialog.addNumericField("" + LAMBDA + SUB_1,
                        m_param[1], 5);
                dialog.addCheckbox("Fix",
                        !m_free[1]);
                dialog.addNumericField("A" + SUB_2,
                        m_param[2], 5);
                dialog.addCheckbox("Fix",
                        !m_free[2]);
                dialog.addNumericField("" + LAMBDA + SUB_2,
                        m_param[3], 5);
                dialog.addCheckbox("Fix",
                        !m_free[3]);
                dialog.addNumericField("A" + SUB_3,
                        m_param[4], 5);
                dialog.addCheckbox("Fix",
                        !m_free[4]);
                dialog.addNumericField("" + LAMBDA + SUB_3,
                        m_param[5], 5);
                dialog.addCheckbox("Fix",
                        !m_free[5]);
                dialog.addNumericField("C",
                        m_param[6], 5);
                dialog.addCheckbox("Fix",
                        !m_free[6]);
                break;
            case STRETCHED_EXPONENTIAL:
                break;

        }
        if (0 == m_stopBin) {
            m_stopBin = m_timeBins - 1;
        }
        dialog.addNumericField("Start", m_startBin, 0, 2, "bins");
        dialog.addNumericField("Stop", m_stopBin, 0, 2, "bins");
        dialog.addNumericField("Start X", m_startX, 0, 2, "bins");
        dialog.addNumericField("Threshold", m_threshold, 0, 2, "photons");
        dialog.addNumericField("" + CHI + SQUARE + " Target", m_chiSqTarget, 0, 2, null);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }
        if (FitRegion.POINT == m_region) {
            m_x = (int) dialog.getNextNumber();
            m_y = (int) dialog.getNextNumber();
        }
        switch (m_function) {
            case SINGLE_EXPONENTIAL:
                m_param[0] = dialog.getNextNumber();
                m_free[0] = !dialog.getNextBoolean();
                m_param[1] = dialog.getNextNumber();
                m_free[1] = !dialog.getNextBoolean();
                m_param[2] = dialog.getNextNumber();
                m_free[2] = !dialog.getNextBoolean();
                break;
            case DOUBLE_EXPONENTIAL:
                m_param[0] = dialog.getNextNumber();
                m_free[0] = !dialog.getNextBoolean();
                m_param[1] = dialog.getNextNumber();
                m_free[1] = !dialog.getNextBoolean();
                m_param[2] = dialog.getNextNumber();
                m_free[2] = !dialog.getNextBoolean();
                m_param[3] = dialog.getNextNumber();
                m_free[3] = !dialog.getNextBoolean();
                m_param[4] = dialog.getNextNumber();
                m_free[4] = !dialog.getNextBoolean();
                break;
            case TRIPLE_EXPONENTIAL:
                m_param[0] = dialog.getNextNumber();
                m_free[0] = !dialog.getNextBoolean();
                m_param[1] = dialog.getNextNumber();
                m_free[1] = !dialog.getNextBoolean();
                m_param[2] = dialog.getNextNumber();
                m_free[2] = !dialog.getNextBoolean();
                m_param[3] = dialog.getNextNumber();
                m_free[3] = !dialog.getNextBoolean();
                m_param[4] = dialog.getNextNumber();
                m_free[4] = !dialog.getNextBoolean();
                m_param[5] = dialog.getNextNumber();
                m_free[5] = !dialog.getNextBoolean();
                m_param[6] = dialog.getNextNumber();
                m_free[6] = !dialog.getNextBoolean();
               break;
            case STRETCHED_EXPONENTIAL:
                break;
        }
        m_startBin = (int) dialog.getNextNumber();
        m_stopBin  = (int) dialog.getNextNumber();
        m_startX = (int) dialog.getNextNumber();
        m_threshold = (int) dialog.getNextNumber();
      //  m_chiSqTarget = (double) dialog.getNextNumber();
        return true;
    }

    /**
     * MouseListener handler for button press.
     * Ignored.
     *
     * @param e
     */
    public void mousePressed(MouseEvent e) {
    }
    
    /**
     * MouseListener handler for mouse exit.
     * Ignored.
     *
     * @param e
     */
    public void mouseExited(MouseEvent e) {
    }
    
    /**
     * MouseListener handler for button click.
     * Ignored.
     *
     * @param e
     */
    public void mouseClicked(MouseEvent e) {
    }
    
    /**
     * MouseListener handler for mouse enter.
     * Ignored.
     *
     * @param e
     */
    public void mouseEntered(MouseEvent e) {
    }


    /**
     * MouseListener handler for button release.
     * Clicking on a pixel triggers a fit.
     *
     * @param e
     */
    public void mouseReleased(MouseEvent e) {
        // just ignore clicks during a fit
        System.out.println("mouseReleased " + e);
        if (!m_fitInProgress) {
            System.out.println("fit " + e.getX() + " " + e.getY());
            fitPixel(e.getX(), e.getY());
        }
     }
    
    private void fitData() {
        System.out.println("FIT DATA");
        // only one fit at a time
        synchronized (m_synchFit) {
            // disable mouse click pixel fits
            m_fitInProgress = true;
            
            switch (m_region) {
                case SUMMED:
                    // sum all pixels
                    fitSummed();
                    break;
                case ROI:
                    // fit summed ROIs
                    fitROIs();
                    break;
                case POINT:
                    // fit single pixel
                    fitPixel(m_x, m_y);
                    break;
                case EACH:
                    // fit every pixel
                    fitEachPixel();
                    break;
            }
System.out.println("m_fitInProgress goes false");
            m_fitInProgress = false;
        }
    }

    /*
     * Sums all pixels and fits the result.
     */
    private void fitSummed() {
        double params[] = getParams();
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];
        
        // sum up all the photons
        curveFitData = new CurveFitData();
        curveFitData.setParams(params);
        yCount = new double[m_timeBins];
        for (int b = 0; b < m_timeBins; ++b) {
            yCount[b] = 0.0;
        }
        int photons = 0;
        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                for (int b = 0; b < m_timeBins; ++b) {
                    yCount[b] += m_data[m_channel][y][x][b];
                    photons += m_data[m_channel][y][x][b];
                }
            }
        }
        System.out.println("SUMMED photons " + photons);
        curveFitData.setYCount(yCount);
        yFitted = new double[m_timeBins];
        curveFitData.setYFitted(yFitted);
        curveFitDataList.add(curveFitData);

        // do the fit
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        doFit(dataArray);
        
        //TODO display results for summed?
        IJ.showMessage("Summed " + dataArray[0].getParams()[0] + " " + dataArray[0].getParams()[1] + " " + dataArray[0].getParams()[2]);

        showDecayGraph(dataArray);
        saveParams(dataArray);
    }

    /*
     * Sums and fits each ROI.
     */
    private void fitROIs() {
        double params[] = getParams();
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];
        
        int roiNumber = 0;
        for (Roi roi: getRois()) {
            ++roiNumber;
            curveFitData = new CurveFitData();
            curveFitData.setParams(params.clone());
            yCount = new double[m_timeBins];
            for (int b = 0; b < m_timeBins; ++b) {
                yCount[b] = 0.0;
            }
            Rectangle bounds = roi.getBounds();
            for (int x = 0; x < bounds.width; ++x) {
                for (int y = 0; y < bounds.height; ++y) {
                    if (roi.contains(bounds.x + x, bounds.y + y)) {
                        System.out.println("roi " + roiNumber + " x " + x + " Y " + y);
                        for (int b = 0; b < m_timeBins; ++b) {
                           yCount[b] += m_data[m_channel][y][x][b];
                        }
                    }
                }
            }
            curveFitData.setYCount(yCount);
            yFitted = new double[m_timeBins];
            curveFitData.setYFitted(yFitted);
            curveFitDataList.add(curveFitData);
        }
        
        // do the fit
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        doFit(dataArray);
        
        showDecayGraph(dataArray);
        
        // show colorized lifetimes
        ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
        ImagePlus imagePlus = new ImagePlus("Fitted Lifetimes", imageProcessor);
        int i = 0;
        for (Roi roi: getRois()) {
            IJ.showMessage("Roi " + i + " " + dataArray[i].getParams()[0] + " " + dataArray[i].getParams()[1] + " " + dataArray[i].getParams()[2]);
            double lifetime = dataArray[i++].getParams()[1];
            imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lifetime));

            Rectangle bounds = roi.getBounds();
            for (int x = 0; x < bounds.width; ++x) {
                for (int y = 0; y < bounds.height; ++y) {
                    if (roi.contains(bounds.x + x, bounds.y + y)) {
                        imageProcessor.drawPixel(bounds.x + x, bounds.y + y);
                    }
                }
            }
        }
        imagePlus.show();  
        
        saveParams(dataArray);
    }

    /*
     * Fits a given pixel.
     * 
     * @param x
     * @param y
     */
    private void fitPixel(int x, int y) {
        double params[] = getParams();
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];
        
        curveFitData = new CurveFitData();
        curveFitData.setParams(params);
        yCount = new double[m_timeBins];
        for (int b = 0; b < m_timeBins; ++b) {
            yCount[b] = m_data[m_channel][m_height - y - 1][x][b];
        }
        curveFitData.setYCount(yCount);
        yFitted = new double[m_timeBins];
        curveFitData.setYFitted(yFitted);
        curveFitDataList.add(curveFitData);
        
        // do the fit
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        doFit(dataArray);
        
        showDecayGraph(dataArray);
        
        //TODO display results for single point?
        IJ.showMessage("Point A " + dataArray[0].getParams()[0] + " " + LAMBDA + " " + dataArray[0].getParams()[1] + " b " + dataArray[0].getParams()[2]);
        
        saveParams(dataArray);
    }
 
    /*
     * Fits each and every pixel.  This is the most complicated fit.
     *
     * If a channel is visible it is fit first and drawn incrementally.
     *
     * Results of the fit go to VisAD for analysis.
     */
    private void fitEachPixel() {
        long start = System.nanoTime();
        
        double params[] = getParams();
        
        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ArrayList<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];

        // special handling for visible channel
        int visibleChannel = m_channel; //TODO somehow else; m_channel s/b 0
        if (-1 != visibleChannel) {
            // show colorized image
            DataColorizer dataColorizer = new DataColorizer(m_width, m_height, m_algorithm + " Fitted Lifetimes");

            ChunkyPixelEffectIterator pixelIterator = new ChunkyPixelEffectIterator(new ChunkyPixelTableImpl(), m_width, m_height);

            int pixelCount = 0;
            int pixelsToProcessCount = 0;
            while (pixelIterator.hasNext()) {
                ++pixelCount;
                IJ.showProgress(pixelCount, m_height * m_width);
                ChunkyPixel pixel = pixelIterator.next();
                if (wantFitted(pixel.getX(), pixel.getY())) {
                    curveFitData = new CurveFitData();
                    curveFitData.setParams(params.clone());
                    yCount = new double[m_timeBins];
                    for (int b = 0; b < m_timeBins; ++b) {
                        yCount[b] = m_data[visibleChannel][pixel.getY()][pixel.getX()][b];
                    }
                    curveFitData.setYCount(yCount);
                    yFitted = new double[m_timeBins];
                    curveFitData.setYFitted(yFitted);
                    curveFitDataList.add(curveFitData);
                    pixelList.add(pixel);


                    if (++pixelsToProcessCount >= PIXEL_COUNT) {
                        processPixels(dataColorizer, m_height, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
                        curveFitDataList.clear();
                        pixelList.clear();
                        pixelsToProcessCount = 0;
                    }
                }
            }
            if (0 < pixelsToProcessCount) {
                processPixels(dataColorizer, m_height, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
            }
        }
 
        // any channels remaining?
        if (-1 == visibleChannel || m_channels > 1) {
            // fit rest of channels in expeditious way
            Roi[] rois = getRois();
            // are there ROIs defined?
            if (0 < rois.length) {
                for (Roi roi: rois) {
                    // yes, use ROI bounding boxes to limit iteration
                    Rectangle bounds = roi.getBounds();
                    for (int x = 0; x < bounds.width; ++x) {
                        for (int y = 0; y < bounds.height; ++y) {
                            if (roi.contains(bounds.x + x, bounds.y + y)) {
                                for (int channel = 0; channel < m_channels; ++channel) {
                                    if (channel != visibleChannel) {
                                        if (wantFitted(bounds.x + x, bounds.y + y)) {
                                            curveFitData = new CurveFitData();
                                            curveFitData.setParams(params.clone());
                                            yCount = new double[m_timeBins];
                                            for (int b = 0; b < m_timeBins; ++b) {
                                                yCount[b] = m_data[channel][y][x][b];
                                            }
                                            curveFitData.setYCount(yCount);
                                            yFitted = new double[m_timeBins];
                                            curveFitData.setYFitted(yFitted);
                                            curveFitDataList.add(curveFitData);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                // no ROIs, loop over entire image
                for (int channel = 0; channel < m_channels; ++channel) {
                    if (channel != visibleChannel) {
                        for (int y = 0; y < m_height; ++y) {
                            for (int x = 0; x < m_width; ++x) {
                                if (aboveThreshold(x, y)) {
                                    curveFitData = new CurveFitData();
                                    curveFitData.setParams(params.clone());
                                    yCount = new double[m_timeBins];
                                    for (int b = 0; b < m_timeBins; ++b) {
                                        yCount[b] = m_data[channel][y][x][b];
                                    }
                                    curveFitData.setYCount(yCount);
                                    yFitted = new double[m_timeBins];
                                    curveFitData.setYFitted(yFitted);
                                    curveFitDataList.add(curveFitData);
                                }
                            }
                        }
                    }

                }
            }
        }     
        //TODO break the fit up into chunks to lower memory requirements
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
        doFit(dataArray);
        //TODO save results

        long elapsed = System.nanoTime() - start;
        System.out.println("nanoseconds " + elapsed);
    }

    /**
     * Processes (fits) a batch of pixels.
     *
     * @param dataColorizer automatically sets colorization range and updates colorized image
     * @param height passed in to fix a vertical orientation problem
     * @param data list of data corresponding to pixels to be fitted
     * @param pixels parallel list of rectangles with which to draw the fitted pixel
     */
    void processPixels(DataColorizer dataColorizer, int height, ICurveFitData data[], ChunkyPixel pixels[]) {
        doFit(data);
        //TODO save results

        // draw as you go; 'chunky' pixels get smaller as the overall fit progresses
        for (int i = 0; i < pixels.length; ++i) {
            ChunkyPixel pixel = pixels[i];
            double lifetime = data[i].getParams()[1];

            //TODO debugging:
            if (lifetime > 2 * m_param[1]) {
                System.out.println("BAD FIT??? x " + pixel.getX() + " y " + pixel.getY() + " fitted lifetime " + lifetime);
            }

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
                    if (wantFitted(x, y)) {
                        // (flip vertically)
                        dataColorizer.setData(firstTime, x, height - y - 1 , lifetime);
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
     * @param x
     * @param y
     * @return whether to include or ignore this pixel
     */
    boolean wantFitted(int x, int y) {
        return (aboveThreshold(x, y) & isInROIs(x, y));
    }

    /**
     * Checks whether a given pixel is above threshold photon count value.
     *
     * @param x
     * @param y
     * @return whether above threshold
     */
    boolean aboveThreshold(int x, int y) {
        //TODO should the threshold be the same for all channels?
        return (m_threshold <= m_grayscaleImageProcessor.getPixel(x, m_height - y - 1));
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
     * @param max
     * @param lifetime
     * @return
     */
    //TODO make consistent with fitEachPixel's DataColorizer
     private Color lifetimeColorMap(double max, double lifetime) {
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
     * Helper function for the fit.  Initializes params array.
     * 
     * @return initialized params array
     */
    private double[] getParams() {
        // build the params
        double params[] = null;
        switch (m_function) {
            case SINGLE_EXPONENTIAL:
                params = new double[3];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                break;
            case DOUBLE_EXPONENTIAL:
                params = new double[5];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                params[3] = m_param[3];
                params[4] = m_param[4];
                break;
            case TRIPLE_EXPONENTIAL:
                params = new double[7];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                params[3] = m_param[3];
                params[4] = m_param[4];
                params[5] = m_param[5];
                params[6] = m_param[6];
                break;
            case STRETCHED_EXPONENTIAL:
                System.out.println("NOT IMPLEMENTED YET");
                break;
        }
        return params;
    }
 
    /*
     * Helper function for the fit.  Does the actual fit.
     * 
     * @param dataArray array of data to fit
     */
    //TODO s/b a mechanism to add these curve fit libraries?
    private void doFit(ICurveFitData dataArray[]) {
        // do the fit
        ICurveFitter curveFitter = null;
        switch (m_algorithm) {
            case JAOLHO:
                curveFitter = new JaolhoCurveFitter();
                break;
           /* case AKUTAN:
                curveFitter = new AkutanCurveFitter();
                break; */
            case BARBER_RLD:
                curveFitter = new GrayCurveFitter(0);
                break;
            case BARBER_LMA:
                curveFitter = new GrayCurveFitter(1);
                break;
            case MARKWARDT:
                curveFitter = new MarkwardtCurveFitter();
                break;
            case BARBER2_RLD:
                curveFitter = new GrayNRCurveFitter(0);
                break;
            case BARBER2_LMA:
                curveFitter = new GrayNRCurveFitter(1);
                break;
            case SLIMCURVE_RLD:
                curveFitter = new SLIMCurveFitter(0);
                break;
            case SLIMCURVE_LMA:
                curveFitter = new SLIMCurveFitter(1);
                break;
        }
        curveFitter.setXInc(m_timeRange);
        curveFitter.setFree(m_free);
        int startBin = m_startBin + (256 * m_startX);
        curveFitter.fitData(dataArray, startBin, m_stopBin);        
    }

    /*
     * Helper function for the fit.  Shows the decay curve.
     * 
     * @param dataArray array of fitted data
     */
    private void showDecayGraph(ICurveFitData dataArray[]) {
        if (0 < dataArray.length) {
            //TODO need to be able to examine any fitted pixel; for now just show the last fitted pixel. // first!
            DecayGraph decayGraph = new DecayGraph(m_timeRange, m_startBin, m_stopBin, dataArray[0]); //dataArray.length - 1]);
            decayGraph.setStartStopListener(new MyListener());
                    JFrame window = new JFrame("SLIM");
                    JComponent component = decayGraph.getComponent();
                    window.getContentPane().add(component);
                    window.setSize(450, 450);
                    window.pack();
                    window.setVisible(true);
        }
        
    }

    /*
     * Helper function for the fit.  Saves params array.
     * 
     * @param dataArray array of fitted data
     */
    //TODO params are saved to the UI now
    private void saveParams(ICurveFitData dataArray[]) {
        double params[] = null;
        if (0 < dataArray.length) {
            params = dataArray[0].getParams();
            switch (m_function) {
                case SINGLE_EXPONENTIAL:
                    m_param[0] = params[0];
                    m_param[1] = params[1];
                    m_param[2] = params[2];
                    break;
                case DOUBLE_EXPONENTIAL:
                    m_param[0] = params[0];
                    m_param[1] = params[1];
                    m_param[2] = params[2];
                    m_param[3] = params[3];
                    m_param[4] = params[4];
                    break;
                case TRIPLE_EXPONENTIAL:
                    m_param[0] = params[0];
                    m_param[1] = params[1];
                    m_param[2] = params[2];
                    m_param[3] = params[3];
                    m_param[4] = params[4];
                    m_param[5] = params[5];
                    m_param[6] = params[6];
                    break;
                case STRETCHED_EXPONENTIAL:
                    System.out.println("Not implemented yet");
                    break;
            }
        }
    }
    
    /**
     * This routine does the fit, once all settings have
     * been specified.
     *
     * Note that fitting each pixel is a special case, which is
     * handled by fitEachPixel.  This routine therefore only
     * handles fitting a single set of data from a single pixel
     * or summed from all the pixels.  It can also sum the data
     * and fit by ROI.
     */
    private void fitDataXX() {
        if (m_region.EACH == m_region) {
            fitEachPixel();
            return;
        }

        // build the params
        double params[] = null;
         switch (m_function) {
            case SINGLE_EXPONENTIAL:
                params = new double[3];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                break;
            case DOUBLE_EXPONENTIAL:
                params = new double[5];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                params[3] = m_param[3];
                params[4] = m_param[4];
                break;
            case TRIPLE_EXPONENTIAL:
                params = new double[7];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                params[3] = m_param[3];
                params[4] = m_param[4];
                params[5] = m_param[5];
                params[6] = m_param[6];
                break;
            case STRETCHED_EXPONENTIAL:
                break;
         }

        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];
        switch (m_region) {
            case SUMMED:
                // sum up all the photons
                curveFitData = new CurveFitData();
                curveFitData.setParams(params);
                yCount = new double[m_timeBins];
                for (int b = 0; b < m_timeBins; ++b) {
                    yCount[b] = 0.0;
                }
                int photons = 0;
                for (int y = 0; y < m_height; ++y) {
                    for (int x = 0; x < m_width; ++x) {
                        for (int b = 0; b < m_timeBins; ++b) {
                            yCount[b] += m_data[0][y][x][b];
                            photons += m_data[0][y][x][b];
                        }
                    }
                }
                System.out.println("SUMMED photons " + photons);
                curveFitData.setYCount(yCount);
                yFitted = new double[m_timeBins];
                curveFitData.setYFitted(yFitted);
                curveFitDataList.add(curveFitData);
                break;
            case ROI:
                int roiNumber = 0;
                for (Roi roi: getRois()) {
                    ++roiNumber;
                    curveFitData = new CurveFitData();
                    curveFitData.setParams(params.clone());
                    yCount = new double[m_timeBins];
                    for (int b = 0; b < m_timeBins; ++b) {
                        yCount[b] = 0.0;
                    }
                    Rectangle bounds = roi.getBounds();
                    for (int x = 0; x < bounds.width; ++x) {
                        for (int y = 0; y < bounds.height; ++y) {
                            if (roi.contains(bounds.x + x, bounds.y + y)) {
                                System.out.println("roi " + roiNumber + " x " + x + " Y " + y);
                                for (int b = 0; b < m_timeBins; ++b) {
                                    yCount[b] += m_data[0][y][x][b];
                                }
                            }
                        }
                    }
                    curveFitData.setYCount(yCount);
                    yFitted = new double[m_timeBins];
                    curveFitData.setYFitted(yFitted);
                    curveFitDataList.add(curveFitData);
                }
                break;
            case POINT:
                curveFitData = new CurveFitData();
                curveFitData.setParams(params);
                yCount = new double[m_timeBins];
                for (int b = 0; b < m_timeBins; ++b) {
                    yCount[b] = m_data[0][m_height - m_y - 1][m_x][b];
                }
                curveFitData.setYCount(yCount);
                yFitted = new double[m_timeBins];
                curveFitData.setYFitted(yFitted);
                curveFitDataList.add(curveFitData);
                break;
            case EACH: //TODO this is handled in fitEachPixel below
                Roi[] rois = getRois();
                if (0 < rois.length) {
                    for (Roi roi: rois) {
                        Rectangle bounds = roi.getBounds();
                        for (int x = 0; x < bounds.width; ++x) {
                            for (int y = 0; y < bounds.height; ++y) {
                                if (roi.contains(bounds.x + x, bounds.y + y)) {
                                    curveFitData = new CurveFitData();
                                    curveFitData.setParams(params.clone()); //TODO if you don't clone here each pixel fit uses results of previous fit to start
                                    yCount = new double[m_timeBins];
                                    for (int b = 0; b < m_timeBins; ++b) {
                                        yCount[b] = m_data[0][y][x][b];
                                    }
                                    curveFitData.setYCount(yCount);
                                    yFitted = new double[m_timeBins];
                                    curveFitData.setYFitted(yFitted);
                                    curveFitDataList.add(curveFitData);
                                }
                            }
                        }
                    }
                }
                else {
                    for (int y = 0; y < m_height; ++y) {
                        for (int x = 0; x < m_width; ++x) {
                            curveFitData = new CurveFitData();
                            curveFitData.setParams(params.clone()); //TODO if you don't clone here each pixel fit uses results of previous fit to start
                            yCount = new double[m_timeBins];
                            for (int b = 0; b < m_timeBins; ++b) {
                                yCount[b] = m_data[0][y][x][b];
                            }
                            curveFitData.setYCount(yCount);
                            yFitted = new double[m_timeBins];
                            curveFitData.setYFitted(yFitted);
                            curveFitDataList.add(curveFitData);
                        }
                    }
                }
                break;
        }
        ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);

        // do the fit
        ICurveFitter curveFitter = null;
        switch (m_algorithm) {
            case JAOLHO:
                curveFitter = new JaolhoCurveFitter();
                break;
           /* case AKUTAN:
                curveFitter = new AkutanCurveFitter();
                break; */
            case BARBER_RLD:
                curveFitter = new GrayCurveFitter(0);
                break;
            case BARBER_LMA:
                curveFitter = new GrayCurveFitter(1);
                break;
            case MARKWARDT:
                curveFitter = new MarkwardtCurveFitter();
                break;
            case BARBER2_RLD:
                curveFitter = new GrayNRCurveFitter(0);
                break;
            case BARBER2_LMA:
                curveFitter = new GrayNRCurveFitter(1);
                break;
            case SLIMCURVE_RLD:
                curveFitter = new SLIMCurveFitter(0);
                break;
            case SLIMCURVE_LMA:
                curveFitter = new SLIMCurveFitter(1);
                break;
        }
        curveFitter.setXInc(m_timeRange);
        curveFitter.setFree(m_free);
        int startBin = m_startBin + (256 * m_startX);
        curveFitter.fitData(dataArray, startBin, m_stopBin);

        if (0 < dataArray.length) {
            //TODO need to be able to examine any fitted pixel; for now just show the last fitted pixel. // first!
            DecayGraph decayGraph = new DecayGraph(curveFitter.getXInc(), m_startBin, m_stopBin, dataArray[0]); //dataArray.length - 1]);
            decayGraph.setStartStopListener(new MyListener());
                    JFrame window = new JFrame("SLIM");
                    JComponent component = decayGraph.getComponent();
                    window.getContentPane().add(component);
                    window.setSize(450, 450);
                    window.pack();
                    window.setVisible(true);
        }

        switch (m_region) {
            case SUMMED:
                //TODO display results for summed?
                IJ.showMessage("Summed " + dataArray[0].getParams()[0] + " " + dataArray[0].getParams()[1] + " " + dataArray[0].getParams()[2]);
                break;
            case ROI: {
                // show colorized lifetimes
                ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
                ImagePlus imagePlus = new ImagePlus("Fitted Lifetimes", imageProcessor);

                int i = 0;
                for (Roi roi: getRois()) {
                    IJ.showMessage("Roi " + i + " " + dataArray[i].getParams()[0] + " " + dataArray[i].getParams()[1] + " " + dataArray[i].getParams()[2]);
                    double lifetime = dataArray[i++].getParams()[1];
                    imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lifetime));

                    Rectangle bounds = roi.getBounds();
                    for (int x = 0; x < bounds.width; ++x) {
                        for (int y = 0; y < bounds.height; ++y) {
                            if (roi.contains(bounds.x + x, bounds.y + y)) {
                                imageProcessor.drawPixel(bounds.x + x, bounds.y + y);
                            }
                        }
                    }
                }
                imagePlus.show();
                break; }
            case POINT:
                //TODO display results for single point?
                IJ.showMessage("Point A " + dataArray[0].getParams()[0] + " " + LAMBDA + " " + dataArray[0].getParams()[1] + " b " + dataArray[0].getParams()[2]);
                break;
            case EACH: //TODO DEFUNCT CODE this is handled in fitEachPixel below
                //TODO new version uses "chunky pixel effect" drawing.
                // show colorized lifetimes
                ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
                ImagePlus imagePlus = new ImagePlus("Fitted Lambdas", imageProcessor);

                int i = 0;
                Roi[] rois = getRois();
                if (0 < rois.length) {
                    for (Roi roi: rois) {
                        Rectangle bounds = roi.getBounds();
                        for (int x = 0; x < bounds.width; ++x) {
                            for (int y = 0; y < bounds.height; ++y) {
                                if (roi.contains(bounds.x + x,  bounds.y + y)) {
                                    double lambda = dataArray[i++].getParams()[1];
                                    imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lambda));
                                    imageProcessor.drawPixel(bounds.x + x, m_height - bounds.y - y - 1);
                                }
                            }
                        }

                    }
                }
                else {
                    for (int y = 0; y < m_height; ++y) {
                        for (int x = 0; x < m_width; ++x) {
                            double lambda = dataArray[i++].getParams()[1];
                            imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lambda));
                            imageProcessor.drawPixel(x, m_height - y - 1);
                        }
                    }
                }
                imagePlus.show();
                break;
        }
        
        if (0 < dataArray.length) {
            params = dataArray[0].getParams();
            switch (m_function) {
                case SINGLE_EXPONENTIAL:
                    m_param[0] = params[0];
                    m_param[1] = params[1];
                    m_param[2]  = params[2];
                    break;
                case DOUBLE_EXPONENTIAL:
                    m_param[0] = params[0];
                    m_param[1] = params[1];
                    m_param[2] = params[2];
                    m_param[3] = params[3];
                    m_param[4] = params[4];
                    break;
                case TRIPLE_EXPONENTIAL:
                    m_param[0] = params[0];
                    m_param[1] = params[1];
                    m_param[2] = params[2];
                    m_param[3] = params[3];
                    m_param[4] = params[4];
                    m_param[5] = params[5];
                    m_param[6] = params[6];
                    break;
                case STRETCHED_EXPONENTIAL:
                    break;
            }
        }
    }

    /**
     * Fits each & every pixel in the image that is
     * selected by fitThisPixel.
     *
     * Starts up a DataColorizer to show dynamically colorized
     * lifetimes.
     *
     * Uses a ChunkyPixelEffectIterator to decide which pixel to
     * fit next.  Batches up pixels and calls processPixels to
     * do the fit.
     */
    private void fitEachPixelX() {
        long start = System.nanoTime();

        // build the params
        double params[] = null;
         switch (m_function) {
            case SINGLE_EXPONENTIAL:
                params = new double[3];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                break;
            case DOUBLE_EXPONENTIAL:
                params = new double[5];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                params[3] = m_param[3];
                params[4] = m_param[4];
                break;
            case TRIPLE_EXPONENTIAL:
                params = new double[7];
                params[0] = m_param[0];
                params[1] = m_param[1];
                params[2] = m_param[2];
                params[3] = m_param[3];
                params[4] = m_param[4];
                params[5] = m_param[5];
                params[6] = m_param[6];
                break;
            case STRETCHED_EXPONENTIAL:
                break;
        }

        // show colorized lifetimes
        //ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
        //ImagePlus imagePlus = new ImagePlus("Fitted Lifetimes", imageProcessor);
        //imagePlus.show();
        DataColorizer dataColorizer = new DataColorizer(m_width, m_height, m_algorithm + " Fitted Lifetimes");

        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ArrayList<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();
        ICurveFitData curveFitData;
        double yCount[];
        double yFitted[];

        ChunkyPixelEffectIterator pixelIterator = new ChunkyPixelEffectIterator(new ChunkyPixelTableImpl(), m_width, m_height);

        int pixelCount = 0;
        int pixelsToProcessCount = 0;

        while (pixelIterator.hasNext()) {
            ++pixelCount;
            IJ.showProgress(pixelCount, m_height * m_width);
            ChunkyPixel pixel = pixelIterator.next();
            if (wantFitted(pixel.getX(), pixel.getY())) {
                curveFitData = new CurveFitData();
                curveFitData.setParams(params.clone());
                yCount = new double[m_timeBins];
                for (int b = 0; b < m_timeBins; ++b) {
                    yCount[b] = m_data[0][pixel.getY()][pixel.getX()][b];
                }
                curveFitData.setYCount(yCount);
                yFitted = new double[m_timeBins];
                curveFitData.setYFitted(yFitted);
                curveFitDataList.add(curveFitData);
                pixelList.add(pixel);


                if (++pixelsToProcessCount >= PIXEL_COUNT) {
                    //processPixels(imagePlus, imageProcessor, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
                    processPixels(dataColorizer, m_height, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
                    curveFitDataList.clear();
                    pixelList.clear();
                    pixelsToProcessCount = 0;
                }
            }
        }
        if (0 < pixelsToProcessCount) {
            //processPixels(imagePlus, imageProcessor, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
            processPixels(dataColorizer, m_height, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
        }
        long elapsed = System.nanoTime() - start;
        System.out.println("nanoseconds " + elapsed);
    }

    private class MyListener implements IStartStopListener {
     /**
     * Listens for changes to the start and stop indices of the fit.
     *
     * @param start index
     * @param stop index inclusive
     */
       public void setStartStop(int start, int stop) {
           System.out.println("start " + start + " stop " + stop);
       }
    }

   /* private class MyDialogListener implements DialogListener {
        public boolean dialogItemChanged(GenericDialog dialog, AWTEvent e) {
            boolean showNextDialog = false;
            System.out.println("dialogItemChanged");
            if (null == e) {
                showNextDialog = true;
            }
            else if (e instanceof ActionEvent) {
                System.out.println("!>" + ((ActionEvent) e).getActionCommand() + "<");
                System.out.println("!OK button pressed");
                showNextDialog = true;
            }
            else {
                System.out.println("!Event " + e);
                System.out.println(dialog.getChoices().get(0));
            }
            return true;
        }
    }*/
}
