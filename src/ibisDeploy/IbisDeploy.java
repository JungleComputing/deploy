package ibisDeploy;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.gridlab.gat.*;
import org.gridlab.gat.io.*;
import org.gridlab.gat.monitoring.*;
import org.gridlab.gat.resources.*;

public class IbisDeploy implements MetricListener {
	String gatLocation;

	String ibisHome;

	String ibisAppsHome;

        String satinHome;
        String ibisServer = "fs0.das2.cs.vu.nl";

	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			System.err
					.println("usage: satinRunner <runFile> [runTime in seconds]");
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
		System.err.println("using GAT at: " + gatLocation);

                ibisHome = System.getenv("IBIS_HOME");
                if (ibisHome == null) {
                        System.err.println("please set your IBIS_HOME");
                        System.exit(1);
                }
                System.err.println("using Ibis at: " + ibisHome);

                satinHome = System.getenv("SATIN_HOME");
                if (satinHome == null) {
                        System.err.println("please set your SATIN_HOME");
                        System.exit(1);
                }
                System.err.println("using Satin at: " + satinHome);

		ibisAppsHome = System.getenv("IBIS_APPS_HOME");
		if (ibisAppsHome == null) {
			System.err.println("please set your IBIS_APPS_HOME");
			System.exit(1);
		}
		System.err.println("using Ibis applications at: " + ibisAppsHome);

                String s = System.getenv("IBIS_SERVER");
                if (s != null) {
                    ibisServer = s;
                }

		Run run = Run.loadRun(runFile);

		System.err.println(run);

		GATContext context = new GATContext();
		context.addPreference("ignoreHiddenFiles", "true");
		context.addPreference("ftp.connection.passive", "false");

		ArrayList<Job> requested = run.getRequestedResources();

		for (int i = 0; i < requested.size(); i++) {
			try {
				submitJob(run, context, requested.get(i), runTime);
			} catch (Exception e) {
				// error was already printed.
				GAT.end();
				System.exit(1);
			}
		}

		GAT.end();
		System.exit(1);
	}

	public void submitJob(Run run, GATContext context, Job job, int runTime)
			throws GATInvocationException, GATObjectCreationException,
			URISyntaxException {
		org.gridlab.gat.resources.Job[] jobs = new org.gridlab.gat.resources.Job[job
				.numberOfSubJobs()];
		String poolID = "" + Math.random();
		for (int i = 0; i < job.numberOfSubJobs(); i++) {
			try {
				jobs[i] = submitSubJob(run, context, job, job.get(i), poolID,
						runTime);
			} catch (GATInvocationException e) {
				System.err
						.println("submission of job " + job.get(i) + " failed: " + e);
				e.printStackTrace();
				throw e;
			} catch (GATObjectCreationException e) {
				System.err
						.println("submission of job " + job.get(i) + " failed: " + e);
				e.printStackTrace();
				throw e;
			} catch (URISyntaxException e) {
				System.err
						.println("submission of job " + job.get(i) + " failed: " + e);
				e.printStackTrace();
				throw e;
			}
		}

		System.err.println("job " + job.getJobNr()
				+ " submitted, waiting for subjobs");

		synchronized (this) {
			for (int i = 0; i < job.numberOfSubJobs(); i++) {
				// wait until job is done
				while ((jobs[i].getState() != org.gridlab.gat.resources.Job.STOPPED)
						&& (jobs[i].getState() != org.gridlab.gat.resources.Job.SUBMISSION_ERROR)) {
					try {
						wait();
					} catch (Exception e) {
						// ignore
					}
				}
			}
		}
	}

	public org.gridlab.gat.resources.Job submitSubJob(Run run,
			GATContext context, Job job, SubJob subJob, String poolID,
			int runTime) throws GATInvocationException,
			GATObjectCreationException, URISyntaxException {

		System.err.println("submit of job " + job.getJobNr() + " subJob "
				+ subJob.getSubJobNr());

		Application app = run.getApp();
		Grid grid = run.getGrid();
		Cluster cluster = grid.getCluster(subJob.getClusterName());

		Preferences prefs = new Preferences();
		prefs.put("ResourceBroker.adaptor.name", cluster.getAccessType());
		prefs.put("File.adaptor.name", cluster.getFileAccessType());
		File outFile = GAT.createFile(context, prefs, new URI("any:///"
				+ run.getRunFileName() + "." + subJob.getClusterName() + "."
				+ job.getJobNr() + "." + subJob.getSubJobNr() + "."
				+ job.getTotalMachineCount() + "." + job.getTotalCPUCount()
				+ ".stdout"));
		File errFile = GAT.createFile(context, prefs, new URI("any:///"
				+ run.getRunFileName() + "." + subJob.getClusterName() + "."
				+ job.getJobNr() + "." + subJob.getSubJobNr() + "."
				+ job.getTotalMachineCount() + "." + job.getTotalCPUCount()
				+ ".stderr"));

		File ibisLib = GAT.createFile(context, prefs,
				new URI(satinHome + "/lib"));

		String classpath = "log4j.properties:smartsockets.properties:";

		String cwd = System.getProperty("user.dir");

		File log4jproperties = GAT.createFile(context, prefs, new URI(cwd + "/"
				+ "log4j.properties"));

		File smartsocketsproperties = GAT.createFile(context, prefs, new URI(
				cwd + "/" + "smartsockets.properties"));

		SoftwareDescription sd = new SoftwareDescription();
		sd.setLocation(new URI(app.getExecutable()));
		sd.setStdout(outFile);
		sd.setStderr(errFile);
		sd.addPreStagedFile(ibisLib);
		for (int i = 0; i < app.getPreStaged().length; i++) {
			URI u = new URI(ibisAppsHome + "/satin/" + app.getDirectoryName()
					+ "/" + app.getPreStaged()[i]);
			File tmp = GAT.createFile(context, prefs, u);
			sd.addPreStagedFile(tmp);
			classpath += tmp.getName() + ":";
		}
		for (int i = 0; i < app.getPostStaged().length; i++) {
			URI u = new URI(app.getPostStaged()[i]);
			File tmp = GAT.createFile(context, prefs, u);
			sd.addPostStagedFile(tmp);
		}
		sd.addPreStagedFile(log4jproperties);
		sd.addPreStagedFile(smartsocketsproperties);
		sd.setArguments(app.getArguments());

		int machineCount = subJob.getMachineCount();
		if (machineCount == 0)
			machineCount = cluster.getMachineCount();
		int CPUsPerMachine = subJob.getCPUsPerMachine();
		if (CPUsPerMachine == 0)
			CPUsPerMachine = cluster.getCPUsPerMachine();
		sd.addAttribute("count", machineCount * CPUsPerMachine);
		sd.addAttribute("hostCount", machineCount);
		sd.addAttribute("java.home", new URI(cluster.getJavaHome()));

		if (runTime < 0) {
			sd.addAttribute("maxWallTime", "600");
		} else {
			sd.addAttribute("maxWallTime", "" + runTime);
		}

		java.io.File tmp = new java.io.File(satinHome + "/lib");
		String[] jars = tmp.list();
		for (int i = 0; i < jars.length; i++) {
			classpath += ":lib/" + jars[i];
		}
		sd.addAttribute("java.classpath", classpath);
		sd.addAttribute("java.flags", app.getJavaFlagsAsString());

		HashMap<String, Object> environment = new HashMap<String, Object>();
		environment.put("ibis.server.address", ibisServer);
		// environment.put("ibis.registry.central.ping.interval", "6000");

		environment.put("ibis.pool.name", "satinRunner.job." + job.getJobNr()
				+ "." + poolID);

		environment.put("ibis.pool.size", "" + job.getTotalCPUCount());
		environment.put("ibis.location.postfix", subJob.getClusterName());
		environment.put("ibis.location.automatic", "true");

//		environment.put("satin.closed", "true");
		// environment.put("satin.alg", "RS");
//		environment.put("satin.detailedStats", "true");
		// environment.put("satin.closeConnections", "false");

		sd.setEnvironment(environment);

		Hashtable<String, Object> hardwareAttributes = new Hashtable<String, Object>();
		hardwareAttributes.put("machine.node", cluster.getHostname());

		ResourceDescription rd = new HardwareResourceDescription(
				hardwareAttributes);

		JobDescription jd = new JobDescription(sd, rd);

//		System.err.println("constructed job description: " + jd);

		ResourceBroker broker = GAT.createResourceBroker(context, prefs);

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
