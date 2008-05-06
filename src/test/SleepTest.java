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
                    null // post stage
            );
            deployer.addApplication(application);
            deployer.addGrid("das3.properties");
            Grid das3 = deployer.getGrid("DAS-3");
            Job job = new Job("initial job");
            SubJob vuSubjob = new SubJob("VU");
            vuSubjob.setApplication(application);
            vuSubjob.setGrid(das3);
            vuSubjob.setCluster(das3.getCluster("VU"));
            vuSubjob.setNodes(Integer.parseInt(args[1]));
            vuSubjob.setMulticore(Integer.parseInt(args[2]));
            vuSubjob.setWrapperExecutable("/bin/sh");
            vuSubjob.setWrapperArguments("sleep/my-script.sh");
            job.addSubJob(vuSubjob);
            SubJob uvaSubjob = new SubJob("UvA");
            uvaSubjob.setApplication(application);
            uvaSubjob.setGrid(das3);
            uvaSubjob.setCluster(das3.getCluster("UvA"));
            uvaSubjob.setNodes(Integer.parseInt(args[1]));
            uvaSubjob.setMulticore(Integer.parseInt(args[2]));
            uvaSubjob.setWrapperExecutable("/bin/sh");
            uvaSubjob.setWrapperArguments("sleep/my-script.sh");
            job.addSubJob(uvaSubjob);
            deployer.deploy(job);
            while (job
                    .getStatus()
                    .get(
                            org.gridlab.gat.resources.Job
                                    .getStateString(org.gridlab.gat.resources.Job.STOPPED)) < 2) {
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
