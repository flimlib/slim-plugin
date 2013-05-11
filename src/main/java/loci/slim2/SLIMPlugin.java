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
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.menu.MenuConstants;
import imagej.module.DefaultModuleItem;
import imagej.tool.Tool;
import imagej.tool.ToolService;

import java.util.ArrayList;
import java.util.List;

import loci.slim2.decay.DecayDatasetUtility;
import loci.slim2.decay.LifetimeDatasetWrapper;
import loci.slim2.decay.LifetimeGrayscaleDataset;
import loci.slim2.outputset.temp.CustomAxisType;
import loci.slim2.outputset.IndexedMemberFormula;
import loci.slim2.outputset.temp.RampGenerator;
import loci.slim2.outputset.OutputSetMember;
import loci.slim2.outputset.OutputSet;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ValuePair;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.DisplayService;
import imagej.io.IOService;
import java.io.File;
import net.imglib2.Cursor;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * A command used to analyze time-based lifetime images.
 * 
 * @author Aivar Grislis
 */
@Plugin(type = Command.class, menuPath = "Analyze>Lifetime>Spectral Lifetime Analysis (IJ2)") //TODO ARG rename w/o IJ2
public class SLIMPlugin <T extends RealType<T> & NativeType<T>> implements Command {
	private static final String LIFETIME = "Lifetime";
	private LifetimeDatasetWrapper lifetimeDatasetWrapper;
	private LifetimeGrayscaleDataset lifetimeGrayscaleDataset;
	
	@Parameter
	private IOService ioService;
	
	@Parameter
	private DatasetService datasetService;
	
	@Parameter
	private DisplayService displayService;
	
	@Parameter
	private ToolService toolService;
	
	@Parameter(min = "0")
	private int bins = 0;
	
	@Override
	public void run() {
		
		FittingContext fittingContext = new FittingContext(); //TODO ARG "Fitting" is not quite right here
		
		Dataset dataset = getLifetimeDataset();
		if (null != dataset) {
			// wrap the dataset for lifetime information
			lifetimeDatasetWrapper = new LifetimeDatasetWrapper(dataset);
			fittingContext.setDatasetWrapper(lifetimeDatasetWrapper);
			
			// make a grayscale version of lifetime dataset
			lifetimeGrayscaleDataset = new LifetimeGrayscaleDataset(datasetService, lifetimeDatasetWrapper, bins);
			fittingContext.setGrayscaleDataset(lifetimeGrayscaleDataset);

			// display grayscale version
			Display<?> display = displayService.createDisplay(lifetimeGrayscaleDataset.getDataset());
			//TODO ARG how to draw overlays on top of this display???
			fittingContext.setGrayscaleDisplay(display);
		}

		// allow clicking on the grayscale version
		showTool();
		
		// if a LT image is displayed, use it
		// else prompt for image.
		
		//TODO experimental
		List<OutputSetMember> list = new ArrayList<OutputSetMember>();
		int inputIndex;
		int outputIndex;
		inputIndex = 0;
		outputIndex = 5;
		IndexedMemberFormula formula1 = new IndexedMemberFormula(inputIndex);
		OutputSetMember index1 = new OutputSetMember<T>("X2", outputIndex, formula1);
		list.add(index1);
		inputIndex = 1;
		outputIndex = 4;
		IndexedMemberFormula formula2 = new IndexedMemberFormula(inputIndex);
		OutputSetMember index2 = new OutputSetMember<T>("A1", outputIndex, formula2);
		list.add(index2);
		inputIndex = 2;
		outputIndex = 3;
		IndexedMemberFormula formula3 = new IndexedMemberFormula(inputIndex);
		OutputSetMember index3 = new OutputSetMember<T>("T1", outputIndex, formula3);
		list.add(index3);
		inputIndex = 3;
		outputIndex = 2;
		IndexedMemberFormula formula4 = new IndexedMemberFormula(inputIndex);
		OutputSetMember index4 = new OutputSetMember<T>("A2", outputIndex, formula3);
		list.add(index3);
		inputIndex = 4;
		outputIndex = 1;
		IndexedMemberFormula formula5 = new IndexedMemberFormula(inputIndex);
		OutputSetMember index5 = new OutputSetMember<T>("T2", outputIndex, formula3);
		list.add(index3);		
		inputIndex = 5;
		outputIndex = 0;
		IndexedMemberFormula formula6 = new IndexedMemberFormula(inputIndex);
		OutputSetMember index6 = new OutputSetMember<T>("Z", outputIndex, formula3);
		list.add(index3);		
		
		boolean combined = false; //true; // create a stack
		DoubleType type = new DoubleType();
		long[] dimensions = new long[] { 400, 300, 5 }; // x y z
		AxisType[] axes = new AxisType[3];
		axes[0] = Axes.X;
		axes[1] = Axes.Y;
		axes[2] = Axes.Z;
		OutputSet imageSet = new OutputSet(datasetService, combined, type, dimensions, "Test", axes, list);
		
		List<Dataset> datasetList = imageSet.getDatasets();
		Display display = null;
		for (Dataset d : datasetList) {
			display = displayService.createDisplay(d);
		}
		
		// slightly easier way to create similar sets
		combined = !combined; // try the other variant also (stack vs separate images)
		OutputSet imageSet2 = new OutputSet(datasetService, combined, type, dimensions, "Test 2", axes, new String[] { "X2", "A1", "T1", "A2", "T2", "Z" });
		List<Dataset> datasetList2 = imageSet2.getDatasets();
		if (false) for (Dataset d : datasetList2) {
			display = displayService.createDisplay(d);
		}
		
		// do some drawing
		long width = dimensions[0];
		long height = dimensions[1];
		RampGenerator[] inputs = new RampGenerator[] {
			new RampGenerator(RampGenerator.RampType.UPPER_LEFT, width, height),
			new RampGenerator(RampGenerator.RampType.BOTTOM, width, height),
			new RampGenerator(RampGenerator.RampType.LOWER_RIGHT, width, height),
			new RampGenerator(RampGenerator.RampType.LEFT, width, height),
			new RampGenerator(RampGenerator.RampType.UPPER_RIGHT, width, height),
			new RampGenerator(RampGenerator.RampType.LOWER_LEFT, width, height)
		};
		List<DoubleType> valuesList = new ArrayList<DoubleType>();
		double[] inputValues = new double[inputs.length];
		for (long y = 0; y < height; ++y) {
			for (long x = 0; x < width; ++x) {
				valuesList.clear();
				for (int i = 0; i < inputs.length; ++i) {
					valuesList.add(new DoubleType(inputs[i].getValue(x, y)));
					inputValues[i] = inputs[i].getValue(x, y);
				}
				for (long z = 0; z < dimensions[2]; ++z) {
					long[] position = new long[] { x, y, z };
					imageSet2.setPixelValue(valuesList, position);
				}
			}
		}

		//TODO ARG big problem here!
		// I should be able to createDisplay up above and just update here
		if (true) for (Dataset d : datasetList2) {
			displayService.createDisplay(d);
		}
		
		display.update();
		
		Dataset dataset2 = (Dataset) imageSet2.getDatasets().get(0);
		dataset2.setDirty(true);
		System.out.println("FINIS");
		
		
		// one more time; see if there's a timing issue with updates
		display.update();
		
		//TODO end experimental

		// done clicking on the grayscale version
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
