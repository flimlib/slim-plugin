/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

/**
 * Simple image that just displays one of the input parameters.
 *
 * @author Aivar Grislis
 */
public class FittedImage extends AbstractBaseFittedImage {
    private int _parameterIndex;

    /**
     * Constructor, giving index of input parameter to display
     *
     * @param title
     * @param dimension
     * @param parameterIndex
     */
    public FittedImage(String title, int[] dimension, int parameterIndex) {
        super(title, dimension);
        _parameterIndex = parameterIndex;
    }
    
    /**
     * Given the array of fitted parameters, get the value for this image.
     * 
     * @param parameters
     * @return 
     */
    @Override
    public double getValue(double[] parameters) {
        return parameters[_parameterIndex];
    }
    
}