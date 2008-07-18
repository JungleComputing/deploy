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
import org.gridlab.gat.GATInvocationException;
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

    private Server ibisServer = null;

    private String outputDirectory = "";

    /**
     * Create a {@link Job} with the name <code>name</code>
     *
     * @param name
     *            the name of the {@link Job}
     */
    public Job(String name) {
        this.name = name;
    }

    /**
     * Adds a {@link SubJob} to this {@link Job}
     *
     * @param subjob
     *            the {@link SubJob} to be added
     */
    public void addSubJob(SubJob subjob) {
        subjob.setParent(this);
        subjobs.add(subjob);
        logger.debug("adding subjob completed");
    }

    /**
     * Add a server for this job.
     * @param server
     */
    public void setServer(Server server) {
        ibisServer = server;
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
        int totalCPUs = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            SubJob subJob = subjobs.get(j);
            totalCPUs += subJob.getNodes() * subJob.getMulticore();
        }

        return totalCPUs;
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
        int totalMachines = 0;
        int totalCPUs = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            res += "Job " + name + ": ";
            SubJob subJob = subjobs.get(j);
            res += subJob + "\n";
            totalMachines += subJob.getNodes();
            totalCPUs += subJob.getNodes() * subJob.getMulticore();
        }
        res += " total machines in run: " + totalMachines + " for a total of "
                + totalCPUs + " CPUs\n";
        return res;
    }

    protected static Job load(TypedProperties runprops, Set<Grid> grids,
            Set<Application> applications, String jobName) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("loading job");
        }
        Job job = new Job(jobName);

        String[] subjobNames = TypedPropertiesUtility
                .getHierarchicalStringList(runprops, jobName, "subjobs", null,
                        ",");

        // runprops.getStringList("subjobs");
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
     *            <code>true</code> if the Job is closed world,
     *            <code>false</code> otherwise
     */
    public void setClosedWorld(boolean closedWorld) {
        this.closedWorld = closedWorld;
    }

    /**
     * Gets the size of the pool
     *
     * @return the size of the pool
     * @throws Exception
     *             if one of the {@link SubJob#getPoolSize()} throws an
     *             exception
     */
    public int getPoolSize() throws Exception {
        int result = 0;
        for (SubJob subjob : subjobs) {
            result += subjob.getPoolSize();
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
     * Starts the specified server on the servers cluster
     * @param server
     * @return
     * @throws Exception
     */
    private RemoteClient startServer(Server server) throws Exception {
        if (!server.isStarted()) {
            this.deployJobs.add(server.startServer(this));
            this.deployClients.add(server.getServerClient());
            return server.getServerClient();
        }
        else {
            throw new Exception("Server already started!");
        }
    }

    /**
     * Initializes the ibis server which will act as registry and smartsockets hub
     * @param serverCluster The cluster to start the registry on
     * @throws Exception if something goes wrong starting the server
     */
    protected void initIbis(Cluster serverCluster) throws Exception {
        if (ibisServer == null) {
            ibisServer = new Server("ibis-server", serverCluster);
        }
        startServer(ibisServer);
    }

    // // ... and the hubs ...
    // for (SubJob subjob : subjobs) {
    //
    // System.out.println("CALL 2*");
    //
    // startServer(subjob.getCluster(), true);
    // }
    //
    // // ... then wait until everyone is up and running ...
    // Collection<org.gridlab.gat.resources.Job> jobs = deployJobs.values();
    // for (org.gridlab.gat.resources.Job job : jobs) {
    // logger.debug("waiting until job '" + job.getJobID()
    // + "' is in state RUNNING");
    // while (job.getState() != org.gridlab.gat.resources.Job.RUNNING) {
    //
    // System.out.println("WAIT1");
    //
    // Thread.sleep(1000);
    // }
    // }
    // // ... and update the server and the hubs.
    // Set<URI> uris = deployClients.keySet();
    // for (URI uri : uris) {
    // logger.debug("adding hub addresses '" + getHubAddresses(uri, false)
    // + "' to hub @ '" + uri + "'");
    // deployClients.get(uri).addHubs(getHubAddresses(uri, false));
    // }
    // }

    // protected void initHub(Cluster hubCluster) throws Exception {
    // // first store the hubs known so far...
    // String[] knownHubs = getHubAddresses(null, false);
    // // then start the new hub
    //
    // System.out.println("CALL 3");
    //
    // if (!startServer(hubCluster, true)) {
    // return;
    // }
    //
    // // and wait until it's running
    // // TODO also test for stopped, post staging and submission error!
    // while (deployJobs.get(hubCluster.getDeployBroker()).getState() !=
    // org.gridlab.gat.resources.Job.RUNNING) {
    //
    // System.out.println("WAIT2");
    //
    // Thread.sleep(1000);
    // }
    // logger.debug("retrieve hub address");
    // // then retrieve its address
    // String hubAddress = deployClients.get(hubCluster.getDeployBroker())
    // .getLocalAddress();
    // logger.debug("retrieve hub address done");
    // // and update all already running hubs
    // deployClients.get(serverURI).addHubs(hubAddress);
    // // Set<URI> keys = deployClients.keySet();
    // // for (URI key : keys) {
    // // logger.debug("adding hubaddress " + hubAddress
    // // + " to existing hub " + key + " ("
    // // + deployClients.get(key).getLocalAddress() + ")");
    // // deployClients.get(key).addHubs(hubAddress);
    // // logger.debug("adding hubaddress to existing hub done");
    // //
    // // }
    // // Collection<RemoteClient> clients = deployClients.values();
    // //
    // // for (RemoteClient client : clients) {
    // // logger
    // // .debug("adding hubaddress " + hubAddress
    // // + " to existing hub");
    // // client.addHubs(hubAddress);
    // // logger.debug("adding hubaddress to existing hub done");
    // // }
    // // finally add the known hubs to the new hub
    // deployClients.get(hubCluster.getDeployBroker()).addHubs(knownHubs);
    // }

    // private String[] getHubAddresses(URI first, boolean excludeServer)
    // throws IOException {
    // ArrayList<String> result = new ArrayList<String>();
    // Set<URI> uris = deployClients.keySet();
    // for (URI uri : uris) {
    // logger.debug("getting addresses for uri : " + uri);
    // if (excludeServer && uri.equals(serverURI)) {
    // continue;
    // }
    // if (uri.equals(first)) {
    // result.add(0, deployClients.get(uri).getLocalAddress());
    // } else {
    // result.add(deployClients.get(uri).getLocalAddress());
    // }
    // logger.debug("last after adding address for uri: " + uri + " = "
    // + result.get(result.size() - 1));
    // }
    // logger.debug("getHubAddresses done!");
    // return result.toArray(new String[result.size()]);
    // }

    /**
     * Stops this job and all associated servers
     * @throws Exception if something goes wrong while trying to stop things
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

    public void processMetricEvent(MetricEvent arg0) {
        if (logger.isInfoEnabled()) {
            try {
                logger.info("received a state change for server/hub '"
                        + ((org.gridlab.gat.resources.Job) arg0.getSource())
                                .getJobID() + "': " + arg0.getValue());
            } catch (GATInvocationException e) {
                // ignore
            }
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

    // public String getHubAddresses() throws IOException {
    // String[] hubAddresses = getHubAddresses(serverURI, false);
    // if (hubAddresses == null) {
    // return "";
    // } else {
    // String hubAddressesString = "";
    // for (int i = 0; i < hubAddresses.length; i++) {
    // hubAddressesString += hubAddresses[i];
    // if (i != hubAddresses.length - 1) {
    // hubAddressesString += ",";
    // }
    // }
    // return hubAddressesString;
    // }
    // }

    // protected void submit() throws Exception {
    // if (poolID == null) {
    // poolID = generatePoolID();
    // }
    // logger.debug("submitting new job with pool id '" + poolID
    // + "', subjobs.size=" + subjobs.size());
    // for (SubJob subjob : subjobs) {
    // String serverAddress = deployClients.get(serverURI)
    // .getLocalAddress();
    // String[] hubAddresses = getHubAddresses(subjob.getCluster()
    // .getDeployBroker(), true);
    // subjob.submit(poolID, getPoolSize(), serverAddress, hubAddresses);
    // }
    // }

    protected void singleSubmit(SubJob subjob) throws Exception {
        logger.info("submitting subjob: " + subjob.getName());

        if (subjob.getHub() == null) {
            subjob.setHub(new Server(subjob.getName(), subjob.getCluster(), ibisServer));
        }
        RemoteClient ibisHub = startServer(subjob.getHub());

        String hubAddress = ibisHub.getLocalAddress();

        logger.info("adding just started hub '" + hubAddress
                + "' to ibis server");
        ibisServer.getServerClient().addHubs(hubAddress);

        subjob.submit(poolID, getPoolSize(), getServerAddress(), hubAddress, outputDirectory);
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
}