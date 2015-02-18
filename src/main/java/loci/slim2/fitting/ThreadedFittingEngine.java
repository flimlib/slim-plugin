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

import imagej.thread.ThreadPool;

import java.util.ArrayList;
import java.util.List;

import loci.curvefitter.ICurveFitter;

/**
 * Fitting engine that uses a thread pool.
 * 
 * @author Aivar Grislis
 */
public class ThreadedFittingEngine implements FittingEngine {
	private ThreadPool<FitResults> threadPool;
	private ICurveFitter curveFitter;

	public ThreadedFittingEngine() {
		threadPool = new ThreadPool<FitResults>();
	}

	@Override
	public void shutdown() {
		threadPool.shutdown();
	}

	@Override
	public synchronized void setThreads(int threads) {
		threadPool.setThreads(threads);
	}

	@Override
	public synchronized void setCurveFitter(ICurveFitter curveFitter) {
		this.curveFitter = curveFitter;
	}

	@Override
	public synchronized FitResults fit
			(final GlobalFitParams params, final LocalFitParams data) {
		FittingCallable callable = new DefaultFittingCallable();
		callable.setup(curveFitter, params, data);
		return callable.call();
	}

	@Override
	public synchronized List<FitResults> fit
			(final GlobalFitParams params, final List<LocalFitParams> dataList) {

		List<FittingCallable> callableList
				= new ArrayList<FittingCallable>();

		for (LocalFitParams data : dataList) {
			FittingCallable callable = new DefaultFittingCallable();
			callable.setup(curveFitter, params, data);
			callableList.add(callable);
		}

		List<FitResults> resultList = threadPool.process(callableList);
		return resultList;
	}
}
