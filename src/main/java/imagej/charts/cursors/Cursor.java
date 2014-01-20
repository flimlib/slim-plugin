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

package imagej.charts.cursors;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    public enum Orientation { HORZ, VERT };
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
