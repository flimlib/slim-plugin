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

package loci.slim.fitted;

/**
 * Extracts Fractional Intensity fitted value.
 *
 * @author Aivar Grislis
 */
public class FractionalIntensityFittedValue extends AbstractFittedValue
	implements FittedValue
{

	private int components;
	private int fittedParamIndex;

	public void
		init(final String title, final int component, final int components)
	{
		setTitle(title);
		this.components = components;
		switch (component) {
			case 1:
				fittedParamIndex = FittedValue.A1_INDEX;
				break;
			case 2:
				fittedParamIndex = FittedValue.A2_INDEX;
				break;
			case 3:
				fittedParamIndex = FittedValue.A3_INDEX;
				break;
		}
	}

	@Override
	public double getValue(final double[] values) {
		double sum = values[FittedValue.A1_INDEX];
		if (components > 1) {
			sum += values[FittedValue.A2_INDEX];
		}
		if (components > 2) {
			sum += values[FittedValue.A3_INDEX];
		}
		return values[fittedParamIndex] / sum;
	}
}
