/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.analysis.plugins;

import ij.IJ;
import ij.ImagePlus;

import imagej.process.ImageUtils;

import loci.slim.ui.IUserInterfacePanel.FitFunction;
import loci.slim.ui.IUserInterfacePanel.FitRegion;
import loci.slim.analysis.ISLIMAnalyzer;
import loci.slim.analysis.SLIMAnalyzer;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * Runs the VisADPlugin to analyze SLIMPlugin results.
 *
 * This will run on a separate thread, control returns to the caller right away.
 *
 * @author Aivar Grislis
 */
@SLIMAnalyzer(name="VisAD")
public class VisADAnalysisPlugin implements ISLIMAnalyzer {
    public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function) {
        ImagePlus imp = ImageUtils.createImagePlus(image, "Fitted results");
        IJ.runPlugIn(imp, "imagej.visad.VisADPlugin", "");
    }
}
