/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.charts.cursors;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

import imagej.charts.IChartRectangleListener;

/**
 *
 * @author aivar
 */
public class CursorLayer {
    private volatile Rectangle _rectangle = null;
    private JPanel _panel;
    private List<Cursor> _cursorList;

    public CursorLayer(JPanel panel) {
        _panel = panel;
        _cursorList = new ArrayList<Cursor>();
    }

    public void addCursor(Cursor cursor) {
        _cursorList.add(cursor);
    }

    public IChartRectangleListener getChartRectangleListener() {
        return new IChartRectangleListener () {
            public void setRectangle(Rectangle rectangle) {
                _rectangle = rectangle;
                repaint();
            }
        };
    }

    private void repaint() {
        _panel.repaint();
    }

    
}
