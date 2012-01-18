package ibis.deploy;

import ibis.deploy.util.Colors;
import ibis.deploy.util.OutputPrefixForwarder;
import ibis.deploy.util.StateForwarder;
import ibis.ipl.IbisProperties;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.ipl.server.ServerConnection;
import ibis.ipl.server.ServerProperties;
import ibis.smartsockets.virtual.VirtualSocketFactory;
import ibis.util.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server or Hub running on a remote resource (cluster, stand-alone machine, etc)
 */
public class RemoteServer implements Runnable, Server {

    public static final long TIMEOUT = 1200000;

    private static int nextID = 0;

    static synchronized int getNextID() {
        return nextID++;
    }

    private static final Logger logger = LoggerFactory
            .getLogger(RemoteServer.class);

    private final String id;

    private final boolean hubOnly;

    private final Resource resource;

    private final LocalServer rootHub;

    private final File deployHome;

    private final boolean verbose;

    private final boolean keepSandbox;

    private final GATContext context;

    private org.gridlab.gat.resources.Job gatJob;

    private ServerConnection serverConnection;

    private String address;

    private boolean killed = false;

    private final StateForwarder forwarder;

    RemoteServer(Resource resource, boolean hubOnly, LocalServer rootHub,
            File deployHome, boolean verbose, StateListener listener,
            boolean keepSandbox) throws Exception {
        this.hubOnly = hubOnly;
        this.rootHub = rootHub;
        this.deployHome = deployHome;
        this.verbose = verbose;
        this.keepSandbox = keepSandbox;
        address = null;
        gatJob = null;
        serverConnection = null;

        this.resource = new Resource(resource);

        this.resource.checkSettings("Server", true);

        this.context = createGATContext();

        if (hubOnly) {
            id = "Hub-" + getNextID();
        } else {
            id = "Server-" + getNextID();
        }

        forwarder = new StateForwarder(this.toString());
        if (listener != null) {
            addListener(listener);
        }

        logger.info("Starting " + this + " using "
                + this.resource.getSupportURI());

        ThreadPool.createNew(this, this.toString());
    }

    private GATContext createGATContext() throws Exception {
        GATContext context = new GATContext();
        if (resource.getUserName() != null) {
            String keyFile = resource.getKeyFile();
            SecurityContext securityContext = new CertificateSecurityContext(
                    keyFile == null ? null : new URI(keyFile), null,
                    resource.getUserName(), null);
            context.addSecurityContext(securityContext);
        }
        // ensure files are readable on the other side
        context.addPreference("file.chmod", "0755");
        // make sure non existing files/directories will be created on the
        // fly during a copy
        context.addPreference("file.create", "true");

        if (resource.getSupportAdaptor() != null) {
            context.addPreference("resourcebroker.adaptor.name",
                    resource.getSupportAdaptor() + ",local");
        }

        context.addPreference("file.adaptor.name",
                DeployProperties.strings2CSS(resource.getFileAdaptors()));

        // make sshtrilead file adaptor cache some info
        context.addPreference("sshtrilead.caching.exists", "true");
        context.addPreference("sshtrilead.caching.isdirectory", "true");

        return context;

    }

    private void prestage(File src, Resource resource, JavaSoftwareDescription sd)
            throws Exception {
        org.gridlab.gat.io.File gatFile = GAT.createFile(context, new URI(src
                .getAbsoluteFile().toURI()));
        sd.addPreStagedFile(gatFile);
    }

    private JobDescription createJobDescription() throws Exception {
        logger.debug("creating job description");

        JavaSoftwareDescription sd = new JavaSoftwareDescription();

        sd.setExecutable(resource.getJavaPath());
        logger.debug("executable: " + sd.getExecutable());

        // main class and options
        sd.setJavaMain("ibis.ipl.server.Server");

        List<String> arguments = new ArrayList<String>();

        arguments.add("--remote");
        arguments.add("--port");
        arguments.add("0");
        arguments.add("--errors");

        if (hubOnly) {
            // Hub-only mode causes the JVM not to "end" properly sometimes
            // Doesn't matter if a server is present anyway...
            // arguments.add("--hub-only");
        }

        if (verbose) {
            arguments.add("--events");
            arguments.add("--stats");
        }

        // list of other hubs
        arguments.add("--hub-addresses");
        arguments.add(DeployProperties.strings2CSS(rootHub.getHubs()));

        sd.setJavaArguments(arguments.toArray(new String[0]));

        // add server libraries to pre-stage, use rsync if specified
        File serverLibs = new File(deployHome, "lib-server");
        prestage(serverLibs, resource, sd);

        // classpath.
        // TODO: this assumes that the remote server does not run on windows!
        // --Ceriel
        sd.setJavaClassPath(".:lib-server:lib-server/*");

        // add server log4j file
        File log4j = new File(deployHome, "log4j.properties");
        prestage(log4j, resource, sd);

        sd.addJavaSystemProperty(IbisProperties.LOCATION, resource.getName());

        if (hubOnly) {
            sd.addJavaSystemProperty(
                    ServerProperties.VIZ_INFO,
                    "H^" + resource.getName() + "^Smartsockets Hub @ "
                            + resource.getName() + "^"
                            + Colors.color2colorCode(resource.getColor()));
        } else {
            sd.addJavaSystemProperty(ServerProperties.VIZ_INFO,
                    "S^Ibis Server^Ibis Server @ " + resource.getName() + "^"
                            + Colors.color2colorCode(resource.getColor()));
        }

        if (resource.getSupportSystemProperties() != null) {
            sd.getJavaSystemProperties().putAll(
                    resource.getSupportSystemProperties());
        }

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
        if (address == null) {
            throw new Exception("Address cannot be retrieved");
        }
        return address;
    }

    public State getState() {
        return forwarder.getState();
    }

    /**
     * Add a listener to this server which reports the state of the job. Also
     * causes a new event for this listener with the current state of the job.
     * 
     * @param listener
     *            the listener to attach
     */
    public void addListener(StateListener listener) {
        forwarder.addListener(listener);
    }

    public synchronized void kill() {
        killed = true;

        if (serverConnection != null) {
            // will close standard in, killing server
            try {
                serverConnection.end(0);
            } catch (IOException e) {
                // IGNORE
            }
        }

        if (gatJob != null) {
            try {
                // in case stopping fails, kill gat job
                gatJob.stop();
            } catch (Exception e) {
                logger.warn("error on stopping hub", e);
            }
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            JobDescription jobDescription = createJobDescription();

            if (verbose) {
                logger.info("JavaGAT job description for " + this + " ="
                        + jobDescription);
            } else {
                logger.debug("JavaGAT job description for " + this + " ="
                        + jobDescription);
            }

            logger.debug("creating resource broker for hub");

            forwarder.setState(State.SUBMITTING);

            ResourceBroker jobBroker = GAT.createResourceBroker(context,
                    resource.getSupportURI());

            org.gridlab.gat.resources.Job job = jobBroker.submitJob(
                    jobDescription, forwarder, "job.status");
            setGatJob(job);

            // forward error to deploy console
            new OutputPrefixForwarder(job.getStderr(), System.err,
                    "STDERR FROM " + toString() + ": ");

            VirtualSocketFactory socketFactory = null;
            if (rootHub != null) {
                socketFactory = rootHub.getSocketFactory();
            }

            // fetch server address, forward output to deploy console
            logger.debug("starting remote client");
            ServerConnection connection = new ServerConnection(job.getStdout(),
                    job.getStdin(), System.out, "STDOUT FROM " + toString()
                            + ": ", TIMEOUT, socketFactory);
            setRemoteClient(connection);

            logger.debug("getting address via remote client");

            forwarder.setState(State.DEPLOYED);

            logger.debug("address is " + getAddress());

            if (rootHub != null) {
                logger.debug("adding server to root hub");
                rootHub.addHubs(this.getAddress());
            }

            logger.info(this + " now running (address = " + getAddress() + ")");
        } catch (Exception e) {
            logger.error("cannot start hub/server", e);
            forwarder.setErrorState(e);
        }
    }

    private synchronized void setGatJob(org.gridlab.gat.resources.Job gatJob)
            throws Exception {
        this.gatJob = gatJob;

        // server already killed before it was submitted :(
        // kill server...
        if (killed) {
            kill();
        }
    }

    private synchronized void setRemoteClient(ServerConnection connection)
            throws IOException {
        this.serverConnection = connection;
        this.address = serverConnection.getAddress().trim();

        // server already killed before it was submitted :(
        // kill server...
        if (killed) {
            kill();
        }

        notifyAll();
    }

    /**
     * Ensure this server is running, wait for it if needed.
     * 
     * @throws Exception
     *             when the server could not be started.
     */
    public void waitUntilRunning() throws Exception {
        forwarder.waitUntilDeployed();
    }

    /**
     * Returns if this server is running or not
     * 
     * @return true if this server is running
     * 
     */
    public boolean isRunning() {
        return forwarder.isRunning();
    }

    public boolean isFinished() {
        return forwarder.isFinished();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (hubOnly) {
            return "Hub \"" + id + "\" on \"" + resource.getName() + "\"";
        } else {
            return "Server on \"" + resource.getName() + "\"";
        }
    }

    public RegistryServiceInterface getRegistryService() {
        return serverConnection.getRegistryService();
    }

    public ManagementServiceInterface getManagementService() {
        return serverConnection.getManagementService();
    }

}
