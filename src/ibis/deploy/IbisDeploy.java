package ibis.deploy;

import ibis.server.Server;
import ibis.server.ServerProperties;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class IbisDeploy implements MetricListener {

    private static Logger logger = Logger.getLogger(IbisDeploy.class);

//    String gatLocation;

    String ibisHome;

    String ibisAppsHome;

    String satinHome;

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err
                    .println("usage: ibis-deploy <runFile> [runTime in seconds]");
            System.exit(1);
        }

        int time = -1;
        if (args.length == 2) {
            time = Integer.parseInt(args[1]);
        }
        new IbisDeploy().start(args[0], time);
        System.exit(1);
    }

    public void start(String runFile, int runTime) {
//        gatLocation = System.getenv("GAT_LOCATION");
//        if (gatLocation == null) {
//            logger.warn("please set your GAT_LOCATION");
//            System.exit(1);
//        }
//        if (logger.isInfoEnabled()) {
//            logger.info("using JavaGAT at: " + gatLocation);
//        }

        ibisHome = System.getenv("IBIS_HOME");
        if (ibisHome == null) {
            logger.warn("please set your IBIS_HOME");
            System.exit(1);
        }
        if (logger.isInfoEnabled()) {
            logger.info("using Ibis at: " + ibisHome);
        }

        Run run = null;
        try {
            run = Run.loadRun(runFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (logger.isInfoEnabled()) {
            logger.info(run);
        }

        GATContext context = new GATContext();

        context.addPreference("file.hiddenfiles.ignore", "true");
        context.addPreference("ignoreHiddenFiles", "true");
        context.addPreference("ftp.connection.passive", "false");

        ArrayList<Job> requested = run.getRequestedResources();

        for (int i = 0; i < requested.size(); i++) {
            for (int tries = 0; tries < run.getRetryAttempts(); tries++) {
                if (logger.isInfoEnabled()) {
                    logger.info("try " + tries + " of job '"
                            + requested.get(i).getName() + "'");
                }
                try {
                    submitJob(run, new GATContext(), requested.get(i), runTime);
                    break;
                } catch (Exception e) {
                    if (logger.isInfoEnabled()) {
                        logger
                                .info("try " + tries + " of job '"
                                        + requested.get(i).getName()
                                        + "' failed: " + e);
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(run.getRetryInterval() * 1000);
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                }
            }
        }
        GAT.end();
    }

    public synchronized void submitJob(Run run, GATContext context, Job job,
            int runTime) throws Exception {
        // create a new pool id for this job
        String poolID = "ibis-deploy-" + job.getName() + "-" + Math.random();
        if (logger.isDebugEnabled()) {
            logger.debug("poolID: " + poolID);
        }

        // start an ibis server and a hub on the submitting machine
        Server server = startServer();
        if (logger.isDebugEnabled()) {
            logger.debug("Local server started! (" + server.getLocalAddress()
                    + ")");
        }
        String knownHubs = server.getLocalAddress();
        if (logger.isDebugEnabled()) {
            logger.debug("Known hubs: " + knownHubs);
        }

        // start a hub on the other nodes
        ArrayList<org.gridlab.gat.resources.Job> hubJobs = new ArrayList<org.gridlab.gat.resources.Job>();

        Cluster[] clusters = job.getHubClusters(run.getGrids());
        for (Cluster cluster : clusters) {
            // if this cluster refers to the localhost don't start a hub, the
            // server is already on this machine!
            if (!(new org.gridlab.gat.URI(cluster.getHostname()))
                    .refersToLocalHost()) {
                // in this file name the hub will put its address
                String hubAddressFileName = ".ibis-deploy-hubaddress-"
                        + Math.random();
                // now start the hub
                hubJobs.add(startHub(cluster, knownHubs, "../"
                        + hubAddressFileName, new GATContext()));
                if (logger.isDebugEnabled()) {
                    logger.debug("Hub started at: " + cluster.getHostname());
                }

                // the hub is started, and now we wait... for the file with its
                // address to appear.
                File hubAddressFile = GAT.createFile(context, "any://"
                        + cluster.getHostname() + "/" + hubAddressFileName);
                while (!hubAddressFile.exists()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("waiting for " + hubAddressFile.toString()
                                + " to appear.");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                // the file exists, let's read the address from the file!
                FileInputStream in = GAT.createFileInputStream(context,
                        hubAddressFile);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                try {
                    String hubAddress = reader.readLine();
                    knownHubs += "," + hubAddress;
                    server.addHubs(hubAddress);
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("Exception caught while reading the hub address file: "
                                        + e);
                    }
                }
            }
        }

        org.gridlab.gat.resources.Job[] jobs = new org.gridlab.gat.resources.Job[job
                .numberOfSubJobs()];

        // if both server and hubs are up and running, start the real subjobs
        for (int i = 0; i < job.numberOfSubJobs(); i++) {
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("submitting subjob '" + job.get(i).getName()
                            + "'");
                }
                jobs[i] = submitSubJob(run, context, job, job.get(i), poolID,
                        server.getLocalAddress(), knownHubs, runTime);
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("submission failed: " + e);
                    e.printStackTrace();
                }
                // submission failed, cancel all already submitted jobs that are
                // not stopped yet!
                for (int j = 0; j < i - 1; j++) {
                    if (jobs[j].getState() != org.gridlab.gat.resources.Job.STOPPED
                            && jobs[j].getState() != org.gridlab.gat.resources.Job.SUBMISSION_ERROR) {
                        jobs[j].stop();
                    }
                }
                throw e;
            }

        }
        // wait for all submitted jobs to be finished;
        for (int i = 0; i < job.numberOfSubJobs(); i++) {
            while (jobs[i].getState() != org.gridlab.gat.resources.Job.STOPPED
                    && jobs[i].getState() != org.gridlab.gat.resources.Job.SUBMISSION_ERROR) {
                try {
                    logger.debug("Waiting for job: "
                            + jobs[i]
                            + " ("
                            + org.gridlab.gat.resources.Job
                                    .getStateString(jobs[i].getState()) + ")");
                    wait(1000);
                } catch (InterruptedException e) {
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Ending server ...");
        }
        server.end(true);
        if (logger.isDebugEnabled()) {
            logger.debug("Stopping hubs ...");
        }
        for (org.gridlab.gat.resources.Job hubJob : hubJobs) {
            hubJob.stop();
        }
        if (logger.isDebugEnabled()) {
            logger.info("Stopping hubs DONE");
        }
    }

    private Server startServer() {
        // start up a server at the submitting machine
        Properties properties = new Properties();
        // let the server automatically find a free port
        properties.put(ServerProperties.PORT, "0");
        properties.put(ServerProperties.IMPLEMENTATION_PATH, ibisHome
                + File.separator + "lib");
        properties.put("ibis.registry.central.statistics", "true");

        Server server = null;
        try {
            server = new Server(properties);
            logger.info("started ibis server: " + server);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return server;
    }

    private org.gridlab.gat.resources.Job startHub(Cluster cluster,
            String hubs, String hubAddressFile, GATContext context)
            throws GATObjectCreationException, GATInvocationException {
        // start up a hub on the headnode of all clusters. Use the ssh
        // ResourceBroker to reach the headnode, because other brokers may
        // submit the job to a worker node.
        // TODO add broker that can use FORK!
        Preferences preferences = new Preferences();
        preferences.put("Resourcebroker.adaptor.name",
                "commandlinessh, local, ssh");
        preferences.put("File.adaptor.name", cluster.getFileAccessType());
        Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();

        ResourceDescription rd = new HardwareResourceDescription(
                hardwareAttributes);

        // TODO make this a JavaSoftwareDescription!
        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable(cluster.getJavaHome() + "/bin/java");
        String classpath = ".";
        java.io.File tmp = new java.io.File(ibisHome + "/lib");
        String[] jars = tmp.list();
        for (int i = 0; i < jars.length; i++) {
            classpath += ":lib/" + jars[i];
        }
        sd.setArguments(new String[] { "-classpath", classpath,
                "-Dlog4j.configuration=file:./log4j.properties",
                "ibis.server.Server", "--hub-only", "--hub-addresses", hubs,
                "--port", "0", "--hub-address-file", hubAddressFile });
        sd.addPreStagedFile(GAT.createFile(context, preferences, ibisHome
                + "/lib"));
        sd.addPreStagedFile(GAT.createFile(context, preferences, ibisHome + "/"
                + "log4j.properties"));
        sd.setStderr(GAT.createFile(context, "hub@" + cluster.getFriendlyName()
                + ".err"));
        sd.setStdout(GAT.createFile(context, "hub@" + cluster.getFriendlyName()
                + ".out"));
        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = null;
        try {
            broker = GAT.createResourceBroker(context, preferences, new URI(
                    "any://" + cluster.getHostname() + "/"));
        } catch (URISyntaxException e) {
            // should not happen
        }
        return broker.submitJob(jd);
    }

    private org.gridlab.gat.resources.Job submitSubJob(Run run,
            GATContext context, Job job, SubJob subJob, String poolID,
            String server, String hubAddresses, int runTime)
            throws GATInvocationException, GATObjectCreationException,
            URISyntaxException {

        // retrieve the application of this job and the grid where it should run
        Application app = subJob.getApplication();
        Grid grid = run.getGrid(subJob.getGridName());
        Cluster cluster = grid.getCluster(subJob.getClusterName());

        Preferences preferences = new Preferences();
        preferences.put("ResourceBroker.adaptor.name", cluster.getAccessType());
        preferences.put("File.adaptor.name", cluster.getFileAccessType());

        File outFile = GAT.createFile(context, preferences, new URI("any:///"
                + job.getName() + "." + subJob.getName() + "."
                + subJob.getApplication().getName() + ".stdout"));
        File errFile = GAT.createFile(context, preferences, new URI("any:///"
                + job.getName() + "." + subJob.getName() + "."
                + subJob.getApplication().getName() + ".stderr"));

        // TODO Change this to a JavaSoftwareDescription!
        SoftwareDescription sd = new SoftwareDescription();
        sd.setAttributes(subJob.getAttributes());
        sd.setExecutable(cluster.getJavaHome() + "/bin/java");
        // add ibis/lib jars to the classpath
        String classpath = ".";
        java.io.File tmp = new java.io.File(ibisHome + "/lib");
        String[] jars = tmp.list();
        for (String jar : jars) {
            classpath += ":lib/" + jar;
        }
        // add executable jar to the classpath
        if (app.getClasspath() != null && !app.getClasspath().equals("")) {
            classpath += ":" + app.getClasspath();
        }

        Map<String, Object> env = sd.getEnvironment();
        String[] envArguments = null;
        if (env != null && !env.isEmpty()) {
            envArguments = new String[sd.getEnvironment().size()];
            Set<String> s = env.keySet();
            Object[] keys = (Object[]) s.toArray();
            for (int i = 0; i < keys.length; i++) {
                String val = (String) env.get(keys[i]);
                envArguments[i] = "-D" + keys[i] + "=" + val;
            }
        }
        String[] javaFlags = app.getJavaFlags();
        String[] appArguments = app.getParameters();
        int argumentsSize = 10;
        // fixed number of arguments (classpath(2), ibis(6), log4j(1) and java
        // main class(1))
        if (javaFlags != null) {
            argumentsSize += javaFlags.length;
        }
        if (envArguments != null) {
            argumentsSize += envArguments.length;
        }
        if (appArguments != null) {
            argumentsSize += appArguments.length;
        }
        String[] arguments = new String[argumentsSize];
        int pos = 0;
        if (javaFlags != null) {
            for (int i = 0; i < javaFlags.length; i++) {
                arguments[pos++] = javaFlags[i];
            }
        }
        arguments[pos++] = "-classpath";
        arguments[pos++] = classpath;
        arguments[pos++] = "-Dlog4j.configuration=file:log4j.properties";
        arguments[pos++] = "-Dibis.server.address=" + server;
        arguments[pos++] = "-Dibis.server.hub.addresses=" + hubAddresses;
        arguments[pos++] = "-Dibis.pool.name=" + poolID;
        arguments[pos++] = "-Dibis.pool.size=" + job.getTotalCPUCount();
        arguments[pos++] = "-Dibis.location.postfix=" + subJob.getClusterName();
        arguments[pos++] = "-Dibis.location.automatic=true";

        // add Satin arguments? or let the user do it?
        // environment.put("satin.closed", "true");
        // environment.put("satin.alg", "RS");
        // environment.put("satin.detailedStats", "true");
        // environment.put("satin.closeConnections", "false");

        if (envArguments != null) {
            for (int i = 0; i < envArguments.length; i++) {
                arguments[pos++] = envArguments[i];
            }
        }
        arguments[pos++] = app.getExecutable();
        if (appArguments != null) {
            for (int i = 0; i < appArguments.length; i++) {
                arguments[pos++] = appArguments[i];
            }
        }
        sd.setArguments(arguments);
        sd.addPreStagedFile(GAT.createFile(context, preferences, ibisHome
                + "/lib"));
        String cwd = System.getProperty("user.dir");
        sd.addPreStagedFile(GAT.createFile(context, preferences, cwd + "/"
                + "log4j.properties"));
        sd.addPreStagedFile(GAT.createFile(context, preferences, cwd + "/"
                + "smartsockets.properties"));
        sd.setStderr(errFile);
        sd.setStdout(outFile);
        logger.info("before adding prestage files!");
        for (String filename : app.getPreStaged()) {
            if (logger.isInfoEnabled()) {
                logger.info("adding prestage file: " + filename);
            }
            sd.addPreStagedFile(GAT.createFile(context, preferences, filename));
        }
        logger.info("after adding prestage files!");
        for (String filename : app.getPostStaged()) {
            sd.addPostStagedFile(
                    GAT.createFile(context, preferences, filename), GAT
                            .createFile(context, preferences, job.getName()
                                    + "." + subJob.getName() + "." + filename));
            logger.info("added poststage file '" + filename + "' -> '"
                    + job.getName() + "." + subJob.getName() + "." + filename + "'");
        }
        int machineCount = subJob.getMachineCount();
        if (machineCount == 0)
            machineCount = cluster.getMachineCount();
        int CPUsPerMachine = subJob.getCoresPerMachine();
        if (CPUsPerMachine == 0)
            CPUsPerMachine = cluster.getCPUsPerMachine();
        sd.addAttribute("count", machineCount * CPUsPerMachine);
        sd.addAttribute("host.count", machineCount);

        if (runTime < 0) {
            sd.addAttribute("walltime.max", "20");
        } else {
            sd.addAttribute("walltime.max", "" + runTime);
        }

        Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();
        hardwareAttributes.put("machine.node", cluster.getHostname());

        ResourceDescription rd = new HardwareResourceDescription(
                hardwareAttributes);

        JobDescription jd = new JobDescription(sd, rd);
        context.addPreferences(preferences);
        ResourceBroker broker = GAT.createResourceBroker(context, preferences,
                new URI("any://" + cluster.getHostname() + "/jobmanager-sge"));
        org.gridlab.gat.resources.Job j = broker.submitJob(jd, this,
                "job.status");
        return j;
    }

    public synchronized void processMetricEvent(MetricValue val) {
        String state = (String) val.getValue();
        org.gridlab.gat.resources.Job j = (org.gridlab.gat.resources.Job) val
                .getSource();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Job status of " + j.getJobID() + " changed to : "
                        + state);
            }
        } catch (GATInvocationException e) {
            // ignore this.
        }
        notifyAll();
    }
}
