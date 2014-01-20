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

import imagej.thread.ThreadPool;
import java.util.ArrayList;
import java.util.List;
import loci.curvefitter.ICurveFitter;

/**
 * Fitting engine that uses a thread pool.
 * 
 * @author Aivar Grislis
 */
public class ThreadedFittingEngine implements FittingEngine {
    private ThreadPool<FitResults> threadPool;
    private ICurveFitter curveFitter;
    
    public ThreadedFittingEngine() {
        threadPool = new ThreadPool<FitResults>();
    }
 
    @Override
    public void shutdown() {
        threadPool.shutdown();
    }
    
    @Override
    public synchronized void setThreads(int threads) {
        threadPool.setThreads(threads);
    }
    
    @Override
    public synchronized void setCurveFitter(ICurveFitter curveFitter) {
        this.curveFitter = curveFitter;
    }
    
    @Override
    public synchronized FitResults fit
            (final GlobalFitParams params, final LocalFitParams data) {
        FittingCallable callable = new DefaultFittingCallable();
        callable.setup(curveFitter, params, data);
		return callable.call();
    }
    
    @Override
    public synchronized List<FitResults> fit
            (final GlobalFitParams params, final List<LocalFitParams> dataList) {
        
        List<FittingCallable> callableList
                = new ArrayList<FittingCallable>();
        
        for (LocalFitParams data : dataList) {
            FittingCallable callable = new DefaultFittingCallable();
            callable.setup(curveFitter, params, data);
            callableList.add(callable);
        }
        
        List<FitResults> resultList = threadPool.process(callableList);
        return resultList;
    }
}
