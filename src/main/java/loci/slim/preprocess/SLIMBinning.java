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

package loci.slim.preprocess;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;

import org.scijava.Context;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

/**
 * Helper class for managing binner plugins.
 *
 * @author Aivar Grislis
 * @author Curtis Rueden
 * @see SLIMBinner
 */
public class SLIMBinning {

	public static final String NONE = "None";

	private final List<PluginInfo<SLIMBinner>> binners;
	private final String[] names;

	public SLIMBinning() {
		final List<PluginInfo<SLIMBinner>> binnerPlugins =
			pluginService().getPluginsOfType(SLIMBinner.class);

		// get list of plugins and their names
		binners = new ArrayList<PluginInfo<SLIMBinner>>();

		// add an initial blank entry
		binners.add(null);

		// get all matches
		for (final PluginInfo<SLIMBinner> info : binnerPlugins) {
			binners.add(info);
		}

		// build list of names
		names = new String[binners.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = getName(binners.get(i));
		}
	}

	// -- SLIMBinning methods --

	public String[] getChoices() {
		return names;
	}

	public SLIMBinner createBinner(final String name) {
		for (final PluginInfo<SLIMBinner> binner : binners) {
			if (name.equals(getName(binner))) {
				return pluginService().createInstance(binner);
			}
		}
		return null;
	}

	// -- Helper methods --

	private PluginService pluginService() {
		final Context context = (Context) IJ.runPlugIn("org.scijava.Context", "");
		final PluginService pluginService = context.service(PluginService.class);
		return pluginService;
	}

	private String getName(final PluginInfo<SLIMBinner> binner) {
		return binner == null ? NONE : binner.getName();
	}

}
