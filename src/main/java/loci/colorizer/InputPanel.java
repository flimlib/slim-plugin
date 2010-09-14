//
// InputPanel.java
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

package loci.colorizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/colorizer/InputPanel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/colorizer/InputPanel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class InputPanel extends JPanel implements IColorizeRangeListener, ItemListener, ActionListener {
    JCheckBox m_autoCheckBox;
    JTextField m_startTextField;
    JTextField m_stopTextField;
    boolean m_auto;
    double m_start;
    double m_stop;
    double m_max;
    IColorizeRangeListener m_listener;

    /**
     * Constructor.  Passed in an initial state and a state change
     * listener.
     *
     * @param auto
     * @param start
     * @param stop
     */
    InputPanel(IColorizeRangeListener listener) {
        super();
        m_listener = listener;

        m_auto = true;
        m_start = m_stop = m_max = 0.0;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        m_autoCheckBox = new JCheckBox("Auto", m_auto);
        m_autoCheckBox.addItemListener(this);
        add(m_autoCheckBox);

        m_startTextField = new JTextField();
        m_startTextField.setText("" + m_start);
        m_startTextField.addActionListener(this);
        add(m_startTextField);

        m_stopTextField = new JTextField();
        m_startTextField.setText("" + m_stop);
        m_stopTextField.addActionListener(this);
        add(m_stopTextField);

        enableAppropriately();
    }

    public void setRange(boolean auto, double start, double stop, double max) {
        if (auto != m_auto) {
            m_auto = auto;
            m_autoCheckBox.setSelected(auto);
            enableAppropriately();
        }
        
        if (start != m_start) {
            m_start = start;
            m_startTextField.setText("" + start);
        }

        if (stop != m_stop) {
            m_stop = stop;
            m_stopTextField.setText("" + stop);
        }
        m_max = max;
    }

    private void enableAppropriately() {
        m_startTextField.enable(!m_auto);
        m_stopTextField.enable(!m_auto);
    }

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == m_autoCheckBox) {
            m_auto = m_autoCheckBox.isSelected();
            if (m_auto) {
                m_start = 0.0;
                m_startTextField.setText("" + m_start);

                m_stop = m_max;
                m_stopTextField.setText("" + m_stop);
            }
            enableAppropriately();
            m_listener.setRange(m_auto, m_start, m_stop, m_max);
        }
    }

    public void actionPerformed(ActionEvent e) {
        boolean changed = false;
        Object source = e.getSource();
        if (source == m_startTextField) {
            changed = true;
            m_start = Double.parseDouble(m_startTextField.getText());
        }
        else if (source == m_stopTextField) {
            changed = true;
            m_stop = Double.parseDouble(m_stopTextField.getText());
        }
        if (changed) {
            m_listener.setRange(m_auto, m_start, m_stop, m_max);
        }
    }
}
