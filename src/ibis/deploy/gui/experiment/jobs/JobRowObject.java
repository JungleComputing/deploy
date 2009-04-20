package ibis.deploy.gui.experiment.jobs;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.State;

public class JobRowObject {

    private Job job;

    private JobDescription jobDescription;

    private State hubState = null;

    private State jobState = null;

    public JobRowObject(JobDescription jobDescription, Job job) {
        this.job = job;
        this.jobDescription = jobDescription;
    }

    public Job getJob() {
        return job;
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public State getHubState() {
        return hubState;
    }

    public State getJobState() {
        return jobState;
    }

    public void setJobState(State state) {
        this.jobState = state;
    }

    public void setHubState(State state) {
        this.hubState = state;
    }

    public void setJob(Job job) {
        this.job = job;
    }

}
