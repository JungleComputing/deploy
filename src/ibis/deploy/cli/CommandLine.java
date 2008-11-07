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
            ApplicationSet applications, Deploy deploy) throws Exception {
        logger.info("Running experiment " + experiment);

        // start jobs
        List<Job> jobs = new ArrayList<Job>();
        for (JobDescription jobDescription : experiment.getJobs()) {
            jobs.add(deploy.submitJob(jobDescription, applications, grid, null,
                null));
        }

        // wait for all jobs to end
        for (Job job : jobs) {
            job.waitUntilFinished();
        }
    }

    /**
     * @param arguments
     *            arguments of application
     */
    public static void main(String[] arguments) {
        File gridFile = null;
        File applicationsFile = null;
        List<File> experiments = new ArrayList<File>();
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
                experiments.add(new File(arguments[i]));
            }
        }

        try {

            if (gridFile != null) {
                if (!gridFile.isFile()) {
                    System.err.println("Specified grid file: \"" + gridFile
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
                    System.err.println("Specified applications file: \""
                            + applicationsFile
                            + "\" does not exist or is a directory");
                    System.exit(1);

                }

                applications = new ApplicationSet(applicationsFile);

                if (verbose) {
                    System.err.println("Applications:");
                    System.err.println(applications.toPrintString());
                }
            }

            if (experiments.size() == 0) {
                System.err.println("no experiments specified!");
                System.err
                        .println("Usage: ibis-deploy-cli [-v] [-s SERVER_CLUSTER] [-g GRID_FILE] [-a APPLICATIONS_FILE] [EXPERIMENT_FILE]+...");
                System.exit(1);
            }

            Deploy deploy = new Deploy();

            //initialize deploy, use remote server cluster if specified
            if (serverCluster == null) {
                deploy.initialize(null, null);
            } else {
                if (grid == null) {
                    System.err.println("Server cluster " + serverCluster
                        + " not found, no grid file specified");
                }
                
                Cluster cluster = grid.getCluster(serverCluster);

                if (cluster == null) {
                    System.err.println("Server cluster " + serverCluster
                            + " not found in grid");
                }
                
                deploy.initialize(cluster, null);
            }

            //run experiments
            for (File file : experiments) {
                Experiment experiment = new Experiment(file);

                if (verbose) {
                    System.out.println("Running experiment: "
                            + experiment.toPrintString());
                }

                runExperiment(experiment, grid, applications, deploy);

            }

            deploy.end();
        } catch (Exception e) {
            System.err.println("Exception on running experiments");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
