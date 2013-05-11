/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.fitted;

//TODO ARG this class exists only for testing
/**
 *
 * @author Aivar Grislis
 */
public class RampGenerator {
	public enum RampType { UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT, TOP, LEFT, RIGHT, BOTTOM };
	private final RampType rampType;
	private final long width;
	private final long height;
	private final long diagonal;
	
	public RampGenerator(RampType rampType, long width, long height) {
		this.rampType = rampType;
		this.height = height;
		this.width = width;
		this.diagonal = getDiagonal(width, height);
	}
	
	public double getValue(long[] position) {
		return getValue(position[0], position[1]);
	}
	
	public double getValue(long x, long y) {
		long i = 0;
		long j = diagonal;
		switch (rampType) {
			case UPPER_LEFT:
				i = getDiagonal(x, y);
				break;
			case UPPER_RIGHT:
				i = getDiagonal(width - x - 1, y);
				break;
			case LOWER_LEFT:
				i = getDiagonal(x, height - y - 1);
				break;
			case LOWER_RIGHT:
				i = getDiagonal(width - x - 1, height - y - 1);
				break;
			case TOP:
				i = y;
				j = height;
				break;
			case LEFT:
				i = x;
				j = width;
				break;
			case RIGHT:
				i = width - x - 1;
				j = width;
				break;
			case BOTTOM:
				i = height - y - 1;
				j = height;
				break;
		}
		return ((double) i) / j;
	}
	
	private long getDiagonal(long width, long height) {
		long returnValue = (long) Math.sqrt(width * width + height * height);
		//System.out.println("width " + width + " HEIGHt " + height + " returnValue " + returnValue);
		return returnValue;
	}
}
