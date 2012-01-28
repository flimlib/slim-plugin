//
// FittingEngineCallable.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2011, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
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

package imagej.slim.fitting.callable;

import imagej.slim.fitting.params.FitResults;
import imagej.slim.fitting.params.ILocalFitParams;
import imagej.slim.fitting.params.IGlobalFitParams;
import imagej.slim.fitting.params.IFitResults;

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
        _curveFitter.setFitAlgorithm(_globalParams.getFitAlgorithm());
        _curveFitter.setFitFunction(_globalParams.getFitFunction());
        _curveFitter.setNoiseModel(_globalParams.getNoiseModel());
        _curveFitter.setInstrumentResponse(_globalParams.getPrompt());
        _curveFitter.setXInc(_globalParams.getXInc());
        _curveFitter.setFree(_globalParams.getFree());
       
        ICurveFitData curveFitData = new CurveFitData(); 
        curveFitData.setChiSquareTarget(_globalParams.getChiSquareTarget());
        curveFitData.setYCount(_localParams.getY());
        curveFitData.setSig(_localParams.getSig());
        curveFitData.setParams(_localParams.getParams().clone()); // params is overwritten
        curveFitData.setYFitted(_localParams.getYFitted());
        ICurveFitData[] curveFitDataArray = new ICurveFitData[] { curveFitData };

        _curveFitter.fitData(curveFitDataArray, _localParams.getFitStart(), _localParams.getFitStop());

        _result = new FitResults();
        _result.setChiSquare(curveFitData.getChiSquare());
        _result.setParams(curveFitData.getParams());
        _result.setYFitted(curveFitData.getYFitted());
 
        return _result;
    }   
}
