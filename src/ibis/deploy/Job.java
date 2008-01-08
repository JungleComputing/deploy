package ibis.deploy;

import java.util.ArrayList;
import java.util.HashSet;

public class Job {
    private ArrayList<SubJob> subJobs = new ArrayList<SubJob>();

    private String name;

    private Application app;

    public Job(String name, Application app) {
        this.name = name;
        this.app = app;
    }

    public Application getApplication() {
        return app;
    }

    public String getName() {
        return name;
    }

    public void addSubJob(SubJob j) {
        subJobs.add(j);
    }

    public int numberOfSubJobs() {
        return subJobs.size();
    }

    public SubJob get(int index) {
        return subJobs.get(index);
    }

    public int getTotalMachineCount() {
        int totalMachines = 0;
        for (int j = 0; j < subJobs.size(); j++) {
            SubJob subJob = subJobs.get(j);
            totalMachines += subJob.getMachineCount();
        }
        return totalMachines;
    }

    public int getTotalCPUCount() {
        int totalCPUs = 0;
        for (int j = 0; j < subJobs.size(); j++) {
            SubJob subJob = subJobs.get(j);
            totalCPUs += subJob.getMachineCount() * subJob.getCoresPerMachine();
        }

        return totalCPUs;
    }

    public String toString() {
        String res = "";
        int totalMachines = 0;
        int totalCPUs = 0;
        for (int j = 0; j < subJobs.size(); j++) {
            res += "Job " + name + ": ";
            SubJob subJob = subJobs.get(j);
            res += subJob + "\n";
            totalMachines += subJob.getMachineCount();
            totalCPUs += subJob.getMachineCount() * subJob.getCoresPerMachine();
        }
        res += " total machines in run: " + totalMachines + " for a total of "
                + totalCPUs + " CPUs\n";
        return res;
    }

    public Cluster[] getHubClusters(Grid[] grids) {
        HashSet<Cluster> hubClusters = new HashSet<Cluster>();
        for (int i = 0; i < numberOfSubJobs(); i++) {
            for (Grid grid: grids) {
                if (grid.getGridName().equalsIgnoreCase(get(i).getGridName())) {
                    for (Cluster cluster: grid.getClusters()) {
                        if (cluster.getFriendlyName().equals(get(i).getClusterName())) {
                            hubClusters.add(cluster);
                        }
                    }
                }
            }
        }
        return hubClusters.toArray(new Cluster[hubClusters.size()]);
    }
}
