package ibis.deploy.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ExperimentEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -8580838957929000835L;

    private final static int SPACER = 5;

    public ExperimentEditorPanel(GUI gui, MyTableModel model) {
        setBorder(BorderFactory.createTitledBorder("Experiment Editor"));
        setLayout(new BorderLayout(5, 5));
        WorldMapPanel worldMapPanel = new WorldMapPanel(gui);
        add(worldMapPanel, BorderLayout.CENTER);
        add(new ExperimentEditorTextPanel(gui, model, worldMapPanel),
                BorderLayout.EAST);

    }

}
