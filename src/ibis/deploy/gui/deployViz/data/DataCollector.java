package ibis.deploy.gui.deployViz.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.gui.deployViz.DeployVizPanel;
import ibis.deploy.gui.deployViz.helpers.VizUtils;
import ibis.ipl.support.management.AttributeDescription;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class DataCollector extends Thread {

	private static final Logger logger = LoggerFactory
			.getLogger(DataCollector.class);

	private ManagementServiceInterface manInterface;
	private RegistryServiceInterface regInterface;
	private int refreshrate = 3000;
	private final DeployVizPanel vizPanel;
	private boolean collecting = true;

	private HashMap<String, Set<String>> ibisesPerSite = new HashMap<String, Set<String>>();
	private HashMap<String, HashMap<String, Long>> connectionsPerIbis = new HashMap<String, HashMap<String, Long>>();

	private static final AttributeDescription sentBytesPerIbis = new AttributeDescription(
			"ibis", "sentBytesPerIbis");

	private static final AttributeDescription receivedBytesPerIbis = new AttributeDescription(
			"ibis", "receivedBytesPerIbis");

	public DataCollector(ManagementServiceInterface manInterface,
			RegistryServiceInterface regInterface, DeployVizPanel vPanel) {

		this.manInterface = manInterface;
		this.regInterface = regInterface;
		this.vizPanel = vPanel;
	}

	public synchronized void setCollectingState(boolean state) {
		collecting = state;
	}

	public synchronized boolean isCollecting() {
		return collecting;
	}

	@Override
	public void run() {
		while (true) {
			if (collecting) {

				collectIbisData();

				// update the UI
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						vizPanel.updateVisualization(ibisesPerSite,
								connectionsPerIbis);
					}
				});

			}

			try {
				Thread.sleep(refreshrate);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void collectIbisData() {
		
		Map<String, Integer> poolSizes = null;
		IbisIdentifier[] ibises = null;
		String poolName, ibisLocation;
		String[] locationList;
		Set<String> ibisList;
		String ibisName, neighbourName;

		try {
			poolSizes = regInterface.getPoolSizes();
		} catch (IOException e) {
			System.err.println("Couldn\'t retrieve pool sizes");
			e.printStackTrace();
		}

		// remove the old data completely
		ibisesPerSite.clear();
		connectionsPerIbis.clear();

		if (poolSizes != null) {

			for (Map.Entry<String, Integer> pool : poolSizes.entrySet()) {

				poolName = pool.getKey();

				// first we take into account the sites and the ibises
				try {

					// retrieve the list of locations
					locationList = regInterface.getLocations(poolName);

					// retrieve the list of ibises
					ibises = regInterface.getMembers(poolName);

					// The site name is after the @ sign, we make sure this
					// array only contains unique names
					for (int i = 0; i < locationList.length; i++) {
						locationList[i] = locationList[i].split("@")[1];

						// if the location didn't previously exist in the list,
						// add it
						if (!ibisesPerSite.containsKey(locationList[i])) {
							ibisesPerSite.put(locationList[i],
									new HashSet<String>());
						}

					}

					// add all the ibises
					for (IbisIdentifier ibis : ibises) {
						ibisLocation = ibis.location().toString().split("@")[1];

						ibisName = ibis.name() + "-" + ibis.location();

						// check if that location exists, if not, create it
						if (ibisesPerSite.get(ibisLocation) == null) {
							ibisList = new HashSet<String>();
						} else {
							ibisList = ibisesPerSite.get(ibisLocation);
						}

						// add the ibis to the corresponding list, if it's not
						// already there
						ibisList.add(ibisName);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

				Object[] tempResult;
				HashMap<IbisIdentifier, Long> sentBytes, receivedBytes;
				HashMap<String, Long> totalBytes;
				long sum = 0;

				// retrieve the connections per ibis
				for (IbisIdentifier ibis : ibises) {

					ibisName = ibis.name() + "-" + ibis.location();

					// retrieve the connection hashmap for this ibis
					if (connectionsPerIbis.get(ibisName) == null) {
						totalBytes = new HashMap<String, Long>();
					} else {
						totalBytes = connectionsPerIbis.get(ibisName);
						totalBytes.clear();
					}

					try {

						// get the number of bytes sent to every connected ibis
						tempResult = manInterface.getAttributes(ibis,
								sentBytesPerIbis);
						sentBytes = (HashMap<IbisIdentifier, Long>) tempResult[0];

						// get the number of bytes received from every connected
						// ibis
						tempResult = manInterface.getAttributes(ibis,
								receivedBytesPerIbis);
						receivedBytes = (HashMap<IbisIdentifier, Long>) tempResult[0];

						for (IbisIdentifier neighbour : sentBytes.keySet()) {
							neighbourName = neighbour.name() + "-"
									+ neighbour.location();

							// if the reverse edge is already in the graph, just
							// add this value to the existing value - the weight
							// of the edge will simply be the number of sent
							// bytes from each direction
							if (connectionsPerIbis.get(neighbourName) != null
									&& connectionsPerIbis.get(neighbourName)
											.get(ibisName) != null) {
								sum = connectionsPerIbis.get(neighbourName)
										.get(ibisName)
										+ sentBytes.get(neighbour);
								connectionsPerIbis.get(neighbourName).put(
										ibisName, sum);
							} else {
								totalBytes.put(neighbourName, sentBytes
										.get(neighbour));
								connectionsPerIbis.put(ibisName, totalBytes);
							}

						}

					} catch (Exception e1) {
						logger.error("Could not get monitor info from" + ibis.name() + ": " + e1.getMessage() + ". Did you enable monitoring in the application?");
						logger.debug("Could not get monitoring info", e1);
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							//wait a bit
						}

						// e1.printStackTrace();
						// TODO This one happens pretty often, usually a
						// socket-related exception. Printing the stack clogs up
						// the output...
					}
				}
			}
		}

		VizUtils.updateMinMaxWeights(connectionsPerIbis);
	}

	// this can be used for testing
	private void generateRandomData() {
		int random;
		int sites = 10, nibises = 15;

		ibisesPerSite.clear();
		for (int i = 0; i < sites; i++) {
			random = (int) (Math.random() * 100);
			random = (int) (Math.random() * 100);
			if (random % 2 == 0) {
				String sitename = "site" + i;
				HashSet<String> ibises = new HashSet<String>();
				ibisesPerSite.put(sitename, ibises);
				for (int j = 0; j < nibises; j++) {

					String startIbis = sitename + "ibis" + j;

					HashMap<String, Long> tempedge = connectionsPerIbis
							.get(startIbis);
					if (tempedge == null) {
						tempedge = new HashMap<String, Long>();
					} else {
						tempedge.clear();
					}

					ibises.add(startIbis);
					for (int k = 0; k < nibises; k++) {
						String stopIbis = sitename + "ibis" + k;
						random = (int) (Math.random() * 100);
						if (random % 2 == 0) {
							if (k != j) {

								tempedge.put(stopIbis,
										(long) (Math.random() * 100));
								connectionsPerIbis.put(startIbis, tempedge);
							}
						}
					}
				}
			}
		}
	}
}
