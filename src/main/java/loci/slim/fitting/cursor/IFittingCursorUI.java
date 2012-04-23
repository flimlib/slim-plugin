//
// IFittingCursorUI.java
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

/**
 *  This is an interface to get/set transient and prompt cursors as strings.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public interface IFittingCursorUI {
    
    /**
     * Gets the transient start cursor.
     * 
     * @return 
     */
    public String getTransientStart();
  
    /**
     * Sets the transient start cursor.
     * 
     * @param transientStart 
     */
    public void setTransientStart(String transientStart);
    
    /**
     * Gets the data start cursor.
     * @return 
     */ 
    public String getDataStart();
    
    /**
     * Sets the data start cursor.
     * @return 
     */
    public void setDataStart(String dataStart);

    /**
     * Gets the transient end cursor.
     * 
     * @return 
     */
    public String getTransientStop();

    /**
     * Sets the transient end cursor.
     * 
     * @param transientStop 
     */
    public void setTransientStop(String transientStop);
    
    /**
     * Gets the prompt delay cursor.
     * 
     * @return 
     */
    public String getPromptDelay();
  
    /**
     * Sets the prompt delay cursor.
     * 
     * @param promptDelay 
     */
    public void setPromptDelay(String promptDelay);

    /**
     * Gets the prompt width cursor.
     * 
     * @return 
     */
    public String getPromptWidth();

    /**
     * Sets the prompt width cursor.
     * 
     * @param promptWidth 
     */
    public void setPromptWidth(String promptWidth);
    
    /**
     * Gets the prompt baseline cursor.
     * 
     * @return 
     */
    public String getPromptBaseline();

    /**
     * Sets the prompt baseline cursor.
     * 
     * @param promptBaseline 
     */
    public void setPromptBaseline(String promptBaseline);
}
