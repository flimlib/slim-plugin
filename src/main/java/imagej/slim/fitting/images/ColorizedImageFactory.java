/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting.images;

import imagej.slim.fitting.images.IColorizedImage;
import imagej.slim.fitting.images.ColorizedImage;
import imagej.slim.fitting.images.FractionalIntensityImage;
import imagej.slim.fitting.images.FractionalContributionImage;
import imagej.slim.fitting.images.ColorizedImageFitter.ColorizedImageType;

/**
 *
 * @author Aivar Grislis
 */
public class ColorizedImageFactory {
    private static ColorizedImageFactory INSTANCE = null;
    
    private ColorizedImageFactory() { 
    }
    
    public static synchronized ColorizedImageFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ColorizedImageFactory();
        }
        return INSTANCE;
    }
    
    public IColorizedImage createImage(ColorizedImageType outputImage, int[] dimension, int components) {
        IColorizedImage fittedImage = null;
        String title;
        switch (outputImage) {
            case A1:
                title = (1 == components) ? "A" : "A1";
                fittedImage = new ColorizedImage(title, dimension, ColorizedImageFitter.A1_INDEX);
                break;
            case T1:
                title = (1 == components) ? "T" : "T1";
                fittedImage = new ColorizedImage(title, dimension, ColorizedImageFitter.T1_INDEX);
                break;
            case A2:
                fittedImage = new ColorizedImage("A2", dimension, ColorizedImageFitter.A2_INDEX);
                break;
            case T2:
                fittedImage = new ColorizedImage("T2", dimension, ColorizedImageFitter.T2_INDEX);
                break;
            case A3:
                fittedImage = new ColorizedImage("A3", dimension, ColorizedImageFitter.A2_INDEX);
                break;
            case T3:
                fittedImage = new ColorizedImage("T3", dimension, ColorizedImageFitter.T2_INDEX);
                break;
            case H:
                fittedImage = new ColorizedImage("H", dimension, ColorizedImageFitter.H_INDEX);
                break;
            case Z:
                fittedImage = new ColorizedImage("Z", dimension, ColorizedImageFitter.Z_INDEX);
                break;
            case CHISQ:
                fittedImage = new ColorizedImage("ChiSquare", dimension, ColorizedImageFitter.CHISQ_INDEX);
                break;
            case F1:
                fittedImage = new FractionalIntensityImage("F1", dimension, 0, components);
                break;
            case F2:
                fittedImage = new FractionalIntensityImage("F2", dimension, 1, components);
                break;
            case F3:
                fittedImage = new FractionalIntensityImage("F3", dimension, 2, components);
                break;
            case f1:
                fittedImage = new FractionalContributionImage("f1", dimension, 0, components);
                break;
            case f2:
                fittedImage = new FractionalContributionImage("f2", dimension, 1, components);
                break;
            case f3:
                fittedImage = new FractionalContributionImage("f3", dimension, 2, components);
                break;
        }
        return fittedImage;
    }
}
