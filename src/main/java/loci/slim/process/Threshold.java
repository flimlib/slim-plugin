/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.process;

/**
 * This class thresholds the image to a given photon count.
 * 
 * @author aivar
 */
public class Threshold implements IProcessor {
    private final int _start;
    private final int _stop;
    private final int _photons;
    private IProcessor _processor;
    
    public Threshold(int start, int stop, int threshold) {
        _start            = start;
        _stop             = stop;
        _photons        = threshold;
    }
    
    /**
     * Specifies a source IProcessor to be chained to this one.
     * 
     * @param processor 
     */
    public void chain(IProcessor processor) {
        _processor = processor;
    }
    
    /**
     * Gets input pixel value.
     * 
     * @param x
     * @param y
     * @param channel
     * @return null or pixel value
     */
    public double[] getPixel(int[] location) {
        double[] decay = _processor.getPixel(location);
        
        // reject any pixels that have less than the threshold number of photons
        if (null != decay) {
            double sum = 0.0;
            for (int bin = _start; bin <= _stop; ++bin) {
                sum += decay[bin];
            }
            if (sum < _photons) {
                decay = null;
            }
        }
        return decay;
    }  
}
