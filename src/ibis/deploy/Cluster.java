package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gridlab.gat.URI;

/**
 * Cluster, accessible using some sort of middleware. Used to deploy both
 * servers (like hubs) and jobs (applications) on. Clusters are part of (and
 * created by) a parent "Grid"
 * 
 * @author Niels Drost
 * 
 */
public class Cluster {

    /**
     * Print a table of valid keys and some explanations to the given stream
     * 
     * @param out
     *            stream used for printing
     */
    public static void printTableOfKeys(PrintWriter out) {
        out.println("# Mandatory parameters for clusters:");
        out.println("# KEY                 COMMENT");
        out
                .println("# server.adaptor      JavaGAT adaptor used to deploy server");
        out
                .println("# server.uri          Contact URI used when deploying server");
        out
                .println("# job.adaptor         JavaGAT adaptor used to deploy jobs");
        out
                .println("# job.uri             Contact URI used when deploying job");
        out
                .println("# file.adaptors       Comma separated list of JavaGAT file adaptors used to");
        out
                .println("#                     copy files to and from this cluster(*)");
        out.println("#");
        out.println("# Optional parameters: ");
        out.println("# KEY                 COMMENT");
        out
                .println("# java.path           Path to java executable on this cluster.");
        out.println("#                     If unspecified, \"java\" is used");
        out
                .println("# job.wrapper.script  If specified, the given script is copied to the cluster");
        out.println("#                     and run instead of java");
        out
                .println("# user.name           User name used for authentication at cluster");
        out
                .println("# nodes               Number of nodes(machines) of this cluster (integer)");
        out
                .println("# cores               Total number of cores of this cluster (integer)");
        out
                .println("# latitude            Latitude position of this cluster (double)");
        out
                .println("# longitude           Longitude position of this cluster (double)");
    }

    private final Grid parent;

    // name of this cluster
    private String name;

    // resource broker adaptor used to start server
    private String serverAdaptor;

    // uri of server resource broker
    private URI serverURI;

    // resource broker used to start jobs
    private String jobAdaptor;

    // uri of job broker
    private URI jobURI;

    // adaptor(s) used to copy files to and from the cluster
    private List<String> fileAdaptors;

    // path of java on cluster (simply "java" if not specified)
    private String javaPath;

    // wrapper to use when starting a job
    private File jobWrapperScript;

    // user name to authenticate user with
    private String userName;

    // number of nodes of this cluster
    private int nodes;

    // number of cores of this cluster (in total, not per node)
    private int cores;

    // Latitude position of this cluster
    private double latitude;

    // Longitude position of this cluster
    private double longitude;

    /**
     * Creates a new cluster with a given name. Clusters cannot be created
     * directly, but are constructed by a parent Grid object.
     * 
     * @param name
     *            the name of the cluster
     * @throws Exception
     *             if name is null or contains periods and/or spaces
     */
    Cluster(String name, Grid parent) throws Exception {
        this.parent = parent;
        this.name = name;

        if (name == null) {
            throw new Exception("no name specified for cluster");
        }

        if (name.contains(".")) {
            throw new Exception("cluster name cannot contain periods : \""
                    + name + "\"");
        }
        if (name.contains(" ")) {
            throw new Exception("cluster name cannot contain spaces : \""
                    + name + "\"");
        }

        serverAdaptor = null;
        serverURI = null;
        jobAdaptor = null;
        jobURI = null;
        fileAdaptors = null;
        javaPath = null;
        jobWrapperScript = null;
        userName = null;
        nodes = 0;
        cores = 0;
        latitude = 0;
        longitude = 0;
    }

    /**
     * Creates a cluster with no parent or name.
     * 
     */
    Cluster() {
        name = null;
        parent = null;
        serverAdaptor = null;
        serverURI = null;
        jobAdaptor = null;
        jobURI = null;
        fileAdaptors = null;
        javaPath = null;
        jobWrapperScript = null;
        userName = null;
        nodes = 0;
        cores = 0;
        latitude = 0;
        longitude = 0;
    }

    /**
     * Load cluster from the given properties (usually loaded from a grid file)
     * 
     * @param properties
     *            properties to load cluster from
     * @param name
     *            name of this cluster
     * @param prefix
     *            prefix used for all keys
     * 
     * @throws Exception
     *             if cluster cannot be read properly
     */
    Cluster(TypedProperties properties, String name, String prefix, Grid parent)
            throws Exception {
        this.parent = parent;
        this.name = name;

        // add separator to prefix
        prefix = prefix + ".";

        serverAdaptor = properties.getProperty(prefix + "server.adaptor");
        serverURI = Util.getURIProperty(properties, prefix + "server.uri");
        jobAdaptor = properties.getProperty(prefix + "job.adaptor");
        jobURI = Util.getURIProperty(properties, prefix + "job.uri");

        // get adaptors as list of string, defaults to "null"
        fileAdaptors = Util.getStringListProperty(properties, prefix
                + "file.adaptors");

        javaPath = properties.getProperty(prefix + "java.path");
        jobWrapperScript = Util.getFileProperty(properties, prefix
                + "job.wrapper.script");

        userName = properties.getProperty(prefix + "user.name");
        nodes = properties.getIntProperty(prefix + "nodes", 0);
        cores = properties.getIntProperty(prefix + "cores", 0);
        latitude = properties.getDoubleProperty(prefix + "latitude", 0);
        longitude = properties.getDoubleProperty(prefix + "longitude", 0);
    }

    /**
     * Write all non-null values of given cluster into this cluster.
     * 
     * @param other
     *            source application object
     */
    void overwrite(Cluster other) {
        if (other == null) {
            return;
        }

        if (other.serverAdaptor != null) {
            serverAdaptor = other.serverAdaptor;
        }

        if (other.serverURI != null) {
            serverURI = other.serverURI;
        }

        if (other.jobAdaptor != null) {
            jobAdaptor = other.jobAdaptor;
        }

        if (other.jobURI != null) {
            jobURI = other.jobURI;
        }

        if (other.fileAdaptors != null) {
            fileAdaptors = new ArrayList<String>();
            fileAdaptors.addAll(other.fileAdaptors);
        }

        if (other.javaPath != null) {
            javaPath = other.javaPath;
        }

        if (other.jobWrapperScript != null) {
            jobWrapperScript = other.jobWrapperScript;
        }

        if (other.userName != null) {
            userName = other.userName;
        }

        if (other.nodes != 0) {
            nodes = other.nodes;
        }

        if (other.cores != 0) {
            cores = other.cores;
        }

        if (other.latitude != 0) {
            latitude = other.latitude;
        }

        if (other.longitude != 0) {
            longitude = other.longitude;
        }
    }

    /**
     * Returns grid this cluster belongs to, or null if it does not belong to
     * any grid.
     * 
     * @return parent grid of this cluster.
     */
    public Grid getGrid() {
        return parent;
    }

    /**
     * Returns the name of this cluster
     * 
     * @return the name of this cluster
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this cluster
     * 
     * @param name
     *            the new name of this cluster
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the JavaGAT adaptor used to start servers and hubs on this
     * cluster.
     * 
     * @return the JavaGAT adaptor used to start servers and hubs.
     */
    public String getServerAdaptor() {
        return serverAdaptor;
    }

    /**
     * Sets the JavaGAT adaptor used to start servers and hubs on this cluster.
     * 
     * @param serverAdaptor
     *            the new JavaGAT adaptor used to start servers and hubs.
     */
    public void setServerAdaptor(String serverAdaptor) {
        this.serverAdaptor = serverAdaptor;
    }

    /**
     * Returns the contact uri of this cluster for starting a server or hub
     * (e.g. ssh://machine.domain.com)
     * 
     * @return the contact uri for starting servers and hubs of this cluster.
     */
    public URI getServerURI() {
        return serverURI;
    }

    /**
     * Sets the contact uri of this cluster for starting a server or hub (e.g.
     * ssh://machine.domain.com)
     * 
     * @param serverURI
     *            the new contact uri for starting servers and hubs of this
     *            cluster.
     */
    public void setServerURI(URI serverURI) {
        this.serverURI = serverURI;
    }

    /**
     * Returns the JavaGAT adaptor used to start jobs on this cluster.
     * 
     * @return the JavaGAT adaptor used to start jobs.
     */
    public String getJobAdaptor() {
        return jobAdaptor;
    }

    /**
     * Sets the JavaGAT adaptor used to start jobs on this cluster.
     * 
     * @param jobAdaptor
     *            the new JavaGAT adaptor used to start jobs.
     */
    public void setJobAdaptor(String jobAdaptor) {
        this.jobAdaptor = jobAdaptor;
    }

    /**
     * Returns the contact uri of this cluster for starting jobs (e.g.
     * globus://machine.domain.com/some-local-broker)
     * 
     * @return the contact uri for starting jobs.
     */
    public URI getJobURI() {
        return jobURI;
    }

    /**
     * Sets the contact uri of this cluster for starting jobs (e.g.
     * globus://machine.domain.com/some-local-broker)
     * 
     * @param jobURI
     *            the new contact uri for starting jobs.
     */
    public void setJobURI(URI jobURI) {
        this.jobURI = jobURI;
    }

    /**
     * Returns a list of adaptors used to copy files to and from this cluster.
     * 
     * @return a list of adaptors used to copy files to and from this cluster.
     */
    public String[] getFileAdaptors() {
        if (fileAdaptors == null) {
            return null;
        }
        return fileAdaptors.toArray(new String[0]);
    }

    /**
     * Sets the list of adaptors used to copy files to and from this cluster.
     * 
     * @param fileAdaptors
     *            the new list of adaptors used to copy files to and from this
     *            cluster.
     */
    public void setFileAdaptors(String[] fileAdaptors) {
        if (fileAdaptors == null) {
            this.fileAdaptors = null;
        } else {
            this.fileAdaptors = Arrays.asList(fileAdaptors.clone());
        }
    }

    /**
     * Adds a adaptor to the list of adaptors used to copy files to and from
     * this cluster. The list is created if needed.
     * 
     * @param fileAdaptor
     *            the new adaptors used to copy files to and from this cluster.
     */
    public void addFileAdaptor(String fileAdaptor) {
        if (fileAdaptors == null) {
            fileAdaptors = new ArrayList<String>();
        }
        fileAdaptors.add(fileAdaptor);
    }

    /**
     * Returns the path of the java executable on this cluster. If "null",
     * "java" is used by default.
     * 
     * @return the path of the java executable on this cluster, or null if
     *         unspecified.
     */
    public String getJavaPath() {
        return javaPath;
    }

    /**
     * Sets the path of the java executable on this cluster. If set to "null",
     * "java" is used by default.
     * 
     * @param javaPath
     *            the new path of the java executable on this cluster.
     */
    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    /**
     * Returns the job wrapper script, if any. Useful to start jobs on a cluster
     * without any installed middleware. If specified, this script is pre-staged
     * and executed instead of the java command. This script is passed:
     * <ol>
     * <li>The number of nodes to use</li>
     * <li>The total number of cores to use</li>
     * <li>the java executable</li>
     * <li>jvm options, main class and any additional parameters.</li>
     * </ol>
     * 
     * @return the job wrapper script.
     */
    public File getJobWrapperScript() {
        return jobWrapperScript;
    }

    /**
     * Sets the job wrapper script, if any. Useful to start jobs on a cluster
     * without any installed middleware. If specified, this script is pre-staged
     * and executed instead of the java command. This script is passed:
     * <ol>
     * <li>The number of nodes to use</li>
     * <li>The total number of cores to use</li>
     * <li>the java executable</li>
     * <li>jvm options, main class and any additional parameters.</li>
     * </ol>
     * 
     * @param wrapperScript
     *            the job wrapper script.
     */
    public void setJobWrapperScript(File wrapperScript) {
        this.jobWrapperScript = wrapperScript;
    }

    /**
     * Returns username used to authenticate at this cluster
     * 
     * @return username used to authenticate at this cluster
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets username used to authenticate at this cluster
     * 
     * @param userName
     *            username used to authenticate at this cluster
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Total number of nodes in this cluster
     * 
     * @return Total number of nodes in this cluster. Returns 0 if unknown
     */
    public int getNodes() {
        return nodes;
    }

    /**
     * Sets total number of nodes in this cluster
     * 
     * @param nodes
     *            total number of nodes in this cluster. 0 for unknown
     */
    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    /**
     * Total number of cores in this cluster
     * 
     * @return Total number of cores in this cluster. Returns 0 if unknown
     */
    public int getCores() {
        return cores;
    }

    /**
     * Sets total number of cores in this cluster
     * 
     * @param cores
     *            total number of cores in this cluster. 0 for unknown
     */
    public void setCores(int cores) {
        this.cores = cores;
    }

    /**
     * Latitude position of this cluster
     * 
     * @return Latitude position of this cluster. Returns 0 if unknown
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets latitude position of this cluster
     * 
     * @param latitude
     *            Latitude position of this cluster. Use 0 for unknown.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Longitude position of this cluster
     * 
     * @return Longitude position of this cluster. Returns 0 if unknown
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets longitude position of this cluster
     * 
     * @param longitude
     *            Longitude position of this cluster. Use 0 for unknown.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Checks if this cluster is suitable for deploying. If not, throws an
     * exception.
     * 
     * @param jobName
     *            name of job
     * 
     * @throws Exception
     *             if this cluster is incomplete or incorrect.
     */
    public void checkSettings(String jobName) throws Exception {
        String prefix = "Cannot run job \"" + jobName + "\": Cluster ";

        if (name == null) {
            throw new Exception(prefix + "name not specified");
        }

        if (serverAdaptor == null) {
            throw new Exception(prefix + "server adaptor not specified");
        }

        if (serverURI == null) {
            throw new Exception(prefix + "server URI not specified");
        }

        if (jobAdaptor == null) {
            throw new Exception(prefix + "job adaptor not specified");
        }

        if (jobURI == null) {
            throw new Exception(prefix + "job URI not specified");
        }

        if (fileAdaptors == null || fileAdaptors.size() == 0) {
            throw new Exception(prefix + "file adaptors not specified");
        }

        if (cores < 0) {
            throw new Exception(prefix + "number of cores negative");
        }

        if (nodes < 0) {
            throw new Exception(prefix + "number of nodes negative");
        }
    }

    /**
     * Print the settings of this cluster to a (properties) file
     * 
     * @param out
     *            stream to write this file to
     * @param prefix
     *            prefix for key names, or null to use name of cluster
     * @param printComments
     *            if true, comments are added for all null values
     * @throws Exception
     *             if this cluster has no name
     */
    void save(PrintWriter out, String prefix, boolean printComments)
            throws Exception {
        if (prefix == null && (name == null || name.length() == 0)) {
            throw new Exception("cannot print cluster to file,"
                    + " name is not specified");
        }

        if (prefix == null) {
            prefix = name + ".";
        } else {
            prefix = prefix + ".";
        }

        if (serverAdaptor != null) {
            out.println(prefix + "server.adaptor = " + serverAdaptor);
        } else if (printComments) {
            out.println("#" + prefix + "server.adaptor = ");
        }

        if (serverURI != null) {
            out.println(prefix + "server.uri = " + serverURI.toASCIIString());
        } else if (printComments) {
            out.println("#" + prefix + "server.uri = ");
        }

        if (jobAdaptor != null) {
            out.println(prefix + "job.adaptor = " + jobAdaptor);
        } else if (printComments) {
            out.println("#" + prefix + "job.adaptor = ");
        }

        if (jobURI != null) {
            out.println(prefix + "job.uri = " + jobURI.toASCIIString());
        } else if (printComments) {
            out.println("#" + prefix + "job.uri = ");
        }

        if (fileAdaptors != null) {
            out.println(prefix + "file.adaptors = "
                    + Util.strings2CSS(fileAdaptors));
        } else if (printComments) {
            out.println("#" + prefix + "file.adaptors = ");
        }

        if (javaPath != null) {
            out.println(prefix + "java.path = " + javaPath);
        } else if (printComments) {
            out.println("#" + prefix + "java.path = ");
        }
        
        if (jobWrapperScript != null) {
            out.println(prefix + "job.wrapper.script = " + jobWrapperScript);
        } else if (printComments) {
            out.println("#" + prefix + "job.wrapper.script = ");
        }

        if (userName != null) {
            out.println(prefix + "user.name = " + userName);
        } else if (printComments) {
            out.println("#" + prefix + "user.name = ");
        }

        if (nodes > 0) {
            out.println(prefix + "nodes = " + nodes);
        } else if (printComments) {
            out.println("#" + prefix + "nodes = ");
        }

        if (cores > 0) {
            out.println(prefix + "cores = " + cores);
        } else if (printComments) {
            out.println("#" + prefix + "cores = ");
        }

        if (latitude != 0) {
            out.println(prefix + "latitude = " + latitude);
        } else if (printComments) {
            out.println("#" + prefix + "latitude = ");
        }

        if (longitude != 0) {
            out.println(prefix + "longitude = " + longitude);
        } else if (printComments) {
            out.println("#" + prefix + "longitude = ");
        }

    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }

    /**
     * Returns a new-lined string suitable for printing.
     * 
     * @return Returns a new-lined string suitable for printing.
     */
    public String toPrintString() {
        String result;
        if (name == null) {
            result = "Cluster Settings:\n";
        } else {
            result = "Cluster Settings for \"" + getName() + "\":\n";
        }

        result += " Server adaptor = " + getServerAdaptor() + "\n";
        result += " Server URI = " + getServerURI() + "\n";
        result += " Job adaptor = " + getJobAdaptor() + "\n";
        result += " Job URI = " + getJobURI() + "\n";
        result += " File adaptors = " + Util.strings2CSS(fileAdaptors) + "\n";
        result += " Java path = " + getJavaPath() + "\n";
        result += " Wrapper Script = " + getJobWrapperScript() + "\n";
        result += " User name = " + getUserName() + "\n";
        result += " Nodes = " + getNodes() + "\n";
        result += " Cores = " + getCores() + "\n";
        result += " Latitude = " + getLatitude() + "\n";
        result += " Longitude = " + getLongitude() + "\n";

        return result;
    }

}
