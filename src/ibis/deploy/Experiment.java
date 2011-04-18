package ibis.deploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An experiment. Each experiment consists of multiple jobs, usually running in
 * a single pool.
 * 
 * @author Niels Drost
 * 
 */
public class Experiment {

    // name of the experiment (and default pool)
    private String name;

    // jobs in this experiment
    private List<JobDescription> jobs;

    /**
     * Constructs a experiment with the given name.
     * 
     * @param name
     *            the name of the experiment
     * @throws Exception
     *             if the name is <code>null</code>, or contains periods or
     *             spaces
     */
    public Experiment(String name) throws Exception {
        jobs = new ArrayList<JobDescription>();

        if (name == null) {
            throw new NullPointerException("no name specified for experiment");
        }

        this.name = name;
    }

    /**
     * Constructs a experiment object from properties stored in the given file.
     * Also constructs the jobs inside this experiments.
     * 
     * @param file
     *            the file containing the properties
     * @throws FileNotFoundException
     *             if the given file cannot be found
     * @throws IOException
     *             if reading from the given file fails
     * @throws Exception
     *             if the properties do not contain a valid experiment
     */
    public Experiment(File file) throws FileNotFoundException, IOException,
            Exception {
        jobs = new ArrayList<JobDescription>();

        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + file
                    + "\" does not exist");
        }

        String fileName = file.getName();

        if (!fileName.endsWith(".experiment")) {
            throw new Exception(
                    "experiment files must have a \".experiment\" extension");
        }

        // cut of ".experiment", use the rest as name
        name = fileName.substring(0, fileName.length() - 11);

        if (name.length() == 0) {
            throw new Exception(
                    "no experiment name specified in experiment file name: "
                            + file);
        }

        DeployProperties properties = new DeployProperties();

        properties.loadFromFile(file.getAbsolutePath());

        String[] jobNames = properties.getElementList("");

        for (String jobName : jobNames) {
            JobDescription job = new JobDescription(jobName);

            job.setPoolName(getName());

            job.loadFromProperties(properties, "default");

            job.loadFromProperties(properties, jobName);

            addJob(job);
        }

    }

    /**
     * Returns the name of this experiment
     * 
     * @return the name of this experiment.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this experiment
     * 
     * @param name
     *            the new name of this experiment.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the Jobs in this Experiment.
     * 
     * @return the jobs in this Experiment
     */
    public JobDescription[] getJobs() {
        return jobs.toArray(new JobDescription[0]);
    }

    /**
     * Removes the given job from the experiment (if it belongs to the
     * experiment at all).
     * 
     * @param job
     *            the job to be removed from this experiment
     */
    public void removeJob(JobDescription job) {
        JobDescription toBeRemoved = null;
        for (JobDescription jd : jobs) {
            if (jd.getName().equals(job.getName())) {
                toBeRemoved = jd;
                break;
            }
        }
        if (toBeRemoved != null) {
            jobs.remove(toBeRemoved);
        } else {
            // TODO: complain?
        }
    }

    /**
     * Creates a new job in this experiment, with a given name.
     * 
     * @param job
     *            the job.
     * @throws Exception
     *             if the job already exists.
     */
    public void addJob(JobDescription job) throws Exception {
        if (hasJob(job.getName())) {
            throw new AlreadyExistsException("Cannot add job, job \"" + name
                    + "\" already exists");
        }

        jobs.add(job);
    }

    /**
     * Returns if a Job with the given name exists.
     * 
     * @param name
     *            name of the Job.
     * @return if a Job with the given name exists.
     */
    public boolean hasJob(String name) {
        for (JobDescription job : jobs) {
            if (job.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an job with a given name from this Experiment
     * 
     * @param jobName
     *            the name of the job to search for
     * @return the job with the given name, or <code>null</code> if no jobs with
     *         the given name exist in this Experiment.
     */
    public JobDescription getJob(String jobName) {
        for (JobDescription job : jobs) {
            if (job.getName().equals(jobName)) {
                return job;
            }
        }
        return null;
    }

    /**
     * Save this experiment to the given file
     * 
     * @param file
     *            file to save experiment to
     * @throws Exception
     *             in case file cannot be written
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
        out.println("# Experiment file, " + "generated by Ibis Deploy on "
                + new Date(System.currentTimeMillis()));
        out.println();

        save(out, null);

        out.flush();
        out.close();
    }

    /**
     * Save this experiment to the given stream
     * 
     * @param out
     *            stream to write experiment to
     * @param prefix
     *            prefix for all keys written
     * @throws Exception
     *             in case data cannot be written
     */
    public void save(PrintWriter out, String prefix) throws Exception {
        if (prefix != null) {
            prefix = prefix + ".";
        } else {
            prefix = "";
        }

        JobDescription.printTableOfKeys(out);
        out.println();

        // write jobs
        for (JobDescription job : jobs) {
            out.println();
            out.println("# Details of job \"" + job.getName() + "\"");
            job.save(out, prefix + job.getName(), true);
        }
    }

    /**
     * Returns an info string suitable for printing (with newlines)
     * 
     * @return an info string suitable for printing (with newlines)
     */
    public String toPrintString() {
        String result = "Experiment \"" + getName() + "\" containing "
                + jobs.size() + " jobs:\n\n";

        for (JobDescription job : jobs) {
            result += job.toPrintString() + "\n";
        }

        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
}
