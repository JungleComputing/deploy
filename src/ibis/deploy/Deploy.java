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

public class Deploy {

    private static final Logger logger = LoggerFactory.getLogger(Deploy.class);

    // "root" hub (perhaps including the server)
    private LocalServer rootHub;

    // remote server (if it exists)
    private RemoteServer remoteServer;

    private String serverAddress;

    // libraries used to start a server/hub
    private File[] serverLibs;

    // submitted jobs
    private List<Job> jobs;

    // Map<gridName, Map<clusterName, Server>> with "gobal" hubs
    private Map<String, Map<String, RemoteServer>> hubs;

    Deploy() {
        rootHub = null;
        remoteServer = null;
        serverLibs = null;

        jobs = new ArrayList<Job>();
        hubs = new HashMap<String, Map<String, RemoteServer>>();

    }

    /**
     * Initialize this deployment object.
     * 
     * @param serverCluster
     *            cluster where the server should be started, or null for a
     *            server embedded in this JVM.
     * @param serverLibs
     *            All required files and directories to start a server or hub.
     *            Jar files will also be loaded into this JVM automatically.
     * @throws Exception
     *             if the sercer cannot be started
     */
    synchronized void initialize(Cluster serverCluster, File... serverLibs)
            throws Exception {
        if (serverLibs == null) {
            throw new Exception("no server libraries specified");
        }
        this.serverLibs = serverLibs.clone();

        logger.info("Initializing deployer");

        if (serverCluster == null) {
            // rootHub includes server
            rootHub = new LocalServer(false);
            remoteServer = null;
            serverAddress = rootHub.getAddress();
        } else {
            rootHub = new LocalServer(true);
            remoteServer = new RemoteServer(serverLibs, serverCluster, false,
                    rootHub);
            serverAddress = remoteServer.getAddress();

            // add server to map of hubs (no need to start another hub on the
            // same cluster later)
            Map<String, RemoteServer> clusterMap = new HashMap<String, RemoteServer>();
            clusterMap.put(serverCluster.getName(), remoteServer);
            hubs.put(serverCluster.getGridName(), clusterMap);

            // wait until server is started
            remoteServer.waitUntilRunning();
        }
        logger.info("Deployer initialized succesfully");
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
     * @param jobListener
     *            callback object for status of job
     * @param hubListener
     *            callback object for status of hub
     * @param globalHub
     *            If true, a "global" hub is used, started on each cluster and
     *            shared between all jobs on that cluster. If false, a hub is
     *            created specifically for this job, and stopped after the job
     *            completes.
     * @return the resulting job
     * @throws Exception
     */
    public synchronized Job submitJob(Cluster cluster, int resourceCount,
            Application application, int processCount, String poolName,
            MetricListener jobListener, MetricListener hubListener,
            boolean globalHub) throws Exception {
        if (serverAddress == null) {
            throw new Exception("Deployer not initialized, cannot submit jobs");
        }

        RemoteServer hub = null;
        if (globalHub) {
            hub = getHub(cluster, false);
            if (hubListener != null) {
                hub.addStateListener(hubListener);
            }
        }

        // start job
        Job job = new Job(cluster, resourceCount, application, processCount,
                poolName, serverAddress, rootHub, hub, serverLibs);

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
            result = new RemoteServer(serverLibs, cluster, true, rootHub);
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
