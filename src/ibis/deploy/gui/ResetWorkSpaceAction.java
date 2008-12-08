package ibis.deploy.gui;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ResetWorkSpaceAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -7423012971383325290L;

    private JFrame frame;

    private GUI gui;

    public ResetWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        try {
            gui.setApplicationSet(new ApplicationSet());
            gui.fireApplicationSetUpdated();
            gui.setGrid(new Grid());
            gui.fireGridUpdated();
            gui.setExperiment(new Experiment("experiment"));
            gui.fireExperimentUpdated();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(),
                    "Reset experiment workspace failed",
                    JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace(System.err);
        }

    }

}
