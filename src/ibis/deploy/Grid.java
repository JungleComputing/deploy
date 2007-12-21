/*
 * Created on Mar 6, 2006
 */
package ibis.deploy;

import ibis.smartsockets.util.TypedProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Grid {
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

    public Cluster getCluster(String name) {
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).getFriendlyName().equals(name)) {
                return clusters.get(i);
            }
        }

        return null;
    }

    public int getTotalMachineCount() {
        int res = 0;
        for (int i = 0; i < clusters.size(); i++) {
            Cluster c = (Cluster) clusters.get(i);
            res += c.getMachineCount();
        }
        return res;
    }

    public int getTotalCPUCount() {
        int res = 0;
        for (int i = 0; i < clusters.size(); i++) {
            Cluster c = (Cluster) clusters.get(i);
            res += c.getMachineCount() * c.getCPUsPerMachine();
        }
        return res;
    }

    public static Grid loadGrid(String filename) throws FileNotFoundException,
            IOException {
        System.err.println("loading grid: " + filename + " ...");
        TypedProperties gridprops = new TypedProperties();
        gridprops.load(new FileInputStream(filename));
        String gridName = gridprops.getProperty("name");
        Grid grid = new Grid(gridName);
        String[] clusterNames = gridprops.getStringList("clusters");
        for (String clusterName : clusterNames) {
            String headnode = gridprops.getProperty(clusterName + ".headnode");
            String resourceBrokerAdaptors = gridprops.getProperty(clusterName
                    + ".ResourceBrokerAdaptor");
            String fileAdaptors = gridprops.getProperty(clusterName
                    + ".FileAdaptor");
            int nodes = gridprops.getIntProperty(clusterName + ".nodes");
            int multicore = gridprops
                    .getIntProperty(clusterName + ".multicore");
            String javaPath = gridprops.getProperty(clusterName + ".javapath");
            grid.addCluster(new Cluster(clusterName, headnode,
                    resourceBrokerAdaptors, fileAdaptors, nodes, multicore,
                    javaPath));
        }
        System.err.println("loading grid: " + filename + " DONE");
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
