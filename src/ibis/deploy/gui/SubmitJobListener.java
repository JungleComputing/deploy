package ibis.deploy.gui;

import ibis.deploy.JobDescription;

public interface SubmitJobListener {
    
    public void modify(JobDescription jobDescription);

}
