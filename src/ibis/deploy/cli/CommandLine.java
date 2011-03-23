package ibis.deploy.cli;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Cluster;
import ibis.deploy.Deploy;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;
import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.Workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line interface for ibis-deploy library
 * 
 * @author Niels Drost
 * 
 */
public class CommandLine {

	private static final Logger logger = LoggerFactory
			.getLogger(CommandLine.class);

	// run a single experiment
	private static void runExperiment(Experiment experiment, Grid grid,
			ApplicationSet applications, Deploy deploy, boolean verbose)
			throws Exception {
		logger.info("Running experiment \"" + experiment.getName() + "\"");
		logger.debug("Running experiment: " + experiment.toPrintString());

		// start jobs
		List<Job> jobs = new ArrayList<Job>();
		for (JobDescription jobDescription : experiment.getJobs()) {
			Job job = deploy.submitJob(jobDescription, applications, grid,
					null, null);
			jobs.add(job);
		}

		// wait for all jobs to end
		deploy.waitUntilJobsFinished();

		logger.info("Experiment \"" + experiment + "\" done");
	}

	private static void printUsage() {
		System.err.println("Usage: ibis-deploy-cli [OPTIONS] WORKSPACE_DIR");
		System.err.println("Options:");
		System.err.println("-s CLUSTER\tRun server on specified cluster");
		System.err.println("-v\t\tVerbose mode");
		System.err.println("-k\t\tKeep sandboxes");
		System.err
				.println("-p PORT\t\tLocal port number (defaults to random free port)");
		System.err.println("-h | --help\tThis message");
	}

	/**
	 * @param arguments
	 *            arguments of application
	 */
	public static void main(String[] arguments) {
		File workspaceDir = Workspace.DEFAULT_LOCATION;
		boolean verbose = false;
		boolean keepSandboxes = false;
		String serverCluster = null;
		int port = 0;
		Workspace workspace = null;

		if (arguments.length == 0) {
			printUsage();
			System.exit(0);
		}

		try {

			for (int i = 0; i < arguments.length; i++) {
				if (arguments[i].equals("-s")) {
					i++;
					serverCluster = arguments[i];
				} else if (arguments[i].equals("-v")) {
					verbose = true;
				} else if (arguments[i].equals("-k")) {
					keepSandboxes = true;
				} else if (arguments[i].equals("-p")) {
					i++;
					port = Integer.parseInt(arguments[i]);
				} else if (arguments[i].equals("-h")
						|| arguments[i].equals("--help")) {
					printUsage();
					System.exit(0);
				} else {
					File file = new File(arguments[i]);
					if (file.isDirectory()) {
						workspaceDir = file;
					} else {
						System.err.println("Unknown option: " + arguments[i]);
						printUsage();
						System.exit(1);
					}
				}
			}

			if (!workspaceDir.isDirectory()) {
				System.err.println("ERROR: " + workspaceDir
						+ " does not exist, or is not a directory");
			}

			// load workspace
			workspace = new Workspace(workspaceDir);

			if (verbose) {
				System.err.println(workspace.toPrintString());
			}

			if (workspace.getExperiments().size() == 0) {
				System.err.println("ERROR: no experiments specified!");
				printUsage();
				System.exit(1);
			}

			Cluster cluster = null;
			if (serverCluster == null) {
				logger.info("Initializing Command Line Ibis Deploy"
						+ ", using build-in server");
				cluster = null;
			} else {
				logger
						.info("Initializing Command Line Ibis Deploy"
								+ ", using server on cluster \""
								+ serverCluster + "\"");

				cluster = workspace.getGrid().getCluster(serverCluster);

				if (cluster == null) {
					System.err.println("ERROR: Server cluster " + serverCluster
							+ " not found in grid");
					System.exit(1);
				}
			}

			Deploy deploy = new Deploy(null, verbose, keepSandboxes, port,
					cluster, null, true);

			// run experiments
			for (Experiment experiment : workspace.getExperiments()) {

				runExperiment(experiment, workspace.getGrid(), workspace
						.getApplications(), deploy, verbose);
			}

			deploy.end();
		} catch (Exception e) {
			logger.error("Exception on running experiments", e);
			System.exit(1);
		}
	}

}
