package ibis.deploy;

import ibis.ipl.server.RegistryServiceInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public enum HubPolicy {
        OFF, PER_CLUSTER, PER_JOB,
    }

    /**
     * System property with home dir of Ibis deploy.
     */
    public static final String HOME_PROPERTY = "ibis.deploy.home";

    /**
     * Files needed by ibis-deploy. Searched for in ibis deploy home dir
     */
    public static final String[] REQUIRED_FILES = { "lib-server",
            "log4j.properties", "lib-zorilla" };

    private static final Logger logger = LoggerFactory.getLogger(Deploy.class);

    // local "root" hub (perhaps including a server and/or a zorilla node)
    private LocalServer localServer;

    // remote server (if it exists)
    private RemoteServer remoteServer;

    // home dir of ibis-deploy
    private final File home;

    // promote some logging prints from debug to info
    private boolean verbose;

    // submitted jobs
    private List<Job> jobs;

    // Map<gridName, Map<clusterName, Server>> with "shared" hubs
    private Map<String, RemoteServer> hubs;

    private boolean keepSandboxes;

    private HubPolicy hubPolicy;

    private PoolSizePrinter poolSizePrinter;

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
    public Deploy(File home, boolean verbose) throws Exception {
        localServer = null;
        remoteServer = null;
        keepSandboxes = false;
        poolSizePrinter = null;
        this.verbose = verbose;

        hubPolicy = HubPolicy.PER_CLUSTER;

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

    // is verbose mode turned on?
    boolean isVerbose() {
        return verbose;
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
    public synchronized void setKeepSandboxes(boolean keepSandboxes) {
        this.keepSandboxes = keepSandboxes;
    }

    public synchronized void setHubPolicy(HubPolicy policy) {
        this.hubPolicy = policy;
    }

    /**
     * Returns the address of the build-in root hub.
     * 
     * @return the address of the build-in root hub.
     * @throws Exception
     *             if ibis-deploy has not been initialized yet.
     */
    public synchronized String getRootHubAddress() throws Exception {
        if (localServer == null) {
            throw new Exception("Ibis-deploy not initialized yet");
        }

        return localServer.getAddress();
    }

    /**
     * Returns the build-in root hub.
     * 
     * @return the build-in root hub.
     * @throws Exception
     *             if ibis-deploy has not been initialized yet.
     */
    synchronized LocalServer getRootHub() throws Exception {
        if (localServer == null) {
            throw new Exception("Ibis-deploy not initialized yet");
        }

        return localServer;
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
        if (localServer == null) {
            throw new Exception("Ibis-deploy not initialized yet");
        }

        if (remoteServer == null) {
            return localServer.getAddress();
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

    /**
     * Initialize this deployment object.
     * 
     * @param serverCluster
     *            cluster where the server should be started, or null for a
     *            server embedded in this JVM.
     * @param listener
     *            callback object for status of server
     * @param blocking
     *            if true, will block until the server is running
     * @throws Exception
     *             if the server cannot be started
     *             
     */
    public synchronized void initialize(Cluster serverCluster,
            StateListener listener, boolean blocking) throws Exception {
        logger.debug("Initializing deploy");

        if (serverCluster == null) {
            // rootHub includes server
            localServer = new LocalServer(true, false, verbose);
            localServer.addListener(listener);
            remoteServer = null;
        } else {
            localServer = new LocalServer(false, false, verbose);
            remoteServer = new RemoteServer(serverCluster, false, localServer,
                    home, verbose,

                    listener, keepSandboxes);

            hubs.put(serverCluster.getName(), remoteServer);

            if (blocking) {
                remoteServer.waitUntilRunning();
            }
        }

        // print pool size statistics
        poolSizePrinter = new PoolSizePrinter(this);

        logger.info("Ibis Deploy initialized, root hub address is "
                + localServer.getAddress());
    }

    /**
     * Initialize this deployment object. Will also start a build-in Zorilla node.
     * Additionally, a Zorilla node is either started or used on each cluster specified.
     * 
     * @param clusters
     *             clusters used for zorilla. Will start a zorilla if needed.
     * @throws Exception
     *             if Ibis-Deploy cannot be started for some reason.
     *             
     */
    public synchronized void initializeZorilla(Cluster... clusters) throws Exception {
        logger.debug("Initializing deploy, Zorilla mode...");
        
        localServer = new LocalServer(true, true, verbose);
        
        
        
        
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
        if (localServer == null) {
            throw new Exception("Ibis-deploy not initialized yet");
        }

        if (remoteServer != null && !remoteServer.isRunning()) {
            throw new Exception("Cannot submit job (yet), server \""
                    + remoteServer + "\" not running");
        }

        // resolve given description into single "independent" description
        JobDescription resolvedDescription = description.resolve(
                applicationSet, grid);

        if (verbose) {
            logger.info("Submitting new job:\n"
                    + resolvedDescription.toPrintString());
        } else {
            logger.debug("Submitting new job:\n"
                    + resolvedDescription.toPrintString());
        }

        resolvedDescription.checkSettings();

        Server hub = null;
        if (hubPolicy == HubPolicy.PER_CLUSTER) {
            hub = getClusterHub(resolvedDescription.getClusterOverrides(),
                    false, hubListener);
        }

        // start job
        Job job = new Job(resolvedDescription, hubPolicy, hub, keepSandboxes,
                jobListener, hubListener, localServer, verbose, home,
                getServerAddress(), this);

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
    public synchronized Server getClusterHub(Cluster cluster,
            boolean waitUntilRunning, StateListener listener) throws Exception {
        if (localServer == null) {
            throw new Exception("Ibis Deploy not initialized, cannot get hub");
        }

        String clusterName = cluster.getName();

        logger.debug("starting hub on " + clusterName);

        if (clusterName == null) {
            throw new Exception("cannot start hub on an unnamed cluster");
        }

        if (clusterName.equals("local")) {
            localServer.addListener(listener);
            return localServer;
        }

        RemoteServer result = hubs.get(clusterName);

        if (result == null || result.isFinished()) {
            // new server needed
            result = new RemoteServer(cluster, true, localServer, home, verbose,
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

    public synchronized RegistryServiceInterface getRegistry() throws Exception {
        if (localServer == null) {
            throw new Exception(
                    "Ibis Deploy not initialized, cannot monitor server");
        }

        if (remoteServer != null) {
            if (!remoteServer.isRunning()) {
                throw new Exception("Cannot monitor server \"" + remoteServer
                        + "\" not running");
            }
            return remoteServer.getRegistryService();
        }
        
        return localServer.getRegistryService();

    }

    /**
     * Returns a map containing the size of each pool at the server
     * 
     * @return a map containing the size of each pool at the server. May be
     *         empty if the server could not be reached
     */
    public Map<String, Integer> poolSizes() throws Exception {
        return getRegistry().getPoolSizes();
    }

    /**
     * Returns a map containing the size of each pool at the server
     * 
     * @return a map containing the size of each pool at the server
     * @throws Exception
     *             if the server is not running yet, or communicating with it
     *             failed
     */
    public String[] getLocations(String poolName) throws Exception {
        return getRegistry().getLocations(poolName);

    }

    public synchronized Job[] getJobs() {
        return jobs.toArray(new Job[0]);
    }

    /**
     * Waits until all jobs are finished. If any jobs are submitted while
     * waiting, will not wait on those.
     * 
     * @throws Exception
     *             if one of the jobs failed.
     * 
     */
    public void waitUntilJobsFinished() throws Exception {
        Job[] jobs = getJobs();

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

            Thread.sleep(1000);
        }
    }

    /**
     * Ends all jobs and closes all open connections.
     */
    public synchronized void end() {
        logger.info("ending ibis-deploy engine");

        if (poolSizePrinter != null) {
            poolSizePrinter.end();
        }

        for (Job job : jobs) {
            logger.info("killing job " + job);
            job.kill();
        }

        for (RemoteServer hub : hubs.values()) {
            logger.info("killing Hub " + hub);
            hub.kill();
        }

        if (remoteServer != null) {
            logger.info("killing Server " + remoteServer);
            remoteServer.kill();
        }

        if (localServer != null) {
            logger.info("killing root Hub " + localServer);
            localServer.killAll();
            localServer.kill();
        }

        logger.info("ending GAT");

        GAT.end();
        logger.info("ending ibis-deploy engine DONE :)");
    }

}
