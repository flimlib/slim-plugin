/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.cursor;

/**
 *
 * @author aivar
 */
public interface IPromptCursorUI {
    
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
