package ibis.deploy.gui.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class AboutPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AboutPanel() {

        JEditorPane text = new JEditorPane();

        text.setEditable(false);
        text.setContentType("text/html");
        text.setText("<H1>About Ibis-Deploy</H1><h2>By the Ibis team</h2><h2><a href=http://www.cs.vu.nl/ibis>http://www.cs.vu.nl/ibis</a></h2>");
        text.setOpaque(false);
        
        text.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        java.awt.Desktop.getDesktop().browse(
                                event.getURL().toURI());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(AboutPanel.this,
                                e.getMessage(),
                                "Failed to open " + event.getURL(),
                                JOptionPane.PLAIN_MESSAGE);
                        e.printStackTrace(System.err);
                    }
                }
            }
        });

        add(text);

        JButton button = Utils.createImageButton("images/ibis-logo.png",
                "www.cs.vu.nl/ibis", null);
        button.setBorderPainted(false);
        add(button);

        ActionListener urlListener = new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            new URI("http://www.cs.vu.nl/ibis"));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(AboutPanel.this,
                            e.getMessage(),
                            "Failed to open http://www.cs.vu.nl/ibis",
                            JOptionPane.PLAIN_MESSAGE);
                    e.printStackTrace(System.err);
                } catch (URISyntaxException e) {
                    // ignore
                }

            }

        };

        button.addActionListener(urlListener);

    }
}
