package ibis.deploy;

import ibis.server.remote.RemoteClient;
import ibis.util.ThreadPool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
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
 * Running server/hub
 * 
 */
public class RemoteServer implements Runnable, MetricListener {
    private static final Logger logger = LoggerFactory
            .getLogger(RemoteServer.class);

    private final boolean hubOnly;

    private final String clusterName;

    private final String gridName;

    // reference to rootHub so this server can report its address
    private final LocalServer rootHub;

    // info for submitting gat job

    private final GATContext context;

    private final JobDescription jobDescription;

    private final URI resourceBrokerURI;

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
     *            if true, only start a smartsockets hub. If false, start the
     *            complete server)
     * @param rootHub
     *            roothub of this ibis-deploy. Address of this hub is reported
     *            to root-hub.
     * @param homeDir
     *            home dir of ibis-deploy. Files used to start server should be
     *            here.
     * 
     */
    RemoteServer(Cluster cluster, boolean hubOnly, LocalServer rootHub,
            File homeDir) throws Exception {
        this.hubOnly = hubOnly;
        this.rootHub = rootHub;
        listeners = new ArrayList<MetricListener>();
        gridName = cluster.getGridName();
        clusterName = cluster.getName();

        address = null;

        if (clusterName == null) {
            throw new Exception(
                    "cannot start hub on an unnamed cluster. (grid = "
                            + gridName + ")");
        }

        if (gridName == null) {
            throw new Exception(
                    "cannot start hub on an unnamed grid. (cluster = "
                            + clusterName + ")");
        }

        if (hubOnly) {
            logger.info("Starting hub on " + cluster + " using "
                    + cluster.getServerAdaptor() + "(" + cluster.getServerURI()
                    + ")");
        } else {
            logger.info("Starting server on " + cluster + " using "
                    + cluster.getServerAdaptor() + "(" + cluster.getServerURI()
                    + ")");
        }

        context = createGATContext(cluster);

        resourceBrokerURI = cluster.getServerURI();
        if (resourceBrokerURI == null) {
            throw new Exception("no resource broker URI given for cluster "
                    + cluster);
        }

        jobDescription = createJobDescription(cluster, hubOnly, homeDir);

        ThreadPool.createNew(this, "server on " + cluster);
    }

    private static GATContext createGATContext(Cluster cluster)
            throws Exception {
        GATContext context = new GATContext();
        if (cluster.getUserName() != null) {
            SecurityContext securityContext = new CertificateSecurityContext(
                    null, null, cluster.getUserName(), null);
            // securityContext.addNote("adaptors",
            // "commandlinessh,sshtrilead");
            context.addSecurityContext(securityContext);
        }
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
        org.gridlab.gat.io.File log4JGatFile = GAT.createFile(log4j
                .toString());
        org.gridlab.gat.io.File log4JgatDstFile = GAT
                .createFile("log4j.properties");
        sd.addPreStagedFile(log4JGatFile, log4JgatDstFile);

        // set classpath 
        sd.setJavaClassPath(".:lib-server:lib-server/*");
        
        sd.getAttributes().put("sandbox.delete", "false");

        sd.enableStreamingStdout(true);
        sd.enableStreamingStderr(true);
        sd.enableStreamingStdin(true);

        JobDescription result = new JobDescription(sd);

        result.setProcessCount(1);
        result.setResourceCount(1);

        return result;
    }

    /**
     * Geth the address of this server. Also waits until it is running.
     * 
     * @return the address of this server
     * @throws Exception
     *             if the server failed to start
     */
    public synchronized String getAddress() throws Exception {
        waitUntilRunning();
        return address;
    }

    public synchronized void addStateListener(MetricListener listener)
            throws Exception {
        if (gatJob == null) {
            // listerers added when job is submitted
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
            // closing stdin should do it :)
            gatJob.getStdin().close();
            // TODO:add smartsockets kill mechanism
        } catch (Exception e) {
            logger.warn("error on stopping hub", e);
        }
        try {
            gatJob.stop();
        } catch (Exception e) {
            logger.warn("error on stopping hub", e);
        }

    }

    public void processMetricEvent(MetricEvent event) {
        logger.info(this + " status now " + event.getValue());
    }

    public void run() {
        try {
            logger.debug("creating resource broker for hub");

            ResourceBroker jobBroker = GAT.createResourceBroker(context,
                    resourceBrokerURI);

            logger.debug("submitting job: " + jobDescription.toString());

            org.gridlab.gat.resources.Job job = jobBroker.submitJob(
                    jobDescription, this, "job.status");
            setGatJob(job);

            // TODO: remote remote client stuff
            logger.debug("starting remote client");
            RemoteClient client = new RemoteClient(job.getStdout(), job
                    .getStdin());
            new OutputForwarder(job.getStderr(), System.err);

            logger.debug("getting address via remote client");

            setAddress(client.getLocalAddress());

            logger.debug("address is " + getAddress());

            if (rootHub != null) {
                logger.debug("adding server to root hub");
                rootHub.addHubs(this.getAddress());
            }

            logger.info(this + " now running");
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

        // server already killed before it was submitted :(
        // kill server...
        if (killed) {
            gatJob.stop();
        }
    }

    public String toString() {
        if (hubOnly) {
            return "Remote Hub on " + clusterName + "@" + gridName;
        } else {
            return "Remote Server " + clusterName + "@" + gridName;
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

    public synchronized boolean isRunning() throws Exception {
        return address != null && getState() == JobState.RUNNING;
    }
}
