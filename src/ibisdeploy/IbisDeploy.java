package ibisdeploy;

import ibis.server.Server;
import ibis.smartsockets.direct.DirectSocketAddress;

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

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.security.CertificateSecurityContext;

public class IbisDeploy implements MetricListener {

    String gatLocation;

    String ibisHome;

    String ibisAppsHome;

    String satinHome;
    
    CertificateSecurityContext securityContext;

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err
                    .println("usage: ibis-deploy <runFile> [runTime in seconds]");
        }

        int time = -1;
        if (args.length == 2) {
            time = Integer.parseInt(args[1]);
        }
        new IbisDeploy().start(args[0], time);
    }

    public void start(String runFile, int runTime) {
        gatLocation = System.getenv("GAT_LOCATION");
        if (gatLocation == null) {
            System.err.println("please set your GAT_LOCATION");
            System.exit(1);
        }
        System.err.println("using JavaGAT at: " + gatLocation);

        ibisHome = System.getenv("IBIS_HOME");
        if (ibisHome == null) {
            System.err.println("please set your IBIS_HOME");
            System.exit(1);
        }
        System.err.println("using Ibis at: " + ibisHome);

        // satinHome = System.getenv("SATIN_HOME");
        // if (satinHome == null) {
        // System.err.println("please set your SATIN_HOME");
        // // System.exit(1);
        // }
        // System.err.println("using Satin at: " + satinHome);

        // ibisAppsHome = System.getenv("IBIS_APPS_HOME");
        // if (ibisAppsHome == null) {
        // System.err.println("please set your IBIS_APPS_HOME");
        // // System.exit(1);
        // }
        // System.err.println("using Ibis applications at: " + ibisAppsHome);

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

        System.err.println(run);

        GATContext context = new GATContext();
        String passphrase = null;
        JPasswordField pwd = new JPasswordField();
        Object[] message = { "grid-proxy-init\nPlease enter your passphrase.",
                        pwd };
        JOptionPane.showMessageDialog(null, message, "Grid-Proxy-Init",
                        JOptionPane.QUESTION_MESSAGE);
        passphrase = new String(pwd.getPassword());
        String home = System.getProperty("user.home");
        securityContext = null;
        try {
            securityContext = new CertificateSecurityContext(
                            new URI(home + "/.globus/userkey.pem"), new URI(
                                    home + "/.globus/usercert.pem"), passphrase);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        
        context.addPreference("ignoreHiddenFiles", "true");
        context.addPreference("ftp.connection.passive", "false");

        ArrayList<Job> requested = run.getRequestedResources();

        for (int i = 0; i < requested.size(); i++) {
            try {
                submitJob(run, context, requested.get(i), runTime);
            } catch (Exception e) {
                e.printStackTrace();
                GAT.end();
                System.exit(1);
            }
        }

        GAT.end();
        System.exit(1);
    }

    public synchronized void submitJob(Run run, GATContext context, Job job,
            int runTime) throws GATInvocationException,
            GATObjectCreationException, URISyntaxException {
        org.gridlab.gat.resources.Job[] jobs = new org.gridlab.gat.resources.Job[job
                .numberOfSubJobs()];
        String poolID = "ibis-deploy-" + Math.random();
        // start an ibisserver and a hub on the submitting machine
        Server server = startServer();
        System.err.println("Local server started! (" + server.getLocalAddress()
                + ")");
        String knownHubs = server.getLocalAddress();
        for (DirectSocketAddress address : server.getHubs()) {
            knownHubs += "," + address;
        }
        System.out.println("known hubs: " + knownHubs);
        // start a hub on the other nodes
        ArrayList<org.gridlab.gat.resources.Job> hubJobs = new ArrayList<org.gridlab.gat.resources.Job>();
        Cluster[] clusters = run.getHubClusters();
        for (Cluster cluster : clusters) {
            String hubAddressFileName = ".ibis-deploy-hubaddress-"
                    + Math.random();
            hubJobs.add(startHub(cluster, knownHubs,
                    "../" + hubAddressFileName, context));
            System.err.println("Hub started at: " + cluster.getHostname());
            File hubAddressFile = GAT.createFile(context, "ssh://"
                    + cluster.getHostname() + "/" + hubAddressFileName);
            while (!hubAddressFile.exists()) {
                System.err.println("waiting for " + hubAddressFile.toString()
                        + " to appear.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            FileInputStream in = GAT.createFileInputStream(context,
                    hubAddressFile);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            try {
                String hubAddress = reader.readLine();
                knownHubs += "," + hubAddress;
                server.addHubs(hubAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        server.addHubs(knownHubs.split(","));
        context.addSecurityContext(securityContext);

        // if both server and hubs are up and running, start the real subjobs
        for (int i = 0; i < job.numberOfSubJobs(); i++) {
            try {
                jobs[i] = submitSubJob(run, context, job, job.get(i), poolID,
                        server.getLocalAddress(), knownHubs, runTime);
            } catch (GATInvocationException e) {
                System.err.println("submission of job " + job.get(i)
                        + " failed: " + e);
                e.printStackTrace();
                throw e;
            } catch (GATObjectCreationException e) {
                System.err.println("submission of job " + job.get(i)
                        + " failed: " + e);
                e.printStackTrace();
                throw e;
            } catch (URISyntaxException e) {
                System.err.println("submission of job " + job.get(i)
                        + " failed: " + e);
                e.printStackTrace();
                throw e;
            }
        }
        // wait for all submitted jobs to be finished;
        for (int i = 0; i < job.numberOfSubJobs(); i++) {
            while (jobs[i].getState() != org.gridlab.gat.resources.Job.STOPPED
                    && jobs[i].getState() != org.gridlab.gat.resources.Job.SUBMISSION_ERROR) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }

        server.end(true);
        System.err.println("Stopping hubs ...");
        for (org.gridlab.gat.resources.Job hubJob : hubJobs) {
            hubJob.stop();
        }
        System.err.println("Stopping hubs DONE");
    }

    private Server startServer() {
        // start up a server at the submitting machine
        Properties properties = new Properties();
        // let the server automatically find a free port
        properties.put("ibis.server.port", "0");
        Server server = null;
        try {
            server = new Server(properties);
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
        Preferences preferences = new Preferences();
        preferences.put("Resourcebroker.adaptor.name", "CommandlineSsh");
        preferences.put("File.adaptor.name", cluster.getFileAccessType());
        Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();
        hardwareAttributes.put("machine.node", cluster.getHostname());

        ResourceDescription rd = new HardwareResourceDescription(
                hardwareAttributes);

        SoftwareDescription sd = new SoftwareDescription();
        try {
            sd.setLocation(new URI("ssh://" + cluster.getHostname() + "/"
                    + cluster.getJavaHome() + "/bin/java"));
        } catch (URISyntaxException e) {
            // should not happen
        }
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
        sd.addPreStagedFile(GAT.createFile(context, preferences, ibisHome + "/"
                + "smartsockets.properties"));
        sd.setStderr(GAT.createFile(context, "hub@" + cluster.getFriendlyName()
                + ".err"));
        sd.setStdout(GAT.createFile(context, "hub@" + cluster.getFriendlyName()
                + ".out"));
        JobDescription jd = new JobDescription(sd, rd);
        ResourceBroker broker = GAT.createResourceBroker(context, preferences);
        return broker.submitJob(jd);
    }

    private org.gridlab.gat.resources.Job submitSubJob(Run run,
            GATContext context, Job job, SubJob subJob, String poolID,
            String server, String hubAddresses, int runTime)
            throws GATInvocationException, GATObjectCreationException,
            URISyntaxException {

        System.err.println("submit of job " + job.getJobNr() + " subJob "
                + subJob.getSubJobNr());

        Application app = run.getApp();
        Grid grid = run.getGrid(subJob.getGridName());
        Cluster cluster = grid.getCluster(subJob.getClusterName());

        Preferences preferences = new Preferences();
        preferences.put("ResourceBroker.adaptor.name", cluster.getAccessType());
        preferences.put("File.adaptor.name", cluster.getFileAccessType());
        File outFile = GAT.createFile(context, preferences, new URI("any:///"
                + run.getRunFileName() + "." + subJob.getClusterName() + "."
                + job.getJobNr() + "." + subJob.getSubJobNr() + "."
                + job.getTotalMachineCount() + "." + job.getTotalCPUCount()
                + ".stdout"));
        File errFile = GAT.createFile(context, preferences, new URI("any:///"
                + run.getRunFileName() + "." + subJob.getClusterName() + "."
                + job.getJobNr() + "." + subJob.getSubJobNr() + "."
                + job.getTotalMachineCount() + "." + job.getTotalCPUCount()
                + ".stderr"));

        SoftwareDescription sd = new SoftwareDescription();
        sd.setLocation(new URI("any://" + cluster.getHostname() + "/"
                + cluster.getJavaHome() + "/bin/java"));
        // add ibis/lib jars to the classpath
        String classpath = ".";
        java.io.File tmp = new java.io.File(ibisHome + "/lib");
        String[] jars = tmp.list();
        for (int i = 0; i < jars.length; i++) {
            classpath += ":lib/" + jars[i];
        }
        // add executable jar to the classpath
        classpath += app.getClasspath();

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
        String[] appArguments = app.getArguments();
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
                arguments[i] = javaFlags[pos++];
            }
        }
        arguments[pos++] = "-classpath";
        arguments[pos++] = classpath;
        arguments[pos++] = "-Dlog4j.configuration=file:./log4j.properties";
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
        for (String filename : app.getPreStaged()) {
            sd.addPreStagedFile(GAT.createFile(context, preferences, filename));
        }
        for (String filename : app.getPostStaged()) {
            sd.addPreStagedFile(GAT.createFile(context, preferences, filename));
        }
        int machineCount = subJob.getMachineCount();
        if (machineCount == 0)
            machineCount = cluster.getMachineCount();
        int CPUsPerMachine = subJob.getCPUsPerMachine();
        if (CPUsPerMachine == 0)
            CPUsPerMachine = cluster.getCPUsPerMachine();
        sd.addAttribute("count", machineCount * CPUsPerMachine);
        sd.addAttribute("hostCount", machineCount);

        if (runTime < 0) {
            sd.addAttribute("maxWallTime", "600");
        } else {
            sd.addAttribute("maxWallTime", "" + runTime);
        }

        Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();
        hardwareAttributes.put("machine.node", cluster.getHostname());

        ResourceDescription rd = new HardwareResourceDescription(
                hardwareAttributes);

        JobDescription jd = new JobDescription(sd, rd);
        context.addPreference("ResourceBroker.jobmanagerContact", cluster.getHostname() + "/jobmanager-sge");
        ResourceBroker broker = GAT.createResourceBroker(context, preferences);
        org.gridlab.gat.resources.Job j = broker.submitJob(jd);
        MetricDefinition md = j.getMetricDefinitionByName("job.status");
        Metric m = md.createMetric(null);
        j.addMetricListener(this, m);

        return j;
    }

    public synchronized void processMetricEvent(MetricValue val) {
        String state = (String) val.getValue();
        org.gridlab.gat.resources.Job j = (org.gridlab.gat.resources.Job) val
                .getSource();

        String machine = (String) j.getJobDescription()
                .getResourceDescription().getResourceAttribute("machine.node");

        System.err.println("Job status of " + machine + " changed to : "
                + state);
        notifyAll();
    }
}
