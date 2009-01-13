package ibis.deploy;

import ibis.util.TypedProperties;

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

    // job representing defaults
    private JobDescription defaults;

    // jobs in this experiment
    private List<JobDescription> jobs;

    /**
     * Constructs a experiment object from properties stored in the given file.
     * Also constructs the jobs inside this experiments.
     * 
     * @param file
     *                the file containing the properties
     * @throws FileNotFoundException
     *                 if the given file cannot be found
     * @throws IOException
     *                 if reading from the given file fails
     * @throws Exception
     *                 if the properties don't contain a 'name' property with
     *                 the name of the experiment
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

        TypedProperties properties = new TypedProperties();
        properties.loadFromFile(file.getAbsolutePath());

        defaults = new JobDescription(properties, "default", "default", this);

        String[] jobNames = Util.getElementList(properties);
        for (String jobName : jobNames) {
            if (!jobName.equals("name")) {
                JobDescription job = new JobDescription(properties, jobName,
                        jobName, this);
                jobs.add(job);
            }
        }
    }

    /**
     * Constructs an experiment object from the given properties. Also
     * constructs the jobs inside this experiment.
     * 
     * @param properties
     *                properties of the experiment
     * @param name
     *                name of the experiment
     * @param prefix
     *                prefix to use on all keys
     * @throws Exception
     *                 if job cannot be read properly
     * 
     */
    public Experiment(TypedProperties properties, String name, String prefix)
            throws Exception {
        jobs = new ArrayList<JobDescription>();

        this.name = name;

        if (prefix != null) {
            prefix = prefix + ".";
        }

        defaults = new JobDescription(properties, "default",
                prefix + "default", this);

        String[] jobNames = Util.getElementList(properties, prefix);
        for (String jobName : jobNames) {
            JobDescription job = new JobDescription(properties, jobName, prefix
                    + jobName, this);
            jobs.add(job);
        }

    }

    /**
     * Constructs a experiment with the given name.
     * 
     * @param name
     *                the name of the experiment
     * @throws Exception
     *                 if the name is <code>null</code>, or contains periods
     *                 or spaces
     */
    public Experiment(String name) throws Exception {
        jobs = new ArrayList<JobDescription>();

        if (name == null) {
            throw new NullPointerException("no name specified for experiment");
        }

        this.name = name;
        defaults = new JobDescription("defaults", null);
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
     *                the new name of this experiment.
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
     *                the job to be removed from this experiment
     */
    public void removeJob(JobDescription job) {
        if (!jobs.contains(job)) {
            for (JobDescription jd : jobs) {
                System.out.println(jd + " != " + job);
            }
        }
        jobs.remove(job);
    }

    /**
     * Creates a new job in this experiment, with a given name.
     * 
     * @param name
     *                the name of the job.
     * @return the new job.
     * @throws Exception
     *                 if the name given is <code>null</code>, or the job
     *                 already exists.
     */
    public JobDescription createNewJob(String name) throws Exception {
        if (hasJob(name)) {
            throw new AlreadyExistsException("Cannot add job, job \"" + name
                    + "\" already exists");
        }

        JobDescription result = new JobDescription(name, this);

        jobs.add(result);

        return result;
    }

    /**
     * Returns if a Job with the given name exists.
     * 
     * @param name
     *                name of the Job.
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
     *                the name of the job to search for
     * @return the job with the given name, or <code>null</code> if no jobs
     *         with the given name exist in this Experiment.
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
     * Returns job representing defaults of this experiment.
     * 
     * @return job representing defaults of this experiment.
     */
    public JobDescription getDefaults() {
        return defaults;
    }

    /**
     * Save this experiment to the given file
     * 
     * @param file
     *                file to save experiment to
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
     *                stream to write experiment to
     * @param prefix
     *                prefix for all keys written
     * @throws Exception
     *                 in case data cannot be written
     */
    public void save(PrintWriter out, String prefix) throws Exception {
        if (prefix != null) {
            prefix = prefix + ".";
        } else {
            prefix = "";
        }

        JobDescription.printTableOfKeys(out);
        out.println();
        out.println("# Default settings:");
        defaults.save(out, prefix + "default", false);

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
