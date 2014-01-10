/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2014, UW-Madison LOCI
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

package loci.slim2;

import imagej.display.Display;
import loci.slim2.decay.LifetimeDatasetWrapper;
import loci.slim2.decay.LifetimeGrayscaleDataset;

/**
 * Keeps track of various entities during the fit process.
 * 
 * @author Aivar Grislis
 */
public class FittingContext {
	private LifetimeDatasetWrapper datasetWrapper;
	private LifetimeGrayscaleDataset grayscaleDataset;
	private Display<?> grayscaleDisplay;
	
	public void setDatasetWrapper(LifetimeDatasetWrapper wrapper) {
		datasetWrapper = wrapper;
	}
	
	public LifetimeDatasetWrapper getDatasetWrapper() {
		return datasetWrapper;
	}
	
	public void setGrayscaleDataset(LifetimeGrayscaleDataset dataset) {
		grayscaleDataset = dataset;
	}
	
	public LifetimeGrayscaleDataset getGrayscaleDataset() {
		return grayscaleDataset;
	}
	
	public void setGrayscaleDisplay(Display<?> display) {
		grayscaleDisplay = display;
	}
	
	public Display<?> getGrayscaleDisplay() {
		return grayscaleDisplay;
	}
}