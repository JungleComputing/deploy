package ibis.deploy.cli;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Cluster;
import ibis.deploy.Deploy;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.JobDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line interface for ibis-deploy library
 * 
 * @author Niels Drost
 * 
 */
public class CommandLine {

    // run a single experiment
    private static void runExperiment(Experiment experiment, Grid grid,
            ApplicationSet applications, Deploy deploy, boolean verbose)
            throws Exception {
        if (verbose) {
            System.out.println("DEPLOY: Running experiment: "
                    + experiment.toPrintString());
        } else {
            System.err.println("DEPLOY: Running experiment \"" + experiment + "\" with " + experiment.getJobs().length + " jobs");
        }

        // start jobs
        List<Job> jobs = new ArrayList<Job>();
        for (JobDescription jobDescription : experiment.getJobs()) {
            Job job = deploy.submitJob(jobDescription, applications, grid,
                null, null);
            jobs.add(job);
            if (verbose) {
                System.err.println("DEPLOY: Submitted job "
                        + job.getDescription().toPrintString());
            } else {
                System.err.println("DEPLOY: Submitted job " + job);
            }
        }

        // wait for all jobs to end
        for (Job job : jobs) {
            job.waitUntilFinished();
        }
        System.err.println("DEPLOY: Experiment \"" + experiment + "\" done");
    }

    /**
     * @param arguments
     *            arguments of application
     */
    public static void main(String[] arguments) {
        File gridFile = null;
        File applicationsFile = null;
        List<File> experimentFiles = new ArrayList<File>();
        boolean verbose = false;
        Grid grid = null;
        String serverCluster = null;
        ApplicationSet applications = null;

        if (arguments.length == 0) {
            System.err
                    .println("Usage: ibis-deploy-cli [-g GRID_FILE] [-a APPLICATIONS_FILE] [EXPERIMENT_FILE]+...");
            System.exit(0);
        }

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("-g")) {
                i++;
                gridFile = new File(arguments[i]);
            } else if (arguments[i].equals("-a")) {
                i++;
                applicationsFile = new File(arguments[i]);
            } else if (arguments[i].equals("-s")) {
                i++;
                serverCluster = arguments[i];
            } else if (arguments[i].equals("-v")) {
                verbose = true;
            } else {
                experimentFiles.add(new File(arguments[i]));
            }
        }

        try {

            if (gridFile != null) {
                if (!gridFile.isFile()) {
                    System.err.println("DEPLOY: Specified grid file: \"" + gridFile
                            + "\" does not exist or is a directory");
                    System.exit(1);
                }

                grid = new Grid(gridFile);

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

                applications = new ApplicationSet(applicationsFile);

                if (verbose) {
                    System.err.println("DEPLOY: Applications:");
                    System.err.println(applications.toPrintString());
                }
            }

            if (experimentFiles.size() == 0) {
                System.err.println("DEPLOY: no experiments specified!");
                System.err
                        .println("Usage: ibis-deploy-cli [-v] [-s SERVER_CLUSTER] [-g GRID_FILE] [-a APPLICATIONS_FILE] [EXPERIMENT_FILE]+...");
                System.exit(1);
            }

            Deploy deploy = new Deploy();

            if (serverCluster == null) {
                System.err
                        .println("DEPLOY: Initializing Command Line Ibis Deploy, using build-in server");

                deploy.initialize(null, null);
            } else {
                System.err
                        .println("DEPLOY: Initializing Command Line Ibis Deploy, using server on cluster \""
                                + serverCluster + "\"");

                if (grid == null) {
                    System.err.println("Server cluster " + serverCluster
                            + " not found, no grid file specified");
                }

                Cluster cluster = grid.getCluster(serverCluster);

                if (cluster == null) {
                    System.err.println("DEPLOY: Server cluster " + serverCluster
                            + " not found in grid");
                }

                deploy.initialize(cluster, null);
            }

            // print pool size statistics
            new PoolSizePrinter(deploy);

            // run experiments
            for (File file : experimentFiles) {
                Experiment experiment = new Experiment(file);

                runExperiment(experiment, grid, applications, deploy, verbose);
            }

            deploy.end();
        } catch (Exception e) {
            System.err.println("DEPLOY: Exception on running experiments");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
