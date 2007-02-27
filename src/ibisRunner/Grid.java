/*
 * Created on Mar 6, 2006
 */
package ibisRunner;

import java.util.ArrayList;

public class Grid {
    private ArrayList<Cluster> clusters = new ArrayList();

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
        Input in = new Input(filename);
        String gridName = in.readString();
        in.readln();
        Grid g = new Grid(gridName);

        while (!in.eof()) {
            // VU fs0.das2.cs.vu.nl ssh 64 2
            String friendly = in.readWord();
            String machine = in.readWord();
            String access = in.readWord();
            int machineCount = in.readInt();
            int CPUsPerMachine = in.readInt();
            in.readln();

            Cluster r = new Cluster(friendly, machine, access, machineCount, CPUsPerMachine);
            g.addCluster(r);
        }

        return g;
    }
    
    public String toString() {
        String res = "grid " + gridName + " resources:\n";
        for(int i=0; i<clusters.size(); i++) {
            res += "    " + clusters.get(i);            
        }
        return res;
    }
}
