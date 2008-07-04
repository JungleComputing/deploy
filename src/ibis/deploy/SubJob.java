package ibis.deploy;

import ibis.util.TypedProperties;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.security.CertificateSecurityContext;

public class SubJob implements MetricListener {
	private static Logger logger = Logger.getLogger(SubJob.class);

	private static final int DEFAULT_RUNTIME = 20; // minutes

	private Job parent;

	private String executable;

	private String[] arguments;

	protected static SubJob[] load(TypedProperties runprops, Set<Grid> grids,
			Set<Application> applications, String subjobName) {
		if (logger.isInfoEnabled()) {
			logger.info("loading subjob");
		}
		SubJob subjob = new SubJob(subjobName);
		String gridName = TypedPropertiesUtility.getHierarchicalProperty(
				runprops, subjobName, "grid", null);
		for (Grid grid : grids) {
			if (grid.getGridName().equalsIgnoreCase(gridName)) {
				subjob.grid = grid;
				break;
			}
		}
		if (subjob.grid == null) {
			return null;
		}
		String clusterName = TypedPropertiesUtility.getHierarchicalProperty(
				runprops, subjobName, "cluster", null);
		Cluster[] clusters = subjob.grid.getClusters();
		for (Cluster cluster : clusters) {
			if (cluster.getName().equalsIgnoreCase(clusterName)) {
				subjob.cluster = cluster;
				break;
			}
		}
		if (subjob.cluster == null) {
			return null;
		}
		String nodesString = TypedPropertiesUtility.getHierarchicalProperty(
				runprops, subjobName, "nodes", "-1");
		if (nodesString.equals("max")) {
			subjob.nodes = subjob.cluster.getNodes();
		} else if (nodesString.matches("\\d+?\\s*?%")) {
			Pattern pattern = Pattern.compile("\\d*+");
			Matcher matcher = pattern.matcher(nodesString);
			if (matcher.find()) {
				int percentage = Integer.parseInt(matcher.group());
				subjob.nodes = (int) ((subjob.cluster.getNodes() * percentage) / 100.0);
			}
		} else {
			subjob.nodes = Integer.parseInt(nodesString);
		}
		String multicoreString = TypedPropertiesUtility
				.getHierarchicalProperty(runprops, subjobName, "multicore",
						"-1");
		if (multicoreString.equals("max")) {
			subjob.multicore = subjob.cluster.getMulticore();
		} else {
			subjob.multicore = Integer.parseInt(multicoreString);
		}
		subjob.executables = TypedPropertiesUtility.getHierarchicalInt(
				runprops, subjobName, "exe.count", -1);
		subjob.runtime = TypedPropertiesUtility.getHierarchicalInt(runprops,
				subjobName, "runtime", DEFAULT_RUNTIME);
		subjob.poolID = TypedPropertiesUtility.getHierarchicalProperty(
				runprops, subjobName, "pool.id", null);
		subjob.closedWorld = TypedPropertiesUtility.getHierarchicalBoolean(
				runprops, subjobName, "closed.world", false);
		subjob.attributes = TypedPropertiesUtility.getHierarchicalStringList(
				runprops, subjobName, "gat.attributes", null, " ");
		subjob.preferences = TypedPropertiesUtility.getHierarchicalStringList(
				runprops, subjobName, "gat.preferences", null, " ");
		subjob.executable = TypedPropertiesUtility.getHierarchicalProperty(
				runprops, subjobName, "executable", null);
		subjob.arguments = TypedPropertiesUtility.getHierarchicalStringList(
				runprops, subjobName, "executable.arguments", null, " ");
		String applicationString = TypedPropertiesUtility
				.getHierarchicalProperty(runprops, subjobName, "application",
						null);
		for (Application application : applications) {
			if (application.getName().equalsIgnoreCase(applicationString)) {
				subjob.application = application;
				break;
			}
		}
		try {
			String hubURIString = TypedPropertiesUtility
					.getHierarchicalProperty(runprops, subjobName, "hub.uri",
							null);
			if (hubURIString != null) {
				subjob.hubURI = new URI(hubURIString);
			}
		} catch (URISyntaxException e1) {
			// ignore, default will be set!
		}
		if (subjob.hubURI == null) {
			subjob.hubURI = subjob.getCluster().getDeployBroker();
		}

		// TODO: add support for overriding application properties in the subjob
		// properties?

		int chunksize = TypedPropertiesUtility.getHierarchicalInt(runprops,
				subjobName, "chunksize", 0);
		if (chunksize > 0) {
			// subjobchunksize is the size of the chunks, not the number
			// of chunks.
			int chunks;
			if (subjob.nodes % chunksize == 0) {
				chunks = subjob.nodes / chunksize;
			} else {
				chunks = (subjob.nodes / chunksize) + 1;
			}
			SubJob[] chunkJobs = new SubJob[chunks];
			for (SubJob chunkJob : chunkJobs) {
				try {
					chunkJob = (SubJob) subjob.clone();
				} catch (CloneNotSupportedException e) {
					// will not happen.
				}
				chunkJob.nodes = chunksize;
			}
			// correct the last one if needed.
			if (subjob.nodes % chunksize != 0) {
				chunkJobs[chunks - 1].nodes = subjob.nodes % chunksize;
			}
			return chunkJobs;
		} else {
			return new SubJob[] { subjob };
		}
	}

	private URI hubURI;

	private int executables;

	private String name;

	private String poolID;

	private int nodes;

	private Application application;

	private String[] attributes;

	private String[] preferences;

	private long runtime;

	private Grid grid;

	private Cluster cluster;

	private int multicore;

	private boolean closedWorld;

	private JobState status = JobState.INITIAL;

	/**
	 * Create a {@link SubJob} with the name <code>subjobName</code>
	 * 
	 * @param subjobName
	 *            the name of the {@link SubJob}
	 */
	public SubJob(String subjobName) {
		this.name = subjobName;
	}

	/**
	 * Gets the {@link Application} that will be run by deploying this
	 * {@link SubJob}
	 * 
	 * @return the {@link Application} that will be run by deploying this
	 *         {@link SubJob}
	 */
	public Application getApplication() {
		return application;
	}

	private HashMap<String, Object> getAttributes() {
		if (attributes == null) {
			return null;
		}
		if (attributes.length % 2 > 0) {
			return null;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		for (int i = 0; i < attributes.length; i += 2) {
			result.put(attributes[i], attributes[i + 1]);
		}
		return result;
	}

	/**
	 * Gets the {@link Cluster} where the {@link SubJob} will run if deployed
	 * 
	 * @return the {@link Cluster} where the {@link SubJob} will run if deployed
	 */
	public Cluster getCluster() {
		return cluster;
	}

	/**
	 * Gets the {@link Grid} where the {@link SubJob} will run if deployed
	 * 
	 * @return the {@link Grid} where the {@link SubJob} will run if deployed
	 */
	public Grid getGrid() {
		return grid;
	}

	protected int getMulticore() {
		return multicore;
	}

	/**
	 * Gets the name of this Grid
	 * 
	 * @return the name of this Grid
	 */
	public String getName() {
		return name;
	}

	protected int getNodes() {
		return nodes;
	}

	/**
	 * Gets the poolID of this {@link SubJob}
	 * 
	 * @return the poolID of this {@link SubJob}
	 */
	public String getPoolID() {
		return poolID;
	}

	/**
	 * Gets the pool size of this {@link SubJob}
	 * 
	 * @return the pool size of this {@link SubJob}
	 * @throws Exception
	 *             if the number of nodes and the number of cores and the number
	 *             of executables are set, but are inconsistent (nodes *
	 *             multicore != executables) or the number of executables is
	 *             smaller than the number of nodes or only the number of cores
	 *             is specified
	 * 
	 */
	public int getPoolSize() throws Exception {
		if (!closedWorld) {
			return -1;
		}
		if (nodes > 0) {
			if (multicore > 0) {
				if (executables == nodes * multicore || !(executables > 0)) {
					return nodes * multicore;
				} else {
					throw new Exception("nodes * multicore != executables");
				}
			} else {
				if (executables >= nodes) {
					return executables;
				} else if (executables > 0) {
					throw new Exception("0 < executables < nodes is invalid");
				} else {
					return nodes;
				}
			}
		} else {
			if (executables > 0) {
				return executables;
			} else {
				throw new Exception("only specifying cores doesn't make sense!");
			}
		}
	}

	protected HashMap<String, Object> getPreferences() {
		if (preferences == null) {
			return null;
		}
		if (preferences.length % 2 > 0) {
			return null;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		for (int i = 0; i < preferences.length; i += 2) {
			result.put(preferences[i], preferences[i + 1]);
		}
		return result;
	}

	protected long getRuntime() {
		return runtime;
	}

	/**
	 * Gets the status of this SubJob
	 * 
	 * @return the String indicating the status of this {@link SubJob}
	 */
	public JobState getStatus() {
		return status;
	}

	protected boolean isClosedWorld() {
		return closedWorld;
	}

	/**
	 * Set this {@link SubJob} to run in a closed world ibis
	 * 
	 * @param closedWorld
	 */
	public void setClosedWorld(boolean closedWorld) {
		this.closedWorld = closedWorld;
	}

	/**
	 * Sets the poolID for this {@link SubJob}
	 * 
	 * @param poolID
	 *            the poolID to be used
	 */
	public void setPoolID(String poolID) {
		this.poolID = poolID;
	}

	public String toString() {
		return "SubJob " + name + ": " + grid.getGridName() + " "
				+ cluster.getName() + " " + nodes + " machines, with "
				+ multicore + " cores/machine, for a total of "
				+ (nodes * multicore) + " cores";
	}

	/**
	 * Sets the {@link Application} for this {@link SubJob}
	 * 
	 * @param application
	 *            the {@link Application} to be used.
	 */
	public void setApplication(Application application) {
		this.application = application;
	}

	/**
	 * Sets the gat attributes for this {@link SubJob}
	 * 
	 * @param attributes
	 *            the attributes to be used.
	 */
	public void setAttributes(String... attributes) {
		this.attributes = attributes;
	}

	/**
	 * Sets the {@link Cluster} for this {@link SubJob}
	 * 
	 * @param cluster
	 *            the cluster where the {@link SubJob} should run on.
	 */
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * Sets the total number of executables for this {@link SubJob}
	 * 
	 * @param executables
	 *            the total number of executables
	 */
	public void setExecutables(int executables) {
		this.executables = executables;
	}

	/**
	 * Sets the {@link Grid} for this {@link SubJob}
	 * 
	 * @param grid
	 *            the {@link Grid} for this {@link SubJob}
	 */
	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	/**
	 * Sets the URI of the hub that should be deployed for this {@link SubJob}
	 * 
	 * @param hubURI
	 *            the URI of the hub
	 */
	public void setHubURI(URI hubURI) {
		this.hubURI = hubURI;
	}

	/**
	 * Sets the number of cores for this {@link SubJob}.
	 * 
	 * @param multicore
	 *            the number of cores for this {@link SubJob}
	 */
	public void setMulticore(int multicore) {
		this.multicore = multicore;
	}

	/**
	 * Sets the number of nodes for this {@link SubJob}
	 * 
	 * @param nodes
	 *            the number of nodes for this {@link SubJob}
	 */
	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	/**
	 * Sets the javagat preferences for this {@link SubJob}
	 * 
	 * @param preferences
	 *            the javagat preferences for this {@link SubJob}
	 */
	public void setPreferences(String... preferences) {
		this.preferences = preferences;
	}

	/**
	 * Sets the runtime for this {@link SubJob}
	 * 
	 * @param runtime
	 *            the runtime in minutes
	 */
	public void setRuntime(long runtime) {
		this.runtime = runtime;
	}

	protected String[] getArguments() {
		return arguments;
	}

	protected String getExecutable() {
		return executable;
	}

	protected boolean hasExecutable() {
		return executable != null;
	}

	/**
	 * Sets the additional arguments for the wrapper executable that should be
	 * run
	 * 
	 * @param arguments
	 *            the additional arguments for the wrapper executable that
	 *            should be run
	 */
	public void setWrapperArguments(String... arguments) {
		this.arguments = arguments;
	}

	/**
	 * Sets the wrapper executable
	 * 
	 * @param executable
	 *            the wrapper executable
	 */
	public void setWrapperExecutable(String executable) {
		this.executable = executable;
	}

	/**
	 * <b>DO NOT USE. For internal use only</b>
	 */
	public void processMetricEvent(MetricEvent event) {
		status = (JobState) event.getValue();
		((org.gridlab.gat.resources.Job) (event.getSource())).getState();
		if (status.equals(JobState.STOPPED)
				|| status.equals(JobState.SUBMISSION_ERROR)) {

			try {
				parent.inform();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void setParent(Job parent) {
		this.parent = parent;
	}

	public void submit(String poolID, int poolSize, String serverAddress,
			String hubAddress, String outputDirectory)
			throws GATObjectCreationException, URISyntaxException,
			GATInvocationException {
		if (logger.isInfoEnabled()) {
			logger.info("submitting sub job " + name);
		}
		Preferences preferences = new Preferences();
		preferences.put("ResourceBroker.adaptor.name", cluster.getAccessType());
		preferences.put("File.adaptor.name", cluster.getFileAccessType());
		preferences.put("sshtrilead.stoppable", "true");
		Map<String, Object> additionalPreferences = getPreferences();
		if (additionalPreferences != null) {
			Set<String> preferenceKeys = additionalPreferences.keySet();
			for (String key : preferenceKeys) {
				preferences.put(key, additionalPreferences.get(key));
			}
		}
		File outFile = GAT.createFile(preferences, new URI(outputDirectory
				+ getName() + "." + application.getName() + ".stdout"));
		File errFile = GAT.createFile(preferences, new URI(outputDirectory
				+ getName() + "." + application.getName() + ".stderr"));

		JavaSoftwareDescription sd = new JavaSoftwareDescription();
		Map<String, Object> additionalAttributes = getAttributes();
		if (additionalAttributes != null) {
			sd.setAttributes(getAttributes());
		}
		if (cluster.getJavaPath() != null) {
			if (cluster.isWindows()) {
				sd.setExecutable(cluster.getJavaPath() + "\\bin\\java");
			} else {
				sd.setExecutable(cluster.getJavaPath() + "/bin/java");
			}
		}

		sd.setJavaClassPath(application.getJavaClassPath(application
				.getPreStageSet(), true, cluster.isWindows()));

		Map<String, String> systemProperties = new HashMap<String, String>();
		systemProperties.put("log4j.configuration", "file:"
				+ application.getLog4jPropertiesLocation());
		if (application.getJavaSystemProperties() != null) {
			systemProperties.putAll(application.getJavaSystemProperties());
		}
		systemProperties.put("ibis.server.address", serverAddress);

		systemProperties.put("ibis.server.hub.addresses", hubAddress);

		systemProperties.put("ibis.pool.name", poolID);
		if (isClosedWorld()) {
			systemProperties.put("ibis.pool.size", "" + poolSize);
			// FIXME: these are actually zorilla specific...
			sd.addAttribute("malleable", "false");
		} else {
			sd.addAttribute("malleable", "true");
		}
		// systemProperties.put("ibis.location.postfix",
		// subJob.getClusterName());
		// systemProperties.put("ibis.location.automatic", "true");
		systemProperties.put("ibis.location", getCluster().getName() + "@"
				+ getGrid().getGridName());
		sd.setJavaSystemProperties(systemProperties);
		sd.setJavaOptions(application.getJavaOptions());
		sd.setJavaMain(application.getJavaMain());
		sd.setJavaArguments(application.getJavaArguments());
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
						.createFile(preferences, getName() + "." + filename));
			}
		}
		int nodes = getNodes();
		int multicore = getMulticore();

		logger.debug("nodes = " + nodes + ", multicore = " + multicore);

		sd.addAttribute("count", nodes * multicore);
		sd.addAttribute("host.count", nodes);
		sd.addAttribute("walltime.max", getRuntime());
		JobDescription jd = null;

		if (!hasExecutable()) {
			jd = new JobDescription(sd);
		} else {
			logger.debug("executable = " + getExecutable()
					+ ", creating a non java job");

			SoftwareDescription nonJava = new SoftwareDescription();
			if (sd.getAttributes() != null) {
				nonJava.setAttributes(sd.getAttributes());
			}
			if (sd.getEnvironment() != null) {
				nonJava.setEnvironment(sd.getEnvironment());
			}
			if (application.getPreStageSet() != null) {
				for (String filename : application.getPreStageSet()) {
					nonJava.addPreStagedFile(GAT.createFile(preferences,
							filename));
				}
			}
			if (application.getPostStageSet() != null) {
				for (String filename : application.getPostStageSet()) {
					nonJava.addPostStagedFile(GAT.createFile(preferences,
							filename), GAT.createFile(preferences, getName()
							+ "." + filename));
				}
			}
			nonJava.setStderr(errFile);
			nonJava.setStdout(outFile);
			nonJava.setExecutable(getExecutable());
			List<String> argumentList = new ArrayList<String>();
			if (getArguments() != null) {
				for (String arg : getArguments()) {
					argumentList.add(arg);
				}
			}

			argumentList.add("" + getNodes());
			argumentList.add("" + getMulticore());

			// argumentList.add("" + subjob.getRuntime());
			argumentList.add(sd.getExecutable());
			if (sd.getArguments() != null) {
				for (String arg : sd.getArguments()) {
					argumentList.add(arg);
				}
			}
			nonJava.setArguments(argumentList.toArray(new String[argumentList
					.size()]));
			jd = new JobDescription(nonJava);
		}

		GATContext context = new GATContext();
		context.addSecurityContext(new CertificateSecurityContext(null, null,
				cluster.getUserName(), null));

		ResourceBroker broker = GAT.createResourceBroker(context, preferences,
				cluster.getJobBroker());
		logger.debug("submission of subjob '" + name
				+ "' with job description:\n" + jd);
		broker.submitJob(jd, this, "job.status");

	}
}