/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
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

package loci.slim2.fitting;

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.NoiseModel;
import loci.curvefitter.IFitterEstimator;

/**
 * This contains the global fitting parameters, i.e. those valid for the whole
 * image.
 *
 * @author Aivar Grislis
 */
public class DefaultGlobalFitParams implements GlobalFitParams {

	private IFitterEstimator estimator;
	private FitAlgorithm fitAlgorithm;
	private FitFunction fitFunction;
	private NoiseModel noiseModel;
	private double xInc;
	private double[] prompt;
	private double chiSquareTarget;
	private boolean[] free;
	private int startPrompt;
	private int stopPrompt;
	private int transientStart;
	private int dataStart;
	private int transientStop;

	@Override
	public void setEstimator(final IFitterEstimator estimator) {
		this.estimator = estimator;
	}

	@Override
	public IFitterEstimator getEstimator() {
		return estimator;
	}

	@Override
	public void setFitAlgorithm(final FitAlgorithm fitAlgorithm) {
		this.fitAlgorithm = fitAlgorithm;
	}

	@Override
	public FitAlgorithm getFitAlgorithm() {
		return fitAlgorithm;
	}

	@Override
	public void setFitFunction(final FitFunction fitFunction) {
		this.fitFunction = fitFunction;
	}

	@Override
	public FitFunction getFitFunction() {
		return fitFunction;
	}

	@Override
	public void setNoiseModel(final NoiseModel noiseModel) {
		this.noiseModel = noiseModel;
	}

	@Override
	public NoiseModel getNoiseModel() {
		return noiseModel;
	}

	@Override
	public void setXInc(final double xInc) {
		this.xInc = xInc;
	}

	@Override
	public double getXInc() {
		return xInc;
	}

	@Override
	public void setPrompt(final double[] prompt) {
		this.prompt = prompt;
	}

	@Override
	public double[] getPrompt() {
		return prompt;
	}

	@Override
	public void setChiSquareTarget(final double chiSquareTarget) {
		this.chiSquareTarget = chiSquareTarget;
	}

	@Override
	public double getChiSquareTarget() {
		return chiSquareTarget;
	}

	@Override
	public void setFree(final boolean[] free) {
		this.free = free;
	}

	@Override
	public boolean[] getFree() {
		return free;
	}

	@Override
	public void setStartPrompt(final int startPrompt) {
		this.startPrompt = startPrompt;
	}

	@Override
	public int getStartPrompt() {
		return startPrompt;
	}

	@Override
	public void setStopPrompt(final int stopPrompt) {
		this.stopPrompt = stopPrompt;
	}

	@Override
	public int getStopPrompt() {
		return stopPrompt;
	}

	@Override
	public void setTransientStart(final int transientStart) {
		this.transientStart = transientStart;
	}

	@Override
	public int getTransientStart() {
		return transientStart;
	}

	@Override
	public void setDataStart(final int dataStart) {
		this.dataStart = dataStart;
	}

	@Override
	public int getDataStart() {
		return dataStart;
	}

	@Override
	public void setTransientStop(final int transientStop) {
		this.transientStop = transientStop;
	}

	@Override
	public int getTransientStop() {
		return transientStop;
	}
}
