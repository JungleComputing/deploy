package ibis.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.GAT;
import org.gridlab.gat.URI;

public class Deploy {

	// "root" hub (perhaps including the server)
	private Server rootHub;

	// server (possibly remote)
	private Server server;

	// libraries used to start a server/hub
	private File[] serverLibs;

	private List<Job> jobs;

	// running hubs.
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
	 * @throws Exception if the sercer cannot be started
	 */
	synchronized void initialize(File[] serverLibs, Cluster serverCluster) throws Exception {
		if (serverLibs == null) {
			throw new Exception("no server libraries specified");
		}
		this.serverLibs = serverLibs.clone();
		//TODO: load server libs into this JVM
		
		if (serverCluster == null) {
			//build-in server, including hub
			rootHub = new Server(serverLibs, false);
			server = rootHub;
		} else {
			rootHub = new Server(serverLibs, true);
			server = new Server(serverLibs, serverCluster, false);
			rootHub.addHubs(server.getAddress());
		}
	}

	// returns a hub running on the specified cluster, or starts a new one if
	// needed (or forced)
	private synchronized Server getHub(Cluster cluster, boolean forceNew) {
		if (!forceNew) {
			for (Server server : hubs) {
				if (server.getClusterURI().equals(cluster.getServerURI())) {
					return server;
				}
			}
		}

		// no hub found at specified cluster, or new hub forced
		Server result = new Server(serverLibs, cluster, true);
		
		//notify root hub of new hub
		rootHub.addHubs(result.getAddress());
		
		//add to list
		hubs.add(result);

		return result;
	}
	
	

	/**
	 * Submit a new job.
	 * 
	 * @param cluster
	 *            cluster to submit job to
	 * @param resourceCount
	 *            number of resources to allocate on the cluster
	 * @param aplication
	 *            application
	 * @param processCount
	 *            number of processes to start on the allocated resources
	 * @param poolName
	 *            name of the pool to join
	 * @param forceNewHub
	 *            If true, a new hub is started per job. If false, a single hub
	 *            is started per cluster.
	 * @return
	 * @throws Exception
	 */
	public synchronized Job submit(Cluster cluster, int resourceCount,
			Application application, int processCount, String poolName,
			boolean forceNewHub) throws Exception {
		if (rootHub == null) {
			throw new Exception("Deployer not initialized, cannot submit jobs");
		}

		// start a new hub on the specified cluster (if needed)
		Server hub = getHub(cluster, forceNewHub);
		
		// start job
		Job job = new Job(cluster, resourceCount, application, processCount,
				poolName, server.getAddress(), hub.getAddress(), rootHub.getHubs());

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
