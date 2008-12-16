package ibis.deploy;

import ibis.ipl.impl.registry.central.monitor.RegistryMonitorClient;
import ibis.server.ServerProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.gridlab.gat.GAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for ibis-deploy framework. Allows users to deploy jobs and
 * start hubs on a grid.
 * 
 * @author Niels Drost
 * 
 */
public class Deploy {

    /**
     * System property with home dir of Ibis deploy.
     */
    public static final String HOME_PROPERTY = "ibis.deploy.home";

    /**
     * Files needed by ibis-deploy. Searched for in ibis deploy home dir
     */
    public static final String[] REQUIRED_FILES = { "lib-server",
            "log4j.properties" };

    private static final Logger logger = LoggerFactory.getLogger(Deploy.class);

    // "root" hub (perhaps including the server)
    private LocalServer rootHub;

    // remote server (if it exists)
    private RemoteServer remoteServer;

    private RegistryMonitorClient registryMonitor;

    // home dir of ibis-deploy
    private final File home;

    // submitted jobs
    private List<Job> jobs;

    // Map<gridName, Map<clusterName, Server>> with "shared" hubs
    private Map<String, RemoteServer> hubs;

    private boolean keepSandboxes;

    /**
     * Create a new (uninitialized) deployment interface.
     * 
     * @param home
     *            "home" directory of ibis-deploy. If null, the default location
     *            is used from the "ibis.deploy.home" system property. If this
     *            property is unspecified, final default value is the current
     *            working directory.
     * @throws Exception
     *             if required files cannot be found in home
     */
    public Deploy(File home) throws Exception {
        rootHub = null;
        remoteServer = null;
        keepSandboxes = false;

        jobs = new ArrayList<Job>();
        hubs = new HashMap<String, RemoteServer>();

        if (home == null) {
            String homeProperty = System.getProperty(HOME_PROPERTY);
            if (homeProperty == null || homeProperty.length() == 0) {
                homeProperty = System.getProperty("user.dir");
            }
            this.home = new File(homeProperty);
        } else {
            this.home = home;
        }

        checkFiles();
    }

    /**
     * Returns the home directory of ibis-deploy used to fetch server libraries,
     * images, etc.
     * 
     * @return the home directory of ibis-deploy
     */
    public File getHome() {
        return home;
    }

    /**
     * Checks if required files are in the specified home dir
     * 
     * @param home
     *            ibis-deploy home dir
     * @throws Exception
     *             if one or more files are missing
     */
    private void checkFiles() throws Exception {
        for (String fileName : REQUIRED_FILES) {
            if (!new File(home, fileName).exists()) {
                throw new Exception("required file/dir \"" + fileName
                        + "\" not found in ibis deploy home (" + home + ")");
            }
        }
    }

    /**
     * If set to true, will keep all sandboxes for jobs (not for hubs and
     * servers though). This is turned of by default
     * 
     * @param keepSandboxes
     *            if true, ibis-deploy will keep all sandboxes for jobs from now
     *            on.
     */
    public synchronized void keepSandboxes(boolean keepSandboxes) {
        this.keepSandboxes = keepSandboxes;
    }

    /**
     * Returns the address of the build-in root hub.
     * 
     * @return the address of the build-in root hub.
     * @throws Exception
     *             if ibis-deploy has not been initialized yet.
     */
    public synchronized String getRootHubAddress() throws Exception {
        if (rootHub == null) {
            throw new Exception("Ibis-deploy not initialized yet");
        }

        return rootHub.getAddress();
    }
    
    /**
     * Returns the build-in root hub.
     * 
     * @return the build-in root hub.
     * @throws Exception
     *             if ibis-deploy has not been initialized yet.
     */
    synchronized LocalServer getRootHub() throws Exception {
        if (rootHub == null) {
            throw new Exception("Ibis-deploy not initialized yet");
        }

        return rootHub;
    }


    /**
     * Retrieves address of server. May block if server has not been started
     * yet.
     * 
     * @return address of server
     * @throws Exception
     *             if server state cannot be retrieved.
     */
    public synchronized String getServerAddress() throws Exception {
        if (rootHub == null) {
            throw new Exception("Ibis-deploy not initialized yet");
        }

        if (remoteServer == null) {
            return rootHub.getAddress();
        } else {
            return remoteServer.getAddress();
        }
    }

    /**
     * Initialize this deployment object. Will wait until the server is actually
     * running
     * 
     * @param serverCluster
     *            cluster where the server should be started, or null for a
     *            server embedded in this JVM.
     * 
     * @throws Exception
     *             if the server cannot be started
     */
    public synchronized void initialize(Cluster serverCluster) throws Exception {
        initialize(serverCluster, null, true);
    }

    /**
     * Initialize this deployment object. Will NOT wait until the server is
     * actually running
     * 
     * @param serverCluster
     *            cluster where the server should be started, or null for a
     *            server embedded in this JVM.
     * @param listener
     *            callback object for status of server
     * @throws Exception
     *             if the server cannot be started
     */
    public synchronized void initialize(Cluster serverCluster,
            StateListener listener) throws Exception {
        initialize(serverCluster, listener, false);
    }

    private synchronized void initialize(Cluster serverCluster,
            StateListener listener, boolean blocking) throws Exception {

        logger.debug("Initializing deploy");

        if (serverCluster == null) {
            // rootHub includes server
            rootHub = new LocalServer(false);
            rootHub.addListener(listener);
            remoteServer = null;

        } else {
            rootHub = new LocalServer(true);
            remoteServer = new RemoteServer(serverCluster, false, rootHub,
                    home, listener, keepSandboxes);

            hubs.put(serverCluster.getName(), remoteServer);

            if (blocking) {
                remoteServer.waitUntilRunning();
            }
        }

        logger.info("Ibis Deploy initialized, root hub address is "
                + rootHub.getAddress());
    }

    /**
     * Submit a new job.
     * 
     * @param description
     *            description of the job.
     * @param applicationSet
     *            applicationSet for job
     * @param grid
     *            grid to use
     * @param hubListener
     *            listener for state of hub
     * @param jobListener
     *            listener for state of job
     * 
     * 
     * 
     * 
     * @return the resulting job
     * @throws Exception
     */
    public synchronized Job submitJob(JobDescription description,
            ApplicationSet applicationSet, Grid grid,
            StateListener jobListener, StateListener hubListener)
            throws Exception {
        if (rootHub == null) {
            throw new Exception("Ibis-deploy not initialized yet");
        }

        if (remoteServer != null && !remoteServer.isRunning()) {
            throw new Exception("Cannot submit job (yet), server \""
                    + remoteServer + "\" not running");
        }

        // resolve given description into single "independent" description
        description = description.resolve(applicationSet, grid);

        logger.debug("Submitting new job:\n" + description.toPrintString());

        description.checkSettings();

        // waits until server is running
        String serverAddress = getServerAddress();

        Hub hub = null;
        if (description.getSharedHub() == null || description.getSharedHub()) {
            hub = getHub(description.getClusterOverrides(), false, hubListener);
        }

        // start job
        Job job = new Job(description, hub,
                keepSandboxes, jobListener, hubListener, this);

        jobs.add(job);

        return job;
    }

    /**
     * Returns a hub on the specified cluster. If a hub does not exist on the
     * cluster, one is submitted. May not be running (yet).
     * 
     * @param cluster
     *            cluster to deploy the hub on
     * @param waitUntilRunning
     *            wait until hub is actually running
     * @param listener
     *            listener for state of hub
     * @return reference to a hub on the given cluster
     * @throws Exception
     *             if the hub cannot be started
     */
    public synchronized Hub getHub(Cluster cluster, boolean waitUntilRunning,
            StateListener listener) throws Exception {
        if (rootHub == null) {
            throw new Exception("Ibis Deploy not initialized, cannot get hub");
        }

        String clusterName = cluster.getName();

        logger.debug("starting hub on " + clusterName);

        if (clusterName == null) {
            throw new Exception("cannot start hub on an unnamed cluster");
        }

        if (clusterName.equals("local")) {
            rootHub.addListener(listener);
            return rootHub;
        }

        RemoteServer result = hubs.get(clusterName);

        if (result == null) {
            result = new RemoteServer(cluster, true, rootHub, home,
                    listener, keepSandboxes);
            hubs.put(clusterName, result);
        } else {
            result.addStateListener(listener);
        }

        if (waitUntilRunning) {
            result.waitUntilRunning();
        }

        return result;
    }

    /**
     * Returns a map containing the size of each pool at the server
     * 
     * @return a map containing the size of each pool at the server
     * @throws Exception
     *             if the server is not running yet, or communicating with it
     *             failed
     */
    public synchronized Map<String, Integer> poolSizes() throws Exception {
        if (rootHub == null) {
            throw new Exception(
                    "Ibis Deploy not initialized, cannot monitor server");
        }

        if (remoteServer != null && !remoteServer.isRunning()) {
            throw new Exception("Cannot monitor server \"" + remoteServer
                    + "\" not running");
        }

        if (registryMonitor == null) {
            Properties properties = new Properties();
            properties.put(ServerProperties.ADDRESS, getServerAddress());
            properties.put(ServerProperties.HUB_ADDRESSES, getRootHubAddress());

            registryMonitor = new RegistryMonitorClient(properties, false);
        }

        return registryMonitor.getPoolSizes();
    }
    
    /**
     * Returns a map containing the size of each pool at the server
     * 
     * @return a map containing the size of each pool at the server
     * @throws Exception
     *             if the server is not running yet, or communicating with it
     *             failed
     */
    public synchronized String[] getLocations(String poolName) throws Exception {
        if (rootHub == null) {
            throw new Exception(
                    "Ibis Deploy not initialized, cannot monitor server");
        }

        if (remoteServer != null && !remoteServer.isRunning()) {
            throw new Exception("Cannot monitor server \"" + remoteServer
                    + "\" not running");
        }

        if (registryMonitor == null) {
            Properties properties = new Properties();
            properties.put(ServerProperties.ADDRESS, getServerAddress());
            properties.put(ServerProperties.HUB_ADDRESSES, getRootHubAddress());

            registryMonitor = new RegistryMonitorClient(properties, false);
        }

        return registryMonitor.getLocations(poolName);
    }

    /**
     * Waits until all jobs are finished. If any jobs are submitted while
     * waiting, will also wait on those.
     * 
     * @throws Exception
     *             if one of the jobs failed.
     * 
     */
    public synchronized void waitUntilJobsFinished() throws Exception {
        while (true) {

            boolean done = true;

            for (Job job : jobs) {
                if (!job.isFinished()) {
                    done = false;
                }
            }

            if (done) {
                return;
            }

            wait(1000);
        }
    }

    /**
     * Ends all jobs and closes all open connections.
     */
    public synchronized void end() {
        logger.info("ending ibis-deploy engine");
        for (Job job : jobs) {
            job.kill();
        }

        for (RemoteServer hub : hubs.values()) {
            hub.kill();
        }

        if (remoteServer != null) {
            remoteServer.kill();
        }

        if (rootHub != null) {
            rootHub.killAll();
            rootHub.kill();
        }

        GAT.end();
    }

}
