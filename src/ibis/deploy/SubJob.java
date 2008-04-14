package ibis.deploy;

import ibis.util.TypedProperties;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.Job;

public class SubJob {
	private static Logger logger = Logger.getLogger(SubJob.class);

	private static final int DEFAULT_RUNTIME = 20; // minutes

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
			subjob.hubURI = new URI(TypedPropertiesUtility
					.getHierarchicalProperty(runprops, subjobName, "hub.uri",
							null));
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

	private Job job;

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

	public SubJob(String subjobName) {
		this.name = subjobName;
	}

	// public Object clone(SubJob subjob) {
	// SubJob clone = new SubJob(subjob.name);
	// clone.nodes = subjob.nodes;
	// clone.application = subjob.application;
	// clone.attributes = subjob.attributes;
	// clone.preferences = subjob.preferences;
	// clone.runtime = subjob.runtime;
	// clone.multicore = subjob.multicore;
	// return clone;
	// }

	protected void addGATJob(Job job) {
		this.job = job;
	}

	public Application getApplication() {
		return application;
	}

	public HashMap<String, Object> getAttributes() {
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

	public Cluster getCluster() {
		return cluster;
	}

	public Grid getGrid() {
		return grid;
	}

	public URI getHubURI() {
		return hubURI;
	}

	public int getMulticore() {
		return multicore;
	}

	public String getName() {
		return name;
	}

	public int getNodes() {
		return nodes;
	}

	public String getPoolID() {
		return poolID;
	}

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

	public HashMap<String, Object> getPreferences() {
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

	public long getRuntime() {
		return runtime;
	}

	public int getStatus() {
		if (logger.isDebugEnabled()) {
			logger.debug("subjob " + name + " getStatus (job=" + job + ")");
		}
		if (job == null) {
			return Job.INITIAL;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("subjob " + name + " getStatus=" + job.getState());
		}
		return job.getState();
	}

	public boolean isClosedWorld() {
		return closedWorld;
	}

	public void setClosedWorld(boolean closedWorld) {
		this.closedWorld = closedWorld;
	}

	public void setPoolID(String poolID) {
		this.poolID = poolID;
	}

	public String toString() {
		return "SubJob " + name + ": " + grid.getGridName() + " "
				+ cluster.getName() + " " + nodes + " machines, with "
				+ multicore + " cores/machine, for a total of "
				+ (nodes * multicore) + " cores";
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public void setExecutables(int executables) {
		this.executables = executables;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public void setHubURI(URI hubURI) {
		this.hubURI = hubURI;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public void setMulticore(int multicore) {
		this.multicore = multicore;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	public void setPreferences(String[] preferences) {
		this.preferences = preferences;
	}

	public void setRuntime(long runtime) {
		this.runtime = runtime;
	}
}
