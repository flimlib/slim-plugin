//
// ThreadedFittingEngine.java
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

package loci.slim.fitting.engine;

import loci.slim.fitting.params.ILocalFitParams;
import loci.slim.fitting.params.IGlobalFitParams;
import loci.slim.fitting.params.IFitResults;
import loci.slim.fitting.callable.IFittingEngineCallable;
import loci.slim.fitting.config.Configuration;

import loci.curvefitter.ICurveFitter;

import java.util.ArrayList;
import java.util.List;

import imagej.thread.ThreadPool;

/**
 * Fitting engine that uses a thread pool.
 * 
 * @author Aivar Grislis
 */
public class ThreadedFittingEngine implements IFittingEngine {
    private static int THREADS = 4;
    private int _threads = THREADS;
    private ThreadPool<IFitResults> _threadPool;
    private ICurveFitter _curveFitter;
    
    public ThreadedFittingEngine() {
        _threadPool = new ThreadPool<IFitResults>();
    }
 
    /**
     * Cancel fit or done fitting.
     */
    public void shutdown() {
        _threadPool.shutdown();
    }
    
    /**
     * Sets number of threads to use.
     * 
     * @param threads 
     */
    public synchronized void setThreads(int threads) {
        _threadPool.setThreads(threads);
    }
    
    /**
     * Sets curve fitter to use.
     * 
     * @param curve fitter 
     */
    public synchronized void setCurveFitter(ICurveFitter curveFitter) {
        _curveFitter = curveFitter;
    }
    
    /**
     * Fits a single pixel with given parameters.
     * 
     * Nothing to parallelize, doesn't use the ThreadPool.
     * 
     * @param params
     * @param data
     * @return results
     */
    public synchronized IFitResults fit
            (final IGlobalFitParams params, final ILocalFitParams data) {
        IFittingEngineCallable callable
                = Configuration.getInstance().newFittingEngineCallable();
        callable.setup(_curveFitter, params, data);
        return callable.call();
    }
    
    /**
     * Fit one or more pixels with given parameters.
     * 
     * @param params given parameters
     * @param data one or more pixels data
     * @return results one or more pixels results
     */
    public synchronized List<IFitResults> fit
            (final IGlobalFitParams params, final List<ILocalFitParams> dataList) {
        
        List<IFittingEngineCallable> callableList
                = new ArrayList<IFittingEngineCallable>();
        
        for (ILocalFitParams data : dataList) {
            IFittingEngineCallable callable
                    = Configuration.getInstance().newFittingEngineCallable();
            callable.setup(_curveFitter, params, data);
            callableList.add(callable);
        }
        
        List<IFitResults> resultList = _threadPool.process(callableList);
        return resultList;
    }
}
