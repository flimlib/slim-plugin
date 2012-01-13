/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.charts.cursors;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;

import imagej.charts.IChartRectangleListener;

/**
 *
 * @author aivar
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
