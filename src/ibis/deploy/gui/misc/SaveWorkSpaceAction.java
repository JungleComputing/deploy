package ibis.deploy.gui.misc;

import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SaveWorkSpaceAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JFrame frame;

    private GUI gui;

    public SaveWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        int choice = JOptionPane.showConfirmDialog(frame,
                "Save workspace to \"" + gui.getWorkspaceLocation() + "\"?",
                "Save Workspace?", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                gui.saveWorkspace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        "Saving workspace to \"" + gui.getWorkspaceLocation()
                                + "\" failed", JOptionPane.PLAIN_MESSAGE);
                e.printStackTrace(System.err);
            }

        }

    }

}
