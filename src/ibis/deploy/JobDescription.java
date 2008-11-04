package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.PrintWriter;

/**
 * Single job in an experiment.
 * 
 * @author ndrost
 * 
 */
public class JobDescription {

    // name of job
    private String name;

    // experiment this job belongs to
    private final Experiment parent;

    private String applicationName;

    private final Application applicationOverrides;

    private String clusterName;

    private final Cluster clusterOverrides;

    private int processCount;

    private int resourceCount;
    
    private String poolName;

    private Boolean sharedHub;

    JobDescription() {
        name = null;
        parent = null;
        applicationName = null;
        applicationOverrides = new Application();
        clusterName = null;
        clusterOverrides = new Cluster();
        processCount = 0;
        resourceCount = 0;
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
        clusterName = null;
        clusterOverrides = new Cluster();
        processCount = 0;
        resourceCount = 0;
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
    JobDescription(TypedProperties properties, String name, Experiment parent)
            throws Exception {
        this.parent = parent;
        this.name = name;

        String prefix;
        if (name == null) {
            prefix = "";
        } else {
            prefix = name + ".";
        }

        applicationName = properties.getProperty(prefix + "application");

        // FIXME: fill in
        applicationOverrides = new Application();

        processCount = properties.getIntProperty(prefix + "process.count", 0);

        clusterName = properties.getProperty(prefix + "cluster");

        // FIXME: fill in
        clusterOverrides = new Cluster();

        resourceCount = properties.getIntProperty(prefix + "resource.count", 0);
        if (!properties.containsKey("shared.hub")) {
            sharedHub = null;
        } else {
            sharedHub = properties.getBooleanProperty(prefix + "shared.hub");
        }
    }
    
    public String getExperimentName() {
        if (parent == null) {
            return null;
        }
        return parent.getName();
    }

    public Experiment getExperiment() {
        return parent;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    /**
     * Resolves the stack of
     * JobDescription/Application/ApplicationGroup/Cluster/Grid objects into one
     * new JobDescription with no dependencies and parents. Ordering (highest
     * priority first):
     * 
     * <ol>
     * <li>Overrides and settings in this description</li>
     * <li>Defaults in parent experiment</li>
     * <li>Application settings in given ApplicationGroup</li>
     * <li>Default settings in given ApplicationGroup</li>
     * <li>Cluster settings in given Grid
     * <li>Default settings in given Grid</li>
     * </ol>
     * 
     * @return the resulting application, as a new object.
     * @throws Exception
     */
    public JobDescription resolve(ApplicationGroup applicationGroup, Grid grid)
            throws Exception {
        JobDescription result = new JobDescription(name, null);

        // first we get all fields except the application and cluster objects,
        // we need to know the "applicationName" and "clusterName" to resolve
        // those

        if (parent != null) {
            result.overwrite(parent.getDefaults());
        }
        result.overwrite(this);

        // next, get all settings from the specified application and cluster

        if (applicationGroup != null) {
            // overwrite application defaults with application group defaults
            result.applicationOverrides.overwrite(applicationGroup
                    .getDefaults());

            // add application settings
            Application application = applicationGroup.getApplication(result
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
        result.clusterOverrides.overwrite(this.clusterOverrides);

        return result;
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

        if (other.clusterName != null) {
            this.clusterName = other.clusterName;
        }

        if (other.processCount != 0) {
            this.processCount = other.processCount;
        }

        if (other.resourceCount != 0) {
            this.resourceCount = other.resourceCount;
        }

        if (other.sharedHub != null) {
            this.sharedHub = other.sharedHub;
        }
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String application) {
        this.applicationName = application;
    }

    /**
     * Total number of processes in this job
     * 
     * @return Total number of processes in this job. Returns 0 if unknown
     */
    public int getProcessCount() {
        return processCount;
    }

    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String cluster) {
        this.clusterName = cluster;
    }

    /**
     * Total number of resources in this job
     * 
     * @return Total number of resources in this cluster. Returns 0 if unknown
     */
    public int getResourceCount() {
        return resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
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
     * Sets if this job uses a shared hub, or one is started especially
     * for it.
     * 
     * @param sharedHub
     *            Does this job use a shared hub or not. Use null for "unknown"
     */
    public void setSharedHub(Boolean sharedHub) {
        this.sharedHub = sharedHub;
    }

    /**
     * Print the settings of this job to a (properties) file
     * 
     * @param out
     *            stream to write this file to
     * @param prependName
     *            if true, key/value lines prepended with the job name
     * @throws Exception
     *             if this job has no name
     */
    public void print(PrintWriter out, boolean prependName) throws Exception {
        String prefix;

        if (prependName) {
            if (name == null || name.length() == 0) {
                throw new Exception("cannot print job to file,"
                        + " name is not specified");
            }
            prefix = name + ".";
        } else {
            prefix = "";
        }

        if (applicationName == null) {
            out.println("#" + prefix + "application =");
        } else {
            out.println(prefix + "application = " + applicationName);
        }

        if (processCount == 0) {
            out.println("#" + prefix + "process.count =");
        } else {
            out.println(prefix + "process.count = " + processCount);
        }

        if (clusterName == null) {
            out.println("#" + prefix + "cluster =");
        } else {
            out.println(prefix + "cluster = " + clusterName);
        }

        if (resourceCount == 0) {
            out.println("#" + prefix + "resource.count =");
        } else {
            out.println(prefix + "resource.count = " + resourceCount);
        }

        if (sharedHub == null) {
            out.println("#" + prefix + "shared.hub =");
        } else {
            out.println(prefix + "shared.hub = " + sharedHub);
        }
    }

    public String toPrintString() {
        String result = "Job " + getName() + "\n";
        result += " Application = " + getApplicationName() + "\n";

        result += " Process Count = " + getProcessCount() + "\n";
        result += " Cluster = " + getClusterName() + "\n";
        result += " Resource Count = " + getResourceCount() + "\n";
        result += " Shared Hub = " + getSharedHub() + "\n";

        return result;
    }

    public String toString() {
        return name + "@" + getExperimentName();
    }

}
