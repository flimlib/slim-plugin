//
// FittingCursor.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
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

package loci.slim.fitting.cursor;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps cursor information.  Note that the actual fit process only deals with
 * bin number indices, but we need to keep the time value to be compatible
 * with TRI2.
 * 
 * @author Aivar Grislis
 */
public class FittingCursor {
    private static final String DOUBLE_ZERO_STRING = "0.0";
    private static final String INTEGER_ZERO_STRING = "0";
    private final double _inc;
    private final int _bins;
    private final Set<IFittingCursorListener> _listeners;
    private boolean _showBins;
    private volatile boolean _suspend;
    private boolean _hasPrompt;
    private double _promptStartValue;
    private double _promptStopValue;
    private double _promptBaselineValue;
    private double _transientStartValue;
    private double _dataStartValue;
    private double _transientStopValue;

    /**
     * Constructor
     * 
     * @param inc time increment per bin
     * @param bins total number of bins
     */
    public FittingCursor(double inc, int bins) {
        _inc = inc;
        _bins = bins;
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
                promptDelayValue = _inc * parsedInteger;
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
        _promptStartValue = _inc * bin;
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
            returnValue = (int) Math.ceil(_promptStartValue / _inc);
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
     * or time values.
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
        _promptStopValue = _inc * bin;
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
            returnValue = (int) Math.floor(_promptStopValue / _inc) + 1;
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
            returnValue = _promptBaselineValue;
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
        Double transientStartValue = null;
        if (_showBins) {
            Integer parsedInteger = getIntegerValue(transientStart);
            if (null != parsedInteger) {
                transientStartValue = _inc * parsedInteger;
            }
        }
        else {
            transientStartValue = getDoubleValue(transientStart);
        }
        if (null != transientStartValue) {
            if (transientStartValue <= _dataStartValue &&
                    transientStartValue >= 0.0) {
                _transientStartValue = transientStartValue;
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
        _transientStartValue = _inc * bin;
        notifyListeners();
    }

    /**
     * Gets the start of the transient as a bin number.
     * 
     * @return 
     */
    public int getTransientStartBin() {
        return (int) Math.ceil(_transientStartValue / _inc);
    }

    /**
     * Sets the start of the transient as a time value.
     * 
     * @param value 
     */
    public void setTransientStartValue(double value) {
        _transientStartValue = value;
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
        Double dataStartValue = null;
        if (_showBins) {
            Integer parsedInteger = getIntegerValue(dataStart);
            if (null != parsedInteger) {
                dataStartValue = _inc * parsedInteger;
            }
        }
        else {
            dataStartValue = getDoubleValue(dataStart);
        }
        if (null != dataStartValue) {
            if (dataStartValue <= _transientStopValue) {
                _dataStartValue = dataStartValue;
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
        _dataStartValue = _inc * bin;
        notifyListeners();
    }

    /**
     * Gets the start of the data as a bin number.
     * 
     * @return 
     */
    public int getDataStartBin() {
        return (int) Math.ceil(_dataStartValue / _inc);
    }

    /**
     * Sets the start of the data as a time value.
     * 
     * @param value 
     */
    public void setDataStartValue(double value) {
        _dataStartValue = value;
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
        Double transientStopValue = null;
        if (_showBins) {
            Integer parsedInteger = getIntegerValue(transientStop);
            if (null != parsedInteger) {
                transientStopValue = _inc * parsedInteger;
            }
        }
        else {
            transientStopValue = getDoubleValue(transientStop);
        }
        if (null != transientStopValue) {
            if (transientStopValue >= _dataStartValue &&
                    transientStopValue <= _inc * _bins) {
                _transientStopValue = transientStopValue;
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
        _transientStopValue = _inc * bin;
        notifyListeners();
    }

    /**
     * Gets the end of the transient as a bin number.
     * 
     * @return 
     */
    public int getTransientStopBin() {
        return (int) Math.floor(_transientStopValue / _inc) + 1;
    }

    /**
     * Sets the end of the transient as a time value.
     * 
     * @param value 
     */
    public void setTransientStopValue(double value) {
        _transientStopValue = value;
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
