//
// ExcitationPanel.java
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

package loci.slim.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import loci.slim.Excitation;
import loci.slim.fitting.cursor.FittingCursor;
import loci.slim.heuristics.ExcitationScaler;

/**
 * Panel that holds the excitation graph.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationPanel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationPanel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class ExcitationPanel extends JFrame {
    private static final String TITLE = "Instrument Response Function";
    private Excitation _excitation;
    private int _bins;
    private double _timeInc;

    public ExcitationPanel(Excitation excitation, FittingCursor fittingCursor) {

        _excitation = excitation;

        this.setTitle(TITLE);
        
        double start = fittingCursor.getPromptStartValue();
        double stop  = fittingCursor.getPromptStopValue();
        double base = fittingCursor.getPromptBaselineValue();

        double[] values = excitation.getValues();
        _bins = values.length;
        _timeInc = excitation.getTimeInc();
        ExcitationGraph excitationGraph = new ExcitationGraph(start, stop, base,
                _bins, values, _timeInc);
        excitationGraph.setFittingCursor(fittingCursor);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", createTopPanel());
        panel.add("Center", excitationGraph.getComponent());

        this.getContentPane().add(panel);

        this.setSize(450, 225);
        this.pack();
        this.setVisible(true);
    }
    
    public void quit() {
        this.setVisible(false);
    }
    
    public double[] getRawValues() {
        return _excitation.getValues();
    }

    /**
     * Gets the excitation values scaled for a particular start/stop/base cursor.
     * 
     * @param start
     * @param stop
     * @return 
     */
    public double[] getValues(double start, double stop, double base) {
        
        return ExcitationScaler.scale(_excitation.getValues(), start, stop, base, _timeInc, _bins);
    }

    /*
     * Creates a panel with file name.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
       // panel.setBorder(new EmptyBorder(0, 0, 8, 8));
       // panel.setLayout(new SpringLayout());

        //JLabel fileLabel = new JLabel("File");
        //fileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        //panel.add(fileLabel);

        panel.add(new JLabel(_excitation.getFileName()));

        // rows, cols, initX, initY, xPad, yPad
        //SpringUtilities.makeCompactGrid(panel, 1, 2, 4, 4, 4, 4);

        return panel;
    }
}
