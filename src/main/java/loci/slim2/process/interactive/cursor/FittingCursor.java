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

package loci.slim2.process.interactive.cursor;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import loci.curvefitter.IFitterEstimator;
import loci.slim2.heuristics.DefaultFitterEstimator;

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
	public FittingCursor(final double inc, final int bins,
		final IFitterEstimator fitterEstimator)
	{
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
	 */
	public void addListener(final FittingCursorListener listener) {
		// TODO ARG
		if (null == listener) {
			System.out.println("FittingCursor.addListener is null");
			// TODO ARG just to show up bad listeners during development
			throw new RuntimeException("blah blah");
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
	 */
	public void removeListener(final FittingCursorListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Temporarily suspends listener notifications. Used when several cursors
	 * change at the same time. Call suspendNotifications followed by cursor
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
	 */
	public void setShowBins(final boolean showBins) {
		this.showBins = showBins;
	}

	/**
	 * Gets whether the UI will display bins or time values.
	 *
	 */
	public boolean getShowBins() {
		return showBins;
	}

	/**
	 * Returns whether or not a prompt has been loaded.
	 *
	 */
	public void setHasPrompt(final boolean hasPrompt) {
		this.hasPrompt = hasPrompt;
	}

	/**
	 * Sets whether or not a prompt has been loaded.
	 *
	 */
	public boolean hasPrompt() {
		return hasPrompt;
	}

	/**
	 * Sets the prompt delay as a bin index.
	 *
	 */
	public void setPromptDelayIndex(final int promptDelayIndex) {
		final double promptDelayTime =
			fitterEstimator.binToValue(promptDelayIndex, inc);

		// validate & notify
		checkPromptDelay(promptDelayIndex, promptDelayTime);
	}

	/**
	 * Sets the prompt delay as a time value.
	 *
	 */
	public void setPromptDelayTime(final double promptDelayTime) {
		final int promptDelayIndex =
			fitterEstimator.valueToBin(promptDelayTime, inc);

		// validate & notify
		checkPromptDelay(promptDelayIndex, promptDelayTime);
	}

	/**
	 * Validate & notify prompt delay changes.
	 *
	 */
	private void checkPromptDelay(final int promptDelayIndex,
		final double promptDelayTime)
	{
		// convert delay to start
		final double promptStartTime = promptDelayTime + transientStartTime;
		final int promptStartIndex = promptDelayIndex + transientStartIndex;

		// validate & notify
		checkPromptStart(promptStartIndex, promptStartTime);
	}

	/**
	 * Gets the start of the prompt as a bin index.
	 *
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
	 */
	public double getPromptDelayTime() {
		double returnValue = 0.0;
		if (hasPrompt) {
			returnValue = getPromptStartTime() - getTransientStartTime();
			returnValue =
				fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the start of the prompt as a bin index.
	 *
	 */
	public void setPromptStartIndex(final int promptStartIndex) {
		final double promptStartTime =
			fitterEstimator.binToValue(promptStartIndex, inc);
		checkPromptStart(promptStartIndex, promptStartTime);
	}

	/**
	 * Sets the start of the prompt as a time value.
	 *
	 */
	public void setPromptStartTime(final double promptStartTime) {
		this.promptStartTime = promptStartTime;
		notifyListeners();
	}

	/**
	 * Validate & notify start of prompt changes.
	 *
	 */
	private void checkPromptStart(final int promptStartIndex,
		final double promptStartTime)
	{
		// some very rudimentary error-checking
		if (suspend ||
			(0.0 < promptStartTime && promptStartTime < transientStopTime))
		{
			final double diffTime = promptStartTime - this.promptStartTime;
			this.promptStartTime += diffTime;
			promptStopTime += diffTime;
			final int diffIndex = promptStartIndex - this.promptStartIndex;
			this.promptStartIndex += diffIndex;
			promptStopIndex += diffIndex;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the start of the prompt as a bin index.
	 *
	 */
	public int getPromptStartIndex() {
		int returnValue = 0;
		if (hasPrompt) {
			returnValue = promptStartIndex;
		}
		return returnValue;
	}

	/**
	 * Gets the start of the prompt as a time value.
	 *
	 */
	public double getPromptStartTime() {
		double returnValue = 0.0;
		if (hasPrompt) {
			returnValue = promptStartTime;
			returnValue =
				fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the end of the prompt based on a prompt width bin index.
	 *
	 */
	public void setPromptWidthIndex(final int promptWidthIndex) {
		final double promptWidthTime =
			fitterEstimator.binToValue(promptWidthIndex, inc);

		// validate & notify
		checkPromptWidth(promptWidthIndex, promptWidthTime);
	}

	/**
	 * Sets the end of the prompt based on a prompt width time.
	 *
	 */
	public void setPromptWidthTime(final double promptWidthTime) {
		final int promptWidthIndex =
			fitterEstimator.valueToBin(promptWidthTime, inc);

		// validate & notify
		checkPromptWidth(promptWidthIndex, promptWidthTime);
	}

	/**
	 * Validate & notify prompt width changes.
	 *
	 */
	private void checkPromptWidth(final int promptWidthIndex,
		final double promptWidthTime)
	{
		final int promptStopIndex = getPromptStartIndex() + promptWidthIndex;
		final double promptStopTime = getPromptStartTime() + promptWidthTime;

		if (suspend || (promptStopTime > 0.0 && promptStopTime < bins * inc)) {
			this.promptStopIndex = promptStopIndex;
			this.promptStopTime = promptStopTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the width of the prompt as an index.
	 *
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
	 */
	public double getPromptWidthTime() {
		double returnValue = 0;
		if (hasPrompt) {
			returnValue = getPromptStopTime() - getPromptStartTime();
			returnValue =
				fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the end of the prompt as an index.
	 *
	 */
	public void setPromptStopIndex(final int promptStopIndex) {
		final double promptStopTime =
			fitterEstimator.binToValue(promptStopIndex, inc);

		// validate & notify
		checkPromptStop(promptStopIndex, promptStopTime);
	}

	/**
	 * Sets the end of the prompt as a time value.
	 *
	 */
	public void setPromptStopTime(final double promptStopTime) {
		final int promptStopIndex = fitterEstimator.valueToBin(promptStopTime, inc);

		// validate & notify
		checkPromptStop(promptStopIndex, promptStopTime);
	}

	/**
	 * Validate & notify end of prompt changes.
	 *
	 */
	private void checkPromptStop(final int promptStopIndex,
		final double promptStopTime)
	{
		if (suspend ||
			(promptStopTime > getPromptStartTime() && promptStopTime < bins * inc))
		{
			this.promptStopIndex = promptStopIndex;
			this.promptStopTime = promptStopTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the end of the prompt as an index.
	 *
	 */
	public int getPromptStopIndex() {
		int returnValue = 0;
		if (hasPrompt) {
			returnValue =
				((DefaultFitterEstimator) fitterEstimator).valueToBin(promptStopTime,
					inc);
		}
		return returnValue;
	}

	/**
	 * Gets the end of the prompt as a time value.
	 *
	 */
	public double getPromptStopTime() {
		double returnValue = 0.0;
		if (hasPrompt) {
			returnValue = promptStopTime;
			returnValue =
				fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the baseline of the prompt. Note that this value is actually a photon
	 * count and not based on bins or time values.
	 *
	 */
	public void setPromptBaselineValue(final double promptBaselineValue) {
		this.promptBaselineValue = promptBaselineValue;
		notifyListeners();
	}

	/**
	 * Gets the baseline of the prompt.
	 *
	 */
	public double getPromptBaselineValue() {
		double returnValue = 0.0;
		if (hasPrompt) {
			returnValue = promptBaselineValue;
			returnValue =
				fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

	/**
	 * Sets the start of the transient as an index.
	 *
	 */
	public void setTransientStartIndex(final int transientStartIndex) {
		final double transientStartTime =
			fitterEstimator.binToValue(transientStartIndex, inc);

		checkTransientStart(transientStartIndex, transientStartTime);
	}

	public void setTransientStartTime(final double transientStartTime) {
		final int transientStartIndex =
			fitterEstimator.valueToBin(transientStartTime, inc);

		checkTransientStart(transientStartIndex, transientStartTime);
	}

	/**
	 * Validate & notify start of transient changes.
	 *
	 */
	private void checkTransientStart(final int transientStartIndex,
		final double transientStartTime)
	{
		if (suspend ||
			(0 <= transientStartTime && transientStartTime <= getDataStartTime()))
		{
			this.transientStartIndex = transientStartIndex;
			this.transientStartTime = transientStartTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets the start of the transient as an index.
	 *
	 */
	public int getTransientStartIndex() {
		return transientStartIndex;
	}

	/**
	 * Gets the start of the transient as a time.
	 *
	 */
	public double getTransientStartTime() {
		return transientStartTime;
	}

	/**
	 * Set start of data as an index.
	 *
	 */
	public void setDataStartIndex(final int dataStartIndex) {
		final double dataStartTime =
			fitterEstimator.binToValue(dataStartIndex, inc);

		// validate & notify
		checkDataStart(dataStartIndex, dataStartTime);
	}

	/**
	 * Sets start of data as a time.
	 *
	 */
	public void setDataStartTime(final double dataStartTime) {
		final int dataStartIndex = fitterEstimator.valueToBin(dataStartTime, inc);

		// validate & notify
		checkDataStart(dataStartIndex, dataStartTime);
	}

	/**
	 * Validate & notify start of data changes.
	 *
	 */
	private void checkDataStart(final int dataStartIndex,
		final double dataStartTime)
	{
		if (suspend ||
			(transientStartTime <= dataStartTime && dataStartTime < transientStopTime))
		{
			this.dataStartIndex = dataStartIndex;
			this.dataStartTime = dataStartTime;
		}

		// either update others with new valid value or undo our invalid value
		notifyListeners();
	}

	/**
	 * Gets start of data as an index.
	 *
	 */
	public int getDataStartIndex() {
		return dataStartIndex;
	}

	/**
	 * Gets start of data as a time.
	 *
	 */
	public double getDataStartTime() {
		// TODO ARG round it?
		return dataStartTime;
	}

	/**
	 * Sets end of data as an index.
	 *
	 */
	public void setTransientStopIndex(final int transientStopIndex) {
		final double transientStopTime =
			fitterEstimator.binToValue(transientStopIndex, inc);

		// validate & notify
		checkTransientStop(transientStopIndex, transientStopTime);
	}

	/**
	 * Sets end of data as a time.
	 *
	 */
	public void setTransientStopTime(final double transientStopTime) {
		final int transientStopIndex =
			fitterEstimator.valueToBin(transientStopTime, inc);

		// validate & notify
		checkTransientStop(transientStopIndex, transientStopTime);
	}

	private void checkTransientStop(final int transientStopIndex,
		final double transientStopTime)
	{
		if (suspend ||
			(getDataStartTime() <= transientStopTime && transientStopTime < bins *
				inc))
		{
			this.transientStopIndex = transientStopIndex;
			this.transientStopTime = transientStopTime;
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
						for (final FittingCursorListener listener : listeners) {
							if (null == listener) {
								// TODO ARG error checking during development
								System.out.println("null listener for FC");
							}
							else {
								listener.cursorChanged(this);
							}
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
