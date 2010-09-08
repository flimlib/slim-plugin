/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author aivar
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
