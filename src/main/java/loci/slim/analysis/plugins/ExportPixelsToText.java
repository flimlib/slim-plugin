//
// ExportPixelsToText.java
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.prefs.*;

import loci.slim.analysis.ISLIMAnalyzer;
import loci.slim.analysis.SLIMAnalyzer;
import loci.slim.fitted.FittedValue;
import loci.slim.fitted.FittedValueFactory;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * Exports pixel values as text for further analysis of SLIMPlugin results.
 *
 * @author Aivar Grislis
 */
@SLIMAnalyzer(name="Export Pixels to Text")
public class ExportPixelsToText implements ISLIMAnalyzer {
    private static final String FILE_KEY = "export_pixels_to_text/file";
	private static final String APPEND_KEY = "export_pixels_to_text/append";
    private static final int X_INDEX = 0;
    private static final int Y_INDEX = 1;
    private static final int C_INDEX = 2;
    private static final int P_INDEX = 3;
    private static final String TAB = "\t";
	private String fileName;
	private boolean append;
    private BufferedWriter bufferedWriter;
    private MathContext context = new MathContext(4, RoundingMode.FLOOR);

    public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function, String parameters) {
        boolean export = showFileDialog(getFileFromPreferences(), getAppendFromPreferences());
        if (export && null != fileName) {
            saveFileInPreferences(fileName);
			saveAppendInPreferences(append);
            export(fileName, append, image, region, function, parameters);
        }
    }

    public void export(String fileName, boolean append, Image<DoubleType> image,
			FitRegion region, FitFunction function, String parameters)
	{
		int components = 0;
		switch (function) {
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
				//TODO fix stretched; how many components?
				break;
		}
		FittedValue[] fittedValues = FittedValueFactory.createFittedValues(parameters, components);
		
        // get list of current ROIs
        boolean hasRois = false;
        Roi[] rois = {};
        RoiManager manager = RoiManager.getInstance();
        if (null != manager) {
            hasRois = true;
            rois = manager.getRoisAsArray();
        }

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(fileName, append));
        } catch (IOException e) {
            IJ.log("exception opening file " + fileName);
            IJ.handleException(e);
        }

        if (null != bufferedWriter) {
            try {
				// title this export
				bufferedWriter.write("Export Pixels " + image.getName());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				
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
                        writeHeader(fittedValues);
                        break;
                    case ROI:
                        writeROIsHeader();
                        writeHeader(fittedValues);
                        break;
                    case EACH:
                        writeXYHeader();
                        writeHeader(fittedValues);
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
                                        writeParams(paramArray, fittedValues);
                                        break;
                                    case ROI:
                                        writeROI(x + 1);
                                        writeParams(paramArray, fittedValues);
                                        break;
                                    case EACH:
                                        writeXY(x, y);
                                        writeParams(paramArray, fittedValues);
                                        break;
                                }
                            }

                        } // x loop

                    } // y loop

                } // c loop

				bufferedWriter.newLine();
                bufferedWriter.close();
            }
            catch (IOException e) {
                System.out.println("Error writing file " + e.getMessage());
                IJ.log("exception writing file");
                IJ.handleException(e);
            }
        }
    }

    private void writeChannelHeader() throws IOException {
        bufferedWriter.write("c" + TAB);
    }

    private void writeChannel(int channel) throws IOException {
        bufferedWriter.write("" + channel + TAB);
    }

    private void writeHeader(FittedValue[] fittedValues) throws IOException {
		boolean firstTime = true;
		for (FittedValue fittedValue : fittedValues) {
			if (firstTime) {
				firstTime = false;
			}
			else {
				bufferedWriter.write(TAB);
			}
			bufferedWriter.write(fittedValue.getTitle());
		}
		bufferedWriter.newLine();
    }

    private void writeParams(double[] paramArray, FittedValue[] fittedValues) throws IOException {
		boolean firstTime = true;
		for (FittedValue fittedValue : fittedValues) {
			if (firstTime) {
				firstTime = false;
			}
			else {
				bufferedWriter.write(TAB);
			}
			bufferedWriter.write("" + fittedValue.getValue(paramArray));
		}
		bufferedWriter.newLine();
    }
	
	private double normalize(double A, double sum) {
		return (100.0 * A) / sum;
	}

    private void writeROIsHeader() throws IOException {
        bufferedWriter.write("roi" + TAB);
    }

    private void writeROI(int roi) throws IOException {
        bufferedWriter.write("" + roi + TAB);
    }

    private void writeXYHeader() throws IOException {
        bufferedWriter.write("x" + TAB + "y" + TAB);
    }

    private void writeXY(int x, int y) throws IOException {
        bufferedWriter.write("" + x + TAB + y + TAB);
    }

    private String getFileFromPreferences() {
       Preferences prefs = Preferences.userNodeForPackage(this.getClass());
       return prefs.get(FILE_KEY, fileName);
    }

    private void saveFileInPreferences(String fileName) {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put(FILE_KEY, fileName);
    }
	
	private boolean getAppendFromPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.getBoolean(APPEND_KEY, append);
	}
	
	private void saveAppendInPreferences(boolean append) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putBoolean(APPEND_KEY, append);
	}

    private boolean showFileDialog(String defaultFile, boolean defaultAppend) {
        //TODO shouldn't UI be in separate class?
        GenericDialog dialog = new GenericDialog("Export Pixels to Text");
        dialog.addStringField("Save As:", defaultFile, 24);
		dialog.addCheckbox("Append", defaultAppend);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }
		fileName = dialog.getNextString();
		append   = dialog.getNextBoolean();
		return true;
    }

    private String showParameter(double parameter) {
        return BigDecimal.valueOf(parameter).round(context).toEngineeringString();
    }
}
