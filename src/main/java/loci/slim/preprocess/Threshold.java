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

package loci.slim.preprocess;

/**
 * This class thresholds the image to a given photon count.
 *
 * @author Aivar Grislis
 */
public class Threshold implements IProcessor {

	private final int _threshold;
	private IProcessor _processor;

	public Threshold(final int threshold) {
		_threshold = threshold;
	}

	/**
	 * Specifies a source IProcessor to be chained to this one.
	 *
	 */
	@Override
	public void chain(final IProcessor processor) {
		_processor = processor;
	}

	/**
	 * Gets input pixel value.
	 *
	 * @return null or pixel value
	 */
	@Override
	public double[] getPixel(final int[] location) {
		double[] decay = _processor.getPixel(location);

		// reject any pixels that have less than the threshold number of photons
		if (null != decay) {
			double sum = 0.0;
			for (int bin = 0; bin < decay.length; ++bin) {
				sum += decay[bin];
			}
			if (sum < _threshold) {
				decay = null;
			}
		}
		return decay;
	}
}
