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

package loci.slim2.process.batch;

import ij.IJ;
import ij.gui.GenericDialog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.Preferences;

import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;
import loci.slim.analysis.batch.ui.BatchHistogramListener;
import loci.slim.fitted.FittedValue;
import loci.slim.fitted.FittedValueFactory;
import loci.slim2.analysis.batch.ExportHistogramsToText;
import loci.slim2.analysis.batch.ExportPixelsToText;
import loci.slim2.analysis.batch.ExportSummaryToText;
import loci.slim2.process.BatchProcessor;
import loci.slim2.process.FitSettings;
import loci.slim2.process.ImageFitter;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Context;

/**
 * Handles batch fitting of lifetime images.
 *
 * @author Aivar Grislis
 */
public class DefaultBatchProcessor implements BatchProcessor {

	private static final String CSV_SUFFIX = ".csv";
	private static final String TSV_SUFFIX = ".tsv";
	private static final String EXPORT_PIXELS_KEY = "exportpixels";
	private static final String PIXELS_FILE_KEY = "pixelsfile";
	private static final String EXPORT_HISTOS_KEY = "exporthistograms";
	private static final String HISTOS_FILE_KEY = "histogramsfile";
	private static final String EXPORT_SUMMARY_KEY = "exportsummary";
	private static final String SUMMARY_FILE_KEY = "summaryfile";
	private static final String CSV_KEY = "csv";
	private static final char COMMA_SEPARATOR = ',';
	private static final char TAB_SEPARATOR = '\t';
	private static final String BATCH_ERROR = "Error in Batch Processing";

	@Override
	public void process(final Context context, final int bins,
		final File[] files, final FitSettings fitSettings)
	{
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		final boolean defExportPixels = prefs.getBoolean(EXPORT_PIXELS_KEY, true);
		final String defPixelsFile = prefs.get(PIXELS_FILE_KEY, "pixels");
		final boolean defExportHistograms =
			prefs.getBoolean(EXPORT_HISTOS_KEY, true);
		final String defHistogramsFile = prefs.get(HISTOS_FILE_KEY, "histograms");
		final boolean defExportSummary = prefs.getBoolean(EXPORT_SUMMARY_KEY, true);
		final String defSummaryFile = prefs.get(SUMMARY_FILE_KEY, "summary");
		final boolean defCSV = prefs.getBoolean(CSV_KEY, false);

		// TODO - Consolidate this logic with same in SLIMProcessor!
		final GenericDialog dialog = new GenericDialog("Batch Processing");
		dialog.addCheckbox("Export_Pixels", defExportPixels);
		dialog.addStringField("Pixels_File", defPixelsFile);
		dialog.addCheckbox("Export_Histograms", defExportHistograms);
		dialog.addStringField("Histogram_File", defHistogramsFile);
		dialog.addCheckbox("Export_Summary_Histogram", defExportSummary);
		dialog.addStringField("Summary_File", defSummaryFile);
		dialog.addCheckbox("Comma_Separated", defCSV);
		dialog.showDialog();
		if (dialog.wasCanceled()) {
			return;
		}

		final boolean exportPixels = dialog.getNextBoolean();
		final String tmpPixelsFile = dialog.getNextString();
		final boolean exportHistograms = dialog.getNextBoolean();
		final String tmpHistogramsFile = dialog.getNextString();
		final boolean exportSummary = dialog.getNextBoolean();
		final String tmpSummaryFile = dialog.getNextString();
		final boolean csv = dialog.getNextBoolean();

		// make sure output file suffix is appropriate
		final String pixelsFile = checkSuffix(tmpPixelsFile, csv);
		final String histogramsFile = checkSuffix(tmpHistogramsFile, csv);
		final String summaryFile = checkSuffix(tmpSummaryFile, csv);

		prefs.putBoolean(EXPORT_PIXELS_KEY, exportPixels);
		prefs.put(PIXELS_FILE_KEY, pixelsFile);
		prefs.putBoolean(EXPORT_HISTOS_KEY, exportHistograms);
		prefs.put(HISTOS_FILE_KEY, histogramsFile);
		prefs.putBoolean(EXPORT_SUMMARY_KEY, exportSummary);
		prefs.put(SUMMARY_FILE_KEY, summaryFile);

		batchProcessing(context, bins, exportPixels, pixelsFile, exportHistograms,
			histogramsFile, exportSummary, summaryFile, fitSettings, files, csv);
	}

	/**
	 * Use appropriate file name suffix for comma- and tab-separated values.
	 */
	private String checkSuffix(String file, final boolean csv) {
		final String suffix = csv ? CSV_SUFFIX : TSV_SUFFIX;
		final String otherSuffix = csv ? TSV_SUFFIX : CSV_SUFFIX;
		if (!file.endsWith(suffix)) {
			if (file.endsWith(otherSuffix)) {
				final int i = file.indexOf(otherSuffix);
				file = file.substring(0, i);
			}
			file += suffix;
		}
		return file;
	}

	/**
	 * Does the batch processing.
	 *
	 * @param context
	 * @param exportPixels
	 * @param pixelsFile
	 * @param exportHistograms
	 * @param histogramsFile
	 * @param exportSummary
	 * @param summaryFile
	 * @param fitSettings
	 * @param files
	 * @param csv
	 */
	private void batchProcessing(final Context context, final int batchBins,
		final boolean exportPixels, final String pixelsFile,
		final boolean exportHistograms, final String histogramsFile,
		final boolean exportSummary, final String summaryFile,
		final FitSettings fitSettings, final File[] files, final boolean csv)
	{
		ExportPixelsToText pixels = null;
		ExportHistogramsToText histograms = null;
		ExportSummaryToText summary = null;

		final String fittedImages = fitSettings.getFittedImages();
		final FitFunction fitFunction =
			fitSettings.getGlobalFitParams().getFitFunction();
		final FitRegion fitRegion = FitRegion.EACH;

		// validate file names
		if (exportPixels) {
			if (!checkFileName(pixelsFile)) {
				return;
			}
			pixels = new ExportPixelsToText();
		}
		if (exportHistograms) {
			if (!checkFileName(histogramsFile)) {
				return;
			}
			histograms = new ExportHistogramsToText();
		}
		if (exportSummary) {
			if (!checkFileName(summaryFile)) {
				return;
			}
			summary = new ExportSummaryToText();
			final BatchHistogramListener listener = new BatchHistogramListener() {

				@Override
				public void swapImage(final String filePath) {
					// TODO ARG
					// this all pertainsi to IJ1 version:
					// in IJ1 version the current image c/b swapped merely by
					// changing a few globals, rebuilding grayscale, etc.
					// Since IJ2 is more properly top down m/n/b able to do this.
					System.out.println("swapImage to " + filePath);
					/*
					// load image
					_image = loadImage(filePath);

					// get metadata
					getImageInfo(_image);

					// save new path and file names
					int index = filePath.lastIndexOf(File.separator);
					_path = filePath.substring(0, index);
					_file = filePath.substring(index + 1);

					// turn off old threshold listener
					_uiPanel.setThresholdListener(null);

					// close existing grayscale image
					_grayScaleImage.close();
					_grayScaleImage = null;

					// show new grayscale and fit brightest
					showGrayScaleAndFit(_uiPanel);

					// set up new threshold listener
					_uiPanel.setThresholdListener(_grayScaleImage); */
				}
			};
			int components = 0;
			switch (fitFunction) {
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
					components = 1;
					break;
			}
			final FittedValue[] values =
				FittedValueFactory.createFittedValues(fittedImages, components);
			summary.init(fitFunction, values, listener);
		}

		try {
			char separator = TAB_SEPARATOR;
			if (csv) {
				separator = COMMA_SEPARATOR;
			}

			final ImageFitter imageFitter = new ImageFitter();

			for (int i = 0; i < files.length; ++i) {
				final File file = files[i];

				final ImgPlus<DoubleType> fittedImage =
					imageFitter.fit(context, fitSettings, file, batchBins);
				if (null == fittedImage) {
					final ImageFitter.ErrorCode errorCode = imageFitter.getErrorCode();
					String imageName = file.getCanonicalPath();
					imageName =
						imageName.substring(imageName.lastIndexOf(File.separatorChar) + 1);
					String message = null;

					switch (errorCode) {
						case IO_EXCEPTION:
							message = "IO Exception loading " + imageName + ".";
							break;
						case NO_LIFETIME_AXIS:
							message = "" + imageName + " has no lifetime dimension.";
							break;
						case BIN_COUNT_MISMATCH:
							final int bins = imageFitter.getBins();
							message =
								"Settings are for " + batchBins + " bins, " + imageName +
									" has " + bins + " bins.";
							break;
						default:
							message = "Unknown error";
							break;
					}

					if (null != message) {
						final boolean ok = IJ.showMessageWithCancel(BATCH_ERROR, message);
						if (!ok) {
							// Cancel cancels rest of batch; OK continues
							return;
						}
					}
				}
				else {
					if (exportPixels) {
						pixels.export(pixelsFile, true, fittedImage, fitRegion,
							fitFunction, fittedImages, separator);
					}
					if (exportHistograms) {
						histograms.export(histogramsFile, true, fittedImage, fitFunction,
							fittedImages, separator);
					}
					if (exportSummary) {
						summary.process(file.getCanonicalPath(), fittedImage);
					}
				}
			}

			if (exportSummary) {
				// export summary to text file
				summary.export(summaryFile, separator);
			}
		}
		catch (final Exception e) {
			IJ.handleException(e);
		}

		// TODO ARG need IJ2 version:
		// IJ.showProgress(0,0);
	}

	private boolean checkFileName(final String fileName) {
		try {
			// open and truncate
			final FileWriter fileWriter = new FileWriter(fileName, false);
			fileWriter.flush();
			fileWriter.close();
			return true;
		}
		catch (final IOException e) {
			IJ.showMessage("Error in Batch Processing", "Problem writing to file: " +
				fileName);
			return false;
		}
	}
}
