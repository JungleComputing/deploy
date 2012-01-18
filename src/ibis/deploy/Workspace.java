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
 * Container object for a set of resources(jungle), applications, and experiment.
 * 
 * @author Niels Drost
 * 
 */
public class Workspace {

    public static final File DEFAULT_LOCATION = new File("deploy-workspace");

    public static final String JUNGLE_FILENAME = "deploy.jungle";

    public static final String APPLICATION_FILENAME = "deploy.applications";

    private static final Logger logger = LoggerFactory
            .getLogger(Workspace.class);

    private Jungle jungle;

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
     * Constructs a workspace from the given jungle, application, and experiments
     * 
     * @param jungle
     *            the jungle of the new workspace
     * @param applications
     *            the Applications of the workspace
     * @param experiments
     *            the experiments of the new workspace
     */
    public Workspace(Jungle jungle, ApplicationSet applications,
            Experiment... experiments) {
        this.jungle = jungle;
        this.applications = applications;
        this.experiments = new ArrayList<Experiment>();
        this.experiments.addAll(Arrays.asList(experiments));
    }

    /**
     * Constructs a workspace from all properties files stored in the given
     * directory. Constructs the jungle, applications and experiments within the
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
            jungle = new Jungle();
            applications = new ApplicationSet();
            return;
        }

        if (!location.isDirectory()) {
            throw new FileNotFoundException("location \"" + location
                    + "\" is not a directory");
        }

        File jungleFile = new File(location, JUNGLE_FILENAME);
        if (jungleFile.isFile()) {
            jungle = new Jungle(jungleFile);
        } else {
            logger.warn("Workspace does not contain jungle resource description file: " + jungleFile);
            jungle = new Jungle();
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
     *            directory to save workspace to
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

        File jungleFile = new File(location, JUNGLE_FILENAME);
        if (jungleFile.exists()) {
            jungleFile.renameTo(new File(location, JUNGLE_FILENAME + ".old"));
        }
        jungle.save(jungleFile);

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
     * @return the jungle
     */
    public Jungle getJungle() {
        return jungle;
    }

    /**
     * @param jungle
     *            the jungle to set
     */
    public void setJungle(Jungle jungle) {
        this.jungle = jungle;
    }

    public String toPrintString() {
        String result = "Workspace: \n\n";
        if (jungle != null) {
            result += jungle.toPrintString();
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
