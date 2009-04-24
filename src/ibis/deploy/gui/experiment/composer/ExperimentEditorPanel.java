package ibis.deploy.gui.experiment.composer;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.experiment.jobs.JobTableModel;
import ibis.deploy.gui.worldmap.WorldMapPanel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ExperimentEditorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -8580838957929000835L;

    private final static int SPACER = 5;

    public ExperimentEditorPanel(GUI gui, JobTableModel model) {
        setBorder(BorderFactory.createTitledBorder("Experiment Editor"));
        setLayout(new BorderLayout(SPACER, SPACER));
        WorldMapPanel worldMapPanel = new WorldMapPanel(gui, 16, false);
        add(worldMapPanel, BorderLayout.CENTER);
        add(new ExperimentEditorTextPanel(gui, model, worldMapPanel),
                BorderLayout.EAST);

    }

}
