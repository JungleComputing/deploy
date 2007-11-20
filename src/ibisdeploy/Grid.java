/*
 * Created on Mar 6, 2006
 */
package ibisdeploy;

import java.util.ArrayList;

public class Grid {
    private ArrayList<Cluster> clusters = new ArrayList<Cluster>();

    private String gridName;

    public Grid(String gridName) {
        this.gridName = gridName;
    }

    public void addCluster(Cluster a) {
        clusters.add(a);
    }

    public ArrayList<Cluster> getClusters() {
        return clusters;
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

    public static Grid loadGrid(String filename) {
        System.err.print("loading grid: " + filename);
        Input in = new Input(filename);
        String gridName = in.readString();
        in.readln();
        Grid g = new Grid(gridName);
        while (!in.eof()) {
            // VU fs0.das2.cs.vu.nl ssh 64 2
            String friendly = in.readWord();
            String machine = in.readWord();
            String broker = in.readWord();
            String files = in.readWord();
            int machineCount = in.readInt();
            int CPUsPerMachine = in.readInt();
            String javaHome = in.readWord();
            in.readln();

            Cluster r =
                    new Cluster(friendly, machine, broker, files, machineCount,
                            CPUsPerMachine, javaHome);
            g.addCluster(r);
        }

        System.err.println(" DONE");
        return g;
    }

    public String toString() {
        String res = "grid " + gridName + " resources:\n";
        for (int i = 0; i < clusters.size(); i++) {
            res += "    " + clusters.get(i) + "\n";
        }

        res +=
                "total machine count: " + getTotalMachineCount()
                        + " total CPU count: " + getTotalCPUCount();
        return res;
    }
}
