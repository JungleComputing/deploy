package ibis.deploy;

import ibis.ipl.IbisProperties;
import ibis.util.ThreadPool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;
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
public class Job implements Runnable, MetricListener {

    private static int nextID = 0;

    static synchronized int getNextID() {
        return nextID++;
    }

    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    private final String jobID;

    private final JobDescription description;

    private final Cluster cluster;

    private final Application application;

    private final String serverAddress;

    private final LocalServer rootHub;

    // shared hub. If null, a local hub is used
    private final RemoteServer hub;

    private final File deployHomeDir;

    private final boolean keepSandbox;

    // set in case this jobs fails for some reason.
    private Exception error = null;

    // gat job
    private org.gridlab.gat.resources.Job gatJob;

    // listeners specified by user
    private final List<MetricListener> listeners;

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
     */
    Job(JobDescription description, String serverAddress, LocalServer rootHub,
            RemoteServer hub, File deployHomeDir, boolean keepSandbox) {
        this.description = description;
        this.cluster = description.getClusterSettings();
        this.application = description.getApplicationSettings();
        this.serverAddress = serverAddress;
        this.rootHub = rootHub;
        this.hub = hub;
        this.deployHomeDir = deployHomeDir;
        this.keepSandbox = keepSandbox;
        listeners = new ArrayList<MetricListener>();

        jobID = "Job-" + getNextID();

        // fork thread
        ThreadPool.createNew(this, jobID);
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
     * Attach a listener to this job. Will report the job state when it changes.
     * 
     * @param listener
     *            the listener to attach
     * @throws Exception
     */
    public synchronized void addStateListener(MetricListener listener)
            throws Exception {
        if (gatJob == null) {
            listeners.add(listener);

        } else {
            Metric metric = gatJob.getMetricDefinitionByName("job.state")
                    .createMetric(null);

            gatJob.addMetricListener(listener, metric);
        }
    }

    /**
     * Returns the state of this job
     * 
     * @return the state of this job
     * 
     * @throws Exception
     *             in case the state cannot be retrieved
     */
    public synchronized JobState getState() throws Exception {
        if (error != null) {
            throw error;
        }
        if (gatJob == null) {
            return JobState.UNKNOWN;
        }
        return gatJob.getState();
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

        JobState state = getState();

        if (state == JobState.STOPPED || state == JobState.SUBMISSION_ERROR) {
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

    private synchronized void setGatJob(org.gridlab.gat.resources.Job gatJob)
            throws GATInvocationException {
        this.gatJob = gatJob;

        for (MetricListener listener : listeners) {
            Metric metric = gatJob.getMetricDefinitionByName("job.state")
                    .createMetric(null);

            gatJob.addMetricListener(listener, metric);
        }

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

    // classpath made up of all directories, as well as
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
        sd.setJavaSystemProperties(application.getSystemProperties());
        sd.setJavaOptions(application.getJVMOptions());

        // ibis stuff
        sd.addJavaSystemProperty(IbisProperties.LOCATION_POSTFIX, cluster
                .getName());
        sd.addJavaSystemProperty(IbisProperties.POOL_NAME, description
                .getPoolName());
        sd.addJavaSystemProperty(IbisProperties.POOL_SIZE, ""
                + description.getPoolSize());
        sd.addJavaSystemProperty(IbisProperties.SERVER_ADDRESS, serverAddress);

        // add hub list to software description
        sd.addJavaSystemProperty(IbisProperties.HUB_ADDRESSES, hubList);
        // some versions of the server expect the hubs to be in
        // ibis.server.hub.addresses, set this too
        sd.addJavaSystemProperty("ibis.server.hub.addresses", hubList);

        if (application.getLibs() == null) {
            throw new Exception("no library files specified for application "
                    + application);
        }
        
        // file referring to libs dir of sandbox
        org.gridlab.gat.io.File libDir = GAT.createFile(new URI("lib/"));


        // add library files
        for (File file : application.getLibs()) {
            if (!file.exists()) {
                throw new Exception("File " + file
                        + " in libs of job does not exist");
            }

            URI uri = new URI(file.getAbsolutePath());
            org.gridlab.gat.io.File srcFile = GAT.createFile(uri);

            sd.addPreStagedFile(srcFile, libDir);
        }

        // file referring to root of sandbox / current directory
        org.gridlab.gat.io.File cwd = GAT.createFile(new URI("."));

        if (application.getInputFiles() != null) {
            for (File file : application.getInputFiles()) {
                if (!file.exists()) {
                    throw new Exception("File " + file
                            + " in input files of job does not exist");
                }

                URI uri = new URI(file.getAbsolutePath());
                org.gridlab.gat.io.File gatFile = GAT.createFile(uri);

                sd.addPreStagedFile(gatFile, cwd);
            }
        }

        if (application.getOutputFiles() != null) {
            for (File file : application.getOutputFiles()) {
                URI uri = new URI(file.getAbsolutePath());
                org.gridlab.gat.io.File gatFile = GAT.createFile(uri);

                // FIXME: check if this actually works, probably not
                sd.addPostStagedFile(gatFile);
            }
        }

        if (keepSandbox) {
            sd.getAttributes().put("sandbox.delete", "false");
        }

        // class path
        sd.setJavaClassPath(createClassPath(application.getLibs()));

        sd.setStdout(GAT.createFile(description.getPoolName() + "."
                + description.getName() + ".out"));
        sd.setStderr(GAT.createFile(description.getPoolName() + "."
                + description.getName() + ".err"));

        logger.info("Submitting application \"" + application.getName()
                + "\" to " + cluster.getName() + " using "
                + cluster.getJobAdaptor() + "(" + cluster.getJobURI() + ")");

        return sd;
    }

    private org.gridlab.gat.resources.JobDescription createJobDescription(
            JavaSoftwareDescription sd) throws Exception {
        org.gridlab.gat.resources.JobDescription result;

        File wrapperScript = description.getClusterSettings()
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
            wrapperSd.addPreStagedFile(
                GAT.createFile(wrapperScript.toString()), GAT.createFile("."));

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
        try {
            RemoteServer hub = this.hub;

            if (hub == null) {
                // start a hub especially for this job
                hub = new RemoteServer(description.getClusterSettings(), true,
                        rootHub, deployHomeDir);
            }
            // wait until hub really running
            hub.waitUntilRunning();

            // create list of hubs, add to software description
            String hubList = hub.getAddress();
            for (String address : rootHub.getHubs()) {
                hubList = hubList + "," + address;
            }

            GATContext context = createGATContext();

            JavaSoftwareDescription javaSoftwareDescription = createJavaSoftwareDescription(hubList);

            org.gridlab.gat.resources.JobDescription jobDescription = createJobDescription(javaSoftwareDescription);

            logger.debug("job description = " + jobDescription);

            ResourceBroker jobBroker = GAT.createResourceBroker(context,
                description.getClusterSettings().getJobURI());

            org.gridlab.gat.resources.Job gatJob = jobBroker.submitJob(
                jobDescription, this, "job.status");
            setGatJob(gatJob);

            waitUntilFinished();

        } catch (Exception e) {
            logger.error("Error on running job: ", e);
            setError(e);

        }
        if (this.hub == null) {
            // kill our local hub
            hub.kill();
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
     * @see org.gridlab.gat.monitoring.MetricListener#processMetricEvent(org.gridlab.gat.monitoring.MetricEvent)
     */
    public void processMetricEvent(MetricEvent event) {
        logger.info(this + " status now " + event.getValue());
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return jobID + ":" + description.getName() + "/"
                + description.getPoolName() + "@"
                + description.getClusterName();
    }

}
