package deployer.ibis;

import ibis.server.remote.RemoteClient;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
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
    private IbisCluster serverCluster;

    /**
     * The name for this server
     */
    private final String name;

    /**
     * The RemoteClient for this server that lets us communicate with it
     */
    private RemoteClient serverClient;

    private org.gridlab.gat.resources.Job job;

    /**
     * The registry server (if this is not a registry)
     */
    private IbisApplication application;

    private String serverAddress;

    public Server(String name, IbisCluster serverCluster,
            IbisApplication application, String serverAddress) {
        this.name = name;
        this.serverCluster = serverCluster;
        this.application = application;
        this.serverAddress = serverAddress;
    }

    public Server(String name, IbisCluster serverCluster,
            IbisApplication application) {
        this(name, serverCluster, application, null);
    }

    /**
     * Returns the cluster this server is running on.
     * 
     * @return This servers cluster
     */
    public IbisCluster getCluster() {
        return serverCluster;
    }

    /**
     * Returns true if this server is only acting as a hub.
     * 
     * @return true if this server is only acting as a hub.
     */
    public boolean isHubOnly() {
        return serverAddress != null;
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
        return name() + (isHubOnly() ? " (hub)" : " (server)");
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
     * @throws IOException
     * @throws GATObjectCreationException
     * @throws GATInvocationException
     * @throws Exception
     *                 If there is a problem starting the server
     */
    // org.gridlab.gat.resources.Job startServer(Job forJob) throws Exception {
    // if (logger.isInfoEnabled()) {
    // logger.info("start " + this);
    // }
    //
    // // if (deployClients.containsKey(serverCluster.getDeployBroker())) {
    // // if (logger.isInfoEnabled()) {
    // // logger.info("already a hub available at: '"
    // // + serverCluster.getDeployBroker() + "'");
    // // }
    // // return null;
    // // }
    // Application application = forJob.getFirstApplication();
    // if (application == null) {
    // throw new Exception("cannot start server, no application specified");
    // }
    //
    // Preferences serverPreferences = new Preferences();
    // if (getCluster().getIbisHubFileAdaptors() != null) {
    // serverPreferences.put("file.adaptor.name", getCluster()
    // .getIbisHubFileAdaptors());
    // }
    // if (getCluster().getIbisHubBrokerAdaptors() != null) {
    // serverPreferences.put("resourcebroker.adaptor.name", getCluster()
    // .getIbisHubBrokerAdaptors());
    // }
    //
    // JavaSoftwareDescription sd = new JavaSoftwareDescription();
    //
    // if (getCluster().getJavaPath() != null) {
    // if (getCluster().isWindows()) {
    // sd.setExecutable(getCluster().getJavaPath() + "\\bin\\java");
    // } else {
    // sd.setExecutable(getCluster().getJavaPath() + "/bin/java");
    // }
    // }
    // if (logger.isInfoEnabled()) {
    // logger.info("executable: " + sd.getExecutable());
    // }
    //
    // sd.setJavaMain("ibis.server.Server");
    // if (isHubOnly()) {
    // sd.setJavaArguments(new String[] { "--hub-only", "--remote",
    // "--port", "0", "--hub-addresses",
    // registryServer.getServerClient().getLocalAddress() });
    // } else {
    // sd.setJavaArguments(new String[] { "--remote", "--port", "0",
    // "--events", "--stats" });
    // }
    // sd.setJavaOptions(new String[] {
    // "-classpath",
    // application.getJavaClassPath(
    // application.getServerPreStageSet(), application
    // .hasCustomServerPreStageSet(), getCluster()
    // .isWindows()),
    // "-Dlog4j.configuration=file:log4j.properties" });
    // if (logger.isInfoEnabled()) {
    // logger.info("arguments: " + Arrays.deepToString(sd.getArguments()));
    // }
    // if (application.getServerPreStageSet() != null) {
    // for (String filename : application.getServerPreStageSet()) {
    // sd
    // .addPreStagedFile(GAT.createFile(serverPreferences,
    // filename));
    // }
    // }
    //
    // sd.setStderr(GAT.createFile(serverPreferences, forJob
    // .getOutputDirectory()
    // + "hub-"
    // + getCluster().getName()
    // + "-"
    // + forJob.getName()
    // + ".err"));
    //
    // sd.enableStreamingStdout(true);
    // sd.enableStreamingStdin(true);
    //
    // JobDescription jd = new JobDescription(sd);
    // if (logger.isDebugEnabled()) {
    // logger.debug("starting server at '"
    // + getCluster().getIbisHubBroker() + "'"
    // + " with username '" + getCluster().getUserName() + "'");
    // }
    //
    // GATContext context = new GATContext();
    // if (getCluster().getUserName() != null) {
    // SecurityContext securityContext = new CertificateSecurityContext(
    // null, null, getCluster().getUserName(), getCluster()
    // .getPassword());
    // securityContext.addNote("adaptors", "commandlinessh,sshtrilead");
    // context.addSecurityContext(securityContext);
    // }
    //
    // ResourceBroker broker = GAT.createResourceBroker(context,
    // serverPreferences, getCluster().getIbisHubBroker());
    //
    // org.gridlab.gat.resources.Job job = broker.submitJob(jd, forJob,
    // "job.status");
    //
    // serverClient = new RemoteClient(job.getStdout(), job.getStdin());
    //
    // while (true) {
    // JobState state = job.getState();
    // if (state == JobState.RUNNING) {
    // // add job to lijstje
    // logger.info("hub/server is running");
    // return job;
    // } else if (state == JobState.STOPPED
    // || state == JobState.SUBMISSION_ERROR) {
    // // do something useful in case of error
    // logger.info("hub job already stopped or submission error");
    // } else {
    // logger.info("waiting until hub is in state RUNNING");
    // Thread.sleep(1000);
    // }
    // }
    //
    // // stop het in de administratie
    // // wacht tot de job running is
    // // return de remote client?
    //
    // // deployJobs.put(serverCluster.getDeployBroker(), broker.submitJob(jd,
    // // this, "job.status"));
    // // deployClients.put(serverCluster.getDeployBroker(), ibisServer);
    // //
    // // return true;
    // }
    public void startServer() throws IOException, GATObjectCreationException,
            GATInvocationException {
        if (logger.isInfoEnabled()) {
            logger.info("start " + this);
        }

        Preferences serverPreferences = new Preferences();
        if (getCluster().getServerFileAdaptors() != null) {
            serverPreferences.put("file.adaptor.name", getCluster()
                    .getServerFileAdaptors());
        }
        if (getCluster().getServerBrokerAdaptors() != null) {
            serverPreferences.put("resourcebroker.adaptor.name", getCluster()
                    .getServerBrokerAdaptors());
        }

        JavaSoftwareDescription sd = new JavaSoftwareDescription();

        // if (getCluster().getJavaPath() != null) {
        // if (getCluster().isWindows()) {
        // sd.setExecutable(getCluster().getJavaPath() + "\\bin\\java");
        // } else {
        // sd.setExecutable(getCluster().getJavaPath() + "/bin/java");
        // }
        // }
        sd.setExecutable(getCluster().getJavaPath());
        if (logger.isInfoEnabled()) {
            logger.info("executable: " + sd.getExecutable());
        }

        sd.setJavaMain("ibis.server.Server");
        if (isHubOnly()) {
            sd.setJavaArguments(new String[] { "--hub-only", "--remote",
                    "--port", "0", "--hub-addresses", serverAddress });
        } else {
            sd.setJavaArguments(new String[] { "--remote", "--port", "0",
                    "--events", "--stats" });
        }

        if (application.getIbisPreStage() != null) {
            String ibisDir = new java.io.File(application.getIbisPreStage())
                    .getName()
                    + (getCluster().isWindows() ? "\\" : "/");
            sd.setJavaOptions(new String[] {
                    "-classpath",

                    ibisDir + "*:" + ibisDir + "commons-jxpath.jar",
                    "-Dlog4j.configuration=file:" + ibisDir
                            + "log4j.properties" });
            sd.addPreStagedFile(GAT.createFile(serverPreferences, application
                    .getIbisPreStage()));
        }
        if (logger.isInfoEnabled()) {
            logger.info("arguments: " + Arrays.deepToString(sd.getArguments()));
        }

        sd.setStderr(GAT.createFile(serverPreferences, "hub-"
                + getCluster().getName() + ".err"));

        sd.enableStreamingStdout(true);
        sd.enableStreamingStdin(true);

        JobDescription jd = new JobDescription(sd);
        if (logger.isDebugEnabled()) {
            logger.debug("starting server at '"
                    + getCluster().getServerBroker() + "'" + " with username '"
                    + getCluster().getUserName() + "'");
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
                serverPreferences, getCluster().getServerBroker());

        job = broker.submitJob(jd);

        serverClient = new RemoteClient(job.getStdout(), job.getStdin());

    }

    public void stopServer() throws IOException, GATInvocationException {
        serverClient.end(2000);
        job.stop();
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
