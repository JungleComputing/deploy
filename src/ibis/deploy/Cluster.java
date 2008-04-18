/*
 * Created on Mar 6, 2006
 */
package ibis.deploy;

import ibis.util.TypedProperties;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.gridlab.gat.URI;

public class Cluster {
    private static Logger logger = Logger.getLogger(Cluster.class);

    private String name;

    private URI jobBroker;

    private URI deployBroker;

    private String accessType;

    private int nodes;

    private int multicore;

    private String javapath;

    private String fileAccessType;

    /**
     * Creates a new Cluster which can be identified in a {@link Grid} by its
     * <code>clusterName</code>.
     * 
     * @param clusterName
     *            the name of this Cluster
     */
    public Cluster(String clusterName) {
        this.name = clusterName;
    }

    /**
     * Creates a new Cluster which can be identified in a {@link Grid} by its
     * <code>clusterName</code>. This Cluster has a broker that can be used
     * for deployment at the specified {@link URI}.
     * 
     * @param clusterName
     * @param deployBroker
     */
    public Cluster(String clusterName, URI deployBroker) {
        this.name = clusterName;
        this.deployBroker = deployBroker;
    }

    /**
     * Gets the JavaGAT resource broker adaptors which may be used for this
     * cluster in the order and format as described for the JavaGAT Preference "<code>resourcebroker.adaptor.name</code>".
     * 
     * @return the {@link String} containing the JavaGAT resource broker
     *         adaptors.
     */
    public String getAccessType() {
        return accessType;
    }

    /**
     * Gets the java path of this cluster. If the executable <code>java</code>
     * is located at <code>/usr/local/jdk-1.5/bin/java</code>, then the java
     * path will be: <code>/usr/local/jdk-1.5/</code>
     * 
     * @return the java path
     */
    public String getJavaPath() {
        return javapath;
    }

    /**
     * Gets the JavaGAT file adaptors which may be used for this cluster in the
     * order and format as described for the JavaGAT Preference "<code>file.adaptor.name</code>".
     * 
     * @return the {@link String} containing the JavaGAT file adaptors.
     */
    public String getFileAccessType() {
        return fileAccessType;
    }

    /**
     * Gets the URI of the deploy broker
     * 
     * @return the URI of the deploy broker
     */
    public URI getDeployBroker() {
        return deployBroker;
    }

    /**
     * Gets the URI of the job broker
     * 
     * @return the URI of the job broker
     */
    public URI getJobBroker() {
        return jobBroker;
    }

    /**
     * Gets the URI of either the job or the deploy broker
     * 
     * @param isDeployBroker
     *            <code>true</code> for the deploy broker uri,
     *            <code>false</code> for the job broker uri
     * @return the URI of the specified broker
     */
    public URI getBroker(boolean isDeployBroker) {
        if (isDeployBroker) {
            return deployBroker;
        } else {
            return jobBroker;
        }
    }

    /**
     * Loads a cluster specified by a gridname and a clustername from a
     * {@link TypedProperties} object. The following properties can be set:
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="cluster
     * properties"> <CAPTION>cluster properties </CAPTION> <COLGROUP
     * align="left"> <COLGROUP align="center"> <COLGROUP align="left" > <THEAD
     * valign="top">
     * <TR>
     * <TH>Property
     * <TH>Example
     * <TH>Description<TBODY>
     * <TR>
     * <TD>[[grid.]cluster.]file.adaptors
     * <TD>gridftp, ssh, local
     * <TD>the file adaptors to be used in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]resourcebroker.adaptors
     * <TD>zorilla, ssh, globus
     * <TD>the resource broker adaptors to be used in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]nodes
     * <TD>50
     * <TD>the number of nodes in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]multicore
     * <TD>4
     * <TD>the number of cores per node in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]javapath
     * <TD>/usr/local/jdk-1.5/
     * <TD>the location of java in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]job.broker
     * <TD>any://somehost/jobmanager-sge
     * <TD>the broker that should be used to submit the jobs in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]deploy.broker
     * <TD>any://somehost/jobmanager-fork
     * <TD>the broker that should be used to submit the server and hubs in this
     * cluster
     * <TR> </TABLE>
     * <p>
     * 
     * @param properties
     *            the properties where to load the cluster from
     * @param gridName
     *            the name of the grid that will be prefixed to this cluster
     *            properties
     * @param clusterName
     *            the name of the cluster, which is also prefixed to this
     *            cluster properties
     */
    public static Cluster load(TypedProperties properties, String gridName,
            String clusterName) throws URISyntaxException {
        if (logger.isInfoEnabled()) {
            logger.info("loading cluster " + clusterName);
        }
        Cluster cluster = new Cluster(clusterName);
        String fullName = gridName + "." + clusterName;
        cluster.fileAccessType = TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName, "file.adaptors",
                        "");
        cluster.accessType = TypedPropertiesUtility.getHierarchicalProperty(
                properties, fullName, "resourcebroker.adaptors", "");
        cluster.nodes = TypedPropertiesUtility.getHierarchicalInt(properties,
                fullName, "nodes", 0);
        cluster.multicore = TypedPropertiesUtility.getHierarchicalInt(
                properties, fullName, "multicore", 0);
        cluster.javapath = TypedPropertiesUtility.getHierarchicalProperty(
                properties, fullName, "javapath", null);
        String jobBroker = TypedPropertiesUtility.getHierarchicalProperty(
                properties, fullName, "job.broker", null);
        String deployBroker = TypedPropertiesUtility.getHierarchicalProperty(
                properties, fullName, "deploy.broker", null);
        if (deployBroker == null) {
            return null;
        }
        if (jobBroker == null) {
            jobBroker = deployBroker;
        }
        cluster.jobBroker = new URI(jobBroker);
        cluster.deployBroker = new URI(deployBroker);
        return cluster;
    }

    /**
     * Gets the number of cores per node in this cluster
     * 
     * @return the number of cores per node in this cluster
     */
    public int getMulticore() {
        return multicore;
    }

    /**
     * Gets the name of the cluster
     * 
     * @return the name of the cluster
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of nodes in this cluster
     * 
     * @return the number of nodes in this cluster
     */
    public int getNodes() {
        return nodes;
    }

    /**
     * Sets the deploy broker URI for this cluster.
     * 
     * @param deployBroker
     *            the deploy broker URI
     */
    public void setDeployBroker(URI deployBroker) {
        this.deployBroker = deployBroker;
    }

    /**
     * Sets the job broker URI for this cluster.
     * 
     * @param jobBroker
     *            the job broker URI
     */
    public void setJobBroker(URI jobBroker) {
        this.jobBroker = jobBroker;
    }

    /**
     * Sets the access type for this cluster. A string containing a comma
     * separated list of gat adaptors that may be used for resource brokering.
     * 
     * @param accessType
     *            A string containing a comma separated list of gat adaptors
     *            that may be used for resource brokering.
     */
    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    /**
     * Sets the file access type for this cluster. A string containing a comma
     * separated list of gat adaptors that may be used for file operations.
     * 
     * @param fileAccessType
     *            A string containing a comma separated list of gat adaptors
     *            that may be used for file operations.
     */
    public void setFileAccessType(String fileAccessType) {
        this.fileAccessType = fileAccessType;
    }

    /**
     * Sets the java path for this cluster
     * 
     * @param javapath
     *            the java path for this cluster.
     */
    public void setJavapath(String javapath) {
        this.javapath = javapath;
    }
}