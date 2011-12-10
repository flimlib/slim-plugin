/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

/**
 *
 * @author aivar
 */
public class FittedImage extends AbstractBaseFittedImage {
    private int _parameterIndex;
    
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