package ibis.deploy;

import ibis.server.remote.RemoteClient;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;

/**
 * A representation of an Ibis Server process.
 * 
 */
public class Server {
    private static final Logger logger = Logger.getLogger(Server.class);

    /**
     * The cluster this server should run on
     */
    private Cluster serverCluster;

    /**
     * The name for this server
     */
    private final String name;

    /**
     * The RemoteClient for this server that lets us communicate with it
     */
    private RemoteClient serverClient;

    /**
     * The registry server (if this is not a registry)
     */
    private final Server registryServer;

    /**
     * Constructs a new server with the given name on the given cluster. This
     * server will run both a registry and a smartsockets hub.
     * 
     * @param name
     *                The name of the server
     * @param serverCluster
     *                The cluster the server should be started on
     */
    public Server(String name, Cluster serverCluster) {
        this(name, serverCluster, null);
    }

    /**
     * Constructs a new hub server with the given name on the given cluster.
     * This server will only run as a smartsockets hub and will be directed to
     * connect to the hub.
     * 
     * @param name
     *                The name of this server
     * @param serverCluster
     *                The cluster this server should be started on
     * @param registry
     *                The hub to connect this server to.
     */
    public Server(String name, Cluster serverCluster, Server registry) {
        this.name = name;
        this.serverCluster = serverCluster;
        this.registryServer = registry;
    }

    /**
     * Returns the cluster this server is running on.
     * 
     * @return This servers cluster
     */
    public Cluster getCluster() {
        return serverCluster;
    }

    /**
     * Returns true if this server is only acting as a hub.
     * 
     * @return true if this server is only acting as a hub.
     */
    public boolean isHubOnly() {
        return registryServer != null;
    }

    /**
     * Returns the name for this server.
     * 
     * @return The name for this server.
     */
    public String name() {
        return name;
    }

    public String toString() {
        return "server: " + name() + " (hub only: " + isHubOnly() + ") at "
                + serverCluster.getName();
    }

    /**
     * Returns true if this server has been started. This does not check if it
     * is still alive.
     * 
     * @return true if this server has been started.
     */

    public boolean isStarted() {
        return serverClient != null;
    }

    /**
     * Starts the specified server on the servers cluster
     * 
     * @param job
     *                The job this server is being started for
     * @return The GAT Job object which started this server
     * @throws Exception
     *                 If there is a problem starting the server
     */
    org.gridlab.gat.resources.Job startServer(Job forJob) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("start " + this);
        }

        // if (deployClients.containsKey(serverCluster.getDeployBroker())) {
        // if (logger.isInfoEnabled()) {
        // logger.info("already a hub available at: '"
        // + serverCluster.getDeployBroker() + "'");
        // }
        // return null;
        // }
        Application application = forJob.getFirstApplication();
        if (application == null) {
            throw new Exception("cannot start server, no application specified");
        }

        Preferences serverPreferences = new Preferences();
        if (getCluster().getIbisHubFileAdaptors() != null) {
            serverPreferences.put("file.adaptor.name", getCluster()
                    .getIbisHubFileAdaptors());
        }
        if (getCluster().getIbisHubBrokerAdaptors() != null) {
            serverPreferences.put("resourcebroker.adaptor.name", getCluster()
                    .getIbisHubBrokerAdaptors());
        }

        JavaSoftwareDescription sd = new JavaSoftwareDescription();

        if (getCluster().getJavaPath() != null) {
            if (getCluster().isWindows()) {
                sd.setExecutable(getCluster().getJavaPath() + "\\bin\\java");
            } else {
                sd.setExecutable(getCluster().getJavaPath() + "/bin/java");
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("executable: " + sd.getExecutable());
        }

        sd.setJavaMain("ibis.server.Server");
        if (isHubOnly()) {
            sd.setJavaArguments(new String[] { "--hub-only", "--remote",
                    "--port", "0", "--hub-addresses",
                    registryServer.getServerClient().getLocalAddress() });
        } else {
            sd.setJavaArguments(new String[] { "--remote", "--port", "0",
                    "--events", "--stats" });
        }
        sd.setJavaOptions(new String[] {
                "-classpath",
                application.getJavaClassPath(
                        application.getServerPreStageSet(), application
                                .hasCustomServerPreStageSet(), getCluster()
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

        sd.setStderr(GAT.createFile(serverPreferences, forJob
                .getOutputDirectory()
                + "hub-"
                + getCluster().getName()
                + "-"
                + forJob.getName()
                + ".err"));

        sd.enableStreamingStdout(true);
        sd.enableStreamingStdin(true);

        JobDescription jd = new JobDescription(sd);
        if (logger.isDebugEnabled()) {
            logger.debug("starting server at '"
                    + getCluster().getIbisHubBroker() + "'"
                    + " with username '" + getCluster().getUserName() + "'");
        }

        GATContext context = new GATContext();
        if (getCluster().getUserName() != null) {
            SecurityContext securityContext = new CertificateSecurityContext(
                    null, null, getCluster().getUserName(), getCluster()
                            .getPassword());
            securityContext.addNote("adaptors", "commandlinessh,sshtrilead");
            context.addSecurityContext(securityContext);
        }

        ResourceBroker broker = GAT.createResourceBroker(context,
                serverPreferences, getCluster().getIbisHubBroker());

        org.gridlab.gat.resources.Job job = broker.submitJob(jd, forJob,
                "job.status");

        serverClient = new RemoteClient(job.getStdout(), job.getStdin());

        while (true) {
            JobState state = job.getState();
            if (state == JobState.RUNNING) {
                // add job to lijstje
                logger.info("hub/server is running");
                return job;
            } else if (state == JobState.STOPPED
                    || state == JobState.SUBMISSION_ERROR) {
                // do something useful in case of error
                logger.info("hub job already stopped or submission error");
            } else {
                logger.info("waiting until hub is in state RUNNING");
                Thread.sleep(1000);
            }
        }

        // stop het in de administratie
        // wacht tot de job running is
        // return de remote client?

        // deployJobs.put(serverCluster.getDeployBroker(), broker.submitJob(jd,
        // this, "job.status"));
        // deployClients.put(serverCluster.getDeployBroker(), ibisServer);
        //
        // return true;
    }

    /**
     * Returns the RemoteClient that can be used to talk to the server
     * 
     * @return The RemoteClient for this server or null if it has not been
     *         started.
     */
    public RemoteClient getServerClient() {
        return serverClient;
    }

}
