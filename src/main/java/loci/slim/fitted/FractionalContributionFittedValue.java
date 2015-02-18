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
public class FractionalContributionFittedValue extends AbstractFittedValue
	implements FittedValue
{

	private int component;
	private int components;

	public void
		init(final String title, final int component, final int components)
	{
		setTitle(title);
		this.component = component;
		this.components = components;
	}

	@Override
	public double getValue(final double[] values) {
		double numerator = 0.0;
		switch (component) {
			case 1:
				numerator = values[FittedValue.A1_INDEX] * values[FittedValue.T1_INDEX];
				break;
			case 2:
				numerator = values[FittedValue.A2_INDEX] * values[FittedValue.T2_INDEX];
				break;
			case 3:
				numerator = values[FittedValue.A3_INDEX] * values[FittedValue.T3_INDEX];
				break;
		}
		double denominator =
			values[FittedValue.A1_INDEX] * values[FittedValue.T1_INDEX];
		if (components > 1) {
			denominator +=
				values[FittedValue.A2_INDEX] * values[FittedValue.T2_INDEX];
		}
		if (components > 2) {
			denominator +=
				values[FittedValue.A3_INDEX] * values[FittedValue.T3_INDEX];
		}
		return numerator / denominator;
	}
}
