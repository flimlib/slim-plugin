//
// Configuration.java
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

package loci.slim.fitting.config;

import loci.slim.fitting.callable.IFittingEngineCallable;
import loci.slim.fitting.callable.FittingEngineCallable;
import loci.slim.fitting.cursor.LameCursorEstimator;
import loci.slim.fitting.cursor.ICursorEstimator;
import loci.slim.fitting.engine.IFittingEngine;
import loci.slim.fitting.engine.ThreadedFittingEngine;

import loci.curvefitter.ICurveFitter;
import loci.curvefitter.SLIMCurveFitter;

/**
 * Handles configuration specific to the SLIM Plugin.
 * 
 * A singleton so only one configuration for the SLIMPlugin.//TODO
 * 
 * @author Aivar Grislis
 */
public class Configuration extends ConfigurationHelper {
    private static Configuration _instance = null;
    private int _threads = 8;
    private IFittingEngine _fittingEngine;
    private ICurveFitter _curveFitter;
    private ICursorEstimator _cursorEstimator;

    /**
     * Private constructor for singleton pattern.
     */
    private Configuration() {
    }
    
    public static synchronized Configuration getInstance() {
        if (null == _instance) {
            _instance = new Configuration();
        }
        return _instance;
    }
    
    public int getThreads() {
        return _threads;
    }
    
    public synchronized IFittingEngine getFittingEngine() {
        if (null == _fittingEngine) {
            _fittingEngine = new ThreadedFittingEngine();
        }
        return _fittingEngine;
    }
    
    public synchronized ICurveFitter getCurveFitter() {
        if (null == _curveFitter) {
            _curveFitter = new SLIMCurveFitter();
        }
        return _curveFitter;
    }
    
    public synchronized ICursorEstimator getCursorEstimator() {
        if (null == _cursorEstimator) {
            _cursorEstimator = new LameCursorEstimator();
        }
        return _cursorEstimator;
    }
    
    public IFittingEngineCallable newFittingEngineCallable() {
        return new FittingEngineCallable();
    }
    
}
