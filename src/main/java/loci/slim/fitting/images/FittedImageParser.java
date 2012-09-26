//
// FittedImageParser.java
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

package loci.slim.fitting.images;

import loci.slim.fitting.images.FittedImageFitter.FittedImageType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class parses a string containing a list of output images, such as 
 * "A T Z X2" and produces an array of ColorizedImageType.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class FittedImageParser {
    private static final Character CHI    = '\u03c7';
    private static final Character SQUARE = '\u00b2';
    private static final Character TAU    = '\u03c4';
    private static final String TAU_STRING = "" + TAU;
    private static final String CHI_SQ_STRING = "" + CHI + SQUARE;
    private static final String TAU_MEAN_STRING = "" + TAU + "m";
    private static final int Z_INDEX = 0;
    private static final int A1_INDEX = 1;
    private static final int T1_INDEX = 2;
    private static final int A2_INDEX = 3;
    private static final int H_INDEX = 3;
    private static final int T2_INDEX = 4;
    private static final int A3_INDEX = 5;
    private static final int T3_INDEX = 6;
    private static final int MAX_INDEX = 6;
    private String _input;
    private int _components;
    private boolean _stretched;
    private boolean[] _free;

    /**
     * Creates an instance for a given input string, etc.
     * 
     * @param input string with colorized images to produce
     * @param components number of exponential fit components
     * @param stretched whether it's a stretched exponential
     * @param free whether each parameter is free or fixed
     */
    public FittedImageParser(String input, int components, boolean stretched,
            boolean[] free) {
        _input = input;
        _components = components;
        _stretched = stretched;
        if (null == free) {
            _free = new boolean[MAX_INDEX + 1];
            for (int i = 0; i < _free.length; ++i) {
                _free[i] = true;
            }
        }
        else {
            _free = free;
        }
    }

    /**
     * Parses the input string and creates array of ColorizedImageType.  Only
     * creates images which are appropriate for current fit.
     * 
     * @return 
     */
    public FittedImageType[] getColorizedImages() {
        List<FittedImageType> list = new ArrayList<FittedImageType>();
        String[] tokens = _input.split(" ");
        for (String token : tokens) {
            //System.out.println("TOKEN >" + token + "<");
            if ("A".equals(token)) {
                switch (_components) {
                    case 1:
                        if (_free[A1_INDEX]) {
                            list.add(FittedImageType.A1);
                        }
                        break;
                    case 2:
                        if (_free[A1_INDEX]) {
                            list.add(FittedImageType.A1);
                        }
                        if (_free[A2_INDEX]) {
                            list.add(FittedImageType.A2);
                        }
                        break;
                    case 3:
                        if (_free[A1_INDEX]) {
                            list.add(FittedImageType.A1);
                        }
                        if (_free[A2_INDEX]) {
                            list.add(FittedImageType.A2);
                        }
                        if (_free[A3_INDEX]) {
                            list.add(FittedImageType.A3);
                        }
                        break;
               }
            }
            else if ("T".equals(token) || TAU_STRING.equals(token)) {
                switch (_components) {
                    case 1:
                        if (_free[T1_INDEX]) {
                            list.add(FittedImageType.T1);
                        }
                        break;
                    case 2:
                        if (_free[T1_INDEX]) {
                            list.add(FittedImageType.T1);
                        }
                        if (_free[T2_INDEX]) {
                            list.add(FittedImageType.T2);
                        }
                        break;
                    case 3:
                        if (_free[T1_INDEX]) {
                            list.add(FittedImageType.T1);
                        }
                        if (_free[T2_INDEX]) {
                            list.add(FittedImageType.T2);
                        }
                        if (_free[T3_INDEX]) {
                            list.add(FittedImageType.T3);
                        }
                        break;
               }
            }
            else if ("Z".equals(token)) {
                if (_free[Z_INDEX]) {
                    list.add(FittedImageType.Z);
                }
            }
            else if ("X2".equals(token) || CHI_SQ_STRING.equals(token)) {
                list.add(FittedImageType.CHISQ);
            }
            else if ("H".equals(token)) {
                if (_stretched) {
                    if (_free[H_INDEX]) {
                        list.add(FittedImageType.H);
                    }
                }
            }
            else if ("F".equals(token)) {
                switch (_components) {
                    case 2:
                        list.add(FittedImageType.F1);
                        list.add(FittedImageType.F2);
                        break;
                    case 3:
                        list.add(FittedImageType.F1);
                        list.add(FittedImageType.F2);
                        list.add(FittedImageType.F3);
                        break;
                }
            }
            else if ("f".equals(token)) {
                switch (_components) {
                    case 2:
                        list.add(FittedImageType.f1);
                        list.add(FittedImageType.f2);
                        break;
                    case 3:
                        list.add(FittedImageType.f1);
                        list.add(FittedImageType.f2);
                        list.add(FittedImageType.f3);
                        break;
                }
            }
            else if ("Tm".equals(token) || TAU_MEAN_STRING.equals(token)) {
                list.add(FittedImageType.Tm);
            }
        }
        return list.toArray(new FittedImageType[0]);
    }
}
