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
 *  This is an interface to get/set transient and prompt cursors values.
 * 
 * @author Aivar Grislis
 */
public interface FittingCursorUI {
    
    /**
     * Gets the transient start cursor index.
     * 
     * @return 
     */
    public int getTransientStartIndex();
	
    /**
     * Gets the transient start cursor time.
     * 
     * @return 
     */
    public double getTransientStartTime();
	
    /**
     * Sets the transient start cursor.
     * 
     * @param index
	 * @param time
     */
    public void setTransientStart(int index, double time);
    
    /**
     * Gets the data start cursor index.
	 * 
     * @return 
     */ 
    public int getDataStartIndex();
    
    /**
     * Gets the data start cursor time.
	 * 
     * @return 
     */ 
    public double getDataStartTime(); 
	
    /**
     * Sets the data start cursor.
	 * 
     * @return 
     */
    public void setDataStart(int index, double time);

    /**
     * Gets the transient end cursor index.
     * 
     * @return 
     */
    public int getTransientStopIndex();

    /**
     * Gets the transient end cursor time.
     * 
     * @return 
     */
    public double getTransientStopTime();

    /**
     * Sets the transient end cursor.
     * 
     * @param index
	 * @param time
     */
    public void setTransientStop(int index, double time);
    
    /**
     * Gets the prompt delay cursor index.
     * 
     * @return 
     */
    public int getPromptDelayIndex();
    
    /**
     * Gets the prompt delay cursor time.
     * 
     * @return 
     */
    public double getPromptDelayTime();
	
    /**
     * Sets the prompt delay cursor.
     * 
     * @param index
	 * @param time
     */
    public void setPromptDelay(int index, double time);

    /**
     * Gets the prompt width cursor index.
     * 
     * @return 
     */
    public int getPromptWidthIndex();

    /**
     * Gets the prompt width cursor.
     * 
     * @return 
     */
    public double getPromptWidthTime();
	
    /**
     * Sets the prompt width cursor.
     * 
     * @param index
	 * @param time
     */
    public void setPromptWidth(int index, double time);
    
    /**
     * Gets the prompt baseline cursor.
     * 
     * @return 
     */
    public double getPromptBaseline();

    /**
     * Sets the prompt baseline cursor.
     * 
     * @param promptBaseline 
     */
    public void setPromptBaseline(double promptBaseline);
}
