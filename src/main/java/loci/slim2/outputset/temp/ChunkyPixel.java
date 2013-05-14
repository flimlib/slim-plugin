/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.outputset.temp;

/**
 *
 * @author aivar
 */
public class ChunkyPixel {
	private final long[] position;
	private final long width;
	private final long height;
	
	public ChunkyPixel(long[] position, long width, long height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}
	
	public long[] getPosition() {
		return position;
	}
	
	public long getWidth() {
		return width;
	}
	
	public long getHeight() {
		return height;
	}
}
