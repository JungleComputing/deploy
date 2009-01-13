package ibis.deploy.gui;

import ibis.deploy.Job;
import ibis.deploy.JobDescription;

public class JobRowObject {

    private Job job;

    private JobDescription jobDescription;

    private String hubState = "";

    private String jobState = "";

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

    public String getHubState() {
        return hubState;
    }

    public String getJobState() {
        return jobState;
    }

    public void setJobState(String state) {
        this.jobState = state;
    }

    public void setHubState(String state) {
        this.hubState = state;
    }

}
