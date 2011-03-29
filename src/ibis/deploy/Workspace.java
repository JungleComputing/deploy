package ibis.deploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container object for a grid, application and experiment.
 * 
 * @author Niels Drost
 * 
 */
public class Workspace {

    public static final File DEFAULT_LOCATION = new File("deploy-workspace");

    public static final String GRID_FILENAME = "deploy.grid";

    public static final String APPLICATION_FILENAME = "deploy.applications";

    private static final Logger logger = LoggerFactory
            .getLogger(Workspace.class);

    private Grid grid;

    private ApplicationSet applications;

    private final ArrayList<Experiment> experiments;

    /**
     * Constructs an empty workspace.
     * 
     * @throws Exception
     *             if creating the workspace failed
     */
    public Workspace() throws Exception {
        this(null);
    }
    
    /**
     * Constructs a workspace from the given grid, application, and experiments
     * 
     * @param grid the grid of the new workspace
     * @param applications the Applications of the workspace
     * @param experiments the experiments of the new workspace
     */
    public Workspace(Grid grid, ApplicationSet applications, Experiment... experiments) {
       this.grid = grid;
       this.applications = applications;
       this.experiments = new ArrayList<Experiment>();
       this.experiments.addAll(Arrays.asList(experiments));
    }
    
    

    /**
     * Constructs a workspace from all properties files stored in the given
     * directory. Constructs the grid, applications and experiments within the
     * workspace.
     * 
     * @param location
     *            the directory of the workspace
     * @throws FileNotFoundException
     *             if the given location is not a directory (usually a file)
     * @throws Exception
     * @throws Exception
     *             if reading from the files in the directory fails, or any of
     *             the files contain invalid properties.
     */
    public Workspace(File location) throws Exception {
        experiments = new ArrayList<Experiment>();

        if (location == null || !location.exists()) {
            // do not try to load any further.
            grid = new Grid();
            applications = new ApplicationSet();
            return;
        }

        if (!location.isDirectory()) {
            throw new FileNotFoundException("location \"" + location
                    + "\" is not a directory");
        }

        File gridFile = new File(location, GRID_FILENAME);
        if (gridFile.isFile()) {
            grid = new Grid(gridFile);
        } else {
            logger.warn("Workspace does not contain grid file: " + gridFile);
            grid = new Grid();
        }

        File applicationFile = new File(location, APPLICATION_FILENAME);
        if (applicationFile.isFile()) {
            applications = new ApplicationSet(applicationFile);
        } else {
            logger.warn("Workspace does not contain application file: "
                    + applicationFile);
            applications = new ApplicationSet();
        }

        // load all experiments
        for (File file : location.listFiles()) {
            if (file.getName().endsWith(".experiment")) {
                experiments.add(new Experiment(file));
            }
        }
        if (experiments.size() == 0) {
            logger.warn("No experiments found in workspace: " + location);
        }
    }

    /**
     * Save this workspace to a directory.
     * 
     * @param location
     *            directory to save grid to
     * 
     * @throws Exception
     *             in case file cannot be written
     */
    public void save(File location) throws Exception {
        if (location.isFile()) {
            throw new IOException("cannot save workspace to " + location
                    + " as this is a file, not a directory");
        }

        location.mkdirs();

        if (!location.isDirectory()) {
            throw new IOException("cannot save workspace to " + location
                    + ", failed to create directory");
        }

        File gridFile = new File(location, GRID_FILENAME);
        if (gridFile.exists()) {
            gridFile.renameTo(new File(location, GRID_FILENAME + ".old"));
        }
        grid.save(gridFile);

        File applicationFile = new File(location, APPLICATION_FILENAME);
        if (applicationFile.exists()) {
            applicationFile.renameTo(new File(location, APPLICATION_FILENAME
                    + ".old"));
        }
        applications.save(applicationFile);

        if (!location.isDirectory()) {
            throw new IOException("failed to create workspace directory: "
                    + location);
        }

        // move all experiment files to ".old" files
        for (File file : location.listFiles()) {
            if (file.getName().endsWith(".experiment") && file.exists()) {
                file.renameTo(new File(file.getAbsolutePath() + ".old"));
            }
        }

        // save experiments
        for (Experiment experiment : experiments) {
            experiment.save(new File(location, experiment.getName()
                    + ".experiment"));
        }
    }

    /**
     * @return the applications
     */
    public ApplicationSet getApplications() {
        return applications;
    }

    /**
     * @param applications
     *            the applications to set
     */
    public void setApplications(ApplicationSet applications) {
        this.applications = applications;
    }

    /**
     * @return the experiments. The returned list is not a copy! Changes to the
     *         returned list will result in changes to the workspace.
     */
    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(Experiment[] experiments) {
        this.experiments.clear();
        this.experiments.addAll(Arrays.asList(experiments));
    }

    /**
     * @param experiment
     *            the experiment to set
     */
    public void addExperiment(Experiment experiment) {
        experiments.add(experiment);
    }

    /**
     * @return the grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * @param grid
     *            the grid to set
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public String toPrintString() {
        String result = "Workspace: \n\n";
        if (grid != null) {
            result += grid.toPrintString();
            result += "\n";
        }
        if (applications != null) {
            result += applications.toPrintString();
            result += "\n";
        }

        for (Experiment experiment : experiments) {
            result += experiment.toPrintString();
            result += "\n";
        }

        return result;
    }

}
