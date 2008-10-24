package ibis.deploy;

import ibis.ipl.IbisProperties;
import ibis.server.ServerProperties;
import ibis.util.ThreadPool;

import java.io.File;
import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Job implements Runnable, MetricListener {
	
	private static int nextID = 0;
	
	static synchronized int getNextID() {
		return nextID++;
	}

	private static final Logger logger = LoggerFactory.getLogger(Job.class);

	private final String gridName;

	private final String clusterName;
	
	private final String jobID;
	
	private final Server rootHub;

	private final boolean globalHub;

	private final Server hub;

	private final GATContext context;

	private final URI resourceBrokerURI;

	private final JavaSoftwareDescription javaSoftwareDescription;

	private final JobDescription jobDescription;
	
	//set in case this jobs fails for some reason.
	private Exception error = null;

	// private org.gridlab.gat.resources.Job gatJob;

	/**
	 * Creates a job object. Extracts all needed info from the given objects, so
	 * they can be changed after this constructor finishes.
	 */
	public Job(Cluster cluster, int resourceCount, Application application,
			int processCount, String poolName, String serverAddress,
			Server rootHub, Server hub, File[] serverLibs) throws Exception {
		gridName = cluster.getGridName();
		clusterName = cluster.getName();
		this.rootHub = rootHub;
		
		jobID = "Job-" + getNextID();

		if (hub == null) {
			globalHub = false;
			hub = new Server(serverLibs, cluster, true, rootHub);
		} else {
			globalHub = true;
		}
		this.hub = hub;

		context = createGATContext(cluster);
		resourceBrokerURI = cluster.getJobURI();
		javaSoftwareDescription = createJavaSoftwareDescription(cluster,
				resourceCount, application, processCount, poolName,
				serverAddress);

		jobDescription = new JobDescription(javaSoftwareDescription);
		jobDescription.setProcessCount(processCount);
		jobDescription.setResourceCount(resourceCount);

		logger.info("Submitting application \"" + application + "\" to " + clusterName + "@" + gridName + " using " + cluster.getJobAdaptor() + "(" + cluster.getJobURI() + ")");
		
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

		context.addPreference("file.adaptor.name", Util.strings2CSS(cluster
				.getFileAdaptors()));

		return context;
	}
	
	private synchronized void setError(Exception error) {
		this.error = error;
		notifyAll();
	}
	
	private static String classpathFor(File file, String prefix) {
		//logger.debug("classpath for: " + file + " prefix = " + prefix);
		
		if (!file.isDirectory()) {
			// regular files not in classpath
			return "";
		}
		// classpath for dir "lib" with prefix "dir/" is dir/lib/*:dir/lib
		// both directory itself, and all files in that dir (*)
		String result = prefix + file.getName() + File.separator + "*"
				+ File.pathSeparator + prefix + file.getName()
				+ File.pathSeparator;
		for (File child : file.listFiles()) {
			result = result
					+ classpathFor(child, prefix + file.getName() + File.separator);
		}
		return result;
	}

	// classpath made up of all directories, as well as
	private static String createClassPath(File[] libs) {
		// start with lib directory
		String result = "lib" + File.pathSeparator + "lib" + File.separator + "*" + File.pathSeparator;

		for (File file : libs) {
			result = result + classpathFor(file, "lib" + File.separator);
		}

		return result;
	}

	private JavaSoftwareDescription createJavaSoftwareDescription(
			Cluster cluster, int resourceCount, Application application,
			int processCount, String poolName, String serverAddress) throws Exception {
		logger.debug("creating job description");

		JavaSoftwareDescription sd = new JavaSoftwareDescription();
		
		if (cluster.getJavaPath() == null) {
			sd.setExecutable("java");
		} else {
			sd.setExecutable(cluster.getJavaPath());
		}
		logger.debug("executable: " + sd.getExecutable());

		//basic application properties
		
		if (application.getMainClass() == null) {
			throw new Exception("no main class specified for " + application);
		}
		sd.setJavaMain(application.getMainClass());
		
		sd.setJavaArguments(application.getArguments());
		sd.setJavaSystemProperties(application.getSystemProperties());
		sd.setJavaOptions(application.getJavaOptions());
		
		//ibis stuff
		sd.addJavaSystemProperty(IbisProperties.LOCATION_POSTFIX, gridName + "@" + clusterName);
		sd.addJavaSystemProperty(IbisProperties.POOL_NAME, poolName);
		sd.addJavaSystemProperty(IbisProperties.POOL_SIZE, "" + processCount);
		sd.addJavaSystemProperty(IbisProperties.SERVER_ADDRESS, serverAddress);

		//file referring to lib dir
		org.gridlab.gat.io.File libDir = GAT.createFile(new URI("lib/"));

		if (application.getLibs() == null) {
			throw new Exception("no library files specified for application " + application);
		}
		
		//add library files
		for (File file : application.getLibs()) {
			if(!file.exists()) {
				throw new Exception("File " + file + " in libs of job does not exist");
			}

			URI uri = new URI(file.getAbsolutePath());
			org.gridlab.gat.io.File gatFile = GAT.createFile(uri);

			sd.addPreStagedFile(gatFile, libDir);
		}

		//file referring to root of sandbox / current directory
		org.gridlab.gat.io.File cwd = GAT.createFile(new URI("."));
		
		if (application.getInputFiles() != null) {
		for (File file : application.getInputFiles()) {
			if(!file.exists()) {
				throw new Exception("File " + file + " in input files of job does not exist");
			}
			
			URI uri = new URI(file.getAbsolutePath());
			org.gridlab.gat.io.File gatFile = GAT.createFile(uri);

			sd.addPreStagedFile(gatFile, cwd);
		}
		}

		if (application.getOutputFiles() != null) {
		for (File file : application.getOutputFiles()) {
			URI uri = new URI(file.getAbsolutePath());
			org.gridlab.gat.io.File gatFile = GAT.createFile(uri);

			//FIXME: check if this actually works, probably not
			sd.addPostStagedFile(gatFile);
		}
		}

		sd.getAttributes().put("sandbox.delete", "false");

		// class path
		sd.setJavaClassPath(createClassPath(application.getLibs()));

		sd.setStdout(GAT.createFile(jobID + ".out"));
		sd.setStderr(GAT.createFile(jobID + ".err"));

		return sd;
	}

	public void run() {
		try {
			hub.waitUntilRunning();
			String hubAddress = hub.getAddress();

			// create list of hubs, add to software description
			String hubList = hubAddress;
			for (String hub : rootHub.getHubs()) {
				hubList = hubList + "," + hub;
			}

			// add hub list to softwaredescription
			javaSoftwareDescription.addJavaSystemProperty(
					IbisProperties.HUB_ADDRESSES, hubList);

			ResourceBroker jobBroker = GAT.createResourceBroker(context,
					resourceBrokerURI);
			
			logger.info("job description = " + javaSoftwareDescription);

			 org.gridlab.gat.resources.Job job = jobBroker
			 .submitJob(jobDescription, this, "job.status");

			Thread.sleep(60000);

			// wait until job done

		} catch (Exception e) {
			logger.error("Error on running job: ", e);
			setError(e);
			
		}
		if (!globalHub) {
			// kill our local hub
			hub.kill();
		}
	}

	public void kill() {
		// TODO Auto-generated method stub

	}

	public String getClusterName() {
		return clusterName;
	}

	public String getGridName() {
		return gridName;
	}
	
	public void processMetricEvent(MetricEvent event) {
		logger.info(this + " status now " + event.getValue());
	}
	
	public String toString() {
		return jobID;
	}

}
