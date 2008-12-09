package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AdvancedSelectionPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 5003588493742875299L;

    public AdvancedSelectionPanel(final GUI gui) {
        setLayout(new BorderLayout(5, 5));
        add(new JLabel("Advanced settings"), BorderLayout.NORTH);
        JButton overwritesButton = GUIUtils.createImageButton(
                "/images/preferences-system.png",
                "set the overwrite values and/or ibis parameters for this job",
                "Advanced ...");
        overwritesButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                JDialog dialog = new JDialog(SwingUtilities
                        .getWindowAncestor(AdvancedSelectionPanel.this),
                        "Advanced ...");
                dialog.setLocationRelativeTo(AdvancedSelectionPanel.this);
                dialog.setContentPane(new AdvancedSettingsPanel(gui));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        add(overwritesButton, BorderLayout.SOUTH);
    }

}
