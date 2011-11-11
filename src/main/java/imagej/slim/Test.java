/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim;

import imagej.slim.fitting.params.IGlobalFitParams;
import imagej.slim.fitting.params.LocalFitParams;
import imagej.slim.fitting.params.GlobalFitParams;
import imagej.slim.fitting.engine.IFittingEngine;
import imagej.slim.fitting.params.ILocalFitParams;
import imagej.slim.fitting.config.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aivar Grislis
 */
public class Test {
    IFittingEngine _fittingEngine;
        
    public void run() {
        _fittingEngine = Configuration.getInstance().getFittingEngine();
        _fittingEngine.setThreads(Configuration.getInstance().getThreads());
        IGlobalFitParams params = new GlobalFitParams();
        List<ILocalFitParams> dataList = new ArrayList<ILocalFitParams>();
        for (int i = 0; i < 128*128; ++i) {
            ILocalFitParams data = new LocalFitParams();
            data.setId(i);
            dataList.add(data);
        }
        long time = System.currentTimeMillis();
        _fittingEngine.fit(params, dataList);
        System.out.println("DONE " + ((float) (System.currentTimeMillis() - time)/1000) + " secs");
        _fittingEngine.shutdown();
    }         
}
