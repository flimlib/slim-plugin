/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
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

package loci.slim;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * A kludge needed because StackWindow has issues.
 *
 * @author Aivar Grislis
 */
public class MyStackWindow extends StackWindow {
	private int m_slice = 1;
	private int m_height;

	public MyStackWindow(ImagePlus imp) {
		super(imp);
		//IJ.log("MyStackWindow " + imp.getTitle());
		m_height = imp.getHeight();
		if (null != sliceSelector) {
			sliceSelector.addAdjustmentListener(
				new AdjustmentListener() {
					@Override
					public void adjustmentValueChanged(AdjustmentEvent e) {
						if (e.getValue() != m_slice) {
							//IJ.log("Show slice " + e.getValue());
							m_slice = e.getValue();
							//TODO this does affect the scrollbar, but not the ImagePlus!
							showSlice(m_slice);
						}
					}
				}
					);
		}
	}

	public MyStackWindow(ImagePlus imp, ImageCanvas ic) {
		super(imp, ic);
	}

	public int getSlice() {
		//TODO this approach did not work; StackWindow doesn't keep slice up to date.
		//return slice;
		return m_slice;
	}

	public float getZoomFactor() {
		return (float) m_height / ic.getHeight();
	}
}
