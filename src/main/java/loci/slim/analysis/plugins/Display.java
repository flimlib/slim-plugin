//
// Display.java
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

package loci.slim.analysis.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.MessageDialog;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import java.awt.Color;

import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.*;

import loci.slim.SLIMProcessor.FitFunction;
import loci.slim.SLIMProcessor.FitRegion;
import loci.slim.analysis.ISLIMAnalyzer;
import loci.slim.analysis.SLIMAnalyzer;
import loci.slim.colorizer.FiveColorColorize;
import loci.slim.colorizer.IColorize;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.container.planar.PlanarContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * A plugin within a plugin, this is used to display the fit results.
 *
 * @author Aivar Grislis
 */
@SLIMAnalyzer(name="Display Fit Results")
public class Display implements ISLIMAnalyzer {
    private static final Character TAU = 'T'; //TODO IJ doesn't handle Unicode, was: = '\u03c4';
    private static final String T1 = "" + TAU + '1';
    private static final String T2 = "" + TAU + '2';
    private static final String T3 = "" + TAU + '3';
    private static final String T1_T2 = "" + TAU + "1/" + TAU + '2';
    private static final String T2_T1 = "" + TAU + "2/" + TAU + '1';
    private static final String T1_T3 = "" + TAU + "1/" + TAU + '3';
    private static final String T3_T1 = "" + TAU + "3/" + TAU + '1';
    private static final String T2_T3 = "" + TAU + "2/" + TAU + '3';
    private static final String T3_T2 = "" + TAU + "3/" + TAU + '2';
    private static final String A1 = "A1";
    private static final String A2 = "A2";
    private static final String A3 = "A3";
    private static final String A1_A2 = "A1/A2";
    private static final String A2_A1 = "A2/A1";
    private static final String A1_A3 = "A1/A3";
    private static final String A3_A1 = "A3/A1";
    private static final String A2_A3 = "A2/A3";
    private static final String A3_A2 = "A3/A2";
    private static final String C = "C";

    private static int TOP_OFFSET = 20;
    private static int SIDE_OFFSET = 10;
    private static int BOTTOM_OFFSET = 20;
    private static int TEXT_OFFSET = 10;

    /**
     * Enum that contains the possible formulas for the values to be displayed.
     */
    private static enum Formula {
        T1_FORMULA(T1, 2),
        T2_FORMULA(T2, 4),
        T3_FORMULA(T3, 6),
        T1_T2_FORMULA(T1_T2, 2, 4), // T1/T2
        T2_T1_FORMULA(T2_T1 ,4, 2),
        T1_T3_FORMULA(T1_T3, 2, 6),
        T3_T1_FORMULA(T3_T1, 6, 2),
        T2_T3_FORMULA(T2_T3, 4, 6),
        T3_T2_FORMULA(T3_T2, 6, 4),
        A1_FORMULA(A1, 1),
        A2_FORMULA(A2, 3),
        A3_FORMULA(A3, 5),
        A1_A2_FORMULA(A1_A2, 1, 3),
        A2_A1_FORMULA(A2_A1, 3, 1),
        A1_A3_FORMULA(A1_A3, 1, 5),
        A3_A1_FORMULA(A3_A1, 5, 1),
        A2_A3_FORMULA(A2_A3, 3, 5),
        A3_A2_FORMULA(A3_A2, 5, 3),
        C_FORMULA(C, 0);

        // This contains the displayable name
        private final String m_name;

        // This contains the indices into the fitted parameters for the formula.
        private final int m_indices[];

        /**
         * Simple formula, just use a given parameter.
         *
         * @param index
         */
        private Formula(String name, int index) {
            m_name = name;
            m_indices = new int[1];
            m_indices[0] = index;
        }

        /**
         * Divisor formula, divide first parameter specified by index by second.
         *
         * @param dividendIndex
         * @param divisorIndex
         */
        private Formula(String name, int dividendIndex, int divisorIndex) {
            m_name = name;
            m_indices = new int[2];
            m_indices[0] = dividendIndex;
            m_indices[1] = divisorIndex;
        }

        private String getName() {
            return m_name;
        }

        private int[] getIndices() {
            return m_indices;
        }
    }

    public Display() {
    }

    public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function) {
        boolean combineMinMax = true;

        // is this plugin appropriate for current data?
        if (FitRegion.EACH != region) {
            // not appropriate
            MessageDialog dialog = new MessageDialog(null, "Display Fit Results", "Requires each pixel be fitted.");
            return;
        }
        int dimensions[] = image.getDimensions();
        for (int i = 0; i < dimensions.length; ++i) {
            System.out.println("dim " + i + " " + dimensions[i]);
        }
        int xIndex = 0;
        int yIndex = 1;
        int cIndex = 2;
        int pIndex = 3;
        int width    = dimensions[xIndex];
        int height   = dimensions[yIndex];
        int channels = dimensions[cIndex];
        int params   = dimensions[pIndex];

        Formula formulas[] = null;
        switch (function) {
            case SINGLE_EXPONENTIAL:
                formulas = new Formula[] { Formula.A1_FORMULA, Formula.T1_FORMULA, Formula.C_FORMULA }; //TODO these three formulas are just for testing.
                break;
            case DOUBLE_EXPONENTIAL:
                formulas = new Formula[] { Formula.A1_A2_FORMULA, Formula.T1_T2_FORMULA };
                break;
            case TRIPLE_EXPONENTIAL:
                formulas = new Formula[] { Formula.T1_T2_FORMULA, Formula.T1_T3_FORMULA };
                break;
            case STRETCHED_EXPONENTIAL:
                break;
        }
        DisplayCell cells[][] = new DisplayCell[channels][formulas.length];

        int cellX = 0, cellY = 0;
        int cellWidth = width + 2 * SIDE_OFFSET;
        int cellHeight = height + TOP_OFFSET + BOTTOM_OFFSET;
        for (int c = 0; c < channels; ++c) {
            cellY = 0;
            for (int f = 0; f < formulas.length; ++f) {
                cells[c][f] = new DisplayCell(formulas[f], cellX, cellY, width, height);
                cellY += cellHeight;
            }
            cellX += cellWidth;
        }
        int totalWidth = cellX;
        int totalHeight = cellY;

        int dim[] = new int[4];
        double paramArray[] = new double[params];
        double minValue, maxValue;
        final LocalizableByDimCursor<?> cursor = image.createLocalizableByDimCursor();
        for (int c = 0; c < channels; ++c) {
            dim[cIndex] = c;
            for (int y = 0; y < height; ++y) {
                dim[yIndex] = y;
                for (int x = 0; x < width; ++x) {
                    dim[xIndex] = x;
                    for (int p = 0; p < params; ++p) {
                        dim[pIndex] = p;
                        cursor.moveTo(dim);
                        paramArray[p] = ((RealType) cursor.getType()).getRealFloat();
                    }
                    minValue = Double.MAX_VALUE;
                    maxValue = 0.0;
                    for (int f = 0; f < formulas.length; ++f) {
                        cells[c][f].calculate(x, y, paramArray);
                        if (combineMinMax) {
                            if (cells[c][f].getMin() < minValue) {
                                minValue = cells[c][f].getMin();
                            }
                            if (cells[c][f].getMax() > maxValue) {
                                maxValue = cells[c][f].getMax();
                            }
                        }
                    }
                    if (combineMinMax) {
                        for (int f = 0; f < formulas.length; ++f) {
                            cells[c][f].setMin(minValue);
                            cells[c][f].setMax(maxValue);
                        }
                    }
                }
            }
        }

        ColorProcessor outputProcessor = new ColorProcessor(totalWidth, totalHeight);
        outputProcessor.setAntialiasedText(true);
        outputProcessor.setColor(Color.BLACK);
        outputProcessor.fill();
        ImagePlus imp = new ImagePlus("Display Results", outputProcessor);

        IColorize colorizer = new FiveColorColorize(Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED);
        for (int c = 0; c < channels; ++c) {
            for (int f = 0; f < formulas.length; ++f) {
                cells[c][f].display(outputProcessor, colorizer);
            }
        }
        imp.show();
    }

    private class DisplayCell {
        Formula m_formula;
        int m_x;
        int m_y;
        int m_width;
        int m_height;
        double m_value[][];
        double m_max = 0.0;
        double m_min = Double.MAX_VALUE;

        private DisplayCell(Formula formula, int x, int y, int width, int height) {
            m_formula = formula;
            m_x = x;
            m_y = y;
            m_width = width;
            m_height = height;
            m_value = new double[width][height];
        }

        private void calculate(int x, int y, double[] parameters) {
            double result = 0.0;
            int indices[] = m_formula.getIndices();
            if (1 == indices.length) {
                result = parameters[indices[0]];
            }
            else {
                result = parameters[indices[0]] / parameters[indices[1]];
            }
            m_value[x][y] = result;
            if (result < m_min) {
                m_min = result;
            }
            if (result > m_max) {
                m_max = result;
            }
        }

        private double getMin() {
            return m_min;
        }

        private void setMin(double min) {
            m_min = min;
        }

        private double getMax() {
            return m_max;
        }

        private void setMax(double max) {
            m_max = max;
        }

        private void display(ColorProcessor processor, IColorize colorize) {
            processor.setColor(Color.WHITE);
            processor.moveTo(m_x + SIDE_OFFSET, m_y + TEXT_OFFSET);
            processor.drawString(m_formula.getName());
            String channelName = "440nm";
            int width = processor.getStringWidth(channelName);
            processor.moveTo(m_x + m_width - width, m_y + TEXT_OFFSET);
            processor.drawString(channelName);
            for (int x = 0; x < m_width; ++x) {
                for (int y = 0; y < m_height; ++y) {
                    processor.setColor(colorize.colorize(m_min, m_max, m_value[x][y]));
                    processor.drawPixel(m_x + SIDE_OFFSET + x, m_y + TOP_OFFSET + y);
                }
            }
        }
    }
}
