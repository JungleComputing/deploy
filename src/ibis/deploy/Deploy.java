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
    private final LocalServer localServer;

    // remote server (if it exists)
    private final RemoteServer remoteServer;

    // home dir of ibis-deploy
    private final File home;

    // promote some logging prints from debug to info
    private boolean verbose;

    private boolean keepSandboxes;

    // submitted jobs
    private List<Job> jobs;

    // Map<gridName, Map<clusterName, Server>> with "shared" hubs
    private Map<String, Server> hubs;

    private HubPolicy hubPolicy = HubPolicy.PER_CLUSTER;

    private final PoolSizePrinter poolSizePrinter;

    private static File checkHome(File home) throws Exception {
        if (home == null) {
            String homeProperty = System.getProperty(HOME_PROPERTY);
            if (homeProperty == null || homeProperty.length() == 0) {
                homeProperty = System.getProperty("user.dir");
            }
            home = new File(homeProperty);
        }

        for (String fileName : REQUIRED_FILES) {
            if (!new File(home, fileName).exists()) {
                throw new Exception("required file/dir \"" + fileName
                        + "\" not found in ibis deploy home (" + home + ")");
            }
        }

        return home;
    }
    

    /**
     * Create a new deployment interface. Also deploys the server, embedded in
     * this JVM. Convenience constructor provides all possible defaults.
     * 
     * @param home
     *            "home" directory of ibis-deploy. If null, the default location
     *            is used from the "ibis.deploy.home" system property. If this
     *            property is unspecified, final default value is the current
     *            working directory.
     * @throws Exception
     *             if required files cannot be found in home, or the server
     *             cannot be started.
     * 
     */
    public Deploy(File home) throws Exception {
        this(home, false, false, null, null, true);
    }

    /**
     * Create a new deployment interface. Also deploys the server, either
     * locally or remotely.
     * 
     * @param home
     *            "home" directory of ibis-deploy. If null, the default location
     *            is used from the "ibis.deploy.home" system property. If this
     *            property is unspecified, final default value is the current
     *            working directory.
     * @param verbose
     *            If true, start Ibis-Deploy in verbose mode
     * @param keepSandboxes
     *            If true, will keep sandboxes of servers and jobs
     * @param serverCluster
     *            cluster where the server should be started, or null for a
     *            server embedded in this JVM.
     * @param listener
     *            callback object for status of server
     * @param blocking
     *            if true, will block until the server is running
     * @throws Exception
     *             if required files cannot be found in home, or the server
     *             cannot be started.
     * 
     */
    public Deploy(File home, boolean verbose, boolean keepSandboxes,
            Cluster serverCluster, StateListener listener, boolean blocking)
            throws Exception {
        logger.debug("Initializing deploy");

        this.verbose = verbose;
        this.keepSandboxes = keepSandboxes;

        jobs = new ArrayList<Job>();
        hubs = new HashMap<String, Server>();

        this.home = checkHome(home);

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
     * Create a new deployment interface. Also deploys Zorilla, embedded in this
     * JVM, and on each specified cluster.
     * 
     * @param home
     *            "home" directory of ibis-deploy. If null, the default location
     *            is used from the "ibis.deploy.home" system property. If this
     *            property is unspecified, final default value is the current
     *            working directory.
     * @param serverCluster
     *            cluster where the server should be started, or null for a
     *            server embedded in this JVM.
     * @throws Exception
     *             if required files cannot be found in home, or the server
     *             cannot be started.
     * 
     */
    public Deploy(File home, boolean verbose, boolean keepSandboxes,
            Cluster... zorillaClusters) throws Exception {
        this.verbose = verbose;
        this.keepSandboxes = keepSandboxes;

        jobs = new ArrayList<Job>();
        hubs = new HashMap<String, Server>();

        this.home = checkHome(home);

        localServer = new LocalServer(true, true, verbose);
        remoteServer = null;

        for (Cluster cluster : zorillaClusters) {
            if (cluster.getJobAdaptor().equalsIgnoreCase("zorilla")) {
                // this cluster has an existing zorilla node running, add it to
                // the
                // hub/zorilla network
                localServer.addZorillaNode(cluster.getJobURI()
                        .getSchemeSpecificPart());
            } else {
                // start a Zorilla node on the provided cluster

                RemoteZorilla node = new RemoteZorilla(cluster, localServer,
                        home, verbose, null, keepSandboxes);

                node.waitUntilRunning();

                hubs.put(cluster.getName(), node);
            }
        }
        
        // print pool size statistics
        poolSizePrinter = new PoolSizePrinter(this);

        logger.info("Ibis Deploy initialized, root hub/server/zorilla address is "
                + localServer.getAddress());
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
     * If set to true, will keep all sandboxes for jobs. This is turned off by
     * default
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
    public String getRootHubAddress() throws Exception {
        return localServer.getAddress();
    }

    /**
     * Returns the build-in root hub.
     * 
     * @return the build-in root hub.
     * @throws Exception
     *             if ibis-deploy has not been initialized yet.
     */
    LocalServer getRootHub() throws Exception {
        return localServer;
    }
    
    /**
     * Returns the build-in root hub.
     * 
     * @return the build-in root hub.
     * @throws Exception
     *             if ibis-deploy has not been initialized yet.
     */
    public Server getServer() throws Exception {
        if (remoteServer == null) {
            return localServer;
        } else {
            return remoteServer;
        }
    }


    /**
     * Retrieves address of server. May block if server has not been started
     * yet.
     * 
     * @return address of server
     * @throws Exception
     *             if server state cannot be retrieved.
     */
    public String getServerAddress() throws Exception {
        if (remoteServer == null) {
            return localServer.getAddress();
        } else {
            return remoteServer.getAddress();
        }
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

        Server result = hubs.get(clusterName);

        if (result == null || result.isFinished()) {
            // new server needed
            result = new RemoteServer(cluster, true, localServer, home,
                    verbose, listener, keepSandboxes);
            hubs.put(clusterName, result);
        } else {
            result.addListener(listener);
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

        for (Server hub : hubs.values()) {
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
