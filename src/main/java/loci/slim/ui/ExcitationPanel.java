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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;


import loci.slim.Excitation;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationPanel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/ExcitationPanel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class ExcitationPanel extends JFrame {
    private Excitation m_excitation;
    private int m_start;
    private int m_stop;
    private int m_base;
    private JTextField m_fileField;
    private JTextField m_startField;
    private JTextField m_stopField;
    private JTextField m_baseField;

    public ExcitationPanel(Excitation excitation) {

        m_excitation = excitation;

        this.setTitle("Instrument Response Function");

        int start = excitation.getStart();
        int stop = excitation.getStop();
        float base = excitation.getBase();
        float[] values = excitation.getValues();
        int bins = values.length;
        int timeInc = 1;
        ExcitationGraph excitationGraph = new ExcitationGraph(start, stop, base, bins, timeInc, values);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", createTopPanel());
        panel.add("Center", excitationGraph.getComponent());
        panel.add("South", createBottomPanel());

        this.getContentPane().add(panel);

        this.setSize(450, 450);
        this.pack();
        this.setVisible(true);
        // load the excitation curve
        // fit default cursors
        // show excitation graph
        // show additional UI
    }
    
    public void quit() {
        this.setVisible(false);
    }
    
    public int getStart() {
        return m_start;
    }
    
    public int getStop() {
        return m_stop;
    }
    
    public int getBase() {
        return m_base;
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

        panel.add(new JLabel(m_excitation.getFileName()));

        // rows, cols, initX, initY, xPad, yPad
        //SpringUtilities.makeCompactGrid(panel, 1, 2, 4, 4, 4, 4);

        return panel;
    }

    /*
     * Creates a panel with excitation parameters.
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(0, 0, 8, 8));
        panel.setLayout(new SpringLayout());

        JLabel startLabel = new JLabel("Start");
        startLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(startLabel);
        m_startField = new JTextField(9);
        panel.add(m_startField);

        JLabel stopLabel = new JLabel("Stop");
        stopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(stopLabel);
        m_stopField = new JTextField(9);
        panel.add(m_stopField);

        JLabel baseLabel = new JLabel("Base");
        baseLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(baseLabel);
        m_baseField = new JTextField(9);
        panel.add(m_baseField);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(panel, 3, 2, 4, 4, 4, 4);

        JPanel enclosingPanel = new JPanel();
        enclosingPanel.add(panel);

        return enclosingPanel;
    }
}
