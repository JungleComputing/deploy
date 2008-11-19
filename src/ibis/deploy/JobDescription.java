package ibis.deploy;

import ibis.util.TypedProperties;

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
        out
                .println("# application.*       All valid entries for an application, overriding any");
        out
                .println("#                     specified in the application referenced");
        out.println("# process.count       Total number of processes started");
        out
                .println("# cluster.name        Name of cluster to run application on");
        out
                .println("# cluster.*           All valid entries for a cluster, overriding any");
        out
                .println("#                     specified in the cluster referenced");
        out
                .println("# resource.count      Number of machines used on the cluster");
        out
                .println("# pool.name           Pool name. Defaults to name of experiment if unspecified");
        out
                .println("# pool.size           Size of pool. Only used in a closed-world application");

        out
                .println("# shared.hub          if \"true\" (or unset), this job shares a hub with other");
        out
                .println("#                     jobs on this cluster. If \"false\" a hub is started");
        out.println("#                     especially for it");
    }

    // experiment this job belongs to
    private final Experiment parent;

    // name of job
    private String name;

    private String applicationName;

    private final Application applicationOverrides;

    private int processCount;

    private String clusterName;

    private final Cluster clusterOverrides;

    private int resourceCount;

    private String poolName;

    private int poolSize;

    private Boolean sharedHub;

    public JobDescription() throws Exception {
        name = null;
        parent = null;
        applicationName = null;
        applicationOverrides = new Application();
        processCount = 0;
        clusterName = null;
        clusterOverrides = new Cluster();
        resourceCount = 0;
        poolName = null;
        poolSize = 0;
        sharedHub = null;
    }

    /**
     * Creates a new job with the given name. Jobs cannot be created directly,
     * but are constructed by a parent Experiment object.
     * 
     * @param name
     *            the name of the job
     * @throws Exception
     *             if the name given is <code>null</code>
     */

    JobDescription(String name, Experiment parent) throws Exception {
        this.parent = parent;

        if (name == null) {
            throw new Exception("no name specified for job");
        }
        this.name = name;

        applicationName = null;
        applicationOverrides = new Application();
        processCount = 0;
        clusterName = null;
        clusterOverrides = new Cluster();
        resourceCount = 0;
        poolName = null;
        poolSize = 0;
        sharedHub = null;
    }

    /**
     * Load job from the given properties (usually loaded from an experiment
     * file)
     * 
     * @param properties
     *            properties to load job from
     * @param object
     *            name of this job, or null to load "defaults" job
     * @throws Exception
     *             if job cannot be read properly
     */

    JobDescription(TypedProperties properties, String name, String prefix,
            Experiment parent) throws Exception {
        this.parent = parent;
        this.name = name;

        // add separator to prefix
        prefix = prefix + ".";

        applicationName = properties.getProperty(prefix + "application.name");

        applicationOverrides = new Application(properties, null, prefix
                + "application", null);

        processCount = properties.getIntProperty(prefix + "process.count", 0);

        clusterName = properties.getProperty(prefix + "cluster.name");

        clusterOverrides = new Cluster(properties, null, prefix
                + "cluster", null);

        resourceCount = properties.getIntProperty(prefix + "resource.count", 0);

        poolName = properties.getProperty(prefix + "pool.name");

        poolSize = properties.getIntProperty(prefix + "pool.size", 0);

        if (!properties.containsKey("shared.hub")) {
            sharedHub = null;
        } else {
            sharedHub = properties.getBooleanProperty(prefix + "shared.hub");
        }
    }

    // overwrites values with all non-null fields from "other" except
    // application and cluster overwrites
    private void overwrite(JobDescription other) {
        if (other == null) {
            return;
        }

        if (other.applicationName != null) {
            this.applicationName = other.applicationName;
        }

        if (other.processCount != 0) {
            this.processCount = other.processCount;
        }

        if (other.clusterName != null) {
            this.clusterName = other.clusterName;
        }

        if (other.resourceCount != 0) {
            this.resourceCount = other.resourceCount;
        }

        if (other.poolName != null) {
            this.poolName = other.poolName;
        }

        if (other.poolSize != 0) {
            this.poolSize = other.poolSize;
        }

        if (other.sharedHub != null) {
            this.sharedHub = other.sharedHub;
        }
    }

    /**
     * Returns the name of the experiment containing this job. Also the default
     * name of the pool of this job.
     * 
     * @return the name of the experiment containing this job, or null if it
     *         does not exist.
     */
    public String getExperimentName() {
        if (parent == null) {
            return null;
        }
        return parent.getName();
    }

    /**
     * Returns the experiment containing this job.
     * 
     * @return the experiment containing this job.
     */
    public Experiment getExperiment() {
        return parent;
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
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the application run.
     * 
     * @return the name of the application run.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets the name of the application run.
     * 
     * @param applicationName
     *            the name of the application run.
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Returns application object used for "overriding" application settings.
     * 
     * @return application object used for "overriding" application settings.
     */
    public Application getApplicationSettings() {
        return applicationOverrides;
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
     * Returns the name of the cluster used.
     * 
     * @return the name of the cluster used.
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * Sets the name of the cluster used.
     * 
     * @param cluster
     *            the name of the cluster used.
     */
    public void setClusterName(String cluster) {
        this.clusterName = cluster;
    }

    /**
     * Returns cluster object used for "overriding" cluster settings.
     * 
     * @return cluster object used for "overriding" cluster settings.
     */
    public Cluster getClusterSettings() {
        return clusterOverrides;
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
     * Returns if this job uses a shared hub (default), or one started
     * especially for it.
     * 
     * @return if this job uses a shared hub (default), or one started
     *         especially for it. Maybe null (unknown)
     */
    public Boolean getSharedHub() {
        return sharedHub;
    }

    /**
     * Sets if this job uses a shared hub, or one is started especially for it.
     * 
     * @param sharedHub
     *            Does this job use a shared hub or not. Use null for "unknown"
     */
    public void setSharedHub(Boolean sharedHub) {
        this.sharedHub = sharedHub;
    }

    /**
     * Checks if this description is suitable for deploying. If not, throws an
     * exception.
     * @throws Exception if this description is incomplete or incorrect.
     */
    public void checkSettings() throws Exception {
        String prefix = "Cannot run job \"" + name + "\": ";
        
        if (name == null) {
            throw new Exception("Cannot run job: Job name unspecified");
        }
        
        if (applicationName == null) {
            throw new Exception(prefix + "Application name not specified");
        }
        
        if (applicationOverrides == null) {
            throw new Exception(prefix + "Application overrides not specified");
        }
        
        applicationOverrides.checkSettings(name);
        
        if (processCount <= 0) {
            throw new Exception(prefix + "Process count zero or negative");
        }

        if (clusterName == null) {
            throw new Exception(prefix + "Cluster name not specified");
        }

        if (clusterOverrides == null) {
            throw new Exception(prefix + "Cluster overrides not specified");
        }
        
        clusterOverrides.checkSettings(name, false);

        if (resourceCount <= 0) {
            throw new Exception(prefix + "Resource count zero or negative");
        }

        if (poolName == null) {
            throw new Exception(prefix + "Pool name not specified");
        }
        
        if (poolSize < 0) {
            throw new Exception(prefix + "Pool size negative");
        }
    }

    /**
     * Resolves the stack of
     * JobDescription/Application/ApplicationSet/Cluster/Grid objects into one
     * new JobDescription with no dependencies and parents. Ordering (highest
     * priority first):
     * 
     * <ol>
     * <li>Overrides and settings in this description</li>
     * <li>Defaults in parent experiment</li>
     * <li>Application settings in given ApplicationSet</li>
     * <li>Default settings in given ApplicationSet</li>
     * <li>Cluster settings in given Grid
     * <li>Default settings in given Grid</li>
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
    public JobDescription resolve(ApplicationSet applicationSet, Grid grid)
            throws Exception {
        JobDescription result = new JobDescription(name, null);

        // first we get all fields except the application and cluster objects,
        // we need to know the "applicationName" and "clusterName" to resolve
        // those

        if (parent != null) {
            result.overwrite(parent.getDefaults());
            result.setPoolName(parent.getName());
        }
        result.overwrite(this);

        // next, get all settings from the specified application and cluster

        if (applicationSet != null) {
            // overwrite application defaults with application group defaults
            result.applicationOverrides.overwrite(applicationSet.getDefaults());

            // add application settings
            Application application = applicationSet.getApplication(result
                    .getApplicationName());
            result.applicationOverrides.overwrite(application);
        }

        if (grid != null) {
            // overwrite cluster with defaults from grid
            result.clusterOverrides.overwrite(grid.getDefaults());

            // add cluster settings
            Cluster cluster = grid.getCluster(result.getClusterName());
            result.clusterOverrides.overwrite(cluster);
        }

        // add defaults from parent
        if (parent != null) {
            result.applicationOverrides
                    .overwrite(parent.getDefaults().applicationOverrides);
            result.clusterOverrides
                    .overwrite(parent.getDefaults().clusterOverrides);
        }

        // add settings from this object
        result.applicationOverrides.overwrite(this.applicationOverrides);
        result.applicationOverrides.setName(result.getApplicationName());
        result.clusterOverrides.overwrite(this.clusterOverrides);
        result.clusterOverrides.setName(result.getClusterName());

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
    public void save(PrintWriter out, String prefix) throws Exception {
        if (prefix == null && (name == null || name.length() == 0)) {
            throw new Exception("cannot print job description to file,"
                    + " name is not specified");
        }

        if (prefix == null) {
            prefix = name + ".";
        } else {
            prefix = prefix + ".";
        }

        if (applicationName == null) {
            out.println("#" + prefix + "application.name =");
        } else {
            out.println(prefix + "application.name = " + applicationName);
        }

        applicationOverrides.save(out, prefix + "application", false);

        if (processCount == 0) {
            out.println("#" + prefix + "process.count =");
        } else {
            out.println(prefix + "process.count = " + processCount);
        }

        if (clusterName == null) {
            out.println("#" + prefix + "cluster.name =");
        } else {
            out.println(prefix + "cluster.name = " + clusterName);
        }

        clusterOverrides.save(out, prefix + "cluster", false);

        if (resourceCount == 0) {
            out.println("#" + prefix + "resource.count =");
        } else {
            out.println(prefix + "resource.count = " + resourceCount);
        }

        if (poolName == null) {
            out.println("#" + prefix + "pool.name =");
        } else {
            out.println(prefix + "pool.name = " + poolName);
        }

        if (poolSize == 0) {
            out.println("#" + prefix + "pool.size =");
        } else {
            out.println(prefix + "pool.size = " + poolSize);
        }

        if (sharedHub == null) {
            out.println("#" + prefix + "shared.hub =");
        } else {
            out.println(prefix + "shared.hub = " + sharedHub);
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
        result += " Application Name = " + getApplicationName() + "\n";
        result += " Process Count = " + getProcessCount() + "\n";
        result += " Cluster Name = " + getClusterName() + "\n";
        result += " Resource Count = " + getResourceCount() + "\n";
        result += " Pool Name = " + getPoolName() + "\n";
        result += " Pool Size = " + getPoolSize() + "\n";
        result += " Shared Hub = " + getSharedHub() + "\n";
        result += applicationOverrides.toPrintString();
        result += clusterOverrides.toPrintString();

        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name + "@" + getExperimentName();
    }

}
