package ibis.deploy;

import ibis.server.remote.RemoteClient;
import ibis.util.ThreadPool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server or Hub running on a remote cluster
 */
public class RemoteServer implements Runnable, MetricListener {

    private static int nextID = 0;

    static synchronized int getNextID() {
        return nextID++;
    }

    private static final Logger logger = LoggerFactory
            .getLogger(RemoteServer.class);

    private final String id;

    private final boolean hubOnly;

    private final Cluster cluster;

    private final File deployHomeDir;

    // reference to rootHub so this server can report its address
    private final LocalServer rootHub;

    // listeners specified by user
    private final List<MetricListener> listeners;

    private String address;

    private org.gridlab.gat.resources.Job gatJob;

    // in case this hub/server dies or fails to start, the error will be here
    private Exception error = null;

    private boolean killed = false;

    /**
     * Create a server/hub on the given cluster. Does not block, so server may
     * not be available when this constructor completes.
     * 
     * @param cluster
     *            cluster to start this server on
     * @param hubOnly
     *            if true, only start a SmartSockets hub. If false, start the
     *            complete server)
     * @param rootHub
     *            root hub of this ibis-deploy. Address of this hub is reported
     *            to the root-hub.
     * @param homeDir
     *            home directory of ibis-deploy. Files used to start server
     *            should be here.
     * 
     */
    RemoteServer(Cluster cluster, boolean hubOnly, LocalServer rootHub,
            File deployHomeDir) throws Exception {
        this.hubOnly = hubOnly;
        this.rootHub = rootHub;
        this.deployHomeDir = deployHomeDir;
        listeners = new ArrayList<MetricListener>();
        address = null;

        this.cluster = cluster.resolve();

        this.cluster.checkSettings("Server", true);

        if (hubOnly) {
            id = "Hub-" + getNextID();
        } else {
            id = "Server-" + getNextID();
        }

        logger.info("Starting " + this + "\" using "
                + cluster.getServerAdaptor() + "(" + cluster.getServerURI()
                + ")");

        ThreadPool.createNew(this, this.toString());
    }

    private static GATContext createGATContext(Cluster cluster)
            throws Exception {
        GATContext context = new GATContext();
        if (cluster.getUserName() != null) {
            SecurityContext securityContext = new CertificateSecurityContext(
                    null, null, cluster.getUserName(), null);
            context.addSecurityContext(securityContext);
        }
        // FIXME: what's this?
        context.addPreference("file.chmod", "0755");
        if (cluster.getServerAdaptor() == null) {
            throw new Exception("no server adaptor specified for cluster: "
                    + cluster);
        }

        context.addPreference("resourcebroker.adaptor.name", cluster
                .getServerAdaptor());

        if (cluster.getFileAdaptors() == null
                || cluster.getFileAdaptors().length == 0) {
            throw new Exception("no file adaptors specified for cluster: "
                    + cluster);
        }

        context.addPreference("file.adaptor.name", Util.strings2CSS(cluster
                .getFileAdaptors()));

        return context;

    }

    private static JobDescription createJobDescription(Cluster cluster,
            boolean hubOnly, File homeDir) throws Exception {
        logger.debug("creating job description");

        JavaSoftwareDescription sd = new JavaSoftwareDescription();

        sd.setExecutable(cluster.getJavaPath());
        logger.debug("executable: " + sd.getExecutable());

        // main class and options
        sd.setJavaMain("ibis.server.Server");
        if (hubOnly) {
            sd.setJavaArguments(new String[] { "--hub-only", "--remote",
                    "--port", "0" });
        } else {
            sd.setJavaArguments(new String[] { "--remote", "--port", "0",
                    "--events", "--stats" });
        }

        // add server libraries to pre-stage
        File serverLibs = new File(homeDir, "lib-server");

        org.gridlab.gat.io.File gatFile = GAT.createFile(serverLibs.toString());
        org.gridlab.gat.io.File gatDstFile = GAT.createFile(".");
        sd.addPreStagedFile(gatFile, gatDstFile);

        // add server log4j file
        File log4j = new File(homeDir, "log4j.server.properties");
        org.gridlab.gat.io.File log4JGatFile = GAT.createFile(log4j.toString());
        org.gridlab.gat.io.File log4JgatDstFile = GAT
                .createFile("log4j.properties");
        sd.addPreStagedFile(log4JGatFile, log4JgatDstFile);

        // set classpath
        sd.setJavaClassPath(".:lib-server:lib-server/*");

        sd.getAttributes().put("sandbox.delete", "false");

        sd.enableStreamingStdout(true);
        sd.enableStreamingStdin(true);

        if (hubOnly) {
            sd.setStderr(GAT.createFile(cluster.getName() + ".hub.err"));
        } else {
            sd.setStderr(GAT.createFile(cluster.getName() + ".server.err"));
        }

        JobDescription result = new JobDescription(sd);

        result.setProcessCount(1);
        result.setResourceCount(1);

        return result;
    }

    /**
     * Get the address of this server. Also waits until it is running.
     * 
     * @return the address of this server
     * @throws Exception
     *             if the server failed to start
     */
    public synchronized String getAddress() throws Exception {
        waitUntilRunning();
        return address;
    }

    /**
     * Add a listener to this server which reports the state of the JavaGAT job.
     * 
     * @param listener
     *            the listener to attach
     * @throws Exception
     *             in case attaching failed
     */
    public synchronized void addStateListener(MetricListener listener)
            throws Exception {
        
        if (gatJob == null) {
            // listeners added when job is submitted
            listeners.add(listener);
        } else {
            Metric metric = gatJob.getMetricDefinitionByName("job.state")
                    .createMetric(null);

            gatJob.addMetricListener(listener, metric);
        }
    }

    synchronized JobState getState() throws Exception {
        if (error != null) {
            throw error;
        }
        if (gatJob == null) {
            return JobState.UNKNOWN;
        }
        return gatJob.getState();
    }

    synchronized void kill() {
        killed = true;
        if (gatJob == null) {
            // nothing to do :)
            return;
        }
        try {
            // closing standard in should do it :)
            gatJob.getStdin().close();
            // TODO:add SmartSockets kill mechanism
        } catch (Exception e) {
            logger.warn("error on stopping hub", e);
        }
        try {
            gatJob.stop();
        } catch (Exception e) {
            logger.warn("error on stopping hub", e);
        }

    }

    /**
     * @see org.gridlab.gat.monitoring.MetricListener#processMetricEvent(org.gridlab.gat.monitoring.MetricEvent)
     */
    public void processMetricEvent(MetricEvent event) {
        logger.info(this + " status now " + event.getValue());
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            GATContext context = createGATContext(cluster);

            JobDescription jobDescription = createJobDescription(cluster,
                hubOnly, deployHomeDir);

            logger.debug("creating resource broker for hub");

            ResourceBroker jobBroker = GAT.createResourceBroker(context,
                cluster.getServerURI());

            logger.debug("submitting job: " + jobDescription.toString());

            org.gridlab.gat.resources.Job job = jobBroker.submitJob(
                jobDescription, this, "job.status");
            setGatJob(job);

            // TODO: remote remote client stuff
            logger.debug("starting remote client");
            RemoteClient client = new RemoteClient(job.getStdout(), job
                    .getStdin());

            logger.debug("getting address via remote client");

            setAddress(client.getLocalAddress());

            logger.debug("address is " + getAddress());

            if (rootHub != null) {
                logger.debug("adding server to root hub");
                rootHub.addHubs(this.getAddress());
            }

            logger.debug(this + " now running");
        } catch (Exception e) {
            logger.error("cannot start hub", e);
            setError(e);
        }
    }

    private synchronized void setAddress(String address) {
        this.address = address;
        notifyAll();
    }

    private synchronized void setError(Exception error) {
        this.error = error;
        notifyAll();
    }

    private synchronized void setGatJob(org.gridlab.gat.resources.Job gatJob)
            throws Exception {
        this.gatJob = gatJob;

        for (MetricListener listener : listeners) {
            Metric metric = gatJob.getMetricDefinitionByName("job.state")
                    .createMetric(null);

            gatJob.addMetricListener(listener, metric);
        }
        listeners.clear();

        // server already killed before it was submitted :(
        // kill server...
        if (killed) {
            gatJob.stop();
        }
    }

    /**
     * Ensure this server is running, wait for it if needed.
     * 
     * @throws Exception
     *             when the server could not be started.
     */
    public synchronized void waitUntilRunning() throws Exception {
        while (address == null) {
            if (error != null) {
                throw error;
            }

            wait(1000);
        }
        if (error != null) {
            throw error;
        }
    }

    /**
     * Returns if this server is running or not
     * 
     * @return true if this server is running
     * 
     * @throws Exception
     *             if the state of the job could not be determined
     */
    public synchronized boolean isRunning() throws Exception {
        return address != null && getState() == JobState.RUNNING;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (hubOnly) {
            return "Hub \"" + id + "\" on \"" + cluster.getName() + "\"";
        } else {
            return "Server \"" + id + "\" on \"" + cluster.getName() + "\"";
        }
    }
}
