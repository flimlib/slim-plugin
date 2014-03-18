/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
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

package loci.slim.fitting.engine;

import imagej.thread.ThreadPool;

import java.util.ArrayList;
import java.util.List;

import loci.curvefitter.ICurveFitter;
import loci.slim.fitting.callable.IFittingEngineCallable;
import loci.slim.fitting.config.Configuration;
import loci.slim.fitting.params.IFitResults;
import loci.slim.fitting.params.IGlobalFitParams;
import loci.slim.fitting.params.ILocalFitParams;

/**
 * Fitting engine that uses a thread pool.
 * 
 * @author Aivar Grislis
 */
public class ThreadedFittingEngine implements IFittingEngine {
	private static int THREADS = 4;
	private int _threads = THREADS;
	private ThreadPool<IFitResults> _threadPool;
	private ICurveFitter _curveFitter;

	public ThreadedFittingEngine() {
		_threadPool = new ThreadPool<IFitResults>();
	}

	/**
	 * Cancel fit or done fitting.
	 */
	public void shutdown() {
		_threadPool.shutdown();
	}

	/**
	 * Sets number of threads to use.
	 * 
	 * @param threads 
	 */
	public synchronized void setThreads(int threads) {
		_threadPool.setThreads(threads);
	}

	/**
	 * Sets curve fitter to use.
	 * 
	 * @param curve fitter 
	 */
	public synchronized void setCurveFitter(ICurveFitter curveFitter) {
		_curveFitter = curveFitter;
	}

	/**
	 * Fits a single pixel with given parameters.
	 * 
	 * Nothing to parallelize, doesn't use the ThreadPool.
	 * 
	 * @param params
	 * @param data
	 * @return results
	 */
	public synchronized IFitResults fit
			(final IGlobalFitParams params, final ILocalFitParams data) {
		IFittingEngineCallable callable
				= Configuration.getInstance().newFittingEngineCallable();
		callable.setup(_curveFitter, params, data);
		return callable.call();
	}

	/**
	 * Fit one or more pixels with given parameters.
	 * 
	 * @param params given parameters
	 * @param data one or more pixels data
	 * @return results one or more pixels results
	 */
	public synchronized List<IFitResults> fit
			(final IGlobalFitParams params, final List<ILocalFitParams> dataList) {

		List<IFittingEngineCallable> callableList
				= new ArrayList<IFittingEngineCallable>();

		for (ILocalFitParams data : dataList) {
			IFittingEngineCallable callable
					= Configuration.getInstance().newFittingEngineCallable();
			callable.setup(_curveFitter, params, data);
			callableList.add(callable);
		}

		List<IFitResults> resultList = _threadPool.process(callableList);
		return resultList;
	}
}
