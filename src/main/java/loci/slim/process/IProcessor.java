/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.process;

/**
 *
 * @author aivar
 */
public interface IProcessor {

    /**
     * Specifies a source IProcessor to be chained to this one.
     * 
     * @param processor 
     */
    public void chain(IProcessor processor);
    
    /**
     * Gets input pixel value.
     * 
     * @param x
     * @param y
     * @param channel
     * @return null or pixel value
     */
    public double[] getPixel(int[] location);
}
