package ibis.deploy;

import ibis.server.ServerProperties;
import ibis.server.remote.RemoteClient;
import ibis.util.ThreadPool;

import java.io.File;
import java.util.Arrays;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.engine.util.OutputForwarder;
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
public class RemoteServer implements Runnable, Hub {

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

    private final boolean keepSandbox;

    private final GATContext context;

    private String address;

    private org.gridlab.gat.resources.Job gatJob;

    // in case this hub/server dies or fails to start, the error will be here
    private Exception error = null;

    private boolean killed = false;

    private final Listeners listeners;

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
            File deployHomeDir, MetricListener hubListener, boolean keepSandbox)
            throws Exception {
        this.hubOnly = hubOnly;
        this.rootHub = rootHub;
        this.deployHomeDir = deployHomeDir;
        this.keepSandbox = keepSandbox;
        address = null;

        this.cluster = cluster.resolve();

        this.cluster.checkSettings("Server", true);

        this.context = createGATContext();

        if (hubOnly) {
            id = "Hub-" + getNextID();
        } else {
            id = "Server-" + getNextID();
        }

        listeners = new Listeners(this.toString());
        if (hubListener != null) {
            addStateListener(hubListener);
        }

        logger.info("Starting " + this + " using "
                + this.cluster.getServerAdaptor() + "("
                + this.cluster.getServerURI() + ")");

        ThreadPool.createNew(this, this.toString());
    }

    private GATContext createGATContext() throws Exception {
        GATContext context = new GATContext();
        if (cluster.getUserName() != null) {
            SecurityContext securityContext = new CertificateSecurityContext(
                    null, null, cluster.getUserName(), null);
            context.addSecurityContext(securityContext);
        }
        // ensure files are readable on the other side
        context.addPreference("file.chmod", "0755");
        // make sure non existing files/dirs will be created on the
        // fly during a copy
        context.addPreference("file.create", "true");

        context.addPreference("resourcebroker.adaptor.name", cluster
                .getServerAdaptor());

        context.addPreference("file.adaptor.name", Util.strings2CSS(cluster
                .getFileAdaptors()));

        return context;

    }

    private void prestage(File src, Cluster cluster, JavaSoftwareDescription sd)
            throws Exception {
        String host = cluster.getServerURI().getHost();
        String user = cluster.getUserName();
        File cacheDir = cluster.getCacheDir();

        org.gridlab.gat.io.File gatCwd = GAT.createFile(context, ".");

        if (cacheDir == null) {
            org.gridlab.gat.io.File gatFile = GAT.createFile(context, src
                    .toString());
            sd.addPreStagedFile(gatFile, gatCwd);
            return;
        }

        // create cache dir, and server dir within
        org.gridlab.gat.io.File gatCacheDirFile = GAT.createFile(context,
                "any://" + host + "/" + cacheDir + "/server/");
        gatCacheDirFile.mkdirs();

        // rsync to cluster cache server dir
        File rsyncLocation = new File(cacheDir + "/server/");
        Rsync.rsync(src, rsyncLocation, host, user);

        // tell job to pre-stage from cache dir
        org.gridlab.gat.io.File gatFile = GAT.createFile(context, "any://"
                + host + "/" + cacheDir + "/server/" + src.getName());
        sd.addPreStagedFile(gatFile, gatCwd);
        return;
    }

    private JobDescription createJobDescription() throws Exception {
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
            sd.setJavaArguments(new String[] { "--remote", "--port", "0" });
        }

        // add server libraries to pre-stage, use rsync if specified
        File serverLibs = new File(deployHomeDir, "lib-server");
        prestage(serverLibs, cluster, sd);

        // add server log4j file
        File log4j = new File(deployHomeDir, "log4j.properties");
        prestage(log4j, cluster, sd);

        // add server output files to post-stage
        if (cluster.getServerOutputFiles() != null) {
            for (File file : cluster.getServerOutputFiles()) {
                org.gridlab.gat.io.File gatFile = GAT.createFile(context, file
                        .getPath());

                sd.addPostStagedFile(gatFile, GAT.createFile(context, "."));
            }
        }

        sd.addJavaSystemProperty("log4j.prefix", "REMOTE_OUTPUT_FROM_" + id
                + "@" + cluster.getName());

        // give list of know hubs to the new hub
        sd.addJavaSystemProperty(ServerProperties.HUB_ADDRESSES, Util
                .strings2CSS(rootHub.getHubs()));

        // set these last so a user can override any
        // and all settings made by ibis-deploy
        if (cluster.getServerSystemProperties() != null) {
            sd.getJavaSystemProperties().putAll(
                    cluster.getServerSystemProperties());
        }

        // set classpath
        sd.setJavaClassPath(".:lib-server:lib-server/*");

        if (keepSandbox) {
            sd.getAttributes().put("sandbox.delete", "false");
        }

        sd.enableStreamingStdout(true);
        sd.enableStreamingStderr(true);
        sd.enableStreamingStdin(true);

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
     * Also causes a new {@link MetricEvent} for this listener with the current
     * state of the job.
     * 
     * @param listener
     *            the listener to attach
     * @throws Exception
     *             in case attaching failed
     */
    public void addStateListener(MetricListener listener) throws Exception {
        if (listener == null) {
            throw new Exception("tried to attach null listener");
        }

        // causes a new event for this listener with the current state of the
        // job.
        if (gatJob != null) {
            Metric metric = gatJob.getMetricDefinitionByName("job.status")
                    .createMetric(null);
            listener.processMetricEvent(new MetricEvent(gatJob, gatJob
                    .getState(), metric, System.currentTimeMillis()));
        }

        listeners.addListener(listener);
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
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            if (cluster.getCacheDir() != null) {
                logger.info(this + " doing pre-stage using rsync");
            }

            JobDescription jobDescription = createJobDescription();

            logger.debug("creating resource broker for hub");

            ResourceBroker jobBroker = GAT.createResourceBroker(context,
                    cluster.getServerURI());

            logger.info("JavaGAT job description for " + this + " ="
                    + jobDescription);

            org.gridlab.gat.resources.Job job = jobBroker.submitJob(
                    jobDescription, listeners, "job.status");
            setGatJob(job);

            // forward error to deploy console
            new OutputForwarder(job.getStderr(), System.err);

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

            logger.info(this + " now running (address = " + getAddress() + ")");
        } catch (Exception e) {
            logger.error("cannot start hub/server", e);
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
