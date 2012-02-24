package ibis.deploy.gui.experiment.composer;

import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ProcessCountPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1677946251801621514L;

    public ProcessCountPanel(final GUI gui) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(new JLabel("x"));
        final JSpinner processCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

        gui.addSubmitJobListener(new SubmitJobListener() {

            public void modify(JobDescription jobDescription) {
                jobDescription.setProcessCount(((SpinnerNumberModel) processCountSpinner.getModel()).getNumber()
                        .intValue());
            }

        });
        processCountSpinner.setPreferredSize(new Dimension(50, 25));
        add(processCountSpinner);

    }
}
