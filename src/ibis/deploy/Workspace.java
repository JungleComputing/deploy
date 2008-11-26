package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Container object for a grid, application and experiment.
 * 
 * @author Niels Drost
 * 
 */
public class Workspace {

    private Grid grid;

    private ApplicationSet applications;

    private Experiment experiment;

    /**
     * Constructs a workspace
     * 
     * @param experimentName
     *                the name of the experiment in the workspace.
     * @throws NullPointerException
     *                 if the experiment name is <code>null</code>
     */
    public Workspace(String experimentName) {
        grid = new Grid();
        applications = new ApplicationSet();
        experiment = new Experiment(experimentName);
    }

    /**
     * Constructs a workspace object from properties stored in the given file.
     * Also constructs the grid, applications and experiment within the
     * workspace.
     * 
     * @param file
     *                the file containing the properties
     * @throws FileNotFoundException
     *                 if the given file cannot be found
     * @throws Exception
     * @throws Exception
     *                 if reading from the given file fails, or the file
     *                 contains invalid properties
     */
    public Workspace(File file) throws Exception {
        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + file
                    + "\" does not exist");
        }

        String fileName = file.getName();

        if (!fileName.endsWith(".workspace")) {
            throw new Exception(
                    "workspace files must have a \".workspace\" extension");
        }

        // cut of ".workspace", use the rest as experiment name
        String name = fileName.substring(0, fileName.length()
                - ".workspace".length());

        TypedProperties properties = new TypedProperties();

        properties.loadFromFile(file.getAbsolutePath());

        grid = new Grid(properties, "grid");
        applications = new ApplicationSet(properties, "applications");
        experiment = new Experiment(properties, name, "experiment");
    }

    /**
     * Save this grid and all contained clusters to a property file
     * 
     * @param file
     *                file to save grid to
     * 
     * @throws Exception
     *                 in case file cannot be written
     */
    public void save(File file) throws Exception {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("failed to create a new file '" + file
                        + "'.");
            }
        }
        PrintWriter out = new PrintWriter(file);
        // write defaults
        out.println("# Workspace file, " + "generated by Ibis Deploy on "
                + new Date(System.currentTimeMillis()));

        out.println();
        out.println("################ Grid settings ###############");
        out.println();
        grid.save(out, "grid");

        out.println();
        out.println("################ Application settings ###############");
        out.println();
        applications.save(out, "applications");

        out.println();
        out.println("################ Experiment settings ###############");
        out.println();
        experiment.save(out, "experiment");

        out.flush();
        out.close();
    }

    /**
     * @return the applications
     */
    public ApplicationSet getApplications() {
        return applications;
    }

    /**
     * @param applications
     *                the applications to set
     */
    public void setApplications(ApplicationSet applications) {
        this.applications = applications;
    }

    /**
     * @return the experiment
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * @param experiment
     *                the experiment to set
     */
    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    /**
     * @return the grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * @param grid
     *                the grid to set
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

}
