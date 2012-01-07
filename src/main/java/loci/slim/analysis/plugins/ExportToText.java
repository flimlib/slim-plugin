//
// ExportToText.java
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

package loci.slim.analysis.plugins;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.prefs.*;

import loci.slim.analysis.ISLIMAnalyzer;
import loci.slim.analysis.SLIMAnalyzer;
import imagej.slim.fitting.FitInfo.FitFunction;
import imagej.slim.fitting.FitInfo.FitRegion;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * Exports to text for further analysis of SLIMPlugin results.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/analysis/plugins/ExportToText.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/analysis/plugins/ExportToText.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
@SLIMAnalyzer(name="Export to Text")
public class ExportToText implements ISLIMAnalyzer {
    private static final String FILE_KEY = "export_results_to_text";
    private static final int X_INDEX = 0;
    private static final int Y_INDEX = 1;
    private static final int C_INDEX = 2;
    private static final int P_INDEX = 3;
    private static final char TAB = '\t';
    private static final char EOL = '\n';
    private FileWriter m_fileWriter = null;
    private MathContext m_context = new MathContext(4, RoundingMode.FLOOR);

    public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function) {
        String fileName = showFileDialog(getFileFromPreferences());
        if (null != fileName) {
            saveFileInPreferences(fileName);
            export(fileName, image, region, function);
        }
    }

    public static enum xFitRegion {
        SUMMED, ROI, POINT, EACH
    }

    public static enum xFitAlgorithm { //TODO not really algorithm, usu. LMA
       JAOLHO, /*AKUTAN,*/ BARBER_RLD, BARBER_LMA, MARKWARDT, BARBER2_RLD, BARBER2_LMA, SLIMCURVE_RLD, SLIMCURVE_LMA, SLIMCURVE_RLD_LMA
    }

    public static enum xFitFunction {
        SINGLE_EXPONENTIAL, DOUBLE_EXPONENTIAL, TRIPLE_EXPONENTIAL, STRETCHED_EXPONENTIAL
    }


    public void export(String fileName, Image<DoubleType> image, FitRegion region, FitFunction function) {
        // get list of current ROIs
        boolean hasRois = false;
        Roi[] rois = {};
        RoiManager manager = RoiManager.getInstance();
        if (null != manager) {
            hasRois = true;
            rois = manager.getRoisAsArray();
        }

        try {
            m_fileWriter = new FileWriter(fileName);
        } catch (IOException e) {
            IJ.log("exception opening file " + fileName);
            IJ.handleException(e);
        }

        if (null != m_fileWriter) {
            try {
                // look at image dimensions
                int dimensions[] = image.getDimensions();
                int width    = dimensions[X_INDEX];
                int height   = dimensions[Y_INDEX];
                int channels = dimensions[C_INDEX];
                int params   = dimensions[P_INDEX];

                // write headers
                if (channels > 2) { //TODO s/b 1; workaround for ImgLib bug -> always get 2 channels
                    writeChannelHeader();
                }
                switch (region) {
                    case SUMMED:
                    case POINT:
                        writeHeader(function);
                        break;
                    case ROI:
                        writeROIsHeader();
                        writeHeader(function);
                        break;
                    case EACH:
                        writeXYHeader();
                        writeHeader(function);
                        break;
                }

                // traverse the image
                final LocalizableByDimCursor<?> cursor = image.createLocalizableByDimCursor();
                int dimForCursor[] = new int[4];
                double paramArray[] = new double[params];

                // get the parameters for each pixel,
                for (int c = 0; c < channels; ++c) {
                    dimForCursor[C_INDEX] = c;

                    for (int y = 0; y < height; ++y) {
                        dimForCursor[Y_INDEX] = y;

                        for (int x = 0; x < width; ++x) {
                            dimForCursor[X_INDEX] = x;

                            // get the fitted parameters for c, y, x
                            for (int p = 0; p < params; ++p) {
                                dimForCursor[P_INDEX] = p;

                                // get the fitted parameter
                                cursor.moveTo(dimForCursor);
                                paramArray[p] = ((RealType) cursor.getType()).getRealFloat();
                            }

                            // if point has been fitted
                            //TODO distinguish between not fitted and error in fit!
                            if (!Double.isNaN(paramArray[0])) {
                                if (channels > 2) { //TODO see above; this is a hacky workaround for a bug; s/b " > 1"
                                    writeChannel(c + 1);
                                }

                                switch (region) {
                                    case SUMMED:
                                    case POINT:
                                        writeParams(function, paramArray);
                                        break;
                                    case ROI:
                                        writeROI(x + 1);
                                        writeParams(function, paramArray);
                                        break;
                                    case EACH:
                                        writeXY(x, y);
                                        writeParams(function, paramArray);
                                        break;
                                }
                            }

                        } // x loop

                    } // y loop

                } // c loop

                m_fileWriter.close();
            }
            catch (IOException e) {
                System.out.println("Error writing file " + e.getMessage());
                IJ.log("exception writing file");
                IJ.handleException(e);
            }
        }
    }

    private void writeChannelHeader() throws IOException {
        m_fileWriter.write("c\t");
    }

    private void writeChannel(int channel) throws IOException {
        m_fileWriter.write(channel + TAB);
    }

    private void writeHeader(FitFunction function) throws IOException {
        switch (function) {
            case SINGLE_EXPONENTIAL:
                m_fileWriter.write("A\tT\tZ\tX2\n");
                break;
            case DOUBLE_EXPONENTIAL:
                m_fileWriter.write("A1\tT1\rA2\tT2\tZ\tX2\n");
                break;
            case TRIPLE_EXPONENTIAL:
                m_fileWriter.write("A1\tT1\tA2\tT2\tA3\tT3\tZ\tX2\n");
                break;
            case STRETCHED_EXPONENTIAL:
                m_fileWriter.write("A\tT\tH\tZ\tX2\n");
                break;
        }
    }

    private void writeParams(FitFunction function, double[] paramArray) throws IOException {
        switch (function) {
            case SINGLE_EXPONENTIAL:
                m_fileWriter.write("" +
                        showParameter(paramArray[2]) + TAB +  // A
                        showParameter(paramArray[3]) + TAB +  // T
                        showParameter(paramArray[1]) + TAB +  // Z
                        showParameter(paramArray[0]) + EOL    // X2
                        );
                break;
            case DOUBLE_EXPONENTIAL:
                m_fileWriter.write("" +
                        showParameter(paramArray[2]) + TAB +  // A1
                        showParameter(paramArray[3]) + TAB +  // T1
                        showParameter(paramArray[4]) + TAB +  // A2
                        showParameter(paramArray[5]) + TAB +  // T2
                        showParameter(paramArray[1]) + TAB +  // Z
                        showParameter(paramArray[0]) + EOL    // X2
                        );
                break;
            case TRIPLE_EXPONENTIAL:
                m_fileWriter.write("" +
                        showParameter(paramArray[2]) + TAB +  // A1
                        showParameter(paramArray[3]) + TAB +  // T1
                        showParameter(paramArray[4]) + TAB +  // A2
                        showParameter(paramArray[5]) + TAB +  // T2
                        showParameter(paramArray[6]) + TAB +  // A3
                        showParameter(paramArray[7]) + TAB +  // T3
                        showParameter(paramArray[1]) + TAB +  // Z
                        showParameter(paramArray[0]) + EOL    // X2
                        );
                break;
            case STRETCHED_EXPONENTIAL:
                m_fileWriter.write("" +
                        showParameter(paramArray[2]) + TAB +  // A
                        showParameter(paramArray[3]) + TAB +  // T
                        showParameter(paramArray[4]) + TAB +  // H
                        showParameter(paramArray[1]) + TAB +  // Z
                        showParameter(paramArray[0]) + EOL    // X2
                        );
                break;
        }
    }

    private void writeROIsHeader() throws IOException {
        m_fileWriter.write("roi\t");
    }

    private void writeROI(int roi) throws IOException {
        m_fileWriter.write("" + roi + '\t');
    }

    private void writeXYHeader() throws IOException {
        m_fileWriter.write("x\ty\t");
    }

    private void writeXY(int x, int y) throws IOException {
        m_fileWriter.write("" + x + '\t' + y + '\t');
    }

    private String getFileFromPreferences() {
       Preferences prefs = Preferences.userNodeForPackage(this.getClass());
       return prefs.get(FILE_KEY, "");
    }

    private void saveFileInPreferences(String file) {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put(FILE_KEY, file);
    }

    private String showFileDialog(String defaultFile) {
        //TODO shouldn't UI be in separate class?
        GenericDialog dialog = new GenericDialog("Export Results to Text");
        dialog.addStringField("Save As:", defaultFile, 24);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return null;
        }

        return dialog.getNextString();
    }

    private String showParameter(double parameter) {
        return BigDecimal.valueOf(parameter).round(m_context).toEngineeringString();
    }
}
