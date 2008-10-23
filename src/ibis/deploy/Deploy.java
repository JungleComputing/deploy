package ibis.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    Deploy() {
        rootHub = null;
        server = null;
        serverLibs = null;

        jobs = new ArrayList<Job>();
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

        if (serverCluster == null) {
            logger.info("Initializing deployer, server build-in");

            rootHub = new Server(false);
            server = rootHub;
        } else {
            logger.info("Initializing deployer, server running on: "
                    + serverCluster);

            rootHub = new Server(true);
            server = new Server(serverLibs, serverCluster, false);
            rootHub.addHubs(server.getAddress());
        }
        logger.info("Deployer initialized succesfully");
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
     * @param application
     *            application to run.
     * @param processCount
     *            number of processes to start on the allocated resources
     * @param poolName
     *            name of the pool to join
     * @param listener
     *            callback object for status of job
     * @return the resulting job
     * @throws Exception
     */
    public synchronized Job submit(Grid grid, Cluster cluster,
            int resourceCount, Application application, int processCount,
            String poolName, MetricListener listener) throws Exception {
        if (rootHub == null) {
            throw new Exception("Deployer not initialized, cannot submit jobs");
        }

        // start job
        Job job = new Job(grid, cluster, resourceCount, application,
                processCount, poolName, server.getAddress(), rootHub,
                serverLibs);

        jobs.add(job);

        return job;
    }

    /**
     * Ends all jobs and closes all open connections.
     */
    public synchronized void end() {
        logger.info("ending ibis-deploy engine");
        for (Job job : jobs) {
            job.kill();
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
