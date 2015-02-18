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

package loci.slim2.process;

import java.io.File;
import java.io.IOException;

import loci.curvefitter.ICurveFitter;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.JaolhoCurveFitter;
import loci.curvefitter.SLIMCurveFitter;
import loci.slim2.decay.LifetimeDatasetWrapper;
import loci.slim2.decay.NoLifetimeAxisFoundException;
import loci.slim2.fitting.DefaultLocalFitParams;
import loci.slim2.fitting.FitResults;
import loci.slim2.fitting.FittingEngine;
import loci.slim2.fitting.GlobalFitParams;
import loci.slim2.fitting.LocalFitParams;
import loci.slim2.fitting.ThreadedFittingEngine;
import loci.slim2.heuristics.DefaultFitterEstimator;
import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Context;

/**
 * Fits an entire image.  Useful for batch processing.
 * 
 * @author Aivar Grislis
 */
public class ImageFitter {
	public enum ErrorCode { NONE, IO_EXCEPTION, NO_LIFETIME_AXIS, BIN_COUNT_MISMATCH }
	private int IMPOSSIBLE_VALUE = -1;
	private int X_INDEX = 0;
	private int Y_INDEX = 1;
	private int PARAM_INDEX = 2;
	private ErrorCode errorCode;
	private int bins;
	private FittingEngine fittingEngine;

	/**
	 * Creates a fitted image.
	 * 
	 * @param context
	 * @param file
	 * @return fitted image or null; if null, errorCode is set
	 */
	public ImgPlus<DoubleType> fit(Context context, FitSettings fitSettings, File file) {
		return fit(context, fitSettings, file, IMPOSSIBLE_VALUE);
	}

	/**
	 * Creates a fitted image.  This version checks the bin count
	 * for consistency when batch processing.
	 * 
	 * @param context
	 * @param file
	 * @param batchBins
	 * @return fitted image or null; if null, errorCode is set
	 */
	public ImgPlus<DoubleType> fit(Context context, FitSettings fitSettings, File file, int batchBins) {
		errorCode = ErrorCode.NONE;

		// load the lifetime dataset
		LifetimeDatasetWrapper lifetime;
		try {
			lifetime = new LifetimeDatasetWrapper(context, file);
		}
		catch (IOException e) {
			errorCode = ErrorCode.IO_EXCEPTION;
			return null;
		}
		catch (NoLifetimeAxisFoundException e) {
			errorCode = ErrorCode.NO_LIFETIME_AXIS;
			return null;
		}

		// in order for fitting cursors to work must have same number bins
		if (IMPOSSIBLE_VALUE != batchBins) {
			bins = lifetime.getBins();
			if (batchBins != bins) {
				errorCode = ErrorCode.BIN_COUNT_MISMATCH;
				return null;
			}
		}
		GlobalFitParams params = fitSettings.getGlobalFitParams();

		// create output image
		long[] srcDims = lifetime.getDims();
		int parameterCount = getParameterCount(params.getFitFunction());
		long[] dstDims = new long[] { srcDims[X_INDEX], srcDims[Y_INDEX], parameterCount };
		ImgPlus<DoubleType> outputImage = new ImgPlus<DoubleType>(PlanarImgs.doubles(dstDims));
		outputImage.setName("outputImage"); //TODO ARG for now

		// fill image with NaNs
		Cursor<DoubleType> cursor = outputImage.cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.get().set(Double.NaN);
		}

		// set up fitting engine
		boolean[] free = new boolean[parameterCount];
		for (int i = 0; i < parameterCount; ++i) {
			free[i] = true;
		}
		FittingEngine fittingEngine = getFittingEngine(
				params.getFitAlgorithm(),
				params.getFitFunction(),
				params.getNoiseModel(),
				fitSettings.getTimeInc(),
				free);

		// do the fit
		int binSize = fitSettings.getBinningFactor();
		long[] dims = lifetime.getDims();
		long[] srcPosition = new long[dims.length];
		RandomAccess<DoubleType> randomAccess = outputImage.randomAccess();
		long[] dstPosition = new long[3];
		for (long y = 0; y < dims[Y_INDEX]; ++y) {
			for (long x = 0; x < dims[X_INDEX]; ++x) {
				srcPosition[X_INDEX] = x;
				srcPosition[Y_INDEX] = y;
				// other dimensional positions remain at zero

				dstPosition[X_INDEX] = x;
				dstPosition[Y_INDEX] = y;

				double[] decay = lifetime.getBinnedDecay(binSize, srcPosition);
				FitResults fitResults = fitDecay(fittingEngine, params, decay);
				for (int param = 0; param < parameterCount; ++param) {
					dstPosition[PARAM_INDEX] = param;
					randomAccess.setPosition(dstPosition);
					randomAccess.get().set(fitResults.getParams()[param]);
				}
			}
		}
		return outputImage;
	}

	/**
	 * Helper routine to do the fit.
	 * 
	 * @param params
	 * @param decay
	 * @return 
	 */
	private FitResults fitDecay(FittingEngine fittingEngine, GlobalFitParams params, double[] decay) {
		LocalFitParams data = new DefaultLocalFitParams();
		data.setY(decay);
		data.setSig(null);

		int paramCount = getParameterCount(params.getFitFunction());
		data.setParams(new double[paramCount]);
		double[] yFitted = new double[bins];
		data.setYFitted(yFitted);

		return fittingEngine.fit(params, data);
	}

	/**
	 * Gets the error code of the last execution.
	 * 
	 * @return 
	 */
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	/**
	 * Gets the bin count of the last execution.
	 * 
	 * @return 
	 */
	public int getBins() {
		return bins;
	}

	/**
	 * Helper routine to get and set up fitting engine.
	 * 
	 * @param fitAlgorithm
	 * @param fitFunction
	 * @param noiseModel
	 * @param timeInc
	 * @param free
	 * @return 
	 */
	public FittingEngine getFittingEngine(
				ICurveFitter.FitAlgorithm fitAlgorithm,
				ICurveFitter.FitFunction fitFunction,
				ICurveFitter.NoiseModel noiseModel,
				double timeInc, boolean[] free)
	{
		if (null == fittingEngine) {
			fittingEngine = new ThreadedFittingEngine();
		}
		ICurveFitter curveFitter = null;
		switch (fitAlgorithm) {
			case JAOLHO:
				curveFitter = new JaolhoCurveFitter();
				break;
			case SLIMCURVE_RLD:
				curveFitter = new SLIMCurveFitter();
				curveFitter.setFitAlgorithm(ICurveFitter.FitAlgorithm.SLIMCURVE_RLD);
				break;
			case SLIMCURVE_LMA:
				curveFitter = new SLIMCurveFitter();
				curveFitter.setFitAlgorithm(ICurveFitter.FitAlgorithm.SLIMCURVE_LMA);
				break;
			case SLIMCURVE_RLD_LMA:
				curveFitter = new SLIMCurveFitter();
				curveFitter.setFitAlgorithm(ICurveFitter.FitAlgorithm.SLIMCURVE_RLD_LMA);
				break;
		}
		curveFitter.setEstimator(new DefaultFitterEstimator());
		curveFitter.setFitFunction(fitFunction);
		curveFitter.setNoiseModel(noiseModel);
		curveFitter.setXInc(timeInc);
		curveFitter.setFree(free);
		//TODO ARG PROMPT get prompt working again:
		/* if (null != _excitationPanel) {
			double[] excitation = null;
			int startIndex = _fittingCursor.getPromptStartBin();
			int stopIndex  = _fittingCursor.getPromptStopBin();
			double base    = _fittingCursor.getPromptBaselineValue();
			excitation = _excitationPanel.getValues(startIndex, stopIndex, base);
			curveFitter.setInstrumentResponse(excitation);
		} */
		fittingEngine.setCurveFitter(curveFitter);
		return fittingEngine;
	}

	public int getParameterCount(FitFunction fitFunction) {
		int count = 0;
		switch (fitFunction) {
			case SINGLE_EXPONENTIAL:
				count = 4;
				break;
			case DOUBLE_EXPONENTIAL:
				count = 6;
				break;
			case TRIPLE_EXPONENTIAL:
				count = 8;
				break;
			case STRETCHED_EXPONENTIAL:
				count = 5;
				break;
		}
		return count;
	}
}
