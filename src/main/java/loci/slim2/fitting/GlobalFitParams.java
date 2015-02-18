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
 * Interface for container for the global fitting parameters, i.e. those valid
 * for the whole image.
 * 
 * @author Aivar Grislis
 */
public interface GlobalFitParams {

	public void setEstimator(IFitterEstimator estimator);

	public IFitterEstimator getEstimator();

	public void setFitAlgorithm(FitAlgorithm fitAlgorithm);

	public FitAlgorithm getFitAlgorithm();

	public void setFitFunction(FitFunction fitFunction);

	public FitFunction getFitFunction();

	public void setNoiseModel(NoiseModel noiseModel);

	public NoiseModel getNoiseModel();

	public void setXInc(double xInc);

	public double getXInc();

	public void setPrompt(double[] prompt);

	public double[] getPrompt();

	public void setChiSquareTarget(double chiSquareTarget);

	public double getChiSquareTarget();

	public void setFree(boolean[] free);

	public boolean[] getFree();

	public void setStartPrompt(int startPrompt);

	public int getStartPrompt();

	public void setStopPrompt(int stopPrompt);

	public int getStopPrompt();

	public int getTransientStart();

	public void setTransientStart(int transientStart);

	public int getDataStart();

	public void setDataStart(int dataStart);

	public int getTransientStop();

	public void setTransientStop(int transientStop);
}

