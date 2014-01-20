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

package loci.slim2.process;

/**
 * Horizontal decay cursor.  Divides decay into two regions for fitting.
 * 
 * @author Aivar Grislis
 */
public class HorzCursor {
	private final double timeInc;
	private int bin;
	private double time;

	/**
	 * Constructor, specifies time increment per bin.
	 * 
	 * @param timeInc 
	 */
	public HorzCursor(double timeInc) {
		this.timeInc = timeInc;
	}

	/**
	 * Get bin number.
	 * 
	 * @return 
	 */
	public int getBin() {
		return bin;
	}

	/**
	 * Set bin number.
	 * 
	 * @param bin 
	 */
	public void setBin(int bin) {
		this.bin = bin;
		time = bin * timeInc;
	}

	/**
	 * Get time value.
	 * 
	 * @return 
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Set time value.
	 * 
	 * @param time 
	 */
	public void setTime(double time) {
		this.time = time;
		bin = (int) (time / timeInc);
	}
}
