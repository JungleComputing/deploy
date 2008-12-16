package ibis.deploy.gui.action;

import ibis.deploy.ApplicationSet;
import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class ResetApplicationSetWorkSpaceAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 0L;

    private JFrame frame;

    private GUI gui;

    public ResetApplicationSetWorkSpaceAction(String label, JFrame frame,
            GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        try {
            gui.setApplicationSet(new ApplicationSet());
            gui.fireApplicationSetUpdated();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(),
                    "Reset application workspace failed",
                    JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace(System.err);
        }

    }

}
