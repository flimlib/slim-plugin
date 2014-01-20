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

package loci.slim.preprocess;

/**
 * This class thresholds the image to a given photon count.
 * 
 * @author Aivar Grislis
 */
public class Threshold implements IProcessor {
    private final int _threshold;
    private IProcessor _processor;
    
    public Threshold(int threshold) {
        _threshold = threshold;
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
     * @param location
     * @return null or pixel value
     */
    public double[] getPixel(int[] location) {
        double[] decay = _processor.getPixel(location);
        
        // reject any pixels that have less than the threshold number of photons
        if (null != decay) {
            double sum = 0.0;
            for (int bin = 0; bin < decay.length; ++bin) {
                sum += decay[bin];
            }
            if (sum < _threshold) {
                decay = null;
            }
        }
        return decay;
    }  
}
