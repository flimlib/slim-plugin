//
// SLIMCurveCallable.java
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

import imagej.slim.fitting.params.LocalFitResult;
import imagej.slim.fitting.params.ILocalFitParams;
import imagej.slim.fitting.params.IGlobalFitParams;
import imagej.slim.fitting.params.ILocalFitResults;
import java.util.Random;
import loci.curvefitter.CurveFitData;
import loci.curvefitter.GrayCurveFitter;
import loci.curvefitter.GrayNRCurveFitter;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;
import loci.curvefitter.JaolhoCurveFitter;
import loci.curvefitter.MarkwardtCurveFitter;
import loci.curvefitter.SLIMCurveFitter;

/**
 *
 * @author Aivar Grislis
 */
public class SLIMCurveCallable implements IFittingEngineCallable {
    private IGlobalFitParams _globalParams;
    private ILocalFitParams _localParams;
    private ILocalFitResults _result;
    private ICurveFitter _curveFitter;
    
    public void setup(final IGlobalFitParams globalParams, final ILocalFitParams localParams) {
        _globalParams = globalParams;
        _localParams = localParams;
    }
    
    public ILocalFitResults call() {
        System.out.println(">>> " + _localParams.getId() + "-" + Thread.currentThread().getName());
        _result = new LocalFitResult();
        _result.setId(_localParams.getId());
 
        if (true) {
            int waitTime = (new Random()).nextInt(10);
            try {
                Thread.sleep(waitTime);
            }
            catch (InterruptedException e) {

            }
        }
        
        System.out.println("<<< " + _localParams.getId() + "-" + Thread.currentThread().getName());
        return _result;
    }
}
