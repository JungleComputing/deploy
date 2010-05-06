package ibis.deploy;

import ibis.ipl.IbisProperties;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;
import ibis.util.ThreadPool;
import ibis.zorilla.Config;
import ibis.zorilla.util.Remote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Server, Hub, or Zorilla node running on a remote cluster
 */
public class RemoteZorilla implements Runnable, Server {

    public static final long TIMEOUT = 1200000;

    private static int nextID = 0;

    static synchronized int getNextID() {
        return nextID++;
    }

    private static final Logger logger = LoggerFactory
            .getLogger(RemoteZorilla.class);

    private final Cluster cluster;

    private final LocalServer localZorillaNode;

    private final File deployHome;

    private final boolean verbose;

    private final boolean keepSandbox;

    private final GATContext context;

    private org.gridlab.gat.resources.Job gatJob;

    private Remote remote;

    private String address;

    private boolean killed = false;

    private final StateForwarder forwarder;

    RemoteZorilla(Cluster cluster, LocalServer localZorillaNode,
            File deployHome, boolean verbose, StateListener listener,
            boolean keepSandbox) throws Exception {
        this.localZorillaNode = localZorillaNode;
        this.deployHome = deployHome;
        this.verbose = verbose;
        this.keepSandbox = keepSandbox;
        address = null;
        gatJob = null;
        remote = null;

        this.cluster = cluster.resolve();

        this.cluster.checkSettings("Zorilla", true);

        this.context = createGATContext();

        forwarder = new StateForwarder(this.toString());
        if (listener != null) {
            addListener(listener);
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

        // context.addPreference("sshtrilead.stoppable", "true");

        // ensure files are readable on the other side
        context.addPreference("file.chmod", "0755");
        // make sure non existing files/directories will be created on the
        // fly during a copy
        context.addPreference("file.create", "true");

        context.addPreference("resourcebroker.adaptor.name", cluster
                .getServerAdaptor()
                + ",local");

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
            org.gridlab.gat.io.File gatFile = GAT.createFile(context, new URI(
                    src.getAbsoluteFile().toURI()));
            sd.addPreStagedFile(gatFile, gatCwd);
            return;
        }

        // create cache dir, and server dir within
        org.gridlab.gat.io.File gatCacheDirFile = GAT.createFile(context,
                "any://" + host + "/" + cacheDir + "/zorilla/");
        gatCacheDirFile.mkdirs();

        // rsync to cluster cache server dir
        File rsyncLocation = new File(cacheDir + "/zorilla/");
        Rsync.rsync(src, rsyncLocation, host, user);

        // tell job to pre-stage from cache dir
        org.gridlab.gat.io.File gatFile = GAT.createFile(context, "any://"
                + host + "/" + cacheDir + "/zorilla/" + src.getName());
        sd.addPreStagedFile(gatFile, gatCwd);
        return;
    }

    private JobDescription createJobDescription() throws Exception {
        logger.debug("creating job description");

        JavaSoftwareDescription sd = new JavaSoftwareDescription();

        sd.setExecutable(cluster.getJavaPath());
        logger.debug("executable: " + sd.getExecutable());

        // main class and options
        sd.setJavaMain("ibis.zorilla.Main");

        List<String> arguments = new ArrayList<String>();

        arguments.add("--remote");
        // arguments.add("--port");
        // arguments.add("0");

        if (cluster.getJobAdaptor() != null) {
            arguments.add("--resource-adaptor");
            arguments.add(cluster.getJobAdaptor());
        }

        if (cluster.getJobURI() != null) {
            arguments.add("--resource-uri");
            arguments.add(cluster.getJobURI().toString());
        }

        arguments.add("--nodes");
        arguments.add("" + cluster.getNodes());

        arguments.add("--cores");
        arguments.add("" + cluster.getCores());

        arguments.add("--memory");
        arguments.add("" + cluster.getMemory());

        if (cluster.getJobWrapperScript() != null) {
            arguments.add("--resource-wrapper");
            arguments.add("" + cluster.getJobWrapperScript().getName());

            prestage(cluster.getJobWrapperScript(), cluster, sd);
        }

        // list of other hubs
        arguments.add("--hub-addresses");
        arguments.add(Util.strings2CSS(localZorillaNode.getHubs()));

        arguments.add("--peers");
        arguments.add(Util.strings2CSS(localZorillaNode.getHubs()));

        sd.setJavaArguments(arguments.toArray(new String[0]));

        // add zorilla libs
        File zorillaLibs = new File(deployHome, "lib-zorilla");
        prestage(zorillaLibs, cluster, sd);

        // classpath includes zorilla
        sd.setJavaClassPath(".:lib-zorilla:lib-zorilla/*");

        sd.addJavaSystemProperty("gat.adaptor.path", "lib-zorilla/adaptors");

        // FIXME:remove DAS specific SGE hack
        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put("SGE_ROOT", "/usr/local/Cluster-Apps/sge");
        environment.put("SGE_ARCH", "lx26-amd64");
        sd.setEnvironment(environment);

        // add server log4j file
        File log4j = new File(deployHome, "log4j.properties");
        prestage(log4j, cluster, sd);

        // add server output files to post-stage
        if (cluster.getServerOutputFiles() != null) {
            for (File file : cluster.getServerOutputFiles()) {
                org.gridlab.gat.io.File gatFile = GAT.createFile(context, file
                        .getPath());

                sd.addPostStagedFile(gatFile, GAT.createFile(context, "."));
            }
        }

        sd.addJavaSystemProperty(IbisProperties.LOCATION, cluster.getName());

//        sd.addJavaSystemProperty(Config.VIZ_INFO, cluster.getName() + "^Zorilla @ "
//                + cluster.getName() + "^" + cluster.getColorCode());
        
        sd.addJavaSystemProperty(Config.VIZ_INFO, "Z^Zorilla @ "
                + cluster.getName() + "^" + cluster.getColorCode());


        if (cluster.getServerSystemProperties() != null) {
            sd.getJavaSystemProperties().putAll(
                    cluster.getServerSystemProperties());
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

        if (remote != null) {
            // will close standard in, killing server
            remote.end();
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
            if (cluster.getCacheDir() != null) {
                logger.debug(this + " doing pre-stage using rsync");
                forwarder.setState(State.UPLOADING);
            }

            JobDescription jobDescription = createJobDescription();

            if (verbose) {
                logger.info("JavaGAT job description for " + this + " ="
                        + jobDescription);
            } else {
                logger.debug("JavaGAT job description for " + this + " ="
                        + jobDescription);
            }

            logger.debug("creating resource broker for zorilla");

            forwarder.setState(State.SUBMITTING);

            ResourceBroker jobBroker = GAT.createResourceBroker(context,
                    cluster.getServerURI());

            org.gridlab.gat.resources.Job job = jobBroker.submitJob(
                    jobDescription, forwarder, "job.status");
            setGatJob(job);

            // forward error to deploy console
            new OutputPrefixForwarder(job.getStderr(), System.err,
                    "STDERR FROM " + toString() + ": ");

            // fetch server address, forward output to deploy console
            logger.debug("starting remote client");

            Remote remote = new Remote(job.getStdout(), job.getStdin(),
                    System.out, "STDOUT FROM " + toString() + ": ");

            setRemoteClient(remote);

            logger.debug("getting address via remote client");

            forwarder.setState(State.DEPLOYED);

            logger.debug("address is " + getAddress());

            if (localZorillaNode != null) {
                logger.debug("adding server to root hub");
                localZorillaNode.addHubs(this.getAddress());
                localZorillaNode.addZorillaNode(this.getAddress());
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

    private synchronized void setRemoteClient(Remote remote) throws IOException {
        this.remote = remote;
        this.address = remote.getAddress(TIMEOUT);

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
        return "Zorilla node on \"" + cluster.getName() + "\"";
    }

    public RegistryServiceInterface getRegistryService() {
        throw new Error("not implemented");
    }

    public ManagementServiceInterface getManagementService() {
        throw new Error("not implemented");
    }

}
