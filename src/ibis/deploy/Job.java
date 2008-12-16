package ibis.deploy;

import ibis.ipl.IbisProperties;
import ibis.util.ThreadPool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Running job of an experiment
 * 
 * @author Niels Drost
 * 
 */
public class Job implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    private final JobDescription description;

    private final Cluster cluster;

    private final Application application;

    // shared hub. If null, a local hub is used
    private final Hub sharedHub;

    private final boolean keepSandbox;

    private final GATContext context;

    private final RootHub rootHub;

    private final boolean verbose;

    private final File deployHome;

    private final String serverAddress;
    
    private final Deploy deploy;

    // private final Deploy deploy;

    // set in case this jobs fails for some reason.
    private Exception error = null;

    // gat job
    private org.gridlab.gat.resources.Job gatJob;

    // listeners specified by user
    private final StateForwarder forwarder;

    // listener for hub
    private final StateListener hubListener;

    private boolean killed = false;

    /**
     * Creates a job object from the given description.
     * 
     * @param description
     *            description of new job
     * @param serverAddress
     *            address of server
     * @param rootHub
     *            root hub.
     * @param hub
     *            shared hub. null for local hub
     * @param deployHomeDir
     *            home dir of deploy. Libs of server should be here
     * @throws Exception
     *             if the listener could not be attached to this job
     */
    Job(JobDescription description, Hub hub, boolean keepSandbox,
            StateListener jobListener, StateListener hubListener,
            RootHub rootHub, boolean verbose, File deployHome, String serverAddress,

            Deploy deploy) throws Exception {
        this.description = description;
        this.cluster = description.getClusterOverrides();
        this.application = description.getApplicationOverrides();
        this.sharedHub = hub;
        this.keepSandbox = keepSandbox;
        this.hubListener = hubListener;
        this.rootHub = rootHub;
        this.verbose = verbose;
        this.deployHome = deployHome;
        this.serverAddress = serverAddress;
        this.deploy = deploy;

        this.context = createGATContext();

        forwarder = new StateForwarder(this.toString());
        forwarder.addListener(jobListener);

        // fork thread
        ThreadPool.createNew(this, toString());
    }

    /**
     * Returns the description used to start this job.
     * 
     * @return the description used to start this job.
     */
    public JobDescription getDescription() {
        return description;
    }

    private synchronized void setError(Exception error) {
        this.error = error;
        notifyAll();
    }

    /**
     * Add a listener to this server which reports the state of the Job. Also
     * causes a new event for this listener with the current state of the job.
     * 
     * @param listener
     *            the listener to attach
     */
    public void addStateListener(StateListener listener) {
        forwarder.addListener(listener);
    }

    /**
     * Returns the state of this job
     * 
     * @return the state of this job
     * 
     */
    public State getState() throws Exception {
        return forwarder.getState();
    }

    /**
     * Returns if this job is in the "STOPPED" or "SUBMISSION_ERROR" state.
     * 
     * @return true if this job is in the "STOPPED" or "SUBMISSION_ERROR" state.
     * @throws Exception
     *             in case an error occurs.
     */
    public synchronized boolean isFinished() throws Exception {
        if (error != null) {
            throw error;
        }

        State state = getState();

        if (state == State.STOPPED || state == State.ERROR) {
            return true;
        }

        return false;
    }

    /**
     * Wait until this job is in the "STOPPED" or "SUBMISSION_ERROR" state.
     * 
     * @throws Exception
     *             in case an error occurs.
     */
    public synchronized void waitUntilFinished() throws Exception {
        while (!isFinished()) {
            wait(1000);
        }
    }

    public void waitUntilDeployed() throws Exception {
        String location = description.getName() + "@" + cluster.getName();

        while (!isFinished()) {
            if (deploy.poolSizes().containsKey(description.getPoolName())) {
                for (String string : deploy.getLocations(description
                        .getPoolName())) {
                    if (string.equals(location)) {
                        forwarder.setState(State.DEPLOYED);
                        return;
                    }
                }
            }

            synchronized (this) {
                wait(1000);
            }
        }
    }

    private synchronized void setGatJob(org.gridlab.gat.resources.Job gatJob)
            throws GATInvocationException {
        this.gatJob = gatJob;

        // job already killed before it was submitted :(
        // kill job...
        if (killed) {
            gatJob.stop();
        }
    }

    private static String classpathFor(File file, String prefix) {
        // logger.debug("classpath for: " + file + " prefix = " + prefix);

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
                    + classpathFor(child, prefix + file.getName()
                            + File.separator);
        }
        return result;
    }

    // classpath made up of all directories, as well as jar
    private static String createClassPath(File[] libs) {
        // start with lib directory
        String result = "lib" + File.pathSeparator + "lib" + File.separator
                + "*" + File.pathSeparator;

        for (File file : libs) {
            result = result + classpathFor(file, "lib" + File.separator);
        }

        return result;
    }

    private GATContext createGATContext() throws Exception {
        logger.debug("creating context");

        GATContext context = new GATContext();

        if (cluster.getUserName() != null) {
            SecurityContext securityContext = new CertificateSecurityContext(
                    null, null, cluster.getUserName(), null);
            context.addSecurityContext(securityContext);
        }

        // make sure files are readable on the other side
        context.addPreference("file.chmod", "0755");
        // make sure non existing files/directories will be created on the fly
        // during a
        // copy
        context.addPreference("file.create", "true");
        // make ssh jobs stoppable, we lose the difference between stdout and
        // stderr
        context.addPreference("sshtrilead.stoppable", "true");
        if (cluster.getJobAdaptor() == null) {
            throw new Exception("no job adaptor specified for cluster: "
                    + cluster);
        }

        context.addPreference("resourcebroker.adaptor.name", cluster
                .getJobAdaptor());

        context.addPreference("file.adaptor.name", Util.strings2CSS(cluster
                .getFileAdaptors()));

        return context;
    }

    private void prestage(File src, File dstDir, JavaSoftwareDescription sd)
            throws Exception {
        String host = cluster.getServerURI().getHost();
        String user = cluster.getUserName();
        File clusterCacheDir = cluster.getCacheDir();

        if (clusterCacheDir == null) {
            org.gridlab.gat.io.File gatFile = GAT.createFile(context, src
                    .toString());
            org.gridlab.gat.io.File gatDstFile = GAT.createFile(context, dstDir
                    .getPath()
                    + "/");
            sd.addPreStagedFile(gatFile, gatDstFile);
            return;
        }

        String fileCacheDir = clusterCacheDir + "/" + description.getPoolName()
                + "/" + description.getName() + "/" + dstDir;

        // create cache dir, and server dir within
        org.gridlab.gat.io.File gatCacheDirFile = GAT.createFile(context,
            "any://" + host + "/" + fileCacheDir);
        gatCacheDirFile.mkdirs();

        // rsync to cluster cache server dir
        File rsyncLocation = new File(fileCacheDir);
        Rsync.rsync(src, rsyncLocation, host, user);

        // tell job to pre-stage from cache dir
        org.gridlab.gat.io.File gatFile = GAT.createFile(context, "any://"
                + host + "/" + fileCacheDir + "/" + src.getName());
        org.gridlab.gat.io.File gatDstFile = GAT.createFile(context, dstDir
                .getPath()
                + "/");
        sd.addPreStagedFile(gatFile, gatDstFile);
        return;
    }

    private JavaSoftwareDescription createJavaSoftwareDescription(String hubList)
            throws Exception {
        logger.debug("creating job description");

        JavaSoftwareDescription sd = new JavaSoftwareDescription();

        if (cluster.getJavaPath() == null) {
            sd.setExecutable("java");
        } else {
            sd.setExecutable(cluster.getJavaPath());
        }
        logger.debug("executable: " + sd.getExecutable());

        // basic application properties
        sd.setJavaMain(application.getMainClass());
        sd.setJavaArguments(application.getArguments());
        sd.setJavaOptions(application.getJVMOptions());

        // ibis stuff
        sd.addJavaSystemProperty(IbisProperties.LOCATION, (description
                .getName()
                + "@" + cluster.getName()));
        sd.addJavaSystemProperty(IbisProperties.POOL_NAME, description
                .getPoolName());
        sd.addJavaSystemProperty(IbisProperties.POOL_SIZE, ""
                + description.getPoolSize());
        sd.addJavaSystemProperty(IbisProperties.SERVER_ADDRESS, serverAddress);

        sd.addJavaSystemProperty("ibis.deploy.job.id", description.getName());
        sd.addJavaSystemProperty("ibis.deploy.job.size", Integer
                .toString(description.getProcessCount()));

        // add hub list to software description
        sd.addJavaSystemProperty(IbisProperties.HUB_ADDRESSES, hubList);

        // Also set old property name for hub addresses
        sd.addJavaSystemProperty("ibis.server.hub.addresses", hubList);

        // set these last so a user can override any
        // and all settings made by ibis-deploy
        if (application.getSystemProperties() != null) {
            sd.getJavaSystemProperties().putAll(
                application.getSystemProperties());
        }

        if (application.getLibs() == null) {
            throw new Exception("no library files specified for application "
                    + application);
        }

        if (cluster.getCacheDir() != null) {
            forwarder.setState(State.RSYNC);
            logger.debug(this + " doing pre-stage using rsync");
        }

        // file referring to libs dir of sandbox
        File libDir = new File("lib");

        // add library files
        for (File file : application.getLibs()) {
            if (!file.exists()) {
                throw new Exception("File " + file
                        + " in libs of job does not exist");
            }

            prestage(file, libDir, sd);
        }

        // file referring to root of sandbox / current directory
        File cwd = new File(".");
        org.gridlab.gat.io.File gatCwd = GAT.createFile(context, ".");

        if (application.getInputFiles() != null) {
            for (File file : application.getInputFiles()) {
                if (!file.exists()) {
                    throw new Exception("File " + file
                            + " in input files of job does not exist");
                }

                prestage(file, cwd, sd);
            }
        }

        File log4jFile = application.getLog4jFile();

        if (log4jFile == null) {
            log4jFile = new File(deployHome, "log4j.properties");
        }

        // add log4j file to pre-stage, and add log4j property to use it
        org.gridlab.gat.io.File log4jGatFile = GAT.createFile(context,
            log4jFile.getPath());
        sd.addPreStagedFile(log4jGatFile, gatCwd);
        sd.addJavaSystemProperty("log4j.configuration", "file:"
                + log4jFile.getName());

        if (application.getOutputFiles() != null) {
            for (File file : application.getOutputFiles()) {
                org.gridlab.gat.io.File gatFile = GAT.createFile(context, file
                        .getPath());

                sd.addPostStagedFile(gatFile, gatCwd);
            }
        }

        if (keepSandbox) {
            sd.getAttributes().put("sandbox.delete", "false");
        }

        // class path
        sd.setJavaClassPath(createClassPath(application.getLibs()));

        sd.setStdout(GAT.createFile(context, description.getPoolName() + "."
                + description.getName() + ".out"));
        sd.setStderr(GAT.createFile(context, description.getPoolName() + "."
                + description.getName() + ".err"));

        logger.info("Submitting application \"" + application.getName()
                + "\" to " + cluster.getName() + " using "
                + cluster.getJobAdaptor() + "(" + cluster.getJobURI() + ")");

        return sd;
    }

    private org.gridlab.gat.resources.JobDescription createJobDescription(
            JavaSoftwareDescription sd) throws Exception {
        org.gridlab.gat.resources.JobDescription result;

        File wrapperScript = description.getClusterOverrides()
                .getJobWrapperScript();

        if (wrapperScript == null) {
            result = new org.gridlab.gat.resources.JobDescription(sd);
            result.setProcessCount(description.getProcessCount());
            result.setResourceCount(description.getResourceCount());
        } else {
            if (!wrapperScript.exists()) {
                throw new Exception("wrapper script \"" + wrapperScript
                        + "\" does not exist");
            }

            // copy all settings from the java description to a "normal"
            // software description
            SoftwareDescription wrapperSd = new SoftwareDescription();
            if (sd.getAttributes() != null) {
                wrapperSd.setAttributes(sd.getAttributes());
            }
            if (sd.getEnvironment() != null) {
                wrapperSd.setEnvironment(sd.getEnvironment());
            }
            if (sd.getPreStaged() != null) {
                for (org.gridlab.gat.io.File src : sd.getPreStaged().keySet()) {
                    wrapperSd.addPreStagedFile(src, sd.getPreStaged().get(src));
                }
            }
            if (sd.getPostStaged() != null) {
                for (org.gridlab.gat.io.File src : sd.getPostStaged().keySet()) {
                    wrapperSd.addPostStagedFile(src, sd.getPostStaged()
                            .get(src));
                }
            }
            wrapperSd.setStderr(sd.getStderr());
            wrapperSd.setStdout(sd.getStdout());

            // add wrapper to pre-stage files
            wrapperSd.addPreStagedFile(GAT.createFile(context, wrapperScript
                    .toString()), GAT.createFile(context, "."));

            // set /bin/sh as executable
            wrapperSd.setExecutable("/bin/sh");

            // prepend arguments with script, java exec, resource and process
            // count
            List<String> argumentList = new ArrayList<String>();
            argumentList.add(wrapperScript.getName());
            argumentList.add("" + description.getResourceCount());
            argumentList.add("" + description.getProcessCount());
            argumentList.add(sd.getExecutable());
            if (sd.getArguments() != null) {
                for (String arg : sd.getArguments()) {
                    argumentList.add(arg);
                }
            }
            wrapperSd.setArguments(argumentList.toArray(new String[argumentList
                    .size()]));

            result = new org.gridlab.gat.resources.JobDescription(wrapperSd);
            result.setProcessCount(1);
            result.setResourceCount(1);
        }

        return result;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        RemoteServer localHub = null;

        try {
            String hubList;

            forwarder.setState(State.WAITING);
            if (sharedHub == null) {
                // start a hub especially for this job
                localHub = new RemoteServer(description.getClusterOverrides(),
                        true, rootHub, deployHome, verbose, hubListener,
                        keepSandbox);

                // wait until hub really running
                localHub.waitUntilRunning();

                // create list of hubs, add to software description
                hubList = localHub.getAddress();
            } else {
                sharedHub.waitUntilRunning();

                hubList = sharedHub.getAddress();
            }
            // add hubs list from root hub
            for (String address : rootHub.getHubs()) {
                hubList = hubList + "," + address;
            }

            GATContext context = createGATContext();

            // Creating software description also does rsync of files to
            // cluster.
            JavaSoftwareDescription javaSoftwareDescription = createJavaSoftwareDescription(hubList);

            org.gridlab.gat.resources.JobDescription jobDescription = createJobDescription(javaSoftwareDescription);

            if (verbose) {
                logger.info("JavaGAT job description for " + this + " ="
                        + jobDescription);
            } else {
                logger.debug("JavaGAT job description for " + this + " ="
                        + jobDescription);
            }

            forwarder.setState(State.SUBMITTING);

            ResourceBroker jobBroker = GAT.createResourceBroker(context,
                description.getClusterOverrides().getJobURI());

            org.gridlab.gat.resources.Job gatJob = jobBroker.submitJob(
                jobDescription, forwarder, "job.status");
            setGatJob(gatJob);

            waitUntilDeployed();

            if (localHub != null) {
                waitUntilFinished();

                // kill our local hub
                localHub.kill();
            }
        } catch (Exception e) {
            logger.error("Error on running job: ", e);
            forwarder.setState(State.ERROR);
            setError(e);

        }
    }

    /**
     * Kill this job.
     */
    public void kill() {
        org.gridlab.gat.resources.Job gatJob = null;

        synchronized (this) {
            killed = true;
            gatJob = this.gatJob;
        }

        if (gatJob != null) {
            try {
                gatJob.stop();
            } catch (Exception e) {
                logger.warn("error on stopping job", e);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Job: " + description.getName() + "/"
                + description.getPoolName() + "@"
                + description.getClusterName();
    }

    /**
     * Returns the exit value of this job.
     * 
     * @return the exit value
     */
    public String getExitValue() {
        try {
            return "" + gatJob.getExitStatus();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the application of this job.
     * 
     * @return the application of this job.
     */
    public Application getApplication() {
        return application;
    }

}
