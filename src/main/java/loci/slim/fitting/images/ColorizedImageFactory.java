/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import loci.slim.IGrayScalePixelValue;
import loci.slim.fitting.images.IColorizedImage;
import loci.slim.fitting.images.ColorizedImage;
import loci.slim.fitting.images.FractionalIntensityImage;
import loci.slim.fitting.images.FractionalContributionImage;
import loci.slim.fitting.images.ColorizedImageFitter.ColorizedImageType;

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
    
    public IColorizedImage createImage(
            ColorizedImageType outputImageType,
            int[] dimension,
            IndexColorModel indexColorModel,
            int components,
            boolean colorizeGrayScale,
            IGrayScalePixelValue grayScalePixelValue)
    {
        IColorizedImage fittedImage = null;
        String title;
        switch (outputImageType) {
            case A1:
                title = (1 == components) ? "A" : "A1";
                fittedImage = new ColorizedImage(title, dimension,
                        indexColorModel, ColorizedImageFitter.A1_INDEX,
                        colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case T1:
                title = (1 == components) ? "T" : "T1";
                fittedImage = new ColorizedImage(title, dimension,
                        indexColorModel, ColorizedImageFitter.T1_INDEX,
                        colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case A2:
                fittedImage = new ColorizedImage("A2", dimension,
                        indexColorModel, ColorizedImageFitter.A2_INDEX,
                        colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case T2:
                fittedImage = new ColorizedImage("T2", dimension,
                        indexColorModel, ColorizedImageFitter.T2_INDEX,
                        colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case A3:
                fittedImage = new ColorizedImage("A3", dimension,
                        indexColorModel, ColorizedImageFitter.A2_INDEX,
                        colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case T3:
                fittedImage = new ColorizedImage("T3", dimension,
                        indexColorModel, ColorizedImageFitter.T2_INDEX,
                        colorizeGrayScale, 
                        grayScalePixelValue);
                break;
            case H:
                fittedImage = new ColorizedImage("H", dimension,
                        indexColorModel, ColorizedImageFitter.H_INDEX,
                        colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case Z:
                fittedImage = new ColorizedImage("Z", dimension,
                        indexColorModel, ColorizedImageFitter.Z_INDEX,
                        colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case CHISQ:
                fittedImage = new ColorizedImage("X2", dimension,
                        indexColorModel, ColorizedImageFitter.CHISQ_INDEX,
                        colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case F1:
                fittedImage = new FractionalIntensityImage("F1", dimension,
                        indexColorModel, 0, components, colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case F2:
                fittedImage = new FractionalIntensityImage("F2", dimension,
                        indexColorModel, 1, components, colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case F3:
                fittedImage = new FractionalIntensityImage("F3", dimension,
                        indexColorModel, 2, components, colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case f1:
                fittedImage = new FractionalContributionImage("f1", dimension,
                        indexColorModel, 0, components, colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case f2:
                fittedImage = new FractionalContributionImage("f2", dimension,
                        indexColorModel, 1, components, colorizeGrayScale,
                        grayScalePixelValue);
                break;
            case f3:
                fittedImage = new FractionalContributionImage("f3", dimension,
                        indexColorModel, 2, components, colorizeGrayScale,
                        grayScalePixelValue);
                break;
        }
        return fittedImage;
    }
}
