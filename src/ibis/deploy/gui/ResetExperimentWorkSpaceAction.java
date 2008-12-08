package ibis.deploy.gui;

import ibis.deploy.Experiment;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ResetExperimentWorkSpaceAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -526039939859716237L;

    private JFrame frame;

    private GUI gui;

    public ResetExperimentWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        try {
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
