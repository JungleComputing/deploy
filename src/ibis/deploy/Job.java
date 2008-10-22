package ibis.deploy;

import ibis.server.ServerProperties;
import ibis.util.ThreadPool;

import java.io.File;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Job implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Job.class);

	private final String gridName;

	private final String clusterName;

	private final Server rootHub;

	private final Server hub;

	private final GATContext context;

	private final URI resourceBrokerURI;

	private final JobDescription jobDescription;

	private org.gridlab.gat.resources.Job gatJob;

	/**
	 * Creates a job object. Extracts all needed info from the given objects, so
	 * they can be changed after this constructor finishes.
	 */
	public Job(Grid grid, Cluster cluster, int resourceCount,
			Application application, int processCount, String poolName,
			String serverAddress, Server rootHub, File[] serverLibs)
			throws Exception {
		gridName = grid.getName();
		clusterName = cluster.getName();
		this.rootHub = rootHub;

		hub = new Server(serverLibs, cluster, true);

		context = createGATContext(cluster);
		resourceBrokerURI = cluster.getJobURI();
		jobDescription = createJobDescription(grid, cluster, resourceCount,
				application, processCount, poolName, serverAddress);
		gatJob = null;

		// fork thread
		ThreadPool.createNew(this, "ibis deploy job");
	}

	private static GATContext createGATContext(Cluster cluster)
			throws Exception {
		logger.debug("creating context");

		GATContext context = new GATContext();
		if (cluster.getUserName() != null) {
			SecurityContext securityContext = new CertificateSecurityContext(
					null, null, cluster.getUserName(), null);
			// securityContext.addNote("adaptors",
			// "commandlinessh,sshtrilead");
			context.addSecurityContext(securityContext);
		}
		context.addPreference("file.chmod", "0755");
		if (cluster.getJobAdaptor() == null) {
			throw new Exception("no job adaptor specified for cluster: "
					+ cluster);
		}

		context.addPreference("resourcebroker.adaptor.name", cluster
				.getJobAdaptor());

		if (cluster.getFileAdaptors() == null
				|| cluster.getFileAdaptors().length == 0) {
			throw new Exception("no file adaptors specified for cluster: "
					+ cluster);
		}

		context.addPreference("file.adaptor.name", cluster
				.getFileAdaptorString());

		return context;
	}

	private static JobDescription createJobDescription(Grid grid,
			Cluster cluster, int resourceCount, Application application,
			int processCount, String poolName, String serverAddress) {
		logger.debug("creating job description");

		JavaSoftwareDescription softwareDescription = new JavaSoftwareDescription();

		JobDescription result = new JobDescription(softwareDescription);

		result.setProcessCount(processCount);
		result.setResourceCount(resourceCount);

		return result;
	}

	public void run() {
		try {
			hub.waitUntilRunning();
			String hubAddress = hub.getAddress();
			rootHub.addHubs(hubAddress);

			// create list of hubs, add to software description
			String hubList = hubAddress;
			for (String hub : rootHub.getHubs()) {
				hubList = hubList + "," + hub;
			}
			
			//add hub list to softwaredescription
			JavaSoftwareDescription sd = (JavaSoftwareDescription) jobDescription
					.getSoftwareDescription();
			sd.getJavaSystemProperties().put(ServerProperties.HUB_ADDRESSES,
					hubList);

			logger.debug("creating resource broker for job");

			ResourceBroker jobBroker = GAT.createResourceBroker(context,
					resourceBrokerURI);
			
			logger.info("submitting job");

			org.gridlab.gat.resources.Job job = jobBroker.submitJob(
					jobDescription, null, "job.status");

			

		} catch (Exception e) {
			// FIXME: do something with errors
			logger.error("Error on running job: ", e);
		}
	}

	public void kill() {
		// TODO Auto-generated method stub
		
	}

}
