/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

import imagej.slim.fitting.FLIMImageFitter.OutputImage;

/**
 *
 * @author aivar
 */
public class FLIMFittedImageFactory {
    private static FLIMFittedImageFactory INSTANCE = null;
    
    private FLIMFittedImageFactory() { 
    }
    
    public static synchronized FLIMFittedImageFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new FLIMFittedImageFactory();
        }
        return INSTANCE;
    }
    
    public IFittedImage createImage(OutputImage outputImage, int[] dimension, int components) {
        IFittedImage fittedImage = null;
        String title;
        switch (outputImage) {
            case A1:
                title = (1 == components) ? "A" : "A1";
                fittedImage = new FittedImage(title, dimension, FLIMImageFitter.A1_INDEX);
                break;
            case T1:
                title = (1 == components) ? "T" : "T1";
                fittedImage = new FittedImage(title, dimension, FLIMImageFitter.T1_INDEX);
                break;
            case A2:
                fittedImage = new FittedImage("A2", dimension, FLIMImageFitter.A2_INDEX);
                break;
            case T2:
                fittedImage = new FittedImage("T2", dimension, FLIMImageFitter.T2_INDEX);
                break;
            case A3:
                fittedImage = new FittedImage("A3", dimension, FLIMImageFitter.A2_INDEX);
                break;
            case T3:
                fittedImage = new FittedImage("T3", dimension, FLIMImageFitter.T2_INDEX);
                break;
            case H:
                fittedImage = new FittedImage("H", dimension, FLIMImageFitter.H_INDEX);
                break;
            case Z:
                fittedImage = new FittedImage("Z", dimension, FLIMImageFitter.Z_INDEX);
                break;
            case CHISQ:
                fittedImage = new FittedImage("ChiSquare", dimension, FLIMImageFitter.CHISQ_INDEX);
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
