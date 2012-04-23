//
// GlobalFitParams.java
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

package loci.slim.fitting.params;

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.curvefitter.IFitterEstimator;

/**
 * This contains the global fitting parameters, i.e. those valid for the whole
 * image.
 * 
 * @author Aivar Grislis
 */
public class GlobalFitParams implements IGlobalFitParams {
    private IFitterEstimator _estimator;
    private FitAlgorithm _fitAlgorithm;
    private FitFunction _fitFunction;
    private NoiseModel _noiseModel;
    private double _xInc;
    private double[] _prompt;
    private double _chiSquareTarget;
    private boolean[] _free;
    private int _startPrompt;
    private int _stopPrompt;
    private int _startDecay;
    private int _stopDecay;

    @Override
    public void setEstimator(IFitterEstimator estimator) {
        _estimator = estimator;
    }

    @Override
    public IFitterEstimator getEstimator() {
        return _estimator;
    }
 
    @Override
    public void setFitAlgorithm(FitAlgorithm fitAlgorithm) {
        _fitAlgorithm = fitAlgorithm;
    }
    
    @Override
    public FitAlgorithm getFitAlgorithm() {
        return _fitAlgorithm;
    }
    
    @Override
    public void setFitFunction(FitFunction fitFunction) {
        _fitFunction = fitFunction;
    }
    
    @Override
    public FitFunction getFitFunction() {
        return _fitFunction;
    }

    @Override
    public void setNoiseModel(NoiseModel noiseModel) {
        _noiseModel = noiseModel;
    }

    @Override
    public NoiseModel getNoiseModel() {
        return _noiseModel;
    }
    
    @Override
    public void setXInc(double xInc) {
        _xInc = xInc;
    }
    
    @Override
    public double getXInc() {
        return _xInc;
    }
    
    @Override
    public void setPrompt(double[] prompt) {
        _prompt = prompt;
    }
    
    @Override
    public double[] getPrompt() {
        return _prompt;
    }
    
    @Override
    public void setChiSquareTarget(double chiSquareTarget) {
        _chiSquareTarget = chiSquareTarget;
    }
    
    @Override
    public double getChiSquareTarget() {
        return _chiSquareTarget;
    }
    
    @Override
    public void setFree(boolean[] free) {
        _free = free;
    }
    
    @Override
    public boolean[] getFree() {
        return _free;
    }
    
    @Override
    public void setStartPrompt(int startPrompt) {
        _startPrompt = startPrompt;
    }
    
    @Override
    public int getStartPrompt() {
        return _startPrompt;
    }
    
    @Override
    public void setStopPrompt(int stopPrompt) {
        _stopPrompt = stopPrompt;
    }
    
    @Override
    public int getStopPrompt() {
        return _stopPrompt;
    }
    
    @Override
    public void setStartDecay(int startDecay) {
        _startDecay = startDecay;
    }
    
    @Override
    public int getStartDecay() {
        return _startDecay;
    }
    
    @Override
    public void setStopDecay(int stopDecay) {
        _stopDecay = stopDecay;
    }
    
    @Override
    public int getStopDecay() {
        return _stopDecay;
    }
}
