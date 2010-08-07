/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import ij.*;
import ij.gui.*;
import ij.plugin.PlugIn;
import ij.process.*;

import loci.SLIMProcessor;

/**
 *
 * @author aivar
 */
public class SLIM_PlugIn implements PlugIn {
    public interface CLibrary extends Library {
        public int hello();
   }
    
    public void run(String arg) {
        SLIMProcessor slimProcessor = new SLIMProcessor();
        slimProcessor.run(arg);
    }

    public static void main(String [] args)
    {
        new ImageJ();
        SLIM_PlugIn plugIn = new SLIM_PlugIn();
  String name = System.getProperties().getProperty("os.name");
  String arch = System.getProperties().getProperty("os.arch");
  String version = System.getProperties().getProperty("os.version");
  System.out.println("name " + name + " arch " + arch + " version " + version);
  //System.loadLibrary("test.dylib");
	CLibrary lib = (CLibrary) Native.loadLibrary("test", CLibrary.class);
        lib.hello();
        lib.hello();
        
        plugIn.run("");
        System.exit(0);
    }
}
