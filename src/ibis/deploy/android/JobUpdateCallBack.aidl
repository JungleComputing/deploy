package ibis.deploy.android;

oneway interface JobUpdateCallBack {
 
    void updateHubState(String jobName, int state);
    
    void updateJobState(String jobName, int state);
    
}