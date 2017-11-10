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

package loci.slim.histogram;

import loci.slim.fitting.images.IFittedImage;

/**
 * Keeps an array of HistogramDataNodes for a given image. Builds the histogram
 * data as appropriate. Handles updates as the fitted results are available.
 * Handles optional auto-ranging.
 *
 * @author Aivar Grislis
 */
public class HistogramDataGroup {

	private final IFittedImage _image;
	private final String _title;
	private final HistogramDataNode[] _channel;
	private int _channelIndex;
	private boolean _autoRange;
	private boolean _excludePixels;
	private boolean _combineChannels;
	private boolean _displayChannels;
	private double _minView;
	private double _maxView;
	private double _minLUT;
	private double _maxLUT;
	private double _minData;
	private double _maxData;
	private double _minDataCurrent;
	private double _maxDataCurrent;
	private IHistogramDataListener _listener;
	private HistogramStatistics _statistics;

	/**
	 * Constructor, takes an array of HistogramChannels.
	 *
	 */
	public HistogramDataGroup(final IFittedImage image, final String title,
		final HistogramDataNode[] channel)
	{
		_image = image;
		_title = title;
		_channel = channel;
		_autoRange = true;
		_excludePixels = false;
		_combineChannels = hasChannels();
		_displayChannels = hasChannels();
		_channelIndex = 0;
		_minView = _maxView = 0.0;
		_minLUT = _maxLUT = 0.0;
		_minData = _maxData = 0.0;
		_minDataCurrent = _maxDataCurrent = 0.0;
		_listener = null;
	}

	/**
	 * Sets a listener for histogram data changes. Listener is unique.
	 *
	 */
	public void setListener(final IHistogramDataListener listener) {
		_listener = listener;
	}

	/**
	 * Gets a descriptive title to display on histogram UI for this data.
	 *
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * Gets current channel index.
	 *
	 */
	public int getChannelIndex() {
		return _channelIndex;
	}

	/**
	 * Sets current channel index.
	 *
	 */
	public void setChannelIndex(final int channelIndex) {
		_channelIndex = channelIndex;
	}

	/**
	 * Gets whether or not histogram should automatically scale to values.
	 *
	 * @return whether automatically scales
	 */
	public boolean getAutoRange() {
		return _autoRange;
	}

	/**
	 * Sets whether or not histogram should automatically scale to values.
	 *
	 * @param autoRange whether automatically scales
	 */
	public void setAutoRange(final boolean autoRange) {
		update(autoRange, _combineChannels);
	}

	/**
	 * Sets whether or not we should hide out-of-range pixels.
	 *
	 */
	public void setExcludePixels(final boolean excludePixels) {
		_excludePixels = excludePixels;
	}

	/**
	 * Sets whether or not we should hide out-of-range pixels.
	 *
	 */
	public boolean getExcludePixels() {
		return _excludePixels;
	}

	/**
	 * Gets whether or not histogram should combine all the channels.
	 *
	 * @return whether to combine all the channels
	 */
	public boolean getCombineChannels() {
		return _combineChannels;
	}

	/**
	 * Sets whether or not histogram should combine all the channels.
	 *
	 */
	public void setCombineChannels(final boolean combineChannels) {
		update(_autoRange, combineChannels);
	}

	/**
	 * Gets whether or not histogram should display all channels.
	 *
	 */
	public boolean getDisplayChannels() {
		return _displayChannels;
	}

	/**
	 * Sets whether or not histogram should display all channels.
	 *
	 */
	public void setDisplayChannels(final boolean displayChannels) {
		_displayChannels = displayChannels;
	}

	/**
	 * Returns whether or not histogram has channels.
	 *
	 */
	public boolean hasChannels() {
		return _channel.length > 1;
	}

	/*
	 * Helper function to update autoranging or channel combination setting.
	 */
	private void update(final boolean autoRange, final boolean combineChannels) {
		if (_autoRange != autoRange || _combineChannels != combineChannels) {
			double minView = _minView;
			double maxView = _maxView;
			double minLUT = _minLUT;
			double maxLUT = _maxLUT;

			_autoRange = autoRange;
			_combineChannels = combineChannels;

			if (_autoRange) {
				if (_combineChannels) {
					// LUT and view bounded by data for all channels
					minLUT = _minData;
					maxLUT = _maxData;

					minView = minLUT;
					maxView = maxLUT;
				}
				else {
					// LUT is bounded by data for current channel
					minLUT = _minDataCurrent;
					maxLUT = _maxDataCurrent;

					if (_displayChannels) {
						minView = _minData;
						maxView = _maxData;
					}
					else {
						minView = minLUT;
						maxView = maxLUT;
					}
				}
			}
			else {
				// set min/max view/LUT to quartile-based fences
				if (null != _statistics) {
					final double[] fences = _statistics.getFences();
					minView = minLUT = fences[0];
					maxView = maxLUT = fences[1];
					if (minView < _minData) {
						minView = minLUT = _minData;
					}
					if (maxView > _maxData) {
						maxView = maxLUT = _maxData;
					}
				}
			}

			// did anything really change?
			if (_minView != minView || _maxView != maxView || _minLUT != minLUT ||
				_maxLUT != maxLUT)
			{
				_minView = minView;
				_maxView = maxView;
				_minLUT = minLUT;
				_maxLUT = maxLUT;

				// update listener, if any
				if (null != _listener) {
					_listener.minMaxChanged(this, minView, maxView, minLUT, maxLUT);
				}
			}
		}
	}

	/**
	 * Gets minimum and maximum extents of the view.
	 *
	 */
	public double[] getMinMaxView() {
		return new double[] { _minView, _maxView };
	}

	/**
	 * Sets minimum and maximum extents of the view.
	 *
	 */
	public void setMinMaxView(final double min, final double max) {
		_minView = min;
		_maxView = max;
	}

	/**
	 * Gets minimum and maximum extents of the LUT.
	 *
	 */
	public double[] getMinMaxLUT() {
		return new double[] { _minLUT, _maxLUT };
	}

	/**
	 * Sets minimum and maximum extents of the LUT.
	 *
	 */
	public void setMinMaxLUT(final double min, final double max) {
		_minLUT = min;
		_maxLUT = max;
		redisplay();
	}

	/**
	 * Gets minimum and maximum extents of the data.
	 *
	 */
	public double[] getMinMaxData() {
		return new double[] { _minData, _maxData };
	}

	// TODO
	// this is setting exclude pixels on/off once you are viewing the histogram
	// if you click on another image and the histogram shows data for that image
	// that doesn't necessarily trigger mask propagation.
	//
	// Note that this is just setting up pixel masking for one channel. Shouldn't
	// this pertain to all channels? (With different mask for each channel).
	// The other UI is not channel-specific, so neither should this checkbox be.
	// When/if we do have different LUTs for each channel this will get even more
	// complicated.
	//
	// Note also that if this is to work during the fit process it gets more
	// complicated. As more and more pixels are drawn, we need to build newer
	// exclusion masks and propagate them.

	public void excludePixels(final boolean excludePixels) {
		setExcludePixels(excludePixels);
		if (excludePixels) {
			_channel[_channelIndex].propagateMask(_minLUT, _maxLUT);
		}
		else {
			_channel[_channelIndex].rescindMask();
		}
	}

	/**
	 * Sets the current min and max automatically if need be. Called periodically
	 * during the fit process. Updates listener as a side effect.
	 *
	 * @return min and max of the LUT
	 */
	// TODO these recalculation events need to be synchronized so that they
	// don't step on other ways to change min/maxLUT/View
	// Perhaps this class should have a synch object that also has a getter.
	public double[] updateRanges() {
		double minData;
		double maxData;
		double minDataCurrent;
		double maxDataCurrent;
		double[] minMaxData;

		minData = maxData = 0.0;
		minDataCurrent = maxDataCurrent = 0.0;

		if (1 < _channel.length && (_displayChannels || _combineChannels)) {
			minData = Double.MAX_VALUE;
			maxData = -Double.MAX_VALUE;

			// calculate actual minimum and maximum for all channels
			for (int i = 0; i < _channel.length; ++i) {
				minMaxData = _channel[i].findMinMax();
				if (minMaxData[0] < minData) {
					minData = minMaxData[0];
				}
				if (minMaxData[1] > maxData) {
					maxData = minMaxData[1];
				}
				if (i == _channelIndex) {
					minDataCurrent = minMaxData[0];
					maxDataCurrent = minMaxData[1];
				}
			}
		}
		else {
			// calculate actual minimum and maximum for current channel
			minMaxData = _channel[_channelIndex].findMinMax();
			minData = minDataCurrent = minMaxData[0];
			maxData = maxDataCurrent = minMaxData[1];
		}

		if (_autoRange) {
			if (_combineChannels) {
				// LUT and view bounded by data for all channels
				_minLUT = minData;
				_maxLUT = maxData;
			}
			else {
				// LUT is bounded by data for current channel
				_minLUT = minDataCurrent;
				_maxLUT = maxDataCurrent;
			}

			_minView = minData;
			_maxView = maxData;
		}
		_minData = minData;
		_maxData = maxData;

		_minDataCurrent = minDataCurrent;
		_maxDataCurrent = maxDataCurrent;

		if (null != _listener) {
			_listener.minMaxChanged(this, _minView, _maxView, _minLUT, _maxLUT);
		}

		return new double[] { _minLUT, _maxLUT };
	}

	public void redisplay() {
		_image.redisplay();
	}

	public HistogramStatistics getStatistics(final int binCount) {
		_statistics = new HistogramStatistics();
		int[] bins = null;
		double[] binValues = null;
		final double[] quartiles = new double[3];
		final int[] quartileIndices = new int[3];

		if (_displayChannels) {
			// start new histogram bins
			bins = new int[binCount];
			for (int i = 0; i < binCount; ++i) {
				bins[i] = 0;
			}

			// add all channels to histogram
			for (int i = 0; i < _channel.length; ++i) {
				final int[] channelBins =
					_channel[i].binValues(binCount, _minView, _maxView);
				for (int j = 0; j < binCount; ++j) {
					bins[j] += channelBins[j];
				}
			}
		}
		else {
			// just show current channel in histogram
			bins = _channel[_channelIndex].binValues(binCount, _minView, _maxView);

			// figure values for bins
			binValues = new double[binCount];
			for (int i = 0; i < binCount; ++i) {
				binValues[i] = _minView + i * (_maxView - _minView) / binCount;
			}
		}

		// get median and quartiles of current channel
		_channel[_channelIndex].findQuartiles(quartiles, quartileIndices, binCount,
			_minView, _maxView);

		// report results
		_statistics.setBins(bins);
		_statistics.setBinValues(binValues);
		_statistics.setQuartileIndices(quartileIndices);
		_statistics.setQuartiles(quartiles);
		return _statistics;
	}
}
