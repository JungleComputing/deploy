package ibis.deploy.gui.listener;

import ibis.deploy.JobDescription;

public interface SubmitJobListener {
    
    public void modify(JobDescription jobDescription);

}
