package ibis.deploy.cli;

import ibis.deploy.ApplicationSet;
import ibis.deploy.Experiment;
import ibis.deploy.Grid;

import java.io.File;

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
        if (arguments.length == 0) {
            System.err
                    .println("Usage: scrub [-g GRID_FILE] [-a APPLICATIONS_FILE] [-e EXPERIMENT_FILE]");
            System.exit(0);
        }

        try {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].equals("-g")) {
                    i++;
                    File file = new File(arguments[i]);
                    System.err.println("Scrubbing GRID file \"" + file + "\"");
                    Grid grid = new Grid(file);
                    file.renameTo(new File(file.getPath() + ".backup"));
                    grid.save(file);
                } else if (arguments[i].equals("-a")) {
                    i++;
                    File file = new File(arguments[i]);
                    System.err.println("Scrubbing APPLICATION file \"" + file + "\"");
                    ApplicationSet applications = new ApplicationSet(file);
                    file.renameTo(new File(file.getPath() + ".backup"));
                    applications.save(file);
                } else if (arguments[i].equals("-e")) {
                    i++;
                    File file = new File(arguments[i]);
                    System.err.println("Scrubbing EXPERIMENT file \"" + file + "\"");
                    Experiment experiment = new Experiment(file);
                    file.renameTo(new File(file.getPath() + ".backup"));
                    experiment.save(file);
                } else {
                    System.err.println("unknown argument: " + arguments[i]);
                    System.err
                            .println("Usage: scrub [-g GRID_FILE] [-a APPLICATIONS_FILE] [-e EXPERIMENT_FILE]");
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception on scrubbing file");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
