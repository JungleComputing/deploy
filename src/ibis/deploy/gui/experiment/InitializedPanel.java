package ibis.deploy.gui.experiment;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.experiment.composer.ExperimentEditorPanel;
import ibis.deploy.gui.experiment.jobs.JobTableModel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class InitializedPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 2528896847051125675L;

    private GUI gui;

    public InitializedPanel(GUI gui) {
        setLayout(new BorderLayout());
        this.gui = gui;
    }

    public void init() {
        JobTableModel model = new JobTableModel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new ExperimentEditorPanel(gui, model), new MonitoringPanel(gui,
                        model));
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(320);
        add(splitPane, BorderLayout.CENTER);

    }
}
