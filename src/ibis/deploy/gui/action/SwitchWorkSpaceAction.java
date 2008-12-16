package ibis.deploy.gui.action;

import ibis.deploy.Workspace;
import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SwitchWorkSpaceAction extends AbstractAction {

    private JFrame frame;

    private GUI gui;

    public SwitchWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                Workspace wp = new Workspace(chooser.getSelectedFile());
                gui.setGrid(wp.getGrid());
                gui.fireGridUpdated();
                gui.setApplicationSet(wp.getApplications());
                gui.fireApplicationSetUpdated();
                gui.setExperiment(wp.getExperiment());
                gui.fireExperimentUpdated();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        "Switching entire workspace failed",
                        JOptionPane.PLAIN_MESSAGE);
                e.printStackTrace(System.err);
            }

        }

    }

}
