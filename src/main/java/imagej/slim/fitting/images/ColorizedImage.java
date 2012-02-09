/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting.images;

import java.awt.image.IndexColorModel;

import imagej.slim.fitting.images.AbstractBaseColorizedImage;

/**
 * Simple image that just displays one of the input parameters.
 *
 * @author Aivar Grislis
 */
public class ColorizedImage extends AbstractBaseColorizedImage {
    private int _parameterIndex;

    /**
     * Constructor, giving index of input parameter to display
     *
     * @param title
     * @param dimension
     * @param parameterIndex
     */
    public ColorizedImage(String title, int[] dimension,
            IndexColorModel indexColorModel, int parameterIndex) {
        super(title, dimension, indexColorModel);
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