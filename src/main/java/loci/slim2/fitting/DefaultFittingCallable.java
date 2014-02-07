/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
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

package loci.slim2.fitting;

import loci.curvefitter.CurveFitData;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;

/**
 * This class brings together everything needed to fit one pixel.  It is a 
 * Callable, meant to be called from multiple threads.
 *
 * @author Aivar Grislis
 */
public class DefaultFittingCallable implements FittingCallable {
	private ICurveFitter curveFitter;
	private GlobalFitParams globalParams;
	private LocalFitParams localParams;
	private FitResults result;

	@Override
	public void setup(final ICurveFitter curveFitter,
			final GlobalFitParams globalParams,
			final LocalFitParams localParams) {
		this.curveFitter = curveFitter;
		this.globalParams = globalParams;
		this.localParams = localParams;
	}

	@Override
	public FitResults call() {
		int dataStart;
		int transientStop;
		int photonCount;
		double[] decay;

		curveFitter.setEstimator(globalParams.getEstimator());
		curveFitter.setFitAlgorithm(globalParams.getFitAlgorithm());
		curveFitter.setFitFunction(globalParams.getFitFunction());
		curveFitter.setNoiseModel(globalParams.getNoiseModel());
		curveFitter.setInstrumentResponse(globalParams.getPrompt());
		curveFitter.setXInc(globalParams.getXInc());
		curveFitter.setFree(globalParams.getFree());

		ICurveFitData curveFitData = new CurveFitData();
		curveFitData.setChiSquareTarget(globalParams.getChiSquareTarget());
		decay = localParams.getY();
		curveFitData.setYCount(decay);
		curveFitData.setTransStartIndex(globalParams.getTransientStart());
		dataStart = globalParams.getDataStart();
		curveFitData.setDataStartIndex(dataStart);
		transientStop = globalParams.getTransientStop();
		curveFitData.setTransEndIndex(transientStop);
		curveFitData.setSig(localParams.getSig());
		curveFitData.setParams(localParams.getParams().clone()); // params is overwritten
		curveFitData.setYFitted(localParams.getYFitted());

		ICurveFitData[] curveFitDataArray = new ICurveFitData[] { curveFitData }; //TODO refactor to non-array

		int returnValue = curveFitter.fitData(curveFitDataArray);

		result = new DefaultFitResults();
		result.setErrorCode(returnValue);
		result.setChiSquare(curveFitData.getChiSquare());
		result.setParams(curveFitData.getParams());
		result.setYFitted(curveFitData.getYFitted());
		photonCount = 0;
		for (int c = dataStart; c < transientStop; ++c) {
			photonCount += decay[c];
		}
		result.setPhotonCount(photonCount);
		result.setTransient(localParams.getY());
		result.setTransStart(globalParams.getTransientStart());
		result.setDataStart(globalParams.getDataStart());
		result.setTransStop(globalParams.getTransientStop());

		return result;
	}
}
