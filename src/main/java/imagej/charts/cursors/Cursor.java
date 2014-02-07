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

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Aivar Grislis
 */
public class Cursor {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UPPER = 0;
    public static final int LOWER = 1;
    public static final int SINGLE = 1;
    public static final int PAIR = 2;
    public enum Orientation { HORZ, VERT }
    int _ids;
    Orientation _orientation;
    private volatile boolean _dragListening;
    private volatile boolean _moveListening;
    private volatile boolean _stretchListening;
    private Set<ICursorDragListener> _dragListener;
    private Set<ICursorMoveListener> _moveListener;
    private Set<ICursorStretchListener> _stretchListener;
    private Rectangle _rectangle;

    public Cursor() {
        init(1, Orientation.HORZ);
    }

    public Cursor(int ids) {
        init(ids, Orientation.HORZ);
    }

    public Cursor(int ids, Orientation orientation) {
        init(ids, orientation);
    }

    private void init(int ids, Orientation orientation) {
        _ids = ids;
        _dragListening = _moveListening = _stretchListening = false;
        _dragListener = new HashSet<ICursorDragListener>();
        _moveListener = new HashSet<ICursorMoveListener>();
        _stretchListener = new HashSet<ICursorStretchListener>();
        _rectangle = null;
    }

    public void addCursorDragListener(ICursorDragListener dragListener) {
        _dragListener.add(dragListener);
        _dragListening = true;
    }

    public void removeCursorDragListener(ICursorDragListener dragListener) {
        _dragListener.remove(dragListener);
        _dragListening = _dragListener.size() > 0;
    }

    public void addCursorMoveListener(ICursorMoveListener moveListener) {
        _moveListener.add(moveListener);
        _moveListening = true;
    }

    public void removeCursorMoveListener(ICursorMoveListener moveListener) {
        _moveListener.remove(moveListener);
        _moveListening = _moveListener.size() > 0;
    }

    public void addCursorStretchListener(ICursorStretchListener stretchListener) {
        _stretchListener.add(stretchListener);
        _stretchListening = true;
    }

    public void removeCursorStretchListener(ICursorStretchListener stretchListener) {
        _stretchListener.remove(stretchListener);
        _stretchListening = _stretchListener.size() > 0;
    }

    public void setRectangle(Rectangle rectangle) {
        _rectangle = rectangle;
    }

    public boolean mouseDragged(int x, int y) {
        return false;
    }

    public boolean mouseMoved(int x, int y) {
        return false;
    }

}
