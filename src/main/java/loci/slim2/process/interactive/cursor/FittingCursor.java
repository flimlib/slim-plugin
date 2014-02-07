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

package loci.slim2.process.interactive.cursor;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import loci.curvefitter.IFitterEstimator;
import loci.slim2.heuristics.DefaultFitterEstimator;

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
	private final double inc;
	private final int bins;
	private final Set<FittingCursorListener> listeners;
	private final IFitterEstimator fitterEstimator;
	private boolean showBins;
	private volatile boolean suspend;
	private boolean hasPrompt;
	private int promptStartIndex;
	private int promptStopIndex;
	private int transientStartIndex;
	private int dataStartIndex;
	private int transientStopIndex;
	private double promptStartTime;
	private double promptStopTime;
	private double promptBaselineValue;
	private double transientStartTime;
	private double dataStartTime;
	private double transientStopTime;

	/**
	 * Constructor
	 * 
	 * @param inc time increment per bin
	 * @param bins total number of bins
	 * @params fitterEstimator
	 */
	public FittingCursor(double inc, int bins, IFitterEstimator fitterEstimator) {
		this.inc = inc;
		this.bins = bins;
		this.fitterEstimator = fitterEstimator;
		listeners = new HashSet<FittingCursorListener>();
		showBins = false;
		suspend = false;
		hasPrompt = false;
	}

	/**
	 * Adds a listener for cursor changes.
	 * 
	 * @param listener 
	 */
	public void addListener(FittingCursorListener listener) {
		if (null == listener) { System.out.println("FittingCursor.addListener is null"); //TODO ARG
		 throw new RuntimeException("blah blah"); //TODO ARG just to show up bad listeners during development
		}
		synchronized (listeners) {
			// avoid duplicates
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	/**
	 * Removes a listener for cursor changes.
	 * 
	 * @param listener 
	 */
	public void removeListener(FittingCursorListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Temporarily suspends listener notifications.  Used when several cursors
	 * change at the same time.  Call suspendNotifications followed by cursor
	 * changes followed by sendNotifications.
	 * <p>
	 * Also suspends checking for cursor consistency.
	 */
	public void suspendNotifications() {
		suspend = true;
	}

	/**
	 * Used to send listener notifications after a batched change.
	 */
	public void sendNotifications() {
		suspend = false;
		notifyListeners();
	}

	/**
	 * Sets whether the UI will display bins or time values.
	 * 
	 * @param showBins 
	 */
	public void setShowBins(boolean showBins) {
		this.showBins = showBins;
	}

	/**
	 * Gets whether the UI will display bins or time values.
	 * 
	 * @return 
	 */
	public boolean getShowBins() {
		return showBins;
	}

	/**
	 * Returns whether or not a prompt has been loaded.
	 * 
	 * @param hasPrompt 
	 */
	public void setHasPrompt(boolean hasPrompt) {
		this.hasPrompt = hasPrompt;
	}

	/**
	 * Sets whether or not a prompt has been loaded.
	 * 
	 * @return 
	 */
	public boolean hasPrompt() {
		return hasPrompt;
	}

	/**
	 * Sets the prompt delay as a bin index.
	 * 
	 * @param promptDelay 
	 */
	public void setPromptDelayIndex(int promptDelayIndex) {
		double promptDelayTime = fitterEstimator.binToValue(promptDelayIndex, inc);

		// validate & notify
		checkPromptDelay(promptDelayIndex, promptDelayTime);
	}

	/**
	 * Sets the prompt delay as a time value.
	 * 
	 * @param promptDelayTime 
	 */
	public void setPromptDelayTime(double promptDelayTime) {
		int promptDelayIndex = fitterEstimator.valueToBin(promptDelayTime, inc);

		// validate & notify
		checkPromptDelay(promptDelayIndex, promptDelayTime);
	}

	/**
	 * Validate & notify prompt delay changes.
	 * 
	 * @param promptDelayIndex
	 * @param promptDelayTime 
	 */
	private void checkPromptDelay(int promptDelayIndex, double promptDelayTime) {
		// convert delay to start
		double promptStartTime = promptDelayTime + transientStartTime;
		int promptStartIndex = promptDelayIndex + transientStartIndex;

		// validate & notify
		checkPromptStart(promptStartIndex, promptStartTime);
	}

	/**
	 * Gets the start of the prompt as a bin index.
	 * 
	 * @return 
	 */
	public int getPromptDelayIndex() {
		int returnValue = 0;
		if (hasPrompt) {
			returnValue = getPromptStartIndex() - getTransientStartIndex();
		}
		return returnValue;
	}
	/**
	 * Gets the start of the prompt as a time value.
	 * 
	 * @return 
	 */
	public double getPromptDelayTime() {
		double returnValue = 0.0;
		if (hasPrompt) {
			returnValue = getPromptStartTime() - getTransientStartTime();
			returnValue = fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the start of the prompt as a bin index.
	 * 
	 * @param index
	 */
	public void setPromptStartIndex(int promptStartIndex) {
		double promptStartTime = fitterEstimator.binToValue(promptStartIndex, inc);
		checkPromptStart(promptStartIndex, promptStartTime);
	}

	/**
	 * Sets the start of the prompt as a time value.
	 * 
	 * @param value 
	 */
	public void setPromptStartTime(double promptStartTime) {
		this.promptStartTime = promptStartTime;
		notifyListeners();
	}

	/**
	 * Validate & notify start of prompt changes.
	 * 
	 * @param promptStartIndex
	 * @param promptStartTime 
	 */
	private void checkPromptStart(int promptStartIndex, double promptStartTime) {
		// some very rudimentary error-checking
		if (suspend || (0.0 < promptStartTime && promptStartTime < transientStopTime)) {
			double diffTime = promptStartTime - this.promptStartTime;
			this.promptStartTime += diffTime;
			promptStopTime       += diffTime;
			int diffIndex = promptStartIndex - this.promptStartIndex;
			this.promptStartIndex += diffIndex;
			promptStopIndex       += diffIndex;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the start of the prompt as a bin index.
	 * 
	 * @return 
	 */
	public int getPromptStartIndex() {
		int returnValue  = 0;
		if (hasPrompt) {
			returnValue = promptStartIndex;
		}
		return returnValue;
	}

	/**
	 * Gets the start of the prompt as a time value.
	 * 
	 * @return 
	 */
	public double getPromptStartTime() {
		double returnValue = 0.0;
		if (hasPrompt) {
			returnValue = promptStartTime;
			returnValue = fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the end of the prompt based on a prompt width bin index.
	 * 
	 * @param promptWidth 
	 */
	public void setPromptWidthIndex(int promptWidthIndex) {
		double promptWidthTime = fitterEstimator.binToValue(promptWidthIndex, inc);

		// validate & notify
		checkPromptWidth(promptWidthIndex, promptWidthTime);
	}

	/**
	 * Sets the end of the prompt based on a prompt width time.
	 * 
	 * @param promptWidthTime 
	 */
	public void setPromptWidthTime(double promptWidthTime) {
		int promptWidthIndex = fitterEstimator.valueToBin(promptWidthTime, inc);

		// validate & notify
		checkPromptWidth(promptWidthIndex, promptWidthTime);
	}

	/**
	 * Validate & notify prompt width changes.
	 * 
	 * @param promptWidthIndex
	 * @param promptWidthTime 
	 */
	private void checkPromptWidth(int promptWidthIndex, double promptWidthTime) {
		int promptStopIndex = getPromptStartIndex() + promptWidthIndex;
		double promptStopTime = getPromptStartTime() + promptWidthTime;

		if (suspend || (promptStopTime > 0.0 && promptStopTime < bins * inc)) {
			this.promptStopIndex = promptStopIndex;
			this.promptStopTime  = promptStopTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the width of the prompt as an index.
	 * 
	 * @return 
	 */
	public int getPromptWidthIndex() {
		int returnValue = 0;
		if (hasPrompt) {
			returnValue = getPromptStopIndex() - getPromptStartIndex();
		}
		return returnValue;
	}

	/**
	 * Gets the width of the prompt as a time.
	 * 
	 * @return 
	 */
	public double getPromptWidthTime() {
		double returnValue = 0;
		if (hasPrompt) {
			returnValue = getPromptStopTime() - getPromptStartTime();
			returnValue = fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the end of the prompt as an index.
	 * 
	 * @param promptStopIndex 
	 */
	public void setPromptStopIndex(int promptStopIndex) {
		double promptStopTime = fitterEstimator.binToValue(promptStopIndex, inc);

		// validate & notify
		checkPromptStop(promptStopIndex, promptStopTime);
	}

	/**
	 * Sets the end of the prompt as a time value.
	 * 
	 * @param promptStopTime 
	 */
	public void setPromptStopTime(double promptStopTime) {
		int promptStopIndex = fitterEstimator.valueToBin(promptStopTime, inc);

		// validate & notify
		checkPromptStop(promptStopIndex, promptStopTime);
	}

	/**
	 * Validate & notify end of prompt changes.
	 * 
	 * @param promptStopIndex
	 * @param promptStopTime 
	 */
	private void checkPromptStop(int promptStopIndex, double promptStopTime) {
		if (suspend || (promptStopTime > getPromptStartTime() && promptStopTime < bins * inc)) {
			this.promptStopIndex = promptStopIndex;
			this.promptStopTime  = promptStopTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the end of the prompt as an index.
	 * 
	 * @return 
	 */
	public int getPromptStopIndex() {
		int returnValue = 0;
		if (hasPrompt) {
			returnValue = ((DefaultFitterEstimator) fitterEstimator).valueToBin(promptStopTime, inc);
		}
		return returnValue;
	}

	/**
	 * Gets the end of the prompt as a time value.
	 * 
	 * @return 
	 */
	public double getPromptStopTime() {
		double returnValue = 0.0;
		if (hasPrompt) {
			returnValue = promptStopTime;
			returnValue = fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the baseline of the prompt.
	 * 
	 * Note that this value is actually a photon count and not based on bins or
	 * time values.
	 * 
	 * @param promptBaselineValue 
	 */
	public void setPromptBaselineValue(double promptBaselineValue) {
		this.promptBaselineValue = promptBaselineValue;
		notifyListeners();
	}

	/**
	 * Gets the baseline of the prompt.
	 * 
	 * @return
	 */
	public double getPromptBaselineValue() {
		double returnValue = 0.0;
		if (hasPrompt) {
			returnValue = promptBaselineValue;
			returnValue = fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the start of the transient as an index.
	 * 
	 * @param transientStartIndex
	 */
	public void setTransientStartIndex(int transientStartIndex) {
		double transientStartTime = fitterEstimator.binToValue(transientStartIndex, inc);

		checkTransientStart(transientStartIndex, transientStartTime);
	}

	public void setTransientStartTime(double transientStartTime) {
		int transientStartIndex = fitterEstimator.valueToBin(transientStartTime, inc);

		checkTransientStart(transientStartIndex, transientStartTime);
	}

	/**
	 * Validate & notify start of transient changes.
	 * 
	 * @param transientStartIndex
	 * @param transientStartTime 
	 */
	private void checkTransientStart(int transientStartIndex, double transientStartTime) {
		if (suspend || (0 <= transientStartTime && transientStartTime <= getDataStartTime())) {
			this.transientStartIndex = transientStartIndex;
			this.transientStartTime = transientStartTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}


	/**
	 * Gets the start of the transient as an index.
	 * 
	 * @return 
	 */
	public int getTransientStartIndex() {
		return transientStartIndex;
	}

	/**
	 * Gets the start of the transient as a time.
	 * 
	 * @return 
	 */
	public double getTransientStartTime() {
		return transientStartTime;
	}

	/**
	 * Set start of data as an index.
	 * 
	 * @param dataStartIndex 
	 */
	public void setDataStartIndex(int dataStartIndex) {
		double dataStartTime = fitterEstimator.binToValue(dataStartIndex, inc);

		// validate & notify
		checkDataStart(dataStartIndex, dataStartTime);
	}

	/**
	 * Sets start of data as a time.
	 * 
	 * @param dataStartTime 
	 */
	public void setDataStartTime(double dataStartTime) {
		int dataStartIndex = fitterEstimator.valueToBin(dataStartTime, inc);

		// validate & notify
		checkDataStart(dataStartIndex, dataStartTime);
	}

	/**
	 * Validate & notify start of data changes.
	 * 
	 * @param dataStartIndex
	 * @param dataStartTime 
	 */
	private void checkDataStart(int dataStartIndex, double dataStartTime) {
		if (suspend || (transientStartTime <= dataStartTime && dataStartTime < transientStopTime)) {
			this.dataStartIndex = dataStartIndex;
			this.dataStartTime  = dataStartTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets start of data as an index.
	 * 
	 * @return 
	 */
	public int getDataStartIndex() {
		return dataStartIndex;
	}

	/**
	 * Gets start of data as a time.
	 * 
	 * @return 
	 */
	public double getDataStartTime() {
		return dataStartTime; //TODO ARG round it?
	}

	/**
	 * Sets end of data as an index.
	 * 
	 * @param transientStopIndex 
	 */
	public void setTransientStopIndex(int transientStopIndex) {
		double transientStopTime = fitterEstimator.binToValue(transientStopIndex, inc);

		// validate & notify
		checkTransientStop(transientStopIndex, transientStopTime);
	}

	/**
	 * Sets end of data as a time.
	 * 
	 * @param transientStopTime 
	 */
	public void setTransientStopTime(double transientStopTime) {
		int transientStopIndex = fitterEstimator.valueToBin(transientStopTime, inc);

		// validate & notify
		checkTransientStop(transientStopIndex, transientStopTime);
	}

	private void checkTransientStop(int transientStopIndex, double transientStopTime) {
		if (suspend || (getDataStartTime() <= transientStopTime && transientStopTime < bins * inc)) {
			this.transientStopIndex = transientStopIndex;
			this.transientStopTime  = transientStopTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	public int getTransientStopIndex() {
		return transientStopIndex;
	}

	public double getTransientStopTime() {
		return transientStopTime;
	}

	/**
	 * Notifies all listeners of a change.
	 */
	private void notifyListeners() {
		boolean success = false;
		if (!suspend) {
			while (!success) {
				try {
					synchronized (listeners) {
						for (FittingCursorListener listener : listeners) {
							if (null == listener) {
								System.out.println("null listener for FC"); //TODO ARG error checking during development
							}
							else {
								listener.cursorChanged(this);
							}
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

