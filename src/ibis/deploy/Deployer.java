package ibis.deploy;

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
import org.gridlab.gat.resources.SoftwareDescription;

public class Deployer implements MetricListener {

    private static Logger logger = Logger.getLogger(Deployer.class);

    private Set<Application> applications = new HashSet<Application>();

    private Map<String, List<org.gridlab.gat.resources.Job>> runningJobs = new HashMap<String, List<org.gridlab.gat.resources.Job>>();

    private Set<Grid> grids = new HashSet<Grid>();

    private URI serverURI;

    private Hashtable<URI, String> existingHubs = new Hashtable<URI, String>();

    private Cluster serverCluster;

    /**
     * Creates a deployer. The deployments with this deployer have a server on
     * the localhost.
     */
    public Deployer() {
        Cluster cluster = null;
        try {
            cluster = new Cluster("server", new URI("any://localhost"));
        } catch (URISyntaxException e) {
        }
        cluster.setAccessType("local");
        cluster.setFileAccessType("local");
        cluster.setJavapath(System.getProperty("java.home"));
        this.serverCluster = cluster;
    }

    /**
     * Creates a deployer. The deployments with this deployer have a server
     * which will be submitted to the deploy broker of the cluster.
     * 
     * @param serverCluster
     *            the cluster to be used to deploy the server on
     */
    public Deployer(Cluster serverCluster) {
        if (logger.isInfoEnabled()) {
            logger.info("constructor");
        }
        this.serverCluster = serverCluster;
    }

    /**
     * Adds a {@link Set} of {@link Application}s to this deployer. These
     * Applications can be referred to by a run, a {@link Job} or a
     * {@link SubJob}.
     * 
     * @param applications
     *            the {@link Application}s to be added.
     */
    public void addApplications(Set<Application> applications) {
        this.applications.addAll(applications);
    }

    /**
     * Adds a single {@link Application} to this deployer. This Application can
     * be referred to by a run, a {@link Job} or a {@link SubJob}.
     * 
     * @param application
     *            the {@link Application} to be added.
     */
    public void addApplication(Application application) {
        this.applications.add(application);
    }

    /**
     * Adds all {@link Application}s described in a properties file to this
     * deployer. These Applications can be referred to by a run, a {@link Job}
     * or a {@link SubJob}.
     * 
     * @param applicationFileName
     *            the properties file describing the {@link Application}s to be
     *            added.
     * @throws FileNotFoundException
     *             if the file cannot be found
     * @throws IOException
     *             if an IO error occurs during the loading from the file.
     */
    public void addApplications(String applicationFileName)
            throws FileNotFoundException, IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(applicationFileName));
        addApplications(properties);
    }

    /**
     * Adds all {@link Application}s described in a the {@link TypedProperties}
     * object to this deployer. These Applications can be referred to by a run,
     * a {@link Job} or a {@link SubJob}.
     * 
     * @param properties
     *            the properties describing the {@link Application}s to be
     *            added.
     */
    public void addApplications(TypedProperties properties) {
        properties.expandSystemVariables();
        addApplications(Application.load(properties));
    }

    /**
     * Adds a {@link Grid} to this deployer. This Grid can be referred to by a
     * run, a {@link Job} or a {@link SubJob}.
     * 
     * @param grid
     *            the {@link Grid} to be added
     */
    public void addGrid(Grid grid) {
        this.grids.add(grid);
    }

    /**
     * Adds a {@link Grid} described in a properties file to this deployer. This
     * Grid can be referred to by a run, a {@link Job} or a {@link SubJob}.
     * 
     * @param gridFileName
     *            the properties file describing the {@link Grid} to be added.
     * @throws FileNotFoundException
     *             if the file cannot be found
     * @throws IOException
     *             if an IO error occurs during the loading from the file.
     */
    public void addGrid(String gridFileName) throws FileNotFoundException,
            IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(gridFileName));
        addGrid(properties);
    }

    /**
     * Adds a {@link Grid} described in a {@link TypedProperties} object to this
     * deployer. This Grid can be referred to by a run, a {@link Job} or a
     * {@link SubJob}.
     * 
     * @param properties
     *            the properties object describing the {@link Grid} to be added.
     */
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

    /**
     * Deploys a {@link Job}. In order to deploy a job, first a server is
     * started at the location defined at the construction of this deployer.
     * Then the hubs are started and finally all {@link SubJob}s are started.
     * The server and hubs will live as long as the job lives. All sub jobs will
     * run in a single pool.
     * 
     * @param job
     *            the job to be deployed
     * @throws Exception
     *             if the deployment fails
     */
    public void deploy(Job job) throws Exception {
        // start server
        job.setDeployer(this);
        startExternalServer(serverCluster, job.getName(), job
                .getFirstApplication());
        URI[] hubURIs = job.getHubURIs();
        if (hubURIs != null) {
            for (URI hubURI : hubURIs) {
                if (hubURI != null) {
                    startHub(hubURI, job.getName(), job.getFirstApplication());
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

    /**
     * Deploys a {@link SubJob}. In order to deploy a sub job, first a server
     * is started at the location defined at the construction of this deployer.
     * Then the hubs are started and finally the sub job is started. The server
     * and hubs will live as long as the sub job lives.
     * 
     * @param subjob
     *            the job to be deployed
     * @throws Exception
     *             if the deployment fails
     */
    public void deploy(SubJob subjob) throws Exception {
        subjob.setDeployer(this);
        startExternalServer(serverCluster, subjob.getName(), subjob
                .getApplication());
        URI hubURI = subjob.getHubURI();
        if (hubURI != null) {
            startHub(hubURI, subjob.getName(), subjob.getApplication());
        }
        if (subjob.getPoolID() == null) {
            subjob.setPoolID(generatePoolID());
        }
        submitSubJob(subjob, subjob.getPoolID(), subjob.getPoolSize());
    }

    /**
     * Adds a {@link SubJob} to a {@link Job}. The server will be or is already
     * started by the Job, the sub job only starts a hub and then will run the
     * sub job.
     * 
     * @param subjob
     *            the sub job to be deployed in the same pool as the job
     * @param job
     *            the job with the pool where the sub job joins
     * @throws Exception
     *             if the deployment fails
     */
    public void deploy(SubJob subjob, Job job) throws Exception {
        if (job.isClosedWorld() || subjob.isClosedWorld()) {
            throw new Exception("Cannot add to a closed world!");
        }
        URI hubURI = subjob.getHubURI();
        if (hubURI != null) {
            startHub(hubURI, subjob.getName(), job.getFirstApplication());
        }
        if (job.getPoolID() == null) {
            job.setPoolID(generatePoolID());
        }
        job.addSubJob(subjob);
        submitSubJob(subjob, job.getPoolID(), -1);
    }

    protected void end(String name) {
        // gets all gat jobs belonging to name and cancels them
        List<org.gridlab.gat.resources.Job> jobs = runningJobs.remove(name);
        for (org.gridlab.gat.resources.Job job : jobs) {
            cancelJob(job);
        }
    }

    /**
     * Ends all jobs and closes all open connections.
     */
    public void end() {
        GAT.end();
    }

    private String generatePoolID() {
        if (logger.isInfoEnabled()) {
            logger.info("generate pool id");
        }
        // TODO: better
        return "pool-id-" + Math.random();
    }

    /**
     * Returns the {@link Set} of {@link Application}s associated with this
     * deployer
     * 
     * @return the {@link Set} of {@link Application}s associated with this
     *         deployer
     */
    public Set<Application> getApplications() {
        return applications;
    }

    /**
     * Returns the {@link Set} of {@link Grid}s associated with this deployer
     * 
     * @return the {@link Set} of {@link Grid}s associated with this deployer
     */
    public Set<Grid> getGrids() {
        return grids;
    }

    /**
     * Gets the {@link Grid} associated with this deployer with the provided
     * name
     * 
     * @return the {@link Grid} associated with this deployer with the provided
     *         name
     */
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

    /**
     * Loads a run from a properties file. All the jobs described in the
     * properties file will be returned.
     * 
     * @param runFileName
     *            the file containing the run properties
     * @return The {@link List} of {@link Job}s, loaded from the run file.
     * @throws FileNotFoundException
     *             if the file cannot be found
     * @throws IOException
     *             if something fails during the loading of the run.
     */
    public List<Job> loadRun(String runFileName) throws FileNotFoundException,
            IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(runFileName));
        return loadRun(properties);
    }

    /**
     * Loads a run from a TypedProperties object. All the jobs described in the
     * properties object will be returned.
     * 
     * @param properties
     *            the properties containing the details of the run
     * @return The {@link List} of {@link Job}s, loaded from the run
     *         properties.
     */
    public List<Job> loadRun(TypedProperties properties) {
        properties.expandSystemVariables();
        return Run.load(properties, grids, applications);
    }

    /**
     * <b>DO NOT USE</b>. This method is for internal use only!
     */
    public void processMetricEvent(MetricEvent arg0) {
        System.out.println("DEPLOYER: " + arg0.getValue());
    }

    private void startExternalServer(Cluster serverCluster, String id,
            Application application) throws GATObjectCreationException,
            GATInvocationException, IOException {
        if (logger.isInfoEnabled()) {
            logger.info("start external server");
        }
        serverURI = serverCluster.getDeployBroker();
        Preferences serverPreferences = new Preferences();
        if (serverCluster.getFileAccessType() != null) {
            serverPreferences.put("file.adaptor.name", serverCluster
                    .getFileAccessType());
        }
        if (serverCluster.getAccessType() != null) {
            serverPreferences.put("resourcebroker.adaptor.name", serverCluster
                    .getAccessType());
        }
        JavaSoftwareDescription sd = new JavaSoftwareDescription();
        sd.setExecutable(serverCluster.getJavaPath() + "/bin/java");

        sd.setJavaMain("ibis.server.Server");
        sd.setJavaArguments(new String[] { "--port", "0", "--hub-address-file",
                "../" + serverURI.getHost() + ".address" });
        sd.setJavaOptions(new String[] {
                "-classpath",
                application
                        .getJavaClassPath(application.getServerPreStageSet()),
                "-Dlog4j.configuration=file:./log4j.properties" });
        if (application.getServerPreStageSet() != null) {
            for (String filename : application.getServerPreStageSet()) {
                sd
                        .addPreStagedFile(GAT.createFile(serverPreferences,
                                filename));
            }
        }
        sd.setStderr(GAT.createFile(serverPreferences, "server@"
                + serverCluster.getName() + ".err"));
        sd.setStdout(GAT.createFile(serverPreferences, "server@"
                + serverCluster.getName() + ".out"));
        JobDescription jd = new JobDescription(sd);
        if (logger.isDebugEnabled()) {
            logger.debug("starting server at '" + serverURI
                    + "' with job description: \n" + jd);
        }

        ResourceBroker broker = GAT.createResourceBroker(serverPreferences,
                serverURI);
        List<org.gridlab.gat.resources.Job> initialJob = new ArrayList<org.gridlab.gat.resources.Job>();
        initialJob.add(broker.submitJob(jd, this, "job.status"));
        runningJobs.put(id, initialJob);
        waitForHubAddress(serverURI);
    }

    private void startHub(URI hubURI, String id, Application application)
            throws GATObjectCreationException, GATInvocationException,
            IOException {
        // TODO: preferences for hub?
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

        sd.setJavaOptions(new String[] {
                "-classpath",
                application
                        .getJavaClassPath(application.getServerPreStageSet()),
                "-Dlog4j.configuration=file:./log4j.properties" });

        if (application.getServerPreStageSet() != null) {
            for (String filename : application.getServerPreStageSet()) {
                sd.addPreStagedFile(GAT.createFile(filename));
            }
        }
        sd.setStderr(GAT.createFile("hub@" + hubURI.getHost() + ".err"));
        sd.setStdout(GAT.createFile("hub@" + hubURI.getHost() + ".out"));
        JobDescription jd = new JobDescription(sd);
        ResourceBroker broker = GAT.createResourceBroker(hubURI);
        runningJobs.get(id).add(broker.submitJob(jd));
        waitForHubAddress(hubURI);
    }

    // private void startInternalServer() throws Exception {
    // if (logger.isInfoEnabled()) {
    // logger.info("starting internal server");
    // }
    // Properties properties = new Properties();
    // // let the server automatically find a free port
    // properties.put(ServerProperties.PORT, "0");
    // properties.put(ServerProperties.IMPLEMENTATION_PATH, ibisHome
    // + File.separator + "lib");
    // properties.put("ibis.registry.central.statistics", "true");
    // properties.put(ServerProperties.PRINT_EVENTS, "" + true);
    // properties.put(ServerProperties.PRINT_ERRORS, "" + true);
    // properties.put(ServerProperties.PRINT_STATS, "" + true);
    // server = new Server(properties);
    // logger.info("started ibis server: " + server);
    // serverURI = new URI("any://localhost");
    // existingHubs.put(serverURI, server.getLocalAddress());
    // }

    // private void startServer(Cluster serverCluster) throws Exception {
    // // TODO: booleans for events, errors etc.
    // if (logger.isInfoEnabled()) {
    // logger.info("starting server");
    // }
    // if (serverCluster.getDeployBroker().refersToLocalHost()) {
    // // start the server on the localhost using a server object.
    // startInternalServer();
    // } else {
    // // start the server just like the hubs
    // startExternalServer(serverCluster, null);
    // }
    // }

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

        sd.setJavaClassPath(application.getJavaClassPath(application
                .getPreStageSet()));

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
        JobDescription jd = null;
        if (!subjob.hasExecutable()) {
            jd = new JobDescription(sd);
        } else {
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
                            filename), GAT.createFile(preferences, subjob
                            .getName()
                            + "." + filename));
                }
            }
            nonJava.setStderr(errFile);
            nonJava.setStdout(outFile);
            nonJava.setExecutable(subjob.getExecutable());
            List<String> argumentList = new ArrayList<String>();
            if (subjob.getArguments() != null) {
                for (String arg : subjob.getArguments()) {
                    argumentList.add(arg);
                }
            }
            argumentList.add("" + subjob.getNodes());
            argumentList.add("" + subjob.getMulticore());
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

        ResourceBroker broker = GAT.createResourceBroker(preferences, cluster
                .getJobBroker());
        broker.submitJob(jd, subjob, "job.status");
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
