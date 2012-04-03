package ibis.deploy.gui.experiment.composer;

import ibis.deploy.Application;
import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.WorkSpaceChangedListener;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;

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

        gui.addApplicationSetWorkSpaceListener(new WorkSpaceChangedListener() {

            public void workSpaceChanged(GUI gui) {
                applicationComboBox.removeAllItems();
                for (Application application : gui.getApplicationSet().getApplications()) {
                    applicationComboBox.addItem(application);
                }
                ApplicationSelectionPanel.this.repaint();
            }

        });

        gui.addSubmitJobListener(new SubmitJobListener() {

            public void modify(JobDescription jobDescription) throws Exception {
                jobDescription.getApplication().setName(applicationComboBox.getSelectedItem().toString());
            }

        });

        setLayout(new BorderLayout(5, 5));

        for (Application application : gui.getApplicationSet().getApplications()) {
            applicationComboBox.addItem(application);
        }
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(new JLabel("Application"), BorderLayout.WEST);

        add(titlePanel, BorderLayout.NORTH);

        JPanel selectPanel = new JPanel(new BorderLayout(5, 5));
        selectPanel.add(Utils.createImageLabel("images/applications-system.png", "process count"), BorderLayout.WEST);

        selectPanel.add(applicationComboBox, BorderLayout.CENTER);
        selectPanel.add(new ProcessCountPanel(gui), BorderLayout.EAST);

        add(selectPanel, BorderLayout.SOUTH);

    }

}
