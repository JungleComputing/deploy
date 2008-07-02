package ibis.deploy.application;

import ibis.deploy.Deployer;
import ibis.deploy.Job;
import ibis.util.TypedProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class IbisDeploy {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err
                    .println("Usage: ibis.deploy.application.IbisDeploy <runfile>");
            System.exit(1);
        }
        TypedProperties properties = new TypedProperties();
        try {
            properties.load(new java.io.FileInputStream(args[0]));
        } catch (FileNotFoundException e) {
            System.err.println("File '" + args[0] + "' not found: " + e);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading file '" + args[0] + "': " + e);
            System.exit(1);
        }

        Deployer deployer = new Deployer();

        // load the grids from the grid files
        String[] gridFiles = properties.getStringList("grid.files");
        if (gridFiles == null) {
            System.err
                    .println("Reading the property 'grid.files' resulted in null, please specify at least one grid file");
            System.exit(1);
        }
        for (String gridFile : gridFiles) {
            try {
                deployer.addGrid(gridFile);
            } catch (FileNotFoundException e) {
                System.err.println("File '" + gridFile + "' not found: " + e);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Error reading file '" + gridFile + "': "
                        + e);
                System.exit(1);
            }
        }

        // load the applications from the application files
        String[] applicationFiles = properties
                .getStringList("application.files");
        if (applicationFiles == null) {
            System.err
                    .println("Reading the property 'application.files' resulted in null, please specify at least one application file");
            System.exit(1);
        }
        for (String applicationFile : applicationFiles) {
            try {
                deployer.addApplications(applicationFile);
            } catch (FileNotFoundException e) {
                System.err.println("File '" + applicationFile + "' not found: "
                        + e);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Error reading file '" + applicationFile
                        + "': " + e);
                System.exit(1);
            }
        }
        // load the jobs from the run file
        List<Job> jobs = deployer.loadRun(properties);

        if (jobs == null) {
            System.err.println("failed to load any jobs!");
            System.exit(1);
        }
        // deploy the loaded jobs
        for (Job job : jobs) {
            try {
                deployer.deploy(job);
            } catch (Exception e) {
                System.err.println("Failed to deploy job '" + job.getName()
                        + "'");
                e.printStackTrace();
                System.exit(1);
            }
        }

        while (true) {
            // wait until all jobs are done
            boolean allJobsDone = true;

            for (Job job : jobs) {
                allJobsDone = allJobsDone
                        && (job.getStatus().get(
                                org.gridlab.gat.resources.Job.STOPPED_STRING) + job
                                .getStatus()
                                .get(
                                        org.gridlab.gat.resources.Job.SUBMISSION_ERROR_STRING)) == job
                                .getSubJobs().size();
            }
            if (allJobsDone) {
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

}
