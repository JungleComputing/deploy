package ibis.deploy;

import ibis.util.TypedProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gridlab.gat.URI;

public class Job {
    private static Logger logger = Logger.getLogger(Job.class);

    private static final int DEFAULT_RETRY_ATTEMPTS = 3; // times

    private static final int DEFAULT_RETRY_INTERVAL = 60; // seconds

    private List<SubJob> subjobs = new ArrayList<SubJob>();

    private Deployer deployer;

    private String name;

    private String poolID;

    private int retryAttempts;

    private int retryInterval;

    private boolean closedWorld;

    /**
     * Create a {@link Job} with the name <code>name</code>
     * 
     * @param name
     *            the name of the {@link Job}
     */
    public Job(String name) {
        this.name = name;
    }

    /**
     * Adds a {@link SubJob} to this {@link Job}
     * 
     * @param subjob
     *            the {@link SubJob} to be added
     */
    public void addSubJob(SubJob subjob) {
        subjob.setParent(this);
        subjobs.add(subjob);
    }

    protected void inform() {
        if (subjobs.size() == getStatus().get(
                org.gridlab.gat.resources.Job
                        .getStateString(org.gridlab.gat.resources.Job.STOPPED))
                + getStatus()
                        .get(
                                org.gridlab.gat.resources.Job
                                        .getStateString(org.gridlab.gat.resources.Job.SUBMISSION_ERROR))) {
            deployer.end(name);
        }
    }

    protected URI[] getHubURIs() {
        URI[] result = new URI[subjobs.size()];
        int i = 0;
        for (SubJob subjob : subjobs) {
            result[i++] = subjob.getHubURI();
        }
        return result;
    }

    /**
     * Gets the name of the Job
     * 
     * @return the name of the Job
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the ibis poolID of the Job
     * 
     * @return the ibis poolID of the Job
     */
    public String getPoolID() {
        if (poolID != null) {
            return poolID;
        } else {
            for (SubJob subjob : subjobs) {
                if (subjob.getPoolID() != null) {
                    poolID = subjob.getPoolID();
                    return poolID;
                }
            }
        }
        return null;
    }

    /**
     * Gets the total number of cores used by this Job
     * 
     * @return the total number of cores used by this Job
     */
    public int getTotalCores() {
        int totalCPUs = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            SubJob subJob = subjobs.get(j);
            totalCPUs += subJob.getNodes() * subJob.getMulticore();
        }

        return totalCPUs;
    }

    /**
     * Gets the total number of nodes used by this Job
     * 
     * @return the total number of nodes used by this Job
     */
    public int getTotalNodes() {
        int totalMachines = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            SubJob subjob = subjobs.get(j);
            totalMachines += subjob.getNodes();
        }
        return totalMachines;
    }

    /**
     * Gets the number of {@link SubJob}s of this Job
     * 
     * @return the number of {@link SubJob}s of this Job
     */
    public int numberOfSubJobs() {
        return subjobs.size();
    }

    /**
     * Sets the poolID of this Job
     * 
     * @param poolID
     */
    public void setPoolID(String poolID) {
        this.poolID = poolID;
    }

    public String toString() {
        String res = "";
        int totalMachines = 0;
        int totalCPUs = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            res += "Job " + name + ": ";
            SubJob subJob = subjobs.get(j);
            res += subJob + "\n";
            totalMachines += subJob.getNodes();
            totalCPUs += subJob.getNodes() * subJob.getMulticore();
        }
        res += " total machines in run: " + totalMachines + " for a total of "
                + totalCPUs + " CPUs\n";
        return res;
    }

    protected static Job load(TypedProperties runprops, Set<Grid> grids,
            Set<Application> applications, String jobName) {
        if (logger.isInfoEnabled()) {
            logger.info("loading job");
        }
        Job job = new Job(jobName);
        job.retryAttempts = TypedPropertiesUtility.getHierarchicalInt(runprops,
                jobName, "retry.attempts", DEFAULT_RETRY_ATTEMPTS);
        job.retryInterval = TypedPropertiesUtility.getHierarchicalInt(runprops,
                jobName, "retry.interval", DEFAULT_RETRY_INTERVAL);

        String[] subjobNames = TypedPropertiesUtility
                .getHierarchicalStringList(runprops, jobName, "subjobs", null,
                        ",");

        // runprops.getStringList("subjobs");
        if (subjobNames == null || subjobNames.length == 0) {
            return null;
        } else {
            for (String subjobName : subjobNames) {
                if (logger.isInfoEnabled()) {
                    logger.info("loading subjob: " + subjobName);
                }
                SubJob[] subjobs = SubJob.load(runprops, grids, applications,
                        jobName + "." + subjobName);
                if (subjobs != null) {
                    for (SubJob subjob : subjobs) {
                        job.addSubJob(subjob);
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("subjob '" + subjobName
                                + "' succesfully loaded!");
                    }
                }

            }
        }
        if (job.subjobs.size() == 0) {
            return null;
        } else {
            job.closedWorld = false;
            for (SubJob subjob : job.subjobs) {
                if (job.closedWorld && !subjob.isClosedWorld()) {
                    return null;
                } else {
                    job.closedWorld = subjob.isClosedWorld();
                }
            }
            return job;
        }
    }

    protected int getRetryAttempts() {
        return retryAttempts;
    }

    protected int getRetryInterval() {
        return retryInterval;
    }

    /**
     * Gets a {@link List} of the {@link SubJob}s of this Job.
     * 
     * @return a {@link List} of the {@link SubJob}s of this Job.
     */
    public List<SubJob> getSubJobs() {
        return subjobs;
    }

    /**
     * Gets the status of this Job. The status is a Map containing all the
     * possible states as keys and the number of sub jobs that are in the
     * particular state as value.
     * 
     * @return the status of this Job
     */
    public Map<String, Integer> getStatus() {
        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.INITIAL), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.SCHEDULED), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.RUNNING), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.STOPPED), 0);
        result
                .put(
                        org.gridlab.gat.resources.Job
                                .getStateString(org.gridlab.gat.resources.Job.SUBMISSION_ERROR),
                        0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.ON_HOLD), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.PRE_STAGING), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.POST_STAGING), 0);
        result.put(org.gridlab.gat.resources.Job
                .getStateString(org.gridlab.gat.resources.Job.UNKNOWN), 0);
        for (SubJob subjob : subjobs) {
            String subjobState = subjob.getStatus();
            logger.debug("subjob '" + subjob.getName() + "' is in state '"
                    + subjobState + "'");
            int othersInSameState = result.get(subjobState);
            result.put(subjobState, (othersInSameState + 1));
        }
        return result;
    }

    /**
     * Returns whether this Job is a closed world job or an open world job.
     * 
     * @return <code>true</code> if the Job is closed world,
     *         <code>false</code> otherwise
     */
    public boolean isClosedWorld() {
        return closedWorld;
    }

    /**
     * Sets whether this Job is a closed world job or not
     * 
     * @param closedWorld
     *            <code>true</code> if the Job is closed world,
     *            <code>false</code> otherwise
     */
    public void setClosedWorld(boolean closedWorld) {
        this.closedWorld = closedWorld;
    }

    /**
     * Gets the size of the pool
     * 
     * @return the size of the pool
     * @throws Exception
     *             if one of the {@link SubJob#getPoolSize()} throws an
     *             exception
     */
    public int getPoolSize() throws Exception {
        int result = 0;
        for (SubJob subjob : subjobs) {
            result += subjob.getPoolSize();
        }
        return result;
    }

    protected void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

    protected Application getFirstApplication() {
        if (subjobs != null && subjobs.size() > 0) {
            return subjobs.get(0).getApplication();
        }
        return null;
    }

}