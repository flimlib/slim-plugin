/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

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
public class UIPanel extends JPanel {
    IUIPanelListener _listener;
    JCheckBox m_autoCheckBox;
    JTextField m_startTextField;
    JTextField m_stopTextField;
    boolean m_auto;
    double m_start;
    double m_stop;
    double m_min;
    double m_max;

    /**
     * Constructor.  Passed in an initial state and a state change //TODO this doc. et. al. outdated
     * listener.
     *
     * @param auto
     * @param start
     * @param stop
     */
    UIPanel() {
        super();
        //m_listener = listener;

        m_auto = true;
        m_start = m_stop = m_min = m_max = 0.0; //TODO gotta be a better way

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel readOutPanel = new JPanel();
        readOutPanel.setLayout(new BoxLayout(readOutPanel, BoxLayout.X_AXIS));

        m_startTextField = new JTextField();
        m_startTextField.setText("" + m_start);
        m_startTextField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    m_start = Double.parseDouble(m_startTextField.getText());
                //    m_listener.setRange(m_auto, m_start, m_stop, m_min, m_max);
                }
            }
        );
        readOutPanel.add(m_startTextField);

        m_stopTextField = new JTextField();
        m_startTextField.setText("" + m_stop);
        m_stopTextField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    m_stop = Double.parseDouble(m_stopTextField.getText());
                 //   m_listener.setRange(m_auto, m_start, m_stop, m_min, m_max);
                }
            }
        );
        readOutPanel.add(m_stopTextField);
        add(readOutPanel);
 
        m_autoCheckBox = new JCheckBox("Auto", m_auto);
        m_autoCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    m_auto = m_autoCheckBox.isSelected();
                    if (m_auto) {
                        m_start = m_min;
                        m_startTextField.setText("" + m_start);

                        m_stop = m_max;
                        m_stopTextField.setText("" + m_stop);
                    }
                    enableAppropriately();
                 //   m_listener.setRange(m_auto, m_start, m_stop, m_min, m_max);
                }
            }
        );
        add(m_autoCheckBox);


        enableAppropriately();
    }

    public void setListener(IUIPanelListener listener) {
        _listener = listener;
    }

    public void setAuto(boolean auto) {

    }

    public void setMinMaxLUT(double min, double max) {
        System.out.println("SetMinMaxLUT " + min + " " + max);
        m_startTextField.setText("" + min);
        m_stopTextField.setText("" + max);
    }

    // not
    /**
     * IColorizeRangeListener method.  Gets external changes to settings.
     *
     * @param auto
     * @param start
     * @param stop
     * @param min
     * @param max
     */
    public void setRange(boolean auto, double start, double stop, double min, double max) {
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
        m_min = min;
        m_max = max;
    }

    /**
     * Enable/disable start/stop text fields.
     */
    private void enableAppropriately() {
        m_startTextField.setEnabled(!m_auto);
        m_stopTextField.setEnabled(!m_auto);
    }
}
