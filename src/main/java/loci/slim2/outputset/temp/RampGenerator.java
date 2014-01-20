/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.slim2.outputset.temp;

//TODO ARG this class exists only for testing
/**
 *
 * @author Aivar Grislis
 */
public class RampGenerator {
	public enum RampType { UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT, TOP, LEFT, RIGHT, BOTTOM };
	private final RampType rampType;
	private final long width;
	private final long height;
	private final long diagonal;
	
	public RampGenerator(RampType rampType, long width, long height) {
		this.rampType = rampType;
		this.height = height;
		this.width = width;
		this.diagonal = getDiagonal(width, height);
	}
	
	public double getValue(long[] position) {
		return getValue(position[0], position[1]);
	}
	
	public double getValue(long x, long y) {
		long i = 0;
		long j = diagonal;
		switch (rampType) {
			case UPPER_LEFT:
				i = getDiagonal(x, y);
				break;
			case UPPER_RIGHT:
				i = getDiagonal(width - x - 1, y);
				break;
			case LOWER_LEFT:
				i = getDiagonal(x, height - y - 1);
				break;
			case LOWER_RIGHT:
				i = getDiagonal(width - x - 1, height - y - 1);
				break;
			case TOP:
				i = y;
				j = height;
				break;
			case LEFT:
				i = x;
				j = width;
				break;
			case RIGHT:
				i = width - x - 1;
				j = width;
				break;
			case BOTTOM:
				i = height - y - 1;
				j = height;
				break;
		}
		if (RampType.BOTTOM == rampType) { //TODO ARG just checking if LUT range will vary for this plane
			return ((double) i) / (j * 2);
		}
		return ((double) i) / j;
	}
	
	private long getDiagonal(long width, long height) {
		long returnValue = (long) Math.sqrt(width * width + height * height);
		//System.out.println("width " + width + " HEIGHt " + height + " returnValue " + returnValue);
		return returnValue;
	}
}
