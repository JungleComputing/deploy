package ibis.deploy.gui.experiment;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.experiment.composer.ExperimentEditorPanel;
import ibis.deploy.gui.experiment.jobs.JobTableModel;
import ibis.deploy.gui.experiment.jobs.JobTablePanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ExperimentsPanel extends JPanel {

    private static final long serialVersionUID = -5264882651577509288L;

    public ExperimentsPanel(GUI gui) {
        setLayout(new BorderLayout());

        JobTableModel jobTableModel = new JobTableModel(gui);

        SmartSocketsVizPanel smartSockets = new SmartSocketsVizPanel(gui,
                jobTableModel);
        smartSockets.setSize(100, 100);

        ExperimentEditorPanel editor = new ExperimentEditorPanel(gui,
                jobTableModel);

        JobTablePanel jobTable = new JobTablePanel(gui, jobTableModel);

        // pane containing experiment editor to the left
        // and SmartSockets visualizer to the right
        JSplitPane horizontalSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, editor, smartSockets);
        horizontalSplitPane.setOneTouchExpandable(true);
        horizontalSplitPane
                .setDividerLocation((int) (GUI.DEFAULT_SCREEN_WIDTH * 0.65));
        // resize left and right evenly
        horizontalSplitPane.setResizeWeight(0.5);

        // pane containing editor/SmartSockets on top
        // and job table at the bottom
        JSplitPane verticalSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, horizontalSplitPane, jobTable);
        verticalSplitPane.setOneTouchExpandable(true);
        verticalSplitPane
                .setDividerLocation((int) (GUI.DEFAULT_SCREEN_HEIGHT * 0.4));

        // resize top and bottom evenly
        verticalSplitPane.setResizeWeight(0.5);

        add(verticalSplitPane, BorderLayout.CENTER);
        
    }

}
