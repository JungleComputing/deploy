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
	private Server rootHub;

	// server (possibly remote)
	private Server server;

	// libraries used to start a server/hub
	private File[] serverLibs;

	// submitted jobs
	private List<Job> jobs;

	// Map<gridName, Map<clusterName, Server>> with "gobal" hubs
	private Map<String, Map<String, Server>> hubs;

	Deploy() {
		rootHub = null;
		server = null;
		serverLibs = null;

		jobs = new ArrayList<Job>();
		hubs = new HashMap<String, Map<String,Server>>(); 
		
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

			rootHub = new Server(false, null);
			server = rootHub;
		} else {
			rootHub = new Server(true, null);
			server = new Server(serverLibs, serverCluster, false, rootHub);

			//add server to map of hubs (no need to start another hub on the 
			//same cluster later)
			Map<String, Server> clusterMap = new HashMap<String, Server>();
			clusterMap.put(serverCluster.getName(), server);
			hubs.put(serverCluster.getGridName(), clusterMap);
			
			//wait until server is started
			server.waitUntilRunning();
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
	 * @param listener
	 *            callback object for status of job
	 *  @param globalHub
	 *            If true, a "global" hub is used, started on each cluster and shared between all hubs.
	 *            If false, a hub is created specifically for this job, and stopped after the job completes. 
	 * @return the resulting job
	 * @throws Exception
	 */
	public synchronized Job submit(Cluster cluster,
			int resourceCount, Application application, int processCount,
			String poolName, MetricListener listener, boolean globalHub) throws Exception {
		if (rootHub == null) {
			throw new Exception("Deployer not initialized, cannot submit jobs");
		}
		
		Server hub = null;
		if (globalHub) {
			hub = submitHub(cluster, false);
		}

		// start job
		Job job = new Job(cluster, resourceCount, application,
				processCount, poolName, server.getAddress(), rootHub, hub,
				serverLibs);

		jobs.add(job);

		return job;
	}

	/**
	 * Submit a hub to the given cluster. If a hub is already running, does
	 * nothing.
	 * 
	 * @param cluster
	 *            cluster to deploy the hub on
	 * @return a hub running on the given cluster
	 * @throws Exception  if the hub cannot be started
	 */
	public synchronized Server submitHub(Cluster cluster, boolean waitUntilRunning) throws Exception  {
		String clusterName = cluster.getName();
		String gridName = cluster.getGridName();
		
		logger.debug("starting hub on " + clusterName + "@" + gridName);
		
		if (clusterName == null) {
			throw new Exception("cannot start hub on an unnamed cluster. (grid = " + gridName + ")");
		}
		
		if (gridName == null) {
			throw new Exception("cannot start hub on an unnamed grid. (cluster = " + clusterName + ")");
		}
		
		//find map containing all clusters of the specified grid. Create if needed.
		Map<String, Server> clusterMap = hubs.get(gridName);
		if (clusterMap == null) {
			clusterMap = new HashMap<String, Server>();
			hubs.put(gridName, clusterMap);
		}
		
		Server result = clusterMap.get(clusterName);

		if (result == null) {
			result = new Server(serverLibs, cluster, true, rootHub);
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
		
		for(Map<String, Server> grids: hubs.values()) {
			for(Server hub: grids.values()) {
				hub.kill();
			}
		}

		if (rootHub != null) {
			rootHub.killAll();
			rootHub.kill();
		}

		if (server != null) {
			server.kill();
		}

		GAT.end();
	}

}
