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

package loci.slim.heuristics;

import loci.curvefitter.CurveFitData;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;
import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.curvefitter.IFitterEstimator;
import loci.curvefitter.SLIMCurveFitter;

/**
 * Based on TRI2 TRCursors.c.  Comments in quotes are from that source file.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/CursorHelper.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/CursorHelper.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class CursorEstimator {
    public static final int PROMPT_START        = 0;
    public static final int PROMPT_STOP         = 1;
    public static final int PROMPT_BASELINE     = 2;
    public static final int TRANSIENT_START     = 3;
    public static final int DATA_START          = 4;
    public static final int TRANSIENT_STOP      = 5;
    private static final int ATTEMPTS = 10;
    // This is the value I get for unitialized floats in Visual Studio 2008;
    // used to debug & compare SLIM Plugin and TRI2 in marginal situations.
    private static final double C_UNITIALIZED = -1.0737418E8;

    public static double[] estimateExcitationCursors(double[] excitation) {
        double baseline;
        double maxval;
        int index;
        int startp = 0;
        int endp = 0;
        int i;
        double[] diffed = new double[excitation.length];
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

        double[] returnValue = new double[3];
        returnValue[PROMPT_START]    = startp;
        returnValue[PROMPT_STOP]     = endp;
        returnValue[PROMPT_BASELINE] = baseline;
        return returnValue;
    }

    /**
     * Provides estimation of decay cursors.
     * 
     * Note that TRI2 does not support this.  There is no "Estimate Cursors"
     * button if you don't have a prompt.  TRI2 saves and restores the decay
     * cursor values even if you switch to a new image.
     * 
     * @param xInc
     * @param decay
     * @return 
     */
    public static int[] estimateDecayCursors(double xInc, double[] decay) {
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

        int[] returnValue = new int[6];
        returnValue[TRANSIENT_START] = 0;
        returnValue[DATA_START]      = startIndex;
        returnValue[TRANSIENT_STOP]  = stopIndex;
        return returnValue;
    }

    public static double[] estimateCursors(double xInc, double[] prompt,
            double[] decay, double chiSqTarget) {
        System.out.println("xInc " + xInc + " prompt " + prompt + " decay " + decay + " chiSqTarget " + chiSqTarget);
        double[] returnValue = new double[6];
        double baseline;
        double maxval; // TRCursors.c has "unsigned short maxsval, maxval; double maxfval, *diffed;"
        int index;
        int startp = 0;
        int startt = 0;
        int endp = 0;
        int endt = 0;
        int i;
        double[] diffed = new double[prompt.length];
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
        double[] diffedd = new double[decay.length];
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
            //TODO ARG transStartIndex etc. are unitialized
            //  do_estimate_resets restores values to previous, not this!
            returnValue[PROMPT_START]    = startp;
            returnValue[PROMPT_STOP]     = endp;
            returnValue[PROMPT_BASELINE] = baseline;
            returnValue[TRANSIENT_START] = transStartIndex;
            returnValue[DATA_START]      = startt;
            returnValue[TRANSIENT_STOP]  = transEndIndex;
            
            return returnValue; //TODO "do_estimate_resets; do_estimate_frees; "
        }

        System.out.println("prompt " + prompt.length + " decay " + decay.length);
        
        double[] adjustedPrompt = adjustPrompt(prompt, startp*xInc, endp*xInc, baseline, xInc);

        for (i = 0; i < 2 * ATTEMPTS + 1; ++i, ++transStartIndex) {

            transFitStartIndex = transStartIndex;
            System.out.println("transStartIndex " + transStartIndex + " transFitStartIndex " + transFitStartIndex + " transEndIndex " + transEndIndex);

            int fitStart = transFitStartIndex - transStartIndex; // e.g. always zero
            int fitStop = transEndIndex - transStartIndex;
            int nData = transEndIndex - transStartIndex;
            System.out.println("  fitStart " + fitStart + " fitStop " + fitStop + " nData " + nData);

            CurveFitData curveFitData = new CurveFitData();
            param[1] = param[2] = param[3] = C_UNITIALIZED;              
            curveFitData.setParams(param); //TODO param has random values!!
                        
            double[] adjustedDecay = adjustDecay(decay, transStartIndex);
            curveFitData.setYCount(adjustedDecay);
            curveFitData.setTransStartIndex(0);
            curveFitData.setDataStartIndex(fitStart);
            curveFitData.setTransEndIndex(fitStop);            
            curveFitData.setChiSquareTarget(chiSqTarget);
            curveFitData.setSig(null);
            curveFitData.setYFitted(yFitted);
            CurveFitData[] data = new CurveFitData[] { curveFitData };
            
            SLIMCurveFitter curveFitter = new SLIMCurveFitter();
            curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD);
            curveFitter.setXInc(xInc);
            curveFitter.setFree(free);
            curveFitter.setInstrumentResponse(adjustedPrompt);
            curveFitter.setNoiseModel(NoiseModel.POISSON_FIT); 
            
            int ret = curveFitter.fitData(data);
            
            if (ret < 0) {
                param[1] = 0.0;
                int j = findMax(decay, transFitStartIndex, transEndIndex);
                param[2] = decay[j];
                param[3] = 2.0;
            }
            
            System.out.println("i " + i + " Z " + param[1] + " A " + param[2] + " T " + param[3]);
            
            curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_LMA);

            ret = curveFitter.fitData(data);

            if (ret >= 0) {
                System.out.println("for start " + fitStart + " stop " + fitStop + " chiSq is " + data[0].getChiSquare());
                chiSqTable[i] = data[0].getParams()[0]; //TODO ARG s/b same or better yet not kept in two places: data[0].getChiSquare();
            }
            else {
                System.out.println("ret from fitData is " + ret);
                chiSqTable[i] = 1e10f; // "silly value"
            }
        }

        // "Find the minimum chisq in this range"
        index = findMin(chiSqTable, 2 * ATTEMPTS + 1);
        System.out.println("min chisq index is " + index + " value " + chiSqTable[index]);
        
        if (chiSqTable[index] > 9e9f) {  // "no luck here..."
            System.out.println("no luck here return");
            for (double chiSq : chiSqTable) {
                System.out.println("chiSq is " + chiSq);
            }
            System.out.println("index is " + index);

            returnValue[PROMPT_START]    = startp;
            returnValue[PROMPT_STOP]     = endp;
            returnValue[PROMPT_BASELINE] = baseline;
            returnValue[TRANSIENT_START] = transStartIndex;
            returnValue[DATA_START]      = startt;
            returnValue[TRANSIENT_STOP]  = transEndIndex;
            System.out.print("1 ");
            dump(returnValue);
            return returnValue; //TODO do estimate resets/frees???
        }

        // "Then we're rolling!"
        transStartIndex = startt - ATTEMPTS;
        if (transStartIndex < 0) {
            transStartIndex = 0;
        }
        transStartIndex += index;
        transFitStartIndex = transStartIndex + (transEndIndex - transStartIndex) / 20;
        
        System.out.println("made it all the way to the end of estimateCursors");
        
        returnValue[PROMPT_START]    = startp;
        returnValue[PROMPT_STOP]     = endp;
        returnValue[PROMPT_BASELINE] = baseline;
        returnValue[TRANSIENT_START] = transStartIndex;
        returnValue[DATA_START]      = transFitStartIndex;
        returnValue[TRANSIENT_STOP]  = transEndIndex;
        System.out.print("2 ");
        dump(returnValue);
        return returnValue;
    }
    
    private static void dump(double[] value) {
        System.out.print("prompt ");
        System.out.print("start " + value[PROMPT_START]);
        System.out.print("end " + value[PROMPT_STOP]);
        System.out.print("transient ");
        System.out.print("start " + value[TRANSIENT_START]);
        System.out.print("data start " + value[DATA_START]);
        System.out.println("end " + value[TRANSIENT_STOP]);
    }

    /**
     * Based on TRI2 TRCursor.c UpdatePrompt
     * 
     * @param prompt
     * @param start
     * @param stop
     * @param baseline
     * @param inc
     * @return 
     */
    private static double[] adjustPrompt(double[] prompt, double start, double stop,
            double baseline, double inc)
    {
        double[] adjusted = null;
        int startIndex = (int) Math.ceil(start / inc);
        int stopIndex = (int) Math.floor(stop / inc) + 1;
        System.out.println("stop is " + stop + " stopIndex " + stopIndex);
        int length = stopIndex - startIndex;
        if (length <= 0) {
            return adjusted;
        }
        double scaling = 0.0;
        for (int i = startIndex; i < stopIndex; ++i) {
            if (i < prompt.length) {
                scaling += prompt[i];
            }
        }
        if (0.0 == scaling) {
            return adjusted;
        }
        adjusted = new double[length];
        for (int i = startIndex; i < stopIndex; ++i) {
            adjusted[i - startIndex] = (prompt[i] - baseline) / scaling;
        }
        System.out.println("adjusted " + adjusted[0] + " " + adjusted[1] + " " + adjusted[2]);
        return adjusted;
    }
    
    private static double[] adjustDecay(double[] decay, int startIndex) {
        int size = decay.length - startIndex;
        double[] adjusted = new double[size];
        for (int i = 0; i < size; ++i) {
            adjusted[i] = decay[i + startIndex];
        }
        return adjusted;
    }
    
    /**
     * Calculates the Z background value looking at the prepulse
     * part of the decay curve.
     * (Based on calcBgFromPrepulse in TRfitting.c
     * @param prepulse
     * @param n
     * @return
     */
    private static double calcBgFromPrepulse(double[] prepulse, int n) {
        double z = 0.0f;

        if (z > 0) {
            double val = 0.0f;
            for (int i = 0; i <n; ++i) {
                val += prepulse[i];
            }
            z = val/n;
        }
        return z;
    }

    /**
     * "Get initial estimates for the params that go forward to Marquardt".
     * (Based on expParameterEstimation from TRfitting.c.)
     *
     * @return
     */
    public static double[] estimateParameters(boolean useRLD,
            boolean useBackground,
            double[] trans,
            int transFitStartIndex,
            int transStartIndex,
            int transEndIndex) {
        double a, t, z;

        // initial guess
        a = 1000.0f;
        t = 2.0f;
        z = 0.0f;
        
        if (useRLD) {
            transFitStartIndex = findMax(trans, transFitStartIndex, transEndIndex);
            //TODO ARG do a RLD fit using trans, transFitStartIndex, transStartIndex, transEndIndex
            // see wiki entry "expParameterEstimation from TRfitting.c"
            //   note that the prompt is disregarded here
            int returnValue = -1;
            if (returnValue >= 0) {
                a = t = z = 0.0f;
            }
        }
        else if (useBackground) {
            z = calcBgFromPrepulse(trans, transStartIndex);
        }

        return new double[] { z, a, t };
    }
 
    /**
     * Convert time-based value to a bin number.
     *
     * Based on TRI2.  Note that 'valueToBin' and 'binToValue' won't round-trip.
     *
     * @param upper
     * @param value
     * @param inc
     * @param max
     * @return 
     */
    public static int valueToBin(boolean upper, double value, double inc,
            int max)
    {
        int intValue = 0;
        if (upper) {
            intValue = (int) Math.ceil(value / inc) + 1;
        }
        else {
            intValue = (int) Math.floor(value / inc);
        }
        // constrain within limits
        if (intValue < 0) {
            intValue = 0;
        }
        if (intValue > max) {
            intValue = max;
        }
        return intValue;
    }

    /**
     * Convert bin number to time-based value.
     * 
     * Based on TRI2.  Note that 'binToValue' and 'valueToBin' won't round-trip.
     * 
     * The 'upper' and 'max' parameters are not utilized in this implementation.
     * 
     * @param upper
     * @param bin
     * @param inc
     * @param max
     * @return 
     */
    public static double binToValue(boolean upper, int bin, double inc,
            double max) {
        return bin * inc;
    }

    private static int findMax(double[] values) {
        return findMax(values, 0, values.length);
    }

    private static int findMax(double[] values, int endIndex) {
        return findMax(values, 0, endIndex);
    }

    private static int findMax(double[] values, int startIndex, int endIndex) {
        if (endIndex > values.length) {
            System.out.println("CursorEstimator.findMax endIndex is " + endIndex + " values.length is " + values.length);
            endIndex = values.length;
        }
        if (startIndex > values.length) {
            System.out.println("CursorEstimator.findMax startIndex is " + startIndex + " values.length is " + values.length);
            startIndex = values.length;
        }
        if (values.length == 0) {
            System.out.println("CursorEstimator.findMax but values is length zero");
            return startIndex;
        }
        int index = startIndex;
        double max = values[startIndex];
        for (int i = startIndex; i < endIndex; ++i) {
            if (values[i] > max) {
                max = values[i];
                index = i;
            }
        }
        return index;
    }

    private static int findMin(double[] values, int endIndex) {
        return findMin(values, 0, endIndex);
    }

    private static int findMin(double[] values, int startIndex, int endIndex) {
        int index = startIndex;
        double min = values[startIndex];
        for (int i = startIndex; i < endIndex; ++i) {
            if (values[i] < min) {
                min = values[i];
                index = i;
            }
        }
        return index;
    }
}
