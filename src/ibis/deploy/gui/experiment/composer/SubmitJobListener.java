package ibis.deploy.gui.experiment.composer;

import ibis.deploy.JobDescription;

public interface SubmitJobListener {

    public void modify(JobDescription jobDescription) throws Exception;

}
