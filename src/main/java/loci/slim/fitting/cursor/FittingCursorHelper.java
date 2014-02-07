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

/**
 * This is a helper class for the user interface for setting and displaying
 * the fitting cursor start and stop values.  This listens for changes and
 * keeps the user interface display up to date.
 * 
 * @author Aivar Grislis
 */
public class FittingCursorHelper implements IFittingCursorUI {
	private FittingCursor _fittingCursor;
	private IFittingCursorListener _fittingCursorListener;
	private IFittingCursorUI _fittingCursorUI;

	/**
	 * Sets the UI source and destination of fitting cursor strings, e.g. a
	 * wrapper around some JTextField.
	 * 
	 * @param promptCursorUI 
	 */
	public void setFittingCursorUI(IFittingCursorUI fittingCursorUI) {
		_fittingCursorUI = fittingCursorUI;
		showFittingCursor();
	}

	/**
	 * Sets the fitting cursor that is keeping track of cursor settings.
	 * 
	 * @param fittingCursor 
	 */
	public void setFittingCursor(FittingCursor fittingCursor) {
		if (null == _fittingCursor) {
			_fittingCursorListener = new FittingCursorListener();
		}
		else if (_fittingCursor != fittingCursor) {
			_fittingCursor.removeListener(_fittingCursorListener);
		}
		_fittingCursor = fittingCursor;
		_fittingCursor.addListener(_fittingCursorListener);
	}

	/**
	 * Gets whether to show bins or time values for cursors.
	 * 
	 * @return 
	 */
	public boolean getShowBins() {
		return _fittingCursor.getShowBins();
	}

	/**
	 * Sets whether to show bins or time values for cursors.
	 * 
	 * @param showBins 
	 */
	public void setShowBins(boolean showBins) {
		_fittingCursor.setShowBins(showBins);
		showFittingCursor();
	}

	/**
	 * Turns on/off prompt cursors.
	 * 
	 * @param enable 
	 */
	public void enablePrompt(boolean enable) {
		_fittingCursor.setHasPrompt(enable);
	}

	/**
	 * Gets whether there is a prompt.
	 * 
	 * @return 
	 */
	public boolean getPrompt() {
		return _fittingCursor.getHasPrompt();
	}

	/**
	 * Gets the transient start cursor.
	 * 
	 * @return 
	 */
	@Override
	public String getTransientStart() {
		return _fittingCursorUI.getTransientStart();
	}

	/**
	 * Sets the transient start cursor.
	 * 
	 * @param transientStart 
	 */
	@Override
	public void setTransientStart(String transientStart) {
		_fittingCursor.setTransientStart(transientStart);
	}

	/**
	 * Gets the data start cursor.
	 * 
	 * @return 
	 */
	@Override
	public String getDataStart() {
		return _fittingCursorUI.getDataStart();
	}

	/**
	 * Sets the data start cursor.
	 * 
	 * @param transientBaseline 
	 */
	@Override
	public void setDataStart(String dataStart) {
		_fittingCursor.setDataStart(dataStart);
	}

	/**
	 * Gets the transient end cursor.
	 * 
	 * @return 
	 */
	@Override
	public String getTransientStop() {
		return _fittingCursorUI.getTransientStop();
	}

	/**
	 * Sets the transient end cursor.
	 * 
	 * @param transientStop 
	 */
	@Override
	public void setTransientStop(String transientStop) {
		_fittingCursor.setTransientStop(transientStop);
	}

	/**
	 * Gets the prompt delay cursor.
	 * 
	 * @return 
	 */
	@Override
	public String getPromptDelay() {
		return _fittingCursorUI.getPromptDelay();
	}

	/**
	 * Sets the prompt delay cursor.
	 * 
	 * @param promptStart 
	 */
	@Override
	public void setPromptDelay(String promptDelay) {
		_fittingCursor.setPromptDelay(promptDelay);
	}

	/**
	 * Gets the prompt width cursor.
	 * 
	 * @return 
	 */
	@Override
	public String getPromptWidth() {
		return _fittingCursorUI.getPromptWidth();
	}

	/**
	 * Sets the prompt width cursor.
	 * 
	 * @param promptWidth
	 */
	@Override
	public void setPromptWidth(String promptWidth) {
		_fittingCursor.setPromptWidth(promptWidth);
	}

	/**
	 * Gets the prompt baseline cursor as a string.
	 * @return 
	 */
	@Override
	public String getPromptBaseline() {
		return _fittingCursorUI.getPromptBaseline();
	}

	/**
	 * Sets the prompt baseline cursor as a string.
	 * 
	 * @param promptBaseline 
	 */
	@Override
	public void setPromptBaseline(String promptBaseline) {
		_fittingCursor.setPromptBaseline(promptBaseline);
	}

	/**
	 * Shows current fitting cursor settings in UI.
	 */
	private void showFittingCursor() {
		if (null != _fittingCursorUI) {
			_fittingCursorUI.setTransientStart(_fittingCursor.getTransientStart());
			_fittingCursorUI.setDataStart(_fittingCursor.getDataStart());
			_fittingCursorUI.setTransientStop(_fittingCursor.getTransientStop());
			_fittingCursorUI.setPromptDelay(_fittingCursor.getPromptDelay());
			_fittingCursorUI.setPromptWidth(_fittingCursor.getPromptWidth());
			_fittingCursorUI.setPromptBaseline(_fittingCursor.getPromptBaseline());
		}
	}

	/**
	 * Inner listener for cursor changes.
	 */
	private class FittingCursorListener implements IFittingCursorListener {
		@Override
		public void cursorChanged(FittingCursor cursor) {
			showFittingCursor();
		}
	}
}
