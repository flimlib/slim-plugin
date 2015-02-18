/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
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
import loci.curvefitter.ICurveFitter.FitRegion;
import loci.slim.SLIMProcessor;
import loci.slim.analysis.SLIMAnalyzer;
import loci.slim.fitted.FittedValue;
import loci.slim.fitted.FittedValueFactory;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.plugin.Plugin;

/**
 * Exports pixel values as text for further analysis of SLIM Curve results.
 *
 * @author Aivar Grislis
 */
@Plugin(type = SLIMAnalyzer.class, name = "Export Pixels to Text")
public class ExportPixelsToText implements SLIMAnalyzer {

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
	private final MathContext context = new MathContext(4, RoundingMode.FLOOR);

	@Override
	public void analyze(final ImgPlus<DoubleType> image, final FitRegion region,
		final FitFunction function, final String parameters)
	{
		char separator = COMMA;
		if (!SLIMProcessor.macroParams.isAnalysisListUsed) {
			final boolean export =
				showFileDialog(getFileFromPreferences(), getAppendFromPreferences(),
					getCSVFromPreferences());
			if (export && null != fileName) {

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
				final String recordingCharString = Character.toString(separator);
				IJ.log(recordingCharString);
				SLIMProcessor.record(SLIMProcessor.SET_EXPORT_PIXEL_FILE_NAME_SLIM2,
					fileName, recordingCharString);
			}
		}
		else {
			fileName = SLIMProcessor.macroParams.exportPixelFileNameSingleFileSLIM2;
			separator =
				SLIMProcessor.macroParams.exportPixelFileNameSingleFileSeperatorSLIM2
					.charAt(0);
			IJ.log(Character.toString(separator));
			saveFileInPreferences(fileName);
			saveAppendInPreferences(append);
			saveCSVInPreferences(csv);

		}

		export(fileName, append, image, region, function, parameters, separator);

	}

	public void export(final String fileName, final boolean append,
		final ImgPlus<DoubleType> image, final FitRegion region,
		final FitFunction function, final String parameters, final char separator)
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
				// TODO fix stretched; how many components?
				break;
		}
		final FittedValue[] fittedValues =
			FittedValueFactory.createFittedValues(parameters, components);

		// get list of current ROIs
		boolean hasRois = false;
		Roi[] rois = {};
		final RoiManager manager = RoiManager.getInstance();
		if (null != manager) {
			hasRois = true;
			rois = manager.getRoisAsArray();
		}

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(fileName, append));
		}
		catch (final IOException e) {
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
				final long dimensions[] = new long[image.numDimensions()];
				image.dimensions(dimensions);
				final int width = (int) dimensions[X_INDEX];
				final int height = (int) dimensions[Y_INDEX];
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
				final int params = (int) dimensions[paramIndex];

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
				final int dimForCursor[] = new int[image.numDimensions()];
				final double paramArray[] = new double[params];

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
							// TODO distinguish between not fitted and error in fit!
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
			catch (final IOException e) {
				System.out.println("Error writing file " + e.getMessage());
				IJ.log("exception writing file");
				IJ.handleException(e);
			}
		}
	}

	private void writeChannelHeader(final char separator) throws IOException {
		bufferedWriter.write("c" + separator);
	}

	private void writeChannel(final int channel, final char separator)
		throws IOException
	{
		bufferedWriter.write("" + channel + separator);
	}

	private void writeHeader(final FittedValue[] fittedValues,
		final char separator) throws IOException
	{
		boolean firstTime = true;
		for (final FittedValue fittedValue : fittedValues) {
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

	private void writeParams(final double[] paramArray,
		final FittedValue[] fittedValues, final char separator) throws IOException
	{
		boolean firstTime = true;
		for (final FittedValue fittedValue : fittedValues) {
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

	private double normalize(final double A, final double sum) {
		return (100.0 * A) / sum;
	}

	private void writeROIsHeader(final char separator) throws IOException {
		bufferedWriter.write("roi" + separator);
	}

	private void writeROI(final int roi, final char separator) throws IOException
	{
		bufferedWriter.write("" + roi + separator);
	}

	private void writeXYHeader(final char separator) throws IOException {
		bufferedWriter.write("x" + separator + "y" + separator);
	}

	private void writeXY(final int x, final int y, final char separator)
		throws IOException
	{
		bufferedWriter.write("" + x + separator + y + separator);
	}

	private String getFileFromPreferences() {
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.get(FILE_KEY, fileName);
	}

	private void saveFileInPreferences(final String fileName) {
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.put(FILE_KEY, fileName);
	}

	private boolean getAppendFromPreferences() {
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.getBoolean(APPEND_KEY, append);
	}

	private void saveAppendInPreferences(final boolean append) {
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putBoolean(APPEND_KEY, append);
	}

	private boolean getCSVFromPreferences() {
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.getBoolean(CSV_KEY, csv);
	}

	private void saveCSVInPreferences(final boolean csv) {
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putBoolean(CSV_KEY, csv);
	}

	private boolean showFileDialog(final String defaultFile,
		final boolean defaultAppend, final boolean defaultCSV)
	{
		// TODO shouldn't UI be in separate class?
		final GenericDialog dialog = new GenericDialog("Export Pixels to Text");
		dialog.addStringField("Save_As", defaultFile, 24);
		dialog.addCheckbox("Append", defaultAppend);
		dialog.addCheckbox("Comma_Separated", defaultCSV);
		dialog.showDialog();
		if (dialog.wasCanceled()) {
			return false;
		}
		fileName = dialog.getNextString();
		append = dialog.getNextBoolean();
		csv = dialog.getNextBoolean();
		return true;
	}

	private String showParameter(final double parameter) {
		return BigDecimal.valueOf(parameter).round(context).toEngineeringString();
	}
}
