package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.URI;

public class Deployer {

    private static Logger logger = Logger.getLogger(Deployer.class);

    private Set<Application> applications = new HashSet<Application>();

    private Set<Grid> grids = new HashSet<Grid>();

    private Cluster serverCluster;

    /**
     * Creates a deployer. The deployments with this deployer have a server on
     * the localhost.
     */
    public Deployer() {
        Cluster cluster = null;
        try {
            cluster = new Cluster("server", new URI("any://localhost"));
        } catch (URISyntaxException e) {
        }
        cluster.setAccessType("local");
        cluster.setFileAccessType("local");
        cluster.setJavapath(System.getProperty("java.home"));
        this.serverCluster = cluster;
    }

    /**
     * Creates a deployer. The deployments with this deployer have a server
     * which will be submitted to the deploy broker of the cluster.
     * 
     * @param serverCluster
     *            the cluster to be used to deploy the server on
     */
    public Deployer(Cluster serverCluster) {
        if (logger.isInfoEnabled()) {
            logger.info("constructor");
        }
        this.serverCluster = serverCluster;
    }

    /**
     * Adds a {@link Set} of {@link Application}s to this deployer. These
     * Applications can be referred to by a run, a {@link Job} or a
     * {@link SubJob}.
     * 
     * @param applications
     *            the {@link Application}s to be added.
     */
    public void addApplications(Set<Application> applications) {
        this.applications.addAll(applications);
    }

    /**
     * Adds a single {@link Application} to this deployer. This Application can
     * be referred to by a run, a {@link Job} or a {@link SubJob}.
     * 
     * @param application
     *            the {@link Application} to be added.
     */
    public void addApplication(Application application) {
        this.applications.add(application);
    }

    /**
     * Adds all {@link Application}s described in a properties file to this
     * deployer. These Applications can be referred to by a run, a {@link Job}
     * or a {@link SubJob}.
     * 
     * @param applicationFileName
     *            the properties file describing the {@link Application}s to be
     *            added.
     * @throws FileNotFoundException
     *             if the file cannot be found
     * @throws IOException
     *             if an IO error occurs during the loading from the file.
     */
    public void addApplications(String applicationFileName)
            throws FileNotFoundException, IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(applicationFileName));
        addApplications(properties);
    }

    /**
     * Adds all {@link Application}s described in a the {@link TypedProperties}
     * object to this deployer. These Applications can be referred to by a run,
     * a {@link Job} or a {@link SubJob}.
     * 
     * @param properties
     *            the properties describing the {@link Application}s to be
     *            added.
     */
    public void addApplications(TypedProperties properties) {
        properties.expandSystemVariables();
        addApplications(Application.load(properties));
    }

    /**
     * Adds a {@link Grid} to this deployer. This Grid can be referred to by a
     * run, a {@link Job} or a {@link SubJob}.
     * 
     * @param grid
     *            the {@link Grid} to be added
     */
    public void addGrid(Grid grid) {
        this.grids.add(grid);
    }

    /**
     * Adds a {@link Grid} described in a properties file to this deployer. This
     * Grid can be referred to by a run, a {@link Job} or a {@link SubJob}.
     * 
     * @param gridFileName
     *            the properties file describing the {@link Grid} to be added.
     * @throws FileNotFoundException
     *             if the file cannot be found
     * @throws IOException
     *             if an IO error occurs during the loading from the file.
     */
    public void addGrid(String gridFileName) throws FileNotFoundException,
            IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(gridFileName));
        addGrid(properties);
    }

    /**
     * Adds a {@link Grid} described in a {@link TypedProperties} object to this
     * deployer. This Grid can be referred to by a run, a {@link Job} or a
     * {@link SubJob}.
     * 
     * @param properties
     *            the properties object describing the {@link Grid} to be added.
     */
    public void addGrid(TypedProperties properties) {
        properties.expandSystemVariables();
        addGrid(Grid.load(properties));
    }

    /**
     * Deploys a {@link Job}. In order to deploy a job, first a server is
     * started at the location defined at the construction of this deployer.
     * Then the hubs are started and finally all {@link SubJob}s are started.
     * The server and hubs will live as long as the job lives. All sub jobs will
     * run in a single pool.
     * 
     * @param job
     *            the job to be deployed
     * @throws Exception
     *             if the deployment fails
     */
    public void deploy(Job job) throws Exception {
        // start server
        job.initIbis(serverCluster);
        job.submit();
    }

    /**
     * Deploys a {@link SubJob}. In order to deploy a sub job, first a server
     * is started at the location defined at the construction of this deployer.
     * Then the hubs are started and finally the sub job is started. The server
     * and hubs will live as long as the sub job lives.
     * 
     * @param subjob
     *            the job to be deployed
     * @throws Exception
     *             if the deployment fails
     */
    public void deploy(SubJob subjob) throws Exception {
        Job job = new Job("empty container job");
        job.addSubJob(subjob);
        deploy(job);
    }

    /**
     * Adds a {@link SubJob} to a {@link Job}. The server will be or is already
     * started by the Job, the sub job only starts a hub and then will run the
     * sub job.
     * 
     * @param subjob
     *            the sub job to be deployed in the same pool as the job
     * @param job
     *            the job with the pool where the sub job joins
     * @throws Exception
     *             if the deployment fails
     */
    public void deploy(SubJob subjob, Job job) throws Exception {
        job.initHub(subjob.getCluster());
        job.addSubJob(subjob);
        job.singleSubmit(subjob);
    }

    /**
     * Ends all jobs and closes all open connections.
     */
    public void end() {
        GAT.end();
    }

    /**
     * Returns the {@link Set} of {@link Application}s associated with this
     * deployer
     * 
     * @return the {@link Set} of {@link Application}s associated with this
     *         deployer
     */
    public Set<Application> getApplications() {
        return applications;
    }

    /**
     * Returns the {@link Set} of {@link Grid}s associated with this deployer
     * 
     * @return the {@link Set} of {@link Grid}s associated with this deployer
     */
    public Set<Grid> getGrids() {
        return grids;
    }

    /**
     * Gets the {@link Grid} associated with this deployer with the provided
     * name
     * 
     * @return the {@link Grid} associated with this deployer with the provided
     *         name
     */
    public Grid getGrid(String gridName) {
        if (grids == null) {
            return null;
        }
        for (Grid grid : grids) {
            if (grid.getGridName().equalsIgnoreCase(gridName)) {
                return grid;
            }
        }
        return null;
    }

    /**
     * Loads a run from a properties file. All the jobs described in the
     * properties file will be returned.
     * 
     * @param runFileName
     *            the file containing the run properties
     * @return The {@link List} of {@link Job}s, loaded from the run file.
     * @throws FileNotFoundException
     *             if the file cannot be found
     * @throws IOException
     *             if something fails during the loading of the run.
     */
    public List<Job> loadRun(String runFileName) throws FileNotFoundException,
            IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new java.io.FileInputStream(runFileName));
        return loadRun(properties);
    }

    /**
     * Loads a run from a TypedProperties object. All the jobs described in the
     * properties object will be returned.
     * 
     * @param properties
     *            the properties containing the details of the run
     * @return The {@link List} of {@link Job}s, loaded from the run
     *         properties.
     */
    public List<Job> loadRun(TypedProperties properties) {
        properties.expandSystemVariables();
        return Run.load(properties, grids, applications);
    }

}
