/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.analysis;

import loci.slim.SLIMProcessor.FitFunction;
import loci.slim.SLIMProcessor.FitRegion;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * An interface for analyzing the results of a SLIM Plugin fit.
 * 
 * @author Aivar Grislis
 */
public interface ISLIMAnalyzer {
    public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function);
}
