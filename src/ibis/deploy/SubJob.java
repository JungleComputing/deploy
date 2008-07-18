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
import org.gridlab.gat.security.SecurityContext;

public class SubJob implements MetricListener {
    private static Logger logger = Logger.getLogger(SubJob.class);

    private static final int DEFAULT_RUNTIME = 20; // minutes

    private Job parent;

    private String wrapperExecutable;

    private String[] wrapperArguments;

    private String name;

    private String poolID;

    private int nodes;

    private Application application;

    private String[] attributes;

    private String[] preferences;

    private long runtime; // minutes

    private Grid grid;

    private Cluster cluster;

    private int cores;

    private boolean closedWorld;

    private JobState status = JobState.INITIAL;

    private Server hub;

    /**
     * Create a {@link SubJob} with the name <code>subjobName</code>
     * 
     * @param subjobName
     *                the name of the {@link SubJob}
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
     * Gets the number of cores that the {@link SubJob} should run on.
     * 
     * @return the number of cores that the {@link SubJob} should run on.
     */
    public int getCores() {
        if (cores <= 0) {
            if (nodes > 0) {
                return nodes;
            } else {
                return 1;
            }
        }
        return cores;
    }

    /**
     * Gets the {@link Grid} where the {@link SubJob} will run if deployed
     * 
     * @return the {@link Grid} where the {@link SubJob} will run if deployed
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Gets the name of this {@link Grid}
     * 
     * @return the name of this {@link Grid}
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of nodes that the {@link SubJob} should run on.
     * 
     * @return the number of nodes that the {@link SubJob} should run on.
     */
    public int getNodes() {
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
     * Gets a {@link HashMap} containing the JavaGAT preferences for this
     * {@link SubJob}.
     * 
     * @return a {@link HashMap} containing the JavaGAT preferences for this
     *         {@link SubJob}.
     */
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

    /**
     * Gets the runtime in minutes
     * 
     * @return the runtime in minutes
     */
    public long getRuntime() {
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

    /**
     * Gets the wrapper arguments for this {@link SubJob}.
     * 
     * @return the wrapper arguments for this {@link SubJob}.
     */
    public String[] getWrapperArguments() {
        return wrapperArguments;
    }

    /**
     * Gets the wrapper executable for this {@link SubJob}.
     * 
     * @return the wrapper executable for this {@link SubJob}.
     */
    protected String getWrapperExecutable() {
        return wrapperExecutable;
    }

    protected boolean hasExecutable() {
        return wrapperExecutable != null;
    }

    /**
     * Returns whether this {@link SubJob} is a closed world {@link SubJob}.
     * 
     * @return whether this {@link SubJob} is a closed world {@link SubJob}
     */
    public boolean isClosedWorld() {
        return closedWorld;
    }

    /**
     * Loads a {@link SubJob} specified by its name and from a
     * {@link TypedProperties} object and a set of {@link Grid}s and a set of
     * {@link Application}s. The following properties can be set:
     * 
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="subjob properties">
     * <CAPTION>subjob properties </CAPTION> <COLGROUP align="left"> <COLGROUP
     * align="center"> <COLGROUP align="left" > <THEAD valign="top">
     * <TR>
     * <TH>Property
     * <TH>Example
     * <TH>Description<TBODY>
     * <TR>
     * <TD>[[job.]subjob.]application
     * <TD>'application name'
     * <TD>The name of the application that should be run
     * <TR>
     * <TD>[[job.]subjob.]grid
     * <TD>'grid name'
     * <TD>The name of the grid where the cluster where this subjob will run on
     * is part of
     * <TR>
     * <TD>[[job.]subjob.]cluster
     * <TD>'cluster name'
     * <TD>The name of the cluster where this subjob will run on
     * <TR>
     * <TD>[[job.]subjob.]cores
     * <TD>'integer' | 'max' | 'percentage (e.g. 20 %)'
     * <TD>The total number of cores where the subjob will run on
     * <TR>
     * <TD>[[job.]subjob.]nodes
     * <TD>'integer' | 'max' | 'percentage (e.g. 20 %)'
     * <TD>The total number of nodes where the subjob will run on
     * <TR>
     * <TD>[[job.]subjob.]runtime
     * <TD>'runtime in minutes'
     * <TD>The runtime of this subjob in minutes
     * <TR>
     * <TD>[[job.]subjob.]pool.id
     * <TD>'pool id'
     * <TD>The pool id for this subjob (if different from the job)
     * <TR>
     * <TD>[[job.]subjob.]closed.world
     * <TD>'true' | 'false'
     * <TD>'true' if this subjob is a closed world job, 'false' otherwise
     * <TR>
     * <TD>[[job.]subjob.]gat.attributes
     * <TD>key=value key2=value2
     * <TD>javagat softwaredescription attributes
     * <TR>
     * <TD>[[job.]subjob.]gat.preferences
     * <TD>key=value key2=value2
     * <TD>javagat preferences for the resourcebroker
     * <TR>
     * <TD>[[job.]subjob.]chunk.size
     * <TD>integer
     * <TD>split this subjob in multiple subjobs of this size (chunk.size of
     * the size of the subjobs, not the number of subjobs)
     * <TR>
     * <TD>[[job.]subjob.]wrapper.executable
     * <TD>'/bin/sh'
     * <TD>if this job shouldn't be started by directly invoking java, but by
     * invoking for instance a script, the executable for this script can be set
     * <TR>
     * <TD>[[job.]subjob.]wrapper.arguments
     * <TD>'script.sh argument1 argument2'
     * <TD>if this job shouldn't be started by directly invoking java, but by
     * invoking for instance a script, the arguments for the executable for this
     * script can be set </TABLE>
     * <p>
     * 
     * @param runprops
     *                the {@link TypedProperties} where the {@link SubJob} will
     *                be loaded from
     * @param grids
     *                the set of {@link Grid}s that's used to load the
     *                {@link SubJob}
     * @param applications
     *                the set of {@link Application}s that's used to load the
     *                {@link SubJob}
     * @param subjobName
     *                the name of the {@link SubJob}
     * @return an array of loaded {@link SubJob}s (can be more than 1
     *         {@link SubJob}, due to chunk.size)
     * @throws Exception
     *                 if the gridname, clustername or applicationname cannot be
     *                 found in the provided sets
     */
    public static SubJob[] load(TypedProperties runprops, Set<Grid> grids,
            Set<Application> applications, String subjobName) throws Exception {
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
            throw new Exception("grid '" + gridName
                    + "' unknown, but used in subjob '" + subjob + "'");
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
            throw new Exception("cluster '" + clusterName
                    + "' not found in grid '" + gridName
                    + "', but used in subjob '" + subjob + "'");
        }
        String nodesString = TypedPropertiesUtility.getHierarchicalProperty(
                runprops, subjobName, "nodes", "-1");
        if (nodesString.equals("max")) {
            subjob.nodes = subjob.cluster.getTotalNodes();
        } else if (nodesString.matches("\\d+?\\s*?%")) {
            Pattern pattern = Pattern.compile("\\d*+");
            Matcher matcher = pattern.matcher(nodesString);
            if (matcher.find()) {
                int percentage = Integer.parseInt(matcher.group());
                subjob.nodes = (int) ((subjob.cluster.getTotalNodes() * percentage) / 100.0);
            }
        } else {
            subjob.nodes = Integer.parseInt(nodesString);
        }
        String coresString = TypedPropertiesUtility.getHierarchicalProperty(
                runprops, subjobName, "cores", "-1");
        if (coresString.equals("max")) {
            subjob.cores = subjob.cluster.getTotalCores();
        } else {
            subjob.cores = Integer.parseInt(coresString);
        }
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
        subjob.wrapperExecutable = TypedPropertiesUtility
                .getHierarchicalProperty(runprops, subjobName,
                        "wrapper.executable", subjob.cluster
                                .getApplicationWrapperExecutable());
        subjob.wrapperArguments = TypedPropertiesUtility
                .getHierarchicalStringList(runprops, subjobName,
                        "wrapper.arguments", subjob.cluster
                                .getApplicationWrapperArguments(), " ");
        String applicationString = TypedPropertiesUtility
                .getHierarchicalProperty(runprops, subjobName, "application",
                        null);
        for (Application application : applications) {
            if (application.getName().equalsIgnoreCase(applicationString)) {
                subjob.application = application;
                break;
            }
        }

        if (subjob.application == null) {
            throw new Exception("application '" + applicationString
                    + "' not found, but used in subjob '" + subjob + "'");
        }

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

    /**
     * Sets the {@link Application} for this {@link SubJob}
     * 
     * @param application
     *                the {@link Application} to be used.
     */
    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * Sets the gat attributes for this {@link SubJob}
     * 
     * @param attributes
     *                the attributes to be used.
     */
    public void setAttributes(String... attributes) {
        this.attributes = attributes;
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
     * Sets the {@link Cluster} for this {@link SubJob}
     * 
     * @param cluster
     *                the cluster where the {@link SubJob} should run on.
     */
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Sets the number of cores where this {@link SubJob} should run on.
     * 
     * @param cores
     *                the number of cores where this {@link SubJob} should run
     *                on.
     */
    public void setCores(int cores) {
        this.cores = cores;
    }

    /**
     * Sets the {@link Grid} for this {@link SubJob}
     * 
     * @param grid
     *                the {@link Grid} for this {@link SubJob}
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    /**
     * Sets the number of nodes for this {@link SubJob}
     * 
     * @param nodes
     *                the number of nodes for this {@link SubJob}
     */
    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    protected void setParent(Job parent) {
        this.parent = parent;
    }

    /**
     * Sets the poolID for this {@link SubJob}
     * 
     * @param poolID
     *                the poolID to be used
     */
    public void setPoolID(String poolID) {
        this.poolID = poolID;
    }

    /**
     * Sets the javagat preferences for this {@link SubJob}
     * 
     * @param preferences
     *                the javagat preferences for this {@link SubJob}
     */
    public void setPreferences(String... preferences) {
        this.preferences = preferences;
    }

    /**
     * Sets the runtime for this {@link SubJob}
     * 
     * @param runtime
     *                the runtime in minutes
     */
    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    /**
     * Sets the additional arguments for the wrapper executable that should be
     * run
     * 
     * @param wrapperArguments
     *                the additional arguments for the wrapper executable that
     *                should be run
     */
    public void setWrapperArguments(String... wrapperArguments) {
        this.wrapperArguments = wrapperArguments;
    }

    /**
     * Sets the wrapper executable
     * 
     * @param wrapperExecutable
     *                the wrapper executable
     */
    public void setWrapperExecutable(String wrapperExecutable) {
        this.wrapperExecutable = wrapperExecutable;
    }

    /**
     * Submits the subjob with the given poolID, poolSize, serverAddress and
     * hubAddresses. The output will be staged to the provided output directory.
     * 
     * @param poolID
     * @param poolSize
     * @param serverAddress
     * @param hubAddress
     * @param outputDirectory
     * @throws GATObjectCreationException
     * @throws URISyntaxException
     * @throws GATInvocationException
     */
    public void submit(String poolID, int poolSize, String serverAddress,
            String hubAddress, String outputDirectory)
            throws GATObjectCreationException, URISyntaxException,
            GATInvocationException {
        if (logger.isInfoEnabled()) {
            logger.info("submitting sub job " + name);
        }
        Preferences preferences = new Preferences();
        preferences.put("ResourceBroker.adaptor.name", cluster
                .getApplicationBrokerAdaptors());
        preferences.put("File.adaptor.name", cluster
                .getApplicationFileAdaptors());
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
        sd.addAttribute("walltime.max", getRuntime());
        JobDescription jd = null;

        if (!hasExecutable()) {
            jd = new JobDescription(sd);
            jd.setProcessCount(getCores());
            jd.setResourceCount(getNodes());
        } else {
            logger.debug("executable = " + getWrapperExecutable()
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
            nonJava.setExecutable(getWrapperExecutable());
            List<String> argumentList = new ArrayList<String>();
            if (getWrapperArguments() != null) {
                for (String arg : getWrapperArguments()) {
                    argumentList.add(arg);
                }
            }

            argumentList.add("" + getNodes());
            argumentList.add("" + getCores());

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
        SecurityContext securityContext = new CertificateSecurityContext(null,
                null, cluster.getUserName(), null);
        securityContext.addNote("adaptors", "commandlinessh,sshtrilead");
        context.addSecurityContext(securityContext);

        ResourceBroker broker = GAT.createResourceBroker(context, preferences,
                cluster.getApplicationBroker());
        logger.debug("submission of subjob '" + name
                + "' with job description:\n" + jd);
        broker.submitJob(jd, this, "job.status");

    }

    public void setHub(Server hub) {
        this.hub = hub;
    }

    public Server getHub() {
        return hub;
    }

    public String toString() {
        String gridName = "null";
        if (grid != null) {
            gridName = grid.getGridName();
        }

        String clusterName = "null";
        if (cluster != null) {
            clusterName = cluster.getName();
        }

        return "SubJob " + name + ": " + gridName + " " + clusterName + " "
                + nodes + " nodes, with " + (cores / nodes)
                + " cores/nodes, for a total of " + cores + " cores";
    }
}