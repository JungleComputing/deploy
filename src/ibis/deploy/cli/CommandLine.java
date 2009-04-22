package ibis.deploy.cli;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Cluster;
import ibis.deploy.Deploy;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.Workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line interface for ibis-deploy library
 * 
 * @author Niels Drost
 * 
 */
public class CommandLine {

    private static final Logger logger = LoggerFactory
            .getLogger(CommandLine.class);

    // run a single experiment
    private static void runExperiment(Experiment experiment, Grid grid,
            ApplicationSet applications, Deploy deploy, boolean verbose)
            throws Exception {
        if (verbose) {
            logger.info("Running experiment: " + experiment.toPrintString());
        } else {
            logger.info("Running experiment \"" + experiment.getName() + "\"");
            logger.debug("Running experiment: " + experiment.toPrintString());
        }

        // start jobs
        List<Job> jobs = new ArrayList<Job>();
        for (JobDescription jobDescription : experiment.getJobs()) {
            Job job = deploy.submitJob(jobDescription, applications, grid,
                    null, null);
            jobs.add(job);
        }

        // wait for all jobs to end
        deploy.waitUntilJobsFinished();

        logger.info("Experiment \"" + experiment + "\" done");
    }

    private static void printUsage() {
        System.err
                .println("Usage: ibis-deploy-cli [OPTIONS] [WORKSPACE_DIR] [GRID_FILE] [APP_FILE] [EXPERIMENT_FILE]+...");
        System.err.println("Options:");
        System.err.println("-s CLUSTER\tRun server on specified cluster");
        System.err.println("-v\t\tVerbose mode");
        System.err.println("-k\t\tKeep sandboxes");
        System.err.println("-h | --help\tThis message");
    }

    /**
     * @param arguments
     *            arguments of application
     */
    public static void main(String[] arguments) {
        File workspaceDir = null;
        File gridFile = null;
        File applicationsFile = null;
        List<Experiment> experiments = new ArrayList<Experiment>();
        boolean verbose = false;
        boolean keepSandboxes = false;
        Grid grid = null;
        String serverCluster = null;
        ApplicationSet applications = null;

        if (arguments.length == 0) {
            printUsage();
            System.exit(0);
        }

        try {

            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].equals("-s")) {
                    i++;
                    serverCluster = arguments[i];
                } else if (arguments[i].equals("-v")) {
                    verbose = true;
                } else if (arguments[i].equals("-k")) {
                    keepSandboxes = true;
                } else if (arguments[i].equals("-h")
                        || arguments[i].equals("--help")) {
                    printUsage();
                    System.exit(0);
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
                    File file = new File(arguments[i]);
                    Experiment experiment = new Experiment(file);

                    experiments.add(experiment);
                } else {
                    File file = new File(arguments[i]);
                    if (file.isDirectory()) {
                        if (workspaceDir != null) {
                            System.err
                                    .println("ERROR: can only specify a single workspace dir");
                            System.exit(1);
                        }

                        workspaceDir = file;
                    } else {

                        System.err.println("Unknown option or file type: "
                                + arguments[i]);
                        printUsage();
                        System.exit(1);
                    }
                }
            }

            // load stuff from workspace
            if (workspaceDir != null) {
                Workspace workspace = new Workspace(workspaceDir);
                grid = workspace.getGrid();
                applications = workspace.getApplications();
                experiments.addAll(workspace.getExperiments());
            }

            if (gridFile != null) {
                if (!gridFile.isFile()) {
                    System.err.println("ERROR: Specified grid file: \""
                            + gridFile + "\" does not exist or is a directory");
                    System.exit(1);
                }

                grid = new Grid(gridFile);

            }

            if (grid != null && verbose) {
                System.err.println("Grid:");
                System.err.println(grid.toPrintString());
            }

            if (applicationsFile != null) {
                if (!applicationsFile.isFile()) {
                    System.err.println("ERROR: Specified applications file: \""
                            + applicationsFile
                            + "\" does not exist or is a directory");
                    System.exit(1);

                }

                applications = new ApplicationSet(applicationsFile);

            }

            if (applications != null && verbose) {
                logger.info("Applications: " + applications.toPrintString());
            } else if (applications != null) {
                logger.debug("Applications: " + applications.toPrintString());
            }

            if (experiments.size() == 0) {
                System.err.println("ERROR: no experiments specified!");
                printUsage();
                System.exit(1);
            }

            Deploy deploy = new Deploy(null, verbose);

            deploy.keepSandboxes(keepSandboxes);

            if (serverCluster == null) {
                logger
                        .info("Initializing Command Line Ibis Deploy, using build-in server");

                deploy.initialize(null);
            } else {
                logger
                        .info("Initializing Command Line Ibis Deploy, using server on cluster \""
                                + serverCluster + "\"");

                if (grid == null) {
                    System.err.println("ERROR: Server cluster " + serverCluster
                            + " not found, no grid file specified");
                    System.exit(1);
                }

                Cluster cluster = grid.getCluster(serverCluster);

                if (cluster == null) {
                    System.err.println("ERROR: Server cluster " + serverCluster
                            + " not found in grid");
                    System.exit(1);
                }

                deploy.initialize(cluster);
            }

            // run experiments
            for (Experiment experiment : experiments) {
                runExperiment(experiment, grid, applications, deploy, verbose);
            }

            deploy.end();
        } catch (Exception e) {
            logger.error("Exception on running experiments", e);
            System.exit(1);
        }
    }

}
