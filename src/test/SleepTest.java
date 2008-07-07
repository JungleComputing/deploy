package test;

import org.gridlab.gat.resources.Job.JobState;

import ibis.deploy.Application;
import ibis.deploy.Deployer;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.SubJob;

public class SleepTest {

    /**
     * args[0] = seconds to sleep
     * 
     * args[1] = nodes
     * 
     * args[2] = cores/node
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            // first create a new deployer object
            Deployer deployer = new Deployer();

            // then we've to create the application description, we can do that
            // by loading a properties file or by creating an Application object
            Application application = new Application("sleep", // name of the
                    // application
                    "Sleep", // main class
                    null, // java options
                    null, // java system properties
                    new String[] { args[0] }, // java arguments
                    new String[] { "sleep", "sleep/log4j.properties" }, // pre
                                                                        // stage
                    null, // post stage
                    null // ibis server pre stage
            );
            // add this application description to the deployer. Now the
            // deployer knows about this application using its name
            deployer.addApplication(application);

            // add a grid to the deployer (this can also be done using a Grid
            // object, or by loading a properties file)
            deployer.addGrid("das3.properties");

            // request a grid from the deployer
            Grid das3 = deployer.getGrid("DAS-3");

            // create a job we want to run on this grid (called 'main job')
            Job job = new Job("main job");

            // this job consists of several sub jobs, the first of it is named
            // 'sub job 1'
            SubJob subJob1 = new SubJob("subjob1");
            // it runs the specified application
            subJob1.setApplication(application);
            // on the specified grid
            subJob1.setGrid(das3);
            // on the specific cluster
            subJob1.setCluster(das3.getCluster("VU"));
            // with these number of nodes
            subJob1.setNodes(Integer.parseInt(args[1]));
            // and these number of instances per node
            subJob1.setMulticore(Integer.parseInt(args[2]));
            // we're done specifying the sub job and now add it to the main job
            job.addSubJob(subJob1);

            SubJob subJob2 = new SubJob("subjob2");
            // it runs the specified application
            subJob2.setApplication(application);
            // on the specified grid
            subJob2.setGrid(das3);
            // on the specific cluster
            subJob2.setCluster(das3.getCluster("VU"));
            // with these number of nodes
            subJob2.setNodes(Integer.parseInt(args[1]));
            // and these number of instances per node
            subJob2.setMulticore(Integer.parseInt(args[2]));
            // we're done specifying the sub job and now add it to the main job
            job.addSubJob(subJob2);

            // now we're going to deploy the main job using our deployer
            deployer.deploy(job);

            // and we're polling for our job to be finished
            while (job.getStatus().get(JobState.STOPPED) < 1) {
                System.out.println("job.status:\n" + job.getStatus());
                Thread.sleep(1000);
            }

            System.out.println("Deployer end!");

            // to clean up everything we call the end method
            deployer.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
