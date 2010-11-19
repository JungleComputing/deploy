package ibis.deploy;

import ibis.deploy.gui.worldmap.MapUtilities;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.URI;

/**
 * Cluster, accessible using some sort of middleware. Used to deploy both
 * servers (like hubs) and jobs (applications) on. Clusters are part of (and
 * created by) a parent "Grid".
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
                .println("# user.key            User keyfile used for authentication at cluster (only when user.name is set)");
        out
                .println("# cache.dir           Directory on cluster used to cache pre-stage files");
        out.println("#                     (updated using rsync)");
        out
                .println("# server.output.files Output files copied when server exits (e.g. statistics)");

        out
                .println("# server.system.properties system properties for the server (e.g. smartsocekts settings)");

        out
                .println("# nodes               Number of nodes(machines) of this cluster (integer)");
        out
                .println("# cores               Total number of cores of this cluster (integer)");
        out
                .println("# memory               Amount of memory per node in Megabytes (integer)");
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
    
    // Key file for user authentication
    private String keyFile;

    // cache dir for pre-stage files (updating using rsync)
    private File cacheDir;

    // output files of server (statistics, logs and such)
    private List<File> serverOutputFiles;

    // custom system properties for a server (e.g. smartsockets settings)
    private Map<String, String> serverSystemProperties;

    // number of nodes of this cluster
    private int nodes;

    // number of cores of this cluster (in total, not per node)
    private int cores;

    // amount of memory per node, in Megabytes
    private int memory;

    // Latitude position of this cluster
    private double latitude;

    // Longitude position of this cluster
    private double longitude;

    private String color;

    private boolean visibleOnMap;

    /**
     * Applies to the current cluster a set of properties that are specific for
     * the local cluster
     */
    private void applyLocalClusterSettings() throws Exception {
        setName("local");
        setServerAdaptor("local");
        setServerURI(new URI("any://localhost"));
        setJobAdaptor("local");
        setJobURI(new URI("any://localhost"));
        setFileAdaptors("local");
        setJavaPath(System.getProperty("java.home") + File.separator + "bin"
                + File.separator + "java");
        setNodes(1);
        setCores(Runtime.getRuntime().availableProcessors());
        setLatitude(MapUtilities.localClusterLatitude);
        setLongitude(MapUtilities.localClusterLongitude);

        setVisibleOnMap(false);
    }

    /**
     * Creates a new cluster with a given name. Clusters cannot be created
     * directly, but are constructed by a parent Grid object.
     * 
     * @param name
     *            the name of the cluster
     * @throws Exception
     *             if name is null or contains periods and/or spaces
     */
    public Cluster(String name, Grid parent) throws Exception {
        this.parent = parent;
        setName(name);

        if (name == "local") {
            applyLocalClusterSettings();
        } else {
            serverAdaptor = null;
            serverURI = null;
            jobAdaptor = null;
            jobURI = null;
            fileAdaptors = null;
            javaPath = null;
            jobWrapperScript = null;
            userName = null;
            keyFile = null;
            cacheDir = null;
            serverOutputFiles = null;
            serverSystemProperties = null;
            nodes = 0;
            cores = 0;
            memory = 0;
            latitude = 0;
            longitude = 0;
            color = null;
            visibleOnMap = true;
        }
    }

    /**
     * Creates a cluster with no parent or name.
     * 
     */
    public Cluster() {
        name = "anonymous";
        parent = null;
        serverAdaptor = null;
        serverURI = null;
        serverOutputFiles = null;
        jobAdaptor = null;
        jobURI = null;
        fileAdaptors = null;
        javaPath = null;
        jobWrapperScript = null;
        userName = null;
        keyFile = null;
        cacheDir = null;
        serverOutputFiles = null;
        serverSystemProperties = null;
        nodes = 0;
        cores = 0;
        memory = 0;
        latitude = 0;
        longitude = 0;
        color = null;
        visibleOnMap = true;
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
     * @param defaults
     *            cluster containing default values for this cluster
     * 
     * @throws Exception
     *             if cluster cannot be read properly, or its name is invalid
     */
    Cluster(DeployProperties properties, String name, String prefix, Grid parent)
            throws Exception {
        this.parent = parent;
        setName(name);

        // add separator to prefix
        prefix = prefix + ".";

        String defaultPrefix = "default.";

        // first load all the default properties
        if (name == "local") {
            applyLocalClusterSettings(); // the default settings for the local
            // cluster
        } else { // apply the default settings from the file
            serverAdaptor = properties.getProperty(defaultPrefix
                    + "server.adaptor");
            serverURI = properties.getURIProperty(defaultPrefix + "server.uri");
            jobAdaptor = properties.getProperty(defaultPrefix + "job.adaptor");
            jobURI = properties.getURIProperty(defaultPrefix + "job.uri");

            // get adaptors as list of string, defaults to "null"
            fileAdaptors = properties.getStringListProperty(defaultPrefix
                    + "file.adaptors");

            javaPath = properties.getProperty(defaultPrefix + "java.path");
            jobWrapperScript = properties.getFileProperty(defaultPrefix
                    + "job.wrapper.script");

            userName = properties.getProperty(defaultPrefix + "user.name");
            keyFile = properties.getProperty(defaultPrefix + "user.key");
            cacheDir = properties.getFileProperty(defaultPrefix + "cache.dir");
            serverOutputFiles = properties.getFileListProperty(defaultPrefix
                    + "server.output.files");
            serverSystemProperties = properties
                    .getStringMapProperty(defaultPrefix
                            + "server.system.properties");

            nodes = properties.getIntProperty(defaultPrefix + "nodes", 0);
            cores = properties.getIntProperty(defaultPrefix + "cores", 0);
            memory = properties.getIntProperty(defaultPrefix + "memory", 0);
            latitude = properties.getDoubleProperty(defaultPrefix + "latitude",
                    0);
            longitude = properties.getDoubleProperty(defaultPrefix
                    + "longitude", 0);

            visibleOnMap = true;
        }

        // load all the properties corresponding to this cluster,
        // but only if they are set

        if (properties.getProperty(prefix + "server.adaptor") != null) {
            serverAdaptor = properties.getProperty(prefix + "server.adaptor");
        }
        if (properties.getURIProperty(prefix + "server.uri") != null) {
            serverURI = properties.getURIProperty(prefix + "server.uri");
        }
        if (properties.getProperty(prefix + "job.adaptor") != null) {
            jobAdaptor = properties.getProperty(prefix + "job.adaptor");
        }
        if (properties.getURIProperty(prefix + "job.uri") != null) {
            jobURI = properties.getURIProperty(prefix + "job.uri");
        }
        // get adaptors as list of string, defaults to "null"
        if (properties.getStringListProperty(prefix + "file.adaptors") != null) {
            fileAdaptors = properties.getStringListProperty(prefix
                    + "file.adaptors");
        }
        if (properties.getProperty(prefix + "java.path") != null) {
            javaPath = properties.getProperty(prefix + "java.path");
        }
        if (properties.getFileProperty(prefix + "job.wrapper.script") != null) {
            jobWrapperScript = properties.getFileProperty(prefix
                    + "job.wrapper.script");
        }
        if (properties.getProperty(prefix + "user.name") != null) {
            userName = properties.getProperty(prefix + "user.name");
        }
        if (properties.getProperty(prefix + "user.key") != null) {
            keyFile = properties.getProperty(prefix + "user.key");
        }
        if (properties.getFileProperty(prefix + "cache.dir") != null) {
            cacheDir = properties.getFileProperty(prefix + "cache.dir");
        }
        if (properties.getFileListProperty(prefix + "server.output.files") != null) {
            serverOutputFiles = properties.getFileListProperty(prefix
                    + "server.output.files");
        }
        if (properties
                .getStringMapProperty(prefix + "server.system.properties") != null) {
            serverSystemProperties = properties.getStringMapProperty(prefix
                    + "server.system.properties");
        }
        if (properties.getIntProperty(prefix + "nodes", 0) != 0) {
            nodes = properties.getIntProperty(prefix + "nodes", 0);
        }
        if (properties.getIntProperty(prefix + "cores", 0) != 0) {
            cores = properties.getIntProperty(prefix + "cores", 0);
        }
        if (properties.getIntProperty(prefix + "memory", 0) != 0) {
            memory = properties.getIntProperty(prefix + "memory", 0);
        }
        if (properties.getDoubleProperty(prefix + "latitude", 0) != 0) {
            latitude = properties.getDoubleProperty(prefix + "latitude", 0);
        }
        if (properties.getDoubleProperty(prefix + "longitude", 0) != 0) {
            longitude = properties.getDoubleProperty(prefix + "longitude", 0);
        }

        this.color = null;
    }

    public boolean isEmpty() {
        return serverAdaptor == null && serverURI == null
                && serverOutputFiles == null && jobAdaptor == null
                && jobURI == null && fileAdaptors == null && javaPath == null
                && jobWrapperScript == null && userName == null
                && keyFile == null
                && cacheDir == null && serverOutputFiles == null
                && serverSystemProperties == null && nodes == 0 && cores == 0
                && memory == 0 && latitude == 0 && longitude == 0;
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

        if (other.name != null) {
            name = other.name;
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
        
        if (other.keyFile != null) {
            keyFile = other.keyFile;
        }

        if (other.cacheDir != null) {
            cacheDir = other.cacheDir;
        }

        if (other.serverOutputFiles != null) {
            serverOutputFiles = new ArrayList<File>();
            serverOutputFiles.addAll(other.serverOutputFiles);
        }

        if (other.serverSystemProperties != null) {
            for (Map.Entry<String, String> entry : other.serverSystemProperties
                    .entrySet()) {
                setServerSystemProperty(entry.getKey(), entry.getValue());
            }
        }

        if (other.nodes != 0) {
            nodes = other.nodes;
        }

        if (other.cores != 0) {
            cores = other.cores;
        }

        if (other.memory != 0) {
            memory = other.memory;
        }

        if (other.latitude != 0) {
            latitude = other.latitude;
        }

        if (other.longitude != 0) {
            longitude = other.longitude;
        }

        if (other.color != null) {
            color = other.color;
        }

        visibleOnMap = other.visibleOnMap;
    }

    /**
     * Returns grid of this cluster.
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
     * 
     * @throws Exception
     *             if the name is invalid, or a cluster with the given name
     *             already exists in the parent grid of this cluster.
     */
    public void setName(String name) throws Exception {
        if (name == null) {
            throw new Exception("no name specified for cluster");
        }

        if (name.equals(this.name)) {
            // name unchanged
            return;
        }

        if (this.name != null && this.name.equals("local")) {
            throw new Exception("cannot rename local cluster");
        }

        if (name.contains(".")) {
            throw new Exception("cluster name cannot contain periods : \""
                    + name + "\"");
        }
        if (name.contains(" ")) {
            throw new Exception("cluster name cannot contain spaces : \""
                    + name + "\"");
        }

        if (parent != null) {
            if (parent.hasCluster(name)) {
                throw new Exception("cannot set Cluster name to \"" + name
                        + "\", parent Grid already contains "
                        + "a Cluster with that name");
            }
        }

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
    public void setFileAdaptors(String... fileAdaptors) {
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
     * Returns keyfile used to authenticate at this cluster
     * 
     * @return keyfile used to authenticate at this cluster
     */    
    public String getKeyFile() {
        return keyFile;
    }
    
    /**
     * Sets keyfile used to authenticate at this cluster
     * 
     * @param keyFile
     *            keyfile used to authenticate at this cluster
     */   
    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    /**
     * Cache directory used for pre-stage files (updated using rsync)
     * 
     * @return directory used as cache
     */
    public File getCacheDir() {
        return cacheDir;
    }

    /**
     * Sets sache directory used for pre-stage files (updated using rsync)
     * 
     * @param cacheDir
     *            directory used as cache
     */
    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    /**
     * Returns list of server output files. Files are copied from the "root"
     * directory of the server sandbox to the local file specified.
     * 
     * @return list of output files.
     */
    public File[] getServerOutputFiles() {
        if (serverOutputFiles == null) {
            return null;
        }
        return serverOutputFiles.toArray(new File[0]);
    }

    /**
     * Sets list of server output files. Files are copied from the "root"
     * directory of the server sandbox, to the local file specified.
     * 
     * @param serverOutputFiles
     *            new list of output files.
     */
    public void setOutputFiles(File... serverOutputFiles) {
        if (serverOutputFiles == null) {
            this.serverOutputFiles = null;
        } else {
            this.serverOutputFiles = Arrays.asList(serverOutputFiles.clone());
        }
    }

    /**
     * Returns (copy of) map of all system properties for the server.
     * 
     * @return all system properties of the server, or null if unset.
     */
    public Map<String, String> getServerSystemProperties() {
        if (serverSystemProperties == null) {
            return null;
        }
        return new HashMap<String, String>(serverSystemProperties);
    }

    /**
     * Sets map of all system properties for the server.
     * 
     * @param systemProperties
     *            new system properties for the server, or null to unset.
     */
    public void setServerSystemProperties(Map<String, String> systemProperties) {
        if (systemProperties == null) {
            this.serverSystemProperties = null;
        } else {
            this.serverSystemProperties = new HashMap<String, String>(
                    systemProperties);
        }
    }

    /**
     * Sets a single system property for the server. Map will be created if
     * needed.
     * 
     * @param name
     *            name of new property
     * @param value
     *            value of new property.
     */
    public void setServerSystemProperty(String name, String value) {
        if (serverSystemProperties == null) {
            serverSystemProperties = new HashMap<String, String>();
        }
        serverSystemProperties.put(name, value);
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
     * Amount of memory per node in Megabytes
     * 
     * @return Amount of memory per node in Megabytes. Returns 0 if unknown
     */
    public int getMemory() {
        return memory;
    }

    /**
     * Sets Amount of memory per node in Megabytes
     * 
     * @param memory
     *            Amount of memory per node in Megabytes. 0 for unknown
     */
    public void setMemory(int memory) {
        this.memory = memory;
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

    public String getColorCode() {
        return color;
    }

    // public Color getColor() {
    // if (color == null || color.equals("")) {
    // return null;
    // }
    // return Color.decode(color);
    // }
    //
    // public Color getLightColor() {
    // Color color = getColor();
    //        
    // if (color == null) {
    // return null;
    // }
    // return new Color(color.getRed(), color.getGreen(), color.getBlue(), 125);
    // }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Checks if this cluster is suitable for deploying. If not, throws an
     * exception.
     * 
     * @param jobName
     *            name of job
     * @param serverOnly
     *            if only the necessary settings for starting a server need to
     *            be checked
     * 
     * @throws Exception
     *             if this cluster is incomplete or incorrect.
     */
    public void checkSettings(String jobName, boolean serverOnly)
            throws Exception {
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

        if (!serverOnly) {

            if (jobAdaptor == null) {
                throw new Exception(prefix + "job adaptor not specified");
            }

            if (jobURI == null) {
                throw new Exception(prefix + "job URI not specified");
            }

        }

        if (fileAdaptors == null || fileAdaptors.size() == 0) {
            throw new Exception(prefix + "file adaptors not specified");
        }

        // if (cacheDir != null && !cacheDir.isAbsolute()) {
        // throw new Exception("Cache dir must be absolute");
        // }

        if (cores < 0) {
            throw new Exception(prefix + "number of cores negative");
        }

        if (nodes < 0) {
            throw new Exception(prefix + "number of nodes negative");
        }
    }

    /**
     * Resolves cluster and its defaults into a single new cluster object
     * recursively.
     * 
     * @return a resolved cluster
     */
    Cluster resolve() {
        Cluster result = new Cluster();
        result.overwrite(this);
        return result;
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
        boolean empty = true;

        if (prefix == null) {
            throw new Exception("cannot print cluster to file,"
                    + " prefix is not specified");
        }

        String dotPrefix = prefix + ".";

        if (serverAdaptor != null) {
            out.println(dotPrefix + "server.adaptor = " + serverAdaptor);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "server.adaptor = ");
        }

        if (serverURI != null) {
            out
                    .println(dotPrefix + "server.uri = "
                            + serverURI.toASCIIString());
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "server.uri = ");
        }

        if (jobAdaptor != null) {
            out.println(dotPrefix + "job.adaptor = " + jobAdaptor);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "job.adaptor = ");
        }

        if (jobURI != null) {
            out.println(dotPrefix + "job.uri = " + jobURI.toASCIIString());
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "job.uri = ");
        }

        if (fileAdaptors != null) {
            out.println(dotPrefix + "file.adaptors = "
                    + Util.strings2CSS(fileAdaptors));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "file.adaptors = ");
        }

        if (javaPath != null) {
            out.println(dotPrefix + "java.path = " + javaPath);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "java.path = ");
        }

        if (jobWrapperScript != null) {
            out.println(dotPrefix + "job.wrapper.script = " + jobWrapperScript);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "job.wrapper.script = ");
        }

        if (userName != null) {
            out.println(dotPrefix + "user.name = " + userName);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "user.name = ");
        }
        
        if (keyFile != null) {
            out.println(dotPrefix + "user.key = " + keyFile);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "user.key = ");
        }

        if (cacheDir != null) {
            out.println(dotPrefix + "cache.dir = " + cacheDir);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "cache.dir = ");
        }

        if (serverOutputFiles != null) {
            out.println(dotPrefix + "server.output.files = "
                    + Util.files2CSS(serverOutputFiles));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "server.output.files =");
        }

        if (serverSystemProperties != null) {
            out.println(dotPrefix + "server.system.properties = "
                    + Util.toCSString(serverSystemProperties));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "server.system.properties =");
        }

        if (nodes > 0) {
            out.println(dotPrefix + "nodes = " + nodes);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "nodes = ");
        }

        if (cores > 0) {
            out.println(dotPrefix + "cores = " + cores);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "cores = ");
        }

        if (memory > 0) {
            out.println(dotPrefix + "memory = " + memory);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "memory = ");
        }

        if (latitude != 0) {
            out.println(dotPrefix + "latitude = " + latitude);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "latitude = ");
        }

        if (longitude != 0) {
            out.println(dotPrefix + "longitude = " + longitude);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "longitude = ");
        }

        if (empty && printComments) {
            out
                    .println("#Dummy property to make sure cluster is actually defined");
            out.println(prefix);
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
        result += " User keyfile = " + getKeyFile() + "\n";
        result += " Cache dir = " + getCacheDir() + "\n";
        result += " Server output files = " + Util.files2CSS(serverOutputFiles)
                + "\n";
        result += " Server system properties = "
                + Util.toCSString(getServerSystemProperties()) + "\n";
        result += " Nodes = " + getNodes() + "\n";
        result += " Cores = " + getCores() + "\n";
        result += " Memory = " + getMemory() + "\n";
        result += " Latitude = " + getLatitude() + "\n";
        result += " Longitude = " + getLongitude() + "\n";
        result += " Color = " + getColorCode() + "\n";

        return result;
    }

    public boolean isVisibleOnMap() {
        return visibleOnMap;
    }

    public void setVisibleOnMap(boolean visible) {
        visibleOnMap = visible;
    }

}
