package ibis.deploy.gui;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Deploy;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;
import ibis.deploy.JobDescription;
import ibis.deploy.Workspace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

public class GUI {

    private Deploy deploy;

    private List<WorkSpaceChangedListener> gridListeners = new ArrayList<WorkSpaceChangedListener>();

    private List<WorkSpaceChangedListener> applicationSetListeners = new ArrayList<WorkSpaceChangedListener>();

    private List<WorkSpaceChangedListener> experimentListeners = new ArrayList<WorkSpaceChangedListener>();

    private List<SubmitJobListener> submitJobListeners = new ArrayList<SubmitJobListener>();

    private Grid grid = null;

    private ApplicationSet applications = null;

    private Experiment experiment = null;

    private JMenuBar menuBar = null;

    private Boolean sharedHubs;

    private static final File DEFAULT_WORKSPACE = new File("default.workspace");

    /**
     * @param args
     */
    public static void main(String[] args) {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        final GUI gui = new GUI(args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    gui.getWorkSpace().save(DEFAULT_WORKSPACE);
                    gui.getDeploy().end();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        });

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(gui);
            }
        });

    }

    protected static void createAndShowGUI(final GUI gui) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        final JFrame frame = new JFrame("Ibis Deploy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(GUIUtils.createImageIcon(
                "images/ibis-logo-left.png", null).getImage());

        gui.menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem menuItem = new JMenuItem("Exit");
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });

        gui.menuBar.add(menu);
        menu = new JMenu("Workspaces");

        JMenu subMenu = new JMenu("Save as ...");
        subMenu.add(new SaveWorkSpaceAction("Entire Workspace", frame, gui));
        subMenu.add(new SaveGridWorkSpaceAction("Grid Workspace", frame, gui));
        subMenu.add(new SaveApplicationSetWorkSpaceAction(
                "Application Workspace", frame, gui));
        subMenu.add(new SaveExperimentWorkSpaceAction("Experiment Workspace",
                frame, gui));
        menu.add(subMenu);

        subMenu = new JMenu("Switch");
        subMenu.add(new SwitchWorkSpaceAction("Entire Workspace", frame, gui));
        subMenu
                .add(new SwitchGridWorkSpaceAction("Grid Workspace", frame, gui));
        subMenu.add(new SwitchApplicationSetWorkSpaceAction(
                "Application Workspace", frame, gui));
        subMenu.add(new SwitchExperimentWorkSpaceAction("Experiment Workspace",
                frame, gui));
        menu.add(subMenu);

        gui.menuBar.add(menu);

        menu = new JMenu("Help");
        menu.add(new AboutAction(frame));

        gui.menuBar.add(menu, -1);

        frame.setJMenuBar(gui.menuBar);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new RootPanel(gui), BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(900, 650));

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    protected GUI(String[] arguments) {
        File gridFile = null;
        File applicationsFile = null;
        File experimentFile = null;
        File workSpaceFile = null;
        boolean verbose = false;

        if (arguments.length == 0) {
            if (DEFAULT_WORKSPACE.exists()) {
                workSpaceFile = DEFAULT_WORKSPACE;
            }
        }

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("-g")) {
                i++;
                gridFile = new File(arguments[i]);
            } else if (arguments[i].equals("-a")) {
                i++;
                applicationsFile = new File(arguments[i]);
            } else if (arguments[i].equals("-v")) {
                verbose = true;
            } else if (arguments[i].equals("-w")) {
                i++;
                workSpaceFile = new File(arguments[i]);
            } else {
                experimentFile = new File(arguments[i]);
            }
        }

        if (workSpaceFile == null) {
            // do nothing, there might be separate grid, applications and
            // experiment files
        } else {
            if (!workSpaceFile.isFile()) {
                System.err
                        .println("DEPLOY: Specified workspace file: \""
                                + workSpaceFile
                                + "\" does not exist or is a directory");
                System.exit(1);
            }

            try {
                Workspace workSpace = new Workspace(workSpaceFile);
                grid = workSpace.getGrid();
                applications = workSpace.getApplications();
                experiment = workSpace.getExperiment();
            } catch (FileNotFoundException e) {
                System.err.println("Exception for file '" + workSpaceFile
                        + "': " + e);
            } catch (Exception e) {
                System.err.println("Exception for file '" + workSpaceFile
                        + "': " + e);
            }

            if (verbose) {
                System.err.println("Workspace:");
                System.err.println(grid.toPrintString());
                System.err.println(applications.toPrintString());
                System.err.println(experiment.toPrintString());
            }
        }

        if (gridFile == null) {
            if (grid == null) {
                grid = new Grid();
            }
        } else {
            if (!gridFile.isFile()) {
                System.err.println("DEPLOY: Specified grid file: \"" + gridFile
                        + "\" does not exist or is a directory");
                System.exit(1);
            }

            try {
                grid = new Grid(gridFile);
            } catch (FileNotFoundException e) {
                System.err.println("Exception for file '" + gridFile + "': "
                        + e);
            } catch (Exception e) {
                System.err.println("Exception for file '" + gridFile + "': "
                        + e);
            }

            if (verbose) {
                System.err.println("Grid:");
                System.err.println(grid.toPrintString());
            }
        }

        if (applicationsFile == null) {
            if (applications == null) {
                applications = new ApplicationSet();
            }
        } else {
            if (!applicationsFile.isFile()) {
                System.err.println("DEPLOY: Specified applications file: \""
                        + applicationsFile
                        + "\" does not exist or is a directory");
                System.exit(1);

            }

            try {
                applications = new ApplicationSet(applicationsFile);
            } catch (Exception e) {
                System.err.println("Exception for file '" + applicationsFile
                        + "': " + e);
            }

            if (verbose) {
                System.err.println("DEPLOY: Applications:");
                System.err.println(applications.toPrintString());
            }
        }

        if (experimentFile != null) {
            if (!experimentFile.isFile()) {
                System.err.println("DEPLOY: Specified applications file: \""
                        + experimentFile
                        + "\" does not exist or is a directory");
                System.exit(1);

            }

            try {
                experiment = new Experiment(experimentFile);
            } catch (FileNotFoundException e) {
                System.err.println("Exception for file '" + experimentFile
                        + "': " + e);
            } catch (IOException e) {
                System.err.println("Exception for file '" + experimentFile
                        + "': " + e);
            } catch (Exception e) {
                System.err.println("Exception for file '" + experimentFile
                        + "': " + e);
            }

            if (verbose) {
                System.err.println("DEPLOY: Experiment:");
                System.err.println(experiment.toPrintString());
            }
        } else if (experiment == null) {
            try {
                experiment = new Experiment("default");
            } catch (Exception e) {
                // ignore will always work!
                e.printStackTrace();
            }
        }

        try {
            deploy = new Deploy(null);
        } catch (Exception e) {
            System.err.println("Could not initialize ibis-deploy: " + e);
            System.exit(1);
        }

    }

    protected Deploy getDeploy() {
        return deploy;
    }

    protected Grid getGrid() {
        return grid;
    }

    protected void setGrid(Grid grid) {
        this.grid = grid;
    }

    protected ApplicationSet getApplicationSet() {
        return applications;
    }

    protected void setApplicationSet(ApplicationSet applications) {
        this.applications = applications;

    }

    protected Experiment getExperiment() {
        return experiment;
    }

    protected void setExperiment(Experiment experiment) {
        this.experiment = experiment;

    }

    protected Workspace getWorkSpace() {
        Workspace wp = new Workspace(getExperiment().getName());
        wp.setExperiment(getExperiment());
        wp.setApplications(getApplicationSet());
        wp.setGrid(getGrid());
        return wp;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public Boolean getSharedHubs() {
        return sharedHubs;
    }

    public void setSharedHubs(boolean sharedHubs) {
        this.sharedHubs = sharedHubs;
    }

    protected void fireSubmitJob(JobDescription jobDescription) {
        for (SubmitJobListener listener : submitJobListeners) {
            listener.modify(jobDescription);
        }
    }

    protected void fireWorkSpaceUpdated() {
        fireGridUpdated();
        fireApplicationSetUpdated();
        fireExperimentUpdated();
    }

    protected void fireGridUpdated() {
        for (WorkSpaceChangedListener listener : gridListeners) {
            listener.workSpaceChanged(this);
        }
    }

    protected void fireApplicationSetUpdated() {
        for (WorkSpaceChangedListener listener : applicationSetListeners) {
            listener.workSpaceChanged(this);
        }
    }

    protected void fireExperimentUpdated() {
        for (WorkSpaceChangedListener listener : experimentListeners) {
            listener.workSpaceChanged(this);
        }
    }

    public void addSubmitJobListener(SubmitJobListener listener) {
        submitJobListeners.add(listener);
    }

    public void addGridWorkSpaceListener(WorkSpaceChangedListener listener) {
        gridListeners.add(listener);
    }

    public void addApplicationSetWorkSpaceListener(
            WorkSpaceChangedListener listener) {
        applicationSetListeners.add(listener);
    }

    public void addExperimentWorkSpaceListener(WorkSpaceChangedListener listener) {
        experimentListeners.add(listener);
    }

}
