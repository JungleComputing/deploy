package ibis.deploy.gui;

import ibis.deploy.Application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AdvancedSettingsPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 5815368955496553106L;

    public AdvancedSettingsPanel(final GUI gui) {
        add(new JLabel("set hub policy: "));
        final JComboBox hubPolicyComboBox = new JComboBox();
        hubPolicyComboBox.addItem("single hub per cluster");
        hubPolicyComboBox.addItem("one hub per job");
        add(hubPolicyComboBox);
        hubPolicyComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                gui.setSharedHubs(hubPolicyComboBox.getSelectedIndex() == 0);
            }

        });

        final JButton closeButton = new JButton("close");
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.getWindowAncestor(AdvancedSettingsPanel.this)
                        .dispose();
            }
        });
        add(closeButton);
    }

}
