package ibis.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Deploy {

    // "root" hub (perhaps including the server)
    private Hub rootHub;

    // server (possibly remote)
    private Hub server;

    private List<Job> jobs;

    Deploy() {
        rootHub = null;
        server = null;

        jobs = new ArrayList<Job>();
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
     */
    void initialize(File[] serverLibs, Cluster serverCluster) {

    }
    
    private Hub getHub(Cluster cluster, boolean forceNew) {
        return null;
        
    }
    

    // submit a new job
    /**
     * @param serverCluster,
     */
    public Job submit(Cluster cluster, int resourceCount,
            Application aplication, int processCount, String poolName,
            boolean startHub) throws Exception {

        // ensure a hub is running on the specified cluster
        Hub hub = getHub(cluster);
        

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
