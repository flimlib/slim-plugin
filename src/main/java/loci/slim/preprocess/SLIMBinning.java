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

package loci.slim.preprocess;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/process/SLIMBinning.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/process/SLIMBinning.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class SLIMBinning {
    public static final String NONE = "None";
    IndexItem<SLIMBinner, ISLIMBinner> m_plugins[];
    String m_names[];

    public SLIMBinning() {
        // get list of plugins and their names
        List<String> names = new ArrayList<String>();
        List<IndexItem> plugins = new ArrayList<IndexItem>();
        names.add(NONE);
        plugins.add(null);

        // get all matches
        for (final IndexItem<SLIMBinner, ISLIMBinner> item :
                Index.load(SLIMBinner.class, ISLIMBinner.class, IJ.getClassLoader())) {
            plugins.add(item);
            names.add(item.annotation().value());
        }
        m_plugins = plugins.toArray(new IndexItem[0]);
        m_names = names.toArray(new String[0]);
    }

    public String[] getChoices() {
        return m_names;
    }
    
    public ISLIMBinner getBinner(String name) {
        ISLIMBinner instance = null;
        
        IndexItem<SLIMBinner, ISLIMBinner> selectedPlugin = null;
        for (int i = 0; i < m_names.length; ++i) {
            if (name.equals(m_names[i])) {
                selectedPlugin = m_plugins[i];
            }
        }
        
        if (null != selectedPlugin) {
            // create an instance
            try {
                instance = selectedPlugin.instance();
            }
            catch (InstantiationException e) {
                System.out.println("Error instantiating plugin " + e.getMessage());
            }
        }
        
        return instance;
    }
}
