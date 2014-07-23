/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
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

package loci.slim2;
//TODO ARG 'slim2' is just a temporary package name for IJ2 version to keep the two codebases separate

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import loci.slim2.decay.LifetimeDatasetWrapper;
import loci.slim2.decay.NoLifetimeAxisFoundException;
import loci.slim2.heuristics.DefaultEstimator;
import loci.slim2.heuristics.Estimator;
import loci.slim2.outputset.IndexedMemberFormula;
import loci.slim2.outputset.OutputSet;
import loci.slim2.outputset.OutputSetMember;
import loci.slim2.outputset.temp.ChunkyPixel;
import loci.slim2.outputset.temp.ChunkyPixelIterator;
import loci.slim2.outputset.temp.RampGenerator;
import loci.slim2.process.BatchProcessor;
import loci.slim2.process.InteractiveProcessor;
import loci.slim2.process.batch.DefaultBatchProcessor;
import loci.slim2.process.interactive.DefaultInteractiveProcessor;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.display.DataView;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.threshold.ThresholdService;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.tool.Tool;
import org.scijava.tool.ToolService;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 * A command used to analyze time-based lifetime images.
 * 
 * @author Aivar Grislis
 */
@Plugin(type = Command.class, menuPath = "Analyze>Lifetime>Spectral Lifetime Analysis (IJ2)") //TODO ARG rename w/o IJ2
public class SLIMPlugin <T extends RealType<T> & NativeType<T>> implements Command {
	private static final String PATH_KEY = "path";
	private static final String LIFETIME = "Lifetime";
	private static final String SDT_SUFFIX = ".sdt";
	private static final String ICS_SUFFIX = ".ics";
	private InteractiveProcessor interactiveProcessor;
	private LifetimeDatasetWrapper activeLifetime;
	private volatile boolean quit = false;

	@Parameter
	private Context context;

	@Parameter
	private CommandService commandService;

	@Parameter
	private IOService ioService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private ToolService toolService;

	@Parameter
	private UIService uiService;

	@Parameter
	private ThresholdService thresholdService;

	private Component parent;

	@Override
	public void run() {

		// allow clicking on the grayscale version during this session
		showTool();

		// special case first time through
		boolean firstTime = true;
		parent = ij.ImageJ.getFrames()[0];
		do {
			// new lifetime dataset wrapper
			LifetimeDatasetWrapper lifetime = null;

			if (firstTime) {
				// look for an already open LT image
				lifetime = getLifetimeDatasetWrapper();
			}

			// none found?
			if (null == lifetime) {
				// prompt for lifetime dataset name
				File[] files = showFileDialog(parent, getPathFromPreferences());				System.out.println("back from showFileDialog");
				if (null == files) {
					// dialog cancelled
					if (null == activeLifetime) {
						// cancel the whole plugin
						quit = true;
					}
					else {
						// reload previous
						lifetime = activeLifetime;
					}
				}
				else {
					// save source directory for next time
					savePathInPreferences(files[0].getParent());

					if (files.length > 1) {
						if (null != interactiveProcessor) {
							// do some batch processing
							final BatchProcessor batchProcessor = new DefaultBatchProcessor();
							int batchBins = interactiveProcessor.getFitSettings().getBins(); // check for same number bins
							batchProcessor.process(context, batchBins, files, interactiveProcessor.getFitSettings());
						}
						else {
							// error; no settings available yet
							showWarning("Manually process a single image before doing batch processing.");
						}

						// reload previous
						lifetime = activeLifetime;
					}
					else {
						// load the lifetime dataset
						try {
							lifetime = new LifetimeDatasetWrapper(context, files[0]);
						}
						catch (IOException e) {
							showWarning("Problem reading " + files[0].getAbsolutePath() + " " + e.getMessage());
						}
						catch (NoLifetimeAxisFoundException e) {
							showWarning("No Lifetime Axis found in " + files[0].getAbsolutePath());
						}
					}
				}
			}

			if (null != lifetime) {
				// keep track of current dataset
				activeLifetime = lifetime;

				// create processor first time through
				if (null == interactiveProcessor) {
					Estimator estimator = new DefaultEstimator();
					interactiveProcessor = new DefaultInteractiveProcessor();
					interactiveProcessor.init(context, commandService, datasetService, displayService, estimator);
				}

				// gives up control to load a new dataset or when done
				quit = interactiveProcessor.process(lifetime);
			}

			// one shot
			firstTime = false;
		}
		while (!quit);

		// done clicking on the grayscale version
		hideTool();
	}

	/**
	 * Throws up a warning message dialog.
	 *
	 * @param message
	 */
	private void showWarning(String message) {
		uiService.showDialog(message, DialogPrompt.MessageType.WARNING_MESSAGE);
	}

	/**
	 * Throws up an error message dialog.
	 * 
	 * @param message 
	 */
	private void showError(String message) {
		uiService.showDialog(message, DialogPrompt.MessageType.ERROR_MESSAGE);
	}

	/**
	 * Restores path name from Java Preferences.
	 *
	 * @return String with path name
	 */
	private String getPathFromPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.get(PATH_KEY, "");
	}

	/**
	 * Saves the path name to Java Preferences.
	 *
	 * @param path
	 */
	private void savePathInPreferences(String path) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.put(PATH_KEY, path);
	}

	/**
	 * Prompts for a FLIM file.
	 *
	 * @param parent
	 * @param default path
	 * @return null or array of Files
	 */
	private File[] showFileDialog(final Component parent, String defaultPath) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(defaultPath));
		chooser.setDialogTitle("Open Lifetime Image(s)");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(new ShowFileDialogFilter());

		// run on event dispatch thread
		final int[] returnCode = new int[1];
		try {
			SwingUtilities.invokeAndWait(
				new Runnable() {
					public void run() {
						returnCode[0] = chooser.showOpenDialog(parent);
					}
				});
		}
		catch (InterruptedException e) {
		}
		catch (InvocationTargetException e) {
		}

		if (returnCode[0] == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			List<File> fileList = new ArrayList<File>();
			for (File file : files) {
				if (file.isDirectory()) {
					for (File f : file.listFiles()) {
						if (f.getName().endsWith(ICS_SUFFIX)
								|| f.getName().endsWith(SDT_SUFFIX))
						{
							fileList.add(f);
						}
					}
				}
				else {
					fileList.add(file);
				}
			}
			return fileList.toArray(new File[fileList.size()]);
		}
		// no files selected
		return null;
	}

	/**
	 * File filter for lifetime files.
	 */
	private class ShowFileDialogFilter extends FileFilter {
		private static final String DESCRIPTION = "Lifetime .ics & .sdt";

		@Override
		public boolean accept(File file) {
			if (file.getName().endsWith(ICS_SUFFIX)) {
				return true;
			}
			if (file.getName().endsWith(SDT_SUFFIX)) {
				return true;
			}
			if (file.isDirectory()) {
				return true;
			}
			return false;
		}

		@Override
		public String getDescription() {
			return DESCRIPTION;
		}
	}

	private void test() {
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
		OutputSetMember index4 = new OutputSetMember<T>("A2", outputIndex, formula4);
		list.add(index4);
		inputIndex = 4;
		outputIndex = 1;
		IndexedMemberFormula formula5 = new IndexedMemberFormula(inputIndex);
		OutputSetMember index5 = new OutputSetMember<T>("T2", outputIndex, formula5);
		list.add(index5);
		inputIndex = 5;
		outputIndex = 0;
		IndexedMemberFormula formula6 = new IndexedMemberFormula(inputIndex);
		OutputSetMember index6 = new OutputSetMember<T>("Z", outputIndex, formula6);
		list.add(index6);

		boolean combined = false; //true; // create a stack
		boolean useChannelDimension = false;
		DoubleType type = new DoubleType();
		long[] dimensions = new long[] { 400, 300, 5 }; // x y z
		AxisType[] axes = new AxisType[3];
		axes[0] = Axes.X;
		axes[1] = Axes.Y;
		axes[2] = Axes.Z;
		OutputSet imageSet = new OutputSet(commandService, datasetService, combined, useChannelDimension, type, dimensions, "Test", axes, list);

		List<Dataset> datasetList = imageSet.getDatasets();
		Display display = null;
		for (Dataset d : datasetList) {
			d.getImgPlus().setChannelMinimum(0, 0.0); //TODO ARG just to see if this works; set min/max
			d.getImgPlus().setChannelMaximum(0, 1.0);
			d.setDirty(true);
			//d.getImgPlus().getImg().
			display = displayService.createDisplay(d);
		}

		// slightly easier way to create similar sets
		combined = !combined; // try the other variant also (stack vs separate images)
		System.out.println("!!! commandService is " + commandService);
		System.out.println("!!! datasetService is " + datasetService);
		OutputSet imageSet2 = new OutputSet(commandService, datasetService, combined, useChannelDimension, type, dimensions, "Test 2", axes, new String[] { "X2", "A1", "T1", "A2", "T2", "Z" });
		List<Dataset> datasetList2 = imageSet2.getDatasets();
		Display<?>[] displays = new Display<?>[datasetList2.size()];
		int index = 0;
		if (true) for (Dataset d : datasetList2) {
			d.getImgPlus().setChannelMinimum(0, 0.0);
			d.getImgPlus().setChannelMaximum(0, 1.0);
			d.setDirty(true);
			displays[index++] = displayService.createDisplay(d);
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
		double[] inputValues = new double[inputs.length];

		long time = System.currentTimeMillis();
		ChunkyPixelIterator iterator = new ChunkyPixelIterator(dimensions);
		while (iterator.hasNext()) {
			try {
				Thread.sleep(1);
			}
			catch (Exception e) {

			}
			ChunkyPixel chunkyPixel = iterator.next();
			long[] position = chunkyPixel.getPosition();
			long x = position[0];
			long y = position[1];
			for (int i = 0; i < inputs.length; ++i) {
				inputValues[i] = inputs[i].getValue(x, y);
			}
			for (long z = 0; z < dimensions[2]; ++z) {
				imageSet.setPixelValue(inputValues, position); //TODO ARG how does this work?  why iterate over z when nothing changes within the loop????
			}
		}
		System.out.println("Elapsed chunky pixel overhead time " + (System.currentTimeMillis() - time));

		Dataset dd = datasetList.get(0);
		System.out.println("dd name is " + dd.getName());
		dd.getImgPlus().setChannelMaximum(0, 1.0);
		dd.getImgPlus().setChannelMinimum(0, 0.0);
		dd.setDirty(true);
		dd.update();

		/*
		// previous, non-chunky drawing code:
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
		 */

		//TODO ARG big problem here!
		// I should be able to createDisplay up above and just update here
		//Display<?>[] displays = new Display<?>[datasetList2.size()];
		//int index = 0;
		//if (false) for (Dataset d : datasetList2) {
		//	displays[index++] = displayService.createDisplay(d);
		//}

		for (Display d : displays) {
			if (d instanceof DefaultImageDisplay) {
				DefaultImageDisplay defaultImageDisplay = (DefaultImageDisplay) d;
				DataView dataView = defaultImageDisplay.getActiveView();
				dataView.update();
				dataView.rebuild();
			}
			d.update(); // just monkeys with the axes
		}

		//displays[0].

		//display.update();

		//private void setMinMax(final double min, final double max) {
		//	view.setChannelRanges(min, max);
		//	view.getProjector().map();
		//	view.update();
		//}


		Dataset dataset2 = (Dataset) imageSet2.getDatasets().get(0);
		dataset2.setDirty(true);
		System.out.println("FINIS");


		// one more time; see if there's a timing issue with updates
		display.update();

		//TODO end experimental
	}

	/**
	 * Shows special lifetime tool in IJ toolbar.
	 * <p>
	 * Tool allows user to click on grayscale image to fit pixels.
	 */
	private void showTool() {
		Tool tool = getTool();
		if (null != tool) {
			// show the tool
			//TODO
		}
	}

	/**
	 * Hides lifetime tool in IJ toolbar.
	 */
	private void hideTool() {
		Tool tool = getTool();
		if (null != tool) {
			// hide the tool
			//TODO
		}
	}

	/**
	 * Gets the lifetime tool from IJ toolbar.
	 * 
	 * @return 
	 */
	private Tool getTool() {
		return toolService.getTool("aivar"); //TODO ARG find instance of LifetimeTool
	}

	/**
	 * Finds the first lifetime Dataset from the DisplayService and wraps it.
	 * 
	 * @return null or lifetime dataset wrapper
	 */
	private LifetimeDatasetWrapper getLifetimeDatasetWrapper() {
		List<Dataset> datasets = datasetService.getDatasets();
		for (Dataset dataset : datasets) {
			ImgPlus<?> img = dataset.getImgPlus();
			for (int i = 0; i < img.numDimensions(); ++i) {
				if (LIFETIME.equals(img.axis(i).type().getLabel())) {
					LifetimeDatasetWrapper lifetime = null;
					try {
						lifetime = new LifetimeDatasetWrapper(dataset);
					}
					catch (NoLifetimeAxisFoundException e) {
						// we just determined that there is a LIFETIME axis
					}
					finally {
						if (null != lifetime) {
							return lifetime;
						}
					}
				}
			}
		}
		return null;
	}

	/** Tests our command. */
	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = net.imagej.Main.launch(args);

		// Launch the "SLIMPlugin" command right away.
		ij.command().run(SLIMPlugin.class, true);
	}
}
