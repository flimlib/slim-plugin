/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
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
				IJ.log("Error instantiating plugin " + e.getMessage());
			}

			if (null != instance) {
				instance.analyze(image, region, function, parameters);
			}
		}
	}
}
