/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.process;

/**
 * This class bins the image.
 * 
 * @author aivar
 */
public class SquareBinner implements IProcessor {
    private int _size;
    private int _width;
    private int _height;
    private IProcessor _processor;

    /**
     * Initializes the binner.  Must be called once after instantiation and
     * before use.
     * 
     * @param size
     * @param width
     * @param height 
     */
    public void init(int size, int width, int height) {
        _size   = size;
        _width  = width;
        _height = height;
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
     * @param location
     * @return pixel value
     */
    public double[] getPixel(int[] location) {
        double[] sum = _processor.getPixel(location).clone();
        
        int x = location[0];
        int y = location[1];
        
        int startX = x - _size;
        if (startX < 0) {
            startX = 0;
        }
        int stopX  = x + _size;
        if (stopX >= _width) {
            stopX = _width - 1;
        }
        int startY = y - _size;
        if (startY < 0) {
            startY = 0;
        }
        int stopY  = y + _size;
        if (stopY >= _height) {
            stopY = _height - 1;
        }
        
        for (int j = startY; j <= stopY; ++j) {
            for (int i = startX; i <= stopX; ++i) {
                if (j != y || i != x) {
                    location[0] = i;
                    location[1] = j;
                    double[] pixel = _processor.getPixel(location);
                    
                    if (null != pixel) {
                        add(sum, pixel);
                    }
                }
            }
        }
        return sum;
    }

    /*
     * Adds together two decays.
     */
    private void add(double[] sum, double[] pixel) {
        for (int i = 0; i < sum.length; ++i) {
            sum[i] += pixel[i];
        }
    }
}
