package ibis.deploy;

import ibis.server.ServerProperties;
import ibis.server.remote.RemoteClient;
import ibis.util.ThreadPool;

import java.io.File;
import java.util.Properties;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.OutputForwarder;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Running server/hub
 * 
 */
public class Server implements Runnable, MetricListener {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    // used in case of a local server
    private final ibis.server.Server server;

    private String address;

    private final boolean local;

    private final boolean hubOnly;

    private final GATContext context;

    private final URI resourceBrokerURI;

    private final JobDescription jobDescription;

    // in case this hub/server dies or fails to start, the error will be here
    private Exception error = null;

    /**
     * Start a server/hub locally.
     * 
     * @param hubOnly
     *            if true, only start a hub. If false, also start a server.
     * @throws Exception
     *             if starting the server fails.
     */
    @SuppressWarnings("unchecked")
    public Server(boolean hubOnly) throws Exception {
        local = true;
        this.hubOnly = hubOnly;

        logger.info("Starting build-in server, hub only: " + hubOnly);

        Properties properties = new Properties();
        properties.put(ServerProperties.HUB_ONLY, hubOnly + "");
        properties.put(ServerProperties.PRINT_ERRORS, "true");
        properties.put(ServerProperties.PRINT_EVENTS, "true");
        properties.put(ServerProperties.PRINT_STATS, "true");

        server = new ibis.server.Server(properties);
        address = server.getLocalAddress();

        context = null;
        resourceBrokerURI = null;
        jobDescription = null;

        logger.info(server.toString());
    }

    /**
     * Create a server/hub on the given cluster. Does not block, so server may
     * not be available when this constructor completes.
     * 
     * @param libs
     *            jar files/directories needed to start the server
     * @param hubOnly
     *            if true, only start a smartsockets hub. If false, start the
     *            complete server)
     */
    public Server(File[] libs, Cluster cluster, boolean hubOnly)
            throws Exception {
        local = false;
        server = null;
        this.hubOnly = hubOnly;

        address = null;

        logger
                .info("Starting server on " + cluster + " , hub only: "
                        + hubOnly);

        context = createGATContext(cluster);

        resourceBrokerURI = cluster.getServerURI();
        if (resourceBrokerURI == null) {
            throw new Exception("no resource broker URI given for cluster "
                    + cluster);
        }

        jobDescription = createJobDescription(cluster, libs, hubOnly);

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

    private static String classpathFor(File file, String prefix) {
        logger
                .debug("getting classpath for " + file + " with prefix "
                        + prefix);

        if (!file.isDirectory()) {
            // regular files not in classpath
            return "";
        }
        // classpath for dir "lib" with prefix "dir/" is dir/lib/*:dir/lib
        // both directory itself, and all files in that dir (*)
        String result = prefix + file.getName() + File.separator + "*"
                + File.pathSeparator + prefix + file.getName()
                + File.pathSeparator;
        for (File child : file.listFiles()) {
            result = result
                    + classpathFor(child, file.getName() + File.separator);
        }
        return result;
    }

    // classpath made up of all directories, as well as
    private static String createClassPath(File[] serverLibs) {
        // start with root directory
        String result = "." + File.pathSeparator + "*" + File.pathSeparator;

        for (File file : serverLibs) {
            result = result + classpathFor(file, "");
        }

        return result;
    }

    private static JobDescription createJobDescription(Cluster cluster,
            File[] serverLibs, boolean hubOnly) throws Exception {
        logger.debug("creating job description");

        JavaSoftwareDescription sd = new JavaSoftwareDescription();

        sd.setExecutable(cluster.getJavaPath());
        if (logger.isInfoEnabled()) {
            logger.info("executable: " + sd.getExecutable());
        }

        // main class and options
        sd.setJavaMain("ibis.server.Server");
        if (hubOnly) {
            sd.setJavaArguments(new String[] { "--hub-only", "--remote",
                    "--port", "0" });
        } else {
            sd.setJavaArguments(new String[] { "--remote", "--port", "0",
                    "--events", "--stats" });
        }

        for (File file : serverLibs) {
            URI uri = new URI(file.getAbsolutePath());
            org.gridlab.gat.io.File gatFile = GAT.createFile(uri);

            org.gridlab.gat.io.File gatDstFile = GAT.createFile(new URI("."));

            sd.addPreStagedFile(gatFile, gatDstFile);
        }

        sd.getAttributes().put("sandbox.delete", "false");

        // class path
        sd.setJavaClassPath(createClassPath(serverLibs));

        sd.enableStreamingStdout(true);
        sd.enableStreamingStderr(true);
        sd.enableStreamingStdin(true);

        JobDescription result = new JobDescription(sd);

        result.setProcessCount(1);
        result.setResourceCount(1);

        return result;
    }

    public synchronized String getAddress() throws Exception {
        waitUntilRunning();
        return address;
    }

    private synchronized void setAddress(String address) {
        this.address = address;
        notifyAll();
    }

    public String[] getHubs() throws Exception {
        if (!local) {
            throw new Exception("cannot get hubs for remote server");
        }
        return server.getHubs();
    }

    public void addHubs(String... hubs) throws Exception {
        if (!local) {
            throw new Exception("cannot add hubs to remote server");
        }

        server.addHubs(hubs);
    }

    public void killAll() {
        // TODO:implement
    }

    public void kill() {
        if (local) {
            server.end(-1);
        } else {
            // TODO: kill server
        }
    }

    private synchronized void setError(Exception error) {
        this.error = error;
        notifyAll();
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

    public String toString() {
        if (local && hubOnly) {
            return "Local Hub";
        } else if (local) {
            return "Local Server";
        } else if (hubOnly) {
            return "Remote Hub";
        } else {
            return "Remote Server";
        }
    }

    public void run() {
        try {
            logger.debug("creating resource broker for hub");

            ResourceBroker jobBroker = GAT.createResourceBroker(context,
                    resourceBrokerURI);

            logger.info("submitting job: " + jobDescription.toString());

            org.gridlab.gat.resources.Job job = jobBroker.submitJob(
                    jobDescription, this, "job.status");

            // TODO: remote remote client stuff
            RemoteClient client = new RemoteClient(job.getStdout(), job
                    .getStdin());
            new OutputForwarder(job.getStderr(), System.err);
            setAddress(client.getLocalAddress());
        } catch (Exception e) {
            logger.error("cannot start hub", e);
            setError(e);
        }
    }

    public void processMetricEvent(MetricEvent event) {
        logger.info("Status of job now: " + event);
    }
}
