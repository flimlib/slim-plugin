/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim.analysis;

/**
 * Static utility methods to convert from a value to a bin, useful for dealing
 * with histograms and LUTs.
 * 
 * @author Aivar Grislis
 */
public class Binning {
    
    //-- Utility methods --

    /**
     * Convert value to bin number.<p>
     * This variant is inclusive, it assigns all values to the range 0..(bins-1).
     * 
     * @param bins
     * @param min
     * @param max
     * @param value
     * @return bin number 0...(bins-1)
     */
    public static int valueToBin(int bins, double min, double max, double value) {
        int bin = exclusiveValueToBin(bins, min, max, value);
        bin = Math.max(bin, 0);
        bin = Math.min(bin, bins - 1);
        return bin;
    }

    /**
     * Convert value to bin number.<p>
     * This variant is exclusive, not all values map to the range 0...(bins-1).
     * 
     * @param bins
     * @param min
     * @param max
     * @param value
     * @return 
     */
    public static int exclusiveValueToBin(int bins, double min, double max, double value) {
        int bin;
        if (max != min) {
            if (value != max) {
                // convert in-range values to 0.0...1.0
                double temp = (value - min) / (max - min);
                
                // note multiply by bins, not (bins - 1)
                // note floor is needed so that small negative values go to -1
                bin = (int) Math.floor(temp * bins);
            }
            else {
                // value == max, special case, otherwise 1.0 * bins is bins  
                bin = bins - 1;
            }
        }
        else {
            // max == min, degenerate case
            bin = bins / 2;
        }
        return bin;
    }
 
    /**
     * Returns array of left edge values for each bin.
     * 
     * @param bins
     * @param min
     * @param max
     * @return 
     */
    public static double[] edgeValuesPerBin(int bins, double min, double max) {
        double[] edgeValues = new double[bins];
        
        for (int i = 0; i < bins; ++i) {
            edgeValues[i] = min + (max - min) * i / bins;
        }
        return edgeValues;
    }
 
    /**
     * Returns array of center values for each bin.
     * 
     * @param bins
     * @param min
     * @param max
     * @return 
     */
    public static double[] centerValuesPerBin(int bins, double min, double max) {
        double[] edgeValues = edgeValuesPerBin(bins, min, max);
        double[] centerValues = new double[(int) bins];
        
        // average the edge values to get centers
        for (int i = 0; i < bins - 1; ++i) {
            centerValues[i] = (edgeValues[i] + edgeValues[i + 1]) / 2;
        }
        
        // special case for last bin
        centerValues[bins - 1] = (edgeValues[bins - 1] + max) / 2;
        
        return centerValues;
    }
}
