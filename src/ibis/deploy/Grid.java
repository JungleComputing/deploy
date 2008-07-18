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

	/**
	 * Creates a {@link Grid} with the name <code>gridName</code>
	 *
	 * @param gridName
	 *            the name of the Grid
	 */
	public Grid(String gridName) {
		this.gridName = gridName;
	}

	/**
	 * Gets the name of the Grid
	 *
	 * @return the name of the Grid
	 */
	public String getGridName() {
		return gridName;
	}

	/**
	 * Adds a {@link Cluster} to this Grid
	 *
	 * @param cluster
	 *            the {@link Cluster} to be added
	 */
	public void addCluster(Cluster cluster) {
		clusters.add(cluster);
	}

	/**
	 * Gets the {@link Cluster}s in this Grid
	 *
	 * @return the {@link Cluster}s in this Grid.
	 */
	public Cluster[] getClusters() {
		return clusters.toArray(new Cluster[clusters.size()]);
	}

	/**
	 * Gets the {@link Cluster} with name <code>clusterName</code>.
	 *
	 * @param clusterName
	 *            the name of the {@link Cluster}
	 * @return a {@link Cluster} with the <code>clusterName</code> or
	 *         <code>null</code> if no {@link Cluster} with name
	 *         <code>clusterName</code> exists in this Grid
	 */
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

	/**
	 * Gets the total number of nodes in this Grid
	 *
	 * @return the total number of nodes in this Grid
	 */
	public int getTotalNodes() {
		int res = 0;
		for (int i = 0; i < clusters.size(); i++) {
			Cluster c = (Cluster) clusters.get(i);
			res += c.getNodes();
		}
		return res;
	}

	/**
	 * Gets the total number of cores in this Grid
	 *
	 * @return the total number of cores in this Grid
	 */
	public int getTotalCores() {
		int res = 0;
		for (int i = 0; i < clusters.size(); i++) {
			Cluster c = (Cluster) clusters.get(i);
			res += c.getNodes() * c.getMulticore();
		}
		return res;
	}

	/**
	 * Loads a grid from a {@link TypedProperties} object. The following
	 * properties can be set:
	 * <p>
	 * <TABLE border="2" frame="box" rules="groups" summary="grid properties">
	 * <CAPTION>grid properties </CAPTION> <COLGROUP align="left"> <COLGROUP
	 * align="center"> <COLGROUP align="left" > <THEAD valign="top">
	 * <TR>
	 * <TH>Property
	 * <TH>Example
	 * <TH>Description<TBODY>
	 * <TR>
	 * <TD>clusters
	 * <TD>myCluster1,myCluster2
	 * <TD>the names of the clusters described in the properties
	 * <TR>
	 * <TD>all cluster properties
	 * <TD>
	 * <TD>see {@link Cluster#load(TypedProperties, String, String)}
	 * <TR> </TABLE>
	 *
	 * @param gridprops
	 *            the grid and cluster properties
	 * @return the loaded grid
	 */
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
				Cluster cluster = Cluster
						.load(gridprops, gridName, clusterName);
				if (cluster != null) {
					grid.addCluster(cluster);
				}
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

		res += "total machine count: " + getTotalNodes() + " total CPU count: "
				+ getTotalCores();
		return res;
	}
}