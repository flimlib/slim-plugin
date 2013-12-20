/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2013, UW-Madison LOCI
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

/**
 * Container for  the local fitting parameters, i.e. those valid for the current
 * pixel.
 * 
 * @author Aivar Grislis
 */
public class DefaultLocalFitParams implements LocalFitParams {
    private double[] y;
    private double[] sig;
    private int fitStart;
    private int fitStop;
    private double[] params;
    private double[] yFitted;
    
    @Override
    public void setY(double[] y) {
        this.y = y;
    }
    
    @Override
    public double[] getY() {
        return y;
    }
 
    @Override
    public void setSig(double[] sig) {
        this.sig = sig;
    }

    @Override
    public double[] getSig() {
        return sig;
    }
    
    @Override
    public void setParams(double[] params) {
        this.params = params;
    }
    
    @Override
    public double[] getParams() {
        return params;
    }
    
    @Override
    public void setYFitted(double[] yFitted) {
        this.yFitted = yFitted;
    }
    
    @Override
    public double[] getYFitted() {
        return yFitted;
    }
}
