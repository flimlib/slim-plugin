/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package imagej.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Manages a FixedThreadPool to parallelize Callable tasks.  The type parameter
 * T represents the type of the task result.
 * 
 * @author Aivar Grislis
 */
public class ThreadPool<T> {
    private static int _threadPoolCounter = 0;
    private static int THREADS = 4;  
    private int _threads = THREADS;
    private ExecutorService _executorService = null;
    private ThreadPoolThreadFactory _threadFactory
            = new ThreadPoolThreadFactory();
    private int _threadPoolNumber;
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
     * @param threads 
     */
    public synchronized void setThreads(int threads) {
        if (threads != _threads) {
            shutdownExecutorService();
            _threads = threads;
        }
    }

    /**
     * Given a List of Callables defining tasks, execute them in parallel
     * chunks using the thread pool.
     * 
     * @param callableArray array of tasks
     * @return array of results
     */
    public synchronized List<T> process
            (final List<? extends Callable<T>> callableList) {
        // use to build return value array
        List<T> returnList = new ArrayList<T>();
        
        // how many threads needed?
        if (1 == _threads || 1 == callableList.size()) {
            // if single thread sufficient just use current thread
            for (Callable<T> callable : callableList) {
                T result = null;
                try {
                    result = callable.call();
                }
                catch (Exception e) {
                    System.out.println("Exception " + e.getMessage()); //TODO IJ.log it!  
                }
                returnList.add(result);
            }
        }
        else {
            // multiple threads needed
            
            // lazily instantiate ExecutorService
            if (null == _executorService) {
                _executorService
                        = Executors.newFixedThreadPool(_threads, _threadFactory);
            }
           
            // execute given tasks: apportion among threads, wait for completion
            List<Future<T>> futureList = null;
            try {
                
                futureList = _executorService.invokeAll(callableList);
            }
            catch (InterruptedException e) {
                System.out.println("ExecutorService.invokeAll was interrupted " + e.getMessage());
                //TODO just IJ.log it
            }
            
             // get results
            for (Future<T> future: futureList) {
                try {
                    T result = future.get();
                    returnList.add(result);
                }
                catch (ExecutionException e) {
                    System.out.println("ExecutionException " + e.getMessage());
                    //TODO just IJ.log it
                }
                catch (InterruptedException e) {
                    System.out.println("InterruptedException " + e.getMessage());
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
        public Thread newThread(final Runnable r) {
            final String threadName =
                    "ImageJ-" /* + getContext().getID() */
                    + "ThreadPool-" + _threadPoolNumber
                    + "-Thread-" + _threadNumber++;
            //System.out.println("NEW THREAD");
            return new Thread(r, threadName);
        }
    }
}
