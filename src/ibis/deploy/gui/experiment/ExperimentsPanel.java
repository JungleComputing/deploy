package ibis.deploy.gui.experiment;

import ibis.deploy.State;
import ibis.deploy.StateListener;
import ibis.deploy.cli.PoolSizePrinter;
import ibis.deploy.gui.GUI;
import ibis.deploy.gui.experiment.composer.ExperimentEditorPanel;
import ibis.deploy.gui.experiment.jobs.JobTableModel;
import ibis.deploy.gui.experiment.jobs.JobTablePanel;
import ibis.deploy.gui.worldmap.WorldMapPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

public class ExperimentsPanel extends JPanel implements StateListener,
        Runnable, ActionListener {

    private static final long serialVersionUID = -5264882651577509288L;

    private final GUI gui;

    private final JButton initButton;
    private final WorldMapPanel worldMap;
    private final JProgressBar progressBar;

    public ExperimentsPanel(GUI gui) {
        setLayout(new BorderLayout());

        this.gui = gui;

        // initial content: world map, init button
        setLayout(new BorderLayout());
        worldMap = new WorldMapPanel(gui, 15);
        add(worldMap, BorderLayout.CENTER);

        initButton = new JButton("init");
        initButton.addActionListener(this);
        add(initButton, BorderLayout.SOUTH);

        // used in "initizing status" dialog
        progressBar = new JProgressBar();

    }

    /**
     * Create final content of experiments tab. Top left: experiment editor with
     * world map. Top right: Smartsockets. Bottom: Job Table.
     */
    private void createContent() {
        removeAll();

        JobTableModel jobTableModel = new JobTableModel();

        SmartSocketsVizPanel smartSockets = new SmartSocketsVizPanel(gui,
                jobTableModel);

        ExperimentEditorPanel editor = new ExperimentEditorPanel(gui,
                jobTableModel);

        JobTablePanel jobTable = new JobTablePanel(gui, jobTableModel);

        // pane containing experiment editor to the left
        // and smartsockets visualizer to the right
        JSplitPane horizontalSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, editor, smartSockets);
        horizontalSplitPane.setOneTouchExpandable(true);
        horizontalSplitPane
                .setDividerLocation((int) (GUI.DEFAULT_SCREEN_WIDTH * 0.585));
        // resize left and right evenly
        horizontalSplitPane.setResizeWeight(0.5);

        // pane containing editor/smartsockets on top
        // and job table at the bottom
        JSplitPane verticalSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, horizontalSplitPane, jobTable);
        verticalSplitPane.setOneTouchExpandable(true);
        verticalSplitPane
                .setDividerLocation((int) (GUI.DEFAULT_SCREEN_HEIGHT * 0.4));

        // resize top and bottom evenly
        verticalSplitPane.setResizeWeight(0.5);

        add(verticalSplitPane, BorderLayout.CENTER);

        getRootPane().repaint();
    }

    public void stateUpdated(State state, Exception exception) {
        progressBar.setString("" + state);
    }

    // init button pressed, fork of thread to initialize Ibis-Deploy lib
    public void actionPerformed(ActionEvent event) {
        initButton.setEnabled(false);

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("Deploy Initialization Thread");
        thread.start();
    }

    // Initialize Ibis Deploy lib, update content of panel when done
    public void run() {
        JDialog dialog = null;
        try {
            // create status dialog
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Starting the Ibis Server..."),
                    BorderLayout.NORTH);
            panel.add(progressBar, BorderLayout.SOUTH);
            dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "Initializing");
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // initialize, wait until finished
            gui.getDeploy().initialize(worldMap.getSelectedCluster(), this,
                    true);

            // print pool size statistics
            new PoolSizePrinter(gui.getDeploy());

            createContent();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(getRootPane(), e.getMessage(),
                    "Initialize failed", JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace(System.err);
        }
        if (dialog != null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            dialog.dispose();
        }
    }
}
