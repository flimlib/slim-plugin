//
// Cursor.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2011, ImageJDev.org.
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

import java.util.Collections;
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
    private double _inc;
    private Set<IFittingCursorListener> _listeners;
    private boolean _showBins;
    private double _promptStartValue;
    private double _promptStopValue;
    private double _promptBaselineValue;
    private double _transientStartValue;
    private double _dataStartValue;
    private double _transientStopValue;
    
    public FittingCursor(double inc) {
        _inc = inc;
        _listeners = Collections.synchronizedSet(new HashSet<IFittingCursorListener>());
        _showBins = false;
    }
    
    public void addListener(IFittingCursorListener listener) { 
        _listeners.add(listener);
    }
    
    public void removeListener(IFittingCursorListener listener) {
        _listeners.remove(listener);
    }
    
    public void setShowBins(boolean showBins) {
        _showBins = showBins;
    }
    
    public boolean getShowBins() {
        return _showBins;
    }
    
    public void setPromptStart(String promptStart) {
        
    }
    
    public String getPromptStart() {
        StringBuffer returnValue = new StringBuffer();
        if (_showBins) {
            returnValue.append(getPromptStartBin());
        }
        else {
            returnValue.append(getPromptStartValue());
        }
        return returnValue.toString();
    }

    public void setPromptStartBin(int bin) {
        _promptStartValue = _inc * bin;
        notifyListeners();
    }

    public int getPromptStartBin() {
        return (int) Math.floor(_promptStartValue / _inc);
    }
    
    public void setPromptStartValue(double value) {
        _promptStartValue = value;
        notifyListeners();
    }
    
    public double getPromptStartValue() {
        int bin = getPromptStartBin();
        return bin * _inc;
    }
    
    public void setPromptStop(String promptStop) {
        
    }
    
    public String getPromptStop() {
        StringBuffer returnValue = new StringBuffer();
        if (_showBins) {
            returnValue.append(getPromptStopBin());
        }
        else {
            returnValue.append(getPromptStopValue());
        }
        return returnValue.toString();
    }

    public void setPromptStopBin(int bin) {
        _promptStopValue = _inc * bin;
        notifyListeners();
    }

    public int getPromptStopBin() {
        return (int) Math.ceil(_promptStopValue / _inc);
    }
    
    public void setPromptStopValue(double value) {
        _promptStopValue = value;
        notifyListeners();
    }
    
    public double getPromptStopValue() {
        return _promptStopValue;
    }
    
    public void setPromptBaseline(String promptBaseline) {
        
    }
    
    public String getPromptBaseline() {
        StringBuffer returnValue = new StringBuffer();
        if (_showBins) {
            returnValue.append(getPromptBaselineBin());
        }
        else {
            returnValue.append(getPromptBaselineValue());
        }
        return returnValue.toString();
    }

    public void setPromptBaselineBin(int bin) {
        _promptBaselineValue = _inc * bin;
        notifyListeners();
    }
    
    public int getPromptBaselineBin() {
        return (int) Math.floor(_promptBaselineValue / _inc);
    }
    
    public void setPromptBaselineValue(double value) {
        _promptBaselineValue = value;
        notifyListeners();
    }
    
    public double getPromptBaselineValue() {
        return _promptBaselineValue;
    }
    
    public void setTransientStart(String transientStart) {
        
    }
    
    public String getTransientStart() {
        StringBuffer returnValue = new StringBuffer();
        if (_showBins) {
            returnValue.append(getTransientStartBin());
        }
        else {
            returnValue.append(getTransientStartValue());
        }
        return returnValue.toString();
    }
    
    public void setTransientStartBin(int bin) {
        _transientStartValue = _inc * bin;
        notifyListeners();
    }
    
    public int getTransientStartBin() {
        return (int) Math.floor(_transientStartValue / _inc);
    }
    
    public void setTransientStartValue(double value) {
        _transientStartValue = value;
        notifyListeners();
    }
    
    public double getTransientStartValue() {
        return _transientStartValue;
    }
    
    public void setDataStart(String dataStart) {
        
    }
    
    public String getDataStart() {
        StringBuffer returnValue = new StringBuffer();
        if (_showBins) {
            returnValue.append(getDataStartBin());
        }
        else {
            returnValue.append(getDataStartValue());
        }
        return returnValue.toString();
    }
    
    public void setDataStartBin(int bin) {
        _dataStartValue = _inc * bin;
        notifyListeners();
    }
    
    public int getDataStartBin() {
        return (int) Math.floor(_dataStartValue / _inc);
    }
    
    public void setDataStartValue(double value) {
        _dataStartValue = value;
        notifyListeners();
    }
    
    public double getDataStartValue() {
        return _dataStartValue;
    }
    
    
    public void setTransientStop(String transientStop) {
        
    }
    
    public String getTransientStop() {
        StringBuffer returnValue = new StringBuffer();
        if (_showBins) {
            returnValue.append(getTransientStopBin());
        }
        else {
            returnValue.append(getTransientStopValue());
        }
        return returnValue.toString();
    }
    
    public void setTransientStopBin(int bin) {
        _transientStopValue = _inc * bin;
        notifyListeners();
    }
    
    public int getTransientStopBin() {
        return (int) Math.ceil(_transientStopValue / _inc);
    }
    
    public void setTransientStopValue(double value) {
        _transientStopValue = value;
        notifyListeners();
    }
    
    public double getTransientStopValue() {
        return _transientStopValue;
    }
    
    public double binToValue(int bin, boolean isStart) {
        return isStart ? 0.0 : 1.0;
    }
    
    public int valueToBin(double value) {
        return 1;
    }
    
    private void notifyListeners() {
        System.out.println("notify " + _listeners.size() + " listeners");
        for (IFittingCursorListener listener : _listeners) {
            listener.cursorChanged(this);
        }
    }
}
