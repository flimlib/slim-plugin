/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
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

package loci.slim;

import ij.IJ;

import io.scif.ByteArrayPlane;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.Metadata;
import io.scif.SCIFIO;
import io.scif.Writer;
import io.scif.filters.PlaneSeparator;
import io.scif.filters.ReaderFilter;
import io.scif.img.axes.SCIFIOAxes;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;

import org.scijava.util.Bytes;

/**
 * Loads and saves excitation files.
 *
 * @author Aivar Grislis
 */
public class ExcitationFileUtility {

	private static final String ICS = ".ics";
	private static final String IRF = ".irf";

	public static Excitation
		loadExcitation(String fileName, final double timeInc)
	{
		Excitation excitation = null;
		double values[] = null;
		if (fileName.toLowerCase().endsWith(ICS)) {
			values = loadICSExcitationFile(fileName);
		}
		else {
			if (!fileName.toLowerCase().endsWith(IRF)) {
				fileName += IRF;
			}
			values = loadIRFExcitationFile(fileName);
		}
		if (null != values) {
			excitation = new Excitation(fileName, values, timeInc);
		}
		return excitation;
	}

	public static Excitation createExcitation(String fileName,
		final double[] values, final double timeInc)
	{
		Excitation excitation = null;
		boolean success = false;
		if (fileName.endsWith(ICS)) {
			success = saveICSExcitationFile(fileName, values);
		}
		else {
			if (!fileName.endsWith(IRF)) {
				fileName += IRF;
			}
			success = saveIRFExcitationFile(fileName, values);
		}
		if (success) {
			excitation = new Excitation(fileName, values, timeInc);
		}
		return excitation;
	}

	private static double[] loadICSExcitationFile(final String fileName) {
		double[] results = null;
		try {
			final SCIFIO scifio = new SCIFIO();
			final ReaderFilter reader =
				scifio.initializer().initializeReader(fileName);
			reader.enable(PlaneSeparator.class).separate(axesToSplit(reader));
			final ImageMetadata meta = reader.getMetadata().get(0);
			final int bitsPerPixel = meta.getBitsPerPixel();
			final boolean littleEndian = meta.isLittleEndian();
			// CTR FIXME use ChannelSeparator to prevent interleaved
			final boolean interleaved = false; // meta.isInterleaved();
			final long lifetimeLength = meta.getAxisLength(SCIFIOAxes.LIFETIME);
			if (lifetimeLength > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Lifetime dimension too large: " +
					lifetimeLength);
			}
			final int bins = (int) lifetimeLength;
			results = new double[bins];
			byte bytes[];

			for (int bin = 0; bin < bins; ++bin) {
				bytes = reader.openPlane(0, bin).getBytes();
				results[bin] =
					Bytes.toDouble(bytes, 0, bitsPerPixel, littleEndian);
			}
			reader.close();
		}
		catch (final IOException e) {
			IJ.log("IOException " + e.getMessage());
		}
		catch (final FormatException e) {
			IJ.log("FormatException " + e.getMessage());
		}
		return results;
	}

	// TODO doesn't work; needed to interoperate with TRI2
	private static boolean saveICSExcitationFile(final String fileName,
		final double[] values)
	{
		boolean success = false;
		final SCIFIO scifio = new SCIFIO();
		// NB: Use a fake string as a shorthand for metadata values.
		final String source =
			"pixelType=uint16&lengths=1,1," + values.length +
				"&axes=X,Y,Lifetime.fake";
		try {
			final Writer writer =
				scifio.initializer().initializeWriter(source, fileName);
			// TODO: Writer may require bytes to be structured according to a
			// particular endianness. But at this point, is it possible yet to
			// interrogate the writer to ask for its desired endianness?
			final boolean little = true;
			final ByteArrayPlane plane = new ByteArrayPlane(scifio.getContext());
			for (int bin = 0; bin < values.length; ++bin) {
				plane.setData(Bytes.fromDouble(values[bin], little));
				writer.savePlane(0, bin, plane);
			}
			success = true;
		}
		catch (final IOException e) {
			IJ.log("IOException " + e.getMessage());
		}
		catch (final FormatException e) {
			IJ.log("FormatException " + e.getMessage());
		}
		return success;
	}

	private static double[] loadIRFExcitationFile(final String fileName) {
		double[] values = null;
		try {
			final ArrayList<Float> valuesArrayList = new ArrayList<Float>();
			final Scanner scanner = new Scanner(new FileReader(fileName));
			String line = null;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				valuesArrayList.add(Float.parseFloat(line));
			}
			values = new double[valuesArrayList.size()];
			for (int i = 0; i < valuesArrayList.size(); ++i) {
				values[i] = valuesArrayList.get(i);
			}
		}
		catch (final Exception e) {
			IJ.log("Exception " + e.getMessage());
		}
		return values;
	}

	private static boolean saveIRFExcitationFile(final String fileName,
		final double[] values)
	{
		boolean success = false;
		try {
			final FileWriter writer = new FileWriter(fileName);
			for (int i = 0; i < values.length; ++i) {
				if (i > 0) {
					writer.append('\n');
				}
				writer.append(Double.toString(values[i]));
			}
			writer.flush();
			writer.close();
			success = true;
		}
		catch (final IOException e) {
			IJ.log("IOException " + e.getMessage());
		}
		return success;
	}

	private static AxisType[] axesToSplit(final ReaderFilter r) {
		final Set<AxisType> axes = new HashSet<AxisType>();
		final Metadata meta = r.getTail().getMetadata();
		// Split any non-X,Y axis
		for (final CalibratedAxis t : meta.get(0).getAxesPlanar()) {
			final AxisType type = t.type();
			if (!(type == Axes.X || type == Axes.Y)) {
				axes.add(type);
			}
		}
		// Ensure channel is attempted to be split
		axes.add(Axes.CHANNEL);
		return axes.toArray(new AxisType[axes.size()]);
	}
}
