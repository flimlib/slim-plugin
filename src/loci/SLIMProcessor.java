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
import ij.plugin.PlugIn;
import ij.process.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.prefs.*;

import loci.common.DataTools;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;

/**
 *
 * @author aivar
 */
public class SLIMProcessor {
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

    public void run(String arg) {
        if (showFileDialog(getFileFromPreferences())) {
            if (loadFile(m_file)) {
                saveFileInPreferences(m_file);
                if (showParamsDialog()) {
                    loadData();
                    createGlobalGrayScale();
                    createGlobalHistogram();
                    System.out.println(arg);
                }
            }
            else {
                //TODO shouldn't UI be separate?
                IJ.error("File Error", "Unable to load file.");
            }
        }
    }

    private String getFileFromPreferences() {
       Preferences prefs = Preferences.userNodeForPackage(this.getClass());
       return prefs.get(FILE_KEY, "");
    }

    private void saveFileInPreferences(String file) {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put(FILE_KEY, file);
    }

    private boolean showFileDialog(String defaultFile) {
        //TODO shouldn't UI be in separate class?
        GenericDialog dialog = new GenericDialog("Load Data");
        dialog.addStringField("File:", defaultFile, 24);
        dialog.addNumericField("Test:", 100, 0);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }

        m_file = dialog.getNextString();
        int test = (int) dialog.getNextNumber();

        IJ.showMessage("file " + m_file);

        IJ.write("file " + m_file);
        IJ.write("test " + test);

        //System.out.println("file " + m_file);
        //System.out.println("test " + test);
        return true;
    }

    // based on loci.slim.SlimData constructor
    private boolean loadFile(String file) {
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

    private static final int HISTOGRAM_HEIGHT = 256;
    private boolean createGlobalHistogram() {
        byte[][] histogram = new byte[m_timeBins][HISTOGRAM_HEIGHT];
        int[] sums = new int[m_timeBins];
        int maxSum = 0;

        for (int b = 0; b < m_timeBins; ++b) {
            for (int h = 0; h < HISTOGRAM_HEIGHT; ++h) {
                histogram[b][h] = (byte) 0xff;
            }
            sums[b] = 0;
            for (int x = 0; x < m_width; ++x) {
                for (int y = 0; y < m_height; ++y) {
                    for (int c = 0; c < m_channels; ++c) {
                        sums[b] += m_data[c][y][x][b];
                    }
                }
            }
            if (sums[b] > maxSum) {
                maxSum = sums[b];
            }
        }

        for (int b = 0; b < m_timeBins; ++b) {
            int h = (sums[b] * (HISTOGRAM_HEIGHT - 1)) / maxSum;
            histogram[b][HISTOGRAM_HEIGHT - h - 1] = 0;
        }

        ImageProcessor imageProcessor = new ByteProcessor(m_timeBins, HISTOGRAM_HEIGHT);
        ImagePlus imagePlus = new ImagePlus("Global Histogram", imageProcessor);
        byte[] outPixels = (byte[]) imageProcessor.getPixels();
        for (int b = 0; b < m_timeBins; ++b) {
            for (int h = 0; h < HISTOGRAM_HEIGHT; ++h) {
                outPixels[(m_timeBins * h) + b] = histogram[b][h];
            }
        }
        imagePlus.show();
        IJ.showMessage("global maximum count per bin is " + maxSum);
        dumpData(sums, m_timeBins);
        return true;
    }

    private void dumpData(int[] sums, int length) {
        String file = "/Users/aivar/global";
        File outFile = new File(file);
        PrintStream printStream = null;
        try {
            printStream = new PrintStream(new FileOutputStream(file));
        }
        catch (FileNotFoundException e) {
            IJ.showMessage("dumpData, file " + file + " not found");
        }
        for (int i = 0; i < length; ++i) {
            printStream.print(" " + sums[i] + ",\r\n");
        }
        printStream.close();
    }

}
