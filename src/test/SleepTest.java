package test;

import ibis.deploy.Application;
import ibis.deploy.Deployer;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.SubJob;

public class SleepTest {

    /**
     * args[0] = seconds to sleep args[1] = nodes args[2] = cores/node
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            Deployer deployer = new Deployer();
            Application application = new Application("sleep", // name of the
                    // application
                    "Sleep", // main class
                    null, // java options
                    null, // java system properties
                    new String[] { args[0] }, // java arguments
                    new String[] { "sleep" }, // pre stage
                    null, // post stage
                    null // ibis server pre stage
            );
            deployer.addApplication(application);
            deployer.addGrid("das3.properties");
            Grid das3 = deployer.getGrid("DAS-3");
            Job job = new Job("initial job");
            SubJob vuSubjob = new SubJob("keg");
            vuSubjob.setApplication(application);
            vuSubjob.setGrid(das3);
            vuSubjob.setCluster(das3.getCluster("keg"));
            vuSubjob.setNodes(Integer.parseInt(args[1]));
            vuSubjob.setMulticore(Integer.parseInt(args[2]));
            job.addSubJob(vuSubjob);
            deployer.deploy(job);
            while (job
                    .getStatus()
                    .get(
                            org.gridlab.gat.resources.Job
                                    .getStateString(org.gridlab.gat.resources.Job.STOPPED)) < 1) {
                System.out.println("job.status:\n" + job.getStatus());
                Thread.sleep(1000);
            }
            System.out.println("Deployer end!");
            deployer.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
