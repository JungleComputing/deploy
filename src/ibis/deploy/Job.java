package ibis.deploy;

import ibis.server.remote.RemoteClient;
import ibis.util.TypedProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.security.CertificateSecurityContext;

public class Job implements MetricListener {
    private static Logger logger = Logger.getLogger(Job.class);

    private List<SubJob> subjobs = new ArrayList<SubJob>();

    private String name;

    private String poolID;

    private boolean closedWorld;

    private List<org.gridlab.gat.resources.Job> deployJobs = new ArrayList<org.gridlab.gat.resources.Job>();

    private List<RemoteClient> deployClients = new ArrayList<RemoteClient>();

    private RemoteClient serverRemoteClient;

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
        if (subjobs.size() == getStatus().get(
                org.gridlab.gat.resources.Job
                        .getStateString(org.gridlab.gat.resources.Job.STOPPED))
                + getStatus()
                        .get(
                                org.gridlab.gat.resources.Job
                                        .getStateString(org.gridlab.gat.resources.Job.SUBMISSION_ERROR))) {
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
            Set<Application> applications, String jobName) {
        if (logger.isInfoEnabled()) {
            logger.info("loading job");
        }
        Job job = new Job(jobName);

        String[] subjobNames = TypedPropertiesUtility
                .getHierarchicalStringList(runprops, jobName, "subjobs", null,
                        ",");

        // runprops.getStringList("subjobs");
        if (subjobNames == null || subjobNames.length == 0) {
            return null;
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
            return null;
        } else {
            job.closedWorld = false;
            for (SubJob subjob : job.subjobs) {
                if (job.closedWorld && !subjob.isClosedWorld()) {
                    return null;
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
    public Map<String, Integer> getStatus() {
        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.INITIAL), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.SCHEDULED), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.RUNNING), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.STOPPED), 0);
        result
                .put(
                        org.gridlab.gat.resources.Job
                                .getStateString(org.gridlab.gat.resources.Job.SUBMISSION_ERROR),
                        0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.ON_HOLD), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.PRE_STAGING), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.POST_STAGING), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.UNKNOWN), 0);
        for (SubJob subjob : subjobs) {
            String subjobState = subjob.getStatus();
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
     * @throws Exception
     *                 if one of the {@link SubJob#getPoolSize()} throws an
     *                 exception
     */
    public int getPoolSize() throws Exception {
        int result = 0;
        for (SubJob subjob : subjobs) {
            result += subjob.getPoolSize();
        }
        return result;
    }

    private Application getFirstApplication() {
        if (subjobs != null && subjobs.size() > 0) {
            return subjobs.get(0).getApplication();
        }
        return null;
    }

    private RemoteClient startServer(Cluster serverCluster, boolean hubOnly,
            String name) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("start server (hub only is " + hubOnly + ") at "
                    + serverCluster.getName());
        }
        // if (deployClients.containsKey(serverCluster.getDeployBroker())) {
        // if (logger.isInfoEnabled()) {
        // logger.info("already a hub available at: '"
        // + serverCluster.getDeployBroker() + "'");
        // }
        // return null;
        // }
        Application application = getFirstApplication();
        if (application == null) {
            // TODO do something (exception oid)
        }
        Preferences serverPreferences = new Preferences();
        if (serverCluster.getFileAccessType() != null) {
            serverPreferences.put("file.adaptor.name", serverCluster
                    .getFileAccessType());
        }
        if (serverCluster.getAccessType() != null) {
            serverPreferences.put("resourcebroker.adaptor.name", serverCluster
                    .getAccessType());
        }

        JavaSoftwareDescription sd = new JavaSoftwareDescription();
        if (serverCluster.isWindows()) {
            sd.setExecutable(serverCluster.getJavaPath() + "\\bin\\java");
        } else {
            sd.setExecutable(serverCluster.getJavaPath() + "/bin/java");
        }
        if (logger.isInfoEnabled()) {
            logger.info("executable: " + sd.getExecutable());
        }

        sd.setJavaMain("ibis.server.Server");
        if (hubOnly) {
            sd.setJavaArguments(new String[] { "--hub-only", "--remote",
                    "--port", "0", "--hub-addresses",
                    serverRemoteClient.getLocalAddress() });
        } else {
            sd.setJavaArguments(new String[] { "--remote", "--port", "0" });
        }
        sd.setJavaOptions(new String[] {
                "-classpath",
                application.getJavaClassPath(
                        application.getServerPreStageSet(), application
                                .hasCustomServerPreStageSet(), serverCluster
                                .isWindows()),
                "-Dlog4j.configuration=file:log4j.properties" });
        if (logger.isInfoEnabled()) {
            logger.info("arguments: " + Arrays.deepToString(sd.getArguments()));
        }
        if (application.getServerPreStageSet() != null) {
            for (String filename : application.getServerPreStageSet()) {
                sd
                        .addPreStagedFile(GAT.createFile(serverPreferences,
                                filename));
            }
        }
        RemoteClient ibisServer = new RemoteClient();
        sd.setStderr(GAT.createFile(serverPreferences, "hub-"
                + serverCluster.getName() + "-" + name + ".err"));

        sd.setStdout(ibisServer.getOutputStream());
        sd.setStdin(ibisServer.getInputStream());
        JobDescription jd = new JobDescription(sd);
        if (logger.isDebugEnabled()) {
            logger.debug("starting server at '"
                    + serverCluster.getDeployBroker() + "'"
                    + " with username '" + serverCluster.getUserName() + "'");
        }

        GATContext context = new GATContext();
        context.addSecurityContext(new CertificateSecurityContext(null, null,
                serverCluster.getUserName(), serverCluster.getPassword()));

        ResourceBroker broker = GAT.createResourceBroker(context,
                serverPreferences, serverCluster.getDeployBroker());

        org.gridlab.gat.resources.Job job = broker.submitJob(jd, this,
                "job.status");

        while (true) {
            int state = job.getState();
            if (state == org.gridlab.gat.resources.Job.RUNNING) {
                // add job to lijstje
                logger.info("hub/server is running");
                deployJobs.add(job);
                deployClients.add(ibisServer);
                break;
            } else if (state == org.gridlab.gat.resources.Job.STOPPED
                    || state == org.gridlab.gat.resources.Job.SUBMISSION_ERROR) {
                // do something useful in case of error
                logger.info("hub job already stopped or submission error");
            } else {
                logger.info("waiting until hub is in state RUNNING");
                Thread.sleep(1000);
            }
        }

        return ibisServer;
        // stop het in de administratie
        // wacht tot de job running is
        // return de remote client?

        // deployJobs.put(serverCluster.getDeployBroker(), broker.submitJob(jd,
        // this, "job.status"));
        // deployClients.put(serverCluster.getDeployBroker(), ibisServer);
        //
        // return true;
    }

    protected void initIbis(Cluster serverCluster) throws Exception {
        // start the server ...
        RemoteClient ibisServer = startServer(serverCluster, false,
                "ibis-server");
        serverRemoteClient = ibisServer;
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

    public void stop() throws Exception {
        for (RemoteClient client : deployClients) {
            try {
                client.end(2000);
            } catch (IOException e) {
                // ignore
            }
        }
        for (org.gridlab.gat.resources.Job job : deployJobs) {
            if (job.getState() == org.gridlab.gat.resources.Job.RUNNING) {
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
        return serverRemoteClient.getLocalAddress();
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

        RemoteClient ibisHub = startServer(subjob.getCluster(), true, subjob.getName());

        String hubAddress = ibisHub.getLocalAddress();

        logger.info("adding just started hub '" + hubAddress
                + "' to ibis server");
        serverRemoteClient.addHubs(hubAddress);

        subjob.submit(poolID, getPoolSize(), serverRemoteClient
                .getLocalAddress(), hubAddress);
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
}