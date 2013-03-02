/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting;

/**
 *
 * @author aivar
 */
public interface Integrator {
	public double integrate(double[] function, double offset, double inc, int start, int stop);
}
