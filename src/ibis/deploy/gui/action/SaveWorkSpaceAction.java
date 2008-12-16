package ibis.deploy.gui.action;

import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SaveWorkSpaceAction extends AbstractAction {

    private JFrame frame;

    private GUI gui;

    public SaveWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                if (chooser.getSelectedFile() != null) {
                    String fileName = chooser.getSelectedFile().getName();
                    if (fileName != null) {
                        if (!fileName.endsWith(".workspace")) {
                            fileName = fileName + ".workspace";
                        }
                        gui.getWorkSpace().save(new File(fileName));
                    }
                } else {
                    throw new Exception("Please enter or select a file name!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        "Saving entire workspace failed",
                        JOptionPane.PLAIN_MESSAGE);
                e.printStackTrace(System.err);
            }

        }

    }

}
