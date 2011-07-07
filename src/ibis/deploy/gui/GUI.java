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
import ibis.deploy.gui.misc.Utils;
import ibis.deploy.gui.worldmap.MapUtilities;

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
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.simulator.FakeManagementService;
import ibis.deploy.monitoring.simulator.FakeRegistryService;
import ibis.deploy.monitoring.visualization.gridvision.JungleGoggles;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

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

    private Workspace workspace = null;

    private File workspaceLocation = Workspace.DEFAULT_LOCATION;

    private JFrame frame = null;

    private JMenuBar menuBar = null;

    private RootPanel myRoot;

    private final Mode mode;

    private Collector collector;
    
    public static boolean fakeData = false;

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

        /*
         * Turned off the invokeLater stuff, because it was interfering with
         * jogl. At first glance, this does not seem to have any impact on the
         * rest of deploy anyway, but if that is a wrong assessment, please
         * contact me. - Maarten
         */
        // javax.swing.SwingUtilities.invokeLater(new Runnable() {
        // public void run() {
        try {
            gui.createAndShowGUI();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        // }
        // });
    }

    private void close() {
        int choice = JOptionPane.showConfirmDialog(frame, "Really exit?",
                "Exiting Ibis-Deploy", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            frame.dispose();
            System.exit(0);
        } else {
            // no, do nothing :)
        }
    }

    private void saveAndClose() {
        File location = getWorkspaceLocation();

        JOptionPane options = new JOptionPane(
                "Exiting ibis-deploy. Save workspace to \"" + location + "\"?",
                JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION,
                null, new Object[] { "Yes", "No", "Cancel" }, "No");

        JDialog dialog = options.createDialog(frame, "Save Workspace?");

        dialog.setVisible(true);

        Object choice = options.getValue();

        if (choice != null && choice.equals("Yes")) {
            try {
                saveWorkspace();
            } catch (Exception e) {
                logger.error("Could not save workspace to " + location, e);
            }
            frame.dispose();
            System.exit(0);
        } else if (choice != null && choice.equals("No")) {
            frame.dispose();
            System.exit(0);
        } else {
            // cancel, do nothing :)
        }
    }

    private void createAndShowGUI(String... logos) throws Exception {
        JMenuItem menuItem;

        UIManager.put("swing.boldMetal", Boolean.FALSE);
        frame = new JFrame("Ibis Deploy - " + workspaceLocation.getName());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        ImageIcon icon = Utils.createImageIcon("images/favicon.ico", null);
        if (icon != null) {
            frame.setIconImage(icon.getImage());
        }

        this.menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");

        if (getMode() == Mode.MONITOR) {
            frame.setTitle("Ibis Deloy Monitoring");
        }

        if (isReadOnly()) {
            menuItem = new JMenuItem("Exit");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    close();
                }
            });
            menu.add(menuItem);
        } else {
            menu.add(new NewWorkSpaceAction("New Workspace", frame, this));
            menu.add(new OpenWorkSpaceAction("Open Workspace", frame, this));
            menu.addSeparator();
            menu.add(new SaveWorkSpaceAction("Save Workspace", frame, this));
            menu.add(new SaveAsWorkSpaceAction("Save Workspace As...", frame,
                    this));
            menu.addSeparator();
            menuItem = new JMenuItem("Exit");
            menu.add(menuItem);

            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    saveAndClose();
                }
            });
        }

        this.menuBar.add(menu);

        menu = new JMenu("Options");

        menu.add(MapUtilities.getMapMenu());

        if (!isReadOnly()) {
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
        }

        this.menuBar.add(menu);

        menu = new JMenu("Help");
        menu.add(new AboutAction(frame));

        this.menuBar.add(menu, -1);

        frame.setJMenuBar(this.menuBar);

        frame.getContentPane().setLayout(new BorderLayout());
        myRoot = new RootPanel(this, logos);
        frame.getContentPane().add(myRoot, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(DEFAULT_SCREEN_WIDTH,
                DEFAULT_SCREEN_HEIGHT));

        if (isReadOnly()) {
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    close();
                }
            });
        } else {
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    saveAndClose();
                }
            });

        }

        // Display the window.
        frame.pack();

        // center on screen
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);

    }

    private static void printUsage() {
        System.err
                .println("Usage: ibis-deploy-gui [OPTIONS] [GRID_FILE] [APP_FILE] [EXPERIMENT_FILE] [WORKSPACE_DIR]");
        System.err.println("Options:");
        System.err.println("-s CLUSTER\tRun server on specified cluster");
        System.err.println("-k\t\tKeep sandboxes");
        System.err.println("-r\t\tRead only mode");
        System.err.println("-v\t\tVerbose mode");
        System.err
                .println("-p PORT\t\tLocal port number (defaults to random free port)");
        System.err.println("-h | --help\tThis message");
    }

    public GUI(Deploy deploy, Workspace workspace, Mode mode, String... logos)
            throws Exception {
        this.deploy = deploy;
        this.mode = mode;
        this.workspace = workspace;
        createAndShowGUI(logos);
    }

    protected GUI(String[] arguments) {
        boolean verbose = false;
        boolean keepSandboxes = false;
        String serverCluster = null;
        int port = 0;
        Mode mode = Mode.NORMAL;

        try {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].equals("-s")) {
                    i++;
                    serverCluster = arguments[i];
                } else if (arguments[i].equals("-v")) {
                    verbose = true;
                } else if (arguments[i].equals("-k")) {
                    keepSandboxes = true;
                } else if (arguments[i].equals("-p")) {
                    i++;
                    port = Integer.parseInt(arguments[i]);
                } else if (arguments[i].equals("-h")
                        || arguments[i].equals("--help")) {
                    printUsage();
                    System.exit(0);
                } else if (arguments[i].equals("-r")) {
                    mode = Mode.READ_ONLY;
                } else if (arguments[i].equals("-f")) {
                    fakeData = true;
                } else {
                    File file = new File(arguments[i]);
                    if (file.isDirectory()) {
                        workspaceLocation = file;
                    } else {
                        System.err.println("Unknown option: " + arguments[i]);
                        printUsage();
                        System.exit(1);
                    }
                }
            }

            workspace = new Workspace(workspaceLocation);

            if (workspace.getExperiments().size() == 0) {
                workspace.addExperiment(new Experiment("default"));
            } else if (workspace.getExperiments().size() > 1) {
                logger.warn("Multiple experiments in workspace"
                        + ", GUI only supports one, using: "
                        + workspace.getExperiments().get(0).getName()
                        + " as experiment");
            }

        } catch (Exception e) {
            System.err.println("Exception when loading setting files: " + e);
            System.exit(1);
        }

        this.mode = mode;

        if (verbose) {
            System.err.println("DEPLOY: Workspace:");
            System.err.println(workspace.toPrintString());
        }

        try {
            if (serverCluster == null) {
                logger.info("Initializing Ibis Deploy, using build-in server");

                // init with build-in server

                deploy = new Deploy(null, verbose, keepSandboxes, port, null,
                        null, true);
            } else {
                logger.info("Initializing Ibis Deploy"
                        + ", using server on cluster \"" + serverCluster + "\"");

                Cluster cluster = workspace.getGrid().getCluster(serverCluster);

                if (cluster == null) {
                    System.err.println("ERROR: Server cluster " + serverCluster
                            + " not found in grid");
                    System.exit(1);
                }

                InitializationFrame initWindow = new InitializationFrame();
                deploy = new Deploy(null, verbose, keepSandboxes, port,
                        cluster, initWindow, true);
                // will call dispose in the Swing thread
                initWindow.remove();

            }

            RegistryServiceInterface regInterface;
            ManagementServiceInterface manInterface;
            if (fakeData) {
                logger.info("Monitor using simulated data.");

                // Ibis/JMX variables
                regInterface = new FakeRegistryService();
                manInterface = new FakeManagementService(regInterface);
            } else {
                logger.info("Monitor using real data.");
                regInterface = deploy.getServer().getRegistryService();
                manInterface = deploy.getServer().getManagementService();
            }

            // Data interface
            collector = ibis.deploy.monitoring.collection.impl.CollectorImpl
                    .getCollector(manInterface, regInterface, this);
            new Thread(collector).start();

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
        workspaceLocation = Workspace.DEFAULT_LOCATION;
        frame.setTitle("Ibis Deploy - " + Workspace.DEFAULT_LOCATION);
    }

    public void loadWorkspace(File location) throws Exception {
        workspace = new Workspace(location);
        fireWorkSpaceUpdated();
        workspaceLocation = location;
        frame.setTitle("Ibis Deploy - " + workspaceLocation.getName());
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

    public void fireSubmitJob(JobDescription jobDescription) throws Exception {
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

    public boolean isReadOnly() {
        return mode == Mode.READ_ONLY || mode == Mode.MONITOR;
    }

    public JFrame getFrame() {
        return frame;
    }

    public RootPanel getRootPanel() {
        return myRoot;
    }

    public Mode getMode() {
        return mode;
    }

    public Collector getCollector() {
        return collector;
    }
}
