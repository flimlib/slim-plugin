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

package loci.slim.heuristics;

/**
 * This class contains all estimates and rules of thumb.
 *
 * @author Aivar Grislis
 */
public class Estimator implements IEstimator {
	private static final double[] DEFAULT_SINGLE_EXP_PARAMS  = { 0.0, 0.5, 100.0, 0.5 };                      // 0 Z A T
	private static final double[] DEFAULT_DOUBLE_EXP_PARAMS  = { 0.0, 0.5, 50.0, 0.5, 50, 0.25 };             // 0 Z A1 T1 A2 T2
	private static final double[] DEFAULT_TRIPLE_EXP_PARAMS  = { 0.0, 0.5, 40.0, 0.5, 30.0, 0.25, 30, 0.10 }; // 0 Z A1 T1 A2 T2 A3 T3
	private static final double[] DEFAULT_STRETCH_EXP_PARAMS = { 0.0, 0.5, 100.0, 0.5, 0.5 };                 // 0 Z A T H

	@Override
	public int getStart(int bins) {
		return bins / 4;
	}

	@Override
	public int getStop(int bins) {
		return 5 * bins / 6;
	}

	@Override
	public int getThreshold() {
		return 100;
	}

	@Override
	public double getChiSquareTarget() {
		return 1.5;
	}

	@Override
	public double[] getParameters(int components, boolean stretched) {
		double[] parameters;
		if (stretched) {
			// Z T A H
			parameters = DEFAULT_STRETCH_EXP_PARAMS;
		}
		else {
			switch (components) {
				case 1:
					// Z T A
					parameters = DEFAULT_SINGLE_EXP_PARAMS;
					break;
				case 2:
					// Z T1 A1 T2 A2
					parameters = DEFAULT_DOUBLE_EXP_PARAMS;
					break;
				case 3:
				default:
					parameters = DEFAULT_TRIPLE_EXP_PARAMS;
					break;
			}
		}
		return parameters;
	}

}
