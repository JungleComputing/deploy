package ibis.deploy.gui;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Cluster;
import ibis.deploy.Deploy;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;
import ibis.deploy.JobDescription;
import ibis.deploy.Workspace;
import ibis.deploy.Deploy.HubPolicy;
import ibis.deploy.gui.experiment.composer.SubmitJobListener;
import ibis.deploy.gui.misc.AboutAction;
import ibis.deploy.gui.misc.HubPolicyAction;
import ibis.deploy.gui.misc.NewWorkSpaceAction;
import ibis.deploy.gui.misc.OpenWorkSpaceAction;
import ibis.deploy.gui.misc.SaveAsWorkSpaceAction;
import ibis.deploy.gui.misc.SaveWorkSpaceAction;
import ibis.deploy.gui.misc.SmartSocketsVizAction;
import ibis.deploy.gui.misc.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GUI {

    public static final int DEFAULT_SCREEN_WIDTH = 1024;

    public static final int DEFAULT_SCREEN_HEIGHT = 768;

    private static final Logger logger = LoggerFactory.getLogger(GUI.class);

    private Deploy deploy;

    private List<WorkSpaceChangedListener> gridListeners = new ArrayList<WorkSpaceChangedListener>();

    private List<WorkSpaceChangedListener> applicationSetListeners = new ArrayList<WorkSpaceChangedListener>();

    private List<WorkSpaceChangedListener> experimentListeners = new ArrayList<WorkSpaceChangedListener>();

    private List<SubmitJobListener> submitJobListeners = new ArrayList<SubmitJobListener>();

    private Workspace workspace;

    private File workspaceLocation;

    private JFrame frame = null;

    private JMenuBar menuBar = null;

    // private Boolean sharedHubs;

    private static class Shutdown extends Thread {
        private final GUI gui;

        Shutdown(GUI gui) {
            this.gui = gui;
        }

        public void run() {
            gui.getDeploy().end();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        final GUI gui = new GUI(args);
        Runtime.getRuntime().addShutdownHook(new Shutdown(gui));

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.createAndShowGUI();
            }
        });

    }

    private void saveAndClose() {
        File location = getWorkspaceLocation();

        int choice = JOptionPane.showConfirmDialog(frame,
                "Exiting ibis-deploy. Save workspace to \"" + location + "\"?",
                "Save Workspace?", JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                saveWorkspace();
            } catch (Exception e) {
                logger.error("Could not save workspace to " + location, e);
            }
            frame.dispose();
            System.exit(0);
        } else if (choice == JOptionPane.NO_OPTION) {
            frame.dispose();
            System.exit(0);
        } else {
            // cancel, do nothing :)
        }
    }

    private void createAndShowGUI() {
        JMenuItem menuItem;

        UIManager.put("swing.boldMetal", Boolean.FALSE);
        frame = new JFrame("Ibis Deploy - " + workspaceLocation.getName());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setIconImage(Utils.createImageIcon("/images/favicon.ico", null)
                .getImage());

        // center on screen
        frame.setLocationRelativeTo(null);

        this.menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");

        menu.add(new NewWorkSpaceAction("New Workspace", frame, this));
        menu.add(new OpenWorkSpaceAction("Open Workspace", frame, this));
        menu.addSeparator();
        menu.add(new SaveWorkSpaceAction("Save Workspace", frame, this));
        menu
                .add(new SaveAsWorkSpaceAction("Save Workspace As...", frame,
                        this));
        menu.addSeparator();
        menuItem = new JMenuItem("Exit");
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                saveAndClose();
            }
        });

        this.menuBar.add(menu);

        menu = new JMenu("View");
        menu.add(new SmartSocketsVizAction(frame, this));
        this.menuBar.add(menu);

        menu = new JMenu("Options");
        JMenu subMenu = new JMenu("Hub Policy");
        ButtonGroup hubPolicy = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem(new HubPolicyAction("No hubs",
                HubPolicy.OFF, this));
        hubPolicy.add(menuItem);
        subMenu.add(menuItem);
        menuItem = new JRadioButtonMenuItem(new HubPolicyAction(
                "One hub per cluster", HubPolicy.PER_CLUSTER, this));
        menuItem.setSelected(true);
        hubPolicy.add(menuItem);
        subMenu.add(menuItem);
        menuItem = new JRadioButtonMenuItem(new HubPolicyAction(
                "One hub per job", HubPolicy.PER_JOB, this));
        hubPolicy.add(menuItem);
        subMenu.add(menuItem);
        menu.add(subMenu);
        this.menuBar.add(menu);

        menu = new JMenu("Help");
        menu.add(new AboutAction(frame));

        this.menuBar.add(menu, -1);

        frame.setJMenuBar(this.menuBar);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new RootPanel(this), BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(DEFAULT_SCREEN_WIDTH,
                DEFAULT_SCREEN_HEIGHT));

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                saveAndClose();
            }
        });

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private static void printUsage() {
        System.err
                .println("Usage: ibis-deploy-gui [OPTIONS] [GRID_FILE] [APP_FILE] [EXPERIMENT_FILE] [WORKSPACE_FILE]");
        System.err.println("Options:");
        System.err.println("-v\t\tVerbose mode");
        System.err.println("-h | --help\tThis message");
    }

    protected GUI(String[] arguments) {
        File gridFile = null;
        File applicationsFile = null;
        File experimentFile = null;
        boolean verbose = false;
        boolean keepSandboxes = false;
        boolean zorilla = false;
        String serverCluster = null;

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("-v")) {
                verbose = true;
            } else if (arguments[i].equals("-h")
                    || arguments[i].equals("--help")) {
                printUsage();
                System.exit(0);
            } else if (arguments[i].equals("-k")) {
                keepSandboxes = true;
            } else if (arguments[i].equals("-s")) {
                i++;
                serverCluster = arguments[i];
            } else if (arguments[i].equals("-z")) {
                zorilla = true;
            } else if (arguments[i].startsWith("-")) {
                System.err.println("unknown option: " + arguments[i]);
                System.exit(1);
            } else if (arguments[i].endsWith(".grid")) {
                if (gridFile != null) {
                    System.err
                            .println("ERROR: can only specify a single grid file");
                    System.exit(1);
                }
                gridFile = new File(arguments[i]);
            } else if (arguments[i].endsWith(".applications")) {
                if (applicationsFile != null) {
                    System.err
                            .println("ERROR: can only specify a single applications file");
                    System.exit(1);
                }
                applicationsFile = new File(arguments[i]);
            } else if (arguments[i].endsWith(".experiment")) {
                if (experimentFile != null) {
                    System.err
                            .println("ERROR: can only specify a single experiment file");
                    System.exit(1);
                }
                experimentFile = (new File(arguments[i]));
            } else {
                if (workspaceLocation != null) {
                    System.err
                            .println("ERROR: can only specify a single workspace directory");
                    System.exit(1);
                }
                workspaceLocation = (new File(arguments[i]));
            }
        }

        try {

            // load workspace
            if (workspaceLocation == null) {
                // default workspace location
                workspaceLocation = Workspace.DEFAULT_LOCATION;
            }
            workspace = new Workspace(workspaceLocation);

            if (workspace.getExperiments().size() > 1) {
                logger
                        .warn("Multiple experiments in workspace, GUI only supports one, using: "
                                + workspace.getExperiments().get(0).getName()
                                + " as experiment");
            }

            // override grid
            if (gridFile != null) {
                if (!gridFile.isFile()) {
                    System.err.println("DEPLOY: Specified grid file: \""
                            + gridFile + "\" does not exist or is a directory");
                    System.exit(1);
                }

                Grid grid = new Grid(gridFile);
                workspace.setGrid(grid);
            }

            if (applicationsFile != null) {
                if (!experimentFile.isFile()) {
                    System.err
                            .println("DEPLOY: Specified applications file: \""
                                    + applicationsFile
                                    + "\" does not exist or is a directory");
                    System.exit(1);
                }

                ApplicationSet applications = new ApplicationSet(
                        applicationsFile);
                workspace.setApplications(applications);
            }

            // replace experiments in workspace with specified experiment, if
            // needed
            if (experimentFile != null) {
                if (!experimentFile.isFile()) {
                    System.err.println("DEPLOY: Specified experiment file: \""
                            + experimentFile
                            + "\" does not exist or is a directory");
                    System.exit(1);
                }
                Experiment experiment = new Experiment(experimentFile);
                workspace.getExperiments().set(0, experiment);
            }

            if (workspace.getExperiments().size() == 0) {
                workspace.addExperiment(new Experiment("default"));
            }

        } catch (Exception e) {
            System.err.println("Exception when loading setting files: " + e);
            System.exit(1);
        }

        if (verbose) {
            System.err.println("DEPLOY: Workspace:");
            System.err.println(workspace.toPrintString());
        }

        try {
            if (zorilla) {
                Grid grid = workspace.getGrid();
                
                if (grid == null) {
                    System.err.println("ERROR: Cannot initialize zorilla " +
                    		"mode, no grid file specified");
                    System.exit(1);
                }
                
                deploy = new Deploy(null, verbose, keepSandboxes, grid);
                System.err.println(grid.toPrintString());
            } else if (serverCluster == null) {
                logger.info("Initializing Ibis Deploy, using build-in server");

                // init with build-in server

                deploy = new Deploy(null, verbose, keepSandboxes, null, null,
                        true);
            } else {
                logger.info("Initializing Command Line Ibis Deploy"
                                + ", using server on cluster \""
                                + serverCluster + "\"");

                if (workspace.getGrid() == null) {
                    System.err.println("ERROR: Server cluster " + serverCluster
                            + " not found, no grid file specified");
                    System.exit(1);
                }

                Cluster cluster = workspace.getGrid().getCluster(serverCluster);

                if (cluster == null) {
                    System.err.println("ERROR: Server cluster " + serverCluster
                            + " not found in grid");
                    System.exit(1);
                }
                
                InitializationFrame initWindow = new InitializationFrame();
                deploy = new Deploy(null, verbose, keepSandboxes, cluster,
                        initWindow, true);
                // will call dispose in the Swing thread
                initWindow.remove();

            }

        } catch (Exception e) {
            System.err.println("Could not initialize ibis-deploy: " + e);
            e.printStackTrace(System.err);
            System.exit(1);
        }

    }

    public Deploy getDeploy() {
        return deploy;
    }

    public Grid getGrid() {
        return workspace.getGrid();
    }

    public ApplicationSet getApplicationSet() {
        return workspace.getApplications();
    }

    public Experiment getExperiment() {
        if (workspace.getExperiments().size() == 0) {
            try {
                workspace.addExperiment(new Experiment("default"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return workspace.getExperiments().get(0);
    }

    public Workspace getWorkSpace() {
        return workspace;
    }

    public File getWorkspaceLocation() {
        return workspaceLocation;
    }

    public void clearWorkspace() throws Exception {
        workspace = new Workspace();
        fireWorkSpaceUpdated();
    }

    public void loadWorkspace(File location) throws Exception {
        workspace = new Workspace(location);
        fireWorkSpaceUpdated();
    }

    public void saveWorkspace() throws Exception {
        workspace.save(workspaceLocation);
    }

    public void saveWorkspace(File newLocation) throws Exception {
        workspace.save(newLocation);
        // set location last, so it only gets set if the save succeeds
        workspaceLocation = newLocation;
        frame.setTitle("Ibis Deploy - " + workspaceLocation.getName());
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    // public Boolean getSharedHubs() {
    // return sharedHubs;
    // }
    //
    // public void setSharedHubs(boolean sharedHubs) {
    // this.sharedHubs = sharedHubs;
    // }

    public void fireSubmitJob(JobDescription jobDescription) {
        for (SubmitJobListener listener : submitJobListeners) {
            listener.modify(jobDescription);
        }
    }

    public void fireWorkSpaceUpdated() {
        fireGridUpdated();
        fireApplicationSetUpdated();
        fireExperimentUpdated();
    }

    public void fireGridUpdated() {
        for (WorkSpaceChangedListener listener : gridListeners) {
            listener.workSpaceChanged(this);
        }
    }

    public void fireApplicationSetUpdated() {
        for (WorkSpaceChangedListener listener : applicationSetListeners) {
            listener.workSpaceChanged(this);
        }
    }

    public void fireExperimentUpdated() {
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

    public JFrame getFrame() {
        return frame;
    }
}
