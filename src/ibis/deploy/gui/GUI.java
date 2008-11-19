package ibis.deploy.gui;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Deploy;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;
import ibis.deploy.JobDescription;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

public class GUI {

    private Deploy deploy;

    private Grid grid = null;

    private ApplicationSet applications = null;

    private Experiment experiment = null;

    private JMenuBar menuBar = null;

    private Boolean sharedHubs;

    /**
     * @param args
     */
    public static void main(String[] args) {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        final GUI gui = new GUI(args);
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(gui);
            }
        });

    }

    protected static void createAndShowGUI(GUI gui) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        JFrame frame = new JFrame("Ibis Deploy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gui.menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.add("Exit");
        gui.menuBar.add(menu);
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
        boolean verbose = false;

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("-g")) {
                i++;
                gridFile = new File(arguments[i]);
            } else if (arguments[i].equals("-a")) {
                i++;
                applicationsFile = new File(arguments[i]);
            } else if (arguments[i].equals("-v")) {
                verbose = true;
            } else {
                experimentFile = new File(arguments[i]);
            }
        }

        if (gridFile != null) {
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

        if (applicationsFile != null) {
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
        } else {
            try {
                experiment = new Experiment("default");
                experiment.createNewJob("default");
            } catch (Exception e) {
                // ignore will always work!
                e.printStackTrace();
            }
        }

        deploy = new Deploy();

    }

    protected Deploy getDeploy() {
        return deploy;
    }

    protected File getDeployHome() {
        return new File(System.getenv("DEPLOY_HOME"));
    }

    protected Grid getGrid() {
        return grid;
    }

    protected ApplicationSet getApplicationSet() {
        return applications;
    }

    protected Experiment getExperiment() {
        return experiment;
    }

    protected JobDescription getCurrentJobDescription() {
        return experiment.getJobs()[experiment.getJobs().length - 1];
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

}
