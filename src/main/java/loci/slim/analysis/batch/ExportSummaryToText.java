//
// ExportSummaryToText.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2013, UW-Madison LOCI
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

package loci.slim.analysis.batch;

import loci.slim.analysis.HistogramStatistics;
import loci.slim.analysis.batch.BatchHistogram;
import ij.IJ;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.curvefitter.ICurveFitter;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * Exports a summary histogram in batch mode.
 * 
 * @author Aivar Grislis
 */
public class ExportSummaryToText {
	private ICurveFitter.FitFunction function;
	private List<BatchHistogram> histograms;
	
	public void init(ICurveFitter.FitFunction function) {
		this.function = function;
		histograms = new ArrayList<BatchHistogram>();
		switch (function) {
			case SINGLE_EXPONENTIAL:
				histograms.add(new BatchHistogram("T", 3));
				break;
			case DOUBLE_EXPONENTIAL:
				histograms.add(new BatchHistogram("T1", 3));
				histograms.add(new BatchHistogram("T2", 5));
				break;
			case TRIPLE_EXPONENTIAL:
				histograms.add(new BatchHistogram("T1", 3));
				histograms.add(new BatchHistogram("T2", 5));
				histograms.add(new BatchHistogram("T3", 7));
				break;
			case STRETCHED_EXPONENTIAL:
				break;
		}
	}
	
	public void process(Image<DoubleType> image) {
		int[] dimensions = image.getDimensions();
		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();

		int[] position = new int[dimensions.length];
		for (int y = 0; y < dimensions[1]; ++y) {
			for (int x = 0; x < dimensions[0]; ++x) {
				// set position
				position[0] = x;
				position[1] = y;
				// non-xy dimensions remain at zero
				
				for (BatchHistogram histogram : histograms) {
					int fittedParamIndex = histogram.getFittedParamIndex();
					position[3] = fittedParamIndex;
					
					cursor.setPosition(position);
					double value = cursor.getType().getRealDouble();
					if (!Double.isNaN(value)) {
						histogram.process(value);
					}
				}
			}
		}
	}
	
    public void export(String fileName) {
		BufferedWriter bufferedWriter = null;
		try {
            bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));
        }
		catch (IOException e) {
            IJ.log("exception opening file " + fileName);
            IJ.handleException(e);
        }
		
		if (null != bufferedWriter) {
			try {
				// title this export
				bufferedWriter.write("Export Summary Histogram");
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				
				for (BatchHistogram histogram : histograms) {
					HistogramStatistics statistics = histogram.getStatistics();
					statistics.export(bufferedWriter);
				}
				bufferedWriter.close();
			}
			catch (IOException exception) {
				IJ.log("exception writing to file " + fileName);
				IJ.handleException(exception);
			}
		}
	}
}
