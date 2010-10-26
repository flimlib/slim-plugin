//
// DataColorizer.java
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

package loci.slim.colorizer;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/colorizer/DataColorizer.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/colorizer/DataColorizer.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class DataColorizer implements IColorizeRangeListener {
    final Object m_synchObject = new Object();
    int m_width;
    int m_height;
    boolean m_auto;
    double m_start;
    double m_stop;
    double m_max;
    double m_workMax;
    ImagePlus m_imagePlus;
    ImageProcessor m_imageProcessor;
    double m_histogramData[];
    int m_histogramDataIndex;
    double m_imageData[][];
    IColorize m_colorize;
    DataColorizerUI m_ui;

    public DataColorizer(int width, int height, String title) {
        m_width = width;
        m_height = height;
        m_imageProcessor = new ColorProcessor(width, height);
        m_imagePlus = new ImagePlus(title, m_imageProcessor);
        init();
    }

    public DataColorizer(ImagePlus imagePlus) {
        m_imagePlus = imagePlus;
        m_imageProcessor = imagePlus.getProcessor();
        m_width = imagePlus.getWidth();
        m_height = imagePlus.getHeight();
        init();
    }

    private void init() {
        m_imagePlus.show();
        m_histogramData = new double[m_width * m_height];
        m_histogramDataIndex = 0;
        m_imageData = new double[m_width][m_height];
        
        m_auto = true;
        m_start = m_stop = m_max = 0.0;
        m_workMax = 0.0; 
 
        m_colorize = new FiveColorColorize(Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED); //ThreeColorColorize(Color.GREEN, Color.YELLOW, Color.RED);
        m_ui = new DataColorizerUI(m_colorize, this);
    }

    /**
     * During the fit, sets the data for a given x, y and updates
     * the colorized version also.
     *
     * @param firstTime first pixel to represent this data value?
     * @param x
     * @param y
     * @param datum
     */
    public void setData(boolean firstTime, int x, int y, double datum) {
        synchronized (m_synchObject) {
            if (firstTime) {
                // add to histogram
                m_histogramData[m_histogramDataIndex++] = datum;

                // keep track of maximum
                if (datum > m_workMax) {
                    m_workMax = datum;
                }
            }

            // save value for possible recolorization
            m_imageData[x][y] = datum;

            // are we past the initial update cycle?
            if (m_max > 0.0) {
                // show colorized pixel
                m_imageProcessor.setColor(lookUpColor(datum));
                m_imageProcessor.drawPixel(x, y);
            }
        }
    }

    /**
     * During the fit, ets the data for a given x, y and updates
     * the colorized version also.
     *
     * @param x
     * @param y
     * @param value
     */
    public void setData(int x, int y, double value) {
        setData(true, x, y, value);
    }

    /**
     * During the fit, signals the end of an update cycle.  The
     * histogram is recalculated and the colorized version is
     * redrawn as necessary.
     */
    public void update() {
        synchronized (m_synchObject) {
            // did the maximum grow?
            if (m_workMax > m_max) {

                // first update or on automatic
                if (0.0 == m_max || m_auto) {
                    m_max = m_workMax;
                    
                    // handle automatic colorization
                    if (m_auto) {
                        m_stop = m_max;
                    }
                    // recolorize, based on new maximum, colors have shifted
                    recolorize();
                }
                else {
                    m_max = m_workMax;
                }

                //TODO ARG enable this code to show the progression of drawing
                // with each update call.
                //m_imageProcessor = new ColorProcessor(m_width, m_height);
                //m_imagePlus = new ImagePlus("...", m_imageProcessor);

            }
            m_ui.updateData(m_histogramData, m_max);
            m_imagePlus.draw();
        }
    }

    public void setRange(boolean auto, double start, double stop, double max) {
        boolean redo = false;
        if (auto != m_auto) {
            m_auto = auto;
        }
        if (start != m_start) {
            redo = true;
            m_start = start;
        }
        if (stop != m_stop) {
            redo = true;
            m_stop = stop;
        }
       // if (max != m_max) {
       //     redo = true;
       //     m_max = max;
       // }
        if (redo) {
            recolorize();
            m_imagePlus.draw();
        }
    }

    public void quit() {
        m_imagePlus.close();
        m_ui.quit();
    }

    private void recolorize() {
        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                if (m_imageData[x][y] > 0.0) {
                    m_imageProcessor.setColor(lookUpColor(m_imageData[x][y]));
                    m_imageProcessor.drawPixel(x, y);
                }
            }
        }
    }

    private Color lookUpColor(double datum) {
        return m_colorize.colorize(m_start, m_stop, datum);
    }
}
