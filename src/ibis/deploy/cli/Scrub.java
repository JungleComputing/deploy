package ibis.deploy.cli;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;
import ibis.deploy.Workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Cleans up property files by reading, then writing them. Also creates a
 * backup, just in case.
 * 
 * @author Niels Drost
 * 
 */
public class Scrub {

    /**
     * @param arguments
     *            arguments of application
     */
    public static void main(String[] arguments) {
        boolean verbose = false;
        List<File> files = new ArrayList<File>();

        if (arguments.length == 0) {
            System.err
                    .println("Usage: scrub [GRID_FILE] [APPLICATIONS_FILE] [EXPERIMENT_FILE]");
            System.exit(0);
        }

        try {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].endsWith(".grid")
                        || arguments[i].endsWith(".applications")
                        || arguments[i].endsWith(".experiment")
                        || arguments[i].endsWith(".workspace")) {
                    File file = new File(arguments[i]);
                    files.add(file);
                    
                    if (!file.exists()) {
                        System.err.println("File " + file + " does not exist");
                        System.exit(1);
                    }
                    
                    if (!file.isFile()) {
                        System.err.println("Path " + file + " is not a file");
                        System.exit(1);
                    }
                } else if (arguments[i].equals("-v")) {
                    verbose = true;
                } else {
                    System.err.println("unknown file type: " + arguments[i]);
                    System.err
                            .println("Usage: scrub [OPTIONS] [GRID_FILE] [APPLICATIONS_FILE] [EXPERIMENT_FILE]");
                    System.err.println();
                    System.err.println("OPTIONS:");
                    System.err.println("-v\tVerbose mode");
                    System.exit(1);
                }
            }

            for (File file : files) {
                if (file.getName().endsWith(".grid")) {
                    System.err.println("Scrubbing GRID file \"" + file + "\"");
                    Grid grid = new Grid(file);
                    if (verbose) {
                        System.out.println(grid.toPrintString());
                    }
                    file.renameTo(new File(file.getPath() + ".backup"));
                    grid.save(file);
                } else if (file.getName().endsWith(".applications")) {
                    System.err.println("Scrubbing APPLICATION file \"" + file
                            + "\"");
                    ApplicationSet applications = new ApplicationSet(file);
                    if (verbose) {
                        System.out.println(applications.toPrintString());
                    }
                    file.renameTo(new File(file.getPath() + ".backup"));
                    applications.save(file);
                } else if (file.getName().endsWith(".experiment")) {
                    System.err.println("Scrubbing EXPERIMENT file \"" + file
                            + "\"");
                    Experiment experiment = new Experiment(file);
                    if (verbose) {
                        System.out.println(experiment.toPrintString());
                    }
                    file.renameTo(new File(file.getPath() + ".backup"));
                    experiment.save(file);
                } else if (file.getName().endsWith(".workspace")) {
                    System.err.println("Scrubbing WORKSPACE file \"" + file
                            + "\"");
                    Workspace workspace = new Workspace(file);
                    if (verbose) {
                        System.out.println(workspace.toPrintString());
                    }
                    file.renameTo(new File(file.getPath() + ".backup"));
                    workspace.save(file);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception on scrubbing file");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
