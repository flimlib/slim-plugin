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

package loci.slim2.process.interactive.ui;

/**
 * Interface for the UI to pick a pixel to fit.  Shows/hides a crosshair cursor.
 * @author Aivar Grislis
 */
public interface PixelPicker {

	/**
	 * Show the cursor at fitted point.
	 * 
	 * @param x
	 * @param y 
	 */
	public void showCursor(int x, int y);

	/**
	 * Hide the cursor.
	 */
	public void hideCursor();
}
