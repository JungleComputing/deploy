package ibis.deploy.cli;

import ibis.deploy.Application;
import ibis.deploy.ApplicationGroup;
import ibis.deploy.Cluster;
import ibis.deploy.Deploy;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;
import ibis.deploy.JobDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLine {

    private static final Logger logger = LoggerFactory
            .getLogger(CommandLine.class);

    private static void runExperiment(Experiment experiment, Deploy deploy)
            throws Exception {
        logger.info("Running experiment " + experiment);

        // start jobs
        List<ibis.deploy.Job> deployJobs = new ArrayList<ibis.deploy.Job>();
        for (JobDescription job : experiment.getJobs()) {

//            deployJobs.add(deploy.submitJob(cluster, job.getResourceCount(),
//                    application, job.getProcessCount(), experiment.getName(), 0,
//                    null, null, job.getSharedHub()));
        }
        
        //wait for all jobs to end
        for (ibis.deploy.Job job: deployJobs) {
            job.waitUntilFinished();
        }
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length == 0) {
            System.err.println("Usage: ibis-deploy-cli [EXPERIMENT_FILE]...");
            System.exit(0);
        }

        Deploy deploy = new Deploy();
        
        deploy.initialize(null, null);

        List<Experiment> experiments = new ArrayList<Experiment>();

        // create experiments from files
        for (String argument : arguments) {
            File file = new File(argument);
            experiments.add(new Experiment(file));
        }

        for (Experiment experiment : experiments) {
            runExperiment(experiment, deploy);
        }

        deploy.end();
    }

}
