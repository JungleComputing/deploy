package test;

import ibis.deploy.Cluster;
import ibis.deploy.Deployer;
import ibis.deploy.Job;

import java.util.List;

import org.gridlab.gat.URI;
import org.gridlab.gat.resources.Job.JobState;

public class DeployTest {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		try {
			Cluster serverCluster = new Cluster("my little server cluster");
			serverCluster.setDeployBroker(new URI("any://fs0.das3.cs.vu.nl/jobmanager-fork"));
			serverCluster.setJavapath("/usr/local/package/jdk1.6.0");
			serverCluster.setFileAccessType("local, gridftp, sshtrilead");
			serverCluster.setAccessType("local, globus, sshtrilead");
			Deployer deployer = new Deployer(serverCluster);
			deployer.addApplications("application-test.properties");
			deployer.addGrid("grid-test.properties");
			List<Job> jobs = deployer.loadRun("run-test.properties");
			for (Job job : jobs) {
				deployer.deploy(job);
				while (job.getStatus().get(JobState.STOPPED) < 1) {
					System.out.println("job.status:\n" + job.getStatus());
				}
			}
			deployer.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
