package ibis.deploy.gui.experiment;

import ibis.deploy.gui.GUI;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class ExperimentsPanel extends JPanel {

    private static final long serialVersionUID = -5264882651577509288L;

    /**
     * Actual content in {@link InitPanel} before pressing init, and
     * {@link InitializedPanel} after pressing init.
     * 
     * @param gui
     *            The GUI.
     */
    public ExperimentsPanel(final GUI gui) {
        setLayout(new BorderLayout());
        add(new InitPanel(gui, new InitializingDialog(this,
                new InitializedPanel(gui))), BorderLayout.CENTER);
    }

}
