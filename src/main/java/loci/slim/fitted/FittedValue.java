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
 * Interface for dealing with fitted values.
 * 
 * @author Aivar Grislis
 */
public interface FittedValue {
	
	// Unicode special characters
	public static final Character CHI    = '\u03c7',
	                              SQUARE = '\u00b2',
	                              TAU    = '\u03c4',
	                              LAMBDA = '\u03bb',
	                              SIGMA  = '\u03c3',
	                              SUB_1  = '\u2081',
	                              SUB_2  = '\u2082',
	                              SUB_3  = '\u2083',
	                              SUB_M  = '\u2098', // Unicode 6.0.0 (October 2010)
	                              SUB_R  = '\u1d63';
	
	// labels
	public static final String CHI_SQ = "" + CHI + SQUARE, //TODO actually the reduced chi square: + SUB_R,
						       Z = "Z",
	                           A = "A",
	                           A1 = A + SUB_1,
	                           A2 = A+ SUB_2,
	                           A3 = A+ SUB_3,
	                           T = "" + TAU,
	                           T1 = T + SUB_1,
	                           T2 = T + SUB_2,
	                           T3 = T + SUB_3,
	                           F_INT = "F",
	                           F_INT1 = F_INT + SUB_1,
	                           F_INT2 = F_INT + SUB_2,
	                           F_INT3 = F_INT + SUB_3,
	                           F_CONT = "f",
	                           F_CONT1 = F_CONT + SUB_1,
	                           F_CONT2 = F_CONT + SUB_2,
	                           F_CONT3 = F_CONT + SUB_3,
	                           T_MEAN = "" + TAU + SUB_M;
	
	// indices
	public static final int CHI_SQ_INDEX = 0,
	                        Z_INDEX      = 1,
	                        A1_INDEX     = 2,
	                        T1_INDEX     = 3,
	                        H_INDEX      = 4, // for stretched
	                        A2_INDEX     = 4,
	                        T2_INDEX     = 5,
	                        A3_INDEX     = 6,
	                        T3_INDEX     = 7;
	
	/**
	 * Sets a title for a particular fitted value.
	 * 
	 * @param title 
	 */
	public void setTitle(String title);
	
	/**
	 * Gets the title.
	 * 
	 * @return title
	 */
	public String getTitle();
	
	/**
	 * Gets the fitted value.
	 * 
	 * @param values tuple of fitted parameters
	 * @return fitted value
	 */
	public double getValue(double[] values);
}
