package ibis.deploy.gui.experiment;

import ibis.deploy.State;
import ibis.deploy.StateListener;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class InitializingDialog implements StateListener {

    /**
     * 
     */
    private static final long serialVersionUID = 5370845817270887558L;

    private ExperimentsPanel experimentsPanel;

    private InitializedPanel initializedPanel;

    private final JProgressBar progressBar = new JProgressBar();

    private JDialog dialog;

    public InitializingDialog(ExperimentsPanel experimentsPanel,
            InitializedPanel initializedPanel) {
        this.experimentsPanel = experimentsPanel;
        this.initializedPanel = initializedPanel;
        progressBar.setString("" + State.INITIALIZING);
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
    }

    public void init() {
        JPanel panel = new JPanel(new BorderLayout());
        panel
                .add(new JLabel("Starting the Ibis Server..."),
                        BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.SOUTH);
        dialog = new JDialog(
                SwingUtilities.getWindowAncestor(experimentsPanel),
                "Initializing");
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(experimentsPanel);
        dialog.setVisible(true);
    }
    
    public void stateUpdated(State state, Exception exception) {
        progressBar.setString("" + state);
        if (state == State.DEPLOYED) {
            experimentsPanel.removeAll();
            initializedPanel.init();
            experimentsPanel.add(initializedPanel);
            experimentsPanel.getRootPane().repaint();
            // show the user we're now deployed
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            if (dialog != null) {
                dialog.dispose();
            }
        }
    }

}
