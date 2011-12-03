/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.process;

import java.awt.image.ColorModel;

/**
 *
 * @author aivar
 */
public class MyFloatProcessor extends FloatProcessor {
    
	/** Creates a new FloatProcessor using the specified pixel array and ColorModel.
		Set 'cm' to null to use the default grayscale LUT. */
	public MyFloatProcessor(int width, int height, float[] pixels, ColorModel cm) {
            super(width, height, pixels, cm);
	}

	/** Creates a blank FloatProcessor using the default grayscale LUT that
		displays zero as black. Call invertLut() to display zero as white. */
	public MyFloatProcessor(int width, int height) {
            super(width, height);
	}

	/** Creates a FloatProcessor from an int array using the default grayscale LUT. */
	public MyFloatProcessor(int width, int height, int[] pixels) {
            super(width, height, pixels);
	}
	
	/** Creates a FloatProcessor from a double array using the default grayscale LUT. */
	public MyFloatProcessor(int width, int height, double[] pixels) {
            super(width, height, pixels);
	}
	
	/** Creates a FloatProcessor from a 2D float array using the default LUT. */
	public MyFloatProcessor(float[][] array) {
            super(array);
	}

	/** Creates a FloatProcessor from an int[][] array. */
	public MyFloatProcessor(int[][] array) {
            super(array);
	}
        
        public byte[] create8BitImage() {
            return super.create8BitImage();
        }
    
}
