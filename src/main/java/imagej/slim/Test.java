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

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;

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
        _fittingEngine.setCurveFitter(Configuration.getInstance().getCurveFitter());
        
        double[] y = new double[]{
          0,3,1,2,2,0,0,0,1,1,0,0,1,1,0,1,0,2,2,1,1,1,1,1,0,1,2,1,0,0,1,0,2,3,2,0,0,
          0,0,1,0,2,0,2,0,0,0,2,0,0,2,3,2,3,4,3,7,12,3,5,3,7,10,1,7,3,6,8,1,10,6,4,6,
          7,6,4,7,4,3,4,3,0,2,3,1,4,2,3,5,1,3,6,5,7,4,3,4,6,6,5,3,3,5,5,4,1,3,4,4,5,
          5,3,4,3,2,2,4,3,3,2,4,1,3,3,4,4,2,6,0,1,0,6,3,3,4,3,3,4,1,3,0,2,1,1,2,1,1,
          0,3,2,1,1,2,1,3,1,1,3,1,1,0,2,1,1,3,0,3,1,1,2,0,1,3,0,2,0,1,4,1,1,3,0,3,1,
          3,2,4,1,1,0,3,1,2,0,1,1,0,2,1,4,2,0,2,0,2,0,2,0,2,0,0,1,2,0,0,1,0,1,1,2,2,
          1,3,3,1,1,1,2,0,0,0,4,2,1,1,2,3,4,1,4,1,2,1,2,2,0,2,0,1,0,1,1,0,1,1,1 };
        double[] yFitted = new double[y.length];
        double xInc = 0.0390625;
        int fitStart = 40;
        int fitStop = 210;
        int nData = y.length;
        double[] sig = null; // new double[] { 1.0 };
        double[] params = new double[] { 0.0, 0.5, 100.0, 0.5 };
        
        IGlobalFitParams globalFitParams = new GlobalFitParams();
        globalFitParams.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA);
        globalFitParams.setFitFunction(FitFunction.SINGLE_EXPONENTIAL);
        globalFitParams.setXInc(xInc);
        globalFitParams.setPrompt(null);
        globalFitParams.setChiSquareTarget(0.0);
        //TODO globalFitParams.setStartPrompt/setStopPrompt/setStartDecay/setStopDecay
        
        System.out.println(" x inc is " + globalFitParams.getXInc());
        
        ILocalFitParams localFitParams = new LocalFitParams();
        localFitParams.setY(y);
        localFitParams.setSig(sig);
        localFitParams.setParams(params);
        localFitParams.setFitStart(fitStart);
        localFitParams.setFitStop(fitStop);
        localFitParams.setYFitted(yFitted); //TODO ARG kludgey
        
        /*IGlobalFitParams params = new GlobalFitParams();
        List<ILocalFitParams> dataList = new ArrayList<ILocalFitParams>();
        for (int i = 0; i < 128*128; ++i) {
            ILocalFitParams data = new LocalFitParams();
            //data.setId(i);
            dataList.add(data);
        }*/
        List<ILocalFitParams> dataList = new ArrayList<ILocalFitParams>();
        dataList.add(localFitParams);
        long time = System.currentTimeMillis();
        _fittingEngine.fit(globalFitParams, dataList);
        System.out.println("DONE " + ((float) (System.currentTimeMillis() - time)/1000) + " secs");
        _fittingEngine.shutdown();
    }         
}
