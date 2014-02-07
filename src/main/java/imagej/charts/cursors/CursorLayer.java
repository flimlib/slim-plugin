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

package imagej.charts.cursors;

import imagej.charts.IChartRectangleListener;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

/**
 *
 * @author Aivar Grislis
 */
public class CursorLayer {
	private volatile Rectangle _rectangle = null;
	private JPanel _panel;
	private Set<Cursor> _cursorSet;

	public CursorLayer(JPanel panel) {
		_panel = panel;
		_cursorSet = new HashSet<Cursor>();
	}

	public void addCursor(Cursor cursor) {
		_cursorSet.add(cursor);
	}

	public void removeCursor(Cursor cursor) {
		_cursorSet.remove(cursor);
	}

	public IChartRectangleListener getChartRectangleListener() {
		return new IChartRectangleListener () {
			public void setRectangle(Rectangle rectangle) {
				_rectangle = rectangle;
				for (Cursor cursor : _cursorSet) {
					cursor.setRectangle(rectangle);
				}
				repaint();
			}
		};
	}

	private void repaint() {
		_panel.repaint();
	}


}
