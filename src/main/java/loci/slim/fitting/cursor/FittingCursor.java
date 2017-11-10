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

package loci.slim.fitting.cursor;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import loci.curvefitter.IFitterEstimator;
import loci.slim.SLIMProcessor;
import loci.slim.heuristics.FitterEstimator;

/**
 * Keeps cursor information. Cursors mark the sections of the excitation
 * histogram and the transient histogram that will be used for fitting.
 *
 * @author Aivar Grislis
 */
public class FittingCursor {

	private static final String DOUBLE_ZERO_STRING = "0.0";
	private static final String INTEGER_ZERO_STRING = "0";
	private static final int DECIMAL_PLACES = 4;
	private final double _inc;
	private final int _bins;
	private final Set<IFittingCursorListener> _listeners;
	private final IFitterEstimator _fitterEstimator;
	private boolean _showBins;
	private volatile boolean _suspend;
	private boolean _hasPrompt;
	private int _promptStartBin;
	private int _promptStopBin;
	private int _transientStartBin;
	private int _dataStartBin;
	private int _transientStopBin;
	private double _promptStartValue;
	private double _promptStopValue;
	private double _promptBaselineValue;
	private double _transientStartValue;
	private double _dataStartValue;
	private double _transientStopValue;
	// TODO ARG
	private final boolean _kludge = true;

	/**
	 * Constructor
	 *
	 * @param inc time increment per bin
	 * @param bins total number of bins
	 * @params fitterEstimator
	 */
	public FittingCursor(final double inc, final int bins,
		final IFitterEstimator fitterEstimator)
	{
		_inc = inc;
		_bins = bins;
		_fitterEstimator = fitterEstimator;
		_listeners = new HashSet<IFittingCursorListener>();
		_showBins = false;
		_suspend = false;
		_hasPrompt = false;
	}

	/**
	 * Adds a listener for cursor changes.
	 *
	 */
	public void addListener(final IFittingCursorListener listener) {
		synchronized (_listeners) {
			if (!_listeners.contains(listener)) {
				_listeners.add(listener);
			}
		}
	}

	/**
	 * Removes a listener for cursor changes.
	 *
	 */
	public void removeListener(final IFittingCursorListener listener) {
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	/**
	 * Sets whether the UI will display bins or time values.
	 *
	 */
	public void setShowBins(final boolean showBins) {
		_showBins = showBins;
	}

	/**
	 * Gets whether the UI will display bins or time values.
	 *
	 */
	public boolean getShowBins() {
		return _showBins;
	}

	/**
	 * Temporarily suspends listener notifications. Used when several cursors
	 * change at the same time. Call suspendNotifications followed by cursor
	 * changes followed by sendNotifications.
	 */
	public void suspendNotifications() {
		_suspend = true;
	}

	/**
	 * Used to send listener notifications after a batched change.
	 */
	public void sendNotifications() {
		_suspend = false;
		notifyListeners();
	}

	/**
	 * Returns whether or not a prompt has been loaded.
	 *
	 */
	public void setHasPrompt(final boolean hasPrompt) {
		_hasPrompt = hasPrompt;
	}

	/**
	 * Sets whether or not a prompt has been loaded.
	 *
	 */
	public boolean getHasPrompt() {
		return _hasPrompt;
	}

	/**
	 * Sets the start of the prompt based on a prompt delay string. Handles bins
	 * or time values.
	 *
	 */
	public void setPromptDelay(final String promptDelay) {

		Double promptDelayValue = null;
		if (_showBins) {
			final Integer parsedInteger = getIntegerValue(promptDelay);
			if (null != parsedInteger) {
				promptDelayValue = _fitterEstimator.binToValue(parsedInteger, _inc);
			}
		}
		else {
			promptDelayValue = getDoubleValue(promptDelay);
		}
		if (null != promptDelayValue) {
			// convert delay to start
			final double promptStartValue = promptDelayValue + _transientStartValue;

			// some very rudimentary error-checking
			if (0.0 < promptStartValue && promptStartValue < _transientStopValue) {
				final double diff = promptStartValue - _promptStartValue;
				_promptStartValue += diff;
				_promptStopValue += diff;
			}
		}
		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the start of the prompt as a string showing prompt delay. Handles bins
	 * or time values.
	 *
	 */
	public String getPromptDelay() {
		final StringBuilder returnValue = new StringBuilder();
		if (_showBins) {
			if (_hasPrompt) {
				final int delay = getPromptStartBin() - getTransientStartBin();
				returnValue.append(delay);
			}
			else {
				returnValue.append(INTEGER_ZERO_STRING);
			}
		}
		else {
			if (_hasPrompt) {
				double delay = getPromptStartValue() - getTransientStartValue();
				delay = _fitterEstimator.roundToDecimalPlaces(delay, DECIMAL_PLACES);
				returnValue.append(delay);
			}
			else {
				returnValue.append(DOUBLE_ZERO_STRING);
			}
		}
		return returnValue.toString();
	}

	/**
	 * Sets the start of the prompt as a bin number.
	 *
	 */
	public void setPromptStartBin(final int bin) {
		_promptStartValue = _fitterEstimator.binToValue(bin, _inc);
		notifyListeners();
	}

	/**
	 * Gets the start of the prompt as a bin number.
	 *
	 */
	public int getPromptStartBin() {
		int returnValue = 0;
		if (_hasPrompt) {
			returnValue = _fitterEstimator.valueToBin(_promptStartValue, _inc);

			/*int tmp = (int) Math.ceil(_promptStartValue / _inc);

			if (returnValue != tmp) {
				IJ.log("*******getPromptStartBin********");
				IJ.log("SP " + returnValue + " TRI2 " + tmp);
				if (_kludge) {
					IJ.log("go with " + tmp);
					returnValue = tmp;
				}
			}*/
		}
		return returnValue;
	}

	/**
	 * Sets the start of the prompt as a time value.
	 *
	 */
	public void setPromptStartValue(final double value) {
		_promptStartValue = value;
		notifyListeners();
	}

	/**
	 * Gets the start of the prompt as a time value.
	 *
	 */
	public double getPromptStartValue() {
		double returnValue = 0.0;
		if (_hasPrompt) {
			returnValue = _promptStartValue;
		}
		return returnValue;
	}

	/**
	 * Sets the end of the prompt based on a prompt width string. Handles bins or
	 * time values (that's why we are parsing a string vs taking an int or float
	 * parameter).
	 *
	 */
	public void setPromptWidth(final String promptWidth) {
		Double promptWidthValue = null;
		if (_showBins) {
			final Integer parsedInteger = getIntegerValue(promptWidth);
			if (null != parsedInteger) {
				promptWidthValue = _inc * parsedInteger;
			}
		}
		else {
			promptWidthValue = getDoubleValue(promptWidth);
		}
		if (null != promptWidthValue) {
			final double promptStopValue = getPromptStartValue() + promptWidthValue;
			if (promptStopValue >= _promptStartValue &&
				promptStopValue <= _bins * _inc)
			{
				_promptStopValue = promptStopValue;
			}
		}
		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the end of the prompt as a string showing prompt width. Handles bins
	 * or time values.
	 *
	 */
	public String getPromptWidth() {
		final StringBuilder returnValue = new StringBuilder();
		if (_showBins) {
			if (_hasPrompt) {
				final int width = getPromptStopBin() - getPromptStartBin();
				returnValue.append(width);
			}
			else {
				returnValue.append(INTEGER_ZERO_STRING);
			}
		}
		else {
			if (_hasPrompt) {
				double width = getPromptStopValue() - getPromptStartValue();
				width = _fitterEstimator.roundToDecimalPlaces(width, DECIMAL_PLACES);
				returnValue.append(width);
			}
			else {
				returnValue.append(DOUBLE_ZERO_STRING);
			}
		}
		return returnValue.toString();
	}

	/**
	 * Sets the end of the prompt as a bin number.
	 *
	 */
	public void setPromptStopBin(final int bin) {
		_promptStopValue = _fitterEstimator.binToValue(bin, _inc);
		notifyListeners();
	}

	/**
	 * Gets the end of the prompt as a bin number.
	 *
	 */
	public int getPromptStopBin() {
		int returnValue = 0;
		if (_hasPrompt) {
			returnValue =
				((FitterEstimator) _fitterEstimator).valueToBin(_promptStopValue, _inc);
		}
		return returnValue;
	}

	/**
	 * Sets the end of the prompt as a time value.
	 *
	 */
	public void setPromptStopValue(final double value) {
		_promptStopValue = value;
		notifyListeners();
	}

	/**
	 * Gets the end of the prompt as a time value.
	 *
	 */
	public double getPromptStopValue() {
		double returnValue = 0.0;
		if (_hasPrompt) {
			returnValue = _promptStopValue;
		}
		return returnValue;
	}

	/**
	 * Sets the baseline of the prompt based on a string.
	 *
	 */
	public void setPromptBaseline(final String promptBaseline) {
		final Double promptBaselineValue = getDoubleValue(promptBaseline);
		if (null != promptBaselineValue) {
			_promptBaselineValue = promptBaselineValue;
		}
		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the baseline of the prompt based on a string.
	 *
	 */
	public String getPromptBaseline() {
		String returnValue = DOUBLE_ZERO_STRING;
		if (_hasPrompt) {
			returnValue = "" + getPromptBaselineValue();
		}
		return returnValue;
	}

	/**
	 * Sets the baseline of the prompt as a value. Note that this value is
	 * actually a photon count and not based on bins or time values.
	 *
	 */
	public void setPromptBaselineValue(final double value) {
		_promptBaselineValue = value;
		notifyListeners();
	}

	/**
	 * Gets the baseline of the prompt as a value.
	 *
	 */
	public double getPromptBaselineValue() {
		double returnValue = 0.0;

		if (!SLIMProcessor.macroParams.isPromptBaseLineMacroused) {
			if (_hasPrompt) {
				returnValue =
					_fitterEstimator.roundToDecimalPlaces(_promptBaselineValue,
						DECIMAL_PLACES);
			}
			return returnValue;
		}

		else {
			return _fitterEstimator.roundToDecimalPlaces(SLIMProcessor.macroParams
				.getPromptBaseLine(), DECIMAL_PLACES);
		}
	}

	/**
	 * Sets the start of the transient based on a string. Handles bins or time
	 * values.
	 *
	 */
	public void setTransientStart(final String transientStart) {
		if (_showBins) {
			final Integer bin = getIntegerValue(transientStart);
			if (null != bin) {
				if (bin <= _dataStartBin && bin >= 0) {
					_transientStartBin = bin;
					_transientStartValue = _fitterEstimator.binToValue(bin, _inc);
				}
			}
		}
		else {
			final Double value = getDoubleValue(transientStart);
			if (null != value) {
				if (value <= _dataStartValue && value >= 0.0) {
					_transientStartValue = value;
					_transientStartBin = _fitterEstimator.valueToBin(value, _inc);
				}
			}
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the start of the transient as a string. Handles bins or time values.
	 *
	 */
	public String getTransientStart() {
		final StringBuilder returnValue = new StringBuilder();
		if (_showBins) {
			returnValue.append(getTransientStartBin());
		}
		else {
			returnValue.append(getTransientStartValue());
		}
		return returnValue.toString();
	}

	/**
	 * Sets the start of the transient as a bin number.
	 *
	 */
	public void setTransientStartBin(final int bin) {
		_transientStartBin = bin;
		_transientStartValue = _fitterEstimator.binToValue(bin, _inc);

		notifyListeners();
	}

	/**
	 * Gets the start of the transient as a bin number.
	 *
	 */
	public int getTransientStartBin() {

		if (SLIMProcessor.macroParams.transientStartMacroUsed) {
			return _fitterEstimator.valueToBin(SLIMProcessor.macroParams
				.getTransientStartFromMacro(), _inc);
		}
		else return _transientStartBin;

	}

	/**
	 * Sets the start of the transient as a time value.
	 *
	 */
	public void setTransientStartValue(final double value) {
		_transientStartValue =
			_fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
		_transientStartBin = _fitterEstimator.valueToBin(value, _inc);

		notifyListeners();
	}

	/**
	 * Gets the start of the transient as a time value.
	 *
	 */
	public double getTransientStartValue() {
		return _transientStartValue;
	}

	/**
	 * Sets the start of the data based on a string. Handles bins or time values.
	 *
	 */
	public void setDataStart(final String dataStart) {
		if (_showBins) {
			final Integer bin = getIntegerValue(dataStart);
			if (null != bin) {
				if (bin >= _transientStartBin && bin <= _transientStopBin) {
					_dataStartBin = bin;
					_dataStartValue = _fitterEstimator.binToValue(bin, _inc);
				}
			}
		}
		else {
			final Double value = getDoubleValue(dataStart);
			if (null != value) {
				if (value >= _transientStartValue && value <= _transientStopValue) {
					_dataStartValue = value;
					_dataStartBin = _fitterEstimator.valueToBin(value, _inc);
				}
			}
		}

		// either update with new valid values or undo invalid values
		notifyListeners();
	}

	/**
	 * Gets the start of the data as a string. Handles bins or time values.
	 *
	 */
	public String getDataStart() {
		final StringBuilder returnValue = new StringBuilder();
		if (_showBins) {
			returnValue.append(getDataStartBin());
		}
		else {
			returnValue.append(getDataStartValue());
		}
		return returnValue.toString();
	}

	/**
	 * Sets the start of the data as a bin number.
	 *
	 */
	public void setDataStartBin(final int bin) {
		_dataStartBin = bin;
		_dataStartValue = _fitterEstimator.binToValue(bin, _inc);
		notifyListeners();
	}

	/**
	 * Gets the start of the data as a bin number.
	 *
	 */
	public int getDataStartBin() {
		// return _dataStartBin;

		if (SLIMProcessor.macroParams.DataStartMacroUsed) {
			return _fitterEstimator.valueToBin(SLIMProcessor.macroParams
				.getDataStartFromMacro(), _inc);
		}
		else return _dataStartBin;

	}

	/**
	 * Sets the start of the data as a time value.
	 *
	 */
	public void setDataStartValue(final double value) {
		_dataStartValue =
			_fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
		_dataStartBin = _fitterEstimator.valueToBin(value, _inc);
		notifyListeners();
	}

	/**
	 * Gets the start of the data as a time value.
	 *
	 */
	public double getDataStartValue() {
		return _dataStartValue;
	}

	/**
	 * Sets the end of the transient based on a string. Handles bins or time
	 * values.
	 *
	 */
	public void setTransientStop(final String transientStop) {
		if (_showBins) {
			final Integer bin = getIntegerValue(transientStop);
			if (null != bin) {
				if (bin >= _dataStartBin && bin < _bins) {
					_transientStopBin = bin;
					_transientStopValue = _fitterEstimator.binToValue(bin, _inc);
				}
			}
		}
		else {
			final Double value = getDoubleValue(transientStop);
			if (null != value) {
				if (value >= _dataStartValue && value <= _inc * _bins) {
					_transientStopValue = value;
					_transientStopBin =
						((FitterEstimator) _fitterEstimator).endValueToBin(value, _inc);
				}
			}
		}

		// either update with new valid values or undo invalid values
		notifyListeners();
	}

	/**
	 * Gets the end of the transient as a string. Handles bins or time values.
	 *
	 */
	public String getTransientStop() {
		final StringBuilder returnValue = new StringBuilder();
		if (_showBins) {
			returnValue.append(getTransientStopBin());
		}
		else {
			returnValue.append(getTransientStopValue());
		}
		return returnValue.toString();
	}

	/**
	 * Sets the end of the transient as a bin number.
	 *
	 */
	public void setTransientStopBin(final int bin) {
		_transientStopBin = bin;
		_transientStopValue = _fitterEstimator.binToValue(bin, _inc);
		notifyListeners();
	}

	/**
	 * Gets the end of the transient as a bin number.
	 *
	 */
	public int getTransientStopBin() {

		if (SLIMProcessor.macroParams.transientStopMacroUsed) {
			return _fitterEstimator.valueToBin(SLIMProcessor.macroParams
				.getTransientStopFromMacro(), _inc);
		}
		else return _transientStopBin;

	}

	/**
	 * Sets the end of the transient as a time value.
	 *
	 */
	public void setTransientStopValue(final double value) {
		_transientStopValue =
			_fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
		_transientStopBin =
			((FitterEstimator) _fitterEstimator).endValueToBin(value, _inc);
		notifyListeners();
	}

	/**
	 * Gets the end of the transient as a time value.
	 *
	 */
	public double getTransientStopValue() {
		return _transientStopValue;
	}

	/**
	 * Helper function to extract integers from strings.
	 *
	 * @return integer or null
	 */
	private Integer getIntegerValue(final String string) {
		Integer value = null;
		try {
			value = Integer.parseInt(string);
		}
		catch (final NumberFormatException e) {}
		return value;
	}

	/**
	 * Helper function to extract doubles from strings.
	 *
	 * @return integer or null
	 */
	private Double getDoubleValue(final String string) {
		Double value = null;
		try {
			value = Double.parseDouble(string);
		}
		catch (final NumberFormatException e) {}
		return value;
	}

	/**
	 * Notifies all listeners of a change.
	 */
	private void notifyListeners() {
		boolean success = false;
		if (!_suspend) {
			while (!success) {
				try {
					synchronized (_listeners) {
						for (final IFittingCursorListener listener : _listeners) {
							listener.cursorChanged(this);
						}
					}
					success = true;
				}
				catch (final ConcurrentModificationException e) {
					// avoid timing issues
				}
			}
		}
	}
}
