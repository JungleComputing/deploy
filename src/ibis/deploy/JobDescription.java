package ibis.deploy;

import java.io.File;
import java.io.PrintWriter;

/**
 * Single job in an experiment.
 * 
 * @author Niels Drost
 * 
 */
public class JobDescription {

    /**
     * Print a table of valid keys and some explanations to the given stream
     * 
     * @param out
     *            stream used for printing
     */
    public static void printTableOfKeys(PrintWriter out) {
        out.println("# Valid parameters for jobs:");
        out.println("# KEY                 COMMENT");
        out.println("# application.name    Name of application to run");
        out.println("# application.*       All valid entries for an application, overriding any");
        out.println("#                     specified in the application referenced");
        out.println("# process.count       Total number of processes started");
        out.println("# resource.name       Name of resource to run application on");
        out.println("# resource.*          All valid entries for a resource, overriding any");
        out.println("#                     specified in the resource referenced");
        out.println("# resource.count      Number of machines used on the resource");

        out.println("# runtime             Maximum runtime of job (in minutes)");

        out.println("# pool.name           Pool name. Defaults to name of experiment if unspecified");
        out.println("# pool.size           Size of pool. Only used in a closed-world application");
        out.println("# stdout.file         File used to write stdout to");
        out.println("# stderr.file         File used to write stderr to");

    }

    // name of job
    private String name;

    private final Application application;

    private int processCount;

    private final Resource resource;

    private int resourceCount;

    private int runtime;

    private String poolName;

    private int poolSize;

    private File stdoutFile;

    private File stderrFile;

    /**
     * Create an empty job description
     */
    JobDescription() {
        name = "anonymous";
        application = new Application();
        processCount = 0;
        resource = new Resource();
        resourceCount = 0;
        runtime = 0;
        poolName = null;
        poolSize = 0;
        stdoutFile = null;
        stderrFile = null;
    }

    /**
     * Creates a new job with the given name.
     * 
     * @param name
     *            the name of the job
     * @throws Exception
     *             if name is unspecified, or contains periods or spaces
     */
    public JobDescription(String name) throws Exception {
        this();
        setName(name);
    }

    public void loadFromProperties(DeployProperties properties, String prefix) throws Exception {

        // add separator to prefix
        prefix = prefix + ".";

        String applicationName = properties.getProperty(prefix + "application.name");

        application.setName(applicationName);
        application.setFromProperties(properties, prefix + "application");

        processCount = properties.getIntProperty(prefix + "process.count", processCount);

        String resourceName = properties.getProperty(prefix + "resource.name");
        resource.setName(resourceName);
        resource.loadFromProperties(properties, prefix + "resource");

        resourceCount = properties.getIntProperty(prefix + "resource.count", resourceCount);

        runtime = properties.getIntProperty(prefix + "runtime", runtime);

        poolName = properties.getProperty(prefix + "pool.name", poolName);

        poolSize = properties.getIntProperty(prefix + "pool.size", poolSize);

        stdoutFile = properties.getFileProperty(prefix + "stdout.file");

        stderrFile = properties.getFileProperty(prefix + "stderr.file");
    }

    /**
     * Returns the name of this job.
     * 
     * @return the name of this job.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this job.
     * 
     * @param name
     *            the name of this job.
     * 
     * @throws Exception
     *             if name is null, or contains periods or spaces, or a job with
     *             the given name already exists.
     */
    public void setName(String name) throws Exception {
        if (name == null) {
            throw new Exception("no name specified for job");
        }

        if (name.equals(this.name)) {
            // name unchanged
            return;
        }

        if (name.contains(".")) {
            throw new Exception("job name cannot contain periods : \"" + name + "\"");
        }
        if (name.contains(" ")) {
            throw new Exception("job name cannot contain spaces : \"" + name + "\"");
        }

        this.name = name;
    }

    /**
     * Returns application object used for "overriding" application settings.
     * 
     * @return application object used for "overriding" application settings.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Total number of times application is started.
     * 
     * @return Total number of processes in this job. Returns 0 if unknown.
     */
    public int getProcessCount() {
        return processCount;
    }

    /**
     * Sets total number of times application is started.
     * 
     * @param processCount
     *            total number of processes in this job, 0 for unknown.
     */
    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }

    /**
     * Returns resource object used for "overriding" resource settings.
     * 
     * @return resource object used for "overriding" resource settings.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Total number of resources used for this job.
     * 
     * @return Total number of machines used on the specified resource. Returns
     *         0 if unknown
     */
    public int getResourceCount() {
        return resourceCount;
    }

    /**
     * Sets total number of resources used for this job.
     * 
     * @param resourceCount
     *            number of machines used on the specified resource, or 0 for
     *            unknown.
     */
    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    /**
     * Maximum runtime of this job in minutes.
     * 
     * @return maximum runtime of this job in minutes. Returns 0 if unset.
     */
    public int getRuntime() {
        return runtime;
    }

    /**
     * Sets the maximum runtime of this job in minutes.
     * 
     * @param runtime
     *            maximum runtime of this job in minutes., or 0 for not
     *            specified.
     */
    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    /**
     * Returns the name of the pool of this job. If null, the name of the
     * experiment is used by default.
     * 
     * @return the name of the pool of this job.
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * Sets the name of the pool of this job. If null, the name of the
     * experiment is used by default.
     * 
     * @param poolName
     *            the new name of the pool of this job.
     */
    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    /**
     * Returns the size of the pool of this job. Only used in closed-world
     * applications, defaults to 0 for "unknown"
     * 
     * @return the size of the pool of this job.
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Sets the size of the pool of this job. Only used in closed-world
     * applications, defaults to 0 for "unknown"
     * 
     * @param poolSize
     *            the new size of the pool of this job.
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    /**
     * Returns the file stdout is written to.
     * 
     * @return the file stdout is written to.
     */
    public File getStdoutFile() {
        return stdoutFile;
    }

    /**
     * Sets the file stdout is written to.
     * 
     * @param stdoutFile
     *            the new file used to write stdout to.
     */
    public void setStdoutFile(File stdoutFile) {
        this.stdoutFile = stdoutFile;
    }

    /**
     * Returns the file stderr is written to.
     * 
     * @return the file stderr is written to.
     */
    public File getStderrFile() {
        return stderrFile;
    }

    /**
     * Sets the file stderr is written to.
     * 
     * @param stderrFile
     *            the new file used to write stderr to.
     */
    public void setStderrFile(File stderrFile) {
        this.stderrFile = stderrFile;
    }

    /**
     * Checks if this description is suitable for deploying. If not, throws an
     * exception.
     * 
     * @throws Exception
     *             if this description is incomplete or incorrect.
     */
    public void checkSettings() throws Exception {
        String prefix = "Cannot run job \"" + name + "\": ";

        if (name == null) {
            throw new Exception("Cannot run job: Job name unspecified");
        }

        if (application == null) {
            throw new Exception(prefix + "Application overrides not specified");
        }

        application.checkSettings(name);

        if (processCount <= 0) {
            throw new Exception(prefix + "Process count zero or negative");
        }

        if (resource == null) {
            throw new Exception(prefix + "Resource overrides not specified");
        }

        resource.checkSettings(name, false);

        if (resourceCount < 0) {
            throw new Exception(prefix + "Resource count negative");
        }

        if (runtime < 0) {
            throw new Exception(prefix + "Runtime negative");
        }

        if (poolName == null) {
            throw new Exception(prefix + "Pool name not specified");
        }

        if (poolSize < 0) {
            throw new Exception(prefix + "Pool size negative");
        }
    }

    /**
     * Copy constructor
     * 
     * @param original
     *            original description to get data from
     */
    public JobDescription(JobDescription original) {
        this.name = original.name;
        this.processCount = original.processCount;
        this.resourceCount = original.resourceCount;
        this.runtime = original.runtime;
        this.poolName = original.poolName;
        this.poolSize = original.poolSize;
        this.stdoutFile = original.stdoutFile;
        this.stderrFile = original.stderrFile;

        this.application = new Application(original.application);
        this.resource = new Resource(original.resource);
    }

    public JobDescription resolve(ApplicationSet applicationSet, Jungle jungle) throws Exception {
        Application application = applicationSet.getApplication(getApplication().getName());
        Resource resource = jungle.getResource(getResource().getName());

        return resolve(application, resource);
    }

    /**
     * Resolves the stack of JobDescription/Application/Resource objects into
     * one new JobDescription with no dependencies. Ordering (highest priority
     * first):
     * 
     * <ol>
     * <li>Overrides and settings in this description</li>
     * <li>Application settings in given Application</li>
     * <li>Resource settings in given Resource</li>
     * </ol>
     * 
     * @param application
     *            application to use for resolving settings.
     * @param resource
     *            resource to use for resolving settings.
     * 
     * @return the resulting job description, as a new object.
     */
    public JobDescription resolve(Application application, Resource resource) {
        JobDescription result = new JobDescription(this);

        // fetch any unset settings from the given application
        result.application.resolve(application);

        // fetch any unset settings from the given resource
        result.resource.resolve(resource);

        return result;
    }

    /**
     * Print the settings of this job to a (properties) file
     * 
     * @param out
     *            stream to write this file to
     * @param prefix
     *            prefix to add to all keys, or null to use name of job.
     * @throws Exception
     *             if this job has no name
     */
    public void save(PrintWriter out, String prefix, boolean ensureExists) throws Exception {
        boolean empty = true;

        if (prefix == null) {
            throw new Exception("cannot print job description to file," + " name is not specified");
        }

        String dotPrefix = prefix + ".";

        out.println(dotPrefix + "application.name = " + application.getName());
        application.save(out, dotPrefix + "application", false);

        if (processCount == 0) {
            out.println("#" + dotPrefix + "process.count =");
        } else {
            out.println(dotPrefix + "process.count = " + processCount);
            empty = false;
        }

        out.println(dotPrefix + "resource.name = " + resource.getName());
        resource.save(out, dotPrefix + "resource", false);

        if (resourceCount == 0) {
            out.println("#" + dotPrefix + "resource.count =");
        } else {
            out.println(dotPrefix + "resource.count = " + resourceCount);
            empty = false;
        }

        if (runtime == 0) {
            out.println("#" + dotPrefix + "runtime =");
        } else {
            out.println(dotPrefix + "runtime = " + runtime);
            empty = false;
        }

        if (poolName == null) {
            out.println("#" + dotPrefix + "pool.name =");
        } else {
            out.println(dotPrefix + "pool.name = " + poolName);
            empty = false;
        }

        if (poolSize == 0) {
            out.println("#" + dotPrefix + "pool.size =");
        } else {
            out.println(dotPrefix + "pool.size = " + poolSize);
            empty = false;
        }

        if (stdoutFile == null) {
            out.println("#" + dotPrefix + "stdout.file =");
        } else {
            out.println(dotPrefix + "stdout.file = " + stdoutFile);
            empty = false;
        }

        if (stderrFile == null) {
            out.println("#" + dotPrefix + "stderr.file =");
        } else {
            out.println(dotPrefix + "stderr.file = " + stderrFile);
            empty = false;
        }

        if (empty && ensureExists) {
            out.println("#Dummy property to make sure job is actually defined");
            out.println(prefix);
        }
    }

    /**
     * Returns a newline separated string useful for printing.
     * 
     * @return a newline separated string useful for printing.
     */
    public String toPrintString() {
        String result = "Job \"" + getName() + "\"\n";
        result += "Generic Settings:\n";
        result += " Process Count = " + getProcessCount() + "\n";
        result += " Resource Count = " + getResourceCount() + "\n";
        result += " Runtime = " + getRuntime() + "\n";
        result += " Pool Name = " + getPoolName() + "\n";
        result += " Pool Size = " + getPoolSize() + "\n";
        result += " Stdout file = " + getStdoutFile() + "\n";
        result += " Stderr file = " + getStderrFile() + "\n";

        result += application.toPrintString();
        result += resource.toPrintString();

        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }

}
