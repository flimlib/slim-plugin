/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting.images;

import imagej.slim.fitting.images.AbstractBaseColorizedImage;

/**
 * This class builds a fitted image that shows the fractional intensity.
 * 
 * Fractional Intensity Fi = Ai / sum of all Aj.
 *
 * @author Aivar Grislis
 */
public class FractionalIntensityImage extends AbstractBaseColorizedImage {
    private int _component;
    private int _components;

    /**
     * Create the fitted image.  Specifies number of components which should
     * be 2 or 3 and the current component which should be 0..1 or 0..2
     * respectively.
     * 
     * @param title
     * @param dimension
     * @param component
     * @param components 
     */
    public FractionalIntensityImage(String title, int[] dimension, int component, int components) {
        super(title, dimension);
        _component = component;
        _components = components;
    }
    
    public double getValue(double[] parameters) {
        double value = 0.0;
        double sum = 0.0;
        switch (_components) {
            case 2:
                sum = parameters[ColorizedImageFitter.A1_INDEX]
                        + parameters[ColorizedImageFitter.A2_INDEX];
                switch (_component) {
                    case 0:
                        value = parameters[ColorizedImageFitter.A1_INDEX];
                        break;
                    case 1:
                        value = parameters[ColorizedImageFitter.A2_INDEX];;
                        break;
                }
                break;
            case 3:
                sum = parameters[ColorizedImageFitter.A1_INDEX]
                        + parameters[ColorizedImageFitter.A2_INDEX]
                        + parameters[ColorizedImageFitter.A3_INDEX];
                switch (_component) {
                    case 0:
                        value = parameters[ColorizedImageFitter.A1_INDEX];
                        break;
                    case 1:
                        value = parameters[ColorizedImageFitter.A2_INDEX];
                        break;
                    case 2:
                        value = parameters[ColorizedImageFitter.A3_INDEX];
                        break;
                }
                break;
        }
        return value / sum;
    }   
}
