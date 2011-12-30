/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.charts.cursors;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aivar
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
    List<ICursorMoveListener> _moveListeners = new ArrayList<ICursorMoveListener>();
    List<ICursorDragListener> _dragListeners = new ArrayList<ICursorDragListener>();
    List<ICursorStretchListener> _stretchListeners = new ArrayList<ICursorStretchListener>();

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
    }

    public void addMoveListener(ICursorMoveListener listener) {
        synchronized (_moveListeners) {
            _moveListeners.add(listener);
        }
    }

    public void addDragListener(ICursorDragListener listener) {
        synchronized (_dragListeners) {
            _dragListeners.add(listener);
        }
    }

    public void addStretchListener(ICursorStretchListener listener) {
        synchronized (_stretchListeners) {
            _stretchListeners.add(listener);
        }
    }

    public void removeMoveListener(ICursorMoveListener listener) {
        synchronized (_moveListeners) {
            _moveListeners.remove(listener);
        }
    }

    public void removeDragListener(ICursorDragListener listener) {
        synchronized (_dragListeners) {
            _dragListeners.remove(listener);
        }
    }

    public void removeStretchListener(ICursorMoveListener listener) {
        synchronized (_stretchListeners) {
            _stretchListeners.remove(listener);
        }
    }
}
