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

package loci.slim.fitting.config;

import loci.curvefitter.ICurveFitter;
import loci.curvefitter.IFitterEstimator;
import loci.curvefitter.SLIMCurveFitter;
import loci.slim.fitting.callable.FittingEngineCallable;
import loci.slim.fitting.callable.IFittingEngineCallable;
import loci.slim.fitting.engine.IFittingEngine;
import loci.slim.fitting.engine.ThreadedFittingEngine;
import loci.slim.heuristics.FitterEstimator;

/**
 * Handles configuration specific to the SLIM Curve plugin for ImageJ.
 * 
 * A singleton so only one configuration.//TODO
 * 
 * @author Aivar Grislis
 */
public class Configuration extends ConfigurationHelper {
	private static Configuration _instance = null;
	private int _threads = 8;
	private IFittingEngine _fittingEngine;
	private ICurveFitter _curveFitter;
	private IFitterEstimator _cursorEstimator;

	/**
	 * Private constructor for singleton pattern.
	 */
	private Configuration() {
	}

	public static synchronized Configuration getInstance() {
		if (null == _instance) {
			_instance = new Configuration();
		}
		return _instance;
	}

	public int getThreads() {
		return _threads;
	}

	public synchronized IFittingEngine getFittingEngine() {
		if (null == _fittingEngine) {
			_fittingEngine = new ThreadedFittingEngine();
		}
		return _fittingEngine;
	}

	public synchronized ICurveFitter getCurveFitter() {
		if (null == _curveFitter) {
			_curveFitter = new SLIMCurveFitter();
		}
		return _curveFitter;
	}

	public synchronized IFitterEstimator getCursorEstimator() {
		if (null == _cursorEstimator) {
			_cursorEstimator = new FitterEstimator();
		}
		return _cursorEstimator;
	}

	public IFittingEngineCallable newFittingEngineCallable() {
		return new FittingEngineCallable();
	}

}
