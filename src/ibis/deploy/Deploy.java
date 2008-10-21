package ibis.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GAT;
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

	// running hubs. Does not include hubs specificically started for a single
	// job
	private List<Server> hubs;

	Deploy() {
		rootHub = null;
		server = null;
		serverLibs = null;

		jobs = new ArrayList<Job>();
		hubs = new ArrayList<Server>();
	}

	/**
	 * Initialize this deployment object.
	 * 
	 * @param serverLibs
	 *            All required files and directories to start a server or hub.
	 *            Jar files will also be loaded into this JVM automatically.
	 * @param serverCluster
	 *            cluster where the server should be started, or null for a
	 *            server embedded in this JVM.
	 * @throws Exception
	 *             if the sercer cannot be started
	 */
	synchronized void initialize(File[] serverLibs, Cluster serverCluster)
			throws Exception {
		if (serverLibs == null) {
			throw new Exception("no server libraries specified");
		}
		this.serverLibs = serverLibs.clone();
		// TODO: load server libs into this JVM

		if (serverCluster == null) {
			logger.info("Initializing deployer, server build-in");

			rootHub = new Server(serverLibs, false);
			server = rootHub;
		} else {
			logger.info("Initializing deployer, server running on: "
					+ serverCluster);

			rootHub = new Server(serverLibs, true);
			server = new Server(serverLibs, serverCluster, false);
			rootHub.addHubs(server.getAddress());
		}
	}

	// returns a hub running on the specified cluster.
	synchronized Server getHub(Cluster cluster) {
		for (Server server : hubs) {
			if (server.getClusterURI().equals(cluster.getServerURI())) {
				return server;
			}
		}

		// no hub found at specified cluster, start a new one
		Server result = new Server(serverLibs, cluster, true);

		// notify root hub of new hub
		rootHub.addHubs(result.getAddress());

		// add to list
		hubs.add(result);

		return result;
	}

	/**
	 * Submit a new job.
	 *
	 * @param grid
	 *            grid to submit job to
	 * @param cluster
	 *            cluster within grid to submit job to
	 * @param resourceCount
	 *            number of resources to allocate on the cluster
	 * @param aplication
	 *            application
	 * @param processCount
	 *            number of processes to start on the allocated resources
	 * @param poolName
	 *            name of the pool to join
	 * @param localHub
	 *            If true, a new hub is started for this job. If false, a single
	 *            hub is started per cluster.
	 * @return
	 * @throws Exception
	 */
	public synchronized Job submit(Grid grid, Cluster cluster, int resourceCount,
			Application application, int processCount, String poolName,
			boolean localHub) throws Exception {
		if (rootHub == null) {
			throw new Exception("Deployer not initialized, cannot submit jobs");
		}

		// start job
		Job job = new Job(grid, cluster, resourceCount, application, processCount,
				poolName, server.getAddress(), rootHub, localHub);

		jobs.add(job);

		return job;
	}

	/**
	 * Ends all jobs and closes all open connections.
	 */
	public void end() {
		GAT.end();
	}

}
