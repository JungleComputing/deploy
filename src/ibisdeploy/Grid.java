/*
 * Created on Mar 6, 2006
 */
package ibisdeploy;

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

    public static Grid[] loadGrid(String filename) throws FileNotFoundException,
            IOException {
        System.err.println("loading grid: " + filename + " ...");
        TypedProperties gridprops = new TypedProperties();
        gridprops.load(new FileInputStream(filename));
        String[] gridNames = gridprops.getStringList("ibis.deploy.grids");
        Grid[] grids = new Grid[gridNames.length];
        int i = 0;
        for (String gridName : gridNames) {
            grids[i] = new Grid(gridName);
            String[] clusterNames = gridprops.getStringList("ibis.deploy."
                    + gridName + ".clusters");
            for (String clusterName : clusterNames) {
                String headnode = gridprops.getProperty("ibis.deploy."
                        + gridName + "." + clusterName + ".headnode");
                String resourceBrokersAdaptors = gridprops
                        .getProperty("ibis.deploy." + gridName + "."
                                + clusterName + ".ResourceBrokerAdaptor");
                String fileAdaptors = gridprops.getProperty("ibis.deploy."
                        + gridName + "." + clusterName + ".FileAdaptor");
                int nodes = gridprops.getIntProperty("ibis.deploy." + gridName
                        + "." + clusterName + ".nodes");
                int multicore = gridprops.getIntProperty("ibis.deploy."
                        + gridName + "." + clusterName + ".multicore");
                String javaPath = gridprops.getProperty("ibis.deploy."
                        + gridName + "." + clusterName + ".javapath");
                grids[i].addCluster(new Cluster(clusterName, headnode,
                        resourceBrokersAdaptors, fileAdaptors, nodes,
                        multicore, javaPath));
            }
            i++;
        }
        System.err.println("loading grid: " + filename + " DONE");
        return grids;
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
