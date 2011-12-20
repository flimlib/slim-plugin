/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

import imagej.slim.fitting.FLIMImageFitter.OutputImage;

import java.util.ArrayList;
import java.util.List;

/**
 * This class parses a string containing a list of output images, such as 
 * "A T Z X2" and produces an array of OutputImage.
 * 
 * @author Aivar Grislis
 */
public class OutputImageParser {
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
    public OutputImageParser(String input, int components, boolean stretched) {
        _input = input;
        _components = components;
        _stretched = stretched;
    }

    /**
     * Parses the input string and creates array of OutputImage.  Only creates
     * images which are appropriate for current fit.
     * 
     * @return 
     */
    public OutputImage[] getOutputImages() {
        List<OutputImage> list = new ArrayList<OutputImage>();
        String[] tokens = _input.split(" ");
        for (String token : tokens) {
            System.out.println("TOKEN >" + token + "<");
            if ("A".equals(token)) {
                switch (_components) {
                    case 1:
                        list.add(OutputImage.A1);
                        break;
                    case 2:
                        list.add(OutputImage.A1);
                        list.add(OutputImage.A2);
                        break;
                    case 3:
                        list.add(OutputImage.A1);
                        list.add(OutputImage.A2);
                        list.add(OutputImage.A3);
                        break;
               }
            }
            else if ("T".equals(token) || TAU_STRING.equals(token)) {
                switch (_components) {
                    case 1:
                        list.add(OutputImage.T1);
                        break;
                    case 2:
                        list.add(OutputImage.T1);
                        list.add(OutputImage.T2);
                        break;
                    case 3:
                        list.add(OutputImage.T1);
                        list.add(OutputImage.T2);
                        list.add(OutputImage.T3);
                        break;
               }
            }
            else if ("Z".equals(token)) {
                list.add(OutputImage.Z);
            }
            else if ("X2".equals(token) || CHI_SQ_STRING.equals(token)) {
                list.add(OutputImage.CHISQ);
            }
            else if ("H".equals(token)) {
                if (_stretched) {
                    list.add(OutputImage.H);
                }
            }
            else if ("F".equals(token)) {
                switch (_components) {
                    case 2:
                        list.add(OutputImage.F1);
                        list.add(OutputImage.F2);
                        break;
                    case 3:
                        list.add(OutputImage.F1);
                        list.add(OutputImage.F2);
                        list.add(OutputImage.F3);
                        break;
                }
            }
            else if ("f".equals(token)) {
                switch (_components) {
                    case 2:
                        list.add(OutputImage.f1);
                        list.add(OutputImage.f2);
                        break;
                    case 3:
                        list.add(OutputImage.f1);
                        list.add(OutputImage.f2);
                        list.add(OutputImage.f3);
                        break;
                }
            }        
        }
        return list.toArray(new OutputImage[0]);
    }
}
