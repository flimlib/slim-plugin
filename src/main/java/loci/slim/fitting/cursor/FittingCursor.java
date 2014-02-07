/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim.fitting.cursor;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import loci.curvefitter.IFitterEstimator;
import loci.slim.heuristics.FitterEstimator;

/**
 * Keeps cursor information.  Cursors mark the sections of the excitation
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
	//TODO ARG
	private boolean _kludge = true;

	/**
	 * Constructor
	 * 
	 * @param inc time increment per bin
	 * @param bins total number of bins
	 * @params fitterEstimator
	 */
	public FittingCursor(double inc, int bins, IFitterEstimator fitterEstimator) {
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
	 * @param listener 
	 */
	public void addListener(IFittingCursorListener listener) {
		synchronized (_listeners) {
			if (!_listeners.contains(listener)) {
				_listeners.add(listener);
			}
		}
	}

	/**
	 * Removes a listener for cursor changes.
	 * 
	 * @param listener 
	 */
	public void removeListener(IFittingCursorListener listener) {
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	/**
	 * Sets whether the UI will display bins or time values.
	 * 
	 * @param showBins 
	 */
	public void setShowBins(boolean showBins) {
		_showBins = showBins;
	}

	/**
	 * Gets whether the UI will display bins or time values.
	 * 
	 * @return 
	 */
	public boolean getShowBins() {
		return _showBins;
	}

	/**
	 * Temporarily suspends listener notifications.  Used when several cursors
	 * change at the same time.  Call suspendNotifications followed by cursor
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
	 * @param hasPrompt 
	 */
	public void setHasPrompt(boolean hasPrompt) {
		_hasPrompt = hasPrompt;
	}

	/**
	 * Sets whether or not a prompt has been loaded.
	 * 
	 * @return 
	 */
	public boolean getHasPrompt() {
		return _hasPrompt;
	}

	/**
	 * Sets the start of the prompt based on a prompt delay string.  Handles
	 * bins or time values.
	 * 
	 * @param promptDelay 
	 */
	public void setPromptDelay(String promptDelay) {
		Double promptDelayValue = null;
		if (_showBins) {
			Integer parsedInteger = getIntegerValue(promptDelay);
			if (null != parsedInteger) {
				promptDelayValue = _fitterEstimator.binToValue(parsedInteger, _inc);
			}
		}
		else {
			promptDelayValue = getDoubleValue(promptDelay);
		}
		if (null != promptDelayValue) {
			// convert delay to start
			double promptStartValue = promptDelayValue + _transientStartValue;

			// some very rudimentary error-checking
			if (0.0 < promptStartValue && promptStartValue < _transientStopValue) {
				double diff = promptStartValue - _promptStartValue;
				_promptStartValue += diff;
				_promptStopValue  += diff;
			}
		}
		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the start of the prompt as a string showing prompt delay.  Handles
	 * bins or time values.
	 * 
	 * @return 
	 */
	public String getPromptDelay() {
		StringBuilder returnValue = new StringBuilder();
		if (_showBins) {
			if (_hasPrompt) {
				int delay = getPromptStartBin() - getTransientStartBin();
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
	 * @param bin 
	 */
	public void setPromptStartBin(int bin) {
		_promptStartValue = _fitterEstimator.binToValue(bin, _inc);
		notifyListeners();
	}

	/**
	 * Gets the start of the prompt as a bin number.
	 * 
	 * @return 
	 */
	public int getPromptStartBin() {
		int returnValue  = 0;
		if (_hasPrompt) {
			returnValue = _fitterEstimator.valueToBin(_promptStartValue, _inc);

			/*int tmp = (int) Math.ceil(_promptStartValue / _inc);

			if (returnValue != tmp) {
				System.out.println("*******getPromptStartBin********");
				System.out.println("SP " + returnValue + " TRI2 " + tmp);
				if (_kludge) {
					System.out.println("go with " + tmp);
					returnValue = tmp;
				}
			}*/
		}
		return returnValue;
	}

	/**
	 * Sets the start of the prompt as a time value.
	 * 
	 * @param value 
	 */
	public void setPromptStartValue(double value) {
		_promptStartValue = value;
		notifyListeners();
	}

	/**
	 * Gets the start of the prompt as a time value.
	 * 
	 * @return 
	 */
	public double getPromptStartValue() {
		double returnValue = 0.0;
		if (_hasPrompt) {
			returnValue = _promptStartValue;
		}
		return returnValue;
	}

	/**
	 * Sets the end of the prompt based on a prompt width string.  Handles bins
	 * or time values (that's why we are parsing a string vs taking an int or
	 * float parameter).
	 * 
	 * @param promptWidth 
	 */
	public void setPromptWidth(String promptWidth) {
		Double promptWidthValue = null;
		if (_showBins) {
			Integer parsedInteger = getIntegerValue(promptWidth);
			if (null != parsedInteger) {
				promptWidthValue = _inc * parsedInteger;
			}
		}
		else {
			promptWidthValue = getDoubleValue(promptWidth);
		}
		if (null != promptWidthValue) {
			double promptStopValue = getPromptStartValue() + promptWidthValue;
			if (promptStopValue >= _promptStartValue &&
					promptStopValue <= _bins * _inc) {
				_promptStopValue = promptStopValue;
			}
		}
		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the end of the prompt as a string showing prompt width.  Handles
	 * bins or time values.
	 * 
	 * @return 
	 */
	public String getPromptWidth() {
		StringBuilder returnValue = new StringBuilder();
		if (_showBins) {
			if (_hasPrompt) {
				int width = getPromptStopBin() - getPromptStartBin();
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
	 * @param bin 
	 */
	public void setPromptStopBin(int bin) {
		_promptStopValue = _fitterEstimator.binToValue(bin, _inc);
		notifyListeners();
	}

	/**
	 * Gets the end of the prompt as a bin number.
	 * 
	 * @return 
	 */
	public int getPromptStopBin() {
		int returnValue = 0;
		if (_hasPrompt) {
			returnValue = ((FitterEstimator) _fitterEstimator).valueToBin(_promptStopValue, _inc);
		}
		return returnValue;
	}

	/**
	 * Sets the end of the prompt as a time value.
	 * 
	 * @param value 
	 */
	public void setPromptStopValue(double value) {
		_promptStopValue = value;
		notifyListeners();
	}

	/**
	 * Gets the end of the prompt as a time value.
	 * 
	 * @return 
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
	 * @param promptBaseline 
	 */
	public void setPromptBaseline(String promptBaseline) {
		Double promptBaselineValue = getDoubleValue(promptBaseline);
		if (null != promptBaselineValue) {
			_promptBaselineValue = promptBaselineValue;
		}
		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the baseline of the prompt based on a string.
	 * 
	 * @return 
	 */
	public String getPromptBaseline() {
		String returnValue = DOUBLE_ZERO_STRING;
		if (_hasPrompt) {
			returnValue = "" + getPromptBaselineValue();
		}
		return returnValue;
	}

	/**
	 * Sets the baseline of the prompt as a value.
	 * 
	 * Note that this value is actually a photon count and not based on bins or
	 * time values.
	 * 
	 * @param value 
	 */
	public void setPromptBaselineValue(double value) {
		_promptBaselineValue = value;
		notifyListeners();
	}

	/**
	 * Gets the baseline of the prompt as a value.
	 * 
	 * @return
	 */
	public double getPromptBaselineValue() {
		double returnValue = 0.0;
		if (_hasPrompt) {
			returnValue = _fitterEstimator.roundToDecimalPlaces(_promptBaselineValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the start of the transient based on a string.  Handles bins or time
	 * values.
	 * 
	 * @param transientStart 
	 */
	public void setTransientStart(String transientStart) {
		if (_showBins) {
			Integer bin = getIntegerValue(transientStart);
			if (null != bin) {
				if (bin <= _dataStartBin && bin >= 0) {
					_transientStartBin = bin;
					_transientStartValue = _fitterEstimator.binToValue(bin, _inc);
				}
			}
		}
		else {
			Double value = getDoubleValue(transientStart);
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
	 * Gets the start of the transient as a string.  Handles bins or time values.
	 * 
	 * @return 
	 */
	public String getTransientStart() {
		StringBuilder returnValue = new StringBuilder();
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
	 * @param bin 
	 */
	public void setTransientStartBin(int bin) {
		_transientStartBin = bin;
		_transientStartValue = _fitterEstimator.binToValue(bin, _inc);

		notifyListeners();
	}

	/**
	 * Gets the start of the transient as a bin number.
	 * 
	 * @return 
	 */
	public int getTransientStartBin() {
		return _transientStartBin;
	}

	/**
	 * Sets the start of the transient as a time value.
	 * 
	 * @param value 
	 */
	public void setTransientStartValue(double value) {
		_transientStartValue = _fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
		_transientStartBin = _fitterEstimator.valueToBin(value, _inc);

		notifyListeners();
	}

	/**
	 * Gets the start of the transient as a time value.
	 * 
	 * @return 
	 */
	public double getTransientStartValue() {
		return _transientStartValue;
	}

	/**
	 * Sets the start of the data based on a string.  Handles bins or time
	 * values.
	 * 
	 * @param dataStart 
	 */
	public void setDataStart(String dataStart) {
		if (_showBins) {
			Integer bin = getIntegerValue(dataStart);
			if (null != bin) {
				if (bin >= _transientStartBin && bin <= _transientStopBin) {
					_dataStartBin = bin;
					_dataStartValue = _fitterEstimator.binToValue(bin, _inc);
				}
			}
		}
		else {
			Double value = getDoubleValue(dataStart);
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
	 * Gets the start of the data as a string.  Handles bins or time values.
	 * 
	 * @return 
	 */
	public String getDataStart() {
		StringBuilder returnValue = new StringBuilder();
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
	 * @param bin 
	 */
	public void setDataStartBin(int bin) {
		_dataStartBin = bin;
		_dataStartValue = _fitterEstimator.binToValue(bin, _inc);
		notifyListeners();
	}

	/**
	 * Gets the start of the data as a bin number.
	 * 
	 * @return 
	 */
	public int getDataStartBin() {
		return _dataStartBin;
	}

	/**
	 * Sets the start of the data as a time value.
	 * 
	 * @param value 
	 */
	public void setDataStartValue(double value) {
		_dataStartValue = _fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
		_dataStartBin = _fitterEstimator.valueToBin(value, _inc);
		notifyListeners();
	}

	/**
	 * Gets the start of the data as a time value.
	 * 
	 * @return 
	 */
	public double getDataStartValue() {
		return _dataStartValue;
	}

	/**
	 * Sets the end of the transient based on a string.  Handles bins or time
	 * values.
	 * 
	 * @param transientStop 
	 */
	public void setTransientStop(String transientStop) {
		if (_showBins) {
			Integer bin = getIntegerValue(transientStop);
			if (null != bin) {
				if (bin >= _dataStartBin && bin < _bins) {
					_transientStopBin = bin;
					_transientStopValue = _fitterEstimator.binToValue(bin, _inc);
				}
			}
		}
		else {
			Double value = getDoubleValue(transientStop);
			if (null != value) {
				if (value >= _dataStartValue && value <= _inc * _bins) {
					_transientStopValue = value;
					_transientStopBin = ((FitterEstimator) _fitterEstimator).endValueToBin(value, _inc);
				}
			}
		}

		// either update with new valid values or undo invalid values
		notifyListeners();
	}

	/**
	 * Gets the end of the transient as a string.  Handles bins or time values.
	 * 
	 * @return 
	 */
	public String getTransientStop() {
		StringBuilder returnValue = new StringBuilder();
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
	 * @param bin 
	 */
	public void setTransientStopBin(int bin) {
		_transientStopBin = bin;
		_transientStopValue = _fitterEstimator.binToValue(bin, _inc);
		notifyListeners();
	}

	/**
	 * Gets the end of the transient as a bin number.
	 * 
	 * @return 
	 */
	public int getTransientStopBin() {
		return _transientStopBin;
	}

	/**
	 * Sets the end of the transient as a time value.
	 * 
	 * @param value 
	 */
	public void setTransientStopValue(double value) {
		_transientStopValue = _fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
		_transientStopBin = ((FitterEstimator) _fitterEstimator).endValueToBin(value, _inc);
		notifyListeners();
	}

	/**
	 * Gets the end of the transient as a time value.
	 * 
	 * @return 
	 */
	public double getTransientStopValue() {
		return _transientStopValue;
	}

	/**
	 * Helper function to extract integers from strings.
	 * 
	 * @param string
	 * @return integer or null
	 */
	private Integer getIntegerValue(String string) {
		Integer value = null;
		try {
			value = Integer.parseInt(string);
		}
		catch (NumberFormatException e) {
		}
		return value;
	}

	/**
	 * Helper function to extract doubles from strings.
	 * 
	 * @param string
	 * @return integer or null
	 */
	private Double getDoubleValue(String string) {
		Double value = null;
		try {
			value = Double.parseDouble(string);
		}
		catch (NumberFormatException e) {
		}
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
						for (IFittingCursorListener listener : _listeners) {
							listener.cursorChanged(this);
						}
					}
					success = true;
				}
				catch (ConcurrentModificationException e) {
					// avoid timing issues
				}
			}
		}
	}
}
