/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
