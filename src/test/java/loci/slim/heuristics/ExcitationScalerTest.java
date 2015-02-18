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

package loci.slim.heuristics;

/**
 * Unit test of scaling a prompt to a particular cursor.
 *
 * @author Aivar Grislis
 */
public class ExcitationScalerTest {

	public ExcitationScalerTest() {}

	@org.junit.BeforeClass
	public static void setUpClass() throws Exception {}

	@org.junit.AfterClass
	public static void tearDownClass() throws Exception {}

	@org.junit.Before
	public void setUp() throws Exception {}

	@org.junit.After
	public void tearDown() throws Exception {}

	/**
	 * Test of scale method of class ExcitationScaler.
	 */
	@org.junit.Test
	public void testScale() {
		final double[] decay =
			{ 1.0, 2.0, 1.0, 3.0, 2.0, 2.0, 0.0, 0.0, 0.0, 1.0, 4.0, 2.0, 1.0, 1.0,
				2.0, 1.0, 2.0, 0.0, 1.0, 0.0, 0.0, 2.0, 1.0, 1.0, 2.0, 1.0, 1.0, 5.0,
				9.0, 10.0, 18.0, 17.0, 17.0, 35.0, 37.0, 32.0, 33.0, 28.0, 39.0, 36.0,
				29.0, 32.0, 37.0, 38.0, 27.0, 31.0, 30.0, 32.0, 26.0, 29.0, 25.0, 25.0,
				25.0, 21.0, 35.0, 23.0, 13.0, 15.0, 21.0, 18.0, 8.0, 16.0, 14.0, 20.0,
				12.0, 18.0, 17.0, 17.0, 13.0, 15.0, 14.0, 16.0, 12.0, 18.0, 14.0, 10.0,
				8.0, 10.0, 18.0, 7.0, 10.0, 8.0, 11.0, 11.0, 12.0, 10.0, 13.0, 7.0,
				15.0, 8.0, 6.0, 10.0, 8.0, 7.0, 9.0, 11.0, 15.0, 6.0, 6.0, 10.0, 3.0,
				8.0, 5.0, 7.0, 9.0, 7.0, 5.0, 3.0, 5.0, 4.0, 6.0, 5.0, 6.0, 7.0, 5.0,
				8.0, 3.0, 11.0, 5.0, 5.0, 7.0, 10.0, 3.0, 6.0, 11.0, 5.0, 10.0, 3.0,
				5.0, 4.0, 7.0, 2.0, 3.0, 3.0, 4.0, 4.0, 4.0, 5.0, 9.0, 8.0, 5.0, 7.0,
				5.0, 4.0, 2.0, 9.0, 5.0, 2.0, 3.0, 7.0, 5.0, 4.0, 4.0, 0.0, 3.0, 5.0,
				6.0, 7.0, 2.0, 2.0, 0.0, 5.0, 6.0, 1.0, 7.0, 5.0, 5.0, 1.0, 8.0, 4.0,
				3.0, 7.0, 3.0, 1.0, 3.0, 2.0, 0.0, 2.0, 9.0, 3.0, 3.0, 3.0, 3.0, 0.0,
				3.0, 2.0, 3.0, 4.0, 5.0, 2.0, 1.0, 1.0, 1.0, 2.0, 3.0, 4.0, 2.0, 1.0,
				4.0, 2.0, 3.0, 2.0, 4.0, 1.0, 1.0, 6.0, 1.0, 3.0, 0.0, 2.0, 2.0, 3.0,
				1.0, 0.0, 1.0, 2.0, 1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 0.0, 2.0, 2.0,
				1.0, 0.0, 0.0, 3.0, 3.0, 1.0, 0.0, 2.0, 1.0, 2.0, 2.0, 3.0, 0.0, 2.0,
				1.0, 2.0, 2.0, 2.0, 2.0, 0.0, 4.0, 0.0, 2.0, 2.0, 1.0, 1.0, 1.0, 2.0,
				0.0, 2.0 };
		final int startIndex = 27;
		final int stopIndex = 91;
		final double base = 2.477064220183486;
		final double timeInc = 0.048828125;
		final int bins = 256;
		final double[] expResult =
			{ 0.0023415415006300885, 0.00605394911617452, 0.006982051020060628,
				0.014406866251149491, 0.013478764347263382, 0.013478764347263382,
				0.030184598617213322, 0.03204080242498554, 0.027400292905555,
				0.028328394809441108, 0.023687885290010566, 0.03389700623275775,
				0.03111270052109943, 0.024615987193896675, 0.027400292905555,
				0.03204080242498554, 0.032968904328871645, 0.02275978338612446,
				0.02647219100166889, 0.025544089097782784, 0.027400292905555,
				0.021831681482238352, 0.024615987193896675, 0.020903579578352243,
				0.020903579578352243, 0.020903579578352243, 0.017191171962807814,
				0.030184598617213322, 0.01904737577058003, 0.009766356731718951,
				0.011622560539491168, 0.017191171962807814, 0.014406866251149491,
				0.005125847212288412, 0.012550662443377275, 0.010694458635605059,
				0.016263070058921705, 0.008838254827832842, 0.014406866251149491,
				0.013478764347263382, 0.013478764347263382, 0.009766356731718951,
				0.011622560539491168, 0.010694458635605059, 0.012550662443377275,
				0.008838254827832842, 0.014406866251149491, 0.010694458635605059,
				0.006982051020060628, 0.005125847212288412, 0.006982051020060628,
				0.014406866251149491, 0.004197745308402305, 0.006982051020060628,
				0.005125847212288412, 0.007910152923946735, 0.007910152923946735,
				0.008838254827832842, 0.006982051020060628, 0.009766356731718951,
				0.004197745308402305, 0.011622560539491168, 0.005125847212288412,
				0.0032696434045161966 };
		final double[] result =
			ExcitationScaler.scale(decay, startIndex, stopIndex, base, timeInc, bins);
		TestHelper.assertArrayComparable(expResult, result, 1);
	}
}
