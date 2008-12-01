package ibis.deploy.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class AboutAction extends AbstractAction {

    private JFrame frame;

    public AboutAction(JFrame frame) {
        super("About");
        this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
        JDialog dialog = new JDialog(frame, "About");
        dialog.setLocationRelativeTo(frame);
        dialog.setContentPane(new AboutPanel());
        dialog.pack();
        dialog.setVisible(true);
    }

}
