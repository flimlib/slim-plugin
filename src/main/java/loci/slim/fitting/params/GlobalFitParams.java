/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
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

package loci.slim.fitting.params;

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
public class GlobalFitParams implements IGlobalFitParams {
    private IFitterEstimator _estimator;
    private FitAlgorithm _fitAlgorithm;
    private FitFunction _fitFunction;
    private NoiseModel _noiseModel;
    private double _xInc;
    private double[] _prompt;
    private double _chiSquareTarget;
    private boolean[] _free;
    private int _startPrompt;
    private int _stopPrompt;
    private int _transientStart;
    private int _dataStart;
    private int _transientStop;

    @Override
    public void setEstimator(IFitterEstimator estimator) {
        _estimator = estimator;
    }

    @Override
    public IFitterEstimator getEstimator() {
        return _estimator;
    }
 
    @Override
    public void setFitAlgorithm(FitAlgorithm fitAlgorithm) {
        _fitAlgorithm = fitAlgorithm;
    }
    
    @Override
    public FitAlgorithm getFitAlgorithm() {
        return _fitAlgorithm;
    }
    
    @Override
    public void setFitFunction(FitFunction fitFunction) {
        _fitFunction = fitFunction;
    }
    
    @Override
    public FitFunction getFitFunction() {
        return _fitFunction;
    }

    @Override
    public void setNoiseModel(NoiseModel noiseModel) {
        _noiseModel = noiseModel;
    }

    @Override
    public NoiseModel getNoiseModel() {
        return _noiseModel;
    }
    
    @Override
    public void setXInc(double xInc) {
        _xInc = xInc;
    }
    
    @Override
    public double getXInc() {
        return _xInc;
    }
    
    @Override
    public void setPrompt(double[] prompt) {
        _prompt = prompt;
    }
    
    @Override
    public double[] getPrompt() {
        return _prompt;
    }
    
    @Override
    public void setChiSquareTarget(double chiSquareTarget) {
        _chiSquareTarget = chiSquareTarget;
    }
    
    @Override
    public double getChiSquareTarget() {
        return _chiSquareTarget;
    }
    
    @Override
    public void setFree(boolean[] free) {
        _free = free;
    }
    
    @Override
    public boolean[] getFree() {
        return _free;
    }
    
    @Override
    public void setStartPrompt(int startPrompt) {
        _startPrompt = startPrompt;
    }
    
    @Override
    public int getStartPrompt() {
        return _startPrompt;
    }
    
    @Override
    public void setStopPrompt(int stopPrompt) {
        _stopPrompt = stopPrompt;
    }
    
    @Override
    public int getStopPrompt() {
        return _stopPrompt;
    }
    
    @Override
    public void setTransientStart(int transientStart) {
        _transientStart = transientStart;
    }

    @Override
    public int getTransientStart() {
        return _transientStart;
    }
    
    @Override
    public void setDataStart(int dataStart) {
        _dataStart = dataStart;
    }
    
    @Override
    public int getDataStart() {
        return _dataStart;
    }
    
    @Override
    public void setTransientStop(int transientStop) {
        _transientStop = transientStop;
    }
    
    @Override
    public int getTransientStop() {
        return _transientStop;
    }
}
