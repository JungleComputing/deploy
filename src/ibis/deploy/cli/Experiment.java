package ibis.deploy.cli;

import ibis.deploy.Util;
import ibis.util.TypedProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * An experiment. Each experiment consists of multiple jobs, running in a single pool
 * 
 * @author ndrost
 * 
 */
public class Experiment {

    // name of the experiment (and pool)
    private String name;
    
    private List<File> gridFiles;
    
    private List<File> applicationFiles; 

    // job representing defaults
    private Job defaults;

    // jobs in this experiment
    private List<Job> jobs;

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
     *             if the properties don't contain a 'name' property with the
     *             name of the experiment
     */
    public Experiment(File file) throws FileNotFoundException, IOException,
            Exception {
        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + file
                    + "\" does not exist");
        }

        TypedProperties properties = new TypedProperties();
        properties.loadFromFile(file.getAbsolutePath());

        name = properties.getProperty("name");
        
        gridFiles = Util.getFileListProperty(properties, "grid.files");
        applicationFiles = Util.getFileListProperty(properties, "application.files");
        
        if (name == null || name.length() == 0) {
            throw new Exception(
                    "no experiment name specified in experiment file: " + file);
        }

        defaults = new Job(properties, null, null);

        jobs = new ArrayList<Job>();
        String[] jobNames = properties.getStringList("jobs");
        if (jobNames != null) {
            for (String jobName : jobNames) {
                Job job = new Job(properties, jobName, this);
                jobs.add(job);
            }
        }
    }

    /**
     * Constructs a experiment with the given name.
     * 
     * @param name
     *            the name of the experiment
     * @throws Exception
     *             if the name is <code>null</code>
     */
    public Experiment(String name) throws Exception {
        if (name == null) {
            throw new Exception("no name specified for experiment");
        }

        this.name = name;
        this.jobs = new ArrayList<Job>();
        defaults = new Job("defaults", null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the Jobs in this Experiment.
     * 
     * @return the jobs in this Experiment
     */
    public Job[] getJobs() {
        return jobs.toArray(new Job[0]);
    }

    /**
     * Removes the given job from the experiment (if it belongs to the
     * experiment at all).
     * 
     * @param job
     *            the job to be removed from this experiment
     */
    public void removeJob(Job job) {
        jobs.remove(job);
    }

    /**
     * Creates a new job in this experiment, with a given name.
     * 
     * @param name
     *            the name of the job
     * @throws Exception
     *             if the name given is <code>null</code>
     */
    public Job createNewJob(String name) throws Exception {
        Job result = new Job(name, this);

        jobs.add(result);

        return result;
    }

    /**
     * Get an job with a given name from this Experiment
     * 
     * @param jobName
     *            the name of the job to search for
     * @return the job with the given name, or <code>null</code> if no jobs
     *         with the given name exist in this Experiment.
     */
    public Job getJob(String jobName) {
        for (Job job : jobs) {
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
    public Job getDefaults() {
        return defaults;
    }
    
    
    public File[] getGridFiles() {
        return gridFiles.toArray(new File[0]);
    }

    public void setGridFiles(File[] gridFiles) {
        if (gridFiles == null) {
            this.gridFiles = null;
        } else {
            this.gridFiles = Arrays.asList(gridFiles.clone());
        }
    }

    public void addGridFile(File gridFile) {
        if (gridFiles == null) {
            gridFiles = new ArrayList<File>();
        }
        gridFiles.add(gridFile);
    }

    public File[] getApplicationFiles() {
        return applicationFiles.toArray(new File[0]);
    }

    public void setApplicationFiles(File[] applicationFiles) {
        if (applicationFiles == null) {
            this.applicationFiles = null;
        } else {
            this.applicationFiles = Arrays.asList(applicationFiles.clone());
        }
    }

    public void addApplicationFile(File applicationFile) {
        if (applicationFiles == null) {
            applicationFiles = new ArrayList<File>();
        }
        applicationFiles.add(applicationFile);
    }

    
    /**
     * Save this experiment to the given file
     * 
     * @param file file to save experiment to
     * @throws Exception in case file cannot be written
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
        out.println("# Experiment properties file, "
                + "generated by Ibis Deploy on "
                + new Date(System.currentTimeMillis()));
        out.println();
        out.println("# Experiment (and pool) name:");
        out.println("name = " + getName());

        out.println();
        out.println("# Files with grid descriptions:");
        out.println("grid.files = " + Util.files2CSS(getGridFiles()));

        out.println();
        out.println("# Files with application descriptions:");
        out.println("application.files = " + Util.files2CSS(getApplicationFiles()));

        out.println();
        out.println("# Default settings:");
        defaults.print(out, false);

        // write names of jobs
        out.println();
        out.println("# Comma separated list of jobs in this experiment:");

        if (jobs.size() > 0) {
            out.print("jobs = ");
            for (int i = 0; i < jobs.size() - 1; i++) {
                out.print(jobs.get(i).getName() + ",");
            }
            out.println(jobs.get(jobs.size() - 1).getName());
        } else {
            out.println("jobs = ");
        }

        // write jobs
        for (Job job : jobs) {
            out.println();
            out.println("# Details of job \"" + job.getName() + "\"");
            job.print(out, true);

        }
        out.flush();
        out.close();
    }

    public String toString() {
        return name;
    }

    /**
     * Returns an info string suitable for printing (with newlines)
     * 
     * @return an info string suitable for printing (with newlines)
     */
    public String toPrintString() {
        String result = "Experiment \"" + getName() + "\" containing "
                + jobs.size() + " jobs\n";

        for (Job job : jobs) {
            result += job.toPrintString() + "\n";
        }

        return result;
    }

}
