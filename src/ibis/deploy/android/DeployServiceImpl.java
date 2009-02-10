package ibis.deploy.android;

import ibis.deploy.Application;
import ibis.deploy.Cluster;
import ibis.deploy.Deploy;
import ibis.deploy.Job;
import ibis.deploy.JobDescription;
import ibis.deploy.State;
import ibis.deploy.StateListener;
import ibis.deploy.Workspace;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.URI;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class DeployServiceImpl extends Service {

    private static final File DEFAULT_WORKSPACE_FILE = new File(
            "default.workspace");

    private static int id = 0;

    private Map<String, JobDescription> mJobDescriptions = new HashMap<String, JobDescription>();

    private Map<String, Job> mJobs = new HashMap<String, Job>();

    private Map<String, State> mJobStates = new HashMap<String, State>();

    private Map<String, State> mHubStates = new HashMap<String, State>();

    private Deploy mDeploy;

    private Workspace mWorkspace;

    private boolean mInitialized = false;

    protected JobUpdateCallBack mListener = null;

    @Override
    public void onCreate() {
        // System.setProperty("user.home", "/data/local");
        // System.setProperty("user.name", "rkemp");
        // System.setProperty("user.dir", "/data/local");
        // System.setProperty("smartsockets.modules.define", "hubrouted");
        System.setProperty("user.home", "/data/data/ibis.deploy.android");
        System.setProperty("user.name", "rkemp");
        System.setProperty("user.dir", "/data/data/ibis.deploy.android");

        try {
            mDeploy = new Deploy(null, true);
            mWorkspace = new Workspace(DEFAULT_WORKSPACE_FILE);
            for (JobDescription jobDescription : mWorkspace.getExperiment()
                    .getJobs()) {
                mJobDescriptions.put(jobDescription.getName(), jobDescription);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        try {
            // mWorkspace.save(DEFAULT_WORKSPACE_FILE);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mDeploy.end();
    }

    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        super.onRebind(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // Select the interface to return. If your service only implements
        // a single interface, you can just return it here without checking
        // the Intent.
        if (DeployService.class.getName().equals(intent.getAction())) {
            return mBinder;
        }
        return null;
    }

    private final DeployService.Stub mBinder = new DeployService.Stub() {

        public List<String> getApplicationNames() throws RemoteException {
            List<String> result = new ArrayList<String>();
            if (mWorkspace.getApplications() != null) {
                for (Application application : mWorkspace.getApplications()
                        .getApplications()) {
                    result.add(application.getName());
                }
            }
            return result;
        }

        public List<String> getClusterNames() throws RemoteException {
            List<String> result = new ArrayList<String>();
            if (mWorkspace.getGrid() != null) {
                for (Cluster cluster : mWorkspace.getGrid().getClusters()) {
                    result.add(cluster.getName());
                }
            }
            return result;
        }

        public List<String> getJobNames() throws RemoteException {
            List<String> result = new ArrayList<String>();
            if (mWorkspace.getExperiment() != null) {
                for (JobDescription jobDescription : mWorkspace.getExperiment()
                        .getJobs()) {
                    result.add(jobDescription.getName());
                }
            }
            return result;
        }

        public void addJob(String application, int processCount,
                String cluster, int resourceCount, boolean start)
                throws RemoteException {
            try {
                JobDescription jobDescription = mWorkspace.getExperiment()
                        .createNewJob("job-" + (id++));
                jobDescription.setApplicationName(application);
                jobDescription.setProcessCount(processCount);
                jobDescription.setClusterName(cluster);
                jobDescription.setResourceCount(resourceCount);
                synchronized (this) {
                    mJobDescriptions.put(jobDescription.getName(),
                            jobDescription);
                    if (start) {
                        start(jobDescription.getName());
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        public void initialize(String clusterName,
                final DeployServiceCallBack callback) throws RemoteException {
            try {
                if (clusterName == null || clusterName.equals("local")) {
                    mDeploy.initialize(null, new StateListener() {
                        public void stateUpdated(State newState,
                                Exception exception) {
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                            try {
                                callback.valueChanged(newState.ordinal());
                            } catch (RemoteException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    mDeploy.initialize(mWorkspace.getGrid().getCluster(
                            clusterName), new StateListener() {

                        public void stateUpdated(State newState,
                                Exception exception) {
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                            try {
                                callback.valueChanged(newState.ordinal());
                            } catch (RemoteException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    });
                }
                mInitialized = true;
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public boolean isInitialized() throws RemoteException {
            return mInitialized;
        }

        public String getRootHubAddress() throws RemoteException {
            try {
                return mDeploy.getRootHubAddress();
            } catch (Exception e) {
                return null;
            }
        }

        public void addListener(JobUpdateCallBack jobUpdateCallBack)
                throws RemoteException {
            mListener = jobUpdateCallBack;
        }

        public void removeListener() throws RemoteException {
            mListener = null;
        }

        public double getLatitude(String clusterName) throws RemoteException {
            return mWorkspace.getGrid().getCluster(clusterName).getLatitude();
        }

        public double getLongitude(String clusterName) throws RemoteException {
            return mWorkspace.getGrid().getCluster(clusterName).getLongitude();
        }

        public int getNodes(String clusterName) throws RemoteException {
            return mWorkspace.getGrid().getCluster(clusterName).getNodes();
        }

        public List<String> getArguments(String applicationName)
                throws RemoteException {
            if (mWorkspace.getApplications().getApplication(applicationName)
                    .getArguments() == null) {
                return null;
            }
            return Arrays.asList(mWorkspace.getApplications().getApplication(
                    applicationName).getArguments());
        }

        public List<String> getInputFiles(String applicationName)
                throws RemoteException {
            if (mWorkspace.getApplications().getApplication(applicationName)
                    .getInputFiles() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (File file : mWorkspace.getApplications().getApplication(
                    applicationName).getInputFiles()) {
                result.add(file.getPath());
            }
            return result;
        }

        public List<String> getJvmOptions(String applicationName)
                throws RemoteException {
            if (mWorkspace.getApplications().getApplication(applicationName)
                    .getJVMOptions() == null) {
                return null;
            }
            return Arrays.asList(mWorkspace.getApplications().getApplication(
                    applicationName).getJVMOptions());
        }

        public List<String> getLibraries(String applicationName)
                throws RemoteException {
            if (mWorkspace.getApplications().getApplication(applicationName)
                    .getLibs() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (File file : mWorkspace.getApplications().getApplication(
                    applicationName).getLibs()) {
                result.add(file.getPath());
            }
            return result;
        }

        public String getMainClass(String applicationName)
                throws RemoteException {
            return mWorkspace.getApplications().getApplication(applicationName)
                    .getMainClass();
        }

        public List<String> getOutputFiles(String applicationName)
                throws RemoteException {
            if (mWorkspace.getApplications().getApplication(applicationName)
                    .getOutputFiles() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (File file : mWorkspace.getApplications().getApplication(
                    applicationName).getOutputFiles()) {
                result.add(file.getPath());
            }
            return result;
        }

        public void setApplicationName(String applicationName,
                String newApplicationName) throws RemoteException {
            try {
                mWorkspace.getApplications().getApplication(applicationName)
                        .setName(newApplicationName);
            } catch (Exception e) {
                // TODO wrong application name, do something here
            }

        }

        public void setArguments(String applicationName, List<String> arguments)
                throws RemoteException {
            mWorkspace.getApplications().getApplication(applicationName)
                    .setArguments(
                            arguments.toArray(new String[arguments.size()]));

        }

        public void setInputFiles(String applicationName,
                List<String> inputFiles) throws RemoteException {
            List<File> files = new ArrayList<File>();
            for (String inputFile : inputFiles) {
                files.add(new File(inputFile));
            }
            mWorkspace.getApplications().getApplication(applicationName)
                    .setInputFiles(files.toArray(new File[files.size()]));
        }

        public void setJvmOptions(String applicationName,
                List<String> jvmOptions) throws RemoteException {
            mWorkspace.getApplications().getApplication(applicationName)
                    .setJVMOptions(
                            jvmOptions.toArray(new String[jvmOptions.size()]));

        }

        public void setLibraries(String applicationName, List<String> libraries)
                throws RemoteException {
            List<File> files = new ArrayList<File>();
            for (String library : libraries) {
                files.add(new File(library));
            }
            mWorkspace.getApplications().getApplication(applicationName)
                    .setLibs(files.toArray(new File[files.size()]));

        }

        public void setMainClass(String applicationName, String mainClass)
                throws RemoteException {
            mWorkspace.getApplications().getApplication(applicationName)
                    .setMainClass(mainClass);

        }

        public void setOutputFiles(String applicationName,
                List<String> outputFiles) throws RemoteException {
            List<File> files = new ArrayList<File>();
            for (String outputFile : outputFiles) {
                files.add(new File(outputFile));
            }
            mWorkspace.getApplications().getApplication(applicationName)
                    .setOutputFiles(files.toArray(new File[files.size()]));
        }

        public List<String> getDefaultArguments() throws RemoteException {
            if (mWorkspace.getApplications().getDefaults().getArguments() == null) {
                return null;
            }
            return Arrays.asList(mWorkspace.getApplications().getDefaults()
                    .getArguments());
        }

        public List<String> getDefaultInputFiles() throws RemoteException {
            if (mWorkspace.getApplications().getDefaults().getInputFiles() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (File file : mWorkspace.getApplications().getDefaults()
                    .getInputFiles()) {
                result.add(file.getPath());
            }
            return result;

        }

        public List<String> getDefaultJvmOptions() throws RemoteException {
            if (mWorkspace.getApplications().getDefaults().getJVMOptions() == null) {
                return null;
            }
            return Arrays.asList(mWorkspace.getApplications().getDefaults()
                    .getJVMOptions());
        }

        public List<String> getDefaultLibraries() throws RemoteException {
            if (mWorkspace.getApplications().getDefaults().getLibs() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (File file : mWorkspace.getApplications().getDefaults()
                    .getLibs()) {
                result.add(file.getPath());
            }
            return result;
        }

        public String getDefaultMainClass() throws RemoteException {
            return mWorkspace.getApplications().getDefaults().getMainClass();
        }

        public List<String> getDefaultOutputFiles() throws RemoteException {
            if (mWorkspace.getApplications().getDefaults().getOutputFiles() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (File file : mWorkspace.getApplications().getDefaults()
                    .getOutputFiles()) {
                result.add(file.getPath());
            }
            return result;
        }

        public void setDefaultArguments(List<String> arguments)
                throws RemoteException {
            mWorkspace.getApplications().getDefaults().setJVMOptions(
                    arguments.toArray(new String[arguments.size()]));
        }

        public void setDefaultInputFiles(List<String> inputFiles)
                throws RemoteException {
            List<File> files = new ArrayList<File>();
            for (String inputFile : inputFiles) {
                files.add(new File(inputFile));
            }
            mWorkspace.getApplications().getDefaults().setInputFiles(
                    files.toArray(new File[files.size()]));

        }

        public void setDefaultJvmOptions(List<String> jvmOptions)
                throws RemoteException {
            mWorkspace.getApplications().getDefaults().setJVMOptions(
                    jvmOptions.toArray(new String[jvmOptions.size()]));

        }

        public void setDefaultLibraries(List<String> libraries)
                throws RemoteException {
            List<File> files = new ArrayList<File>();
            for (String library : libraries) {
                files.add(new File(library));
            }
            mWorkspace.getApplications().getDefaults().setLibs(
                    files.toArray(new File[files.size()]));

        }

        public void setDefaultMainClass(String mainClass)
                throws RemoteException {
            mWorkspace.getApplications().getDefaults().setMainClass(mainClass);
        }

        public void setDefaultOutputFiles(List<String> outputFiles)
                throws RemoteException {
            List<File> files = new ArrayList<File>();
            for (String outputFile : outputFiles) {
                files.add(new File(outputFile));
            }
            mWorkspace.getApplications().getDefaults().setOutputFiles(
                    files.toArray(new File[files.size()]));

        }

        public void createNewApplication(String applicationName)
                throws RemoteException {
            try {
                mWorkspace.getApplications().createNewApplication(
                        applicationName);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void deleteApplication(String applicationName) {
            mWorkspace.getApplications().removeApplication(
                    mWorkspace.getApplications()
                            .getApplication(applicationName));
        }

        public void createNewCluster(String clusterName) {
            try {
                mWorkspace.getGrid().createNewCluster(clusterName);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void deleteCluster(String clusterName) {
            mWorkspace.getGrid().removeCluster(
                    mWorkspace.getGrid().getCluster(clusterName));
        }

        public String getCacheDirectory(String clusterName)
                throws RemoteException {
            if (clusterName == null) {
                return null;
            }
            if (mWorkspace.getGrid().getCluster(clusterName).getCacheDir() == null) {
                return null;
            }
            return mWorkspace.getGrid().getCluster(clusterName).getCacheDir()
                    .getPath();
        }

        public int getCores(String clusterName) throws RemoteException {
            if (clusterName == null) {
                return -1;
            }
            return mWorkspace.getGrid().getCluster(clusterName).getCores();
        }

        public String getDefaultCacheDirectory() throws RemoteException {
            if (mWorkspace.getGrid().getDefaults().getCacheDir() == null) {
                return null;
            }
            return mWorkspace.getGrid().getDefaults().getCacheDir().getPath();
        }

        public int getDefaultCores() throws RemoteException {
            return mWorkspace.getGrid().getDefaults().getCores();
        }

        public String getDefaultJavaPath() throws RemoteException {
            return mWorkspace.getGrid().getDefaults().getJavaPath();
        }

        public String getDefaultJobAdaptor() throws RemoteException {
            return mWorkspace.getGrid().getDefaults().getJobAdaptor();
        }

        public String getDefaultJobUri() throws RemoteException {
            if (mWorkspace.getGrid().getDefaults().getJobURI() == null) {
                return null;
            }
            return mWorkspace.getGrid().getDefaults().getJobURI().toString();
        }

        public String getDefaultJobWrapperScript() throws RemoteException {
            if (mWorkspace.getGrid().getDefaults().getJobWrapperScript() == null) {
                return null;
            }
            return mWorkspace.getGrid().getDefaults().getJobWrapperScript()
                    .getPath();
        }

        public int getDefaultNodes() throws RemoteException {
            return mWorkspace.getGrid().getDefaults().getNodes();
        }

        public String getDefaultServerAdaptor() throws RemoteException {
            return mWorkspace.getGrid().getDefaults().getServerAdaptor();
        }

        public String getDefaultServerUri() throws RemoteException {
            if (mWorkspace.getGrid().getDefaults().getServerURI() == null) {
                return null;
            }
            return mWorkspace.getGrid().getDefaults().getServerURI().toString();
        }

        public String getDefaultUserName() throws RemoteException {
            return mWorkspace.getGrid().getDefaults().getUserName();
        }

        public String getJavaPath(String clusterName) throws RemoteException {
            if (clusterName == null) {
                return null;
            }
            return mWorkspace.getGrid().getCluster(clusterName).getJavaPath();
        }

        public String getJobAdaptor(String clusterName) throws RemoteException {
            if (clusterName == null) {
                return null;
            }
            return mWorkspace.getGrid().getCluster(clusterName).getJobAdaptor();
        }

        public String getJobUri(String clusterName) throws RemoteException {
            if (clusterName == null) {
                return null;
            }
            if (mWorkspace.getGrid().getCluster(clusterName).getJobURI() == null) {
                return null;
            }
            return mWorkspace.getGrid().getCluster(clusterName).getJobURI()
                    .toString();
        }

        public String getJobWrapperScript(String clusterName)
                throws RemoteException {
            if (clusterName == null) {
                return null;
            }
            if (mWorkspace.getGrid().getCluster(clusterName)
                    .getJobWrapperScript() == null) {
                return null;
            }
            return mWorkspace.getGrid().getCluster(clusterName)
                    .getJobWrapperScript().getPath();
        }

        public String getServerAdaptor(String clusterName)
                throws RemoteException {
            if (clusterName == null) {
                return null;
            }
            return mWorkspace.getGrid().getCluster(clusterName)
                    .getServerAdaptor();
        }

        public String getServerUri(String clusterName) throws RemoteException {
            if (clusterName == null) {
                return null;
            }
            if (mWorkspace.getGrid().getCluster(clusterName).getServerURI() == null) {
                return null;
            }
            return mWorkspace.getGrid().getCluster(clusterName).getServerURI()
                    .toString();
        }

        public String getUserName(String clusterName) throws RemoteException {
            if (clusterName == null) {
                return null;
            }
            return mWorkspace.getGrid().getCluster(clusterName).getUserName();
        }

        public void setCacheDirectory(String clusterName, String cacheDirectory)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setCacheDir(
                    new File(cacheDirectory));
        }

        public void setCores(String clusterName, int cores)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setCores(cores);
        }

        public void setDefaultCacheDirectory(String cacheDirectory)
                throws RemoteException {
            mWorkspace.getGrid().getDefaults().setCacheDir(
                    new File(cacheDirectory));
        }

        public void setDefaultCores(int cores) throws RemoteException {
            mWorkspace.getGrid().getDefaults().setCores(cores);
        }

        public void setDefaultJavaPath(String javaPath) throws RemoteException {
            mWorkspace.getGrid().getDefaults().setJavaPath(javaPath);
        }

        public void setDefaultJobAdaptor(String jobAdaptor)
                throws RemoteException {
            mWorkspace.getGrid().getDefaults().setJobAdaptor(jobAdaptor);
        }

        public void setDefaultJobUri(String jobUri) throws RemoteException {
            try {
                mWorkspace.getGrid().getDefaults().setJobURI(new URI(jobUri));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void setDefaultJobWrapperScript(String jobWrapperScript)
                throws RemoteException {
            mWorkspace.getGrid().getDefaults().setJobWrapperScript(
                    new File(jobWrapperScript));
        }

        public void setDefaultNodes(int nodes) throws RemoteException {
            mWorkspace.getGrid().getDefaults().setNodes(nodes);
        }

        public void setDefaultServerAdaptor(String serverAdaptor)
                throws RemoteException {
            mWorkspace.getGrid().getDefaults().setServerAdaptor(serverAdaptor);
        }

        public void setDefaultServerUri(String serverUri)
                throws RemoteException {
            try {
                mWorkspace.getGrid().getDefaults().setServerURI(
                        new URI(serverUri));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void setDefaultUserName(String userName) throws RemoteException {
            mWorkspace.getGrid().getDefaults().setUserName(userName);
        }

        public void setJavaPath(String clusterName, String javaPath)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setJavaPath(javaPath);
        }

        public void setJobAdaptor(String clusterName, String jobAdaptor)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setJobAdaptor(
                    jobAdaptor);
        }

        public void setJobUri(String clusterName, String jobUri)
                throws RemoteException {
            try {
                mWorkspace.getGrid().getCluster(clusterName).setJobURI(
                        new URI(jobUri));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void setJobWrapperScript(String clusterName,
                String jobWrapperScript) throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setJobWrapperScript(
                    new File(jobWrapperScript));
        }

        public void setNodes(String clusterName, int nodes)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setNodes(nodes);
        }

        public void setServerAdaptor(String clusterName, String serverAdaptor)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setServerAdaptor(
                    serverAdaptor);
        }

        public void setServerUri(String clusterName, String serverUri)
                throws RemoteException {
            try {
                mWorkspace.getGrid().getCluster(clusterName).setServerURI(
                        new URI(serverUri));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void setUserName(String clusterName, String userName)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setUserName(userName);
        }

        public void setClusterName(String clusterName, String newClusterName)
                throws RemoteException {
            try {
                mWorkspace.getGrid().getCluster(clusterName).setName(
                        newClusterName);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public String getHubState(String jobName) throws RemoteException {
            if (mHubStates.containsKey(jobName)) {
                return mHubStates.get(jobName).name();
            }
            return "";
        }

        public String getJobName(int i) throws RemoteException {
            return mWorkspace.getExperiment().getJobs()[i].getName();
        }

        public String getJobState(String jobName) throws RemoteException {
            if (mJobStates.containsKey(jobName)) {
                return mJobStates.get(jobName).name();
            }
            return "";
        }

        public void remove(String jobName) throws RemoteException {
            // mWorkspace.getExperiment().removeJob(jobName);

        }

        public void start(final String jobName) throws RemoteException {
            try {
                mJobs.put(jobName, mDeploy.submitJob(mJobDescriptions
                        .get(jobName), mWorkspace.getApplications(), mWorkspace
                        .getGrid(), new StateListener() {

                    public void stateUpdated(State newState, Exception exception) {
                        if (exception != null) {
                            exception.printStackTrace();
                        }
                        try {
                            mJobStates.put(jobName, newState);
                            if (mListener != null) {
                                mListener.updateJobState(jobName, newState
                                        .ordinal());
                            }

                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }, new StateListener() {
                    public void stateUpdated(State newState, Exception exception) {
                        if (exception != null) {
                            exception.printStackTrace();
                        }
                        try {
                            mHubStates.put(jobName, newState);
                            if (mListener != null) {
                                mListener.updateHubState(jobName, newState
                                        .ordinal());
                            }
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stop(String jobName) throws RemoteException {
            mJobs.get(jobName).kill();
        }

        public boolean isStartable(String jobName) throws RemoteException {
            try {
                return !mJobs.containsKey(jobName)
                        || mJobs.containsKey(jobName)
                        && (mJobs.get(jobName).getState() == State.DONE || mJobs
                                .get(jobName).getState() == State.ERROR);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

        public List<String> getDefaultSystemPropertyKeys()
                throws RemoteException {
            if (mWorkspace.getApplications().getDefaults()
                    .getSystemProperties() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (String key : mWorkspace.getApplications().getDefaults()
                    .getSystemProperties().keySet()) {
                result.add(key);
            }
            return result;
        }

        public List<String> getDefaultSystemPropertyValues()
                throws RemoteException {
            if (mWorkspace.getApplications().getDefaults()
                    .getSystemProperties() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (String key : mWorkspace.getApplications().getDefaults()
                    .getSystemProperties().keySet()) {
                result.add(mWorkspace.getApplications().getDefaults()
                        .getSystemProperties().get(key));
            }
            return result;
        }

        public List<String> getSystemPropertyKeys(String applicationName)
                throws RemoteException {
            if (mWorkspace.getApplications().getApplication(applicationName)
                    .getSystemProperties() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (String key : mWorkspace.getApplications().getApplication(
                    applicationName).getSystemProperties().keySet()) {
                result.add(key);
            }
            return result;
        }

        public List<String> getSystemPropertyValues(String applicationName)
                throws RemoteException {
            if (mWorkspace.getApplications().getApplication(applicationName)
                    .getSystemProperties() == null) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (String key : mWorkspace.getApplications().getApplication(
                    applicationName).getSystemProperties().keySet()) {
                result.add(mWorkspace.getApplications().getApplication(
                        applicationName).getSystemProperties().get(key));
            }
            return result;
        }

        public void setDefaultSystemProperties(List<String> keys,
                List<String> values) throws RemoteException {
            Map<String, String> systemProperties = new HashMap<String, String>();
            for (int i = 0; i < keys.size(); i++) {
                systemProperties.put(keys.get(i), values.get(i));
            }
            mWorkspace.getApplications().getDefaults().setSystemProperties(
                    systemProperties);
        }

        public void setSystemProperties(String applicationName,
                List<String> keys, List<String> values) throws RemoteException {
            Map<String, String> systemProperties = new HashMap<String, String>();
            for (int i = 0; i < keys.size(); i++) {
                systemProperties.put(keys.get(i), values.get(i));
            }
            mWorkspace.getApplications().getApplication(applicationName)
                    .setSystemProperties(systemProperties);
        }

        public List<String> getDefaultFileAdaptors() throws RemoteException {
            if (mWorkspace.getGrid().getDefaults().getFileAdaptors() == null) {
                return null;
            }
            return Arrays.asList(mWorkspace.getGrid().getDefaults()
                    .getFileAdaptors());
        }

        public List<String> getFileAdaptors(String clusterName)
                throws RemoteException {
            if (mWorkspace.getGrid().getCluster(clusterName).getFileAdaptors() == null) {
                return null;
            }
            return Arrays.asList(mWorkspace.getGrid().getCluster(clusterName)
                    .getFileAdaptors());
        }

        public void setDefaultFileAdaptors(List<String> fileAdaptors)
                throws RemoteException {
            mWorkspace.getGrid().getDefaults().setFileAdaptors(
                    fileAdaptors.toArray(new String[fileAdaptors.size()]));

        }

        public void setFileAdaptors(String clusterName,
                List<String> fileAdaptors) throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setFileAdaptors(
                    fileAdaptors.toArray(new String[fileAdaptors.size()]));
        }

        public double getDefaultLatitude() throws RemoteException {
            return mWorkspace.getGrid().getDefaults().getLatitude();
        }

        public double getDefaultLongitude() throws RemoteException {
            return mWorkspace.getGrid().getDefaults().getLongitude();
        }

        public void setLatitude(String clusterName, double latitude)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName).setLatitude(latitude);
        }

        public void setLongitude(String clusterName, double longitude)
                throws RemoteException {
            mWorkspace.getGrid().getCluster(clusterName)
                    .setLongitude(longitude);
        }

        public void setDefaultLatitude(double latitude) throws RemoteException {
            mWorkspace.getGrid().getDefaults().setLongitude(latitude);
        }

        public void setDefaultLongitude(double longitude)
                throws RemoteException {
            mWorkspace.getGrid().getDefaults().setLongitude(longitude);
        }

        public String getApplication(String jobName) throws RemoteException {
            return mWorkspace.getExperiment().getJob(jobName)
                    .getApplicationName();
        }

        public String getCluster(String jobName) throws RemoteException {
            return mWorkspace.getExperiment().getJob(jobName).getClusterName();
        }

        public String getPool(String jobName) throws RemoteException {
            return mWorkspace.getExperiment().getJob(jobName).getPoolName();
        }

        public int getProcessCount(String jobName) throws RemoteException {
            return mWorkspace.getExperiment().getJob(jobName).getProcessCount();
        }

        public int getResourceCount(String jobName) throws RemoteException {
            return mWorkspace.getExperiment().getJob(jobName)
                    .getResourceCount();
        }

        public String getStdout(String jobName) {
            if (mJobs.containsKey(jobName)) {
                return mJobs.get(jobName).getDescription().getPoolName() + "."
                        + jobName + ".out";
            } else {
                return "";
            }
        }

        public String getStderr(String jobName) {
            if (mJobs.containsKey(jobName)) {
                return mJobs.get(jobName).getDescription().getPoolName() + "."
                        + jobName + ".err";
            } else {
                return "";
            }
        }

    };

}
