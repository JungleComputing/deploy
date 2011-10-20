package ibis.deploy;

import ibis.deploy.util.PoolSizePrinter;
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
            "log4j.properties" };

    private static final Logger logger = LoggerFactory.getLogger(Deploy.class);

    // local "root" hub (perhaps including a server)
    private final LocalServer localServer;

    // remote server (if it exists)
    private final RemoteServer remoteServer;

    // home dir of ibis-deploy
    private final File home;

    // promote some logging prints from debug to info
    private boolean verbose;

    private boolean keepSandboxes;
    
    private final boolean monitoringEnabled;

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
        this(home, false, false, 0, null, null, true);
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
     * @param port
     *            port used to bind local hub/server to. Defaults to
     *            automatically allocated free port.
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
    public Deploy(File home, boolean verbose, boolean keepSandboxes, int port,
            Cluster serverCluster, StateListener listener, boolean blocking)
            throws Exception {
	this(home, verbose, keepSandboxes, false, port, serverCluster, listener, blocking);
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
     * @param monitoringEnabled
     * 		  If true, set collecting system properties when jobs are started.
     * @param port
     *            port used to bind local hub/server to. Defaults to
     *            automatically allocated free port.
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
    public Deploy(File home, boolean verbose, boolean keepSandboxes, boolean monitoringEnabled, int port,
            Cluster serverCluster, StateListener listener, boolean blocking)
            throws Exception {
    
    
        logger.debug("Initializing deploy");

        this.verbose = verbose;
        this.keepSandboxes = keepSandboxes;
        this.monitoringEnabled = monitoringEnabled;

        jobs = new ArrayList<Job>();
        hubs = new HashMap<String, Server>();

        this.home = checkHome(home);

        if (serverCluster == null) {
            // rootHub includes server
            localServer = new LocalServer(true, verbose, port);
            localServer.addListener(listener);
            remoteServer = null;
        } else {
            localServer = new LocalServer(false, verbose, port);
            remoteServer = new RemoteServer(serverCluster, false, localServer,
                    this.home, verbose,

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
     * Returns whether the collecting flag is set.
     * @return the collecting flag.
     */
    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
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
     * Returns the address of the built-in root hub.
     * 
     * @return the address of the built-in root hub.
     * @throws Exception
     *             if ibis-deploy has not been initialized yet.
     */
    public String getRootHubAddress() throws Exception {
        return localServer.getAddress();
    }

    /**
     * Returns the built-in root hub.
     * 
     * @return the built-in root hub.
     * @throws Exception
     *             if ibis-deploy has not been initialized yet.
     */
    LocalServer getRootHub() throws Exception {
        return localServer;
    }

    /**
     * Returns the built-in server.
     * 
     * @return the built-in server.
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
  
    public synchronized Job submitJob(JobDescription description,
            ApplicationSet applicationSet, Grid grid,
            StateListener jobListener, StateListener hubListener)
            throws Exception {
  
        Application application = applicationSet.getApplication(description
                .getApplication().getName());
        Cluster cluster = grid.getCluster(description.getCluster().getName());

        return submitJob(description, application, cluster, jobListener,
                hubListener);
    }

    /**
     * Submit a new job.
     * 
     * @param description
     *            description of the job.
     * @param application
     *            application for job
     * @param cluster
     *            cluster to use
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
            Application application, Cluster cluster,
            StateListener jobListener, StateListener hubListener)
            throws Exception {
        if (remoteServer != null && !remoteServer.isRunning()) {
            throw new Exception("Cannot submit job (yet), server \""
                    + remoteServer + "\" not running");
        }

        // resolve given description into single "independent" description
        JobDescription resolvedDescription = description.resolve(application,
                cluster);

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
            hub = getClusterHub(resolvedDescription.getCluster(), false,
                    hubListener);
        }

        // start job
        Job job = new Job(resolvedDescription, hubPolicy, hub, keepSandboxes,
                jobListener, hubListener, localServer, verbose, home,
                getServerAddress(), this, monitoringEnabled);

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
     * Returns a map containing all locations of a pool at the server
     * 
     * @return a map containing all locations of a pool at the server
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
     * Parameter indicates whether a message is to be printed (yes when
     * using GUI, no when CLI).
     */
    public synchronized void end() {
        logger.info("ending ibis-deploy engine");

        if (poolSizePrinter != null) {
            poolSizePrinter.end();
        }
        
        if (jobs.size() > 0 || hubs.size() > 0 || remoteServer != null || localServer != null) {
            logger.info("Killing jobs and/or servers, this may take a couple of seconds ...");
        }

        for (Job job : jobs) {
            logger.info("killing job " + job);
            (new Killer(job)).start();
        }
        
        Killer.waitForKillers();

        for (Server hub : hubs.values()) {
            logger.info("killing Hub " + hub);
            (new Killer(hub)).start();
        }

        if (remoteServer != null) {
            logger.info("killing Server " + remoteServer);
            (new Killer(remoteServer)).start();
        }

        if (localServer != null) {
            logger.info("killing Server " + localServer);
            (new Killer(localServer)).start();
        }        
        
        Killer.waitForKillers();

        logger.info("ending GAT");

        GAT.end();
        logger.info("ending ibis-deploy engine DONE :)");
    }
    
    private static class Killer extends Thread {
	
	static int count = 0;

	Job job = null;
	Server server = null;
	
	public Killer(Job job) {
	    synchronized(Killer.class) {
		count++;
	    }
	    this.job = job;
	}
	
	public Killer(Server server) {
	    synchronized(Killer.class) {
		count++;
	    }
	    this.server = server;
	}
	
	public void run() {
	    if (job != null) {
		job.kill();
	    } else if (server != null) {
		server.kill();
	    }
	    synchronized(Killer.class) {
		count--;
		if (count == 0) {
		    Killer.class.notifyAll();
		}
	    }
	}
	
	public synchronized static void waitForKillers() {
	    while (count != 0) {
		try {
		    Killer.class.wait();
		} catch(InterruptedException e) {
		    // ignored
		}
	    }
	}
    }

}
