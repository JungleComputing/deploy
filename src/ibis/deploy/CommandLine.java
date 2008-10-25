package ibis.deploy;

import java.io.File;

import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLine implements MetricListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CommandLine.class);

	public static void main(String[] arguments) throws Exception {

		Deploy deploy = new Deploy();

		File ibisLib = new File("lib-server");
		File log4j = new File("log4j.properties");

		Grid localGrid = new Grid(new File("grids/local.grid"));
		Grid das3 = new Grid(new File("grids/das3.grid"));

		ApplicationGroup applications = new ApplicationGroup(new File(
				"applications/examples.applications"));

		System.err.println(applications.toPrintString());

		applications.save(new File("application.tmp"));

		System.err.println(localGrid.toPrintString());
		//
		// Cluster serverCluster = localGrid.getCluster("local");
		//
		deploy.initialize(null, ibisLib, log4j);
		//
		// deploy.submitHub(serverCluster, false);
		//
		// deploy.submitHub(das3.getCluster("VU"), false);
		//		
		// deploy.submitHub(das3.getCluster("VU"), false);
		//
		// deploy.submitHub(das3.getCluster("VU"), false);
		//
		Job job1 = deploy.submitJob(localGrid.getCluster("local"), 1, applications
				.getApplication("RegistryUpcalls"), 1, "test", null, null,
				false);
		Job job2 = deploy.submitJob(localGrid.getCluster("local"), 1, applications
				.getApplication("RegistryUpcalls"), 1, "test", null, null,
				false);

		job1.waitUntilFinished();
		job2.waitUntilFinished();

		deploy.end();
	}

	public void processMetricEvent(MetricEvent event) {
		logger.info("Command line client, got event: " + event);
		
	}
}
