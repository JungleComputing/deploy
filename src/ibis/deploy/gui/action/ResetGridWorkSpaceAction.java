package ibis.deploy.gui.action;

import ibis.deploy.Grid;
import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ResetGridWorkSpaceAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 0L;

    private JFrame frame;

    private GUI gui;

    public ResetGridWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        try {
            gui.setGrid(new Grid());
            gui.fireGridUpdated();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(),
                    "Reset grid workspace failed", JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace(System.err);
        }

    }

}
