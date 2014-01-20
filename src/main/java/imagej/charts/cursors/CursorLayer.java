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
import javax.swing.JPanel;

import imagej.charts.IChartRectangleListener;

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
