package ibis.deploy.gui.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class AboutPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AboutPanel() {
        JButton button = Utils.createImageButton("/images/ibis-logo-left.png",
                "www.cs.vu.nl/ibis", null);
        add(button);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            new URI("http://www.cs.vu.nl/ibis"));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(AboutPanel.this, e
                            .getMessage(),
                            "Failed to open http://www.cs.vu.nl/ibis",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                } catch (URISyntaxException e) {
                    // ignore
                }

            }

        });
        button = Utils.createImageButton("/images/ibis-logo-middle.png",
                "www.cs.vu.nl/ibis", null);
        add(button);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            new URI("http://www.cs.vu.nl/ibis/contact.html"));
                } catch (IOException e) {
                    JOptionPane
                            .showMessageDialog(
                                    AboutPanel.this,
                                    e.getMessage(),
                                    "Failed to open http://www.cs.vu.nl/ibis/contact.html",
                                    JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                } catch (URISyntaxException e) {
                    // ignore
                }
            }
        });

        button = Utils.createImageButton("/images/ibis-logo-right.png",
                "www.cs.vu.nl", null);
        add(button);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            new URI("http://www.cs.vu.nl/"));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(AboutPanel.this, e
                            .getMessage(),
                            "Failed to open http://www.cs.vu.nl/",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                } catch (URISyntaxException e) {
                    // ignore
                }

            }

        });
        add(button);
    }
}
