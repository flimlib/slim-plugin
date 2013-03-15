//
// SLIM_PlugIn.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim;

import java.util.Stack;

import ij.IJ;
import ij.ImageJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/SLIM_PlugIn.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/SLIM_PlugIn.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class SLIM_PlugIn implements PlugIn {
	private static final String ONE = "1";
	private static final Stack<SLIMProcessor> stack = new Stack<SLIMProcessor>();
	private static volatile SLIMProcessor instance = null;

    public void run(String arg) {
        SLIMProcessor slimProcessor = new SLIMProcessor();
		stack.push(slimProcessor);
        slimProcessor.process(arg);
		stack.pop();
    }

    public static void main(String [] args)
    {
        new ImageJ();
        SLIM_PlugIn plugIn = new SLIM_PlugIn();
        plugIn.run("");
        System.exit(0);
    }

	/**
	 * Starts up batch processing.
	 *
	 * @return whether or not successful
	 */
	public static boolean startBatch() {
		boolean success = false;
		instance = null;
		if (stack.empty()) {
			GenericDialog dialog = new GenericDialog("Error in Batch Processing");
			dialog.addMessage("SLIM Plugin should be running before invoking batch processing macro.");
			dialog.showDialog();
		}
		else {
			instance = stack.peek();
			success = instance.startBatch();
		}
		return success;
	}

	/**
	 * Processes an input file in batch processing.
	 * 
	 * @param input file name
	 * @param output file name
	 * @param exportPixels
	 * @param exportText 
	 */
	public static void batch(String input, String output, String exportPixels, String exportText) {
		if (null != instance) {
			instance.batch(input, output, ONE.equals(exportPixels), ONE.equals(exportText));
		}
	}

	/**
	 * Ends batch processing.
	 */
	public static void endBatch() {
		if (null != instance) {
			instance.endBatch();
		}
		instance = null;
	}
	
	
	//TODO ARG EXPERIMENTAL
	/**
	 * Starts up batch processing.
	 *
	 * @return whether or not successful
	 */
	public static boolean startBatchHisto() {
		boolean success = false;
		instance = null;
		if (stack.empty()) {
			GenericDialog dialog = new GenericDialog("Error in Batch Processing");
			dialog.addMessage("SLIM Plugin should be running before invoking batch processing macro.");
			dialog.showDialog();
		}
		else {
			instance = stack.peek();
			success = instance.startBatchHisto();
		}
		return success;
	}

	/**
	 * Processes an input file in batch processing.
	 * 
	 * @param input file name
	 * @param output file name
	 */
	public static void batchHisto(String input, String output) {
		if (null != instance) {
			instance.batchHisto(input, output);
		}
	}

	/**
	 * Ends batch processing.
	 */
	public static void endBatchHisto() {
		if (null != instance) {
			instance.endBatchHisto();
		}
		instance = null;
	}
	
}
