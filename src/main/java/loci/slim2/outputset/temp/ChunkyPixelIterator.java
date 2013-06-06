/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2013, UW-Madison LOCI
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

package loci.slim2.outputset.temp;

import java.util.Iterator;

/**
 * Iterator for progressively rendering images.
 * 
 * @author Aivar Grislis
 */
public class ChunkyPixelIterator implements Iterator<ChunkyPixel> {
	private static final int X_INDEX = 0;
	private static final int Y_INDEX = 1;
	private static final int Z_INDEX = 2;
	private final long[] dimensions;
	private final Chunk[] chunkTable = new Chunk[] {
		// offset, inc, size
		new Chunk(new long[] { 0, 0 }, new long[] { 16, 16 }, new long[] { 16, 16 }), // 16 x 16 size
		new Chunk(new long[] { 8, 0 }, new long[] { 16, 16 }, new long[] {  8, 16 }), // 8 x 16
		new Chunk(new long[] { 0, 8 }, new long[] {  8, 16 }, new long[] {  8,  8 }), // 8 x 8
		new Chunk(new long[] { 4, 0 }, new long[] {  8,  8 }, new long[] {  4,  8 }), // 4 x 8
		new Chunk(new long[] { 0, 4 }, new long[] {  4,  8 }, new long[] {  4,  4 }), // 4 x 4
		new Chunk(new long[] { 2, 0 }, new long[] {  4,  4 }, new long[] {  2,  4 }), // 2 x 4
		new Chunk(new long[] { 0, 2 }, new long[] {  2,  4 }, new long[] {  2,  2 }), // 2 x 2
		new Chunk(new long[] { 1, 0 }, new long[] {  2,  2 }, new long[] {  1,  2 }), // 1 x 2
		new Chunk(new long[] { 0, 1 }, new long[] {  1,  2 }, new long[] {  1,  1 })  // 1 x 1
	};
	private int chunkIndex;
	private ChunkyPixel chunkyPixel;
	private long[] tail;
	private long x;
	private long y;

	/**
	 * Constructor.
	 * 
	 * @param dimensions to iterate over
	 */
	public ChunkyPixelIterator(long[] dimensions) {
		this.dimensions = dimensions;
		x = y = 0;
		chunkIndex = 0;
		tail = new long[dimensions.length - 2];
		for (int i = 0; i < tail.length; ++i) {
			tail[i] = 0;
		}
	}

	@Override
	public boolean hasNext() {
		chunkyPixel = getNext();
		return (null != chunkyPixel);
	}

	@Override
	public ChunkyPixel next() {
		return chunkyPixel;
	}

	@Override
	public void remove() {
		// optional, not implemented
	}

	/**
	 * Gets the next {@link ChunkyPixel}.
	 * 
	 * @return null or next chunky pixel information
	 */
	private ChunkyPixel getNext() {
		if (x >= dimensions[X_INDEX]) {
			// done with row; time for a new one
			x = chunkTable[chunkIndex].offset[X_INDEX];
			y += chunkTable[chunkIndex].inc[Y_INDEX];
			
			if (y >= dimensions[Y_INDEX]) {
				// done with plane at this chunk size; time for a new chunk
				++chunkIndex;
				
				if (chunkIndex >= chunkTable.length) {
					// done with chunk sizes; time for a new plane
					if (!incTail()) {
						return null;
					}
					chunkIndex = 0;
				}
				x = chunkTable[chunkIndex].offset[X_INDEX];
				y = chunkTable[chunkIndex].offset[Y_INDEX];
			}
		}
		// have position and size of next chunky pixel
		long[] position = getPosition(x, y);
		long width  = chunkTable[chunkIndex].size[X_INDEX];
		long height = chunkTable[chunkIndex].size[Y_INDEX];
		ChunkyPixel returnValue = new ChunkyPixel(position, width, height);
		
		// increment x for next time
		x += chunkTable[chunkIndex].inc[X_INDEX];
		
		return returnValue;
	}

	/**
	 * Increments the tail position.
	 * 
	 * @return whether increment possible
	 */
	private boolean incTail() {
		int i = 0;
		while (i < tail.length) {
			if (++tail[i] < dimensions[i + Z_INDEX]) {
				// successful increment
				return true;
			}
			else {
				tail[i] = 0;
				++i;
			}
		}
		// done iterating tail
		return false;
	}

	/**
	 * Gets complete position (adds tail).
	 * 
	 * @param x
	 * @param y
	 * @return 
	 */
	private long[] getPosition(long x, long y) {
		long[] position = new long[dimensions.length];
		position[X_INDEX] = x;
		position[Y_INDEX] = y;
		for (int i = 0; i < tail.length; ++i) {
			position[i + Z_INDEX] = tail[i];
		}
		return position;
	}

	/**
	 * Inner structure-type class used for table-driven approach.
	 */
	private class Chunk {
		public long[] offset;
		public long[] inc;
		public long[] size;
		
		public Chunk(long[] offset, long[] inc, long[] size) {
			this.offset = offset;
			this.inc    = inc;
			this.size   = size;
		}
	}
}
