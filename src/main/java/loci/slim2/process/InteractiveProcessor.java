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

package loci.slim2.process;

import imagej.command.CommandService;
import imagej.data.DatasetService;
import imagej.display.DisplayService;
import loci.slim2.decay.LifetimeDatasetWrapper;
import loci.slim2.heuristics.Estimator;

import org.scijava.Context;

/**
 * Interface for lifetime processing with interactive UI.
 * 
 * @author Aivar Grislis
 */
public interface InteractiveProcessor {

	/**
	 * Initializes with required services.
	 * 
	 * @param context
	 * @param commandService
	 * @param datasetService
	 * @param displayService
	 * @param uiService
	 * @param estimator
	 */
	public void init(Context context, CommandService commandService,
			DatasetService datasetService, DisplayService displayService,
			Estimator estimator);

	/**
	 * Gets current fit settings.
	 * 
	 * @return 
	 */
	public FitSettings getFitSettings();

	/**
	 * Processes a {@link LifetimeDatasetWrapper}.
	 * 
	 * @param lifetime
	 * @return whether to quit (true) or load new lifetime (false)
	 */
	public boolean process(LifetimeDatasetWrapper lifetime);
}
