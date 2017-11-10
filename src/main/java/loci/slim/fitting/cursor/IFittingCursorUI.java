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

package loci.slim.fitting.cursor;

/**
 * This is an interface to get/set transient and prompt cursors as strings.
 *
 * @author Aivar Grislis
 */
public interface IFittingCursorUI {

	/**
	 * Gets the transient start cursor.
	 *
	 */
	public String getTransientStart();

	/**
	 * Sets the transient start cursor.
	 *
	 */
	public void setTransientStart(String transientStart);

	/**
	 * Gets the data start cursor.
	 * 
	 */
	public String getDataStart();

	/**
	 * Sets the data start cursor.
	 * 
	 */
	public void setDataStart(String dataStart);

	/**
	 * Gets the transient end cursor.
	 *
	 */
	public String getTransientStop();

	/**
	 * Sets the transient end cursor.
	 *
	 */
	public void setTransientStop(String transientStop);

	/**
	 * Gets the prompt delay cursor.
	 *
	 */
	public String getPromptDelay();

	/**
	 * Sets the prompt delay cursor.
	 *
	 */
	public void setPromptDelay(String promptDelay);

	/**
	 * Gets the prompt width cursor.
	 *
	 */
	public String getPromptWidth();

	/**
	 * Sets the prompt width cursor.
	 *
	 */
	public void setPromptWidth(String promptWidth);

	/**
	 * Gets the prompt baseline cursor.
	 *
	 */
	public String getPromptBaseline();

	/**
	 * Sets the prompt baseline cursor.
	 *
	 */
	public void setPromptBaseline(String promptBaseline);
}
