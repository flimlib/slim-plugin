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

package loci.slim;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/DataColorizer.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/DataColorizer.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class DataColorizer {
    final Object m_synchObject = new Object();
    int m_width;
    int m_height;
    ImagePlus m_imagePlus;
    ImageProcessor m_imageProcessor;
    double m_histogramData[];
    int m_histogramDataIndex;
    double m_maxDatum;
    double m_workMaxDatum;
    double m_imageData[][];
    Color m_color1;
    Color m_color2;
    Color m_color3;

    DataColorizer(int width, int height, String title) {
        m_width = width;
        m_height = height;
        m_imageProcessor = new ColorProcessor(width, height);
        m_imagePlus = new ImagePlus(title, m_imageProcessor);
        init();
    }

    DataColorizer(ImagePlus imagePlus) {
        m_imagePlus = imagePlus;
        m_imageProcessor = imagePlus.getProcessor();
        m_width = imagePlus.getWidth();
        m_height = imagePlus.getHeight();
        init();
    }

    private void init() {
        m_histogramData = new double[m_width * m_height];
        m_histogramDataIndex = 0;
        m_maxDatum = m_workMaxDatum = 0.0;
        m_imageData = new double[m_width][m_height];
        m_color1 = Color.BLUE;
        m_color2 = Color.GREEN;
        m_color3 = Color.RED;
    }

    /**
     * Sets the data for a given x, y and updates
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
                if (datum > m_workMaxDatum) {
                    m_workMaxDatum = datum;
                }
            }

            // save value for possible recolorization
            m_imageData[x][y] = datum;

            if (m_maxDatum > 0.0) {
                // show colorized pixel
                m_imageProcessor.setColor(lookUpColor(datum));
                m_imageProcessor.drawPixel(x, y);
            }
        }
    }

    /**
     * Sets the data for a given x, y and updates
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
     * Signals the end of an update cycle.  The histogram is
     * recalculated and the colorized version is redrawn as
     * necessary.
     */
    public void update(boolean force) {
        synchronized (m_synchObject) {
            // did the maximum change?
            if (force || m_workMaxDatum > m_maxDatum) { //TODO only if in automatic mode!
                m_maxDatum = m_workMaxDatum;

                // recolorize, based on new maximum
                for (int y = 0; y < m_height; ++y) {
                    for (int x = 0; x < m_width; ++x) {
                        if (m_imageData[x][y] > 0.0) {
                           m_imageProcessor.setColor(lookUpColor(m_imageData[x][y]));
                           m_imageProcessor.drawPixel(x, y);
                        }
                    }
                }
                m_imagePlus.show();

                //TODO ARG enable this code to show the progression of drawing
                // with each update call.
                // m_imageProcessor = new ColorProcessor(m_width, m_height);
                // m_imagePlus = new ImagePlus("...", m_imageProcessor);

            }
        }
    }

    private Color lookUpColor(double datum) {
        Color returnColor = Color.BLACK;
        if (datum > 0.0) {
            if (datum < m_maxDatum / 2.0) {
                returnColor = interpolateColor(m_color1, m_color2, 2.0 * datum / m_maxDatum);
            }
            else if (datum < m_maxDatum) {
                returnColor = interpolateColor(m_color2, m_color3, 2.0 * (datum - m_maxDatum / 2.0) / m_maxDatum);
            }
            else returnColor = m_color3; //TODO if crush high
        }
        return returnColor;
    }

    private Color interpolateColor(Color start, Color end, double blend) {
        int startRed   = start.getRed();
        int startGreen = start.getGreen();
        int startBlue  = start.getBlue();
        int endRed   = end.getRed();
        int endGreen = end.getGreen();
        int endBlue  = end.getBlue();
        int red   = interpolateColorComponent(startRed, endRed, blend);
        int green = interpolateColorComponent(startGreen, endGreen, blend);
        int blue  = interpolateColorComponent(startBlue, endBlue, blend);
        return new Color(red, green, blue);
    }

    private int interpolateColorComponent(int start, int end, double blend) {
        return (int)(blend * (end - start) + start);
    }


}
