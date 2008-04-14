/*
 * Created on Mar 6, 2006
 */
package ibis.deploy;

import ibis.util.TypedProperties;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Grid {
	private static Logger logger = Logger.getLogger(Grid.class);

	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();

	private String gridName;

	public Grid(String gridName) {
		this.gridName = gridName;
	}

	public String getGridName() {
		return gridName;
	}

	public void addCluster(Cluster a) {
		clusters.add(a);
	}

	public Cluster[] getClusters() {
		return clusters.toArray(new Cluster[clusters.size()]);
	}

	public Cluster getCluster(String clusterName) {
		if (clusters == null) {
			return null;
		}
		for (Cluster cluster : clusters) {
			if (cluster.getName().equalsIgnoreCase(clusterName)) {
				return cluster;
			}
		}
		return null;
	}

	// public Cluster getCluster(String name) {
	// for (int i = 0; i < clusters.size(); i++) {
	// if (clusters.get(i).getFriendlyName().equals(name)) {
	// return clusters.get(i);
	// }
	// }
	//
	// return null;
	// }

	public int getTotalMachineCount() {
		int res = 0;
		for (int i = 0; i < clusters.size(); i++) {
			Cluster c = (Cluster) clusters.get(i);
			res += c.getNodes();
		}
		return res;
	}

	public int getTotalCPUCount() {
		int res = 0;
		for (int i = 0; i < clusters.size(); i++) {
			Cluster c = (Cluster) clusters.get(i);
			res += c.getNodes() * c.getMulticore();
		}
		return res;
	}

	public static Grid load(TypedProperties gridprops) {
		if (logger.isInfoEnabled()) {
			logger.info("loading grid");
		}
		String gridName = gridprops.getProperty("name");
		Grid grid = new Grid(gridName);
		String[] clusterNames = TypedPropertiesUtility
				.getHierarchicalStringList(gridprops, gridName, "clusters",
						null, ",");
		if (clusterNames == null) {
			return null;
		}
		for (String clusterName : clusterNames) {
			try {
				grid.addCluster(Cluster.load(gridprops, gridName, clusterName));
			} catch (Exception e) {
				// TODO: something useful
			}
		}
		return grid;
	}

	public String toString() {
		String res = "grid " + gridName + " resources:\n";
		for (int i = 0; i < clusters.size(); i++) {
			res += "    " + clusters.get(i) + "\n";
		}

		res += "total machine count: " + getTotalMachineCount()
				+ " total CPU count: " + getTotalCPUCount();
		return res;
	}
}
