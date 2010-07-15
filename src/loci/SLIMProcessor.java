/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 * Portions of this code derived from SlimData.java.
 * TODO copyright? license?
 */

package loci;

import ij.*;
import ij.gui.*;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.*;


import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.prefs.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import loci.common.DataTools;
import loci.curvefitter.*;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;

/**
 *
 * @author aivar TODO acknowledge sources
 */
public class SLIMProcessor {
    // this affects how lambdas are colorized:
    private static final double MAXIMUM_LAMBDA = 0.075;

    //TODO total kludge; just to get started
    private boolean m_fakeData = false;

    private static final String FILE_KEY = "file";
    private String m_file;

    IFormatReader m_reader;

    // Actual data values, dimensioned [channel][row][column][bin]
    protected int[][][][] m_data;

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
    private int m_minWave, m_waveStep, m_maxWave;

    // fit parameters
    private int m_numExp;
    private int m_binRadius;
    private int m_cutBins;
    private int m_maxPeak;


    public enum FitRegion {
        SUMMED, ROI, POINT, EACH
    }

    public enum FitAlgorithm { //TODO not really algorithm, usu. LMA
       JAOLHO, AKUTAN, BARBER
    }

    public enum FitFunction {
        SINGLE_EXPONENTIAL, DOUBLE_EXPONENTIAL, TRIPLE_EXPONENTIAL, STRETCHED_EXPONENTIAL
    }

    private FitRegion m_region;
    private FitAlgorithm m_algorithm;
    private FitFunction m_function;

    private int m_x;
    private int m_y;

    private double m_fitA1;
    private double m_fitT1;
    private double m_fitA2;
    private double m_fitT2;
    private double m_fitA3;
    private double m_fitT3;
    private double m_fitC;

    private boolean m_fitA1fixed;
    private boolean m_fitT1fixed;
    private boolean m_fitA2fixed;
    private boolean m_fitT2fixed;
    private boolean m_fitA3fixed;
    private boolean m_fitT3fixed;
    private boolean m_fitCfixed;

    private int m_startBin;
    private int m_stopBin;
    private int m_threshold;
    private float m_chiSqTarget;

    public void run(String arg) {
        // ask for which file to load
        if (showFileDialog(getFileFromPreferences())) {
            // load the file
            if (loadFile(m_file)) {
                if (m_fakeData) {
                    fakeData();
                }
                else {
                    saveFileInPreferences(m_file);
                }
                // show parameters from file
                if (showParamsDialog()) {
                    if (!m_fakeData) {
                        loadData();
                    }
                    // create a grayscale image from the data
                    createGlobalGrayScale();
                    while (true) {
                        // ask what kind of fit
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
            else {
                //TODO shouldn't UI be separate?
                IJ.error("File Error", "Unable to load file.");
            }
        }
    }

    private boolean showFileDialog(String defaultFile) {
        //TODO shouldn't UI be in separate class?
        GenericDialog dialog = new GenericDialog("Load Data");
        dialog.addStringField("File:", defaultFile, 24);
        dialog.addCheckbox("Fake data", m_fakeData);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }

        m_file = dialog.getNextString();
        m_fakeData = dialog.getNextBoolean();

        if (!m_fakeData) IJ.showMessage("file " + m_file);
        return true;
    }

    // based on loci.slim.SlimData constructor
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
            m_binRadius = 3;
            status = true;
        }
        catch (Exception e) {

        }
        return status;
    }

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
        ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
        ImagePlus imagePlus = new ImagePlus("Fake Data", imageProcessor);

        m_data = new int[m_channels][m_height][m_width][m_timeBins];
        for (int y = 0; y < m_height; ++y) {
            A = 1000.0 + y  * 1000.0;
            for (int x = 0; x < m_width; ++x) {
                double tmpX = x;
                lambda = 0.05 + x * 0.0005d; //0.0001 + x * .001; //0.5 + x * 0.01; // .002500 + x * .01;
                //System.out.println("lambda " + lambda + " color " + lambdaColorMap(MAXIMUM_LAMBDA, lambda));
                imageProcessor.setColor(lambdaColorMap(MAXIMUM_LAMBDA, lambda));
                imageProcessor.drawPixel(x, y);
                for (int t = 0; t < m_timeBins; ++t) {
                    m_data[0][y][x][t] = (int)(A * Math.exp(-lambda * m_timeRange * t) + b);
                }
                //System.out.print(" " + m_data[0][y][x][0]);
                if (5 == x && 5 == y) System.out.println("at 5, 5 A is " + A + " lambda " + lambda + " b " + b);
                if (10 == x && 10 == y) System.out.println("at 5, 5 A is " + A + " lambda " + lambda + " b " + b);
            }
            System.out.println();
        }
        imagePlus.show();
        return true;
    }

    private String getFileFromPreferences() {
       Preferences prefs = Preferences.userNodeForPackage(this.getClass());
       return prefs.get(FILE_KEY, "");
    }

    private void saveFileInPreferences(String file) {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put(FILE_KEY, file);
    }

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
        return true;
    }

    // based on loci.slim.SlimData constructor
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
        return true;
    }

    private boolean showFitDialog() {
        GenericDialog dialog = new GenericDialog("Fit Type");
        dialog.addChoice(
            "Region",
            new String[] { "Sum all", "Sum each ROI", "Single pixel", "Each pixel" },
            "Summed");
        dialog.addChoice(
            "Algorithm",
            new String[] { "Jaolho", "Akutan", "Barber" },
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

    private boolean showFitParamsDialog() {
        GenericDialog dialog = new GenericDialog("Fit Params");
        if (FitRegion.POINT == m_region) {
            dialog.addNumericField("X", m_x, 0);
            dialog.addNumericField("Y", m_y, 0);
        }
        switch (m_function) {
            case SINGLE_EXPONENTIAL:
                dialog.addNumericField("A", m_fitA1, 5);
                dialog.addCheckbox("Fix", m_fitA1fixed);
                dialog.addNumericField("T", m_fitT1, 5);
                dialog.addCheckbox("Fix", m_fitT1fixed);
                dialog.addNumericField("C", m_fitC, 5);
                dialog.addCheckbox("Fix", m_fitCfixed);
                break;
            case DOUBLE_EXPONENTIAL:
                dialog.addNumericField("A1", m_fitA1, 5);
                dialog.addCheckbox("Fix", m_fitA1fixed);
                dialog.addNumericField("T1", m_fitT1, 5);
                dialog.addCheckbox("Fix", m_fitT1fixed);
                dialog.addNumericField("A2", m_fitA2, 5);
                dialog.addCheckbox("Fix", m_fitA2fixed);
                dialog.addNumericField("T2", m_fitT2, 5);
                dialog.addCheckbox("Fix", m_fitT2fixed);
                dialog.addNumericField("C", m_fitC, 5);
                dialog.addCheckbox("Fix", m_fitCfixed);
                break;
            case TRIPLE_EXPONENTIAL:
                dialog.addNumericField("A1", m_fitA1, 5);
                dialog.addCheckbox("Fix", m_fitA1fixed);
                dialog.addNumericField("T1", m_fitT1, 5);
                dialog.addCheckbox("Fix", m_fitT1fixed);
                dialog.addNumericField("A2", m_fitA2, 5);
                dialog.addCheckbox("Fix", m_fitA2fixed);
                dialog.addNumericField("T2", m_fitT2, 5);
                dialog.addCheckbox("Fix", m_fitT2fixed);
                dialog.addNumericField("A3", m_fitA3, 5);
                dialog.addCheckbox("Fix", m_fitA3fixed);
                dialog.addNumericField("T3", m_fitT3, 5);
                dialog.addCheckbox("Fix", m_fitT3fixed);
                dialog.addNumericField("C", m_fitC, 5);
                dialog.addCheckbox("Fix", m_fitCfixed);
                break;
            case STRETCHED_EXPONENTIAL:
                break;
        }
        if (0 == m_stopBin) {
            m_stopBin = m_timeBins - 1;
        }
        dialog.addNumericField("Start", m_startBin, 0, 2, "bins");
        dialog.addNumericField("Stop", m_stopBin, 0, 2, "bins");
        dialog.addNumericField("Threshold", m_threshold, 0, 2, "photons");
        dialog.addNumericField("Chi Square Targer", m_chiSqTarget, 0, 2, null);
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
                m_fitA1 = dialog.getNextNumber();
                m_fitA1fixed = dialog.getNextBoolean();
                m_fitT1 = dialog.getNextNumber();
                m_fitT1fixed = dialog.getNextBoolean();
                m_fitC = dialog.getNextNumber();
                m_fitCfixed = dialog.getNextBoolean();
                break;
            case DOUBLE_EXPONENTIAL:
                m_fitA1 = dialog.getNextNumber();
                m_fitA1fixed = dialog.getNextBoolean();
                m_fitT1 = dialog.getNextNumber();
                m_fitT1fixed = dialog.getNextBoolean();
                m_fitA2 = dialog.getNextNumber();
                m_fitA2fixed = dialog.getNextBoolean();
                m_fitT2 = dialog.getNextNumber();
                m_fitT2fixed = dialog.getNextBoolean();
                m_fitC = dialog.getNextNumber();
                m_fitCfixed = dialog.getNextBoolean();
                break;
            case TRIPLE_EXPONENTIAL:
                m_fitA1 = dialog.getNextNumber();
                m_fitA1fixed = dialog.getNextBoolean();
                m_fitT1 = dialog.getNextNumber();
                m_fitT1fixed = dialog.getNextBoolean();
                m_fitA2 = dialog.getNextNumber();
                m_fitA2fixed = dialog.getNextBoolean();
                m_fitT2 = dialog.getNextNumber();
                m_fitT2fixed = dialog.getNextBoolean();
                m_fitA3 = dialog.getNextNumber();
                m_fitA3fixed = dialog.getNextBoolean();
                m_fitT3 = dialog.getNextNumber();
                m_fitT3fixed = dialog.getNextBoolean();
                m_fitC = dialog.getNextNumber();
                m_fitCfixed = dialog.getNextBoolean();
               break;
            case STRETCHED_EXPONENTIAL:
                break;
        }
        m_startBin = (int) dialog.getNextNumber();
        m_stopBin  = (int) dialog.getNextNumber();
        return true;
    }

    private void fitData() {
        // build the params
        double params[] = null;
         switch (m_function) {
            case SINGLE_EXPONENTIAL:
                params = new double[3];
                params[0] = m_fitA1;
                params[1] = m_fitT1;
                params[2] = m_fitC;
                break;
            case DOUBLE_EXPONENTIAL:
                params = new double[5];
                params[0] = m_fitA1;
                params[1] = m_fitT1;
                params[2] = m_fitA2;
                params[3] = m_fitT2;
                params[4] = m_fitC;
                break;
            case TRIPLE_EXPONENTIAL:
                params = new double[7];
                params[0] = m_fitA1;
                params[1] = m_fitT1;
                params[2] = m_fitA2;
                params[3] = m_fitT2;
                params[4] = m_fitA3;
                params[5] = m_fitT3;
                params[6] = m_fitC;
                break;
            case STRETCHED_EXPONENTIAL:
                break;
        }
        //TODO problem: only use predetermined params for a fixed fit?
         for (int i = 0; i < params.length; ++i) {
             params[i] = 1.0;
         }

        // build the data
        ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
        ICurveFitData curveFitData;
        double yDataArray[];
        double yFitted[];
        switch (m_region) {
            case SUMMED:
                // sum up all the photons
                curveFitData = new CurveFitData();
                curveFitData.setParams(params);
                yDataArray = new double[m_timeBins];
                for (int b = 0; b < m_timeBins; ++b) {
                    yDataArray[b] = 0.0;
                }
                int photons = 0;
                for (int y = 0; y < m_height; ++y) {
                    for (int x = 0; x < m_width; ++x) {
                        for (int b = 0; b < m_timeBins; ++b) {
                            yDataArray[b] += m_data[0][y][x][b];
                            photons += m_data[0][y][x][b];
                        }
                    }
                }
                System.out.println("SUMMED photons " + photons);
                curveFitData.setYData(yDataArray);
                yFitted = new double[m_timeBins];
                curveFitData.setYFitted(yFitted);
                curveFitDataList.add(curveFitData);
                break;
            case ROI:
                for (Roi roi: getRois()) {
                    curveFitData = new CurveFitData();
                    curveFitData.setParams(params.clone());
                    yDataArray = new double[m_timeBins];
                    for (int b = 0; b < m_timeBins; ++b) {
                        yDataArray[b] = 0.0;
                    }
                    Rectangle bounds = roi.getBounds();
                    for (int x = 0; x < bounds.width; ++x) {
                        for (int y = 0; y < bounds.height; ++y) {
                            if (roi.contains(bounds.x + x, bounds.y + y)) {
                                for (int b = 0; b < m_timeBins; ++b) {
                                    yDataArray[b] += m_data[0][y][x][b];
                                }
                            }
                        }
                    }
                    curveFitData.setYData(yDataArray);
                    yFitted = new double[m_timeBins];
                    curveFitData.setYFitted(yFitted);
                    curveFitDataList.add(curveFitData);
                }
                break;
            case POINT:
                curveFitData = new CurveFitData();
                curveFitData.setParams(params);
                yDataArray = new double[m_timeBins];
                for (int b = 0; b < m_timeBins; ++b) {
                    yDataArray[b] = m_data[0][m_y][m_x][b];
                }
                curveFitData.setYData(yDataArray);
                yFitted = new double[m_timeBins];
                curveFitData.setYFitted(yFitted);
                curveFitDataList.add(curveFitData);
                break;
            case EACH:
                Roi[] rois = getRois();
                if (0 < rois.length) {
                    for (Roi roi: rois) {
                        Rectangle bounds = roi.getBounds();
                        for (int x = 0; x < bounds.width; ++x) {
                            for (int y = 0; y < bounds.height; ++y) {
                                if (roi.contains(bounds.x + x, bounds.y + y)) {
                                    curveFitData = new CurveFitData();
                                    curveFitData.setParams(params.clone()); //TODO if you don't clone here each pixel fit uses results of previous fit to start
                                    yDataArray = new double[m_timeBins];
                                    for (int b = 0; b < m_timeBins; ++b) {
                                        yDataArray[b] = m_data[0][y][x][b];
                                    }
                                    curveFitData.setYData(yDataArray);
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
                            yDataArray = new double[m_timeBins];
                            for (int b = 0; b < m_timeBins; ++b) {
                                yDataArray[b] = m_data[0][y][x][b];
                            }
                            curveFitData.setYData(yDataArray);
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
            case AKUTAN:
                curveFitter = new AkutanCurveFitter();
                break;
            case BARBER:
                curveFitter = new GrayCurveFitter();
                break;

        }
        curveFitter.setXInc(m_timeRange);
        curveFitter.fitData(dataArray, m_startBin, m_stopBin);

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
                ImagePlus imagePlus = new ImagePlus("Fitted Lambdas", imageProcessor);

                int i = 0;
                for (Roi roi: getRois()) {
                    IJ.showMessage("Roi " + i + " " + dataArray[i].getParams()[0] + " " + dataArray[i].getParams()[1] + " " + dataArray[i].getParams()[2]);
                    double lambda = dataArray[i++].getParams()[1];
                    imageProcessor.setColor(lambdaColorMap(MAXIMUM_LAMBDA, lambda));

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
                IJ.showMessage("Point " + dataArray[0].getParams()[0] + " " + dataArray[0].getParams()[1] + " " + dataArray[0].getParams()[2]);
                break;
            case EACH:
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
                                    imageProcessor.setColor(lambdaColorMap(MAXIMUM_LAMBDA, lambda));
                                    imageProcessor.drawPixel(bounds.x + x, bounds.y + y);
                                }
                            }
                        }

                    }
                }
                else {
                    for (int y = 0; y < m_height; ++y) {
                        for (int x = 0; x < m_width; ++x) {
                            double lambda = dataArray[i++].getParams()[1];
                            imageProcessor.setColor(lambdaColorMap(MAXIMUM_LAMBDA, lambda));
                            imageProcessor.drawPixel(x, y);
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
                    m_fitA1 = params[0];
                    m_fitT1 = params[1];
                    m_fitC  = params[2];
                    break;
                case DOUBLE_EXPONENTIAL:
                    m_fitA1 = params[0];
                    m_fitT1 = params[1];
                    m_fitA2 = params[2];
                    m_fitT2 = params[3];
                    m_fitC  = params[4];
                    break;
                case TRIPLE_EXPONENTIAL:
                    m_fitA1 = params[0];
                    m_fitT1 = params[1];
                    m_fitA2 = params[2];
                    m_fitT2 = params[3];
                    m_fitA3 = params[4];
                    m_fitT3 = params[5];
                    m_fitC  = params[6];
                    break;
                case STRETCHED_EXPONENTIAL:
                    break;
            }
        }
    }

    private Roi[] getRois() {
        Roi[] rois = {};
        RoiManager manager = RoiManager.getInstance();
        if (null != manager) {
            rois = manager.getRoisAsArray();
        }
        return rois;
    }
    
    private void showColorized(double max) {
    }

    private Color lambdaColorMap(double max, double tau) {
        Color returnColor = Color.BLACK;
        if (tau > 0.0) {
            if (tau < max/2.0) {
                returnColor = interpolateColor(Color.BLUE, Color.GREEN, 2.0 * tau / max);
            }
            else if (tau < max) {
                returnColor = interpolateColor(Color.GREEN, Color.RED, 2.0 * (tau - max / 2.0) / max);
            }
        }
        return returnColor;
    }

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

    private int interpolateColorComponent(int start, int end, double blend) {
        return (int)(blend * (end - start) + start);
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
