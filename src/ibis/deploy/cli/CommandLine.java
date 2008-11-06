package ibis.deploy.cli;

import ibis.deploy.ApplicationSet;
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

    CommandLine(File gridFile, File applicationsFile, File[] experiments)
            throws Exception {
        ApplicationSet applications = new ApplicationSet(applicationsFile);
        System.out.println(applications.toPrintString());
        applications.save(new File(applicationsFile.getAbsolutePath()
                + ".backup"));

        Grid grid = new Grid(gridFile);
        System.out.println(grid.toPrintString());
        grid.save(new File(gridFile.getAbsolutePath() + ".backup"));

        Deploy deploy = new Deploy();

        deploy.initialize(null, null);

        for (File file : experiments) {
            Experiment experiment = new Experiment(file);

            System.out.println(experiment.toPrintString());

            runExperiment(experiment, grid, applications, deploy);

            experiment.save(new File(file.getAbsolutePath() + ".backup"));
        }

        deploy.end();

    }

    //run a single experiment
    private void runExperiment(Experiment experiment, Grid grid,
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
            } else {
                experiments.add(new File(arguments[i]));
            }
        }

        if (gridFile != null && !gridFile.isFile()) {
            System.err.println("Specified grid file: \"" + gridFile
                    + "\" does not exist or is a directory");
            System.exit(1);
        }

        if (applicationsFile != null && !applicationsFile.isFile()) {
            System.err.println("Specified applications file: \""
                    + applicationsFile + "\" does not exist or is a directory");
            System.exit(1);
        }

        if (experiments.size() == 0) {
            System.err.println("no experiments specified!");
            System.err
                    .println("Usage: ibis-deploy-cli [-g GRID_FILE] [-a APPLICATIONS_FILE] [EXPERIMENT_FILE]+...");
            System.exit(1);
        }

        try {
            new CommandLine(gridFile, applicationsFile, experiments
                    .toArray(new File[0]));
        } catch (Exception e) {
            System.err.println("Exception on running experiments");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
