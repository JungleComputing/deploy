package ibis.deploy.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ClosableTabTitlePanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 98527178098563677L;

    private final JLabel nameLabel = new JLabel();

    public ClosableTabTitlePanel(String name, final JTabbedPane applicationTabs) {
        setOpaque(false);
        setName(name);
        nameLabel.setText(name);
        add(nameLabel);
        JLabel closeButton = GUIUtils.createImageLabel(
                "images/emblem-unreadable.png", "close tab");
        closeButton.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent arg0) {
                int i = applicationTabs.indexOfTabComponent(ClosableTabTitlePanel.this);
                if (i != -1) {
                    applicationTabs.remove(i);
                }
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
            }

        });
        add(closeButton);
    }

    public void setText(String name) {
        setName(name);
        nameLabel.setText(name);
        nameLabel.repaint();
    }

}
