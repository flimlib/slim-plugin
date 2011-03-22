//
// VisADAnalysisPlugin.java
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

package loci.slim.analysis.plugins;

import ij.IJ;
import ij.ImagePlus;

// CTR FIXME: remove dependency on ij1bridge
import imagej.ij1bridge.process.OldLegacyImageUtils;

import loci.slim.ui.IUserInterfacePanel.FitFunction;
import loci.slim.ui.IUserInterfacePanel.FitRegion;
import loci.slim.analysis.ISLIMAnalyzer;
import loci.slim.analysis.SLIMAnalyzer;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * Runs the VisADPlugin to analyze SLIMPlugin results.
 *
 * This will run on a separate thread, control returns to the caller right away.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/analysis/plugins/VisADAnalysisPlugin.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/analysis/plugins/VisADAnalysisPlugin.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
@SLIMAnalyzer(name="VisAD")
public class VisADAnalysisPlugin implements ISLIMAnalyzer {
    public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function) {
        ImagePlus imp = OldLegacyImageUtils.createImagePlus(image, "Fitted results");
        IJ.runPlugIn(imp, "imagej.visad.VisADPlugin", "");
    }
}
