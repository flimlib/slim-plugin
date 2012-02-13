/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.images;

import loci.slim.fitting.images.ColorizedImageFitter.ColorizedImageType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class parses a string containing a list of output images, such as 
 * "A T Z X2" and produces an array of ColorizedImageType.
 * 
 * @author Aivar Grislis
 */
public class ColorizedImageParser {
    private static final Character CHI    = '\u03c7';
    private static final Character SQUARE = '\u00b2';
    private static final Character TAU    = '\u03c4';
    private static final String TAU_STRING = "" + TAU;
    private static final String CHI_SQ_STRING = "" + CHI + SQUARE;
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
    public ColorizedImageParser(String input, int components, boolean stretched,
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
    public ColorizedImageType[] getColorizedImages() {
        List<ColorizedImageType> list = new ArrayList<ColorizedImageType>();
        String[] tokens = _input.split(" ");
        for (String token : tokens) {
            System.out.println("TOKEN >" + token + "<");
            if ("A".equals(token)) {
                switch (_components) {
                    case 1:
                        if (_free[A1_INDEX]) {
                            list.add(ColorizedImageType.A1);
                        }
                        break;
                    case 2:
                        if (_free[A1_INDEX]) {
                            list.add(ColorizedImageType.A1);
                        }
                        if (_free[A2_INDEX]) {
                            list.add(ColorizedImageType.A2);
                        }
                        break;
                    case 3:
                        if (_free[A1_INDEX]) {
                            list.add(ColorizedImageType.A1);
                        }
                        if (_free[A2_INDEX]) {
                            list.add(ColorizedImageType.A2);
                        }
                        if (_free[A3_INDEX]) {
                            list.add(ColorizedImageType.A3);
                        }
                        break;
               }
            }
            else if ("T".equals(token) || TAU_STRING.equals(token)) {
                switch (_components) {
                    case 1:
                        if (_free[T1_INDEX]) {
                            list.add(ColorizedImageType.T1);
                        }
                        break;
                    case 2:
                        if (_free[T1_INDEX]) {
                            list.add(ColorizedImageType.T1);
                        }
                        if (_free[T2_INDEX]) {
                            list.add(ColorizedImageType.T2);
                        }
                        break;
                    case 3:
                        if (_free[T1_INDEX]) {
                            list.add(ColorizedImageType.T1);
                        }
                        if (_free[T2_INDEX]) {
                            list.add(ColorizedImageType.T2);
                        }
                        if (_free[T3_INDEX]) {
                            list.add(ColorizedImageType.T3);
                        }
                        break;
               }
            }
            else if ("Z".equals(token)) {
                if (_free[Z_INDEX]) {
                    list.add(ColorizedImageType.Z);
                }
            }
            else if ("X2".equals(token) || CHI_SQ_STRING.equals(token)) {
                list.add(ColorizedImageType.CHISQ);
            }
            else if ("H".equals(token)) {
                if (_stretched) {
                    if (_free[H_INDEX]) {
                        list.add(ColorizedImageType.H);
                    }
                }
            }
            else if ("F".equals(token)) {
                switch (_components) {
                    case 2:
                        list.add(ColorizedImageType.F1);
                        list.add(ColorizedImageType.F2);
                        break;
                    case 3:
                        list.add(ColorizedImageType.F1);
                        list.add(ColorizedImageType.F2);
                        list.add(ColorizedImageType.F3);
                        break;
                }
            }
            else if ("f".equals(token)) {
                switch (_components) {
                    case 2:
                        list.add(ColorizedImageType.f1);
                        list.add(ColorizedImageType.f2);
                        break;
                    case 3:
                        list.add(ColorizedImageType.f1);
                        list.add(ColorizedImageType.f2);
                        list.add(ColorizedImageType.f3);
                        break;
                }
            }        
        }
        return list.toArray(new ColorizedImageType[0]);
    }
}
