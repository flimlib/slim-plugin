/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim.fitting.images;

import java.awt.image.IndexColorModel;

import loci.slim.IGrayScaleImage;
import loci.slim.fitting.images.FittedImageFitter.FittedImageType;
import loci.slim.mask.IMaskGroup;

/**
 * Factory creates fitted images.
 * 
 * @author Aivar Grislis
 */
public class FittedImageFactory {
    private static FittedImageFactory INSTANCE = null;
    
    private FittedImageFactory() { 
    }
    
    public static synchronized FittedImageFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new FittedImageFactory();
        }
        return INSTANCE;
    }
    
    public IFittedImage createImage(
			String file,
            FittedImageType outputImageType,
			int channel,
			int ordinal,
            int[] dimension,
            IndexColorModel indexColorModel,
            int components,
            boolean colorizeGrayScale,
            IGrayScaleImage grayScaleImage,
            IMaskGroup[] maskGroup)
    {
		if (dimension.length > 3) {
		    System.out.println("dim 2 " + dimension[2] + " dim 3 " + dimension[3]);
		}
		else {
			System.out.println("dim " + dimension[0] + " " + dimension[1] + " " + dimension[2]);
		}
        IFittedImage fittedImage = null;
        String title;
        switch (outputImageType) {
            case A1:
                title = (1 == components) ? "A" : "A1";
				title = addToTitle(title, channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.A1_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case T1:
                title = (1 == components) ? "T" : "T1";
				title = addToTitle(title, channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.T1_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case A2:
				title = addToTitle("A2", channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.A2_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case T2:
				title = addToTitle("T2", channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.T2_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case A3:
				title = addToTitle("A3", channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.A2_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case T3:
				title = addToTitle("T3", channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.T2_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case H:
				title = addToTitle("H", channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.H_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case Z:
				title = addToTitle("Z", channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.Z_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case CHISQ:
				title = addToTitle("X2", channel, ordinal, file);
                fittedImage = new SimpleFittedImage(title, dimension,
                        indexColorModel, FittedImageFitter.CHISQ_INDEX,
                        colorizeGrayScale, grayScaleImage, maskGroup);
                break;
            case F1:
				title = addToTitle("F1", channel, ordinal, file);
                fittedImage = new FractionalIntensityImage(title, dimension,
                        indexColorModel, 0, components, colorizeGrayScale,
                        grayScaleImage, maskGroup);
                break;
            case F2:
				title = addToTitle("F2", channel, ordinal, file);
                fittedImage = new FractionalIntensityImage(title, dimension,
                        indexColorModel, 1, components, colorizeGrayScale,
                        grayScaleImage, maskGroup);
                break;
            case F3:
				title = addToTitle("F3", channel, ordinal, file);
                fittedImage = new FractionalIntensityImage(title, dimension,
                        indexColorModel, 2, components, colorizeGrayScale,
                        grayScaleImage, maskGroup);
                break;
            case f1:
				title = addToTitle("f1", channel, ordinal, file);
                fittedImage = new FractionalContributionImage(title, dimension,
                        indexColorModel, 0, components, colorizeGrayScale,
                        grayScaleImage, maskGroup);
                break;
            case f2:
				title = addToTitle("f2", channel, ordinal, file);
                fittedImage = new FractionalContributionImage(title, dimension,
                        indexColorModel, 1, components, colorizeGrayScale,
                        grayScaleImage, maskGroup);
                break;
            case f3:
				title = addToTitle("f3", channel, ordinal, file);
                fittedImage = new FractionalContributionImage(title, dimension,
                        indexColorModel, 2, components, colorizeGrayScale,
                        grayScaleImage, maskGroup);
                break;
            case Tm:
				title = addToTitle("Tm", channel, ordinal, file);
                fittedImage = new TauMeanImage(title, dimension,
                        indexColorModel, 0, components, colorizeGrayScale,
                        grayScaleImage, maskGroup);
                break;
        }
        return fittedImage;
    }
	
	private String addToTitle(String title, int channel, int ordinal, String file) {
		if (channel >= 0) {
			title += " " + channel;
		}
		int suffixIndex = file.lastIndexOf('.');
		return title + " (" + romanNumeral(ordinal) + ") " + file.substring(0, suffixIndex);
	}
	
	private String romanNumeral(int ordinal) {
		StringBuffer result = new StringBuffer();
		while (ordinal >= 100) { // works until 400: get "cccc" rather than "cd"
			ordinal -= 100;
			result.append("c");
		}
		if (ordinal >= 90) {
			ordinal -= 90;
			result.append("xc");
		}
		if (ordinal >= 50) {
			ordinal -= 50;
			result.append("l");
		}
		if (ordinal >= 40) {
			ordinal -= 40;
			result.append("xl");
		}
		while (ordinal >= 10) {
			ordinal -= 10;
			result.append("x");
		}
		if (ordinal >= 9) {
			ordinal -= 9;
			result.append("ix");
		}
		if (ordinal >= 5) {
			ordinal -= 5;
			result.append("v");
		}
		if (ordinal >= 4) {
			ordinal -= 4;
			result.append("iv");
		}
		while (ordinal >= 1) {
			--ordinal;
			result.append("i");
		}
		
		return result.toString();
	}
}
