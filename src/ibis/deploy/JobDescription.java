package ibis.deploy;

import java.io.PrintWriter;

/**
 * Single job in an experiment.
 * 
 * @author Niels Drost
 * 
 */
public class JobDescription {

	/**
	 * Print a table of valid keys and some explanations to the given stream
	 * 
	 * @param out
	 *            stream used for printing
	 */
	public static void printTableOfKeys(PrintWriter out) {
		out.println("# Valid parameters for jobs:");
		out.println("# KEY                 COMMENT");
		out.println("# application.name    Name of application to run");
		out.println("# application.*       All valid entries for an application, overriding any");
		out.println("#                     specified in the application referenced");
		out.println("# process.count       Total number of processes started");
		out.println("# cluster.name        Name of cluster to run application on");
		out.println("# cluster.*           All valid entries for a cluster, overriding any");
		out.println("#                     specified in the cluster referenced");
		out.println("# resource.count      Number of machines used on the cluster");

		out.println("# runtime             Maximum runtime of job (in minutes)");

		out.println("# pool.name           Pool name. Defaults to name of experiment if unspecified");
		out.println("# pool.size           Size of pool. Only used in a closed-world application");
	}

	// name of job
	private String name;

	private final Application application;

	private int processCount;

	private final Cluster cluster;

	private int resourceCount;

	private int runtime;

	private String poolName;

	private int poolSize;

	/**
	 * Create an empty job description
	 */
	JobDescription() {
		name = "anonymous";
		application = new Application();
		processCount = 0;
		cluster = new Cluster();
		resourceCount = 0;
		runtime = 0;
		poolName = null;
		poolSize = 0;
	}

	/**
	 * Creates a new job with the given name.
	 * 
	 * @param name
	 *            the name of the job
	 * @throws Exception
	 *             if name is unspecified, or contains periods or spaces
	 */
	public JobDescription(String name) throws Exception {
		this();
		setName(name);
	}

	public void loadFromProperties(DeployProperties properties, String prefix)
			throws Exception {

		// add separator to prefix
		prefix = prefix + ".";

		String applicationName = properties.getProperty(prefix
				+ "application.name");

		application.setName(applicationName);
		application.setFromProperties(properties, prefix + "application");

		processCount = properties.getIntProperty(prefix + "process.count", 0);

		String clusterName = properties.getProperty(prefix + "cluster.name");
		cluster.setName(clusterName);
		cluster.loadFromProperties(properties, prefix + "cluster");

		resourceCount = properties.getIntProperty(prefix + "resource.count", 0);

		runtime = properties.getIntProperty(prefix + "runtime", 0);

		poolName = properties.getProperty(prefix + "pool.name");

		poolSize = properties.getIntProperty(prefix + "pool.size", 0);

	}

	/**
	 * Returns the name of this job.
	 * 
	 * @return the name of this job.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this job.
	 * 
	 * @param name
	 *            the name of this job.
	 * 
	 * @throws Exception
	 *             if name is null, or contains periods or spaces, or a job with
	 *             the given name already exists.
	 */
	public void setName(String name) throws Exception {
		if (name == null) {
			throw new Exception("no name specified for job");
		}

		if (name.equals(this.name)) {
			// name unchanged
			return;
		}

		if (name.contains(".")) {
			throw new Exception("job name cannot contain periods : \"" + name
					+ "\"");
		}
		if (name.contains(" ")) {
			throw new Exception("job name cannot contain spaces : \"" + name
					+ "\"");
		}

		this.name = name;
	}

	/**
	 * Returns application object used for "overriding" application settings.
	 * 
	 * @return application object used for "overriding" application settings.
	 */
	public Application getApplication() {
		return application;
	}

	/**
	 * Total number of times application is started.
	 * 
	 * @return Total number of processes in this job. Returns 0 if unknown.
	 */
	public int getProcessCount() {
		return processCount;
	}

	/**
	 * Sets total number of times application is started.
	 * 
	 * @param processCount
	 *            total number of processes in this job, 0 for unknown.
	 */
	public void setProcessCount(int processCount) {
		this.processCount = processCount;
	}

	/**
	 * Returns cluster object used for "overriding" cluster settings.
	 * 
	 * @return cluster object used for "overriding" cluster settings.
	 */
	public Cluster getCluster() {
		return cluster;
	}

	/**
	 * Total number of resources used for this job.
	 * 
	 * @return Total number of machines used on the specified cluster. Returns 0
	 *         if unknown
	 */
	public int getResourceCount() {
		return resourceCount;
	}

	/**
	 * Sets total number of resources used for this job.
	 * 
	 * @param resourceCount
	 *            number of machines used on the specified cluster, or 0 for
	 *            unknown.
	 */
	public void setResourceCount(int resourceCount) {
		this.resourceCount = resourceCount;
	}

	/**
	 * Maximum runtime of this job in minutes.
	 * 
	 * @return maximum runtime of this job in minutes. Returns 0 if unset.
	 */
	public int getRuntime() {
		return runtime;
	}

	/**
	 * Sets the maximum runtime of this job in minutes.
	 * 
	 * @param runtime
	 *            maximum runtime of this job in minutes., or 0 for not
	 *            specified.
	 */
	public void setRuntime(int runtime) {
		this.runtime = runtime;
	}

	/**
	 * Returns the name of the pool of this job. If null, the name of the
	 * experiment is used by default.
	 * 
	 * @return the name of the pool of this job.
	 */
	public String getPoolName() {
		return poolName;
	}

	/**
	 * Sets the name of the pool of this job. If null, the name of the
	 * experiment is used by default.
	 * 
	 * @param poolName
	 *            the new name of the pool of this job.
	 */
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	/**
	 * Returns the size of the pool of this job. Only used in closed-world
	 * applications, defaults to 0 for "unknown"
	 * 
	 * @return the size of the pool of this job.
	 */
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * Sets the size of the pool of this job. Only used in closed-world
	 * applications, defaults to 0 for "unknown"
	 * 
	 * @param poolSize
	 *            the new size of the pool of this job.
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * Checks if this description is suitable for deploying. If not, throws an
	 * exception.
	 * 
	 * @throws Exception
	 *             if this description is incomplete or incorrect.
	 */
	public void checkSettings() throws Exception {
		String prefix = "Cannot run job \"" + name + "\": ";

		if (name == null) {
			throw new Exception("Cannot run job: Job name unspecified");
		}

		if (application == null) {
			throw new Exception(prefix + "Application overrides not specified");
		}

		application.checkSettings(name);

		if (processCount <= 0) {
			throw new Exception(prefix + "Process count zero or negative");
		}

		if (cluster == null) {
			throw new Exception(prefix + "Cluster overrides not specified");
		}

		cluster.checkSettings(name, false);

		if (resourceCount < 0) {
			throw new Exception(prefix + "Resource count negative");
		}

		if (runtime < 0) {
			throw new Exception(prefix + "Runtime negative");
		}

		if (poolName == null) {
			throw new Exception(prefix + "Pool name not specified");
		}

		if (poolSize < 0) {
			throw new Exception(prefix + "Pool size negative");
		}
	}

	/**
	 * Copy constructor
	 * 
	 * @param other
	 *            original description to get data from
	 */
	public JobDescription(JobDescription original) {
		this.name = original.name;
		this.processCount = original.processCount;
		this.resourceCount = original.resourceCount;
		this.runtime = original.runtime;
		this.poolName = original.poolName;
		this.poolSize = original.poolSize;

		this.application = new Application(original.application);
		this.cluster = new Cluster(original.cluster);
	}

	public JobDescription resolve(ApplicationSet applicationSet, Grid grid)
			throws Exception {
    	Application application = applicationSet.getApplication(getApplication().getName());
    	Cluster cluster = grid.getCluster(getCluster().getName());
    	
    	return resolve(application, cluster);
	}

	/**
	 * Resolves the stack of JobDescription/Application/Cluster objects into one
	 * new JobDescription with no dependencies. Ordering (highest priority
	 * first):
	 * 
	 * <ol>
	 * <li>Overrides and settings in this description</li>
	 * <li>Application settings in given Application</li>
	 * <li>Cluster settings in given Cluster</li>
	 * </ol>
	 * 
	 * @param applicationSet
	 *            applications to use for resolving settings.
	 * @param grid
	 *            clusters to use for resolving settings.
	 * 
	 * @return the resulting application, as a new object.
	 * @throws Exception
	 */
	public JobDescription resolve(Application application, Cluster cluster)
			throws Exception {
		JobDescription result = new JobDescription(this);

		if (application != null) {
			// fetch any unset settings from the given application
			result.application.append(application);
		}

		if (cluster != null) {
			// fetch any unset settings from the given cluster
			result.cluster.append(cluster);
		}

		return result;
	}

	/**
	 * Print the settings of this job to a (properties) file
	 * 
	 * @param out
	 *            stream to write this file to
	 * @param prefix
	 *            prefix to add to all keys, or null to use name of job.
	 * @throws Exception
	 *             if this job has no name
	 */
	public void save(PrintWriter out, String prefix, boolean ensureExists)
			throws Exception {
		boolean empty = true;

		if (prefix == null) {
			throw new Exception("cannot print job description to file,"
					+ " name is not specified");
		}

		String dotPrefix = prefix + ".";

		out.println(dotPrefix + "application.name = " + application.getName());
		application.save(out, dotPrefix + "application", false);

		if (processCount == 0) {
			out.println("#" + dotPrefix + "process.count =");
		} else {
			out.println(dotPrefix + "process.count = " + processCount);
			empty = false;
		}

		out.println(dotPrefix + "cluster.name = " + cluster.getName());
		cluster.save(out, dotPrefix + "cluster", false);

		if (resourceCount == 0) {
			out.println("#" + dotPrefix + "resource.count =");
		} else {
			out.println(dotPrefix + "resource.count = " + resourceCount);
			empty = false;
		}

		if (runtime == 0) {
			out.println("#" + dotPrefix + "runtime =");
		} else {
			out.println(dotPrefix + "runtime = " + runtime);
			empty = false;
		}

		if (poolName == null) {
			out.println("#" + dotPrefix + "pool.name =");
		} else {
			out.println(dotPrefix + "pool.name = " + poolName);
			empty = false;
		}

		if (poolSize == 0) {
			out.println("#" + dotPrefix + "pool.size =");
		} else {
			out.println(dotPrefix + "pool.size = " + poolSize);
			empty = false;
		}

		if (empty && ensureExists) {
			out.println("#Dummy property to make sure job is actually defined");
			out.println(prefix);
		}
	}

	/**
	 * Returns a newline separated string useful for printing.
	 * 
	 * @return a newline separated string useful for printing.
	 */
	public String toPrintString() {
		String result = "Job \"" + getName() + "\"\n";
		result += "Generic Settings:\n";
		result += " Process Count = " + getProcessCount() + "\n";
		result += " Resource Count = " + getResourceCount() + "\n";
		result += " Runtime = " + getRuntime() + "\n";
		result += " Pool Name = " + getPoolName() + "\n";
		result += " Pool Size = " + getPoolSize() + "\n";
		result += application.toPrintString();
		result += cluster.toPrintString();

		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

}
