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

package loci.slim.fitting;

import ij.IJ;

/**
 *
 * @author Aivar Grislis
 */
public class DefaultIntegrator implements Integrator {
	public double integrate(double[] function, double offset, double inc, int start, int stop) {
		IJ.log("integrate " + start + " to " + stop);
		// sum heights
		float sum = 0.0f;
		for (int i = start; i < stop; ++i) {
			sum += function[i] - offset;
		}
		// convert to area; constant factor, doesn't affect results that much either way
		return sum * inc;
		//return sum;
	}
}
