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

import loci.slim2.heuristics.DefaultFitterEstimator;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import loci.curvefitter.IFitterEstimator;

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
    private int transientStartBin;
    private int dataStartBin;
    private int transientStopBin;
    private double promptStartValue;
    private double promptStopValue;
    private double promptBaselineValue;
    private double transientStartValue;
    private double dataStartValue;
    private double transientStopValue;

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
		 throw new RuntimeException("blah blah");
		}
        synchronized (listeners) {
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
     * Temporarily suspends listener notifications.  Used when several cursors
     * change at the same time.  Call suspendNotifications followed by cursor
     * changes followed by sendNotifications.
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
	public void setPromptDelayIndex(int promptDelay) {
		setPromptDelayTime(fitterEstimator.binToValue(promptDelay, inc));
	}

	/**
	 * Sets the prompt delay as a time value.
	 * 
	 * @param promptDelayTime 
	 */
	public void setPromptDelayTime(double promptDelayTime) {
		// convert delay to start
		double promptStartTime = promptDelayTime + transientStartValue;
		System.out.println("promptStartTime " + promptStartTime);
            
		// some very rudimentary error-checking
		if (0.0 < promptStartTime && promptStartTime < transientStopValue) {
			double diff = promptStartTime - promptStartValue;
			promptStartValue += diff;
			promptStopValue  += diff;
			System.out.println("diff " + diff + " promptStartValue " + promptStartValue + " promptStopValue " + promptStopValue);
		}
		else System.out.println("rejected");
			
        // either update others with new valid value or undo our invalid value
        notifyListeners();
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
			returnValue = getPromptStartValue() - getTransientStartValue();
			returnValue = fitterEstimator.roundToDecimalPlaces(returnValue, DECIMAL_PLACES);
		}
		return returnValue;
	}

    /**
     * Sets the start of the prompt as a bin index.
     * 
     * @param index
     */
    public void setPromptStartIndex(int index) {
        promptStartValue = fitterEstimator.binToValue(index, inc);
        notifyListeners();
    }

    /**
     * Sets the start of the prompt as a time value.
     * 
     * @param value 
     */
    public void setPromptStartTime(double value) {
        promptStartValue = value;
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
            returnValue = fitterEstimator.valueToBin(promptStartValue, inc);
        }
        return returnValue;
    }


    /**
     * Gets the start of the prompt as a time value.
     * 
     * @return 
     */
    public double getPromptStartValue() {
        double returnValue = 0.0;
        if (hasPrompt) {
            returnValue = promptStartValue;
        }
        return returnValue;
    }
 
    /**
     * Sets the end of the prompt based on a prompt width bin index.
     * 
     * @param promptWidth 
     */
    public void setPromptWidth(int promptWidth) {
		
        Double promptWidthValue = null;
        if (showBins) {
            Integer parsedInteger = getIntegerValue(promptWidth);
            if (null != parsedInteger) {
                promptWidthValue = inc * parsedInteger;
            }
        }
        else {
            promptWidthValue = getDoubleValue(promptWidth);
        }
        if (null != promptWidthValue) {
            double promptStopValue = getPromptStartValue() + promptWidthValue;
            if (promptStopValue >= promptStartValue &&
                    promptStopValue <= bins * inc) {
                this.promptStopValue = promptStopValue;
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
        if (showBins) {
            if (hasPrompt) {
                int width = getPromptStopBin() - getPromptStartBin();
                returnValue.append(width);
            }
            else {
                returnValue.append(INTEGER_ZERO_STRING);
            }
        }
        else {
            if (hasPrompt) {
                double width = getPromptStopValue() - getPromptStartValue();
                width = fitterEstimator.roundToDecimalPlaces(width, DECIMAL_PLACES);                
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
        promptStopValue = fitterEstimator.binToValue(bin, inc);
        notifyListeners();
    }

    /**
     * Gets the end of the prompt as a bin number.
     * 
     * @return 
     */
    public int getPromptStopBin() {
        int returnValue = 0;
        if (hasPrompt) {
            returnValue = ((DefaultFitterEstimator) fitterEstimator).valueToBin(promptStopValue, inc);
        }
        return returnValue;
    }

    /**
     * Sets the end of the prompt as a time value.
     * 
     * @param value 
     */
    public void setPromptStopValue(double value) {
        promptStopValue = value;
        notifyListeners();
    }

    /**
     * Gets the end of the prompt as a time value.
     * 
     * @return 
     */
    public double getPromptStopValue() {
        double returnValue = 0.0;
        if (hasPrompt) {
            returnValue = promptStopValue;
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
            this.promptBaselineValue = promptBaselineValue;
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
        if (hasPrompt) {
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
        promptBaselineValue = value;
        notifyListeners();
    }

    /**
     * Gets the baseline of the prompt as a value.
     * 
     * @return
     */
    public double getPromptBaselineValue() {
        double returnValue = 0.0;
        if (hasPrompt) {
            returnValue = fitterEstimator.roundToDecimalPlaces(promptBaselineValue, DECIMAL_PLACES);
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
        if (showBins) {
            Integer bin = getIntegerValue(transientStart);
            if (null != bin) {
                if (bin <= dataStartBin && bin >= 0) {
                    transientStartBin = bin;
                    transientStartValue = fitterEstimator.binToValue(bin, inc);
                }
            }
        }
        else {
            Double value = getDoubleValue(transientStart);
            if (null != value) {
                if (value <= dataStartValue && value >= 0.0) {
                    transientStartValue = value;
                    transientStartBin = fitterEstimator.valueToBin(value, inc);
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
        if (showBins) {
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
        transientStartBin = bin;
        transientStartValue = fitterEstimator.binToValue(bin, inc);

        notifyListeners();
    }

    /**
     * Gets the start of the transient as a bin number.
     * 
     * @return 
     */
    public int getTransientStartBin() {
        return transientStartBin;
    }

    /**
     * Sets the start of the transient as a time value.
     * 
     * @param value 
     */
    public void setTransientStartValue(double value) {
        transientStartValue = fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
        transientStartBin = fitterEstimator.valueToBin(value, inc);
        
        notifyListeners();
    }

    /**
     * Gets the start of the transient as a time value.
     * 
     * @return 
     */
    public double getTransientStartValue() {
        return transientStartValue;
    }
    
    /**
     * Sets the start of the data based on a string.  Handles bins or time
     * values.
     * 
     * @param dataStart 
     */    
    public void setDataStart(String dataStart) {
        if (showBins) {
            Integer bin = getIntegerValue(dataStart);
            if (null != bin) {
                if (bin >= transientStartBin && bin <= transientStopBin) {
                    dataStartBin = bin;
                    dataStartValue = fitterEstimator.binToValue(bin, inc);
                }
            }
        }
        else {
            Double value = getDoubleValue(dataStart);
            if (null != value) {
                if (value >= transientStartValue && value <= transientStopValue) {
                    dataStartValue = value;
                    dataStartBin = fitterEstimator.valueToBin(value, inc);
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
        if (showBins) {
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
        dataStartBin = bin;
        dataStartValue = fitterEstimator.binToValue(bin, inc);
        notifyListeners();
    }

    /**
     * Gets the start of the data as a bin number.
     * 
     * @return 
     */
    public int getDataStartBin() {
        return dataStartBin;
    }

    /**
     * Sets the start of the data as a time value.
     * 
     * @param value 
     */
    public void setDataStartValue(double value) {
        dataStartValue = fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
        dataStartBin = fitterEstimator.valueToBin(value, inc);
        notifyListeners();
    }

    /**
     * Gets the start of the data as a time value.
     * 
     * @return 
     */
    public double getDataStartValue() {
        return dataStartValue;
    }
    
    /**
     * Sets the end of the transient based on a string.  Handles bins or time
     * values.
     * 
     * @param transientStop 
     */
    public void setTransientStop(String transientStop) {
        if (showBins) {
            Integer bin = getIntegerValue(transientStop);
            if (null != bin) {
                if (bin >= dataStartBin && bin < bins) {
                    transientStopBin = bin;
                    transientStopValue = fitterEstimator.binToValue(bin, inc);
                }
            }
        }
        else {
            Double value = getDoubleValue(transientStop);
            if (null != value) {
                if (value >= dataStartValue && value <= inc * bins) {
                    transientStopValue = value;
                    transientStopBin = ((DefaultFitterEstimator) fitterEstimator).endValueToBin(value, inc);
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
        if (showBins) {
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
        transientStopBin = bin;
        transientStopValue = fitterEstimator.binToValue(bin, inc);
        notifyListeners();
    }

    /**
     * Gets the end of the transient as a bin number.
     * 
     * @return 
     */
    public int getTransientStopBin() {
        return transientStopBin;
    }

    /**
     * Sets the end of the transient as a time value.
     * 
     * @param value 
     */
    public void setTransientStopValue(double value) {
        transientStopValue = fitterEstimator.roundToDecimalPlaces(value, DECIMAL_PLACES);
        transientStopBin = ((DefaultFitterEstimator) fitterEstimator).endValueToBin(value, inc);
        notifyListeners();
    }

    /**
     * Gets the end of the transient as a time value.
     * 
     * @return 
     */
    public double getTransientStopValue() {
        return transientStopValue;
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
        if (!suspend) {
            while (!success) {
                try {
                    synchronized (listeners) {
                        for (FittingCursorListener listener : listeners) {
							if (null == listener) {
								System.out.println("null listener for FC");
							}
							else {
								System.out.println("notify listener " + listener);
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

