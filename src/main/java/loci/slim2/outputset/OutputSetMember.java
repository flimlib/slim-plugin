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

package loci.slim2.outputset;

import java.util.List;

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
