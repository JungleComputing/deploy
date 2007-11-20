package ibisdeploy;

import java.util.ArrayList;

public class Job {
    private ArrayList<SubJob> subJobs = new ArrayList<SubJob>();

    private int jobNr;

    public Job(int jobNr) {
        this.jobNr = jobNr;
    }

    public void addSubJob(SubJob j) {
        subJobs.add(j);
    }

    public int getJobNr() {
        return jobNr;
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
            totalCPUs += subJob.getMachineCount() * subJob.getCPUsPerMachine();
        }

        return totalCPUs;
    }

    public String toString() {
        String res = "";
        int totalMachines = 0;
        int totalCPUs = 0;
        for (int j = 0; j < subJobs.size(); j++) {
            res += "Job " + jobNr + ": ";
            SubJob subJob = subJobs.get(j);
            res += subJob + "\n";
            totalMachines += subJob.getMachineCount();
            totalCPUs += subJob.getMachineCount() * subJob.getCPUsPerMachine();
        }

        res +=
                " total machines in run: " + totalMachines + " for a total of "
                        + totalCPUs + " CPUs\n";

        return res;
    }
}
