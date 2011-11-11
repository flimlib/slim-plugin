//
// LameCursorEstimator.java
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

package imagej.slim.fitting.cursor;

/**
 * Just a lame implementation to get things going.
 * 
 * S/b based on TRI2; w/b GPL?
 * 
 * @author Aivar Grislis
 */
public class LameCursorEstimator implements ICursorEstimator {

    @Override
    public ICursor globalCursor(double[] prompt, double[] decay) {
        ICursor cursor = new Cursor();
        cursor.setPromptStart(getMostSteep(prompt));
        cursor.setPromptStop(getLeastSteep(prompt));
        cursor.setDecayStart(getMostSteep(decay));
        cursor.setDecayStop(7 * decay.length / 8);
        return null;
    }

    @Override
    public ICursor localCursor(ICursor global, double[] prompt, double[] decay) {
        ICursor cursor = new Cursor();
        cursor.setPromptStart(global.getPromptStart());
        cursor.setPromptStop(global.getPromptStop());
        cursor.setDecayStart(global.getDecayStart());
        cursor.setDecayStop(global.getDecayStop());
        return null;
    }
    
    private int getMostSteep(double[] curve) {
        double delta = 0.0;
        int deltaIndex = 0;
        for (int i = 1; i < curve.length; ++i) {
            if (curve[i] - curve[i - 1] > delta) {
                delta = curve[i] - curve[i - 1];
                deltaIndex = i - 1;
            }
        }
        return deltaIndex;
    }
    
    private int getLeastSteep(double[] curve) {
        double delta = 0.0;
        int deltaIndex = 0;
        for (int i = 1; i < curve.length; ++i) {
            if (curve[i] - curve[i - 1] < delta) {
                delta = curve[i] - curve[i - 1];
                deltaIndex = i - 1;
            }
        }
        return deltaIndex;
    } 
}
