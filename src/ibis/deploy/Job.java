package ibis.deploy;

import ibis.server.remote.RemoteClient;
import ibis.util.TypedProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job.JobState;

public class Job implements MetricListener {
    private static Logger logger = Logger.getLogger(Job.class);

    private List<SubJob> subjobs = new ArrayList<SubJob>();

    private String name;

    private String poolID;

    private boolean closedWorld;

    private List<org.gridlab.gat.resources.Job> deployJobs = new ArrayList<org.gridlab.gat.resources.Job>();

    private List<RemoteClient> deployClients = new ArrayList<RemoteClient>();

    private String outputDirectory = "";

    private Server ibisServer = null;

    /**
     * Create a {@link Job} with the name <code>name</code>
     * 
     * @param name
     *                the name of the {@link Job}
     */
    public Job(String name) {
        this.name = name;
    }

    /**
     * Adds a {@link SubJob} to this {@link Job}
     * 
     * @param subjob
     *                the {@link SubJob} to be added
     */
    public void addSubJob(SubJob subjob) {
        subjob.setParent(this);
        subjobs.add(subjob);
        logger.debug("adding subjob completed");
    }

    protected void inform() throws Exception {
        if (subjobs.size() == getStatus().get(JobState.STOPPED)
                + getStatus().get(JobState.SUBMISSION_ERROR)) {
            stop();
        }
    }

    /**
     * Gets the name of the Job
     * 
     * @return the name of the Job
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the ibis poolID of the Job
     * 
     * @return the ibis poolID of the Job
     */
    public String getPoolID() {
        if (poolID != null) {
            return poolID;
        } else {
            for (SubJob subjob : subjobs) {
                if (subjob.getPoolID() != null) {
                    poolID = subjob.getPoolID();
                    return poolID;
                }
            }
        }
        return null;
    }

    /**
     * Gets the total number of cores used by this Job
     * 
     * @return the total number of cores used by this Job
     */
    public int getTotalCores() {
        int totalCores = 0;
        for (SubJob subjob : subjobs) {
            totalCores += subjob.getCores();
        }

        return totalCores;
    }

    /**
     * Gets the total number of nodes used by this Job
     * 
     * @return the total number of nodes used by this Job
     */
    public int getTotalNodes() {
        int totalMachines = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            SubJob subjob = subjobs.get(j);
            totalMachines += subjob.getNodes();
        }
        return totalMachines;
    }

    /**
     * Gets the number of {@link SubJob}s of this Job
     * 
     * @return the number of {@link SubJob}s of this Job
     */
    public int numberOfSubJobs() {
        return subjobs.size();
    }

    /**
     * Sets the poolID of this Job
     * 
     * @param poolID
     */
    public void setPoolID(String poolID) {
        this.poolID = poolID;
    }

    public String toString() {
        String res = "";
        int totalNodes = 0;
        int totalCores = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            res += "Job " + name + ": ";
            SubJob subJob = subjobs.get(j);
            res += subJob + "\n";
            totalNodes += subJob.getNodes();
            totalCores += subJob.getCores();
        }
        res += " total nodes in run: " + totalNodes + " for a total of "
                + totalCores + " cores\n";
        return res;
    }

    /**
     * Loads a {@link Job} from the properties and the grids and applications.
     * The following properties can be set:
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="job properties">
     * <CAPTION>job properties </CAPTION> <COLGROUP align="left"> <COLGROUP
     * align="center"> <COLGROUP align="left" > <THEAD valign="top">
     * <TR>
     * <TH>Property
     * <TH>Example
     * <TH>Description<TBODY>
     * <TR>
     * <TD>[[job.]subjobs
     * <TD>subjob1,subjob2,subjob3
     * <TD>the names of the subjobs </TABLE>
     * 
     * @param runprops
     * @param grids
     * @param applications
     * @param jobName
     * @return a new {@link Job} loaded from the properties
     * @throws Exception
     */
    public static Job load(TypedProperties runprops, Set<Grid> grids,
            Set<Application> applications, String jobName) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("loading job");
        }
        Job job = new Job(jobName);

        String[] subjobNames = TypedPropertiesUtility
                .getHierarchicalStringList(runprops, jobName, "subjobs", null,
                        ",");

        if (subjobNames == null || subjobNames.length == 0) {
            throw new Exception("no subjobs specified for job " + job);
        } else {
            for (String subjobName : subjobNames) {
                if (logger.isInfoEnabled()) {
                    logger.info("loading subjob: " + subjobName);
                }
                SubJob[] subjobs = SubJob.load(runprops, grids, applications,
                        jobName + "." + subjobName);
                if (subjobs != null) {
                    for (SubJob subjob : subjobs) {
                        job.addSubJob(subjob);
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("subjob '" + subjobName
                                + "' succesfully loaded!");
                    }
                }

            }
        }
        if (job.subjobs.size() == 0) {
            throw new Exception("no subjobs specified for job " + job);
        } else {
            job.closedWorld = false;
            for (SubJob subjob : job.subjobs) {
                if (job.closedWorld && !subjob.isClosedWorld()) {
                    throw new Exception("adding open world subjob " + subjob
                            + " to closed world job " + job);
                } else {
                    job.closedWorld = subjob.isClosedWorld();
                }
            }
            return job;
        }
    }

    /**
     * Gets a {@link List} of the {@link SubJob}s of this Job.
     * 
     * @return a {@link List} of the {@link SubJob}s of this Job.
     */
    public List<SubJob> getSubJobs() {
        return subjobs;
    }

    /**
     * Gets the status of this Job. The status is a Map containing all the
     * possible states as keys and the number of sub jobs that are in the
     * particular state as value.
     * 
     * @return the status of this Job
     */
    public Map<JobState, Integer> getStatus() {
        Map<JobState, Integer> result = new HashMap<JobState, Integer>();
        result.put(JobState.INITIAL, 0);
        result.put(JobState.SCHEDULED, 0);
        result.put(JobState.RUNNING, 0);
        result.put(JobState.STOPPED, 0);
        result.put(JobState.SUBMISSION_ERROR, 0);
        result.put(JobState.ON_HOLD, 0);
        result.put(JobState.PRE_STAGING, 0);
        result.put(JobState.POST_STAGING, 0);
        result.put(JobState.UNKNOWN, 0);
        for (SubJob subjob : subjobs) {
            JobState subjobState = subjob.getStatus();
            int othersInSameState = result.get(subjobState);
            result.put(subjobState, (othersInSameState + 1));
        }
        return result;
    }

    /**
     * Returns whether this Job is a closed world job or an open world job.
     * 
     * @return <code>true</code> if the Job is closed world,
     *         <code>false</code> otherwise
     */
    public boolean isClosedWorld() {
        return closedWorld;
    }

    /**
     * Sets whether this Job is a closed world job or not
     * 
     * @param closedWorld
     *                <code>true</code> if the Job is closed world,
     *                <code>false</code> otherwise
     */
    public void setClosedWorld(boolean closedWorld) {
        this.closedWorld = closedWorld;
    }

    /**
     * Gets the size of the pool
     * 
     * @return the size of the pool
     */
    public int getPoolSize() {
        int result = 0;
        for (SubJob subjob : subjobs) {
            result += subjob.getCores();
        }
        return result;
    }

    Application getFirstApplication() {
        if (subjobs != null && subjobs.size() > 0) {
            return subjobs.get(0).getApplication();
        }
        return null;
    }

    /**
     * Initializes the ibis server which will act as registry and smartsockets
     * hub
     * 
     * @param serverCluster
     *                The cluster to start the registry on
     * @throws Exception
     *                 if something goes wrong starting the server
     */
    protected void initIbis(Cluster serverCluster) throws Exception {
        if (ibisServer == null) {
            ibisServer = new Server("ibis-server", serverCluster);
        }
        startServer(ibisServer);
    }

    /**
     * Stop this job and all it's subjobs and helper jobs (hubs & server)
     * 
     * @throws Exception
     */
    public void stop() throws Exception {
        for (RemoteClient client : deployClients) {
            try {
                client.end(2000);
            } catch (IOException e) {
                // ignore
            }
        }
        for (org.gridlab.gat.resources.Job job : deployJobs) {
            if (job.getState() == JobState.RUNNING) {
                job.stop();
            }
        }
    }

    /**
     * <b>DO NOT USE. For internal use only</b>
     */
    public void processMetricEvent(MetricEvent arg0) {
        if (logger.isInfoEnabled()) {
            logger.info("received a state change for server/hub '"
                    + ((org.gridlab.gat.resources.Job) arg0.getSource())
                            .getJobID() + "': " + arg0.getValue());

        }
    }

    private String generatePoolID() {
        if (logger.isInfoEnabled()) {
            logger.info("generate pool id");
        }
        return "pool-id-" + Math.random();
    }

    public String getServerAddress() throws IOException {
        return ibisServer.getServerClient().getLocalAddress();
    }

    protected void singleSubmit(SubJob subjob) throws Exception {
        logger.info("submitting subjob: " + subjob.getName());

        if (subjob.getHub() == null) {
            subjob.setHub(new Server(subjob.getName(), subjob.getCluster(),
                    ibisServer));
        }
        RemoteClient ibisHub = startServer(subjob.getHub());

        String hubAddress = ibisHub.getLocalAddress();

        logger.info("adding just started hub '" + hubAddress
                + "' to ibis server");
        ibisServer.getServerClient().addHubs(hubAddress);

        subjob.submit(poolID, getPoolSize(), getServerAddress(), hubAddress,
                outputDirectory);
    }

    protected void submit() throws Exception {
        poolID = generatePoolID();

        logger.info("generated pool id: " + poolID);

        if (subjobs != null) {
            for (SubJob subjob : subjobs) {
                singleSubmit(subjob);
            }
        }
    }

    /**
     * Sets the output directory of the job. This is the directory where all the
     * output files will end up.
     * 
     * @param outputDirectory
     *                the output directory of the job
     */
    public void setOutputDirectory(String outputDirectory) {
        if (outputDirectory == null) {
            return;
        }
        if (outputDirectory.endsWith(java.io.File.separator)) {
            this.outputDirectory = outputDirectory;
        } else {
            this.outputDirectory = outputDirectory + java.io.File.separator;
        }
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Add a server for this job.
     * 
     * @param server
     */
    public void setServer(Server server) {
        ibisServer = server;
    }

    /**
     * Starts the specified server on the servers cluster
     * 
     * @param server
     * @return
     * @throws Exception
     */
    private RemoteClient startServer(Server server) throws Exception {
        if (!server.isStarted()) {
            this.deployJobs.add(server.startServer(this));
            this.deployClients.add(server.getServerClient());
            return server.getServerClient();
        } else {
            throw new Exception("Server already started!");
        }
    }

}