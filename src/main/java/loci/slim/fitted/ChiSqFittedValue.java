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
 * Extracts Chi Square fitted values.
 * 
 * @author Aivar Grislis
 */
public class ChiSqFittedValue extends AbstractFittedValue implements FittedValue {

	public void init(String title) {
		setTitle(title);
	}

	@Override
	public double getValue(double[] values) {
		return values[FittedValue.CHI_SQ_INDEX];
	}
}
