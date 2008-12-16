package ibis.deploy.gui;

import ibis.deploy.gui.action.StopExistingJobAction;
import ibis.deploy.gui.action.SubmitExistingJobAction;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

public class JobTableControlPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -8138221871070915449L;

    public JobTableControlPanel(final GUI gui, final JTable table) {
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

        add(runAllButton);
        add(runSelectedButton);
        add(stopAllButton);
        add(stopSelectedButton);

    }
}
