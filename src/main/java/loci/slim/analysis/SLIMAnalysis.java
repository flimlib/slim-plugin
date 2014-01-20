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

package loci.slim.analysis;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;

import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * This class runs post-fit analysis on the fitted image.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/analysis/SLIMAnalysis.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/analysis/SLIMAnalysis.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class SLIMAnalysis {
    IndexItem<SLIMAnalyzer, ISLIMAnalyzer> _plugins[];
    String _names[];

    /**
     * Constructor, gets list of potential analysis plugins.
     * 
     */
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
        _plugins = plugins.toArray(new IndexItem[0]);
        _names = names.toArray(new String[0]);
    }

    /**
     * Returns list of potential analysis plugin names.
     * 
     * @return 
     */
    public String[] getChoices() {
        return _names;
    }

    /**
     * Does image analysis.
     * 
     * @param name
     * @param image
     * @param region
     * @param function
	 * @param parameters
     */
    public void doAnalysis(String name, ImgPlus<DoubleType> image, FitRegion region, FitFunction function, String parameters) {
        
        // find selected plugin
        IndexItem<SLIMAnalyzer, ISLIMAnalyzer> selectedPlugin = null;
        for (int i = 0; i < _names.length; ++i) {
            if (name.equals(_names[i])) {
                selectedPlugin = _plugins[i];
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
                instance.analyze(image, region, function, parameters);
            }
        }
    }
}
