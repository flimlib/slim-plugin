package loci.slim;

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;


public class paramSetMacro {

	private FitAlgorithm algorithm;
	private FitFunction function; 
	
	private static final String JAOLHO_LMA_ALGORITHM = "Jaolho LMA",
			SLIM_CURVE_RLD_ALGORITHM = "SLIMCurve RLD",
			SLIM_CURVE_LMA_ALGORITHM = "SLIMCurve LMA",
			SLIM_CURVE_RLD_LMA_ALGORITHM = "SLIMCurve RLD+LMA";
	
	public paramSetMacro() {
		// TODO Auto-generated constructor stub
	
		algorithm=FitAlgorithm.SLIMCURVE_RLD_LMA;
		function=FitFunction.DOUBLE_EXPONENTIAL;
	}
	

	public void setFunction(String arg){
		
		
		if (arg.equals("Single Exponential")) {
			function = FitFunction.SINGLE_EXPONENTIAL;
		}
		else if (arg.equals("Double Exponential")) {
			function = FitFunction.DOUBLE_EXPONENTIAL;
		}
		else if (arg.equals( "Triple Exponential")) {
			function = FitFunction.TRIPLE_EXPONENTIAL;
		}
		else if (arg.equals("Stretched Exponential")) {
			function = FitFunction.STRETCHED_EXPONENTIAL;
		}
	}
	
	
	
	public void setAlgorithm(String selected) {
		
		
		if (selected.equals(JAOLHO_LMA_ALGORITHM)) {
			algorithm = FitAlgorithm.JAOLHO;
		}
		else if (selected.equals(SLIM_CURVE_RLD_ALGORITHM)) {
			algorithm = FitAlgorithm.SLIMCURVE_RLD;
		}
		else if (selected.equals(SLIM_CURVE_LMA_ALGORITHM)) {
			algorithm = FitAlgorithm.SLIMCURVE_LMA;
		}
		else if (selected.equals(SLIM_CURVE_RLD_LMA_ALGORITHM)) {
			algorithm = FitAlgorithm.SLIMCURVE_RLD_LMA;
		}
		
	}
	

	
	public FitFunction getFunction() {
		return function;
	}
	public FitAlgorithm getAlgorithm(){
		return algorithm;
	} 

	
}
