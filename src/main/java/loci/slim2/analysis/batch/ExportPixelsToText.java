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

package loci.slim2.analysis.batch;

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
import java.util.prefs.Preferences;

import loci.curvefitter.ICurveFitter.FitFunction;
import static loci.curvefitter.ICurveFitter.FitFunction.DOUBLE_EXPONENTIAL;
import static loci.curvefitter.ICurveFitter.FitFunction.SINGLE_EXPONENTIAL;
import static loci.curvefitter.ICurveFitter.FitFunction.STRETCHED_EXPONENTIAL;
import static loci.curvefitter.ICurveFitter.FitFunction.TRIPLE_EXPONENTIAL;
import loci.curvefitter.ICurveFitter.FitRegion;
import static loci.curvefitter.ICurveFitter.FitRegion.EACH;
import static loci.curvefitter.ICurveFitter.FitRegion.POINT;
import static loci.curvefitter.ICurveFitter.FitRegion.ROI;
import static loci.curvefitter.ICurveFitter.FitRegion.SUMMED;
import loci.slim.analysis.ISLIMAnalyzer;
import loci.slim.analysis.SLIMAnalyzer;
import loci.slim.fitted.FittedValue;
import loci.slim.fitted.FittedValueFactory;
import net.imglib2.RandomAccess;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;


/**
 * Exports pixel values as text for further analysis of SLIMPlugin results.
 *
 * @author Aivar Grislis
 */
@SLIMAnalyzer(name="Export Pixels to Text")
public class ExportPixelsToText implements ISLIMAnalyzer {
    private static final String FILE_KEY = "export_pixels_to_text/file";
	private static final String APPEND_KEY = "export_pixels_to_text/append";
	private static final String CSV_KEY = "export_pixels_to_text/csv";
    private static final int X_INDEX = 0;
    private static final int Y_INDEX = 1;
    private static final int C_INDEX = 2;
    private static final char TAB = '\t';
	private static final char COMMA = ',';
	private static final String TSV_SUFFIX = ".tsv";
	private static final String CSV_SUFFIX = ".csv";
	private String fileName;
	private boolean append;
	private boolean csv;
    private BufferedWriter bufferedWriter;
    private MathContext context = new MathContext(4, RoundingMode.FLOOR);

    public void analyze(ImgPlus<DoubleType> image, FitRegion region, FitFunction function, String parameters) {
        boolean export = showFileDialog(getFileFromPreferences(), getAppendFromPreferences(), getCSVFromPreferences());
        if (export && null != fileName) {
			char separator;
			if (csv) {
				separator = COMMA;
				if (!fileName.endsWith(CSV_SUFFIX)) {
					fileName += CSV_SUFFIX;
				}
			}
			else {
				separator = TAB;
				if (!fileName.endsWith(TSV_SUFFIX)) {
					fileName += TSV_SUFFIX;
				}
			}
            saveFileInPreferences(fileName);
			saveAppendInPreferences(append);
			saveCSVInPreferences(csv);
            export(fileName, append, image, region, function, parameters, separator);
        }
    }

    public void export(String fileName, boolean append, ImgPlus<DoubleType> image,
			FitRegion region, FitFunction function, String parameters, char separator)
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
				bufferedWriter.write("Export Pixels" + separator + image.getName());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				
                // look at image dimensions
                long dimensions[] = new long[image.numDimensions()];
                image.dimensions(dimensions);
                int width    = (int) dimensions[X_INDEX];
                int height   = (int) dimensions[Y_INDEX];
                int channels;
                int paramIndex;
				if (3 == image.numDimensions()) {
					channels = 1;
					paramIndex = 2;
				}
				else {
					channels = (int) dimensions[C_INDEX];
					paramIndex = 3;
				}
                int params   = (int) dimensions[paramIndex];

                // write headers
                if (channels > 1) {
                    writeChannelHeader(separator);
                }
                switch (region) {
                    case SUMMED:
                    case POINT:
                        writeHeader(fittedValues, separator);
                        break;
                    case ROI:
                        writeROIsHeader(separator);
                        writeHeader(fittedValues, separator);
                        break;
                    case EACH:
                        writeXYHeader(separator);
                        writeHeader(fittedValues, separator);
                        break;
                }

                // traverse the image
                final RandomAccess<?> cursor = image.randomAccess();
                int dimForCursor[] = new int[image.numDimensions()];
                double paramArray[] = new double[params];

                // get the parameters for each pixel,
                for (int c = 0; c < channels; ++c) {
					if (channels > 1) {
						dimForCursor[C_INDEX] = c;
					}

                    for (int y = 0; y < height; ++y) {
                        dimForCursor[Y_INDEX] = y;

                        for (int x = 0; x < width; ++x) {
                            dimForCursor[X_INDEX] = x;

                            // get the fitted parameters for c, y, x
                            for (int p = 0; p < params; ++p) {
                                dimForCursor[paramIndex] = p;

                                // get the fitted parameter
                                cursor.setPosition(dimForCursor);
                                paramArray[p] = ((RealType) cursor.get()).getRealFloat();
                            }

                            // if point has been fitted
                            //TODO distinguish between not fitted and error in fit!
                            if (!Double.isNaN(paramArray[0])) {
                                if (channels > 1) {
                                    writeChannel(c + 1, separator);
                                }

                                switch (region) {
                                    case SUMMED:
                                    case POINT:
                                        writeParams(paramArray, fittedValues, separator);
                                        break;
                                    case ROI:
                                        writeROI(x + 1, separator);
                                        writeParams(paramArray, fittedValues, separator);
                                        break;
                                    case EACH:
                                        writeXY(x, y, separator);
                                        writeParams(paramArray, fittedValues, separator);
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

    private void writeChannelHeader(char separator) throws IOException {
        bufferedWriter.write("c" + separator);
    }

    private void writeChannel(int channel, char separator) throws IOException {
        bufferedWriter.write("" + channel + separator);
    }

    private void writeHeader(FittedValue[] fittedValues, char separator) throws IOException {
		boolean firstTime = true;
		for (FittedValue fittedValue : fittedValues) {
			if (firstTime) {
				firstTime = false;
			}
			else {
				bufferedWriter.write(separator);
			}
			bufferedWriter.write(fittedValue.getTitle());
		}
		bufferedWriter.newLine();
    }

    private void writeParams(double[] paramArray, FittedValue[] fittedValues, char separator) throws IOException {
		boolean firstTime = true;
		for (FittedValue fittedValue : fittedValues) {
			if (firstTime) {
				firstTime = false;
			}
			else {
				bufferedWriter.write(separator);
			}
			bufferedWriter.write("" + fittedValue.getValue(paramArray));
		}
		bufferedWriter.newLine();
    }
	
	private double normalize(double A, double sum) {
		return (100.0 * A) / sum;
	}

    private void writeROIsHeader(char separator) throws IOException {
        bufferedWriter.write("roi" + separator);
    }

    private void writeROI(int roi, char separator) throws IOException {
        bufferedWriter.write("" + roi + separator);
    }

    private void writeXYHeader(char separator) throws IOException {
        bufferedWriter.write("x" + separator + "y" + separator);
    }

    private void writeXY(int x, int y, char separator) throws IOException {
        bufferedWriter.write("" + x + separator + y + separator);
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
	
	private boolean getCSVFromPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.getBoolean(CSV_KEY, csv);
	}
	
	private void saveCSVInPreferences(boolean csv) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putBoolean(CSV_KEY, csv);
	}

    private boolean showFileDialog(String defaultFile, boolean defaultAppend, boolean defaultCSV) {
        //TODO shouldn't UI be in separate class?
        GenericDialog dialog = new GenericDialog("Export Pixels to Text");
        dialog.addStringField("Save As:", defaultFile, 24);
		dialog.addCheckbox("Append", defaultAppend);
		dialog.addCheckbox("Comma Separated", defaultCSV);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }
		fileName = dialog.getNextString();
		append   = dialog.getNextBoolean();
		csv      = dialog.getNextBoolean();
		return true;
    }

    private String showParameter(double parameter) {
        return BigDecimal.valueOf(parameter).round(context).toEngineeringString();
    }
}
