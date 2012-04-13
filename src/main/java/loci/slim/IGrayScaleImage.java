//
// IGrayScaleImage.java
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

package loci.slim;


/**
 * Interface for the GrayScaleImage UI.  Allows user to click on a pixel and
 * set the current channel selection.  Used for thresholding.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/IGrayScaleImage.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/IGrayScaleImage.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public interface IGrayScaleImage extends IGrayScalePixelValue {

    /**
     * Sets a listener for when the user clicks on the image.
     *
     * @param listener
     */
    public void setListener(ISelectListener listener);

    /**
     * Gets the channel slider selection.
     *
     * @return channel
     */
    public int getChannel();

    /**
     * Disables and enables channel selection, during and after a fit.
     *
     * @param enable
     */
    public void enable(boolean enable);

    /**
     * Used to compensate X, Y position when clicking on zoomed image.
     *
     * @return
     */
    public float getZoomFactor();
    
    /**
     * Gets the minimum, non-zero photon count encountered in the image.
     * 
     * Usually 1.0, but sometimes its 10.0 and all photon counts are multiples
     * of 10.0.
     * 
     * @return 
     */
    public double getMinNonZeroPhotonCount();

    /**
     * Gets the coordinates of the brightest point in the image.
     * 
     * @return { x, y }
     */
    public int[] getBrightestPoint();
}
