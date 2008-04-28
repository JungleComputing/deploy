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
            deployer.addGrid("grid-test.properties");
            Grid das3 = deployer.getGrid("DAS-3");
            Job job = new Job("initial job");
            SubJob vuSubjob = new SubJob("VU");
            vuSubjob.setApplication(application);
            vuSubjob.setGrid(das3);
            vuSubjob.setCluster(das3.getCluster("VU"));
            vuSubjob.setNodes(Integer.parseInt(args[1]));
            vuSubjob.setMulticore(Integer.parseInt(args[2]));
//            vuSubjob.setWrapperExecutable("/bin/sh");
//            vuSubjob.setWrapperArguments("sleep/my-script.sh");
            job.addSubJob(vuSubjob);
//            SubJob uvaSubjob = new SubJob("UvA");
//            uvaSubjob.setApplication(application);
//            uvaSubjob.setGrid(das3);
//            uvaSubjob.setCluster(das3.getCluster("UvA"));
//            uvaSubjob.setNodes(Integer.parseInt(args[1]));
//            uvaSubjob.setMulticore(Integer.parseInt(args[2]));
//            job.addSubJob(uvaSubjob);
//            SubJob leidenSubjob = new SubJob("Leiden");
//            leidenSubjob.setApplication(application);
//            leidenSubjob.setGrid(das3);
//            leidenSubjob.setCluster(das3.getCluster("Leiden"));
//            leidenSubjob.setNodes(Integer.parseInt(args[1]));
//            leidenSubjob.setMulticore(Integer.parseInt(args[2]));
            deployer.deploy(job);
            boolean leidenAdded = false;
            while (job
                    .getStatus()
                    .get(
                            org.gridlab.gat.resources.Job
                                    .getStateString(org.gridlab.gat.resources.Job.STOPPED)) < 1) {
                System.out.println("job.status:\n" + job.getStatus());
//                if (uvaSubjob.getStatus() == org.gridlab.gat.resources.Job
//                        .getStateString(org.gridlab.gat.resources.Job.RUNNING)
//                        && !leidenAdded) {
//                    Thread.sleep(5000);
//                    deployer.deploy(leidenSubjob, job);
//                    leidenAdded = true;
//                }
                Thread.sleep(1000);
            }
            System.out.println("Deployer end!");
            deployer.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
