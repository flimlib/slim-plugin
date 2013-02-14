//
// IUIPanelListener.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
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

package loci.slim.histogram;

/**
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public interface IUIPanelListener {
    
    /**
     * User has clicked the auto ranging checkbox.
     * 
     * @param autoRange
     */
    public void setAutoRange(boolean autoRange);
    
    /**
     * User has clicked the exclude pixels checkbox.
     * 
     * @param excludePixels
     */
    public void setExcludePixels(boolean excludePixels);
    
    /**
     * User has clicked the combine channels checkbox.
     */
    public void setCombineChannels(boolean combineChannels);

    /**
     * User has clicked the display channels checkbox.
     * 
     * @param displayChannels 
     */
    public void setDisplayChannels(boolean displayChannels);
	
	/**
	 * User has clicked the logarithmic display checkbox.
	 * 
	 * @param log
	 */
	public void setLogarithmicDisplay(boolean log);

	/**
	 * User has clicked the smoothing checkbox.
	 * 
	 * @param smooth 
	 */
	public void setSmoothing(boolean smooth);

	/**
	 * User has entered new bandwidth (used for smoothing).
	 * 
	 * @param bandwidth 
	 */
	public void setBandwidth(double bandwidth);
	
	/**
	 * User has clicked the family style 1 checkbox.
	 * 
	 * @param on
	 */
	public void setFamilyStyle1(boolean on);
	
	/**
	 * User has clicked the family style 1 checkbox.
	 * 
	 * @param on 
	 */
	public void setFamilyStyle2(boolean on);

    /**
     * User has entered new min/max LUT value.
     *
     * @param min
     * @param max
     */
    public void setMinMaxLUT(double min, double max);
}
