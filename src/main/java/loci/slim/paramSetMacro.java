package loci.slim;

import ij.IJ;
import ij.Prefs;

import java.io.File;
import java.nio.file.Files;

import loci.curvefitter.ICurveFitter.FitAlgorithm;
import loci.curvefitter.ICurveFitter.FitFunction;


public class paramSetMacro {

	private FitAlgorithm algorithm;
	private FitFunction function; 
	private double chiSquaretarget;
	private int thresholdValue;
	
	///single exponential values
	private double a1;
	private double t1;
	
	//double exponential values
	private double a2;
	private double t2;
	
	//triple exponential values
	private double a3;
	private double t3;
	
	//shift in all exponential parameter set
	private double z1;
	private double h1;
	
	
	private double transientStart;
	private double transientStop;
	private double dataStart;
	
	private double promptBaseLine;
	private double delayPrompt;
	
	/// flags fro using macro or gui values
	public static boolean chi2MacroUsed=false;
	
	public static boolean thresholdMacroUsed=false;
	
	///single exponential macro
	public boolean a1macroused=false;
	public boolean t1macroused=false;
	//double exponential macro
	public boolean a2macroused=false;
	public boolean t2macroused=false;
	
	//triple exponential macro
	public boolean a3macroused=false;
	public boolean t3macroused=false;
	public boolean z1macroused=false;
	public boolean h1macroused=false;
	///macro-being-recorded to check to avoid double recorduing macro 
	public boolean isa1MacroRecording=false;
	public boolean ist1MacroRecording=false;
	
	
	public boolean isa2MacroRecording=false;
	public boolean ist2MacroRecording=false;
	
	public boolean isa3MacroRecording=false;
	public boolean ist3MacroRecording=false;
	
	public boolean firstTimeRecordTransientStart=true;
	public boolean firstTimeRecordTransientStop=true;
	public boolean firstTimeRecordDataStart=true;
	
	
	public boolean transientStartMacroUsed=false;
	public boolean transientStopMacroUsed=false;
	public boolean DataStartMacroUsed=false;
	
	public boolean isPromptBaseLineMacroused=false;
	public boolean isDelayExcitationMacroused=false;
	
	public String excitationFileName=null;
	public boolean isMacroUsedForExcitation=false;
	
	
	public int noOfFilesBatchProcessing=0;
	public boolean isBatchMacroUsed=false;
	
	public File []batchFileList=null;
	public boolean isExportFilemacroUsed=false;
	
	String exportPixelFileName=null;
	String exportHistofileName=null;
	String exportSummaryFileName=null;

	String []analysisType={"Export Histograms to Text",
			"Export Histograms to Text",
			"Export Pixels to Text",
			"Export Pixels to Text"
	};
	
	public boolean isAnalysisListUsed=false;
	public int noOfAnalysisList=0;
	
	
	public String exportPixelFileNameSingleFile=null;
	public String exportPixelFileNameSingleFileSeperator=null;
	
	public String exportPixelFileNameSingleFileSLIM2=null;
	public String exportPixelFileNameSingleFileSeperatorSLIM2=null;
	
	public String exportHistoFileNameSingleFile=null;
	public String exportHistoFileNameSingleFileSeperator=null;
	
	public String exportHistoFileNameSingleFileSLIM2=null;
	public String exportHistoFileNameSingleFileSeperatorSLIM2=null;
	

	
	private static final String JAOLHO_LMA_ALGORITHM = "Jaolho LMA",
			SLIM_CURVE_RLD_ALGORITHM = "SLIMCurve RLD",
			SLIM_CURVE_LMA_ALGORITHM = "SLIMCurve LMA",
			SLIM_CURVE_RLD_LMA_ALGORITHM = "SLIMCurve RLD+LMA";
	
	public paramSetMacro() {
		// TODO Auto-generated constructor stub
	
		algorithm=null;
		chiSquaretarget=-100.0;
		function=FitFunction.SINGLE_EXPONENTIAL;
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
	
	public double getChiSquareTarget(){
		return chiSquaretarget;
	}
	
	public void setChiSquareTarget(double chiVal){
		chiSquaretarget=chiVal;
	}
	
	public int getThresholdValue(){
		return thresholdValue;
	}
	
	public void setThresholdValue(int thVal){
		thresholdValue=thVal;
	}
	
	
	///single exponential parameter getter and setter
	public void seta1(double a1Val){
		a1=a1Val;
	}
	
	public double geta1(){
		return a1;
	}
	
	
	public void sett1(double t1Val){
		t1=t1Val;
	}
	
	public double gett1(){
		return t1;
	}
	
	
	///double exponential parameter getter and setter
	public void seta2(double a2Val){
		a2=a2Val;
	}
	
	public double geta2(){
		return a2;
	}
	
	
	public void sett2(double t2Val){
		t2=t2Val;
	}
	
	public double gett2(){
		return t2;
	}
	
	
	//////single exponential parameter getter and setter
	
	public void seta3(double a3Val){
		a3=a3Val;
	}
	
	public double geta3(){
		return a3;
	}
	
	
	public void sett3(double t3Val){
		t3=t3Val;
	}
	
	public double gett3(){
		return t3;
	}
	
	
	

	///z values for shift
	public void setz1(double z1Val){
		z1=z1Val;
	}
	
	public double getz1(){
		return z1;
	}
	
	
	///h values for streched expoentials
	public void seth1(double h1Val){
		h1=h1Val;
	}
	
	public double geth1(){
		return h1;
	}
///getter
	public double getTransientStartFromMacro(){
		return transientStart;
	}
	
	public double getTransientStopFromMacro(){
		return transientStop;
		
	}
	
	public double getDataStartFromMacro(){
		return dataStart;
	}
	
	
	//setter
	public void setTransientStartFromMacro(double macroVal){
		transientStart=macroVal;
	}
	
	public void setTransientStopFromMacro(double macroVal){
		transientStop=macroVal;
		
	}
	
	public void setDataStartFromMacro(double macroVal){
		dataStart=macroVal;
	}
	
	public void setExcitationFileName(String name){
		excitationFileName=name;
		
	}
	
	public void setPromptBaseLine(double value){
		promptBaseLine=value;
	}
	
	public double getPromptBaseLine(){
		return promptBaseLine;
	}
	
	
	
	public void setDelayPrompt(double value){
		delayPrompt=value;
	}
	
	public double getPromptDelay(){
		return delayPrompt;
	}
	
	public void storeFilesName (File[] files){
		batchFileList=files;
		noOfFilesBatchProcessing=files.length;
		IJ.log(Integer.toString(noOfFilesBatchProcessing));
		Prefs.set(SLIMProcessor.KEY_BATCH_MODE_FILE_NUMBER,noOfFilesBatchProcessing);//stores the number of files
		IJ.log("obtaned value after setting"+Prefs.get(SLIMProcessor.KEY_BATCH_MODE_FILE_NUMBER, null));
		
		for(int i=0; i<files.length;i++){
			String key=SLIMProcessor.KEY_FILE_NAMES+Integer.toString(i);//generates the key
			Prefs.set(key,files[i].toString());//this stores the path for the batch mode file read with macro
		}
	}
}

