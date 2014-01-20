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

/**
 * Used to draw large but increasingly smaller "chunky pixels", to provide better 
 * feedback during a slow process.
 * <p>
 * When a "chunky pixel" is drawn oversize only the upper left pixel is drawn
 * with the correct, final value.  All other pixels will be redrawn during
 * processing as further detail is filled in.
 * 
 * @author Aivar Grislis
 */
public class ChunkyPixel {
	private final long[] position;
	private final long width;
	private final long height;

	/**
	 * Constructor.
	 * 
	 * @param position
	 * @param width
	 * @param height 
	 */
	public ChunkyPixel(long[] position, long width, long height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}

	/**
	 * Gets position of upper left pixel.
	 * 
	 * @return 
	 */
	public long[] getPosition() {
		return position;
	}

	/**
	 * Gets width of pixel.
	 * 
	 * @return 
	 */
	public long getWidth() {
		return width;
	}

	/**
	 * Gets height of pixel.
	 * 
	 * @return 
	 */
	public long getHeight() {
		return height;
	}
}
