package ibis.deploy;

import ibis.server.Server;
import ibis.server.ServerProperties;
import ibis.util.TypedProperties;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;

public class Deployer implements MetricListener {

	private static Logger logger = Logger.getLogger(Deployer.class);

	private Set<Application> applications = new HashSet<Application>();;

	private Set<Grid> grids = new HashSet<Grid>();

	private String ibisClassPath;

	private String ibisHome;

	private Server server;

	private URI serverURI;

	private Hashtable<URI, String> existingHubs = new Hashtable<URI, String>();

	private org.gridlab.gat.resources.Job serverJob;

	private List<org.gridlab.gat.resources.Job> hubJobs = new ArrayList<org.gridlab.gat.resources.Job>();

	// public Deployer(URI serverURI) throws Exception {
	// if (logger.isInfoEnabled()) {
	// logger.info("constructor");
	// }
	// ibisHome = System.getenv("IBIS_HOME");
	// if (ibisHome == null) {
	// throw new Exception("Environment variable 'IBIS_HOME' not set!");
	// }
	//
	// ibisClassPath = "";
	// java.io.File tmp = new java.io.File(ibisHome + "/lib");
	// String[] jars = tmp.list();
	// for (int i = 0; i < jars.length; i++) {
	// ibisClassPath += "lib/" + jars[i];
	// }
	// startServer(serverURI);
	// }

	public Deployer() throws URISyntaxException, Exception {
		this(new Cluster("server", new URI("any://localhost")));
	}

	public Deployer(Cluster serverCluster) throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("constructor");
		}
		ibisHome = System.getenv("IBIS_HOME");
		if (ibisHome == null) {
			throw new Exception("Environment variable 'IBIS_HOME' not set!");
		}

		ibisClassPath = "";
		java.io.File tmp = new java.io.File(ibisHome + "/lib");
		String[] jars = tmp.list();
		for (int i = 0; i < jars.length; i++) {
			ibisClassPath += "lib/" + jars[i] + ":";
		}
		startServer(serverCluster);
	}

	// public Deployer() throws URISyntaxException, Exception {
	// this(new URI("any://localhost"));
	// }

	// TODO: useful?
	// public Deployer(Cluster cluster, boolean startOnHeadNode) throws
	// Exception {
	// this(cluster.getBroker(startOnHeadNode));
	// }

	public void addApplications(Set<Application> applications) {
		this.applications.addAll(applications);
	}

	public void addApplication(Application application) {
		this.applications.add(application);
	}

	public void addApplications(String applicationFileName)
			throws FileNotFoundException, IOException {
		TypedProperties properties = new TypedProperties();
		properties.load(new java.io.FileInputStream(applicationFileName));
		addApplications(properties);
	}

	public void addApplications(TypedProperties properties) {
		properties.expandSystemVariables();
		addApplications(Application.load(properties));
	}

	public void addGrid(Grid grid) {
		this.grids.add(grid);
	}

	public void addGrid(String gridFileName) throws FileNotFoundException,
			IOException {
		TypedProperties properties = new TypedProperties();
		properties.load(new java.io.FileInputStream(gridFileName));
		addGrid(properties);
	}

	public void addGrid(TypedProperties properties) {
		properties.expandSystemVariables();
		addGrid(Grid.load(properties));
	}

	private void cancelJob(org.gridlab.gat.resources.Job gatJob) {
		try {
			gatJob.stop();
		} catch (GATInvocationException e) {
			// TODO: something useful
		}
	}

	public void deploy(Job job) throws Exception {
		URI[] hubURIs = job.getHubURIs();
		if (hubURIs != null) {
			for (URI hubURI : hubURIs) {
				if (hubURI != null) {
					startHub(hubURI);
				}
			}
		}
		if (job.getPoolID() == null) {
			job.setPoolID(generatePoolID());
		}
		for (SubJob subjob : job.getSubJobs()) {
			submitSubJob(subjob, job.getPoolID(), job.getPoolSize());
		}
	}

	public void deploy(SubJob subjob) throws Exception {
		URI hubURI = subjob.getHubURI();
		if (hubURI != null) {
			startHub(hubURI);
		}
		if (subjob.getPoolID() == null) {
			subjob.setPoolID(generatePoolID());
		}
		submitSubJob(subjob, subjob.getPoolID(), subjob.getPoolSize());
	}

	public void deploy(SubJob subjob, Job job) throws Exception {
		if (job.isClosedWorld() || subjob.isClosedWorld()) {
			throw new Exception("Cannot add to a closed world!");
		}
		URI hubURI = subjob.getHubURI();
		if (hubURI != null) {
			startHub(hubURI);
		}
		if (job.getPoolID() == null) {
			job.setPoolID(generatePoolID());
		}
		job.addSubJob(subjob);
		submitSubJob(subjob, job.getPoolID(), -1);
	}

	public void end() {
		if (logger.isInfoEnabled()) {
			logger.info("end");
		}
		if (serverURI.refersToLocalHost()) {
			if (server != null) {
				server.end(true);
			}
		} else {
			cancelJob(serverJob);
		}
		for (org.gridlab.gat.resources.Job hubJob : hubJobs) {
			cancelJob(hubJob);
		}
		GAT.end();
	}

	private String generatePoolID() {
		if (logger.isInfoEnabled()) {
			logger.info("generate pool id");
		}
		// TODO: better
		return "pool-id-" + Math.random();
	}

	public Set<Application> getApplications() {
		return applications;
	}

	public Set<Grid> getGrids() {
		return grids;
	}

	public Grid getGrid(String gridName) {
		if (grids == null) {
			return null;
		}
		for (Grid grid : grids) {
			if (grid.getGridName().equalsIgnoreCase(gridName)) {
				return grid;
			}
		}
		return null;
	}

	private String getHubAddressesString() {
		if (logger.isInfoEnabled()) {
			logger.info("get hub addresses string");
		}
		// start with the server as first element
		String result = existingHubs.get(serverURI);
		Set<URI> others = existingHubs.keySet();
		// then add the others as long as they're not the same as the server.
		for (URI other : others) {
			if (other.equals(serverURI)) {
				continue;
			}
			result += "," + existingHubs.get(other);
		}
		return result;
	}

	private String getJavaHome(URI uri) {
		if (logger.isInfoEnabled()) {
			logger.info("get java home");
		}
		for (Grid grid : grids) {
			Cluster[] clusters = grid.getClusters();
			for (Cluster cluster : clusters) {
				if (cluster.getBroker(true).getHost().equalsIgnoreCase(
						uri.getHost())) {
					return cluster.getJavaPath();
				}
			}
		}
		return null;
	}

	// private void startExternalServer(URI serverURI)
	// throws GATObjectCreationException, GATInvocationException,
	// IOException {
	// if (logger.isInfoEnabled()) {
	// logger.info("start external server");
	// }
	// this.serverURI = serverURI;
	// JavaSoftwareDescription sd = new JavaSoftwareDescription();
	// sd.setExecutable(getJavaHome(serverURI) + "/bin/java");
	//
	// sd.setJavaMain("ibis.server.Server");
	// sd.setJavaArguments(new String[] { "--port", "0", "--hub-address-file",
	// serverURI.getHost() + ".address" });
	// sd.setJavaOptions(new String[] { "-classpath", ibisClassPath,
	// "-Dlog4j.configuration=file:./log4j.properties" });
	// sd.addPreStagedFile(GAT.createFile(ibisHome + "/lib"));
	// sd.addPreStagedFile(GAT.createFile(ibisHome + "/log4j.properties"));
	// sd.setStderr(GAT.createFile("server@" + serverURI.getHost() + ".err"));
	// sd.setStdout(GAT.createFile("server@" + serverURI.getHost() + ".out"));
	// JobDescription jd = new JobDescription(sd);
	// ResourceBroker broker = GAT.createResourceBroker(serverURI);
	// serverJob = broker.submitJob(jd, this, "job.status");
	// waitForHubAddress(serverURI);
	// }

	public List<Job> loadRun(String runFileName) throws FileNotFoundException,
			IOException {
		TypedProperties properties = new TypedProperties();
		properties.load(new java.io.FileInputStream(runFileName));
		return loadRun(properties);
	}

	public List<Job> loadRun(TypedProperties properties) {
		properties.expandSystemVariables();
		return Run.load(properties, grids, applications);
	}

	public void processMetricEvent(MetricEvent arg0) {
		System.out.println("DEPLOYER: " + arg0.getValue());
	}

	private void startExternalServer(Cluster serverCluster)
			throws GATObjectCreationException, GATInvocationException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("start external server");
		}
		serverURI = serverCluster.getDeployBroker();
		Preferences serverPreferences = new Preferences();
		serverPreferences.put("file.adaptor.name", serverCluster
				.getFileAccessType());
		serverPreferences.put("resourcebroker.adaptor.name", serverCluster
				.getAccessType());
		JavaSoftwareDescription sd = new JavaSoftwareDescription();
		sd.setExecutable(serverCluster.getJavaPath() + "/bin/java");

		sd.setJavaMain("ibis.server.Server");
		sd.setJavaArguments(new String[] { "--port", "0", "--hub-address-file",
				"../" + serverCluster.getName() + ".address" });
		sd.setJavaOptions(new String[] { "-classpath", ibisClassPath,
				"-Dlog4j.configuration=file:./log4j.properties" });
		sd.addPreStagedFile(GAT
				.createFile(serverPreferences, ibisHome + "/lib"));
		sd.addPreStagedFile(GAT.createFile(serverPreferences, ibisHome
				+ "/log4j.properties"));
		sd.setStderr(GAT.createFile(serverPreferences, "server@"
				+ serverCluster.getName() + ".err"));
		sd.setStdout(GAT.createFile(serverPreferences, "server@"
				+ serverCluster.getName() + ".out"));
		JobDescription jd = new JobDescription(sd);
		ResourceBroker broker = GAT.createResourceBroker(serverPreferences,
				serverCluster.getDeployBroker());
		serverJob = broker.submitJob(jd, this, "job.status");
		waitForHubAddress(serverCluster.getDeployBroker());
	}

	private void startHub(URI hubURI) throws GATObjectCreationException,
			GATInvocationException, IOException {
		if (logger.isInfoEnabled()) {
			logger.info("starting hub " + hubURI);
		}
		if (existingHubs.containsKey(hubURI)) {
			return;
		}
		JavaSoftwareDescription sd = new JavaSoftwareDescription();
		sd.setExecutable(getJavaHome(hubURI) + "/bin/java");

		sd.setJavaMain("ibis.server.Server");
		sd.setJavaArguments(new String[] { "--hub-only", "--hub-addresses",
				getHubAddressesString(), "--port", "0", "--hub-address-file",
				"../" + hubURI.getHost() + ".address" });
		sd.setJavaOptions(new String[] { "-classpath", ibisClassPath,
				"-Dlog4j.configuration=file:./log4j.properties" });
		sd.addPreStagedFile(GAT.createFile(ibisHome + "/lib"));
		sd.addPreStagedFile(GAT.createFile(ibisHome + "/log4j.properties"));
		sd.setStderr(GAT.createFile("hub@" + hubURI.getHost() + ".err"));
		sd.setStdout(GAT.createFile("hub@" + hubURI.getHost() + ".out"));
		JobDescription jd = new JobDescription(sd);
		ResourceBroker broker = GAT.createResourceBroker(hubURI);
		hubJobs.add(broker.submitJob(jd));
		waitForHubAddress(hubURI);

	}

	private void startInternalServer() throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("starting internal server");
		}
		Properties properties = new Properties();
		// let the server automatically find a free port
		properties.put(ServerProperties.PORT, "0");
		properties.put(ServerProperties.IMPLEMENTATION_PATH, ibisHome
				+ File.separator + "lib");
		properties.put("ibis.registry.central.statistics", "true");
		properties.put(ServerProperties.PRINT_EVENTS, "" + true);
		properties.put(ServerProperties.PRINT_ERRORS, "" + true);
		properties.put(ServerProperties.PRINT_STATS, "" + true);
		server = new Server(properties);
		logger.info("started ibis server: " + server);
		serverURI = new URI("any://localhost");
		existingHubs.put(serverURI, server.getLocalAddress());
	}

	private void startServer(Cluster serverCluster) throws Exception {
		// TODO: booleans for events, errors etc.
		if (logger.isInfoEnabled()) {
			logger.info("starting server");
		}
		if (serverCluster.getDeployBroker().refersToLocalHost()) {
			// start the server on the localhost using a server object.
			startInternalServer();
		} else {
			// start the server just like the hubs
			startExternalServer(serverCluster);
		}
	}

	private void submitSubJob(SubJob subjob, String poolID, int poolSize)
			throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("submitting sub job");
		}
		Cluster cluster = subjob.getCluster();
		Application application = subjob.getApplication();
		Preferences preferences = new Preferences();
		preferences.put("ResourceBroker.adaptor.name", cluster.getAccessType());
		preferences.put("File.adaptor.name", cluster.getFileAccessType());
		// preferences.put("file.hiddenfiles.ignore", "true");
		// preferences.put("ftp.connection.passive", "false");
		Map<String, Object> additionalPreferences = subjob.getPreferences();
		if (additionalPreferences != null) {
			Set<String> preferenceKeys = additionalPreferences.keySet();
			for (String key : preferenceKeys) {
				preferences.put(key, additionalPreferences.get(key));
			}
		}
		File outFile = GAT.createFile(preferences, new URI("any:///"
				+ subjob.getName() + "." + application.getName() + ".stdout"));
		File errFile = GAT.createFile(preferences, new URI("any:///"
				+ subjob.getName() + "." + application.getName() + ".stderr"));

		JavaSoftwareDescription sd = new JavaSoftwareDescription();
		Map<String, Object> additionalAttributes = subjob.getAttributes();
		if (additionalAttributes != null) {
			sd.setAttributes(subjob.getAttributes());
		}
		sd.setExecutable(cluster.getJavaPath() + "/bin/java");
		// add ibis/lib jars to the classpath
		String classpath = ibisClassPath;
		// add executable jar to the classpath
		if (application.getJavaClassPath() != null
				&& !application.getJavaClassPath().equals("")) {
			classpath += ":" + application.getJavaClassPath();
		}
		sd.setJavaClassPath(classpath);

		Map<String, String> systemProperties = new HashMap<String, String>();
		systemProperties.put("log4j.configuration", "file:log4j.properties");
		if (application.getJavaSystemProperties() != null) {
			systemProperties.putAll(application.getJavaSystemProperties());
		}
		systemProperties
				.put("ibis.server.address", existingHubs.get(serverURI));
		systemProperties.put("ibis.server.hub.addresses",
				getHubAddressesString());
		systemProperties.put("ibis.pool.name", poolID);
		if (subjob.isClosedWorld()) {
			systemProperties.put("ibis.pool.size", "" + poolSize);
		}
		// systemProperties.put("ibis.location.postfix",
		// subJob.getClusterName());
		systemProperties.put("ibis.location.automatic", "true");
		sd.setJavaSystemProperties(systemProperties);
		sd.setJavaOptions(application.getJavaOptions());
		sd.setJavaMain(application.getJavaMain());
		sd.setJavaArguments(application.getJavaArguments());
		sd.addPreStagedFile(GAT.createFile(preferences, ibisHome + "/lib"));
		String cwd = System.getProperty("user.dir");
		sd.addPreStagedFile(GAT.createFile(preferences, cwd + "/"
				+ "log4j.properties"));
		sd.addPreStagedFile(GAT.createFile(preferences, cwd + "/"
				+ "smartsockets.properties"));
		sd.setStderr(errFile);
		sd.setStdout(outFile);
		if (application.getPreStageSet() != null) {
			for (String filename : application.getPreStageSet()) {
				sd.addPreStagedFile(GAT.createFile(preferences, filename));
			}
		}
		if (application.getPostStageSet() != null) {
			for (String filename : application.getPostStageSet()) {
				sd.addPostStagedFile(GAT.createFile(preferences, filename), GAT
						.createFile(preferences, subjob.getName() + "."
								+ filename));
			}
		}
		int nodes = subjob.getNodes();
		int multicore = subjob.getMulticore();
		sd.addAttribute("count", nodes * multicore);
		sd.addAttribute("host.count", nodes);
		sd.addAttribute("walltime.max", subjob.getRuntime());

		JobDescription jd = new JobDescription(sd);

		ResourceBroker broker = GAT.createResourceBroker(preferences, cluster
				.getJobBroker());
		subjob.addGATJob(broker.submitJob(jd));
	}

	private void waitForHubAddress(URI hubURI)
			throws GATObjectCreationException, GATInvocationException,
			IOException {
		// the hub is started, and now we wait... for the file with
		// its address to appear.
		if (logger.isInfoEnabled()) {
			logger.info("wait for hub address");
		}
		File hubAddressFile = null;
		hubAddressFile = GAT.createFile(new GATContext(), "any://"
				+ hubURI.getHost() + "/" + hubURI.getHost() + ".address");
		while (!hubAddressFile.getFileInterface().exists()) {
			if (logger.isDebugEnabled()) {
				logger.debug("waiting for " + hubAddressFile.toString()
						+ " to appear.");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		// the file exists, let's read the address from the file!
		FileInputStream in = null;
		in = GAT.createFileInputStream(new GATContext(), hubAddressFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String hubAddress = reader.readLine();
		existingHubs.put(hubURI, hubAddress);
	}

}
