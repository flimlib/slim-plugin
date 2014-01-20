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
