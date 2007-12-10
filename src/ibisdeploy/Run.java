package ibisdeploy;

import ibis.smartsockets.util.TypedProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Run {
    private Grid[] grids;

    private HashSet<Cluster> hubClusters = new HashSet<Cluster>();

    private Application app;

    private ArrayList<Job> job = new ArrayList<Job>();

    private String runFileName;

    public static Run loadRun(String filename) throws FileNotFoundException,
            IOException {
        System.err.println("loading run: " + filename + " ...");
        Run run = new Run();
        run.runFileName = filename;
        TypedProperties runprops = new TypedProperties();
        runprops.load(new FileInputStream(filename));
        // runprops = runprops.filter("ibis.deploy");
        String gridFile = runprops.getProperty("ibis.deploy.grid.file");
        String appFile = runprops.getProperty("ibis.deploy.application.file");
        run.grids = Grid.loadGrid(gridFile);
        run.app = Application.loadApplication(appFile);

        int nrJobs = runprops.getIntProperty("ibis.deploy.jobs");
        for (int i = 1; i <= nrJobs; i++) {
            Job job = new Job(i);
            int nrSubJobs = runprops.getIntProperty("ibis.deploy.job." + i
                    + ".subjobs");
            for (int j = 1; j <= nrSubJobs; j++) {
                String grid = runprops.getProperty("ibis.deploy.job." + i
                        + ".subjob." + j + ".grid");
                String cluster = runprops.getProperty("ibis.deploy.job." + i
                        + ".subjob." + j + ".cluster");
                int hostCount = runprops.getIntProperty("ibis.deploy.job." + i
                        + ".subjob." + j + ".hostcount");
                int CPUsPerMachine = runprops.getIntProperty("ibis.deploy.job."
                        + i + ".subjob." + j + ".cpucount");
                run.hubClusters.add(run.getCluster(run.getGrid(grid), cluster));
                job.addSubJob(new SubJob(grid, cluster, hostCount,
                        CPUsPerMachine, j));
            }
            run.job.add(job);
        }
        System.err.println("loading run: " + filename + " DONE");
        return run;
    }

    public Application getApp() {
        return app;
    }

    public Grid getGrid(String gridName) {
        for (Grid grid : grids) {
            if (grid.getGridName().equals(gridName)) {
                return grid;
            }
        }
        return null;
    }

    public Cluster getCluster(Grid grid, String clusterName) {
        return grid.getCluster(clusterName);
    }

    public Grid[] getGrids() {
        return grids;
    }

    public ArrayList<Job> getRequestedResources() {
        return job;
    }

    public String toString() {
        String res = "Run: " + app + "\n";
        for (Grid grid: grids) {
            res += grid + "\n";
        }

        res += "requests:\n";
        for (int i = 0; i < job.size(); i++) {
            Job r = job.get(i);
            res += r;
        }

        return res;
    }

    public String getRunFileName() {
        return runFileName;
    }

    public Cluster[] getHubClusters() {
        return hubClusters.toArray(new Cluster[hubClusters.size()]);
    }
}
