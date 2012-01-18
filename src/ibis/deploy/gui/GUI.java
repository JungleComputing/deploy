package ibis.deploy.gui;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Resource;
import ibis.deploy.Deploy;
import ibis.deploy.Experiment;
import ibis.deploy.Jungle;
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
import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.simulator.FakeManagementService;
import ibis.deploy.monitoring.simulator.FakeRegistryService;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GUI {

    public static final int DEFAULT_SCREEN_WIDTH = 1024;

    public static final int DEFAULT_SCREEN_HEIGHT = 768;

    private static final Logger logger = LoggerFactory.getLogger(GUI.class);

    private Deploy deploy;

    private final List<WorkSpaceChangedListener> jungleListeners = new ArrayList<WorkSpaceChangedListener>();

    private final List<WorkSpaceChangedListener> applicationSetListeners = new ArrayList<WorkSpaceChangedListener>();

    private final List<WorkSpaceChangedListener> experimentListeners = new ArrayList<WorkSpaceChangedListener>();

    private final List<SubmitJobListener> submitJobListeners = new ArrayList<SubmitJobListener>();

    private Workspace workspace = null;

    private File workspaceLocation = Workspace.DEFAULT_LOCATION;

    private JFrame frame = null;

    private JMenuBar menuBar = null;

    private RootPanel myRoot;

    private Mode mode;

    private final Collector collector;

    private final boolean monitoringEnabled;

    private boolean nativeLoaded = false;

    // private Boolean sharedHubs;

    private static class Shutdown extends Thread {
        private final GUI gui;

        Shutdown(GUI gui) {
            this.gui = gui;
        }

        @Override
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
                try {
                    gui.createAndShowGUI();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        });
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

        if (mode == Mode.READONLY_WORKSPACE || mode == Mode.MONITORING_ONLY) {
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

        if (mode == Mode.NORMAL) {
            JMenu subMenu = new JMenu("Hub Policy");
            ButtonGroup hubPolicy = new ButtonGroup();
            menuItem = new JRadioButtonMenuItem(new HubPolicyAction("No hubs",
                    HubPolicy.OFF, this));
            hubPolicy.add(menuItem);
            subMenu.add(menuItem);
            menuItem = new JRadioButtonMenuItem(new HubPolicyAction(
                    "One hub per resource", HubPolicy.PER_RESOURCE, this));
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

        if (mode == Mode.READONLY_WORKSPACE || mode == Mode.MONITORING_ONLY) {
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    close();
                }
            });
        } else {
            frame.addWindowListener(new WindowAdapter() {
                @Override
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
                .println("Usage: ibis-deploy-gui [OPTIONS] [JUNGLE_FILE] [APP_FILE] [EXPERIMENT_FILE] [WORKSPACE_DIR]");
        System.err.println("Options:");
        System.err.println("-s RESOURCE\tRun server on specified resource");
        System.err.println("-k\t\tKeep sandboxes");
        System.err.println("-r\t\tRead only mode");
        System.err.println("-v\t\tVerbose mode");
        System.err
                .println("--monitoring-enabled | -m \tCollect performance data from running applications.");
        System.err.println("-f\t\tSimulate a jungle (for monitor testing).");
        System.err
                .println("-p PORT\t\tLocal port number (defaults to random free port)");
        System.err.println("-h | --help\tThis message");
    }

    public GUI(Deploy deploy, Workspace workspace, Mode mode,
            final String... logos) throws Exception {
        this(deploy, workspace, mode, deploy.isMonitoringEnabled(), logos);
    }

    public GUI(Deploy deploy, Workspace workspace, Mode mode,
            boolean monitoringEnabled, final String... logos) throws Exception {
        this.deploy = deploy;
        this.mode = mode;
        this.workspace = workspace;
        this.monitoringEnabled = monitoringEnabled;

        if (isMonitoringEnabled()) {
            RegistryServiceInterface regInterface;
            ManagementServiceInterface manInterface;
            logger.info("Monitoring enabled");
            regInterface = deploy.getServer().getRegistryService();
            manInterface = deploy.getServer().getManagementService();

            // Data interface
            collector = ibis.deploy.monitoring.collection.impl.CollectorImpl
                    .getCollector(manInterface, regInterface);
            new Thread(collector).start();
        } else {
            collector = null;
        }
        createAndShowGUI(logos);
    }

    protected GUI(String[] arguments) {
        boolean verbose = false;
        boolean keepSandboxes = false;
        boolean monitoringFakeData = false;
        boolean monitoringEnabled = false;
        Collector collector = null;
        String serverResource = null;
        int port = 0;
        mode = Mode.NORMAL;

        try {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].equals("-s")) {
                    i++;
                    serverResource = arguments[i];
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
                    mode = Mode.READONLY_WORKSPACE;
                } else if (arguments[i].equals("-f")) {
                    monitoringFakeData = true;
                } else if (arguments[i].equals("-m")
                        || arguments.equals("--monitoring-enabled")) {
                    monitoringEnabled = true;
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

        if (verbose) {
            System.err.println("DEPLOY: Workspace:");
            System.err.println(workspace.toPrintString());
        }

        this.monitoringEnabled = monitoringEnabled;

        try {
            if (serverResource == null) {
                logger.info("Initializing Ibis Deploy, using built-in server");

                // init with built-in server

                deploy = new Deploy(null, verbose, keepSandboxes,
                        isMonitoringEnabled(), port, null, null, true);
            } else {
                logger
                        .info("Initializing Ibis Deploy"
                                + ", using server on resource \""
                                + serverResource + "\"");

                Resource resource = workspace.getJungle().getResource(serverResource);

                if (resource == null) {
                    System.err.println("ERROR: Server resource " + serverResource
                            + " not found in jungle");
                    System.exit(1);
                }

                InitializationFrame initWindow = new InitializationFrame();
                deploy = new Deploy(null, verbose, keepSandboxes,
                        isMonitoringEnabled(), port, resource, initWindow, true);
                // will call dispose in the Swing thread
                initWindow.remove();

            }

            if (isMonitoringEnabled()) {
                RegistryServiceInterface regInterface;
                ManagementServiceInterface manInterface;
                if (monitoringFakeData) {
                    logger.info("Collecting simulated data.");

                    // Ibis/JMX variables
                    regInterface = new FakeRegistryService(1, 1, 2, 3, 4);
                    manInterface = new FakeManagementService(regInterface);
                } else {
                    logger.info("Monitoring enabled");
                    regInterface = deploy.getServer().getRegistryService();
                    manInterface = deploy.getServer().getManagementService();
                }

                // Data interface
                collector = ibis.deploy.monitoring.collection.impl.CollectorImpl
                        .getCollector(manInterface, regInterface);
                new Thread(collector).start();
            }
        } catch (Exception e) {
            System.err.println("Could not initialize ibis-deploy: " + e);
            e.printStackTrace(System.err);
            System.exit(1);
        }
        this.collector = collector;
    }

    public Deploy getDeploy() {
        return deploy;
    }

    public Jungle getJungle() {
        return workspace.getJungle();
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
        fireJungleUpdated();
        fireApplicationSetUpdated();
        fireExperimentUpdated();
    }

    public void fireJungleUpdated() {
        for (WorkSpaceChangedListener listener : jungleListeners) {
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

    public void addJungleWorkSpaceListener(WorkSpaceChangedListener listener) {
        jungleListeners.add(listener);
    }

    public void addApplicationSetWorkSpaceListener(
            WorkSpaceChangedListener listener) {
        applicationSetListeners.add(listener);
    }

    public void addExperimentWorkSpaceListener(WorkSpaceChangedListener listener) {
        experimentListeners.add(listener);
    }

    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
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

    // Methods to load native libraries depending on OS and architecture
    private String arch() {
        String a = System.getProperty("os.arch");
        if ("amd64".equals(a) || "x86_64".equals(a)) {
            return "amd64";
        }
        return "i586";
    }

    private String getNativeJarName(String os) {
        String prefix = "-natives-";

        if (os.indexOf("nt") >= 0 || os.indexOf("win") >= 0) {
            return prefix + "windows-" + arch() + ".jar";
        }
        if (os.indexOf("mac") >= 0) {
            return prefix + "macosx-universal.jar";
        }
        if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            return prefix + "linux-" + arch() + ".jar";
        }
        return null;
    }

    private String[] getNativeLibNames(File jarFile) throws IOException {
        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> entries = jar.entries();
        ArrayList<String> fileNames = new ArrayList<String>();

        while (entries.hasMoreElements()) {
            ZipEntry z = entries.nextElement();
            String entryName = z.getName();

            if (entryName.indexOf("META") < 0) {
                fileNames.add(entryName);
            }
        }
        return fileNames.toArray(new String[0]);
    }

    private File makeNativeDir() throws IOException {
        File tmpdir = new File(deploy.getHome().getAbsolutePath()
                + System.getProperty("file.separator") + "lib"
                + System.getProperty("file.separator") + "natives");

        if (!tmpdir.mkdir() && !tmpdir.exists()) {
            throw new IOException("Could not create temp directory: "
                    + tmpdir.getAbsolutePath());
        }

        return tmpdir;
    }

    public void loadNativeLibs() throws IOException, URISyntaxException {
        if (true) {
            nativeLoaded = true;
            return;
        }

        String filesep = System.getProperty("file.separator");
        String os = System.getProperty("os.name").toLowerCase();
        String dirname = deploy.getHome().getAbsolutePath() + filesep + "lib";

        String[] prefixes = { "jogl", "gluegen-rt", "nativewindow", "newt" };

        File tmpdir = makeNativeDir();
        String tmpPath = String.format(tmpdir.getAbsolutePath());

        HashMap<File, File[]> jarStore = new HashMap<File, File[]>();

        for (String prefix : prefixes) {
            // Select the correct jar file based on OS and architecture
            String jarname = dirname + filesep + prefix + getNativeJarName(os);

            File nativeJar = new File(jarname);

            String[] nativeLibNames = getNativeLibNames(nativeJar);
            File[] nativeLibs = new File[nativeLibNames.length];

            for (int i = 0; i < nativeLibNames.length; i++) {
                nativeLibs[i] = new File(tmpPath + filesep + nativeLibNames[i]);
            }

            jarStore.put(nativeJar, nativeLibs);
        }

        for (Entry<File, File[]> entry : jarStore.entrySet()) {
            for (File file : entry.getValue()) {
                extractNativeLib(entry.getKey(), file);
            }
        }
        nativeLoaded = true;
    }

    private void extractNativeLib(File file, File target) throws IOException,
            URISyntaxException {
        JarFile jar = new JarFile(file);
        ZipEntry z = jar.getEntry(target.getName());
        if (z == null) {
            throw new UnsatisfiedLinkError("Could not find library: "
                    + target.getName());
        }

        InputStream in = jar.getInputStream(z);
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(
                    target));
            try {
                byte[] buf = new byte[2048];
                for (;;) {
                    int n = in.read(buf);
                    if (n < 0) {
                        break;
                    }
                    out.write(buf, 0, n);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
