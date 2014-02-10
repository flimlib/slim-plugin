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

package loci.slim.fitting;

import ij.IJ;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.ICurveFitter;

/**
 *
 * @author Aivar Grislis
 */
public class RapidLifetimeDetermination {
	public static final int NOISE_CONST        = 0;
	public static final int NOISE_GIVEN        = 1;
	public static final int NOISE_POISSON_DATA = 2;
	public static final int NOISE_POISSON_FIT  = 3;
	public static final int NOISE_GAUSSIAN_FIT = 4;
	public static final int NOISE_MLE          = 5;
	public static final int MAX_REFITS = 10;
	private Integrator integrator = new DefaultIntegrator();

	// SLIMCurve (float -> double)
	/**
	 * Start with an easy one: the three integral method. This returns 0 on
	 * success, negative on error.
	 */
	int gciTripleIntegral(double xincr, double[] y, int fit_start, int fit_end, int noise, double[] sig, double[] z, double[] a, double[] tau, double[] fitted, double[] residuals, double[] chisq, int division) {
		double d1, d2, d3, d12, d23;
		double t0, dt, exp_dt_tau, exp_t0_tau;
		int width;
		double sigma2, res, chisq_local;

		width = (fit_end - fit_start) / division;
		if (width <= 0) {
			return -1;
		}

		t0 = fit_start * xincr;
		dt = width * xincr;

		d1 = d2 = d3 = 0;
		for (int i = fit_start; i < fit_start + width; ++i) {
			d1 += y[i];
		}
		for (int i = fit_start + width; i < fit_start + 2 * width; ++i) {
			d2 += y[i];
		}
		for (int i = fit_start + 2 * width; i < fit_start + 3 * width; ++i) {
			d3 += y[i];
		}

		// those are raw sums, we now convert into areas
		d1 *= xincr;
		d2 *= xincr;
		d3 *= xincr;

		d12 = d1 - d2;
		d23 = d2 - d3;
		if (d12 <= d23 || d23 <= 0) {
			if (d12 <= d23) {
				IJ.log("d12 " + d12 + " <= d23 " + d23);
			}
			if (d23 <= 0) {
				IJ.log("d23 <= 0 " + d23);
			}
			return -2;
		}

		exp_dt_tau = d23 / d12;  /* exp(-dt/tau) */
		tau[0] = -dt / Math.log(exp_dt_tau);
		exp_t0_tau = Math.exp(-t0 / tau[0]);
		a[0] = d12 / (tau[0] * exp_t0_tau * (1 - 2 * exp_dt_tau + exp_dt_tau * exp_dt_tau));
		z[0] = (d1 - a[0] * tau[0] * exp_t0_tau * (1 - exp_dt_tau)) / dt;

		// now calculate the fitted curve and chi-squared if wanted
		for (int i = 0; i < fit_end; ++i) {
			fitted[i] = z[0] * a[0] * Math.exp(-i * xincr / tau[0]);
		}

		// ok, so now fitted contains our data for the timeslice of interest.
		// we can calculate a chisq value and plot the graph, along with the
		// residuals
		chisq_local = 0.0;
		for (int i = 0; i < fit_start; ++i) {
			residuals[i] = y[i] - fitted[i];
		}

		switch (noise) {
			case NOISE_CONST:
				// summation loop over all data
				for (int i = fit_start; i < fit_end; ++i) {
					res = y[i] - fitted[i];
					residuals[i] = res;
					chisq_local += res * res;
				}
				chisq_local /= (sig[0] * sig[0]);
				break;

			case NOISE_GIVEN:
				// summation loop over all data
				for (int i = fit_start; i < fit_end; ++i) {
					res = y[i] - fitted[i];
					residuals[i] = res;
					chisq_local += (res * res) / (sig[i] * sig[i]);
				}
				break;
			case NOISE_POISSON_DATA:
				// summation loop over all data
				for (int i = fit_start; i < fit_end; ++i) {
					res = y[i] - fitted[i];
					residuals[i] = res;
					sigma2 = (y[i] > 1) ? 1.0f / y[i] : 1.0f;
					chisq_local += res * res * sigma2;
				}
				break;
			case NOISE_POISSON_FIT:
			case NOISE_GAUSSIAN_FIT:
			case NOISE_MLE:
				// summation loop over all data
				for (int i = fit_start; i < fit_end; ++i) {
					res = y[i] - fitted[i];
					residuals[i] = res;
					// don't let variance drop below 1
					sigma2 = (fitted[i] > 1) ? 1.0f / fitted[i] : 1.0f;
					chisq_local += res * res * sigma2;
				}
		}

		chisq[0] = chisq_local;

		return 0;
	}

	int gciTripleIntegralInstr(double xincr, double[] y, int fit_start, int fit_end, double[] instr, int ninstr, int noise, double[] sig, double[] z, double[] a, double[] tau, double[] fitted, double[] residuals, double[] chisq, int division) {
		double d1, d2, d3, d12, d23;
		double t0, dt, exp_dt_tau, exp_t0_tau;
		int width;
		double sigma2, res, chisq_local;
		double sum, scaling;
		int fitted_preconv_size = 0;
		double[] fitted_preconv;

		width = (fit_end - fit_start) / division;
		if (width <= 0) {
			return -1;
		}

		t0 = fit_start * xincr;
		dt = width * xincr;

		d1 = d2 = d3 = 0;
		for (int i = fit_start; i < fit_start + width; ++i) {
			d1 += y[i];
		}
		for (int i = fit_start + width; i < fit_start + 2 * width; ++i) {
			d2 += y[i];
		}
		for (int i = fit_start + 2 * width; i < fit_start + 3 * width; ++i) {
			d3 += y[i];
		}

		// those are raw sums, we now convert into areas
		d1 *= xincr;
		d2 *= xincr;
		d3 *= xincr;

		d12 = d1 - d2;
		d23 = d2 - d3;
		if (d12 <= d23 || d23 <= 0) {
			if (d12 <= d23) {
				IJ.log("d12 " + d12 + " <= d23 " + d23);
			}
			if (d23 <= 0) {
				IJ.log("d23 <= 0 " + d23);
			}
			return -2;
		}

		exp_dt_tau = d23 / d12;  /* exp(-dt/tau) */
		tau[0] = -dt / Math.log(exp_dt_tau);
		exp_t0_tau = Math.exp(-t0 / tau[0]);
		a[0] = d12 / (tau[0] * exp_t0_tau * (1 - 2 * exp_dt_tau + exp_dt_tau * exp_dt_tau));
		z[0] = (d1 - a[0] * tau[0] * exp_t0_tau * (1 - exp_dt_tau)) / dt;

		// we now convolve with the instrument response to hopefully get a slightly
		// better fit.  we'll also scale by an appropriate scale factor, which turns
		// to be:
		// sum_{i=0}^{ninstr-1} instr[i] * exp(i*xincr/tau);
		// which should be only a little greater than the sum of the instrument
		// response values

		sum = scaling = 0;
		for (int i = 0; i < ninstr; ++i) {
			sum += instr[i];
			scaling += instr[i] * Math.exp(i * xincr / tau[0]);
		}
		scaling /= sum; // make instrument response sum to 1.0
		a[0] /= scaling;

		fitted_preconv = new double[fit_end];
		fitted_preconv_size = fit_end;

		for (int i = 0; i < fit_end; ++i) {
			fitted_preconv[i] = a[0] * Math.exp(-i * xincr / tau[0]);
		}
		for (int i = 0; i < fit_end; ++i) {
			int convpts;
			// (Zero-basing everything in sight...)
			// We wish to find fitted = fitted_preconv * instr, so explicitly:
			//
			//   fitted[i] = sum_{j=0}^i fitted_preconv[i-j].instr[j]
			//
			// But instr[k]=0 for k >= ninstr, so we only need to sum:
			//
			//   fitted[i] = sum_{j=0}^{min(ninstr-1,i)}
			//   fitted_preconv[i-j].instr[j]
			fitted[i] = 0;
			convpts = (ninstr <= 1) ? ninstr - 1 : i;
			for (int j = 0; j < convpts; ++j) {
				fitted[i] += fitted_preconv[i - j] * instr[j];
			}
			fitted[i] += z[0];
		}

		// ok, so now fitted contains our data for the timeslice of interest.
		// we can calculate a chisq value and plot the graph, along with the
		// residuals
		chisq_local = 0.0f;
		for (int i = 0; i < fit_start; ++i) {
			residuals[i] = y[i] - fitted[i];
		}

		switch (noise) {
			case NOISE_CONST:
				// summation loop over all data
				for (int i = fit_start; i < fit_end; ++i) {
					res = y[i] - fitted[i];
					residuals[i] = res;
					chisq_local += res * res;
				}
				chisq_local /= (sig[0] * sig[0]);
				break;

			case NOISE_GIVEN:
				// summation loop over all data
				for (int i = fit_start; i < fit_end; ++i) {
					res = y[i] - fitted[i];
					residuals[i] = res;
					chisq_local += (res * res) / (sig[i] * sig[i]);
				}
				break;
			case NOISE_POISSON_DATA:
				// summation loop over all data
				for (int i = fit_start; i < fit_end; ++i) {
					res = y[i] - fitted[i];
					residuals[i] = res;
					sigma2 = (y[i] > 1) ? 1.0 / y[i] : 1.0f;
					chisq_local += res * res * sigma2;
				}
				break;
			case NOISE_POISSON_FIT:
			case NOISE_GAUSSIAN_FIT:
			case NOISE_MLE:
				// summation loop over all data
				for (int i = fit_start; i < fit_end; ++i) {
					res = y[i] - fitted[i];
					residuals[i] = res;
					// don't let variance drop below 1
					sigma2 = (fitted[i] > 1) ? 1.0 / fitted[i] : 1.0f;
					chisq_local += res * res * sigma2;
				}
		}
		chisq[0] = chisq_local;

		return 0;
	}

	int gciTripleIntegralFittingEngine(double xincr, double[] y, int fit_start, int fit_end, double[] instr, int ninstr,
		int noise, double[] sig, double[] z, double[] a, double[] tau, double[] fitted, double[] residuals, double[] chisq, double chisq_target) {
		int tries = 1;
		int division = 3;
		double[] local_chisq = new double[] { 3.0e38 };
		double old_chisq = 3.0e38;
		double oldZ;
		double oldA;
		double oldTau;
		int returnCode;

		if (0 == ninstr) {
			returnCode = gciTripleIntegral(xincr, y, fit_start, fit_end, noise, sig, z, a, tau, fitted, residuals, local_chisq, division);
			IJ.log("tries " + tries + " a t z chisq " + a[0] + " " + tau[0] + " " + z[0] + " " + local_chisq[0] + " returns " + returnCode);

			while (local_chisq[0] > chisq_target && local_chisq[0] <= old_chisq && tries < MAX_REFITS) {
				old_chisq = local_chisq[0];
				oldZ = z[0];
				oldA = a[0];
				oldTau = tau[0];
				++division;
				division += division / 3;
				++tries;
				returnCode = gciTripleIntegral(xincr, y, fit_start, fit_end, noise, sig, z, a, tau, fitted, residuals, local_chisq, division);
				IJ.log("tries " + tries + " a t z chisq " + a[0] + " " + tau[0] + " " + z[0] + " " + local_chisq[0] + " returns " + returnCode);
			}
		}
		else {
			returnCode = gciTripleIntegralInstr(xincr, y, fit_start, fit_end, instr, ninstr, noise, sig, z, a, tau, fitted, residuals, local_chisq, division);
			IJ.log("instr tries " + tries + " a t z chisq " + a[0] + " " + tau[0] + " " + z[0] + " " + local_chisq[0] + " returns " + returnCode);

			while (local_chisq[0] > chisq_target && local_chisq[0] <= old_chisq && tries < MAX_REFITS) {
				old_chisq = local_chisq[0];
				oldZ = z[0];
				oldA = a[0];
				oldTau = tau[0];
				++division;
				division += division / 3;
				++tries;
				returnCode = gciTripleIntegralInstr(xincr, y, fit_start, fit_end, instr, ninstr, noise, sig, z, a, tau, fitted, residuals, local_chisq, division);
				IJ.log("instr tries " + tries + " a t z chisq " + a[0] + " " + tau[0] + " " + z[0] + " " + local_chisq[0] + " returns " + returnCode);
			}
		}
		return 0;
	}

	/**
	 * Invokes java version of SLIMCurve C code.
	 * 
	 * @param fitter
	 * @param data
	 * @return 
	 */
	public int rldFit(ICurveFitter fitter, ICurveFitData data) {
		double chiSquareDelta = 0.01;
		double[] chisquare = new double[1];
		double[] z         = new double[1];
		double[] a         = new double[1];
		double[] tau       = new double[1];

		int start = data.getAdjustedDataStartIndex();
		int stop = data.getAdjustedTransEndIndex();
		double[] trans = data.getAdjustedTransient();

		a[0] = fitter.getEstimator().getDefaultA();
		tau[0] = fitter.getEstimator().getDefaultT();
		z[0] = fitter.getEstimator().getDefaultZ();

		// these lines give more TRI2 compatible fit results
		start = fitter.getEstimator().getEstimateStartIndex(trans, start, stop);
		a[0] = fitter.getEstimator().getEstimateAValue(a[0], trans, start, stop);
		// omitted changing the noise model for the RLD estimate
		int chiSquareAdjust = stop - start - 3;

		double xinc = fitter.getXInc();
		int ninstr = 0;
		double[] instrumentResponse = fitter.getInstrumentResponse(1);
		double[] instrumentResponseFloat = null;
		if (null != instrumentResponse) {
			ninstr = instrumentResponse.length;
		}

		double[] fitted = new double[trans.length];
		double[] residuals = new double[trans.length];
		int noise = 3;

		int returnValue = gciTripleIntegralFittingEngine(xinc, trans, start, stop, instrumentResponseFloat, ninstr, noise, null, z, a, tau, fitted, residuals, chisquare, data.getChiSquareTarget() * chiSquareAdjust);
		IJ.log("return triple integral fitting engine " + returnValue);
		return 0;
	}

	// TRIAL CODE

	// Based on:
	// Error Analysis of the Rapid Lifetime Determination Method for Double-Exponential Decays and New Windowing Schemes
	// Kristin K. Sharman and Ammasi Periasamy, et al
	// Analytic Chemistry, Vol. 71, No. 5, Mark 1, 1999

	private int testDoubleRLDFitOverlapping(double offset, double[] decay, double xinc, int start, int stop) {
		int width = stop - start; // start here is exclusive

		// split decay up into fifths
		int width5Divisible = (width / 5) * 5;
		int fifthWidth = width5Divisible / 5;

		IJ.log("width " + width + " width5Divisible " + width5Divisible + " fifthWidth " + fifthWidth);

		// sum photons in each fifth of the range
		double[] fifth = new double[5];
		for (int i = 0; i < 5; ++i) {
			// get total count of photons in this interval
			int startIndex = start + i * fifthWidth;
			fifth[i] = integrator.integrate(decay, offset, xinc, startIndex, startIndex + fifthWidth);
		}
		double sum = 0.0;
		for (int i = 0; i < 5; ++i) {
			IJ.log(" " + fifth[i]);
			sum += fifth[i];
		}
		IJ.log("fifths summed to " + sum);

		sum = 0.0;
		for (int i = start; i < start + width5Divisible; ++i) {
			sum += decay[i] - offset;
		}
		IJ.log("sum from start " + start + " to stop " + (start + width5Divisible) + " is " + sum);

		// uses overlapping gates see Figure 2b of Sharman
		double d0, d1, d2, d3;
		d0 = fifth[0] + fifth[1];
		d1 = fifth[1] + fifth[2];
		d2 = fifth[2] + fifth[3];
		d3 = fifth[3] + fifth[4];

		// compute intermediate calculations (16)-(21)
		double r = d1 * d1 - d2 * d0;
		IJ.log("r " + r + " = d1 " + d1 + " * d1 " + d1 + " - d2 " + d2 + " * d0 " + d0);
		double p = d1 * d2 + d3 * d0;
		IJ.log("p " + p + " = d1 " + d1 + " * d2 " + d2 + " + d3 " + d3 + " * d0 " + d0);
		double q = d2 * d2 - d3 * d1;
		IJ.log("q " + q + " = d2 " + d2 + " * d2 " + d2 + " - d3 " + d3 + " * d1 " + d1);
		double disc = p * p - 4.0 * r * q;
		IJ.log("disc " + disc + " = p " + p + " * p " + p + " - 4.0 * r " + r + " * q " + q);
		double sqrtDisc = Math.sqrt(disc);
		IJ.log("sqrt disc " + sqrtDisc + " = Math.sqrt disc " + disc);
		double x = (-p - sqrtDisc) / (2 * r);
		IJ.log("x " + x + " = (-p " + p + " - sqrtDisc " + sqrtDisc + ") / (2 * r " + r + ")");
		double y = (-p + sqrtDisc) / (2 * r);

		IJ.log("y " + y + " = (-p " + p + " + sqrtDisc " + sqrtDisc + ") / (2 * r " + r + ")");

		double deltaT = 2 * fifthWidth * xinc;
		IJ.log("deltaT " + deltaT + " = 2 * fifthWidth " + fifthWidth + " * xinc " + xinc);

		IJ.log("delta T is " + deltaT + " y squared is " + (y * y) + " x squared is " + (x * x));

		// compute taus (26)-(27)
		double t1 = -deltaT / Math.log(y * y);
		double t2 = -deltaT / Math.log(x * x);
		IJ.log("RLD overlapping T1 is " + t1 + " T2 is " + t2);
		return 0;
	}

	private int testDoubleRLDFit(double offset, double[] decay, double xinc, int start, int stop) {
		int width = stop - start; // start here is exclusive

		// split decay up into fourths
		int width4Divisible = (width / 4) * 4;
		int fourthWidth = width4Divisible / 4;

		IJ.log("width " + width + " width4Divisible " + width4Divisible + " fourthWidth " + fourthWidth);

		// sum photons in each fifth of the range
		double[] fourth = new double[4];
		for (int i = 0; i < 4; ++i) {
			// get total count of photons in this interval
			int startIndex = start + i * fourthWidth;
			fourth[i] = integrator.integrate(decay, offset, xinc, startIndex, startIndex + fourthWidth);
		}
		double sum = 0.0;
		for (int i = 0; i < 4; ++i) {
			sum += fourth[i];
		}
		IJ.log("sum of fourths " + sum);

		sum = 0.0;
		for (int i = start; i < start + width4Divisible; ++i) {
			sum += decay[i] - offset;
		}
		IJ.log("sum from start " + start + " to " + (start + width4Divisible) + " is " + sum);

		// uses non-overlapping gates see Figure 2a of Sharman
		double d0, d1, d2, d3;
		d0 = fourth[0];
		d1 = fourth[1];
		d2 = fourth[2];
		d3 = fourth[3];

		// compute intermediate calculations (16)-(21)
		double r = d1 * d1 - d2 * d0;
		IJ.log("r " + r + " = d1 " + d1 + " * d1 " + d1 + " - d2 " + d2 + " * d0 " + d0);
		double p = d1 * d2 + d3 * d0;
		IJ.log("p " + p + " = d1 " + d1 + " * d2 " + d2 + " + d3 " + d3 + " * d0 " + d0);
		double q = d2 * d2 - d3 * d1;
		IJ.log("q " + q + " = d2 " + d2 + " * d2 " + d2 + " - d3 " + d3 + " * d1 " + d1);
		double disc = p * p - 4.0 * r * q;
		IJ.log("disc " + disc + " = p " + p + " * p " + p + " - 4.0 * r " + r + " * q " + q);
		double sqrtDisc = Math.sqrt(disc);
		IJ.log("sqrt disc " + sqrtDisc + " = Math.sqrt disc " + disc);
		double x = (-p - sqrtDisc) / (2 * r);
		IJ.log("x " + x + " = (-p " + p + " - sqrtDisc " + sqrtDisc + ") / (2 * r " + r + ")");
		double y = (-p + sqrtDisc) / (2 * r);
		if (x <= 0.0f) {
			IJ.log("OOPS x is negative " + x);
		}
		if (y <= 0.0f) {
			IJ.log("OOPS y is negative " + y);
		}

		IJ.log("y " + y + " = (-p " + p + " + sqrtDisc " + sqrtDisc + ") / (2 * r " + r + ")");

		double deltaT = 2 * fourthWidth * xinc;
		IJ.log("deltaT " + deltaT + " = 2 * fourthWidth " + fourthWidth + " * xinc " + xinc);

		// compute taus (22)-(23)
		double t1 = -deltaT / Math.log(y);
		double t2 = -deltaT / Math.log(x);
		IJ.log("RLD T1 is " + t1 + " T2 is " + t2);
		return 0;
	}

	/**
	 * See "4.1 Resolution of a biexponential decay by Prony's method in the time domain", page 5, equations 6..8 of 
	 * _A DSP-based measuring system for temperature compensated fiberoptical oxygen sensors_, Stehing and Holst.
	 * 
	 * @param offset
	 * @param decay
	 * @param xinc
	 * @param start
	 * @param stop
	 * @return 
	 */
	private int testPronysMethod(double offset, double[] decay, double xinc, int start, int stop) {
		int width = stop - start; // start here is exclusive

		// split decay up into fourths
		int width4Divisible = (width / 4) * 4;
		int fourthWidth = width4Divisible / 4;

		IJ.log("width " + width + " width4Divisible " + width4Divisible + " fourthWidth " + fourthWidth);

		double f0 = decay[0] - offset;
		double f1 = decay[fourthWidth] - offset;
		double f2 = decay[2 * fourthWidth] - offset;
		double f3 = decay[3 * fourthWidth] - offset;

		// try  this
		/*f0 = decay[fourthWidth];
		f1 = decay[2 * fourthWidth];
		f2 = decay[3 * fourthWidth];
		f3 = decay[4 * fourthWidth];*/


		double alpha1 = (f3 * f0 - f2 * f1) / (f1 * f1 - f2 * f0);
		double alpha2 = (f3 * f1 - f2 * f2) / (f0 * f2 - f1 * f1); //TODO eq 7 actually has f0 * f2 * f2 - f1 * f1 as the denominator
		//alpha2 = (f3 * f1 - f2 * f2) / (f0 * f2 * f2 - f1 * f1); //TODO, okay try that, to no avail
		double tau1 = -alpha1 / 2 + Math.sqrt((alpha1 * alpha1 / 4 - alpha2));
		double tau2 = -alpha1 / 2 - Math.sqrt((alpha1 * alpha1 / 4 - alpha2));

		IJ.log("f0 " + f0 + " f1 " + f1 + " f2 " + f2 + " f3 " + f3);
		IJ.log("alpha1 " + alpha1 + " alpha2 " + alpha2);
		IJ.log("tau1 " + tau1 + " tau2 " + tau2);
		// typically tau2 and alpha2 come out negative

		return 0;
	}

	public int trialRldFit(ICurveFitter fitter, ICurveFitData data) {
		double chiSquareDelta = 0.01f;
		double[] chisquare = new double[1];
		double[] z         = new double[1];
		double[] a         = new double[1];
		double[] tau       = new double[1];

		int start = data.getAdjustedDataStartIndex();
		int stop = data.getAdjustedTransEndIndex();
		double[] trans = data.getAdjustedTransient();

		a[0] = fitter.getEstimator().getDefaultA();
		tau[0] = fitter.getEstimator().getDefaultT();
		z[0] = fitter.getEstimator().getDefaultZ();

		// these lines give more TRI2 compatible fit results
		start = fitter.getEstimator().getEstimateStartIndex(trans, start, stop);
		a[0] = fitter.getEstimator().getEstimateAValue(a[0], trans, start, stop);
		// omitted changing the noise model for the RLD estimate
		int chiSquareAdjust = stop - start - 3;

		double xinc = fitter.getXInc();
		int ninstr = 0;
		double[] instrumentResponse = fitter.getInstrumentResponse(1);
		if (null != instrumentResponse) {
			ninstr = instrumentResponse.length;
		}

		double[] fitted = new double[trans.length];
		double[] residuals = new double[trans.length];
		int noise = 3;

		double offset = 0.53; // from SPC Image fitting Brian's _40a.sdt at (142, 215)

		testDoubleRLDFitOverlapping(offset, trans, xinc, start, stop);
		IJ.log("*************************************");
		testDoubleRLDFit(offset, trans, xinc, start, stop);
		IJ.log("=====================================");
		testPronysMethod(offset, trans, xinc, start, stop);

		return 0;
	}
}
