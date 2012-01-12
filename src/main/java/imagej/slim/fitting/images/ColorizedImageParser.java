/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting.images;

import imagej.slim.fitting.images.ColorizedImageFitter.ColorizedImageType;

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
    private String _input;
    private int _components;
    private boolean _stretched;

    /**
     * Creates an instance for a given input string, etc.
     * 
     * @param input
     * @param components
     * @param stretched 
     */
    public ColorizedImageParser(String input, int components, boolean stretched) {
        _input = input;
        _components = components;
        _stretched = stretched;
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
                        list.add(ColorizedImageType.A1);
                        break;
                    case 2:
                        list.add(ColorizedImageType.A1);
                        list.add(ColorizedImageType.A2);
                        break;
                    case 3:
                        list.add(ColorizedImageType.A1);
                        list.add(ColorizedImageType.A2);
                        list.add(ColorizedImageType.A3);
                        break;
               }
            }
            else if ("T".equals(token) || TAU_STRING.equals(token)) {
                switch (_components) {
                    case 1:
                        list.add(ColorizedImageType.T1);
                        break;
                    case 2:
                        list.add(ColorizedImageType.T1);
                        list.add(ColorizedImageType.T2);
                        break;
                    case 3:
                        list.add(ColorizedImageType.T1);
                        list.add(ColorizedImageType.T2);
                        list.add(ColorizedImageType.T3);
                        break;
               }
            }
            else if ("Z".equals(token)) {
                list.add(ColorizedImageType.Z);
            }
            else if ("X2".equals(token) || CHI_SQ_STRING.equals(token)) {
                list.add(ColorizedImageType.CHISQ);
            }
            else if ("H".equals(token)) {
                if (_stretched) {
                    list.add(ColorizedImageType.H);
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
