package ibis.deploy;

import ibis.deploy.Deploy.HubPolicy;
import ibis.deploy.util.Colors;
import ibis.deploy.util.OutputPrefixForwarder;
import ibis.deploy.util.StateForwarder;
import ibis.ipl.IbisProperties;
import ibis.ipl.registry.central.RegistryProperties;
import ibis.util.ThreadPool;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
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

    private final Resource resource;

    private final Application application;

    private final HubPolicy hubPolicy;

    // hub shared between all jobs on this resource (if applicable)
    private final Server sharedHub;

    private final boolean keepSandbox;

    private final GATContext context;

    private final LocalServer rootHub;

    private final boolean verbose;

    private final File deployHome;

    private final String serverAddress;

    private final Deploy deploy;

    // private final Deploy deploy;

    // gat job
    private org.gridlab.gat.resources.Job gatJob;

    // listeners specified by user
    private final StateForwarder forwarder;

    // listener for hub
    private final StateListener hubListener;

    private boolean killed = false;

    private boolean collecting = false;

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
    Job(JobDescription description, HubPolicy hubPolicy, Server hub, boolean keepSandbox, StateListener jobListener,
            StateListener hubListener, LocalServer rootHub, boolean verbose, File deployHome, String serverAddress,
            Deploy deploy, boolean collecting) throws Exception {
        this.description = description;
        this.resource = description.getResource();
        this.application = description.getApplication();
        this.hubPolicy = hubPolicy;
        this.sharedHub = hub;
        this.keepSandbox = keepSandbox;
        this.hubListener = hubListener;
        this.rootHub = rootHub;
        this.verbose = verbose;
        this.deployHome = deployHome;
        this.serverAddress = serverAddress;
        this.deploy = deploy;
        this.collecting = collecting;

        this.context = createGATContext();

        forwarder = new StateForwarder(this.toString());
        forwarder.addListener(jobListener);

        // fork thread
        ThreadPool.createNew(this, toString());
    }

    /**
     * Returns the description used to start this job.
     * 
     * @return a copy of the description used to start this job.
     */
    public JobDescription getDescription() {
        return new JobDescription(description);
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
     * Add a listener to the hub of this server which reports the state of the
     * Hub. Also causes a new event for this listener with the current state of
     * the hub. Only works if a hub is started per resource
     * 
     * @param listener
     *            the listener to attach
     */
    public void addHubStateListener(StateListener listener) {
        if (sharedHub != null) {
            sharedHub.addListener(listener);
        }
    }

    /**
     * Returns the state of this job
     * 
     * @return the state of this job
     * 
     */
    public State getState() {
        return forwarder.getState();
    }

    /**
     * Returns if this job either done or an error occurred.
     * 
     * @return true if this job either done or an error occurred.
     */
    public boolean isFinished() {
        return forwarder.isFinished();
    }

    /**
     * Returns the exception of this job (if any).
     * 
     * @return the exception of this job (if any).
     */
    public Exception getException() {
        return forwarder.getException();
    }

    /**
     * Wait until this job is in the "STOPPED" or "SUBMISSION_ERROR" state.
     * 
     * @throws Exception
     *             in case an error occurs.
     */
    public synchronized void waitUntilFinished() throws Exception {
        forwarder.waitUntilFinished();
    }

    /**
     * Waits until this Job is deployed. This is detected by checking that there
     * is a pool with the poolname of the job, which contains a location whose
     * name starts with the name of the job description. Awful hack, since this
     * means that ibis.location cannot be redefined, or at least only be
     * redefined such that it starts with the name of the job description. TODO:
     * Fix! But how? --Ceriel
     * 
     * @throws Exception
     *             in case an error occurs.
     */
    public void waitUntilDeployed() throws Exception {
        waitUntilDeployed(null);
    }

    
    /**
     * Waits until this Job is deployed. This is detected by checking that there
     * is a pool with the poolname of the job, which contains a location whose
     * name starts with the name of the job description. Awful hack, since this
     * means that ibis.location cannot be redefined, or at least only be
     * redefined such that it starts with the name of the job description. TODO:
     * Fix! But how? --Ceriel
     * 
     * @param logger if not null, an info message is if the state of the job changed
     * 
     * @throws Exception
     *             in case an error occurs.
     */
    public void waitUntilDeployed(Logger logger) throws Exception {
        State currentState = null;
        String location = description.getName();

        while (!isFinished()) {
            if (deploy.poolSizes().containsKey(description.getPoolName())) {
                for (String string : deploy.getLocations(description.getPoolName())) {
                    if (string.startsWith(location)) {
                        forwarder.setState(State.DEPLOYED);
                        return;
                    }
                }
            }
            
            if (logger != null) {
                State newState = getState();
                
                if (newState != currentState) {
                    logger.info(toString() + " now " + getState());
                    currentState = newState;
                }
            }

            synchronized (this) {
                wait(1000);
            }
        }
    }

    private synchronized void setGatJob(org.gridlab.gat.resources.Job gatJob) throws GATInvocationException {
        this.gatJob = gatJob;

        // job already killed before it was submitted :(
        // kill job...
        if (killed) {
            gatJob.stop();
        }
    }

    private static String classpathFor(File file, String prefix, String fsep, String psep) {
        // logger.debug("classpath for: " + file + " prefix = " + prefix);

        if (!file.isDirectory()) {
            // regular files not in classpath
            return prefix + file.getName() + psep;
        }
        // classpath for dir "lib" with prefix "dir/" is dir/lib/*:dir/lib
        String result = prefix + file.getName() + psep + prefix + file.getName() + fsep + "*" + psep;
        for (File child : file.listFiles()) {
            if (child.isDirectory() && !child.isHidden()) {
                result = result + classpathFor(child, prefix + file.getName() + fsep, fsep, psep);
            }
        }
        return result;
    }

    // classpath made up of all directories
    private static String createClassPath(String adaptor, File[] libs) {
        // start with cwd
        String fsep = "/";
        String psep = ":";
        if (adaptor != null && adaptor.startsWith("local")) {
            fsep = File.separator;
            psep = File.pathSeparator;
        }
        String result = "." + psep;

        for (File file : libs) {
            result = result + classpathFor(file, "", fsep, psep);
        }

        return result;
    }

    private GATContext createGATContext() throws Exception {
        logger.debug("creating context");

        GATContext context = new GATContext();

        if (resource.getUserName() != null) {
            String keyFile = resource.getKeyFile();
            SecurityContext securityContext = new CertificateSecurityContext(keyFile == null ? null : new URI(keyFile),
                    null, resource.getUserName(), null);
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
        // make sshtrilead file adaptor cache some info
        context.addPreference("sshtrilead.caching.exists", "true");
        context.addPreference("sshtrilead.caching.isdirectory", "true");

        context.addPreference(IbisProperties.HUB_ADDRESSES, deploy.getRootHubAddress());

        if (resource.getJobAdaptor() != null) {
            context.addPreference("resourcebroker.adaptor.name", resource.getJobAdaptor());
        }

        if (resource.getJobOptions() != null) {
            for (Map.Entry<String, String> option : resource.getJobOptions().entrySet()) {
                context.addPreference(option.getKey(), option.getValue());
                // System.err.println("option! \"" + option.getKey() +
                // "\" equals now \"" + option.getValue() + "\"");
            }
        }
        // context.addPreference("sshsge.native.flags", "-l num_gpu=1");S

        context.addPreference("file.adaptor.name", DeployProperties.strings2CSS(resource.getFileAdaptors()));

        return context;
    }

    private void prestage(File src, JavaSoftwareDescription sd) throws Exception {
        org.gridlab.gat.io.File gatFile = GAT.createFile(context, new URI(src.toURI()));
        // Don't use netURI.toString()! The JavaGAT URI constructor is quite
        // different from the java.net.URI one. Any %-escape in
        // netURI.toString() is
        // handled wrong! --Ceriel

        // de-optimize for better functionality :-)
        sd.addPreStagedFile(gatFile);
        // sd.addPreStagedFile(gatFile);
    }

    private JavaSoftwareDescription createJavaSoftwareDescription(String hubList) throws Exception {
        logger.debug("creating job description");

        JavaSoftwareDescription sd = new JavaSoftwareDescription();

        // ANDROID CHANGE START
        if (application.getMainClass().startsWith("intent:")) {
            sd = new IntentSoftwareDescription();
        } else if (application.getMainClass().endsWith(".py")) {
            sd = new JythonSoftwareDescription();
        }
        // ANDROID CHANGE END

        if (resource.getJavaPath() == null) {
            sd.setExecutable("java");
        } else {
            sd.setExecutable(resource.getJavaPath());
        }
        logger.debug("executable: " + sd.getExecutable());

        // basic application properties
        // ANDROID CHANGE START
        if (application.getMainClass().startsWith("intent:")) {
            sd.setJavaMain(application.getMainClass().substring("intent:".length()));
            sd.getAttributes().put("sandbox.useroot", "true");
            sd.getAttributes().put("sandbox.delete", "false");
        } else if (application.getMainClass().endsWith(".py")) {
            ((JythonSoftwareDescription) sd).setPythonScript(application.getMainClass());
            ((JythonSoftwareDescription) sd).setJythonJar("../jython2.2.1/jython.jar");
        } else {
            sd.setJavaMain(application.getMainClass());
        }
        // ANDROID CHANGE END
        // ORIGINAL START
        // sd.setJavaMain(application.getMainClass());
        // ORIGINAL END

        sd.setJavaArguments(application.getArguments());

        // Set jvm options. Add memory settings if needed.
        int memory = application.getMemorySize();
        if (memory > 0) {
            sd.addAttribute("memory.max", "" + memory);

            ArrayList<String> javaOptions = new ArrayList<String>();
            if (application.getJVMOptions() != null) {
                javaOptions.addAll(Arrays.asList(application.getJVMOptions()));
            }

            javaOptions.add("-Xmx" + memory + "M");
            javaOptions.add("-Xms" + memory + "M");

            sd.setJavaOptions(javaOptions.toArray(new String[0]));
        } else {
            sd.setJavaOptions(application.getJVMOptions());
        }

        // ibis stuff
        String location = resource.getName();

        sd.addJavaSystemProperty(IbisProperties.LOCATION, description.getName() + "@%HOSTNAME%@" + location);
        sd.addJavaSystemProperty(IbisProperties.LOCATION_COLOR, Colors.color2colorCode(resource.getColor()));
        sd.addJavaSystemProperty(IbisProperties.POOL_NAME, description.getPoolName());
        sd.addJavaSystemProperty(IbisProperties.POOL_SIZE, "" + description.getPoolSize());
        sd.addJavaSystemProperty(IbisProperties.SERVER_ADDRESS, serverAddress);

        sd.addJavaSystemProperty(RegistryProperties.HEARTBEAT_INTERVAL, "30");

        sd.addJavaSystemProperty("ibis.deploy.job.id", description.getName());
        sd.addJavaSystemProperty("ibis.deploy.job.size", Integer.toString(description.getProcessCount()));
        if (collecting) {
            sd.addJavaSystemProperty("ibis.managementclient", "true");
            sd.addJavaSystemProperty("ibis.bytescount", "true");
        }

        // add hub list to software description
        sd.addJavaSystemProperty(IbisProperties.HUB_ADDRESSES, hubList);

        // set these last so a user can override any
        // and all settings made by ibis-deploy
        if (application.getSystemProperties() != null) {
            sd.getJavaSystemProperties().putAll(application.getSystemProperties());
        }

        if (application.getLibs() == null) {
            throw new Exception("no library files specified for application " + application);
        }

        // add library files
        for (File file : application.getLibs()) {
            if (!file.exists()) {
                throw new Exception("File " + file + " in libs of job does not exist");
            }

            prestage(file, sd);
        }

        if (application.getInputFiles() != null) {
            for (File file : application.getInputFiles()) {
                if (!file.exists()) {
                    throw new Exception("File " + file + " in input files of job does not exist");
                }

                prestage(file, sd);
            }
        }

        File log4jFile = application.getLog4jFile();

        if (log4jFile == null) {
            log4jFile = new File(deployHome, "log4j.properties");
        }

        prestage(log4jFile, sd);
        sd.addJavaSystemProperty("log4j.configuration", "file:" + log4jFile.getName());

        if (application.getOutputFiles() != null) {
            // org.gridlab.gat.io.File gatCwd = GAT.createFile(context, ".");
            for (File file : application.getOutputFiles()) {
                org.gridlab.gat.io.File gatFile = GAT.createFile(context, "file:///" + file.getPath());

                // sd.addPostStagedFile(gatFile, gatCwd);
                sd.addPostStagedFile(gatFile);
            }
        }

        if (keepSandbox) {
            sd.getAttributes().put("sandbox.delete", "false");
        }

        if (description.getRuntime() != 0) {
            sd.addAttribute("walltime.max", "" + description.getRuntime());
            sd.addAttribute("time.max", "" + description.getRuntime());
            sd.addAttribute("cputime.max", "" + description.getRuntime());
        }

        // class path
        boolean foundClasspathOption = false;
        if (application.getJVMOptions() != null) {
            for (String option : application.getJVMOptions()) {
                if (option.equals("-classpath") || option.equals("-cp")) {
                    foundClasspathOption = true;
                }
            }
        }

        if (!foundClasspathOption) {
            sd.setJavaClassPath(createClassPath(resource.getJobAdaptor(), application.getLibs()));
        }

        if (sd instanceof JythonSoftwareDescription) {
            ((JythonSoftwareDescription) sd).setPythonPath(createClassPath(resource.getJobAdaptor(),
                    application.getLibs()));
        }

        if (description.getStdoutFile() == null) {
            sd.setStdout(GAT.createFile(context, description.getPoolName() + "." + description.getName() + ".out.txt"));
        } else {
            sd.setStdout(GAT.createFile(context, "file:///" + description.getStdoutFile().getPath()));
        }

        if (description.getStderrFile() == null) {
            sd.setStderr(GAT.createFile(context, description.getPoolName() + "." + description.getName() + ".err.txt"));
        } else {
            sd.setStderr(GAT.createFile(context, "file:///" + description.getStderrFile().getPath()));
        }

        logger.info("Submitting application \"" + application.getName() + "\" to " + resource.getName() + " using "
                + resource.getJobURI());

        return sd;
    }

    private org.gridlab.gat.resources.JobDescription createJobDescription(JavaSoftwareDescription sd) throws Exception {
        org.gridlab.gat.resources.JobDescription result;

        File wrapperScript = description.getResource().getJobWrapperScript();

        if (wrapperScript == null) {
            result = new org.gridlab.gat.resources.JobDescription(sd);
            result.setProcessCount(description.getProcessCount());
            result.setResourceCount(description.getResourceCount());
        } else {
            if (!wrapperScript.exists()) {
                // search for wrapperscript in deploy root dir as well.
                wrapperScript = new File(deploy.getHome(), wrapperScript.getName()).getAbsoluteFile();

                if (!wrapperScript.exists()) {
                    throw new Exception("wrapper script \"" + wrapperScript.getName()
                            + "\" does not exist. Searched in "
                            + description.getResource().getJobWrapperScript().getAbsolutePath() + " and "
                            + wrapperScript);
                }
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
                    wrapperSd.addPostStagedFile(src, sd.getPostStaged().get(src));
                }
            }

            wrapperSd.setStderr(sd.getStderr());
            wrapperSd.setStdout(sd.getStdout());

            // add wrapper to pre-stage files
            wrapperSd.addPreStagedFile(
            // GAT.createFile(context, wrapperScript.toString()),
            // GAT.createFile(context, "."));
                    GAT.createFile(context, wrapperScript.toString()));

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
            wrapperSd.setArguments(argumentList.toArray(new String[argumentList.size()]));

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
            String hubAddress;

            if (hubPolicy == HubPolicy.OFF) {
                hubAddress = "";
            } else if (hubPolicy == HubPolicy.PER_JOB) {
                forwarder.setState(State.WAITING);

                // start a hub especially for this job
                localHub = new RemoteServer(description.getResource(), true, rootHub, deployHome, verbose, hubListener,
                        keepSandbox);

                // wait until hub really running
                localHub.waitUntilRunning();

                // create list of hubs, add to software description
                hubAddress = localHub.getAddress();
            } else if (hubPolicy == HubPolicy.PER_RESOURCE) {
                forwarder.setState(State.WAITING);

                sharedHub.waitUntilRunning();

                hubAddress = sharedHub.getAddress();
            } else {
                throw new Exception("Unknown Hub Policy");
            }

            if (hubAddress == null) {
                logger.error("Could not get address of hub for job " + this);
                hubAddress = "";
            }

            String hubList = hubAddress;
            for (String address : rootHub.getHubs()) {
                if (hubList.length() > 0) {
                    hubList = hubList + ",";
                }
                hubList = hubList + address.trim();
            }

            logger.debug("Hub list = " + hubList);

            GATContext context = createGATContext();

            // Creating software description.
            JavaSoftwareDescription javaSoftwareDescription = createJavaSoftwareDescription(hubList);

            org.gridlab.gat.resources.JobDescription jobDescription = createJobDescription(javaSoftwareDescription);

            if (verbose) {
                System.err.println("JavaGAT job description for " + this + " =" + jobDescription);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("JavaGAT job description for " + this + " =" + jobDescription);
            }

            forwarder.setState(State.SUBMITTING);

            ResourceBroker jobBroker;

            // use address provided by user
            jobBroker = GAT.createResourceBroker(context, description.getResource().getJobURI());

            org.gridlab.gat.resources.Job gatJob = jobBroker.submitJob(jobDescription, forwarder, "job.status");
            setGatJob(gatJob);

            waitUntilDeployed();

            if (hubPolicy == HubPolicy.PER_JOB) {
                waitUntilFinished();

                // kill our local hub
                localHub.kill();
            }
        } catch (Exception e) {
            logger.error("Error on running job: ", e);
            forwarder.setErrorState(e);
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
                if (!isFinished()) {
                    gatJob.stop();
                }
            } catch (Exception e) {
                logger.warn("error on stopping job", e);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Job " + description.getName() + "@" + description.getResource().getName();
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
