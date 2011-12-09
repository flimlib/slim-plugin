/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.fitting;

import imagej.slim.fitting.engine.IFittingEngine;
import imagej.slim.fitting.engine.ThreadedFittingEngine;

/**
 *
 * @author aivar
 */
public class ImageFitter {
    public enum OutputImage { A, T, A1, T1, A2, T2, A3, T3, H, Z, CHISQ, F0, F1 };
    private IFittingEngine _fittingEngine;
    
    public ImageFitter() {
        _fittingEngine = new ThreadedFittingEngine();
    }
    
    public void fit(OutputImage[] images) {
        
    }
}
