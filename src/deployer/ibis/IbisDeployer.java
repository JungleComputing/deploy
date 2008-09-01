package deployer.ibis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.Job.JobState;

import deployer.Application;
import deployer.ApplicationGroup;
import deployer.Cluster;
import deployer.Deployer;
import deployer.Grid;
import deployer.JavaApplication;
import deployer.JavaBasedApplicationGroup;

public class IbisDeployer extends Deployer {

    class JobListener implements MetricListener {

        Server server;

        public JobListener(Server server) {
            this.server = server;
        }

        public void processMetricEvent(MetricEvent event) {
            if (event.getValue() == JobState.STOPPED) {
                try {
                    server.stopServer();
                } catch (GATInvocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

    }

    @SuppressWarnings("unchecked")
    public Class<? extends Application>[] getApplicationSubTypes() {
        return new Class[] { Application.class, JavaApplication.class,
                IbisApplication.class };
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ApplicationGroup>[] getApplicationGroupSubTypes() {
        return new Class[] { ApplicationGroup.class,
                JavaBasedApplicationGroup.class,
                IbisBasedApplicationGroup.class };
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Grid>[] getGridSubTypes() {
        return new Class[] { IbisBasedGrid.class };
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Cluster>[] getClusterSubTypes() {
        return new Class[] { IbisCluster.class };
    }

    protected String getJavaClassPath(String[] filenames,
            boolean recursivePrefix, boolean toWindows) {
        String classpath = "";
        if (filenames != null) {
            for (String filename : filenames) {
                classpath += getFiles(new java.io.File(filename), "", ".jar",
                        recursivePrefix);
            }
        }
        if (toWindows) {
            classpath = classpath.replace('/', '\\');
            classpath = classpath.replace(':', ';');
        }
        return classpath;
    }

    private String getFiles(java.io.File file, String prefix, String postfix,
            boolean recursivePrefix) {
        String result = "";
        if (file.isDirectory()) {
            for (java.io.File childfile : file.listFiles()) {
                String resolvedPrefix = "";
                if (recursivePrefix) {
                    resolvedPrefix = prefix + file.getName() + "/";
                }
                result += getFiles(childfile, resolvedPrefix, postfix,
                        recursivePrefix);
            }
        } else if (file.getName().endsWith(postfix)) {
            result += prefix + file.getName() + ":";
        }
        return result;
    }

    public Job deploy(Application application, int processCount,
            Cluster cluster, int resourceCount, IbisPool pool,
            String serverAddress, String hubAddress, MetricListener listener)
            throws Exception, GATInvocationException, IOException {
        JavaSoftwareDescription sd = (JavaSoftwareDescription) application
                .getSoftwareDescription();

        // add ibis specific things to sd
        if (sd.getExecutable() == null || sd.getExecutable().equals("java")) {
            sd.setExecutable(cluster.getJavaPath());
        }
        String[] files = new String[sd.getPreStaged().size()];
        int i = 0;
        for (File file : sd.getPreStaged().keySet()) {
            files[i++] = file.getPath();
        }
        sd.setJavaOptions(new String[] { "-classpath",
                getJavaClassPath(files, true, cluster.isWindows()) });

        Map<String, String> systemProperties = new HashMap<String, String>();
        if (sd.getJavaSystemProperties() != null) {
            systemProperties.putAll(sd.getJavaSystemProperties());
        }
        systemProperties.put("ibis.server.address", serverAddress);
        systemProperties.put("ibis.server.hub.addresses", hubAddress);
        systemProperties.put("ibis.pool.name", pool.getName());
        if (pool.isClosedWorld()) {
            systemProperties.put("ibis.pool.size", "" + pool.getSize());
        }

        // systemProperties.put("log4j.configuration", "file:"
        // + application.getLog4jPropertiesLocation());
        sd.setJavaSystemProperties(systemProperties);

        JobDescription jd = new JobDescription(sd);
        jd.setProcessCount(processCount);
        jd.setResourceCount(resourceCount);
        Preferences preferences = new Preferences();
        preferences.put("file.chmod", "0755");
        if (cluster.getBrokerAdaptors() != null) {
            preferences.put("resourcebroker.adaptor.name", cluster
                    .getBrokerAdaptors());
        }
        if (cluster.getFileAdaptors() != null) {
            preferences.put("file.adaptor.name", cluster.getFileAdaptors());
        }
        ResourceBroker broker = GAT.createResourceBroker(preferences, cluster
                .getBroker());
        Job result = broker.submitJob(jd, listener, "job.status");
        return result;
    }
}
