/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.slim.fitted;

import loci.slim.fitted.AFittedValue;
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
	 * @param fittedValueDescriptor
	 * @param components
	 * @return 
	 */
	public static FittedValue[] createFittedValues(String fittedValueDescriptor, int components) {
		List<FittedValue> fittedValues = new ArrayList<FittedValue>();
		String[] descriptors = fittedValueDescriptor.split(" ");
		for (String descriptor : descriptors) {
			if (FittedValue.CHI_SQ.equals(descriptor)) {
				ChiSqFittedValue fittedValue = new ChiSqFittedValue();
				fittedValue.init("X2");
				fittedValues.add(fittedValue);
			}
			else if (FittedValue.Z.equals(descriptor)) {
				ZFittedValue fittedValue = new ZFittedValue();
				fittedValue.init(FittedValue.Z);
				fittedValues.add(fittedValue);
			}
			else if (FittedValue.A.equals(descriptor)) {
				switch (components) {
					case 1:
					{
						AFittedValue fittedValue = new AFittedValue();
						fittedValue.init(FittedValue.A, 1);
						fittedValues.add(fittedValue);
						break;
					}
					case 2:
					{
						AFittedValue fittedValue1 = new AFittedValue();
						fittedValue1.init(FittedValue.A1, 1);
						fittedValues.add(fittedValue1);
						AFittedValue fittedValue2 = new AFittedValue();
						fittedValue2.init(FittedValue.A2, 2);
						fittedValues.add(fittedValue2);
						break;
					}
					case 3:
					{
						AFittedValue fittedValue1 = new AFittedValue();
						fittedValue1.init(FittedValue.A1, 1);
						fittedValues.add(fittedValue1);
						AFittedValue fittedValue2 = new AFittedValue();
						fittedValue2.init(FittedValue.A2, 2);
						fittedValues.add(fittedValue2);
						AFittedValue fittedValue3 = new AFittedValue();
						fittedValue2.init(FittedValue.A3, 3);
						fittedValues.add(fittedValue3);
						break;
					}
				}
			}
			else if (FittedValue.T.equals(descriptor)) {
				switch (components) {
					case 1:
					{
						TFittedValue fittedValue = new TFittedValue();
						fittedValue.init("T", 1);
						fittedValues.add(fittedValue);
						break;
					}
					case 2:
					{
						TFittedValue fittedValue1 = new TFittedValue();
						fittedValue1.init("T1", 1);
						fittedValues.add(fittedValue1);
						TFittedValue fittedValue2 = new TFittedValue();
						fittedValue2.init("T2", 2);
						fittedValues.add(fittedValue2);
						break;
					}
					case 3:
					{
						TFittedValue fittedValue1 = new TFittedValue();
						fittedValue1.init("T1", 1);
						fittedValues.add(fittedValue1);
						TFittedValue fittedValue2 = new TFittedValue();
						fittedValue2.init("T2", 2);
						fittedValues.add(fittedValue2);
						TFittedValue fittedValue3 = new TFittedValue();
						fittedValue2.init("T3", 3);
						fittedValues.add(fittedValue3);
						break;
					}
				}
			}
			else if (FittedValue.F_INT.equals(descriptor)) {
				switch (components) {
					case 1:
					{
						FractionalIntensityFittedValue fittedValue = new FractionalIntensityFittedValue();
						fittedValue.init(FittedValue.F_INT, 1, components);
						fittedValues.add(fittedValue);
						break;
					}
					case 2:
					{
						FractionalIntensityFittedValue fittedValue1 = new FractionalIntensityFittedValue();
						fittedValue1.init(FittedValue.F_INT1, 1, components);
						fittedValues.add(fittedValue1);
						FractionalIntensityFittedValue fittedValue2 = new FractionalIntensityFittedValue();
						fittedValue2.init(FittedValue.F_INT2, 2, components);
						fittedValues.add(fittedValue2);
						break;
					}
					case 3:
					{
						FractionalIntensityFittedValue fittedValue1 = new FractionalIntensityFittedValue();
						fittedValue1.init(FittedValue.F_INT1, 1, components);
						fittedValues.add(fittedValue1);
						FractionalIntensityFittedValue fittedValue2 = new FractionalIntensityFittedValue();
						fittedValue2.init(FittedValue.F_INT2, 2, components);
						fittedValues.add(fittedValue2);
						FractionalIntensityFittedValue fittedValue3 = new FractionalIntensityFittedValue();
						fittedValue2.init(FittedValue.F_INT3, 3, components);
						fittedValues.add(fittedValue3);
						break;
					}
				}
			}
			else if (FittedValue.F_CONT.equals(descriptor)) {
				switch (components) {
					case 1:
					{
						FractionalContributionFittedValue fittedValue = new FractionalContributionFittedValue();
						fittedValue.init(FittedValue.F_CONT, 1, components);
						fittedValues.add(fittedValue);
						break;
					}
					case 2:
					{
						FractionalContributionFittedValue fittedValue1 = new FractionalContributionFittedValue();
						fittedValue1.init(FittedValue.F_CONT1, 1, components);
						fittedValues.add(fittedValue1);
						FractionalContributionFittedValue fittedValue2 = new FractionalContributionFittedValue();
						fittedValue2.init(FittedValue.F_CONT2, 2, components);
						fittedValues.add(fittedValue2);
						break;
					}
					case 3:
					{
						FractionalContributionFittedValue fittedValue1 = new FractionalContributionFittedValue();
						fittedValue1.init(FittedValue.F_CONT1, 1, components);
						fittedValues.add(fittedValue1);
						FractionalContributionFittedValue fittedValue2 = new FractionalContributionFittedValue();
						fittedValue2.init(FittedValue.F_CONT2, 2, components);
						fittedValues.add(fittedValue2);
						FractionalContributionFittedValue fittedValue3 = new FractionalContributionFittedValue();
						fittedValue2.init(FittedValue.F_CONT3, 3, components);
						fittedValues.add(fittedValue3);
						break;
					}
				}
			}
			else if (FittedValue.T_MEAN.equals(descriptor)) {
				TMeanFittedValue fittedValue = new TMeanFittedValue();
				fittedValue.init("Tm", components);
				fittedValues.add(fittedValue);
			}
		}
		return fittedValues.toArray(new FittedValue[fittedValues.size()]);
	}
}
