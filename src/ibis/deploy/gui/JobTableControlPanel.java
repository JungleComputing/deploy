package ibis.deploy.gui;

import ibis.deploy.gui.action.RemoveExistingJobAction;
import ibis.deploy.gui.action.StopExistingJobAction;
import ibis.deploy.gui.action.SubmitExistingJobAction;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

public class JobTableControlPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -8138221871070915449L;

    public JobTableControlPanel(final GUI gui, final JTable table) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
        JButton runAllButton = GUIUtils.createImageButton(
                new SubmitExistingJobAction(0, true, false, table, gui,
                        getRootPane()), "/images/go-next.png",
                "Runs all jobs of this expirement", "Start All");
        JButton runSelectedButton = GUIUtils.createImageButton(
                new SubmitExistingJobAction(0, true, true, table, gui,
                        getRootPane()), "/images/go-next.png",
                "Runs selected jobs of this expirement", "Start Selected");
        JButton stopAllButton = GUIUtils.createImageButton(
                new StopExistingJobAction(0, true, false, table, gui,
                        getRootPane()), "/images/process-stop.png",
                "Stops all jobs of this expirement", "Stop All");
        JButton stopSelectedButton = GUIUtils.createImageButton(
                new StopExistingJobAction(0, true, true, table, gui,
                        getRootPane()), "/images/process-stop.png",
                "Stops selected jobs of this expirement", "Stop Selected");
        JButton removeAllButton = GUIUtils.createImageButton(
                new RemoveExistingJobAction(0, true, false, table, gui,
                        getRootPane()), "/images/list-remove.png",
                "Remove all jobs from this experiment", "Remove All");
        JButton removeSelectedButton = GUIUtils.createImageButton(
                new RemoveExistingJobAction(0, true, true, table, gui,
                        getRootPane()), "/images/list-remove.png",
                "Remove selected jobs from this experiment", "Remove Selected");
        JPanel runPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        runPanel.add(runAllButton);
        runPanel.add(runSelectedButton);
        add(runPanel);

        JPanel stopPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        stopPanel.add(stopAllButton);
        stopPanel.add(stopSelectedButton);
        add(stopPanel);

        JPanel removePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        removePanel.add(removeAllButton);
        removePanel.add(removeSelectedButton);
        add(removePanel);
    }
}
