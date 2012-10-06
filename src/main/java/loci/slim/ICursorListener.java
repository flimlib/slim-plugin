/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim;

/**
 *
 * @author aivar
 */
public interface ICursorListener {

	/**
	 * Show the cursor at fitted point.
	 * 
	 * @param x
	 * @param y 
	 */
	public void showCursor(int x, int y);

	/**
	 * Hide the cursor.
	 */
	public void hideCursor();
}
