package ibis.deploy.gui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TabTitlePanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -396708441003040950L;

    public TabTitlePanel(String name, ImageIcon icon) {
        setOpaque(false);
        add(new JLabel(icon));
        add(new JLabel(name));
    }

}
