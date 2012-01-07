//
// SLIMAnalysis.java
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

package loci.slim.analysis;

import ij.IJ;
import ij.ImagePlus;

import java.util.ArrayList;
import java.util.List;

import imagej.slim.fitting.FitInfo.FitFunction;
import imagej.slim.fitting.FitInfo.FitRegion;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/analysis/SLIMAnalysis.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/analysis/SLIMAnalysis.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class SLIMAnalysis {
    IndexItem<SLIMAnalyzer, ISLIMAnalyzer> m_plugins[];
    String m_names[];

    public SLIMAnalysis() {
        // get list of plugins and their names
        List<String> names = new ArrayList<String>();
        List<IndexItem> plugins = new ArrayList<IndexItem>();

        // look for matches
        for (final IndexItem<SLIMAnalyzer, ISLIMAnalyzer> item :
                Index.load(SLIMAnalyzer.class, ISLIMAnalyzer.class, IJ.getClassLoader())) {
            plugins.add(item);
            names.add(item.annotation().name());
        }
        m_plugins = plugins.toArray(new IndexItem[0]);
        m_names = names.toArray(new String[0]);
    }

    public String[] getChoices() {
        return m_names;
    }

    public void doAnalysis(String name, Image<DoubleType> image, FitRegion region, FitFunction function) {
        IndexItem<SLIMAnalyzer, ISLIMAnalyzer> selectedPlugin = null;
        for (int i = 0; i < m_names.length; ++i) {
            if (name.equals(m_names[i])) {
                selectedPlugin = m_plugins[i];
            }
        }

        // run selected plugin
        if (null != selectedPlugin) {
            // create an instance
            ISLIMAnalyzer instance = null;
            try {
                instance = selectedPlugin.instance();
            }
            catch (InstantiationException e) {
                System.out.println("Error instantiating plugin " + e.getMessage());
            }

            if (null != instance) {
                instance.analyze(image, region, function);
            }
        }
        else {
            // default behavior
            System.out.println("Default behavior");
        }
    }
}
