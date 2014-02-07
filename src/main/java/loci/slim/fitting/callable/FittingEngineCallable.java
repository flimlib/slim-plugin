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

package loci.slim.fitting.callable;

import loci.slim.fitting.params.FitResults;
import loci.slim.fitting.params.ILocalFitParams;
import loci.slim.fitting.params.IGlobalFitParams;
import loci.slim.fitting.params.IFitResults;

import loci.curvefitter.CurveFitData;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;

/**
 * This class brings together everything needed to fit one pixel.  It is a 
 * Callable, meant to be called from multiple threads.
 *
 * @author Aivar Grislis
 */
public class FittingEngineCallable implements IFittingEngineCallable {
    private ICurveFitter _curveFitter;
    private IGlobalFitParams _globalParams;
    private ILocalFitParams _localParams;
    private IFitResults _result;
    
    @Override
    public void setup(final ICurveFitter curveFitter,
            final IGlobalFitParams globalParams,
            final ILocalFitParams localParams) {
        _curveFitter = curveFitter;
        _globalParams = globalParams;
        _localParams = localParams;
    }
 
    @Override
    public IFitResults call() {
        _curveFitter.setEstimator(_globalParams.getEstimator());
        _curveFitter.setFitAlgorithm(_globalParams.getFitAlgorithm());
        _curveFitter.setFitFunction(_globalParams.getFitFunction());
        _curveFitter.setNoiseModel(_globalParams.getNoiseModel());
        _curveFitter.setInstrumentResponse(_globalParams.getPrompt());
        _curveFitter.setXInc(_globalParams.getXInc());
        _curveFitter.setFree(_globalParams.getFree());
       
        ICurveFitData curveFitData = new CurveFitData(); 
        curveFitData.setChiSquareTarget(_globalParams.getChiSquareTarget());
        curveFitData.setYCount(_localParams.getY());
        curveFitData.setTransStartIndex(_globalParams.getTransientStart());
        curveFitData.setDataStartIndex(_globalParams.getDataStart());
        curveFitData.setTransEndIndex(_globalParams.getTransientStop());        
        curveFitData.setSig(_localParams.getSig());
        curveFitData.setParams(_localParams.getParams().clone()); // params is overwritten
        curveFitData.setYFitted(_localParams.getYFitted());
        
        ICurveFitData[] curveFitDataArray = new ICurveFitData[] { curveFitData };
		
        int returnValue = _curveFitter.fitData(curveFitDataArray);

        _result = new FitResults();
		_result.setParams(curveFitData.getParams());
		if (returnValue >= 0) {
			// success
			_result.setChiSquare(curveFitData.getChiSquare());
			_result.setYFitted(curveFitData.getYFitted());
		}
		else {
			// failed to fit
			_result.setChiSquare(0.0);
			_result.setYFitted(new double[] { });
		}
 
        return _result;
    }   
}
