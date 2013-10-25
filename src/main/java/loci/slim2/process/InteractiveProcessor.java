/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2013, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim2.process;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.threshold.ThresholdService;
import imagej.display.DisplayService;
import imagej.ui.UIService;

import loci.slim2.heuristics.Estimator;

import org.scijava.Context;

/**
 * Interface for lifetime processing with interactive UI.
 * 
 * @author Aivar Grislis
 */
public interface InteractiveProcessor {

	/**
	 * Initializes with required services.
	 * 
	 * @param context
	 * @param datasetService
	 * @param displayService
	 * @param uiService
	 * @param estimator
	 */
	public void init(Context context, DatasetService datasetService, DisplayService displayService, Estimator estimator);
	
	/**
	 * Gets current fit settings.
	 * 
	 * @return 
	 */
	public FitSettings getFitSettings();

	/**
	 * Sets fit settings.
	 * 
	 * @param fitSettings 
	 */
	public void setFitSettings(FitSettings fitSettings);

	/**
	 * Processes a {@link Dataset}.
	 * 
	 * @param dataset 
	 * @return whether to quit (true) or load new Dataset (false)
	 */
	public boolean process(Dataset dataset);
}
