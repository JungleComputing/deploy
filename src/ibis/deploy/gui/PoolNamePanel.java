package ibis.deploy.gui;

import ibis.deploy.JobDescription;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PoolNamePanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -7500688939534010077L;

    public PoolNamePanel(GUI gui) {
        setLayout(new BorderLayout());
        add(new JLabel("Pool Name: "), BorderLayout.WEST);
        final JTextField textField = new JTextField(gui.getExperiment()
                .getName());
        gui.addExperimentWorkSpaceListener(new WorkSpaceChangedListener() {

            public void workSpaceChanged(GUI gui) {
                textField.setText(gui.getExperiment().getDefaults()
                        .getPoolName());
            }

        });
        gui.addSubmitJobListener(new SubmitJobListener() {

            public void modify(JobDescription jobDescription) {
                jobDescription
                        .setPoolName(textField.getText() == null ? "default"
                                : textField.getText());
            }

        });

        add(textField, BorderLayout.CENTER);
    }

}
