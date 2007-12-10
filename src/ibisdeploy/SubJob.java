package ibisdeploy;

public class SubJob {
    private String clusterName, gridName;

    private int machineCount = 0;

    private int CPUsPerMachine = 0;

    private int subJobNr = 0;

    public SubJob(String gridName, String clusterName, int machineCount,
            int cpusPerMachine, int subJobNr) {
        this.gridName = gridName;
        this.clusterName = clusterName;
        this.machineCount = machineCount;
        CPUsPerMachine = cpusPerMachine;
        this.subJobNr = subJobNr;
    }

    public int getSubJobNr() {
        return subJobNr;
    }

    public String getClusterName() {
        return clusterName;
    }

    public int getCPUsPerMachine() {
        return CPUsPerMachine;
    }

    public int getMachineCount() {
        return machineCount;
    }

    public String toString() {
        return "SubJob " + subJobNr + ": " + clusterName + " " + machineCount
                + " machines, with " + CPUsPerMachine
                + " CPUs/machine, for a total of "
                + (machineCount * CPUsPerMachine) + " CPUs";
    }

    public String getGridName() {
        return gridName;
    }
}
