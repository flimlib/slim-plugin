//
// CursorHelper.java
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

package loci.slim;

import loci.curvefitter.CurveFitData;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.SLIMCurveFitter;

/**
 * Based on TRI2 TRCursors.c.  Comments in quotes are from that source file.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/CursorHelper.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/CursorHelper.java">SVN</a></dd></dl>
 *
 * @author aivar
 */
public class CursorHelper {
    private static final int ATTEMPTS = 10;

    public static float[] estimateExcitationCursors(float[] excitation) {
        float baseline;
        float maxval;
        int index;
        int startp = 0;
        int endp = 0;
        int i;
        float[] diffed = new float[excitation.length];
        int steepp;

        // "Estimate prompt baseline; very rough and ready"
        index = findMax(excitation);
        maxval = excitation[index];

        if (index > excitation.length * 3 /4) { // "integer arithmetic"
            baseline = 0.0f;
        }
        else {
            baseline = 0.0f;
            int index2 = (index + excitation.length) / 2;
            for (i = index2; i < excitation.length; ++i) {
                baseline += excitation[i];
            }
            baseline /= (excitation.length - index2);
        }

        // "Where does the prompt first drop to (peak amplitude - baseline) / 10?
        // This could be silly if the baseline is silly; caveat emptor!"
        for (i = index; i > 0; --i) {
            if ((excitation[i] - baseline) < 0.1 * (maxval - baseline)) {
                break;
            }
        }
        startp = i; // "First estimate"

        // "And first drop away again?"
        for (i = index; i < excitation.length - 1; ++i) {
            if ((excitation[i] - baseline) < 0.1 * (maxval - baseline)) {
                break;
            }
        }
        endp = i;

        // "Differentiate"
        for (i = 0; i < index; ++i) {
            diffed[i] = excitation[i + 1] - excitation[i];
        }

        // "Find the steepest rise"
        steepp = (int) findMax(diffed, index);
        if (steepp < startp) {
            startp = steepp;
        }

        // "One more sanity check"
        if (endp == startp) {
            if (endp == excitation.length) {
                --startp;
            }
            else {
                ++endp;
            }
        }

        float[] returnValue = new float[3];
        returnValue[0] = startp;
        returnValue[1] = endp;
        returnValue[2] = baseline;
        return returnValue;
    }

    public static int[] estimateDecayCursors(float xInc, double[] decay) {
        int maxIndex = findMax(decay);
        double[] diffed = new double[maxIndex];
        // "Differentiate"
        for (int i = 0; i < maxIndex - 1; ++i) {
            diffed[i] = decay[i + 1] - decay[i];
        }
        int steepIndex = findMax(diffed);

        int startIndex = 2 * maxIndex - steepIndex;
        int stopIndex = 9 * decay.length / 10;

        // sanity check
        if (startIndex > stopIndex) {
            startIndex = stopIndex - 1;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }

        return new int[] { startIndex, stopIndex };
    }

    public static float[] estimateCursors(float xInc, float[] prompt, double[] decay) {
        float[] returnValue = new float[5];
        float baseline;
        float maxval; // TRCursors.c has "unsigned short maxsval, maxval; float maxfval, *diffed;"
        int index;
        int startp = 0;
        int startt = 0;
        int endp = 0;
        int endt = 0;
        int i;
        float[] diffed = new float[prompt.length];
        int steepp;
        int steept;
        // "For Marquardt fitting"
        double param[] = new double[4];
        boolean free[] = new boolean[] { true, true, true };
        double[] yFitted = new double[decay.length];
        double[] chiSqTable = new double[2 * ATTEMPTS + 1];
        int transStartIndex;
        int transFitStartIndex;
        int transEndIndex;

        // "Estimate prompt baseline; very rough and ready"
        index = findMax(prompt);
        maxval = prompt[index];

        if (index > prompt.length * 3 /4) { // "integer arithmetic"
            baseline = 0.0f;
        }
        else {
            baseline = 0.0f;
            int index2 = (index + prompt.length) / 2;
            for (i = index2; i < prompt.length; ++i) {
                baseline += prompt[i];
            }
            baseline /= (prompt.length - index2);
        }

        // "Where does the prompt first drop to (peak amplitude - baseline) / 10?
        // This could be silly if the baseline is silly; caveat emptor!"
        for (i = index; i > 0; --i) {
            if ((prompt[i] - baseline) < 0.1 * (maxval - baseline)) {
                break;
            }
        }
        startp = i; // "First estimate"

        // "And first drop away again?"
        for (i = index; i < prompt.length - 1; ++i) {
            if ((prompt[i] - baseline) < 0.1 * (maxval - baseline)) {
                break;
            }
        }
        endp = i;

        // "Differentiate"
        for (i = 0; i < index; ++i) {
            diffed[i] = prompt[i + 1] - prompt[i];
        }

        // "Find the steepest rise"
        steepp = (int) findMax(diffed, index);
        if (steepp < startp) {
            startp = steepp;
        }

        // "One more sanity check"
        if (endp == startp) {
            if (endp == prompt.length) {
                --startp;
            }
            else {
                ++endp;
            }
        }

        // "Now do the same for the transient decay"
        index = findMax(decay);

        // "Differentiate"
        double[] diffedd = new double[decay.length]; //TODO double vs float issues
        for (i = 0; i < index; ++i) {
            diffedd[i] = decay[i + 1] - decay[i];
        }

        // "Find the steepest rise"
        steept = (int) findMax(diffedd, index);

        // "Make steep - start the same for both prompt and transient"
        startt = steept - (steepp - startp);
        if (startt < 0) {
            startt = 0;
        }

        System.out.println("steepest prompt " + steepp + " steepest transient " + steept);
        System.out.println("startt is " + startt);
        System.out.println("   startp " + startp + " endp " + endp + " baseline " + baseline);

        // "Now we've got estimates we can do some Marquardt fitting to fine-tune
        // the estimates"

        transStartIndex = startt - ATTEMPTS;
        if (transStartIndex < 0) {
            transStartIndex = 0;
        }
        transEndIndex = 9 * decay.length / 10; // "90% of transient"
        if (transEndIndex <= transStartIndex + 2 * ATTEMPTS) { // "oops"
            returnValue[0] = startp;
            returnValue[1] = endp;
            returnValue[2] = baseline;
            returnValue[3] = startt;
            returnValue[4] = transEndIndex;
            return returnValue; //TODO "do_estimate_resets; do_estimate_frees; "
        }

        System.out.println("prompt " + prompt.length + " decay " + decay.length);

        //TODO convert everything to float, not double
        double[] response = new double[decay.length];
        for (int n = 0; n < decay.length; ++n) {
            response[n] = decay[index];
        }

        for (i = 0; i < 2 * ATTEMPTS + 1; ++i, ++transStartIndex) {

            transFitStartIndex = transStartIndex;
            System.out.println("transStartIndex " + transStartIndex + " transFitStartIndex " + transFitStartIndex + " transEndIndex " + transEndIndex);

            int fitStart = transFitStartIndex - transStartIndex;
            int fitStop = transEndIndex - transStartIndex;
            int nData = transEndIndex - transStartIndex;
            System.out.println("  fitStart " + fitStart + " fitStop " + fitStop + " nData " + nData);

            CurveFitData curveFitData = new CurveFitData();
            curveFitData.setParams(param); //TODO param has random values!!
            curveFitData.setYCount(decay);
            curveFitData.setSig(null);
            curveFitData.setYFitted(yFitted);
            CurveFitData[] data = new CurveFitData[] { curveFitData };

            SLIMCurveFitter curveFitter = new SLIMCurveFitter(SLIMCurveFitter.AlgorithmType.RLD_LMA);
            curveFitter.setXInc(xInc);
            curveFitter.setFree(free);
            curveFitter.setInstrumentResponse(response);

            int ret = curveFitter.fitData(data, fitStart, fitStop);

            if (ret >= 0) {
                System.out.println("for start " + fitStart + " stop " + fitStop + " chiSq is " + data[0].getChiSquare());
                chiSqTable[i] = data[0].getChiSquare();
            }
            else {
                System.out.println("ret from fitData is " + ret);
                chiSqTable[i] = 1e10f; // "silly value"
            }
        }

        // "Find the minimum chisq in this range"
        index = findMin(chiSqTable, 2 * ATTEMPTS + 1);
        if (chiSqTable[index] > 9e9f) {  // "no luck here..."
            System.out.println("no luck here return");
            for (double chiSq : chiSqTable) {
                System.out.println("chiSq is " + chiSq);
            }
            System.out.println("index is " + index);
            returnValue[0] = startp;
            returnValue[1] = endp;
            returnValue[2] = baseline;
            returnValue[3] = startt;
            returnValue[4] = transEndIndex;
            return returnValue; //TODO do estimate resets/frees???
        }

        // "Then we're rolling!"
        transStartIndex = startt - ATTEMPTS;
        if (transStartIndex < 0) {
            transStartIndex = 0;
        }
        transStartIndex += index;
        transFitStartIndex = transStartIndex + (transEndIndex - transStartIndex) / 20;

        returnValue[0] = startp;
        returnValue[1] = endp;
        returnValue[2] = baseline;
        returnValue[3] = transStartIndex;
        returnValue[4] = transFitStartIndex;
        returnValue[5] = transEndIndex;
        return returnValue;
    }

    private static int findMax(float[] values) {
        return findMax(values, values.length);
    }

    private static int findMax(float[] values, int endIndex) {
        int index = 0;
        float max = 0.0f;
        for (int i = 0; i < endIndex; ++i) {
            if (values[i] > max) {
                max = values[i];
                index = i;
            }
        }
        return index;
    }

    private static int findMin(float[] values, int endIndex) {
        int index = 0;
        float min = Float.MAX_VALUE;
        for (int i = 0; i < endIndex; ++i) {
            if (values[i] < min) {
                min = values[i];
                index = i;
            }
        }
        return index;
    }

    private static int findMax(double[] values) {
        return findMax(values, values.length);
    }

    private static int findMax(double[] values, int endIndex) {
        int index = 0;
        double max = 0.0f;
        for (int i = 0; i < endIndex; ++i) {
            if (values[i] > max) {
                max = values[i];
                index = i;
            }
        }
        return index;
    }

    private static int findMin(double[] values, int endIndex) {
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < endIndex; ++i) {
            if (values[i] < min) {
                min = values[i];
                index = i;
            }
        }
        return index;
    }
}
