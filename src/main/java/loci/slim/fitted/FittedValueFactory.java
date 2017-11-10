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

import java.util.ArrayList;
import java.util.List;

/**
 * Creates FittedValues.
 *
 * @author Aivar Grislis
 */
public class FittedValueFactory {

	/**
	 * Creates array of FittedValue from String descriptor.
	 *
	 */
	public static FittedValue[] createFittedValues(
		final String fittedValueDescriptor, final int components)
	{
		final List<FittedValue> fittedValues = new ArrayList<FittedValue>();
		final String[] descriptors = fittedValueDescriptor.split(" ");
		for (final String descriptor : descriptors) {
			if (FittedValue.CHI_SQ.equals(descriptor)) {
				final ChiSqFittedValue fittedValue = new ChiSqFittedValue();
				fittedValue.init("X2");
				fittedValues.add(fittedValue);
			}
			else if (FittedValue.Z.equals(descriptor)) {
				final ZFittedValue fittedValue = new ZFittedValue();
				fittedValue.init(FittedValue.Z);
				fittedValues.add(fittedValue);
			}
			else if (FittedValue.A.equals(descriptor)) {
				switch (components) {
					case 1: {
						final AFittedValue fittedValue = new AFittedValue();
						fittedValue.init(FittedValue.A, 1);
						fittedValues.add(fittedValue);
						break;
					}
					case 2: {
						final AFittedValue fittedValue1 = new AFittedValue();
						fittedValue1.init(FittedValue.A1, 1);
						fittedValues.add(fittedValue1);
						final AFittedValue fittedValue2 = new AFittedValue();
						fittedValue2.init(FittedValue.A2, 2);
						fittedValues.add(fittedValue2);
						break;
					}
					case 3: {
						final AFittedValue fittedValue1 = new AFittedValue();
						fittedValue1.init(FittedValue.A1, 1);
						fittedValues.add(fittedValue1);
						final AFittedValue fittedValue2 = new AFittedValue();
						fittedValue2.init(FittedValue.A2, 2);
						fittedValues.add(fittedValue2);
						final AFittedValue fittedValue3 = new AFittedValue();
						fittedValue2.init(FittedValue.A3, 3);
						fittedValues.add(fittedValue3);
						break;
					}
				}
			}
			else if (FittedValue.T.equals(descriptor)) {
				switch (components) {
					case 1: {
						final TFittedValue fittedValue = new TFittedValue();
						fittedValue.init("T", 1);
						fittedValues.add(fittedValue);
						break;
					}
					case 2: {
						final TFittedValue fittedValue1 = new TFittedValue();
						fittedValue1.init("T1", 1);
						fittedValues.add(fittedValue1);
						final TFittedValue fittedValue2 = new TFittedValue();
						fittedValue2.init("T2", 2);
						fittedValues.add(fittedValue2);
						break;
					}
					case 3: {
						final TFittedValue fittedValue1 = new TFittedValue();
						fittedValue1.init("T1", 1);
						fittedValues.add(fittedValue1);
						final TFittedValue fittedValue2 = new TFittedValue();
						fittedValue2.init("T2", 2);
						fittedValues.add(fittedValue2);
						final TFittedValue fittedValue3 = new TFittedValue();
						fittedValue2.init("T3", 3);
						fittedValues.add(fittedValue3);
						break;
					}
				}
			}
			else if (FittedValue.F_INT.equals(descriptor)) {
				switch (components) {
					case 1: {
						final FractionalIntensityFittedValue fittedValue =
							new FractionalIntensityFittedValue();
						fittedValue.init(FittedValue.F_INT, 1, components);
						fittedValues.add(fittedValue);
						break;
					}
					case 2: {
						final FractionalIntensityFittedValue fittedValue1 =
							new FractionalIntensityFittedValue();
						fittedValue1.init(FittedValue.F_INT1, 1, components);
						fittedValues.add(fittedValue1);
						final FractionalIntensityFittedValue fittedValue2 =
							new FractionalIntensityFittedValue();
						fittedValue2.init(FittedValue.F_INT2, 2, components);
						fittedValues.add(fittedValue2);
						break;
					}
					case 3: {
						final FractionalIntensityFittedValue fittedValue1 =
							new FractionalIntensityFittedValue();
						fittedValue1.init(FittedValue.F_INT1, 1, components);
						fittedValues.add(fittedValue1);
						final FractionalIntensityFittedValue fittedValue2 =
							new FractionalIntensityFittedValue();
						fittedValue2.init(FittedValue.F_INT2, 2, components);
						fittedValues.add(fittedValue2);
						final FractionalIntensityFittedValue fittedValue3 =
							new FractionalIntensityFittedValue();
						fittedValue2.init(FittedValue.F_INT3, 3, components);
						fittedValues.add(fittedValue3);
						break;
					}
				}
			}
			else if (FittedValue.F_CONT.equals(descriptor)) {
				switch (components) {
					case 1: {
						final FractionalContributionFittedValue fittedValue =
							new FractionalContributionFittedValue();
						fittedValue.init(FittedValue.F_CONT, 1, components);
						fittedValues.add(fittedValue);
						break;
					}
					case 2: {
						final FractionalContributionFittedValue fittedValue1 =
							new FractionalContributionFittedValue();
						fittedValue1.init(FittedValue.F_CONT1, 1, components);
						fittedValues.add(fittedValue1);
						final FractionalContributionFittedValue fittedValue2 =
							new FractionalContributionFittedValue();
						fittedValue2.init(FittedValue.F_CONT2, 2, components);
						fittedValues.add(fittedValue2);
						break;
					}
					case 3: {
						final FractionalContributionFittedValue fittedValue1 =
							new FractionalContributionFittedValue();
						fittedValue1.init(FittedValue.F_CONT1, 1, components);
						fittedValues.add(fittedValue1);
						final FractionalContributionFittedValue fittedValue2 =
							new FractionalContributionFittedValue();
						fittedValue2.init(FittedValue.F_CONT2, 2, components);
						fittedValues.add(fittedValue2);
						final FractionalContributionFittedValue fittedValue3 =
							new FractionalContributionFittedValue();
						fittedValue2.init(FittedValue.F_CONT3, 3, components);
						fittedValues.add(fittedValue3);
						break;
					}
				}
			}
			else if (FittedValue.T_MEAN.equals(descriptor)) {
				final TMeanFittedValue fittedValue = new TMeanFittedValue();
				fittedValue.init("Tm", components);
				fittedValues.add(fittedValue);
			}
		}
		return fittedValues.toArray(new FittedValue[fittedValues.size()]);
	}
}
