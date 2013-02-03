/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch;

import loci.slim.analysis.Binning;
import loci.curvefitter.ICurveFitter;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 *
 * @author aivar
 */
public class ExportBatchHistogram {
	private static final int BINS = 10000;
	private static final double TOTAL_MEAN = 2.845409822318876;
	private static final double IN_RANGE_MEAN = 1.8612174728965587;
	private int _paramT = 2;
	private long[] _histoT = new long[BINS];
	private long _histoTUnder = 0;
	private long _histoTOver = 0;
	private double _histoTMax = 10.0;
	private double _histoTMin = 0.0;
	private double totalSum = 0.0;
	private long totalCount = 0;
	private double totalStdDev = 0.0;
	private double sum = 0.0;
	private long count = 0;
	private double stdDev = 0.0;
	
	public void start() {
		double pixels[][] = new double[10][0];
	}
	
    public void export(Image<DoubleType> image,
			ICurveFitter.FitFunction function) {
		int[] dimensions = image.getDimensions();
		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();

		int index = 0;
		int[] position = new int[dimensions.length];
		for (int y = 0; y < dimensions[1]; ++y) {
			for (int x = 0; x < dimensions[0]; ++x) {
				// set position
				position[0] = x;
				position[1] = y;
				position[2] = 1; //TODO ARG hardcoded channel
				position[3] = _paramT;
				cursor.setPosition(position);

				// account for value
				double value = cursor.getType().getRealDouble();
				//System.out.println("value is " + value);
				if (!Double.isNaN(value)) {
					if (value < _histoTMin) {
						++_histoTUnder;
					}
					else if (value > _histoTMax) {
						++_histoTOver;
					}
					else {
						int bin = Binning.valueToBin(BINS, _histoTMin, _histoTMax, value);
						//System.out.println("--> bin " + bin);
						++_histoT[bin];
						stdDev += (IN_RANGE_MEAN - value) * (IN_RANGE_MEAN - value);
						sum += value;
						++count;
					}
				    totalStdDev += (TOTAL_MEAN - value) * (TOTAL_MEAN - value);
					totalSum += value;
					++totalCount;
				}
			}
		}

	}
	
	public void end(String fileName) {
		System.out.println("actual mean in-range is " + sum / count + " count was " + count);
		System.out.println("actual mean total is " + totalSum / totalCount + " totalCount was " + totalCount);
		System.out.println("mean from histo is " + meanFromHisto(_histoT));

		System.out.println("in-range std dev is " + stdDev / count);
		System.out.println("total std dev is " + totalStdDev / totalCount);
		
		System.out.println("std dev from histo is " + standardDeviationFromHisto(_histoT, meanFromHisto(_histoT)));
	}
	
	private double meanFromHisto(long[] histo) {
		double sum = 0.0;
		long count = 0;
		long counter;
		for (int i = 0; i < _histoT.length; ++i) {
			counter = _histoT[i];
			sum += counter * Binning.centerValuesPerBin(BINS, _histoTMin, _histoTMax)[i];
			count += counter;
		}
		System.out.println("histo count is " + count);
		return sum / count;
	}
	
	private double standardDeviationFromHisto(long[] histo, double mean) {
		double sum = 0.0;
		long count = 0;
		long counter;
		for (int i = 0; i < _histoT.length; ++i) {
			counter = _histoT[i];
			double value = Binning.centerValuesPerBin(BINS, _histoTMin, _histoTMax)[i];
			sum += counter * ((mean - value) * (mean - value));
			count += counter;
		}
		return sum / count;
	}
}
