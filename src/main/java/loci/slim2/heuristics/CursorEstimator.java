/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim2.heuristics;

import loci.curvefitter.CurveFitData;
import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.curvefitter.SLIMCurveFitter;

/**
 * Based on TRI2 TRCursors.c.  Comments in quotes are from that source file.
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
	// used to create data for CursorEstimatorTest
	private static final boolean createTestData = false;

	/**
	 * Provides estimation of decay cursors.
	 * 
	 * Note that TRI2 does not support this: there is no "Estimate Cursors"
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
		int startIndex = maxIndex + (maxIndex - steepIndex) / 3;
		int stopIndex = 9 * decay.length / 10;

		// sanity check
		if (startIndex > stopIndex) {
			startIndex = stopIndex - 1;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (steepIndex > startIndex) {
			steepIndex = startIndex - 1;
		}
		if (steepIndex < 0) {
			steepIndex = 0;
		}

		int[] returnValue = new int[6];
		returnValue[TRANSIENT_START] = steepIndex;
		returnValue[DATA_START]      = startIndex;
		returnValue[TRANSIENT_STOP]  = stopIndex;
		return returnValue;
	}

	/**
	 * Provides estimation of prompt and decay cursors.
	 * 
	 * Returns a double array so that the prompt baseline may be returned.
	 * Other values are expressed in integer bins.
	 * 
	 * @param xInc
	 * @param prompt
	 * @param decay
	 * @param chiSqTarget
	 * @return 
	 */
	public static double[] estimateCursors(double xInc, double[] prompt,
			double[] decay, double chiSqTarget) {
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

		// Can be used to generate data to cut & paste into test methods
		if (createTestData) {
			System.out.println("----8<----- 1/2");
			System.out.println("double xInc = " + xInc + ";");

			System.out.print("double[] prompt = {");
			for (int n = 0; n < prompt.length; ++n) {
				if (n > 0) {
					System.out.print(",");
				}
				System.out.print(" " + prompt[n]);
			}
			System.out.println(" };");

			System.out.print("double[] decay = {");
			for (int n = 0; n < decay.length; ++n) {
				if (n > 0) {
					System.out.print(",");
				}
				System.out.print(" " + decay[n]);
			}
			System.out.println(" };");

			System.out.println("double chiSqTarget = " + chiSqTarget + ";");

			System.out.println("-----------");
		}

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
		steepp = findMax(diffed, index);
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
		steept = findMax(diffedd, index);

		// "Make steep - start the same for both prompt and transient"
		startt = steept - (steepp - startp);
		if (startt < 0) {
			startt = 0;
		}

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
			checkValues(returnValue);
			return returnValue; //TODO "do_estimate_resets; do_estimate_frees; "
		}
		double[] adjustedPrompt = ExcitationScaler.scale(prompt, startp, endp, baseline, xInc, decay.length);

		for (i = 0; i < 2 * ATTEMPTS + 1; ++i, ++transStartIndex) {
			transFitStartIndex = transStartIndex;

			int fitStart = transFitStartIndex - transStartIndex; // e.g. always zero
			int fitStop = transEndIndex - transStartIndex;
			int nData = transEndIndex - transStartIndex;

			CurveFitData curveFitData = new CurveFitData();
			curveFitData.setParams(param);

			double[] adjustedDecay = adjustDecay(decay, transStartIndex, transEndIndex);

			curveFitData.setYCount(adjustedDecay);
			curveFitData.setTransStartIndex(0);
			curveFitData.setDataStartIndex(fitStart);
			curveFitData.setTransEndIndex(fitStop);
			curveFitData.setChiSquareTarget(chiSqTarget); //TODO this adjustment happens internally within SLIMCurveFitter * (fitStop - fitStart - 3));
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

			curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_LMA);

			ret = curveFitter.fitData(data);

			if (ret >= 0) {
				double chiSq = data[0].getParams()[0];
				// want non-reduced chi square
				chiSqTable[i] = chiSq * (fitStop - fitStart - 3);
			}
			else {
				chiSqTable[i] = 1e10f; // "silly value"
			}
		}

		// "Find the minimum chisq in this range"
		index = findMin(chiSqTable, 2 * ATTEMPTS + 1);

		if (chiSqTable[index] > 9e9f) {  // "no luck here..."
			returnValue[PROMPT_START]    = startp;
			returnValue[PROMPT_STOP]     = endp;
			returnValue[PROMPT_BASELINE] = baseline;
			returnValue[TRANSIENT_START] = transStartIndex;
			returnValue[DATA_START]      = startt;
			returnValue[TRANSIENT_STOP]  = transEndIndex;

			--returnValue[TRANSIENT_STOP];
			checkValues(returnValue);

			return returnValue; //TODO do estimate resets/frees???
		}

		// "Then we're rolling!"
		transStartIndex = startt - ATTEMPTS;
		if (transStartIndex < 0) {
			transStartIndex = 0;
		}
		transStartIndex += index;
		transFitStartIndex = transStartIndex + (transEndIndex - transStartIndex) / 20;

		returnValue[PROMPT_START]    = startp;
		returnValue[PROMPT_STOP]     = endp;
		returnValue[PROMPT_BASELINE] = baseline;
		returnValue[TRANSIENT_START] = transStartIndex;
		returnValue[DATA_START]      = transFitStartIndex;
		returnValue[TRANSIENT_STOP]  = transEndIndex;
		checkValues(returnValue);
		return returnValue;
	}

	private static void checkValues(double[] value) {
		//TODO ARG patches a bug!:
		if (value[DATA_START] < value[TRANSIENT_START]) {
			if (value[DATA_START] < 0.0) {
				System.out.println("Calculated data start is less than zero!!!");
				value[DATA_START] = 0.0;

			}
			double tmp = value[DATA_START];
			value[DATA_START] = value[TRANSIENT_START];
			value[TRANSIENT_START] = tmp;
		}

		// Can be used to generate data to cut & paste into test methods
		if (createTestData) {
			System.out.println("----8<----- 2/2");

			System.out.print("double[] expResult = {");
			for (int i = 0; i < value.length; ++i) {
				if (i > 0) {
					System.out.print(",");
				}
				System.out.print(" " + value[i]);
			}
			System.out.println(" };");
			System.out.println("-----------");
		}
	}

	private static double[] adjustDecay(double[] decay, int startIndex, int endIndex) {
		int size = endIndex - startIndex;
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

	private static int findMax(double[] values) {
		return findMax(values, 0, values.length);
	}

	private static int findMax(double[] values, int endIndex) {
		return findMax(values, 0, endIndex);
	}

	private static int findMax(double[] values, int startIndex, int endIndex) {
		if (endIndex > values.length) {
			endIndex = values.length;
		}
		if (startIndex > values.length) {
			startIndex = values.length;
		}
		if (values.length == 0) {
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
