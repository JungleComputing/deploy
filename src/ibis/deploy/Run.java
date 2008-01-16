package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class Run {

    private static Logger logger = Logger.getLogger(Run.class);

    private Grid[] grids;

    private Application[] apps;

    private ArrayList<Job> jobs = new ArrayList<Job>();

    private String runFileName;

    public static Run loadRun(String filename) throws FileNotFoundException,
            IOException {
        logger.info("loading run: " + filename + " ...");
        Run run = new Run();
        run.runFileName = filename;

        TypedProperties runprops = new TypedProperties();
        runprops.load(new FileInputStream(filename));

        String[] gridFiles = runprops.getStringList("grid.files");
        if (gridFiles == null || gridFiles.equals("")) {
            logger.warn("Property grid.files in " + filename + " not set!");
            System.exit(1);
        }

        run.grids = new Grid[gridFiles.length];
        for (int i = 0; i < gridFiles.length; i++) {
            run.grids[i] = Grid.loadGrid(gridFiles[i]);
        }

        String[] appFiles = runprops.getStringList("application.files");
        if (appFiles == null || appFiles.equals("")) {
            logger.warn("Property application.files in " + filename
                    + " not set!");
            System.exit(1);
        }

        HashSet<Application> appSet = new HashSet<Application>();
        for (int i = 0; i < appFiles.length; i++) {
            appSet.addAll(Application.loadApplications(appFiles[i]));
        }
        run.apps = appSet.toArray(new Application[appSet.size()]);

        String[] jobNames = runprops.getStringList("jobs");
        for (String jobName : jobNames) {

            Job job = new Job(jobName);

            String[] subjobs = runprops.getStringList(jobName + ".subjobs");
            for (String subjob : subjobs) {
                String grid = runprops.getProperty(jobName + "." + subjob
                        + ".grid");
                if (grid == null || grid.equals("")) {
                    grid = runprops.getProperty(jobName + ".grid");
                }
                if (grid == null || grid.equals("")) {
                    grid = runprops.getProperty("grid");
                }

                String cluster = runprops.getProperty(jobName + "." + subjob
                        + ".cluster");
                if (cluster == null || cluster.equals("")) {
                    cluster = runprops.getProperty(jobName + ".cluster");
                }
                if (cluster == null || cluster.equals("")) {
                    cluster = runprops.getProperty("cluster");
                }

                int nodes;
                try {
                    nodes = runprops.getIntProperty(jobName + "." + subjob
                            + ".nodes");
                } catch (NumberFormatException e) {
                    try {
                        nodes = runprops.getIntProperty(jobName + ".nodes");
                    } catch (NumberFormatException e1) {
                        try {
                            nodes = runprops.getIntProperty("nodes");
                        } catch (NumberFormatException e2) {
                            nodes = -1;
                        }
                    }
                }

                int multicore;
                try {
                    multicore = runprops.getIntProperty(jobName + "." + subjob
                            + ".multicore");
                } catch (NumberFormatException e) {
                    try {
                        multicore = runprops.getIntProperty(jobName
                                + ".multicore");
                    } catch (NumberFormatException e1) {
                        try {
                            multicore = runprops.getIntProperty("multicore");
                        } catch (NumberFormatException e2) {
                            multicore = -1;
                        }
                    }
                }

                int cores;
                try {
                    cores = runprops.getIntProperty(jobName + "." + subjob
                            + ".cores");
                } catch (NumberFormatException e) {
                    try {
                        cores = runprops.getIntProperty(jobName + ".cores");
                    } catch (NumberFormatException e1) {
                        try {
                            cores = runprops.getIntProperty("cores");
                        } catch (NumberFormatException e2) {
                            cores = -1;
                        }
                    }
                }

                String application = runprops.getProperty(jobName + "."
                        + subjob + ".application");
                if (application == null || application.equals("")) {
                    application = runprops.getProperty(jobName + ".application");
                    if (application == null || application.equals("")) {
                        application = runprops.getProperty("application");
                    }
                }
                Application app = run.getApplication(application);

                if (app == null) {
                    logger.warn("Application not found! (" + application + ")");
                    System.exit(1);
                }
                
                String main = runprops.getProperty(application + ".main");
                if (main == null || main.equals("")) {
                    main = runprops.getProperty("main");
                }
                if (!(main == null || main.equals(""))) {
                    app.setMain(main);
                }

                String[] javaFlags = runprops.getStringList(application + ".flags",
                        " ");
                if (javaFlags == null) {
                    javaFlags = runprops.getStringList("flags", " ");
                }
                if (javaFlags != null && javaFlags.length > 0) {
                    app.setJavaFlags(javaFlags);
                }

                String[] parameters = runprops.getStringList(application
                        + ".parameters", " ");
                if (parameters == null) {
                    parameters = runprops.getStringList("parameters", " ");
                }
                if (parameters != null && parameters.length > 0) {
                    app.setParameters(parameters);
                }

                String[] preStaged = runprops.getStringList(application
                        + ".prestage");
                if (preStaged == null) {
                    preStaged = runprops.getStringList("prestage");
                }
                if (preStaged != null && preStaged.length > 0) {
                    app.setPreStaged(preStaged);
                }

                String[] postStaged = runprops.getStringList(application
                        + ".poststage");
                if (postStaged == null) {
                    postStaged = runprops.getStringList("poststage");
                }
                if (postStaged != null && postStaged.length > 0) {
                    app.setPostStaged(postStaged);
                }

                String classpath = runprops.getProperty(application + ".classpath");
                if (classpath == null || classpath.equals("")) {
                    classpath = runprops.getProperty("classpath");
                }
                if (!(classpath == null || classpath.equals(""))) {
                    app.setClasspath(classpath);
                }

                job.addSubJob(new SubJob(subjob, grid, cluster, nodes,
                        multicore, app));
            }
            run.jobs.add(job);
        }
        logger.info("loading run: " + filename + " DONE");
        return run;
    }

    private Application getApplication(String name) {
        for (Application app : apps) {
            if (app.getName().equals(name)) {
                return (Application) app.clone();
            }
        }
        return null;
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
        return jobs;
    }

    public String toString() {
        String res = "Run: " + "app" + "\n";
        for (Grid grid : grids) {
            res += grid + "\n";
        }

        res += "requests:\n";
        for (int i = 0; i < jobs.size(); i++) {
            Job r = jobs.get(i);
            res += r;
        }

        return res;
    }

    public String getRunFileName() {
        return runFileName;
    }

}
