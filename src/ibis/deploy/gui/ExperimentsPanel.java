package ibis.deploy.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class ExperimentsPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -5264882651577509288L;

    public ExperimentsPanel(final GUI gui) {
        setLayout(new BorderLayout());
        add(new InitPanel(gui, new InitializingPanel(this,
                new InitializedPanel(gui))), BorderLayout.CENTER);
    }

}
