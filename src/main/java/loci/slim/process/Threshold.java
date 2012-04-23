//
// Threshold.java
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
    * Neither the name of the UW-Madison LOCI nor the
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

package loci.slim.process;

/**
 * This class thresholds the image to a given photon count.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class Threshold implements IProcessor {
    private final int _start;
    private final int _stop;
    private final int _photons;
    private IProcessor _processor;
    
    public Threshold(int start, int stop, int threshold) {
        _start            = start;
        _stop             = stop;
        _photons        = threshold;
    }
    
    /**
     * Specifies a source IProcessor to be chained to this one.
     * 
     * @param processor 
     */
    public void chain(IProcessor processor) {
        _processor = processor;
    }
    
    /**
     * Gets input pixel value.
     * 
     * @param x
     * @param y
     * @param channel
     * @return null or pixel value
     */
    public double[] getPixel(int[] location) {
        double[] decay = _processor.getPixel(location);
        
        // reject any pixels that have less than the threshold number of photons
        if (null != decay) {
            double sum = 0.0;
            for (int bin = _start; bin <= _stop; ++bin) {
                sum += decay[bin];
            }
            if (sum < _photons) {
                decay = null;
            }
        }
        return decay;
    }  
}
