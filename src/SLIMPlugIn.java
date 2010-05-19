/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.*;
import ij.gui.*;
import ij.plugin.PlugIn;
import ij.process.*;

import loci.SLIMProcessor;

/**
 *
 * @author aivar
 */
public class SLIMPlugIn implements PlugIn {
    
    public void run(String arg) {
        SLIMProcessor slimProcessor = new SLIMProcessor();
        slimProcessor.run(arg);
    }

    public static void main(String [] args)
    {
        SLIMPlugIn plugIn = new SLIMPlugIn();
        plugIn.run("HELLO WORLD");
        System.out.println("back from plugin");
        System.exit(0);
    }
}
