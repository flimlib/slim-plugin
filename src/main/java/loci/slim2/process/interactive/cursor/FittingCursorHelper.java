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
    * Neither the names of the ImageJDev.org developers nor the
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

package loci.slim2.process.interactive.cursor;

/**
 * This is a helper class for the user interface for setting and displaying
 * the fitting cursor start and stop values.  This listens for changes and
 * keeps the user interface display up to date.
 * 
 * @author Aivar Grislis
 */
public class FittingCursorHelper implements FittingCursorUI {
    private FittingCursor fittingCursor;
    private FittingCursorListener fittingCursorListener;
    private FittingCursorUI fittingCursorUI;
    
    /**
     * Sets the UI source and destination of fitting cursor values.
     * 
     * @param promptCursorUI 
     */
    public void setFittingCursorUI(FittingCursorUI fittingCursorUI) {
        this.fittingCursorUI = fittingCursorUI;
        showFittingCursor();
    }

    /**
     * Sets the fitting cursor that is keeping track of cursor settings.
     * 
     * @param fittingCursor 
     */
    public void setFittingCursor(FittingCursor fittingCursor) {
        if (null == this.fittingCursor) {
			// first time, create inner class listener
            fittingCursorListener = new CursorListener();
        }
        else if (this.fittingCursor != fittingCursor) {
			// new fitting cursor, remove old listener
            this.fittingCursor.removeListener(fittingCursorListener);
        }
        this.fittingCursor = fittingCursor;
        fittingCursor.addListener(fittingCursorListener);
    }

    /**
     * Gets whether to show bins or time values for cursors.
     * 
     * @return 
     */
    public boolean getShowBins() {
        return fittingCursor.getShowBins();
    }

    /**
     * Sets whether to show bins or time values for cursors.
     * 
     * @param showBins 
     */
    public void setShowBins(boolean showBins) {
        fittingCursor.setShowBins(showBins);
        showFittingCursor();
    }

    /**
     * Turns on/off prompt cursors.
     * 
     * @param enable 
     */
    public void enablePrompt(boolean enable) {
        fittingCursor.setHasPrompt(enable);
    }
    
    /**
     * Gets whether there is a prompt.
     * 
     * @return 
     */
    public boolean hasPrompt() {
        return fittingCursor.hasPrompt();
    }
    
    /**
     * Gets the transient start cursor index.
     * 
     * @return 
     */
    @Override
    public int getTransientStartIndex() {
        return fittingCursorUI.getTransientStartIndex();
    }

	/**
	 * Gets the transient start cursor time.
	 * 
	 * @return 
	 */
	@Override
	public double getTransientStartTime() {
		return fittingCursorUI.getTransientStartTime();
	}
  
    /**
     * Sets the transient start cursor.
     * 
     * @param index
	 * @param time
     */
    @Override
    public void setTransientStart(int index, double time) {
        fittingCursor.setTransientStart(index, time);
    }
    
    /**
     * Gets the data start cursor index.
     * 
     * @return 
     */
    @Override
    public int getDataStartIndex() {
        return fittingCursorUI.getDataStartIndex();
    }
    
    /**
     * Gets the data start cursor time.
     * 
     * @return 
     */
    @Override
    public String getDataStartTime() {
        return fittingCursorUI.getDataStartTime();
    }
	
    /**
     * Sets the data start cursor.
     * 
     * @param index
	 * @param time
     */
    @Override
    public void setDataStart(int index, double time) {
        fittingCursor.setDataStart(index, time);
    }
    
    /**
     * Gets the transient end cursor index.
     * 
     * @return 
     */
    @Override
    public int getTransientStopIndex() {
        return fittingCursorUI.getTransientStopIndex();
    }
    
    /**
     * Gets the transient end cursor time.
     * 
     * @return 
     */
    @Override
    public double getTransientStopTime() {
        return fittingCursorUI.getTransientStopTime();
    }
	
    /**
     * Sets the transient end cursor.
     * 
     * @param index
	 * @param time
     */
    @Override
    public void setTransientStop(String transientStop) {
        fittingCursor.setTransientStop(transientStop); 
    }
    
    /**
     * Gets the prompt delay cursor index.
     * 
     * @return 
     */
    @Override
    public String getPromptDelayIndex() {
        return fittingCursorUI.getPromptDelayIndex();
    }
     
    /**
     * Gets the prompt delay cursor time.
     * 
     * @return 
     */
    @Override
    public String getPromptDelayTime() {
        return fittingCursorUI.getPromptDelayTime();
    }
	
	/**
     * Sets the prompt delay cursor.
     * 
     * @param index
	 * @param time
     */
    @Override
    public void setPromptDelay(int index, double time) {
        fittingCursor.setPromptDelay(index, time);
    }

    /**
     * Gets the prompt width cursor.
     * 
     * @return 
     */
    @Override
    public String getPromptWidth() {
        return fittingCursorUI.getPromptWidth();
    }

    /**
     * Sets the prompt width cursor.
     * 
     * @param promptWidth
     */
    @Override
    public void setPromptWidth(String promptWidth) {
        fittingCursor.setPromptWidth(promptWidth); 
    }
    
    /**
     * Gets the prompt baseline cursor as a string.
     * @return 
     */
    @Override
    public String getPromptBaseline() {
        return fittingCursorUI.getPromptBaseline();
    }

    /**
     * Sets the prompt baseline cursor as a string.
     * 
     * @param promptBaseline 
     */
    @Override
    public void setPromptBaseline(String promptBaseline) {
        fittingCursor.setPromptBaseline(promptBaseline);
    }

    /**
     * Shows current fitting cursor settings in UI.
     */
    private void showFittingCursor() {
        if (null != fittingCursorUI) {
			fittingCursorUI.setTransientStart(
					fittingCursor.getTransientStartIndex(),
					fittingCursor.getTransientStartTime());
			fittingCursorUI.setDataStart(
					fittingCursor.getDataStartIndex(),
					fittingCursor.getDataStartTime());	
			fittingCursorUI.setTransientStop(
					fittingCursor.getTransientStopIndex(),
					fittingCursor.getTransientStopTime());
            fittingCursorUI.setPromptDelay(
					fittingCursor.getPromptDelayIndex(),
					fittingCursor.getPromptDelayTime());
            fittingCursorUI.setPromptWidth(
					fittingCursor.getPromptWidthIndex(),
					fittingCursor.getPromptWidthTime());
            fittingCursorUI.setPromptBaseline(fittingCursor.getPromptBaseline()); 
        }
    }
    
    /**
     * Inner listener for cursor changes.
     */   
    private class CursorListener implements FittingCursorListener {
        @Override
        public void cursorChanged(FittingCursor cursor) {
            showFittingCursor();
        }
    }  
}
