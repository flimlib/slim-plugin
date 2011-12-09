//
// ColorBar.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.slim.histogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import ij.process.LUT;

/**
 * Displays a color bar with the current colorization scheme.  Live,
 * reflects ongoing changes.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/colorizer/ColorBar.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/colorizer/ColorBar.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class ColorBarPanel extends JPanel {
    private final Object _synchObject = new Object();
    private int _width;
    private int _height;
    private int _inset;
    private Color[] _color;
    float _start;
    float _stop;
    float _startLUT;
    float _stopLUT;

    /**
     * Constructor
     *
     * @param width
     * @param height
     */
    public ColorBarPanel(int width, int height, int inset) {
        super();
        
        _width = width;
        _height = height;
        _inset = inset;
        
        setPreferredSize(new Dimension(width, height));

        _start = _stop = _startLUT = _stopLUT = 0.0f;
        _start  = 0.0f;
        _stop = 100.0f;
        _startLUT = 25.0f;
        _stopLUT = 75.0f;
    }

    /**
     * Changes the color look-up table and redraws.
     * 
     * @param lut 
     */
    public void setLUT(LUT lut) {
        synchronized (_synchObject) {
            _color = colorsFromLUT(lut);
        }
        repaint();
    }

    /**
     * Changes the values and redraws.
     * 
     * @param start
     * @param stop
     * @param startLUT
     * @param stopLUT 
     */
    public void setStartStop(float start, float stop,
            float startLUT, float stopLUT) {
        synchronized (_synchObject) {
            _start = start;
            _stop = stop;
            _startLUT = startLUT;
            _stopLUT = stopLUT;
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (null != _color) {
            synchronized (_synchObject) {
                for (int i = 0; i < _width - 2 * _inset; ++i) {
                    System.out.println("colorize " + i);
                    g.setColor(colorize(i));
                    g.drawLine(_inset + i, 0, _inset + i, _height-1);
                }
            }
        }
    }
    
    private Color[] colorsFromLUT(LUT lut) {
        byte[] bytes = lut.getBytes();
        int numberColors = bytes.length / 3;
        System.out.println("numberColors " + numberColors + " = bytes " + bytes.length + " / 3");
        //TODO make sure numberColors is 256!
        Color[] color = new Color[numberColors];
        for (int n = 0; n < numberColors; ++n) {
            int red   = 0xff & (int) bytes[n];
            int green = 0xff & (int) bytes[256 + n];
            int blue  = 0xff & (int) bytes[512 + n];
            System.out.println("R " + red + " G " + green + " B " + blue);
            color[n] = new Color(red, green, blue);
        }
        return color;
    }
    
    private Color colorize(int i) {
        Color color = Color.BLACK;
        float value = _start + (_stop - _start) * i / _width;
        if (value >= _startLUT && value <= _stopLUT) {
            int index = (int)((value - _startLUT)
                    * _color.length / (_stopLUT - _startLUT));
            index = Math.max(index, 0);
            index = Math.min(index, _color.length - 1);
            color = _color[index];
        }
        return color;
    }
}
