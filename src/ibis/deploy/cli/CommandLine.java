package ibis.deploy.cli;

import ibis.deploy.Application;
import ibis.deploy.ApplicationGroup;
import ibis.deploy.Cluster;
import ibis.deploy.Deploy;
import ibis.deploy.Grid;

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

        // load grids speficied in experiment
        Map<String, Grid> grids = new HashMap<String, Grid>();
        for (File gridFile : experiment.getGridFiles()) {
            Grid grid = new Grid(gridFile);
            grids.put(grid.getName(), grid);
        }

        // load applications specified in experiment
        Map<String, ApplicationGroup> applications = new HashMap<String, ApplicationGroup>();
        for (File applicationFile : experiment.getApplicationFiles()) {
            ApplicationGroup applicationGroup = new ApplicationGroup(
                    applicationFile);
            applications.put(applicationGroup.getName(), applicationGroup);
        }

        // start jobs
        List<ibis.deploy.Job> deployJobs = new ArrayList<ibis.deploy.Job>();
        for (Job job : experiment.getJobs()) {
            if (job.getGrid() == null) {
                throw new Exception("grid not specified in job" + job);
            }
            Grid grid = grids.get(job.getGrid());
            if (grid == null) {
                throw new Exception("could not get grid \"" + job.getGrid()
                        + "\" specified in job " + job.getName());
            }

            if (job.getCluster() == null) {
                throw new Exception("cluster not specified in job " + job);
            }
            Cluster cluster = grid.getCluster(job.getCluster());
            if (cluster == null) {
                throw new Exception("could not get cluster \""
                        + job.getCluster() + "\" specified in job "
                        + job.getName());
            }

            if (job.getApplicationGroup() == null) {
                throw new Exception("Application Group not specified in job " + job);
            }
            ApplicationGroup applicationGroup = applications.get(job
                    .getApplicationGroup());
            if (applicationGroup == null) {
                throw new Exception("could not get applicationGroup \""
                        + job.getApplicationGroup() + "\" specified in job "
                        + job.getName());
            }

            if (job.getApplication() == null) {
                throw new Exception("application not specified in job " + job);
            }
            Application application = applicationGroup.getApplication(job
                    .getApplication());
            if (application == null) {
                throw new Exception("could not get application \""
                        + job.getApplication() + "\" specified in job "
                        + job.getName());
            }

            deployJobs.add(deploy.submitJob(cluster, job.getResourceCount(),
                    application, job.getProcessCount(), experiment.getName(), 0,
                    null, null, job.getSharedHub()));
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
