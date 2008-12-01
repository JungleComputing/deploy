package ibis.deploy.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutPanel extends JPanel {

    public AboutPanel() {
        add(new JLabel("Ibis Deploy"));
        add(GUIUtils.createImageLabel("images/ibis-logo-left.png",
                "www.cs.vu.nl/ibis"));
        add(GUIUtils.createImageLabel("images/ibis-logo-right.png",
                "www.cs.vu.nl"));
        add(GUIUtils.createImageLabel("images/JavaGAT.png",
                "www.cs.vu.nl/javagat"));
    }

}
