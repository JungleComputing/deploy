package ibis.deploy.android;

import ibis.deploy.android.DeployServiceCallBack;
import ibis.deploy.android.JobUpdateCallBack;

interface DeployService {
 
    void addJob(in String application, in int processCount, in String cluster, in int resourceCount, in boolean start);
    
    void createNewApplication(String applicationName);
    
    void deleteApplication(String applicationName);
    
    List<String> getApplicationNames();
    
    void setApplicationName(String applicationName, String newApplicationName);
    
    String getMainClass(String applicationName);
    
    String getDefaultMainClass();
    
    void setDefaultMainClass(String mainClass);
    
    void setMainClass(String applicationName, String mainClass);
    
    List<String> getArguments(String applicationName);
    
    List<String> getDefaultArguments();
    
    void setDefaultArguments(in List<String> arguments);

    void setArguments(String applicationName, in List<String> arguments);
    
    List<String> getSystemPropertyKeys(String applicationName);
    
    List<String> getSystemPropertyValues(String applicationName);
    
    List<String> getDefaultSystemPropertyKeys();
    
    List<String> getDefaultSystemPropertyValues();
    
    void setDefaultSystemProperties(in List<String> keys, in List<String> values);
    
    void setSystemProperties(String applicationName, in List<String> keys, in List<String> values);
    
    List<String> getJvmOptions(String applicationName);
    
    List<String> getDefaultJvmOptions();
    
    void setDefaultJvmOptions(in List<String> jvmOptions);

    void setJvmOptions(String applicationName, in List<String> jvmOptions);
    
    List<String> getLibraries(String applicationName);
    
    List<String> getDefaultLibraries();
    
    void setDefaultLibraries(in List<String> libraries);
    
    void setLibraries(String applicationName, in List<String> libraries);
    
    List<String> getInputFiles(String applicationName);
    
    List<String> getDefaultInputFiles();
    
    void setDefaultInputFiles(in List<String> inputFiles);
    
    void setInputFiles(String applicationName, in List<String> inputFiles);
    
    List<String> getOutputFiles(String applicationName);
    
    List<String> getDefaultOutputFiles();
    
    void setDefaultOutputFiles(in List<String> outputFiles);
    
    void setOutputFiles(String applicationName, in List<String> outputFiles);

    List<String> getClusterNames();
    
    void setClusterName(String clusterName, String newClusterName);
    
    void createNewCluster(String clusterName);
    
    void deleteCluster(String clusterName);
    
    int getDefaultNodes();
    
    int getNodes(String clusterName);
    
    void setNodes(String clusterName, int nodes);
    
    void setDefaultNodes(int nodes);
    
    int getDefaultCores();
    
    int getCores(String clusterName);
    
    void setCores(String clusterName, int cores);
    
    void setDefaultCores(int cores);
    
    String getDefaultJobUri();
    
    String getJobUri(String clusterName);
    
    void setJobUri(String clusterName, String jobUri);
    
    void setDefaultJobUri(String jobUri);
    
    String getDefaultJobAdaptor();
    
    String getJobAdaptor(String clusterName);
    
    void setJobAdaptor(String clusterName, String jobAdaptor);
    
    void setDefaultJobAdaptor(String jobAdaptor);
    
    List<String> getDefaultFileAdaptors();
    
    List<String> getFileAdaptors(String clusterName);
    
    void setFileAdaptors(String clusterName, in List<String> fileAdaptors);
    
    void setDefaultFileAdaptors(in List<String> fileAdaptors);
    
    String getDefaultUserName();
    
    String getUserName(String clusterName);
    
    void setUserName(String clusterName, String userName);
    
    void setDefaultUserName(String userName);
    
    String getDefaultJavaPath();
    
    String getJavaPath(String clusterName);
    
    void setJavaPath(String clusterName, String javaPath);
    
    void setDefaultJavaPath(String javaPath);
    
    String getDefaultJobWrapperScript();
    
    String getJobWrapperScript(String clusterName);
    
    void setJobWrapperScript(String clusterName, String jobWrapperScript);
    
    void setDefaultJobWrapperScript(String jobWrapperScript);
    
    String getDefaultCacheDirectory();
    
    String getCacheDirectory(String clusterName);
    
    void setCacheDirectory(String clusterName, String cacheDirectory);
    
    void setDefaultCacheDirectory(String cacheDirectory);
    
    String getDefaultServerAdaptor();
    
    String getServerAdaptor(String clusterName);
    
    void setServerAdaptor(String clusterName, String serverAdaptor);
    
    void setDefaultServerAdaptor(String serverAdaptor);
    
    String getDefaultServerUri();
    
    String getServerUri(String clusterName);
    
    void setServerUri(String clusterName, String serverUri);
    
    void setDefaultServerUri(String serverUri);
    
    List<String> getJobNames();
    
    void setDefaultLatitude(double latitude);
    
    void setDefaultLongitude(double longitude);
    
    void setLatitude(String clusterName, double latitude);
    
    void setLongitude(String clusterName, double longitude);
    
    double getDefaultLongitude();
    
    double getDefaultLatitude();
    
    double getLatitude(String clusterName);
    
    double getLongitude(String clusterName);
    
    // Start Job Table Methods
    String getJobName(int i);
    
    String getJobState(String jobName);
    
    String getHubState(String jobName);
    
    boolean isStartable(String jobName);
        
    void addListener(JobUpdateCallBack jobUpdateCallBack);
    
    void removeListener();
    
    void start(String jobName);
    
    void stop(String jobName);
    
    void remove(String jobName);
    
    String getApplication(String jobName);
    
    String getCluster(String jobName);
    
    int getProcessCount(String jobName);
    
    int getResourceCount(String jobName);
    
    String getPool(String jobName);
    
    String getStdout(String jobName);
    
    String getStderr(String jobName);
    // End Job Table Methods
    
    
    void initialize(in String clusterName, DeployServiceCallBack cb);
    
    boolean isInitialized();
    
    String getRootHubAddress();
}