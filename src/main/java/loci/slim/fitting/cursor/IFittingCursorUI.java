/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.cursor;

/**
 *  This is an interface to get/set transient and prompt cursors as strings.
 * 
 * @author aivar
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
