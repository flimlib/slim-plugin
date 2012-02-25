/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.cursor;

/**
 *
 * @author aivar
 */
public interface ITransientCursorUI {
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
}
