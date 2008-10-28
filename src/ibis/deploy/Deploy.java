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
 * @author ndrost
 * 
 */
public class Deploy {

    public static final String HOME_PROPERTY = "ibis.deploy.home";

    public static final String[] REQUIRED_FILES = { "lib-server",
            "log4j.server.properties" };

    private static final Logger logger = LoggerFactory.getLogger(Deploy.class);

    // "root" hub (perhaps including the server)
    private LocalServer rootHub;

    // remote server (if it exists)
    private RemoteServer remoteServer;

    // address of server
    private String serverAddress;

    // home dir of ibis-deploy
    private File homeDir;

    // submitted jobs
    private List<Job> jobs;

    // Map<gridName, Map<clusterName, Server>> with "shared" hubs
    private Map<String, Map<String, RemoteServer>> hubs;

    /**
     * Create a new (uninitialized) deployment interface.
     */
    public Deploy() {
        rootHub = null;
        remoteServer = null;
        homeDir = null;

        jobs = new ArrayList<Job>();
        hubs = new HashMap<String, Map<String, RemoteServer>>();

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
     * Initialize this deployment object.
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

        logger.info("Initializing deployer");

        if (serverCluster == null) {
            // rootHub includes server
            rootHub = new LocalServer(false);
            remoteServer = null;
            serverAddress = rootHub.getAddress();
        } else {
            rootHub = new LocalServer(true);
            remoteServer = new RemoteServer(serverCluster, false, rootHub,
                    homeDir);
            serverAddress = remoteServer.getAddress();

            // add server to map of hubs (no need to start another hub on the
            // same cluster later)
            Map<String, RemoteServer> clusterMap = new HashMap<String, RemoteServer>();
            clusterMap.put(serverCluster.getName(), remoteServer);
            hubs.put(serverCluster.getGridName(), clusterMap);

            // wait until server is started
            remoteServer.waitUntilRunning();
        }
        logger.info("Deployer initialized successfully");
    }

    /**
     * Submit a new job.
     * 
     * @param cluster
     *            cluster within grid to submit job to
     * @param resourceCount
     *            number of resources to allocate on the cluster
     * @param application
     *            application to run.
     * @param processCount
     *            number of processes to start on the allocated resources
     * @param poolName
     *            name of the pool to join
     * @param poolSize
     *            size of pool. Only used in case of a closed-world pool.
     * @param jobListener
     *            callback object for status of job
     * @param hubListener
     *            callback object for status of hub
     * @param sharedHub
     *            If true, a "shared" hub is used, started on each cluster and
     *            shared between all jobs on that cluster. If false, a hub is
     *            created specifically for this job, and stopped after the job
     *            completes.
     * @return the resulting job
     * @throws Exception
     */
    public synchronized Job submitJob(Cluster cluster, int resourceCount,
            Application application, int processCount, String poolName,
            int poolSize, MetricListener jobListener,
            MetricListener hubListener, boolean sharedHub) throws Exception {
        if (serverAddress == null) {
            throw new Exception("Deployer not initialized, cannot submit jobs");
        }

        if (cluster == null) {
            throw new Exception("cluster not specified in creating new job");
        }

        if (resourceCount < 0) {
            throw new Exception(
                    "resource count cannot be negative or zero. Resource count = "
                            + resourceCount);
        }

        if (application == null) {
            throw new Exception("no application speficied in creating new job");
        }

        if (processCount < 0) {
            throw new Exception(
                    "process count cannot be negative or zero. Process count = "
                            + processCount);
        }

        if (poolName == null) {
            throw new Exception("pool name not specified in submitting job");
        }
        
        if (poolSize < 0) {
            throw new Exception("pool size cannot be negative");
        }

        RemoteServer hub = null;
        if (sharedHub) {
            hub = getHub(cluster, false);
            if (hubListener != null) {
                hub.addStateListener(hubListener);
            }
        }

        // start job
        Job job = new Job(cluster, resourceCount, application, processCount,
                poolName, poolSize, serverAddress, rootHub, hub, homeDir);

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
        if (serverAddress == null) {
            throw new Exception("Deployer not initialized, cannot get hub");
        }

        String clusterName = cluster.getName();
        String gridName = cluster.getGridName();

        logger.debug("starting hub on " + clusterName + "@" + gridName);

        if (clusterName == null) {
            throw new Exception(
                    "cannot start hub on an unnamed cluster. (grid = "
                            + gridName + ")");
        }

        if (gridName == null) {
            throw new Exception(
                    "cannot start hub on an unnamed grid. (cluster = "
                            + clusterName + ")");
        }

        // find map containing all clusters of the specified grid. Create if
        // needed.
        Map<String, RemoteServer> clusterMap = hubs.get(gridName);
        if (clusterMap == null) {
            clusterMap = new HashMap<String, RemoteServer>();
            hubs.put(gridName, clusterMap);
        }

        RemoteServer result = clusterMap.get(clusterName);

        if (result == null) {
            result = new RemoteServer(cluster, true, rootHub, homeDir);
            clusterMap.put(clusterName, result);
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

        for (Map<String, RemoteServer> grids : hubs.values()) {
            for (RemoteServer hub : grids.values()) {
                hub.kill();
            }
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
