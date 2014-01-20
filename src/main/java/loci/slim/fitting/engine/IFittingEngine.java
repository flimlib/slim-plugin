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

package loci.slim.fitting.engine;

import loci.slim.fitting.params.ILocalFitParams;
import loci.slim.fitting.params.IGlobalFitParams;
import loci.slim.fitting.params.IFitResults;

import java.util.List;

import loci.curvefitter.ICurveFitter;

/**
 * Interface for a fitting engine.
 * 
 * @author Aivar Grislis
 */
public interface IFittingEngine {
    
    /**
     * Shuts down the fitting engine.
     */
    public void shutdown();
    
    /**
     * Sets number of threads to use in a fit.
     * 
     * @param threads 
     */
    public void setThreads(int threads);
    
    /**
     * Sets the ICurveFitter for the fitting engine to use.
     * 
     * @param curveFitter 
     */
    public void setCurveFitter(ICurveFitter curveFitter);
    
    /**
     * Fit one pixel.
     * 
     * @param params
     * @param data
     * @return fitted results
     */
    public IFitResults fit(IGlobalFitParams params, ILocalFitParams data);
    
    /**
     * Fits a list of pixels.
     * 
     * @param params
     * @param dataList
     * @return 
     */
    public List<IFitResults> fit(IGlobalFitParams params, List<ILocalFitParams> dataList);
}
