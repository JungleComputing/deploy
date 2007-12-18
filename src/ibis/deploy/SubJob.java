package ibis.deploy;

public class SubJob {
    private String name, clusterName, gridName;

    private int nodes = 0, coresPerMachine = 0;

    public SubJob(String name, String grid, String cluster, int nodes,
            int multicore) {
        this.name = name;
        this.gridName = grid;
        this.clusterName = cluster;
        this.nodes = nodes;
        this.coresPerMachine = multicore;
    }

    public String getClusterName() {
        return clusterName;
    }

    public int getCoresPerMachine() {
        return coresPerMachine;
    }

    public int getMachineCount() {
        return nodes;
    }

    public String toString() {
        return "SubJob " + name + ": " + gridName + " " + clusterName + " "
                + nodes + " machines, with " + coresPerMachine
                + " cores/machine, for a total of " + (nodes * coresPerMachine)
                + " cores";
    }

    public String getGridName() {
        return gridName;
    }

    public String getName() {
        return name;
    }
}
