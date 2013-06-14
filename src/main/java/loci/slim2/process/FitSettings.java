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

package loci.slim2.process;

/**
 * Holds basic FLIM fit settings.
 * <p>
 * The intention here is to provide a basic set of fit settings that every
 * FLIM fitting implementation ought to need.  If it turns out some settings are
 * irrelevant to a given fitting implementation they can simply be ignored.
 * 
 * @author Aivar Grislis
 */
public interface FitSettings {

	/**
	 * Get number of bins in time histogram.
	 * 
	 * @return 
	 */
	public int getBins();

	/**
	 * Get time increment per time bin.
	 * 
	 * @return 
	 */
	public double getTimeInc();

	/**
	 * Get excitation decay values.
	 * 
	 * @return 
	 */
	public double[] getExcitation();
	
	/**
	 * Gets horizontal cursor positions on excitation decay.
	 * 
	 * @return null or array of cursors
	 */
	public HorzCursor[] getExcitationHorzCursors();

	/**
	 * Gets vertical cursor position on excitation decay.
	 * 
	 * @return null or cursor
	 */
	public VertCursor getExcitationVertCursor();

	/**
	 * Gets horizontal cursor positions on decay.
	 * 
	 * @return null or array of cursors
	 */
	public HorzCursor[] getDecayHorzCursors();

	/**
	 * Gets vertical cursor position on decay.
	 * 
	 * @return null or cursor
	 */
	public VertCursor getDecayVertCursor();
	
	/**
	 * Gets binning factor.
	 * <p>
	 * 0=no binning, 1=3x3, 2=5x5, etc.
	 * 
	 * @return binning factor
	 */
	public int getBinningFactor();
}
