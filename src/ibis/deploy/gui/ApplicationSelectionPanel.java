package ibis.deploy.gui;

import ibis.deploy.Application;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ApplicationSelectionPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 9143140469559257733L;

    public ApplicationSelectionPanel(final GUI gui) {
        final JComboBox applicationComboBox = new JComboBox();
        setLayout(new BorderLayout());

        for (Application application : gui.getApplicationSet()
                .getApplications()) {
            applicationComboBox.addItem(application);
        }
        applicationComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                gui.getCurrentJobDescription().setApplicationName(
                        applicationComboBox.getSelectedItem().toString());

            }

        });
        if (gui.getCurrentJobDescription().getApplicationName() == null) {
            gui.getCurrentJobDescription().setApplicationName(
                    applicationComboBox.getSelectedItem().toString());
        }
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(new JLabel("Select Application"), BorderLayout.WEST);
        titlePanel.add(GUIUtils.createImageLabel(
                "images/applications-system.png", "process count"),
                BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.add(applicationComboBox, BorderLayout.CENTER);
        selectPanel.add(new ProcessCountPanel(gui), BorderLayout.EAST);

        add(selectPanel, BorderLayout.SOUTH);

    }

}