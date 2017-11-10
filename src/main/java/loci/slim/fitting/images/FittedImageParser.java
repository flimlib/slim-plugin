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

package loci.slim.fitting.images;

import java.util.ArrayList;
import java.util.List;

import loci.slim.fitting.images.FittedImageFitter.FittedImageType;

/**
 * This class parses a string containing a list of output images, such as
 * "A T Z X2" and produces an array of ColorizedImageType.
 *
 * @author Aivar Grislis
 */
public class FittedImageParser {

	private static final Character CHI = '\u03c7';
	private static final Character SQUARE = '\u00b2';
	private static final Character TAU = '\u03c4';
	private static final String TAU_STRING = "" + TAU;
	private static final String CHI_SQ_STRING = "" + CHI + SQUARE;
	private static final String TAU_MEAN_STRING = "" + TAU + "m";
	private static final int Z_INDEX = 0;
	private static final int A1_INDEX = 1;
	private static final int T1_INDEX = 2;
	private static final int A2_INDEX = 3;
	private static final int H_INDEX = 3;
	private static final int T2_INDEX = 4;
	private static final int A3_INDEX = 5;
	private static final int T3_INDEX = 6;
	private static final int MAX_INDEX = 6;
	private final String _input;
	private final int _components;
	private final boolean _stretched;
	private boolean[] _free;

	/**
	 * Creates an instance for a given input string, etc.
	 *
	 * @param input string with fitted images to produce
	 * @param components number of exponential fit components
	 * @param stretched whether it's a stretched exponential
	 * @param free whether each parameter is free or fixed
	 * @param ordinal distinguishes groups of fitted images
	 */
	public FittedImageParser(final String input, final int components,
		final boolean stretched, final boolean[] free)
	{
		_input = input;
		_components = components;
		_stretched = stretched;
		if (null == free) {
			_free = new boolean[MAX_INDEX + 1];
			for (int i = 0; i < _free.length; ++i) {
				_free[i] = true;
			}
		}
		else {
			_free = free;
		}
	}

	/**
	 * Parses the input string and creates array of FittedImageType. Only creates
	 * images which are appropriate for current fit.
	 *
	 */
	public FittedImageType[] getFittedImages() {
		final List<FittedImageType> list = new ArrayList<FittedImageType>();
		final String[] tokens = _input.split(" ");
		for (final String token : tokens) {
			// IJ.log("TOKEN >" + token + "<");
			if ("A".equals(token)) {
				switch (_components) {
					case 1:
						if (_free[A1_INDEX]) {
							list.add(FittedImageType.A1);
						}
						break;
					case 2:
						if (_free[A1_INDEX]) {
							list.add(FittedImageType.A1);
						}
						if (_free[A2_INDEX]) {
							list.add(FittedImageType.A2);
						}
						break;
					case 3:
						if (_free[A1_INDEX]) {
							list.add(FittedImageType.A1);
						}
						if (_free[A2_INDEX]) {
							list.add(FittedImageType.A2);
						}
						if (_free[A3_INDEX]) {
							list.add(FittedImageType.A3);
						}
						break;
				}
			}
			else if ("T".equals(token) || TAU_STRING.equals(token)) {
				switch (_components) {
					case 1:
						if (_free[T1_INDEX]) {
							list.add(FittedImageType.T1);
						}
						break;
					case 2:
						if (_free[T1_INDEX]) {
							list.add(FittedImageType.T1);
						}
						if (_free[T2_INDEX]) {
							list.add(FittedImageType.T2);
						}
						break;
					case 3:
						if (_free[T1_INDEX]) {
							list.add(FittedImageType.T1);
						}
						if (_free[T2_INDEX]) {
							list.add(FittedImageType.T2);
						}
						if (_free[T3_INDEX]) {
							list.add(FittedImageType.T3);
						}
						break;
				}
			}
			else if ("Z".equals(token)) {
				if (_free[Z_INDEX]) {
					list.add(FittedImageType.Z);
				}
			}
			else if ("X2".equals(token) || CHI_SQ_STRING.equals(token)) {
				list.add(FittedImageType.CHISQ);
			}
			else if ("H".equals(token)) {
				if (_stretched) {
					if (_free[H_INDEX]) {
						list.add(FittedImageType.H);
					}
				}
			}
			else if ("F".equals(token)) {
				switch (_components) {
					case 2:
						list.add(FittedImageType.F1);
						list.add(FittedImageType.F2);
						break;
					case 3:
						list.add(FittedImageType.F1);
						list.add(FittedImageType.F2);
						list.add(FittedImageType.F3);
						break;
				}
			}
			else if ("f".equals(token)) {
				switch (_components) {
					case 2:
						list.add(FittedImageType.f1);
						list.add(FittedImageType.f2);
						break;
					case 3:
						list.add(FittedImageType.f1);
						list.add(FittedImageType.f2);
						list.add(FittedImageType.f3);
						break;
				}
			}
			else if ("Tm".equals(token) || TAU_MEAN_STRING.equals(token)) {
				list.add(FittedImageType.Tm);
			}
		}
		return list.toArray(new FittedImageType[0]);
	}
}
