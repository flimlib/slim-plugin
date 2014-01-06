/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2014, UW-Madison LOCI
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

package loci.slim2.fitting;

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
public class DefaultGlobalFitParams implements GlobalFitParams {
    private IFitterEstimator estimator;
    private FitAlgorithm fitAlgorithm;
    private FitFunction fitFunction;
    private NoiseModel noiseModel;
    private double xInc;
    private double[] prompt;
    private double chiSquareTarget;
    private boolean[] free;
    private int startPrompt;
    private int stopPrompt;
    private int transientStart;
    private int dataStart;
    private int transientStop;

    @Override
    public void setEstimator(IFitterEstimator estimator) {
        this.estimator = estimator;
    }

    @Override
    public IFitterEstimator getEstimator() {
        return estimator;
    }
 
    @Override
    public void setFitAlgorithm(FitAlgorithm fitAlgorithm) {
        this.fitAlgorithm = fitAlgorithm;
    }
    
    @Override
    public FitAlgorithm getFitAlgorithm() {
        return fitAlgorithm;
    }
    
    @Override
    public void setFitFunction(FitFunction fitFunction) {
        this.fitFunction = fitFunction;
    }
    
    @Override
    public FitFunction getFitFunction() {
        return fitFunction;
    }

    @Override
    public void setNoiseModel(NoiseModel noiseModel) {
        this.noiseModel = noiseModel;
    }

    @Override
    public NoiseModel getNoiseModel() {
        return noiseModel;
    }
    
    @Override
    public void setXInc(double xInc) {
        this.xInc = xInc;
    }
    
    @Override
    public double getXInc() {
        return xInc;
    }
    
    @Override
    public void setPrompt(double[] prompt) {
        this.prompt = prompt;
    }
    
    @Override
    public double[] getPrompt() {
        return prompt;
    }
    
    @Override
    public void setChiSquareTarget(double chiSquareTarget) {
        this.chiSquareTarget = chiSquareTarget;
    }
    
    @Override
    public double getChiSquareTarget() {
        return chiSquareTarget;
    }
    
    @Override
    public void setFree(boolean[] free) {
        this.free = free;
    }
    
    @Override
    public boolean[] getFree() {
        return free;
    }
    
    @Override
    public void setStartPrompt(int startPrompt) {
        this.startPrompt = startPrompt;
    }
    
    @Override
    public int getStartPrompt() {
        return startPrompt;
    }
    
    @Override
    public void setStopPrompt(int stopPrompt) {
        this.stopPrompt = stopPrompt;
    }
    
    @Override
    public int getStopPrompt() {
        return stopPrompt;
    }
    
    @Override
    public void setTransientStart(int transientStart) {
        this.transientStart = transientStart;
    }

    @Override
    public int getTransientStart() {
        return transientStart;
    }
    
    @Override
    public void setDataStart(int dataStart) {
        this.dataStart = dataStart;
    }
    
    @Override
    public int getDataStart() {
        return dataStart;
    }
    
    @Override
    public void setTransientStop(int transientStop) {
        this.transientStop = transientStop;
    }
    
    @Override
    public int getTransientStop() {
        return transientStop;
    }
}
