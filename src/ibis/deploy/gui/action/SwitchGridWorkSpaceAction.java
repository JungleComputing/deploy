package ibis.deploy.gui.action;

import ibis.deploy.Grid;
import ibis.deploy.gui.GUI;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class SwitchGridWorkSpaceAction extends AbstractAction {

    private JFrame frame;

    private GUI gui;

    public SwitchGridWorkSpaceAction(String label, JFrame frame, GUI gui) {
        super(label);
        this.frame = frame;
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setFileFilter(new FileFilter() {

            public boolean accept(File file) {
                if (file.isFile() && (!file.getName().endsWith(".grid"))) {
                    return false;
                }
                return true;
            }

            public String getDescription() {
                return ".grid - grid files";
            }
        });
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                gui.setGrid(new Grid(chooser.getSelectedFile()));
                gui.fireGridUpdated();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        "Switching grid workspace failed",
                        JOptionPane.PLAIN_MESSAGE);
                e.printStackTrace(System.err);
            }

        }

    }

}
