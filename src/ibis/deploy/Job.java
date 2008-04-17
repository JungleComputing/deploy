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

    public Job(String name) {
        this.name = name;
    }

    protected SubJob getSubJob(int index) {
        return subjobs.get(index);
    }

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

    public URI[] getHubURIs() {
        URI[] result = new URI[subjobs.size()];
        int i = 0;
        for (SubJob subjob : subjobs) {
            result[i++] = subjob.getHubURI();
        }
        return result;
    }

    public String getName() {
        return name;
    }

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

    public int getTotalCores() {
        int totalCPUs = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            SubJob subJob = subjobs.get(j);
            totalCPUs += subJob.getNodes() * subJob.getMulticore();
        }

        return totalCPUs;
    }

    public int getTotalNodes() {
        int totalMachines = 0;
        for (int j = 0; j < subjobs.size(); j++) {
            SubJob subjob = subjobs.get(j);
            totalMachines += subjob.getNodes();
        }
        return totalMachines;
    }

    public int numberOfSubJobs() {
        return subjobs.size();
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public List<SubJob> getSubJobs() {
        return subjobs;
    }

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

    public boolean isClosedWorld() {
        return closedWorld;
    }

    public void setClosedWorld(boolean closedWorld) {
        this.closedWorld = closedWorld;
    }

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

    public Application getFirstApplication() {
        if (subjobs != null && subjobs.size() > 0) {
            return subjobs.get(0).getApplication();
        }
        return null;
    }

}
