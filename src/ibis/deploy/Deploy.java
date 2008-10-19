package ibis.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Deploy {

	// "root" server
	private Server rootServer;

	private List<Grid> grids;

	private ApplicationGroup applications;

	private List<Job> jobs;

	private Set<String> pools;

	Deploy(File[] gridFiles, File applicationsFile) {
		rootServer = null;
		grids = new ArrayList<Grid>();

		if (gridFiles != null) {
			for (File file : gridFiles) {
				Grid grid = new Grid(file);
				grids.add(grid);
			}
		}

		this.applications = new ApplicationGroup(applicationsFile);

		jobs = new ArrayList<Job>();

		pools = new HashSet<String>();
	}
	
	public Grid addGrid() {
		Grid result = new Grid();
		grids.add(result);
		return result;
	}

	public synchronized Grid getGrid(String gridName) {
		for(Grid grid: grids) {
			if (grid.getName().equals(gridName)) {
				return grid;
			}
		}
		return null;
	}

	private synchronized void addPool(String poolName) {
		pools.add(poolName);
	}

	private synchronized Server getRootServer() {
		if (rootServer == null) {
			rootServer = new Server(applications.getServerLibs(), false, false);
		}
		return rootServer;
	}

	// submit a new job
	public Job submit(String gridName, String clusterName, int resourceCount,
			String applicationName, int processCount, String poolName)
			throws Exception {
		// find grid
		Grid grid = getGrid(gridName);
		if (grid == null) {
			throw new Exception(gridName + " not a valid grid");
		}

		// find cluster in grid
		Cluster cluster = grid.getCluster(clusterName);
		if (cluster == null) {
			throw new Exception(clusterName + " not a cluster of " + gridName);
		}

		// find application
		Application application = applications.getApplication(applicationName);
		if (application == null) {
			throw new Exception("cannot find application: " + applicationName);
		}

		// make sure the root server is started (requires a configured
		// application)
		Server rootServer = getRootServer();

		// ensure a hub is running on the specified cluster
		cluster.startHub(rootServer);

		// start job
		Job job = new Job(cluster, resourceCount, application, processCount,
				poolName);

		synchronized (this) {
			jobs.add(job);
		}

		// add pool to known pool names
		addPool(poolName);

		return job;
	}

}
