/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.process;

/**
 * A plugin within a plugin, this is used to bin the fit input.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/binning/plugins/Bin3x3.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/binning/plugins/Bin3x3.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
@SLIMBinner("5 x 5")
public class Bin5x5 extends SquareBinner implements ISLIMBinner {
    public void init(int width, int height) {
        super.init(2, width, height);
    }
}
