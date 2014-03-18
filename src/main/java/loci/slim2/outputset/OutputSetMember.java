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

package loci.slim2.outputset;

import net.imglib2.RandomAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author Aivar Grislis
 */
	/**
	 * Class that describes a single member of the output set.
	 * 
	 * @param <T> type of the values
	 * 
	 * @author Aivar Grislis
	 */
	public class OutputSetMember <T extends RealType<T> & NativeType<T>> {
		private final int POST_XY_INDEX = 2; //TODO ARG find Y index constant somewhere in Imglib2, + 1; also, is Z s'posed to be 2???
		private final String label;
		private final int index;
		private final MemberFormula formula;
		private boolean combined;
		private RandomAccess<T> randomAccess;

		/**
		 * Constructor.
		 * 
		 * @param label name of this index value
		 * @param index index in output (used for combined images)
		 * @param formula used to derive index value
		 */
		public OutputSetMember(String label, int index, MemberFormula formula) {
			this.label = label;
			this.index = index;
			this.formula = formula;
		}

		public String getLabel() {
			return label;
		}

		public int getIndex() {
			return index;
		}

		public MemberFormula getFormula() {
			return formula;
		}

		public void setCombined(boolean combined) {
			this.combined = combined;
		}

		public void setRandomAccess(RandomAccess<T> randomAccess) {
			this.randomAccess = randomAccess;
		}

		public void setPixelValue(double[] values, long[] position) {
			double value = formula.compute(values);
			setPixelValue(value, position);
		}

		public void setPixelValue(double[] values, long[] position, int[] chunkyPixelSize) {
			double value = formula.compute(values);
			long x = position[0];
			long y = position[1];
			for (int i = 0; i < chunkyPixelSize[0]; ++i) {
				for (int j = 0; j < chunkyPixelSize[1]; ++j) {
					position[0] = x + i;
					position[1] = y + j;
					setPixelValue(value, position);
				}
			}
		}

		public void setPixelValue(double value, long[] position) {
			if (combined) {
				// adjust position for combined stack
				position = expandPosition(position, POST_XY_INDEX);
				position[POST_XY_INDEX] = index;
			}
			randomAccess.setPosition(position);
			randomAccess.get().setReal(value);
		}

		private long[] expandPosition(long[] position, int index) {
			long[] expandedPosition = new long[position.length + 1];
			for (int i = 0; i < expandedPosition.length; ++i) {
				if (i < index) {
					expandedPosition[i] = position[i];
				}
				else {
					expandedPosition[i] = position[i - 1];
				}
			}
			return expandedPosition;
		}
	}
