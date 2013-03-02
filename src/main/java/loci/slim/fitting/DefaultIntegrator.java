/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting;

/**
 *
 * @author aivar
 */
public class DefaultIntegrator implements Integrator {
	public double integrate(double[] function, double offset, double inc, int start, int stop) {
		System.out.println("integrate " + start + " to " + stop);
		// sum heights
		float sum = 0.0f;
		for (int i = start; i < stop; ++i) {
			sum += function[i] - offset;
		}
		// convert to area; constant factor, doesn't affect results that much either way
		return sum * inc;
		//return sum;
	}
}
