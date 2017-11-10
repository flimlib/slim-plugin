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

package loci.slim2.decay;

import io.scif.DefaultMetaTable;
import io.scif.MetaTable;
import io.scif.io.RandomAccessInputStream;
import io.scif.lifesci.SDTInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;

/**
 * Wraps a {@link Dataset} that has lifetime information.
 *
 * @author Aivar Grislis
 */
public class LifetimeDatasetWrapper {

	private static final String SDT_SUFFIX = ".sdt";
	private static final String LIFETIME = "Lifetime";
	private static final String TIME_BASE = "time base";
	private static final String INCR = "MeasureInfo.incr";
	private static final double DEFAULT_TIME_BASE = 10.0;
	private static final double DECIMAL_ADJUST = 1.0E9;
	private static final int IMPOSSIBLE_INDEX = -1;
	private static final int MAX_BIN_WIDTH = 21;
	private static final int MAX_CACHE_SIZE = 1000;

	private Dataset dataset;
	private RandomAccess<? extends RealType<?>> randomAccess;
	private DecayCache cache;
	private int lifetimeDimension;
	private AxisType[] externalAxes;
	private long[] internalDimensions;
	private long[] externalDimensions;
	private int bins;
	private int factor;
	private double inc;

	/**
	 * Constructor. Wraps a pre-existing Dataset.
	 *
	 */
	public LifetimeDatasetWrapper(final Dataset dataset)
		throws NoLifetimeAxisFoundException
	{
		init(dataset);
		setTimeIncrement(DEFAULT_TIME_BASE / bins);
		setPhotonCountFactor(1);
	}

	/**
	 * Constructor. Loads a Dataset and wraps it.
	 *
	 * @throws IOException
	 * @throws NoLifetimeAxisFoundException
	 */
	public LifetimeDatasetWrapper(final Context context, final File file)
		throws IOException, NoLifetimeAxisFoundException
	{
		final String fileName = file.getAbsolutePath();

		// load and initialize with the dataset
		final DatasetService datasetService =
			context.getService(DatasetService.class);
		final Dataset dataset = datasetService.open(fileName);
		init(dataset);

		// get metadata
		double time = DEFAULT_TIME_BASE;
		int factor = 1;
		if (fileName.endsWith(SDT_SUFFIX)) {
			final RandomAccessInputStream stream =
				new RandomAccessInputStream(context, fileName);
			stream.order(true);
			final MetaTable metaTable = new DefaultMetaTable();
			final SDTInfo sdtInfo = new SDTInfo(stream, metaTable);
			if (0 != sdtInfo.tacG) {
				time = DECIMAL_ADJUST * sdtInfo.tacR / sdtInfo.tacG;
			}
			// dumpMetaTable(metaTable);
			final Number photonFactor = (Number) metaTable.get(INCR);
			if (null != photonFactor) {
				factor = photonFactor.intValue();
			}
		}
		setTimeIncrement(time / bins);
		setPhotonCountFactor(factor);
	}

	private void dumpMetaTable(final MetaTable metaTable) {
		final Set<String> keys = metaTable.keySet();
		for (final String key : keys) {
			System.out
				.println("key >" + key + "> entry <" + metaTable.get(key) + ">");
		}
	}

	private void init(final Dataset dataset) throws NoLifetimeAxisFoundException {
		this.dataset = dataset;
		randomAccess = dataset.getImgPlus().randomAccess();
		cache = new DecayCache(this, MAX_BIN_WIDTH, MAX_CACHE_SIZE);

		// find lifetime axis
		lifetimeDimension = IMPOSSIBLE_INDEX;
		final ImgPlus<?> img = dataset.getImgPlus();
		final List<AxisType> externalAxesList = new ArrayList<AxisType>();
		int i = 0;
		for (int j = 0; j < img.numDimensions(); ++j) {
			if (LIFETIME.equals(img.axis(j).type().getLabel())) {
				// got lifetime axis
				lifetimeDimension = j;
			}
			else {
				// save other axes
				externalAxesList.add(img.axis(j).type());
			}
		}
		// can't proceed without a lifetime axis
		if (IMPOSSIBLE_INDEX == lifetimeDimension) {
			// ask the user which one to use
			final int dimension = chooseLifetimeDimensionUI(externalAxesList);

			if (IMPOSSIBLE_INDEX == dimension) {
				throw new NoLifetimeAxisFoundException("No LIFETIME axis for " +
					dataset.getName());
			}
			lifetimeDimension = dimension;
			externalAxesList.remove(dimension);
		}
		externalAxes = externalAxesList.toArray(new AxisType[0]);

		// TODO ARG this could be done with hyperslice cursor to find bin 0 of
		// decay, another cursor that is limited to cruise the lifetime dim
		// that gets transformed (or origined) into place.

		/*
		MixedTransformView<? extends RealType<?>> r = null;
		MixedTransformView<?> r = null;
		r = Views.<?>hyperSlice(randomAccess, lifetimeDimension, 0);
		Object rr = Views.hyperSlice(randomAccess, lifetimeDimension, 0L);

		RandomAccessibleInterval< T > view =
		Views.interval( randomAccess, new long[] { 0, 0,1 }, new long[]{ randomAccess.dimension(0), randomAccess.dimension(1),1 } );
		*/

		// get 'internal' (with lifetime) and 'external' (no lifetime) dimensions
		internalDimensions = new long[img.numDimensions()];
		img.dimensions(internalDimensions);
		externalDimensions = new long[internalDimensions.length - 1];
		i = 0;
		for (int j = 0; j < internalDimensions.length; ++j) {
			if (j != lifetimeDimension) {
				externalDimensions[i++] = internalDimensions[j];
			}
		}

		bins = (int) internalDimensions[lifetimeDimension];
	}

	/**
	 * Return wrapped {@link Dataset}.
	 *
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Returns external dimensions (i.e. w/o lifetime dimension).
	 *
	 */
	public long[] getDims() {
		return externalDimensions;
	}

	/**
	 * Returns external axis types (i.e. w/o lifetime dimension).
	 * 
	 */
	public AxisType[] getAxes() {
		return externalAxes;
	}

	/**
	 * Returns number of lifetime bins.
	 *
	 */
	public int getBins() {
		return bins;
	}

	/**
	 * Sets photon count factor.
	 *
	 */
	private void setPhotonCountFactor(final int factor) {
		this.factor = factor;
	}

	/**
	 * Gets photon count factor.
	 *
	 */
	public int getPhotonCountFactor() {
		return factor;
	}

	/**
	 * Sets bin width as time.
	 *
	 */
	private void setTimeIncrement(final double inc) {
		this.inc = inc;
	}

	/**
	 * Gets bin width as time.
	 *
	 */
	public double getTimeIncrement() {
		return inc;
	}

	/**
	 * Gets decay histogram at given location.
	 *
	 * @param tail rest of position
	 */
	public double[] getDecay(final long x, final long y, final long[] tail) {
		final long[] position = new long[tail.length + 2];
		int i = 0;
		position[i++] = x;
		position[i++] = y;
		for (final long t : tail) {
			position[i++] = t;
		}
		return getDecay(position);
	}

	/**
	 * Gets decay histogram at given location.
	 *
	 */
	public double[] getDecay(final long[] position) {
		return getDecayFromInternalPosition(internalPosition(position));
	}

	/**
	 * Gets decay histogram at given location. Bins up the decay from surrounding
	 * pixels.
	 *
	 * @param binSize 0=no binning, 1=3x3, 2=5x5, etc.
	 */
	public double[] getBinnedDecay(final int binSize, final long[] position) {
		if (0 == binSize) {
			return getDecay(position);
		}
		final long xAnchor = position[0];
		final long yAnchor = position[1];
		return combineDecay(0, Integer.MAX_VALUE, xAnchor - binSize, xAnchor +
			binSize, yAnchor - binSize, yAnchor + binSize, position);
	}

	/**
	 * Gets decay histogram summed for the whole plane.
	 *
	 */
	public double[] getCombinedPlaneDecay(final int thresholdMin,
		final int thresholdMax, final long[] position)
	{
		final long x0 = 0;
		final long x1 = getDims()[0];
		final long y0 = 0;
		final long y1 = getDims()[1];
		return combineDecay(thresholdMin, thresholdMax, x0, x1, y0, y1, position);
	}

	/**
	 * Gets summed decay histogram at given location, with binning.
	 *
	 * @param binSize 0=no binning, 1=3x3, 2=5x5, etc.
	 */
	public int getSummedDecay(final int binSize, final long[] position) {
		final double[] decay = getBinnedDecay(binSize, position);
		long sum = 0;
		for (final double d : decay) {
			sum += d;
		}
		// avoid overflow; squash highlights
		if (sum > Integer.MAX_VALUE) {
			sum = Integer.MAX_VALUE;
		}
		return (int) sum;
	}

	private int chooseLifetimeDimensionUI(final List<AxisType> dimensions) {
		// skip initial 2 dimensions, which are X and Y
		final String[] choices = new String[dimensions.size() - 2];
		for (int i = 0; i < choices.length; ++i) {
			choices[i] = dimensions.get(i + 2).getLabel();
		}
		final String input =
			(String) JOptionPane.showInputDialog(null,
				"Choose Lifetime Dimension...", "Unknown Lifetime Dimension",
				JOptionPane.QUESTION_MESSAGE, null, // Use default icon
				choices, // Array of choices
				choices[0]); // Initial choice
		int dimension = IMPOSSIBLE_INDEX;
		for (int i = 0; i < choices.length; ++i) {
			// input is null when Canceled
			if (choices[i].equals(input)) {
				dimension = i + 2;
				break;
			}
		}
		return dimension;
	}

	/**
	 * Helper routine, creates combined decay from planar, rectangular area of
	 * pixels.
	 *
	 */
	private double[]
		combineDecay(final int thresholdMin, final int thresholdMax, final long x0,
			final long x1, final long y0, final long y1, final long[] pos)
	{
		final long[] position = pos.clone(); // preserve incoming position
		final double[] combinedDecay = new double[bins];
		for (int i = 0; i < bins; ++i) {
			combinedDecay[i] = 0.0;
		}
		double[] decay;
		for (long x = x0; x < x1; ++x) {
			for (long y = y0; y < y1; ++y) {
				if (x >= 0 && x < getDims()[0] && y >= 0 && y < getDims()[1]) {
					position[0] = x;
					position[1] = y;
					decay = cache.getDecay(position);
					if (withinThreshold(thresholdMin, thresholdMax, decay)) {
						for (int i = 0; i < bins; ++i) {
							combinedDecay[i] += decay[i];
						}
					}
				}
			}
		}
		return combinedDecay;
	}

	/**
	 * Expands external position (without lifetime dimension) to internal position
	 * (with lifetime dimension).
	 *
	 */
	private long[] internalPosition(final long[] position) {
		final long[] internalPosition = new long[position.length + 1];
		for (int i = 0; i < lifetimeDimension; ++i) {
			internalPosition[i] = position[i];
		}
		for (int i = lifetimeDimension + 1; i < internalPosition.length; ++i) {
			internalPosition[i] = position[i - 1];
		}
		return internalPosition;
	}

	/**
	 * Gets the decay histogram at given location.
	 *
	 */
	private double[] getDecayFromInternalPosition(final long[] pos) {
		final long[] position = pos.clone(); // preserve incoming position
		final double[] decay = new double[bins];
		for (int i = 0; i < bins; ++i) {
			position[lifetimeDimension] = i;
			randomAccess.setPosition(position);
			decay[i] = randomAccess.get().getRealDouble() / factor;
		}
		return decay;
	}

	/**
	 * Checks if decay is within threshold limits.
	 *
	 */
	private boolean withinThreshold(final int thresholdMin,
		final int thresholdMax, final double[] decay)
	{
		double sum = 0.0;
		for (final double d : decay) {
			sum += d;
		}
		// TODO ARG was sum > thresholdMin; c/b TRI2 compatibility issue
		return ((sum >= thresholdMin) && (sum <= thresholdMax));
	}

	/**
	 * Inner cache class. Binning is very slow without caching. However this cache
	 * only works well if you are accessing pixels sequentially. Thanks to Tino
	 * at: http://www.java-blog.com/creating-simple-cache-java-linkedhashmap-
	 * anonymous-class
	 */
	private class DecayCache {

		private final int cacheSize;
		private final LifetimeDatasetWrapper wrapper;
		private final long xDim;
		private final LinkedHashMap<Long, double[]> cache;

		/**
		 * Constructor.
		 *
		 * @param wrapper source of decays
		 * @param size e.g. a 3x3 binning would be size 3
		 * @param xDim dimension of x axis
		 */
		public DecayCache(final LifetimeDatasetWrapper wrapper, final int size,
			final long xDim)
		{
			this.wrapper = wrapper;
			this.cacheSize = size * size;
			this.xDim = xDim;
			this.cache = new LinkedHashMap<Long, double[]>(cacheSize, 0.75f, true) {

				@Override
				protected boolean removeEldestEntry(
					final Map.Entry<Long, double[]> eldest)
				{
					// Remove the eldest entry if the size of the cache exceeds the
					// maximum size
					return size() > cacheSize;
				}
			};
		}

		/**
		 * Clears the cache; should be called for each new plane.
		 */
		public void clear() {
			cache.clear();
		}

		/**
		 * Gets the decay at a given position.
		 *
		 */
		public double[] getDecay(final long[] position) {
			double[] decay = null;
			final long x = position[0];
			final long y = position[1];
			final long key = x * xDim + y;

			decay = cache.get(key);

			if (null == decay) {
				decay = wrapper.getDecay(position);
				cache.put(key, decay);
			}
			return decay;
		}
	}
}
