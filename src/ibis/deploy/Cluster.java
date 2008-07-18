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

    private String userName;

    private String password;

    private URI applicationBroker;

    private String applicationBrokerAdaptors;

    private String applicationFileAdaptors;

    private String applicationWrapperExecutable;

    private String[] applicationWrapperArguments;

    private URI ibisHubBroker;

    private String ibisHubBrokerAdaptors;

    private String ibisHubFileAdaptors;

    private int totalNodes;

    private int totalCores;

    private String javapath;

    private boolean isWindows;

    private String physicalLocation;

    /**
     * Creates a new Cluster which can be identified in a {@link Grid} by its
     * <code>clusterName</code>.
     * 
     * @param clusterName
     *                the name of this Cluster
     */
    public Cluster(String clusterName) {
        this.name = clusterName;
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
     * <TD>[[grid.]cluster.]hub.broker.uri
     * <TD>any://myhost/mypath
     * <TD>the location of the broker that should be used to deploy the server
     * and hubs on this cluster
     * <TR>
     * <TD>[[grid.]cluster.]hub.broker.adaptors
     * <TD>zorilla, ssh, globus
     * <TD>the resource broker adaptors to be used in this cluster to deploy
     * the server and hubs
     * <TR>
     * <TD>[[grid.]cluster.]hub.file.adaptors
     * <TD>sshtrilead, gridftp, gt4gridftp
     * <TD>the file adaptors to be used in this cluster to deploy the server
     * and hubs
     * <TR>
     * <TD>[[grid.]cluster.]application.broker.uri
     * <TD>any://myhost/mypath
     * <TD>the location of the broker that should be used to deploy the
     * applications on this cluster
     * <TR>
     * <TD>[[grid.]cluster.]application.broker.adaptors
     * <TD>zorilla, ssh, globus
     * <TD>the resource broker adaptors to be used in this cluster to deploy
     * the applications
     * <TR>
     * <TD>[[grid.]cluster.]application.file.adaptors
     * <TD>sshtrilead, gridftp, gt4gridftp
     * <TD>the file adaptors to be used in this cluster to deploy the
     * applications
     * <TR>
     * <TD>[[grid.]cluster.]nodes.total
     * <TD>50
     * <TD>the number of nodes in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]cores.total
     * <TD>200
     * <TD>the number of cores in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]javapath
     * <TD>/usr/local/jdk-1.5/
     * <TD>the location of java in this cluster
     * <TR>
     * <TD>[[grid.]cluster.]is.windows
     * <TD>true
     * <TD>whether this cluster is a windows cluster
     * <TR>
     * <TD>[[grid.]cluster.]user.name
     * <TD>username
     * <TD>the username to be used for this cluster
     * <TR>
     * <TD>[[grid.]cluster.]physical.location
     * <TD>423,234
     * <TD>the physical location of this cluster (x,y)
     * <TR>
     * <TD>[[grid.]cluster.]application.wrapper.executable
     * <TD>/bin/sh
     * <TD>the wrapper executable for this cluster
     * <TR>
     * <TD>[[grid.]cluster.]application.wrapper.arguments
     * <TD>prunscript.sh arg2
     * <TD>arguments for the wrapper executable </TABLE>
     * <p>
     * 
     * @param properties
     *                the properties where to load the cluster from
     * @param gridName
     *                the name of the grid that will be prefixed to this cluster
     *                properties
     * @param clusterName
     *                the name of the cluster, which is also prefixed to this
     *                cluster properties
     */
    public static Cluster load(TypedProperties properties, String gridName,
            String clusterName) throws URISyntaxException {
        if (logger.isInfoEnabled()) {
            logger.info("loading cluster " + clusterName);
        }
        Cluster cluster = new Cluster(clusterName);
        String fullName = gridName + "." + clusterName;
        cluster.applicationBroker = new URI(TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName,
                        "application.broker.uri", "any://localhost"));
        cluster.applicationBrokerAdaptors = TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName,
                        "application.broker.adaptors", null);
        cluster.applicationFileAdaptors = TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName,
                        "application.file.adaptors", null);
        cluster.applicationWrapperExecutable = TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName,
                        "application.wrapper.executable", null);
        cluster.applicationWrapperArguments = TypedPropertiesUtility
                .getHierarchicalStringList(properties, fullName,
                        "application.wrapper.arguments", null, " ");
        cluster.ibisHubBroker = new URI(TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName,
                        "ibis.hub.broker.uri", cluster.applicationBroker
                                .toString()));
        cluster.ibisHubBrokerAdaptors = TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName,
                        "ibis.hub.broker.adaptors",
                        cluster.applicationBrokerAdaptors);
        cluster.ibisHubFileAdaptors = TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName,
                        "ibis.hub.file.adaptors",
                        cluster.applicationFileAdaptors);

        cluster.javapath = TypedPropertiesUtility.getHierarchicalProperty(
                properties, fullName, "javapath", null);
        cluster.totalNodes = TypedPropertiesUtility.getHierarchicalInt(
                properties, fullName, "nodes.total", 1);
        cluster.totalCores = TypedPropertiesUtility.getHierarchicalInt(
                properties, fullName, "cores.total", 1);
        cluster.isWindows = TypedPropertiesUtility.getHierarchicalBoolean(
                properties, fullName, "is.windows", false);
        cluster.userName = TypedPropertiesUtility.getHierarchicalProperty(
                properties, fullName, "user.name", System
                        .getProperty("user.name"));
        cluster.physicalLocation = TypedPropertiesUtility
                .getHierarchicalProperty(properties, fullName,
                        "physical.location", "0,0");
        return cluster;
    }

    /**
     * Gets the user name for this cluster
     * 
     * @return the user name for this cluster
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the password for this cluster
     * 
     * @return the password for this cluster
     */
    protected String getPassword() {
        return password;
    }

    /**
     * Sets the password for this cluster
     */
    public void setPassword(String password) {
        this.password = password;
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
    public int getTotalNodes() {
        return totalNodes;
    }

    /**
     * Returns the x coordinate of the location of this cluster
     * 
     * @return the x coordinate of the location of this cluster
     */
    public int getPhysicalLocationX() {
        if (physicalLocation == null) {
            return -1;
        }
        try {
            String x = physicalLocation.split(",")[0].trim();
            return Integer.parseInt(x);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Returns the y coordinate of the location of this cluster
     * 
     * @return the y coordinate of the location of this cluster
     */
    public int getPhysicalLocationY() {
        if (physicalLocation == null) {
            return -1;
        }
        try {
            String y = physicalLocation.split(",")[1].trim();
            return Integer.parseInt(y);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Sets the java path for this cluster
     * 
     * @param javapath
     *                the java path for this cluster.
     */
    public void setJavapath(String javapath) {
        this.javapath = javapath;
    }

    /**
     * Sets the cluster type to Windows or non-Windows.
     * 
     * @param isWindows
     *                <code>true</code> if cluster is Windows,
     *                <code>false</code> otherwise
     */
    public void setWindows(boolean isWindows) {
        this.isWindows = isWindows;
    }

    /**
     * Gets whether the cluster is a Windows or non Windows cluster.
     * 
     * @return whether the cluster is a Windows or non Windows cluster.
     */
    public boolean isWindows() {
        return isWindows;
    }

    /**
     * Returns the URI of the broker that is used to submit the application to.
     * 
     * @return the URI of the broker that is used to submit the application to.
     */
    public URI getApplicationBroker() {
        return applicationBroker;
    }

    /**
     * Sets the URI of the broker that is used to submit the application to.
     * 
     * @param applicationBroker
     *                the URI of the broker that is used to submit the
     *                application to.
     * 
     */
    public void setApplicationBroker(URI applicationBroker) {
        this.applicationBroker = applicationBroker;
    }

    /**
     * Gets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the resource broker for the application.
     * 
     * @return the String containing a comma separated list of JavaGAT adaptors
     *         that may be used for the resource broker for the application.
     */
    public String getApplicationBrokerAdaptors() {
        return applicationBrokerAdaptors;
    }

    /**
     * Sets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the resource broker for the application.
     * 
     * @param applicationBrokerAdaptors
     *                the String containing a comma separated list of JavaGAT
     *                adaptors that may be used for the resource broker for the
     *                application.
     */
    public void setApplicationBrokerAdaptors(String applicationBrokerAdaptors) {
        this.applicationBrokerAdaptors = applicationBrokerAdaptors;
    }

    /**
     * Gets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the file operations for the application.
     * 
     * @return the String containing a comma separated list of JavaGAT adaptors
     *         that may be used for the file operations for the application.
     */
    public String getApplicationFileAdaptors() {
        return applicationFileAdaptors;
    }

    /**
     * Sets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the file operations for the application.
     * 
     * @param applicationFileAdaptors
     *                the String containing a comma separated list of JavaGAT
     *                adaptors that may be used for the file operations for the
     *                application.
     */
    public void setApplicationFileAdaptors(String applicationFileAdaptors) {
        this.applicationFileAdaptors = applicationFileAdaptors;
    }

    /**
     * Gets the wrapper executable for applications to be run on this cluster.
     * 
     * @return the wrapper executable for applications to be run on this cluster
     */
    public String getApplicationWrapperExecutable() {
        return applicationWrapperExecutable;
    }

    /**
     * Sets the wrapper executable for applications to be run on this cluster.
     * 
     * @param applicationWrapperExecutable
     *                the wrapper executable for applications to be run on this
     *                cluster.
     */
    public void setApplicationWrapperExecutable(
            String applicationWrapperExecutable) {
        this.applicationWrapperExecutable = applicationWrapperExecutable;
    }

    /**
     * Gets the arguments for the wrapper executable for applications to be run
     * on this cluster.
     * 
     * @return the arguments for the wrapper executable for applications to be
     *         run on this cluster.
     */
    public String[] getApplicationWrapperArguments() {
        return applicationWrapperArguments;
    }

    /**
     * Sets the arguments for the wrapper executable for applications to be run
     * on this cluster.
     * 
     * @param applicationWrapperArguments
     *                the arguments for the wrapper executable for applications
     *                to be run on this cluster.
     */
    public void setApplicationWrapperArguments(
            String[] applicationWrapperArguments) {
        this.applicationWrapperArguments = applicationWrapperArguments;
    }

    /**
     * Returns the URI of the broker that is used to submit the ibis hubs and
     * server to.
     * 
     * @return the URI of the broker that is used to submit the ibis hubs and
     *         server to.
     */
    public URI getIbisHubBroker() {
        return ibisHubBroker;
    }

    /**
     * Sets the URI of the broker that is used to submit the ibis hubs and
     * server to.
     * 
     * @param ibisHubBroker
     *                the URI of the broker that is used to submit the ibis hubs
     *                and server to.
     */
    public void setIbisHubBroker(URI ibisHubBroker) {
        this.ibisHubBroker = ibisHubBroker;
    }

    /**
     * Gets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the resource broker for the ibis hubs and server.
     * 
     * @return the String containing a comma separated list of JavaGAT adaptors
     *         that may be used for the resource broker for the ibis hubs and
     *         server.
     */
    public String getIbisHubBrokerAdaptors() {
        return ibisHubBrokerAdaptors;
    }

    /**
     * Sets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the resource broker for the ibis hubs and server.
     * 
     * @param ibisHubBrokerAdaptors
     *                the String containing a comma separated list of JavaGAT
     *                adaptors that may be used for the resource broker for the
     *                ibis hubs and server.
     */
    public void setIbisHubBrokerAdaptors(String ibisHubBrokerAdaptors) {
        this.ibisHubBrokerAdaptors = ibisHubBrokerAdaptors;
    }

    /**
     * Gets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the file operations for the ibis hubs and server.
     * 
     * @return the String containing a comma separated list of JavaGAT adaptors
     *         that may be used for the file operations for the ibis hubs and
     *         server.
     */
    public String getIbisHubFileAdaptors() {
        return ibisHubFileAdaptors;
    }

    /**
     * Sets the String containing a comma separated list of JavaGAT adaptors
     * that may be used for the file operations for the ibis hubs and server.
     * 
     * @param ibisHubFileAdaptors
     *                the String containing a comma separated list of JavaGAT
     *                adaptors that may be used for the file operations for the
     *                ibis hubs and server.
     */
    public void setIbisHubFileAdaptors(String ibisHubFileAdaptors) {
        this.ibisHubFileAdaptors = ibisHubFileAdaptors;
    }

    /**
     * Gets the total number of cores of this cluster
     * 
     * @return the total number of cores of this cluster
     */
    public int getTotalCores() {
        return totalCores;
    }

    /**
     * Sets the total number of cores of this cluster
     * 
     * @param totalCores
     *                the total number of cores of this cluster
     */
    public void setTotalCores(int totalCores) {
        this.totalCores = totalCores;
    }

    /**
     * Gets the physical location of this cluster
     * 
     * @return the physical location of this cluster
     */
    public String getPhysicalLocation() {
        return physicalLocation;
    }

    /**
     * Sets the physical location of this cluster
     * 
     * @param physicalLocation
     *                the physical location of this cluster
     */
    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    /**
     * Gets the javapath of this cluster
     * 
     * @return the javapath of this cluster
     */
    public String getJavapath() {
        return javapath;
    }

    /**
     * Sets the total number of nodes for this cluster
     * 
     * @param totalNodes
     *                the total number of nodes for this cluster
     */
    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

}