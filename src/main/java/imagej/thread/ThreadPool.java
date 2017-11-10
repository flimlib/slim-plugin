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

package imagej.thread;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Manages a FixedThreadPool to parallelize Callable tasks. The type parameter T
 * represents the type of the task result.
 *
 * @author Aivar Grislis
 */
public class ThreadPool<T> {

	private static int _threadPoolCounter = 0;
	private static int THREADS = 4;
	private int _threads = THREADS;
	private ExecutorService _executorService = null;
	private final ThreadPoolThreadFactory _threadFactory =
		new ThreadPoolThreadFactory();
	private final int _threadPoolNumber;
	private int _threadNumber;

	public ThreadPool() {
		_threadPoolNumber = _threadPoolCounter++;
		_threadNumber = 0;
	}

	/**
	 * Shut down the thread pool.
	 */
	public synchronized void shutdown() {
		shutdownExecutorService();
	}

	/**
	 * Sets number of threads to use.
	 *
	 */
	public synchronized void setThreads(final int threads) {
		if (threads != _threads) {
			shutdownExecutorService();
			_threads = threads;
		}
	}

	/**
	 * Given a List of Callables defining tasks, execute them in parallel chunks
	 * using the thread pool.
	 *
	 * @param callableList array of tasks
	 * @return array of results
	 */
	public synchronized List<T> process(
		final List<? extends Callable<T>> callableList)
	{
		// use to build return value array
		final List<T> returnList = new ArrayList<T>();

		// how many threads needed?
		if (1 == _threads || 1 == callableList.size()) {
			// if single thread sufficient just use current thread
			for (final Callable<T> callable : callableList) {
				T result = null;
				try {
					result = callable.call();
				}
				catch (final Exception e) {
					IJ.log("Exception " + e.getMessage()); // TODO IJ.log it!
				}
				returnList.add(result);
			}
		}
		else {
			// multiple threads needed

			// lazily instantiate ExecutorService
			if (null == _executorService) {
				_executorService =
					Executors.newFixedThreadPool(_threads, _threadFactory);
			}

			// execute given tasks: apportion among threads, wait for completion
			List<Future<T>> futureList = null;
			try {

				futureList = _executorService.invokeAll(callableList);
			}
			catch (final InterruptedException e) {
				IJ.log("ExecutorService.invokeAll was interrupted " + e.getMessage());
				// TODO just IJ.log it
			}

			// get results
			for (final Future<T> future : futureList) {
				try {
					final T result = future.get();
					returnList.add(result);
				}
				catch (final ExecutionException e) {
					IJ.log("ExecutionException " + e.getMessage());
					// TODO just IJ.log it
				}
				catch (final InterruptedException e) {
					IJ.log("InterruptedException " + e.getMessage());
					// TODO just IJ.log it
				}
			}
		}
		return returnList;
	}

	/**
	 * Shuts down an existing ExecutorService.
	 */
	private void shutdownExecutorService() {
		if (null != _executorService) {
			_executorService.shutdownNow();
			_executorService = null;
		}
	}

	/**
	 * Thread factory inner class that names the threads.
	 */
	private class ThreadPoolThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(final Runnable r) {
			final String threadName = "ImageJ-" /* + getContext().getID() */
				+ "ThreadPool-" + _threadPoolNumber + "-Thread-" + _threadNumber++;
			// IJ.log("NEW THREAD");
			return new Thread(r, threadName);
		}
	}
}
