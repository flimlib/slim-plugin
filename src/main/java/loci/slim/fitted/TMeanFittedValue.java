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

package loci.slim.fitted;


/**
 * Extracts Tau Mean fitted values.
 * 
 * @author Aivar Grislis
 */
public class TMeanFittedValue extends AbstractFittedValue implements FittedValue {
	private int components;
	
	public void init(String title, int components) {
		setTitle(title);
		this.components = components;
	}

	@Override
	public double getValue(double[] values) {
		double value = 0.0;
		switch (components) {
			case 1:
				value = values[FittedValue.T1_INDEX];
				break;
			case 2:
				value = values[FittedValue.T1_INDEX] + values[FittedValue.T2_INDEX];
				break;
			case 3:
				value = values[FittedValue.T1_INDEX] + values[FittedValue.T2_INDEX] + values[FittedValue.T3_INDEX];
				break;
		}
		return value / components;
	}
}
