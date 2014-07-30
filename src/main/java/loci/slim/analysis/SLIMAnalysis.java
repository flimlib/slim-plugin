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

import java.util.List;

import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Context;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 * This class runs post-fit analysis on the fitted image.
 *
 * @author Aivar Grislis
 * @author Curtis Rueden
 */
public class SLIMAnalysis {

	private final List<PluginInfo<SLIMAnalyzer>> analyzers;
	private final String[] names;

	/**
	 * Constructor, gets list of potential analysis plugins.
	 */
	public SLIMAnalysis() {
		analyzers = pluginService().getPluginsOfType(SLIMAnalyzer.class);

		// build list of names
		names = new String[analyzers.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = analyzers.get(i).getName();
		}
	}

	/**
	 * Returns list of potential analysis plugin names.
	 */
	public String[] getChoices() {
		return names;
	}

	/**
	 * Does image analysis.
	 */
	public void
		doAnalysis(final String name, final ImgPlus<DoubleType> image,
			final FitRegion region, final FitFunction function,
			final String parameters)
	{
		final SLIMAnalyzer analyzer = createAnalyzer(name);
		if (analyzer == null) {
			IJ.error("No such analyzer: " + name);
			return;
		}
		analyzer.analyze(image, region, function, parameters);
	}

	// -- Helper methods --

	private PluginService pluginService() {
		final Context context = (Context) IJ.runPlugIn("org.scijava.Context", "");
		final PluginService pluginService = context.service(PluginService.class);
		return pluginService;
	}

	private SLIMAnalyzer createAnalyzer(final String name) {
		for (final PluginInfo<SLIMAnalyzer> analyzer : analyzers) {
			if (name.equals(analyzer.getName())) {
				return pluginService().createInstance(analyzer);
			}
		}
		return null;
	}

}
