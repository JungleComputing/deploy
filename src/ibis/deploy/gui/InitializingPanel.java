package ibis.deploy.gui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;

public class InitializingPanel implements MetricListener {

    /**
     * 
     */
    private static final long serialVersionUID = 5370845817270887558L;

    private ExperimentsPanel experimentsPanel;

    private InitializedPanel initializedPanel;

    private final JProgressBar progressBar = new JProgressBar();

    private JDialog dialog;

    public InitializingPanel(ExperimentsPanel experimentsPanel,
            InitializedPanel initializedPanel) {
        this.experimentsPanel = experimentsPanel;
        this.initializedPanel = initializedPanel;
        progressBar.setString("INITIAL");
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
    }

    public void processMetricEvent(MetricEvent event) {
        progressBar.setString("" + event.getValue());
        if (event.getValue() == Job.JobState.RUNNING) {
            experimentsPanel.removeAll();
            initializedPanel.init();
            experimentsPanel.add(initializedPanel);
            experimentsPanel.getRootPane().repaint();
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
            if (dialog != null) {
                dialog.dispose();
            }
        }
    }

    public void init() {
        JPanel panel = new JPanel(new BorderLayout());
        panel
                .add(new JLabel("Starting the Ibis Server..."),
                        BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.SOUTH);
        dialog = new JDialog(
                SwingUtilities.getWindowAncestor(experimentsPanel),
                "Ibis Server Status");
        dialog.setLocationRelativeTo(experimentsPanel);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setVisible(true);
    }

}
