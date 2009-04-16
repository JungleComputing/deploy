package ibis.deploy.gui.misc;

import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class NewWorkSpaceAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -7423012971383325290L;

    private JFrame frame;

    private GUI gui;

    public NewWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        try {
            gui.clearWorkspace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(),
                    "Reset experiment workspace failed",
                    JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace(System.err);
        }

    }

}
