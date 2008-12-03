package ibis.deploy.gui;

import ibis.deploy.Experiment;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class SwitchExperimentWorkSpaceAction extends AbstractAction {

    private JFrame frame;

    private GUI gui;

    public SwitchExperimentWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setFileFilter(new FileFilter() {

            public boolean accept(File file) {
                if (file.isFile() && (!file.getName().endsWith(".experiment"))) {
                    return false;
                }
                return true;
            }

            public String getDescription() {
                return ".experiment - experiment files";
            }
        });
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                gui.setExperiment(new Experiment(chooser.getSelectedFile()));
                gui.fireExperimentUpdated();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        "Switching experiment workspace failed",
                        JOptionPane.PLAIN_MESSAGE);
                e.printStackTrace(System.err);
            }

        }

    }

}
