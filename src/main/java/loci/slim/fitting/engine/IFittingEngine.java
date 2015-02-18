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

package loci.slim.fitting.engine;

import java.util.List;

import loci.curvefitter.ICurveFitter;
import loci.slim.fitting.params.IFitResults;
import loci.slim.fitting.params.IGlobalFitParams;
import loci.slim.fitting.params.ILocalFitParams;

/**
 * Interface for a fitting engine.
 * 
 * @author Aivar Grislis
 */
public interface IFittingEngine {

	/**
	 * Shuts down the fitting engine.
	 */
	public void shutdown();

	/**
	 * Sets number of threads to use in a fit.
	 * 
	 * @param threads 
	 */
	public void setThreads(int threads);

	/**
	 * Sets the ICurveFitter for the fitting engine to use.
	 * 
	 * @param curveFitter 
	 */
	public void setCurveFitter(ICurveFitter curveFitter);

	/**
	 * Fit one pixel.
	 * 
	 * @param params
	 * @param data
	 * @return fitted results
	 */
	public IFitResults fit(IGlobalFitParams params, ILocalFitParams data);

	/**
	 * Fits a list of pixels.
	 * 
	 * @param params
	 * @param dataList
	 * @return 
	 */
	public List<IFitResults> fit(IGlobalFitParams params, List<ILocalFitParams> dataList);
}
