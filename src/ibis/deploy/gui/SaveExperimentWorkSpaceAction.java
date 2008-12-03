package ibis.deploy.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SaveExperimentWorkSpaceAction extends AbstractAction {

    private JFrame frame;

    private GUI gui;

    public SaveExperimentWorkSpaceAction(String label, JFrame frame, GUI gui) {
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
                        if (!fileName.endsWith(".experiment")) {
                            fileName = fileName + ".experiment";
                        }
                        gui.getExperiment().save(new File(fileName));
                    }
                } else {
                    throw new Exception("Please enter or select a file name!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        "Saving experiment workspace failed",
                        JOptionPane.PLAIN_MESSAGE);
                e.printStackTrace(System.err);
            }

        }

    }

}