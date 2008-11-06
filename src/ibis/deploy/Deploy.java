package ibis.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.monitoring.MetricListener;
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
            "log4j.server.properties" };

    private static final Logger logger = LoggerFactory.getLogger(Deploy.class);

    // "root" hub (perhaps including the server)
    private LocalServer rootHub;

    // remote server (if it exists)
    private RemoteServer remoteServer;

    // home dir of ibis-deploy
    private File homeDir;

    // submitted jobs
    private List<Job> jobs;

    // Map<gridName, Map<clusterName, Server>> with "shared" hubs
    private Map<String, RemoteServer> hubs;

    /**
     * Create a new (uninitialized) deployment interface.
     */
    public Deploy() {
        rootHub = null;
        remoteServer = null;
        homeDir = null;

        jobs = new ArrayList<Job>();
        hubs = new HashMap<String, RemoteServer>();

    }

    /**
     * Checks if required files are in the specified home dir
     * 
     * @param home
     *            ibis-deploy home dir
     * @throws Exception
     *             if one or more files are missing
     */
    private static void checkFiles(File home) throws Exception {
        for (String fileName : REQUIRED_FILES) {
            if (!new File(home, fileName).exists()) {
                throw new Exception("required file/dir \"" + fileName
                        + "\" not found in ibis deploy home (" + home + ")");
            }
        }
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
     * @param deployHome
     *            "home" directory of ibis-deploy. If null, the default location
     *            is used from the "ibis.deploy.home" system property. If this
     *            property is unspecified, final default value is the current
     *            working directory.
     * @throws Exception
     *             if the server cannot be started
     */
    public synchronized void initialize(Cluster serverCluster, File deployHome)
            throws Exception {
        initialize(serverCluster, deployHome, null, true);
    }

    /**
     * Initialize this deployment object. Will NOT wait until the server is
     * actually running
     * 
     * @param serverCluster
     *            cluster where the server should be started, or null for a
     *            server embedded in this JVM.
     * @param deployHome
     *            "home" directory of ibis-deploy. If null, the default location
     *            is used from the "ibis.deploy.home" system property. If this
     *            property is unspecified, final default value is the current
     *            working directory.
     * @param listener
     *            callback object for status of server
     * @throws Exception
     *             if the server cannot be started
     */
    public synchronized void initialize(Cluster serverCluster, File deployHome,
            MetricListener listener) throws Exception {
        initialize(serverCluster, deployHome, listener, false);
    }

    private synchronized void initialize(Cluster serverCluster,
            File deployHome, MetricListener listener, boolean blocking)
            throws Exception {
        if (deployHome == null) {
            String homeProperty = System.getProperty(HOME_PROPERTY);
            if (homeProperty == null || homeProperty.length() == 0) {
                homeProperty = System.getProperty("user.dir");
            }
            homeDir = new File(homeProperty);
        } else {
            homeDir = deployHome;
        }

        // see if all files we need are there.
        checkFiles(homeDir);

        logger.info("Initializing deploy");

        if (serverCluster == null) {
            // rootHub includes server
            rootHub = new LocalServer(false);
            remoteServer = null;
        } else {
            rootHub = new LocalServer(true);
            remoteServer = new RemoteServer(serverCluster, false, rootHub,
                    homeDir);

            hubs.put(serverCluster.getName(), remoteServer);
        }
        logger.info("Ibis Deploy initialized successfully");
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
            MetricListener jobListener, MetricListener hubListener)
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

        logger.info("Submitting new job:\n" + description.toPrintString());
        
        description.checkSettings();

        // waits until server is running
        String serverAddress = getServerAddress();

        RemoteServer hub = null;
        if (description.getSharedHub() == null || description.getSharedHub()) {
            hub = getHub(description.getClusterSettings(), false);
            if (hubListener != null) {
                hub.addStateListener(hubListener);
            }
        }

        // start job
        Job job = new Job(description, serverAddress, rootHub, hub, homeDir);

        if (jobListener != null) {
            job.addStateListener(jobListener);
        }

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
     * @return reference to a hub on the given cluster
     * @throws Exception
     *             if the hub cannot be started
     */
    public synchronized RemoteServer getHub(Cluster cluster,
            boolean waitUntilRunning) throws Exception {
        if (rootHub == null) {
            throw new Exception("Ibis Deploy not initialized, cannot get hub");
        }

        String clusterName = cluster.getName();

        logger.debug("starting hub on " + clusterName);

        if (clusterName == null) {
            throw new Exception("cannot start hub on an unnamed cluster.");
        }

        RemoteServer result = hubs.get(clusterName);

        if (result == null) {
            result = new RemoteServer(cluster, true, rootHub, homeDir);
            hubs.put(clusterName, result);
        }

        if (waitUntilRunning) {
            result.waitUntilRunning();
        }

        return result;
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
