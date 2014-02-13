/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
				IJ.log("Error instantiating plugin " + e.getMessage());
			}
		}

		return instance;
	}
}
