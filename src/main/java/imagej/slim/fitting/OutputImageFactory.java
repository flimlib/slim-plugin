/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

import imagej.slim.fitting.ImageFitter.OutputImage;

/**
 *
 * @author aivar
 */
public class OutputImageFactory {
    private static OutputImageFactory INSTANCE = null;
    
    private OutputImageFactory() { 
    }
    
    public static synchronized OutputImageFactory getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new OutputImageFactory();
        }
        return INSTANCE;
    }
    
    public IOutputImage createImage(OutputImage outputImage) {
        switch (outputImage) {
            case A:
            case T:
            case A1:
            case T1:
            case A2:
            case T2:
            case H:
            case Z:
            case F0:
            case F1:
        }
        return null;
    }
}
