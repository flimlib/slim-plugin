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

package loci.slim.analysis.batch;

import loci.slim.fitted.FittedValue;

/**
 * A batch histogram that represents Z baseline values.
 * 
 * @author Aivar Grislis
 */
public class ZBatchHistogram extends AbstractBatchHistogram implements BatchHistogram {

	@Override
	public void init(FittedValue fittedValue) {
		setFittedValue(fittedValue);

		// 200,000 * 24 bytes ~= 4.8Mb memory usage
		int totalBins = 100000;
		setTotalBins(totalBins);

		// bin z values from -50.0..+50.0
		double minRange = -50.0;
		double maxRange = 50.0;
		setMinMaxRange(minRange, maxRange);
	}
}
