/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

/**
 * This class builds a fitted image that shows the fractional contribution.
 * 
 * Fractional Contribution fi = Ai*Ti / sum of all Aj*Tj.
 *
 * @author Aivar Grislis
 */
public class FractionalContributionImage extends AbstractBaseFittedImage {
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
    public FractionalContributionImage(String title, int[] dimension, int component, int components) {
        super(title, dimension);
        _component = component;
        _components = components;
    }
    
    public double getValue(double[] parameters) {
        double value = 0.0;
        double sum = 0.0;
        switch (_components) {
            case 2:
                sum = parameters[FLIMImageFitter.A1_INDEX]
                        * parameters[FLIMImageFitter.T1_INDEX]
                        + parameters[FLIMImageFitter.A2_INDEX]
                        * parameters[FLIMImageFitter.T2_INDEX];
                switch (_component) {
                    case 0:
                        value = parameters[FLIMImageFitter.A1_INDEX]
                                * parameters[FLIMImageFitter.T1_INDEX];
                        break;
                    case 1:
                        value = parameters[FLIMImageFitter.A2_INDEX]
                                * parameters[FLIMImageFitter.T2_INDEX];
                        break;
                }
                break;
            case 3:
                sum = parameters[FLIMImageFitter.A1_INDEX]
                        * parameters[FLIMImageFitter.T1_INDEX]
                        + parameters[FLIMImageFitter.A2_INDEX]
                        * parameters[FLIMImageFitter.T2_INDEX]
                        + parameters[FLIMImageFitter.A3_INDEX]
                        * parameters[FLIMImageFitter.T3_INDEX];
                switch (_component) {
                    case 0:
                        value = parameters[FLIMImageFitter.A1_INDEX]
                                * parameters[FLIMImageFitter.T1_INDEX];
                        break;
                    case 1:
                        value = parameters[FLIMImageFitter.A2_INDEX]
                                * parameters[FLIMImageFitter.T2_INDEX];
                        break;
                    case 2:
                        value = parameters[FLIMImageFitter.A3_INDEX]
                                * parameters[FLIMImageFitter.T3_INDEX];
                        break;
                }
                break;
        }
        return value / sum;
    }   
}
