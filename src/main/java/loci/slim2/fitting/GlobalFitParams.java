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

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.curvefitter.IFitterEstimator;

/**
 * Interface for container for the global fitting parameters, i.e. those valid
 * for the whole image.
 * 
 * @author Aivar Grislis
 */
public interface GlobalFitParams {
    
    public void setEstimator(IFitterEstimator estimator);
    
    public IFitterEstimator getEstimator();
    
    public void setFitAlgorithm(FitAlgorithm fitAlgorithm);
    
    public FitAlgorithm getFitAlgorithm();
    
    public void setFitFunction(FitFunction fitFunction);
    
    public FitFunction getFitFunction();
    
    public void setNoiseModel(NoiseModel noiseModel);
    
    public NoiseModel getNoiseModel();
    
    public void setXInc(double xInc);
    
    public double getXInc();
    
    public void setPrompt(double[] prompt);
    
    public double[] getPrompt();
    
    public void setChiSquareTarget(double chiSquareTarget);
    
    public double getChiSquareTarget();
    
    public void setFree(boolean[] free);
    
    public boolean[] getFree();
    
    public void setStartPrompt(int startPrompt);
    
    public int getStartPrompt();
    
    public void setStopPrompt(int stopPrompt);
    
    public int getStopPrompt();
    
    public int getTransientStart();
    
    public void setTransientStart(int transientStart);
    
    public int getDataStart();
    
    public void setDataStart(int dataStart);
    
    public int getTransientStop();
    
    public void setTransientStop(int transientStop);
}

