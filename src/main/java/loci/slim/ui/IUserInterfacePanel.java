//
// IUserInterfacePanel.java
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

package loci.slim.ui;

import javax.swing.JPanel;

import loci.slim.SLIMProcessor.FitAlgorithm;
import loci.slim.SLIMProcessor.FitFunction;
import loci.slim.SLIMProcessor.FitRegion;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/IUserInterfacePanel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/IUserInterfacePanel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public interface IUserInterfacePanel {

    /**
     * Gets the UI JPanel.
     *
     * @return JPanel
     */
    public JPanel getPanel();

    /**
     * Gets region the fit applies to.
     *
     * @return fit region
     */
    public FitRegion getRegion();

    /**
     * Gets implementation & algorithm for the fit.
     *
     * @return fit algorithm.
     */
    public FitAlgorithm getAlgorithm();

    /**
     * Gets function to be fitted.
     *
     * @return fit function
     */
    public FitFunction getFunction();

    /**
     * Gets starting bin of fit.
     *
     * @return start
     */
    public int getStart();

    /**
     * Gets stopping bin of fit (inclusive).
     *
     * @return stop
     */
    public int getStop();

    /**
     * Gets photon count threshold to fit a pixel.
     *
     * @return threshold
     */
    public int getThreshold();

    /**
     * Gets pixel x.
     *
     * @return x
     */
    public int getX();

    /**
     * Sets pixel x.
     *
     * @param x
     */
    public void setX(int x);

    /**
     * Gets pixel y.
     *
     * @return y
     */
    public int getY();

    /**
     * Sets pixel y.
     *
     * @param y
     */
    public void setY(int y);

    /**
     * Gets number of fit components
     *
     * @return components
     */
    public int getComponents();

    /**
     * Gets the parameters of the fit
     *
     * @return fit parameters
     */
    public double[] getParameters();

    /**
     * Gets which parameters aren't fixed.
     *
     * @return free parameters
     */
    public boolean[] getFree();
}
