/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010-2013, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim2;
//TODO ARG 'slim2' is just a temporary package name for IJ2 version to keep the two codebases separate

import imagej.ImageJ;
import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.operator.CalculatorOp;
import imagej.data.operator.CalculatorService;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.menu.MenuConstants;
import imagej.module.DefaultModuleItem;
import imagej.tool.Tool;
import imagej.tool.ToolService;

import java.util.List;

import loci.slim2.decay.DecayDatasetUtility;
import loci.slim2.decay.LifetimeDatasetWrapper;
import loci.slim2.decay.LifetimeGrayscaleDataset;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.meta.AxisType;

import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ValuePair;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * A command used to analyze time-based lifetime images.
 * 
 * @author Aivar Grislis
 */
@Plugin(type = Command.class, menuPath = "Analyze>Lifetime>Spectral Lifetime Analysis (IJ2)") //TODO ARG rename w/o IJ2
public class SLIMPlugin implements Command {
	private static final String LIFETIME = "Lifetime";
	private LifetimeDatasetWrapper lifetimeDatasetWrapper;
	private LifetimeGrayscaleDataset lifetimeGrayscaleDataset;
	
	@Parameter
	private DatasetService datasetService;
	
	@Parameter
	private DisplayService displayService;
	
	@Parameter
	private ToolService toolService;
	
	@Parameter(min = "1")
	private int width = 512; //TODO just for fun; checking parameters

	@Parameter(min = "1")
	private int height = 512; //TODO just for fun; checking parameters
	
	@Override
	public void run() {
		showTool();
		
		Dataset dataset = getLifetimeDataset();
		if (null != dataset) {
			// wrap the dataset for lifetime information
			lifetimeDatasetWrapper = new LifetimeDatasetWrapper(dataset);
			
			// make a grayscale version of lifetime dataset
			lifetimeGrayscaleDataset = new LifetimeGrayscaleDataset(datasetService, lifetimeDatasetWrapper);

			// display grayscale version
			displayService.createDisplay(lifetimeGrayscaleDataset.getDataset());
		}
		
		// if a LT image is displayed, use it
		// else prompt for image.
		
		hideTool();
	}
	
	private void showTool() {
		Tool tool = getTool();
		if (null != tool) {
			// show the tool
			//TODO
		}
	}
	
	private void hideTool() {
		Tool tool = getTool();
		if (null != tool) {
			// hide the tool
			//TODO
		}
	}
	
	private Tool getTool() {
		return toolService.getTool("aivar"); //TODO
	}

	/**
	 * Finds the first lifetime Dataset from the DisplayService.
	 * 
	 * @return null or lifetime Dataset
	 */
	private Dataset getLifetimeDataset() {
		Display<?> display = displayService.getActiveDisplay();
		List<Dataset> datasets = datasetService.getDatasets();
		for (Dataset dataset : datasets) {
			AxisType[] axisTypes = dataset.getAxes();
			for (int i = 0; i < axisTypes.length; ++i) {
				if (LIFETIME.equals(axisTypes[i].getLabel())) {
					return dataset;
				}
			}
		}
		return null;
	}
	
	/** Tests our command. */
	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = imagej.Main.launch(args);

		// Launch the "SLIMPlugin" command right away.
		ij.command().run(SLIMPlugin.class);
	}
}
