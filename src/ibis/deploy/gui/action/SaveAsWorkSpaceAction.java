package ibis.deploy.gui.action;

import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SaveAsWorkSpaceAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JFrame frame;

    private GUI gui;

    public SaveAsWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                if (chooser.getSelectedFile() == null) {
                    throw new Exception("Please enter or select a directory!");
                }
                    
                gui.saveWorkspace(chooser.getSelectedFile());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        "Saving workspace to \"" + chooser.getSelectedFile() + "\" failed",
                        JOptionPane.PLAIN_MESSAGE);
                e.printStackTrace(System.err);
            }

        }

    }

}
