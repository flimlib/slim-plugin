/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
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

package loci.slim;

import ij.plugin.PlugIn;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.scijava.util.Manifest;
import org.scijava.util.VersionUtils;

/**
 * Displays a small information dialog about the SLIM Curve project.
 *
 * @author Curtis Rueden
 */
public final class About implements PlugIn {

	// -- Constants --

	/** URL of the SLIM Curve library web page. */
	public static final String URL_WEBSITE = "http://slim-curve.github.io/";

	/** URL of SLIM Curve plugin for ImageJ web page. */
	public static final String URL_IMAGEJ = "http://imagej.net/SLIM_Curve";

	// -- PlugIn methods --

	@Override
	public void run(final String arg) {
		about();
	}

	// -- Utility methods --

	public static void about() {
		final List<String> infoItems = new ArrayList<String>();

		// Try to get the version string.
		final String version = VersionUtils.getVersion(SLIM_PlugIn.class);
		if (version != null && !version.isEmpty()) {
			infoItems.add("Version: " + version);
		}

		// Try to get the build date.
		final Manifest m = Manifest.getManifest(SLIM_PlugIn.class);
		final String buildDate = m == null ? null : m.getImplementationDate();
		if (buildDate != null && !buildDate.isEmpty()) {
			infoItems.add("Build date: " + buildDate);
		}

		final StringBuilder sb = new StringBuilder("<html>");
		sb.append("SLIM Curve plugin for ImageJ");
		if (!infoItems.isEmpty()) {
			sb.append("<ul>");
			for (final String item : infoItems)
				sb.append("<li>" + item + "</li>");
			sb.append("</ul>");
		}
		else sb.append("<br>");
		sb.append("Copyright (C) University of Oxford and Board of Regents");
		sb.append("<br>of the University of Wisconsin-Madison.");
		sb.append("<br><br><b>SLIM Curve C library</b>");
		sb.append("<br>Authors: Paul Barber, Aivar Grislis and Curtis Rueden");
		sb.append("<br><i>" + URL_WEBSITE + "</i>");
		sb.append("<br><br><b>SLIM Curve plugin for ImageJ</b>");
		sb.append("<br>Authors: Aivar Grislis, Curtis Rueden and Abdul Kader Sagar");
		sb.append("<br><i>" + URL_IMAGEJ + "</i>");

		final ImageIcon logo =
			new ImageIcon(About.class.getResource("/slim-curve-logo.png"));

		JOptionPane.showMessageDialog(null, sb.toString(),
			"SLIM Curve plugin for ImageJ", JOptionPane.INFORMATION_MESSAGE, logo);
	}

	// -- Main method --

	public static void main(final String[] args) {
		about();
		System.exit(0);
	}

}
