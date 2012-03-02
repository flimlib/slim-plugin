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
     * Gets the prompt start cursor.
     * 
     * @return 
     */
    public String getPromptStart();
  
    /**
     * Sets the prompt start cursor.
     * 
     * @param promptStart 
     */
    public void setPromptStart(String promptStart);

    /**
     * Gets the prompt end cursor.
     * 
     * @return 
     */
    public String getPromptStop();

    /**
     * Sets the prompt end cursor.
     * 
     * @param promptStop 
     */
    public void setPromptStop(String promptStop);
    
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
