package ibis.deploy.gui.experiment.composer;

import ibis.deploy.JobDescription;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class RuntimePanel extends JPanel {

    private static final long serialVersionUID = 1677946251801621514L;

    public RuntimePanel(final GUI gui) {
        setLayout(new BorderLayout(5, 0));
        add(Utils.createImageLabel("images/clock.png",
                "runtime"), BorderLayout.WEST);

        add(new JLabel("Runtime (minutes)"), BorderLayout.CENTER);
        final JSpinner processCountSpinner = new JSpinner(new SpinnerNumberModel(60, 1, Integer.MAX_VALUE, 1));

        gui.addSubmitJobListener(new SubmitJobListener() {
            public void modify(JobDescription jobDescription) {
                jobDescription.setRuntime(((SpinnerNumberModel) processCountSpinner.getModel()).getNumber().intValue());
            }

        });
        processCountSpinner.setPreferredSize(new Dimension(50, processCountSpinner.getPreferredSize().height));
        add(processCountSpinner, BorderLayout.EAST);

    }
}
