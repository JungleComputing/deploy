package ibis.deploy;

import ibis.deploy.util.Colors;
import ibis.util.TypedProperties;

import java.awt.Color;
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
 * support processes (like hubs) and jobs (applications) on. Clusters are part
 * of (and created by) a parent "Grid".
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
        out.println("# support.uri         Contact URI used when deploying support processes (e.g. smartsockets hub)");
        out.println("# job.uri             Contact URI used when deploying job");
        out.println("# file.adaptors       Comma separated list of JavaGAT file adaptors used to");
        out.println("#                     copy files to and from this cluster(*)");
        out.println("#");
        out.println("# Optional parameters: ");
        out.println("# KEY                 COMMENT");
        out.println("# support.adaptor     JavaGAT adaptor used to deploy support processes (e.g. smartsockets hub)");
        out.println("# support.system.properties system properties for the support processes (e.g. smartsocekts settings)");

        out.println("# job.adaptor         JavaGAT adaptor used to deploy jobs");
        out.println("# java.path           Path to java executable on this cluster.");
        out.println("#                     If unspecified, \"java\" is used");
        out.println("# job.wrapper.script  If specified, the given script is copied to the cluster");
        out.println("#                     and run instead of java");
        out.println("# user.name           User name used for authentication at cluster");
        out.println("# user.key            User keyfile used for authentication at cluster (only when user.name is set)");

        out.println("# latitude            Latitude position of this cluster (double)");
        out.println("# longitude           Longitude position of this cluster (double)");
        out.println("# color               Color (as a HTML color string) used to represent this cluster");
    }

    // name of this cluster
    private String name;

    // resource broker adaptor used to start support processes
    private String supportAdaptor;

    // uri of support resource broker
    private URI supportURI;

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

    // custom system properties for a support process (e.g. smartsockets
    // settings)
    private Map<String, String> supportSystemProperties;

    // Latitude position of this cluster
    private double latitude;

    // Longitude position of this cluster
    private double longitude;

    private Color color;

    private boolean visibleOnMap;

    private final DeployProperties properties;

    /**
     * Applies to the current cluster a set of properties that are specific for
     * the local cluster
     */
    public static Cluster getLocalCluster() throws Exception {
        Cluster result = new Cluster("local");

        result.setSupportAdaptor("local");
        result.setSupportURI(new URI("local://localhost"));
        result.setJobAdaptor("local");
        result.setJobURI(new URI("local://localhost"));
        result.setFileAdaptors("local");
        result.setJavaPath(System.getProperty("java.home") + File.separator
                + "bin" + File.separator + "java");
        result.setLatitude(0);
        result.setLongitude(0);

        result.setVisibleOnMap(false);

        result.setColor(Colors.LOCAL_COLOR);

        return result;
    }

    /**
     * Creates a new "anonymous" cluster with no name.
     * 
     */
    public Cluster() {
        this.name = "anonymous";

        properties = new DeployProperties();

        supportAdaptor = null;
        supportURI = null;
        jobAdaptor = null;
        jobURI = null;
        fileAdaptors = null;
        javaPath = null;
        jobWrapperScript = null;
        userName = null;
        keyFile = null;
        supportSystemProperties = null;
        latitude = 0;
        longitude = 0;
        visibleOnMap = true;
        color = null;
    }

    /**
     * Creates a new cluster with a given name.
     * 
     * @param name
     *            the name of the cluster
     * @throws Exception
     *             if name is null or contains periods and/or spaces
     */
    public Cluster(String name) throws Exception {
        setName(name);

        // set color from name
        color = Colors.fromLocation(name);

        properties = new DeployProperties();

        supportAdaptor = null;
        supportURI = null;
        jobAdaptor = null;
        jobURI = null;
        fileAdaptors = null;
        javaPath = null;
        jobWrapperScript = null;
        userName = null;
        keyFile = null;
        supportSystemProperties = null;
        latitude = 0;
        longitude = 0;
        visibleOnMap = true;
    }

    /**
     * Copy constructor
     * 
     * @param original
     *            the original cluster
     */
    public Cluster(Cluster original) {
        this();

        name = original.name;
        color = original.color;

        properties.addProperties(original.properties);

        supportAdaptor = original.supportAdaptor;
        supportURI = original.supportURI;
        jobAdaptor = original.jobAdaptor;
        jobURI = original.jobURI;

        if (original.fileAdaptors != null) {
            fileAdaptors = new ArrayList<String>(original.fileAdaptors);
        }

        javaPath = original.javaPath;

        jobWrapperScript = original.jobWrapperScript;
        userName = original.userName;
        keyFile = original.keyFile;

        if (original.supportSystemProperties != null) {
            supportSystemProperties = new HashMap<String, String>(
                    original.supportSystemProperties);
        }

        latitude = original.latitude;
        longitude = original.longitude;
        visibleOnMap = original.visibleOnMap;
    }

    /**
     * Load cluster from the given properties (usually loaded from a grid file)
     * 
     * @param properties
     *            properties to load cluster from
     * @param prefix
     *            prefix used for all keys
     * 
     * @throws Exception
     *             if cluster cannot be read properly, or its name is invalid
     */
    public void loadFromProperties(DeployProperties properties, String prefix)
            throws Exception {
        // add separator to prefix
        String name = prefix;
        prefix = prefix + ".";

        if (properties.getProperty(prefix + "server.uri") != null
                || properties.getProperty(prefix + "server.adaptor") != null
                || properties.getProperty(prefix + "server.system.properties") != null) {
            throw new Exception("The cluster description for \"" + name
                    + "\" contains \"server.*\" properties, which have "
                    + "been renamed to \"support.*\"");
        }

        // load all the properties corresponding to this cluster,
        // but only if they are set

        if (properties.getProperty(prefix + "support.adaptor") != null) {
            supportAdaptor = properties.getProperty(prefix + "support.adaptor");
        }
        if (properties.getProperty(prefix + "support.uri") != null) {
            supportURI = properties.getURIProperty(prefix + "support.uri");
        }
        if (properties.getProperty(prefix + "job.adaptor") != null) {
            jobAdaptor = properties.getProperty(prefix + "job.adaptor");
        }
        if (properties.getProperty(prefix + "job.uri") != null) {
            jobURI = properties.getURIProperty(prefix + "job.uri");
        }
        // get adaptors as list of string, defaults to "null"
        if (properties.getProperty(prefix + "file.adaptors") != null) {
            fileAdaptors = properties.getStringListProperty(prefix
                    + "file.adaptors");
        }
        if (properties.getProperty(prefix + "java.path") != null) {
            javaPath = properties.getProperty(prefix + "java.path");
        }
        if (properties.getProperty(prefix + "job.wrapper.script") != null) {
            jobWrapperScript = properties.getFileProperty(prefix
                    + "job.wrapper.script");
        }
        if (properties.getProperty(prefix + "user.name") != null) {
            userName = properties.getProperty(prefix + "user.name");
        }
        if (properties.getProperty(prefix + "user.key") != null) {
            keyFile = properties.getProperty(prefix + "user.key");
        }
        if (properties.getDoubleProperty(prefix + "latitude", 0) != 0) {
            latitude = properties.getDoubleProperty(prefix + "latitude", 0);
        }
        if (properties.getDoubleProperty(prefix + "longitude", 0) != 0) {
            longitude = properties.getDoubleProperty(prefix + "longitude", 0);
        }
        if (properties.getProperty(prefix + "support.system.properties") != null) {
            supportSystemProperties = properties.getStringMapProperty(prefix
                    + "support.system.properties");
        }
        if (properties.getProperty(prefix + "color") != null) {
            color = properties.getColorProperty(prefix + "color");
        }

        // copy all properties with right prefix to properties map
        this.properties.addProperties(properties.filter(prefix, true, false));

    }

    public boolean isEmpty() {
        return supportAdaptor == null && supportURI == null
                && jobAdaptor == null && jobURI == null && fileAdaptors == null
                && javaPath == null && jobWrapperScript == null
                && supportSystemProperties == null && userName == null
                && keyFile == null && latitude == 0 && longitude == 0;
    }

    /**
     * Set any unset settings from the given other object
     * 
     * @param other
     *            source cluster object
     */
    void resolve(Cluster other) {
        if (other != null) {

            if (other.supportAdaptor != null && supportAdaptor == null) {
                supportAdaptor = other.supportAdaptor;
            }

            if (other.supportURI != null && supportURI == null) {
                supportURI = other.supportURI;
            }

            if (other.jobAdaptor != null && jobAdaptor == null) {
                jobAdaptor = other.jobAdaptor;
            }

            if (other.jobURI != null && jobURI == null) {
                jobURI = other.jobURI;
            }

            if (other.fileAdaptors != null && fileAdaptors == null) {
                fileAdaptors = new ArrayList<String>();
                fileAdaptors.addAll(other.fileAdaptors);
            }

            if (other.javaPath != null && javaPath == null) {
                javaPath = other.javaPath;
            }

            if (other.jobWrapperScript != null && jobWrapperScript == null) {
                jobWrapperScript = other.jobWrapperScript;
            }

            if (other.userName != null && userName == null) {
                userName = other.userName;
            }

            if (other.keyFile != null && keyFile == null) {
                keyFile = other.keyFile;
            }

            if (other.supportSystemProperties != null
                    && supportSystemProperties == null) {
                for (Map.Entry<String, String> entry : other.supportSystemProperties
                        .entrySet()) {
                    setSupportSystemProperty(entry.getKey(), entry.getValue());
                }
            }

            if (other.latitude != 0 && latitude == 0) {
                latitude = other.latitude;
            }

            if (other.longitude != 0 && longitude == 0) {
                longitude = other.longitude;
            }

            if (other.color != null && color == null) {
                color = other.color;
            }

            // if any is set, show
            visibleOnMap = visibleOnMap || other.visibleOnMap;
        }

        if (color == null) {
            color = Colors.fromLocation(getName());
        }

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
            // throw new Exception("no name specified for cluster");
            return;
        }

        if (name.equals(this.name)) {
            // name unchanged
            return;
        }

        if (name.contains(".")) {
            throw new Exception("cluster name cannot contain periods : \""
                    + name + "\"");
        }
        if (name.contains(" ")) {
            throw new Exception("cluster name cannot contain spaces : \""
                    + name + "\"");
        }

        this.name = name;
    }

    /**
     * Returns the JavaGAT adaptor used to start support processes such as hubs
     * on this cluster.
     * 
     * @return the JavaGAT adaptor used to start support processes such as hubs.
     */
    public String getSupportAdaptor() {
        return supportAdaptor;
    }

    /**
     * Sets the JavaGAT adaptor used to start support processes such as hubs on
     * this cluster.
     * 
     * @param supportAdaptor
     *            the new JavaGAT adaptor used to start support processes such
     *            as hubs.
     */
    public void setSupportAdaptor(String supportAdaptor) {
        this.supportAdaptor = supportAdaptor;
    }

    /**
     * Returns the contact uri of this cluster for starting support processes
     * such as a hub (e.g. ssh://machine.domain.com)
     * 
     * @return the contact uri for starting support processes such as hubs of
     *         this cluster.
     */
    public URI getSupportURI() {
        return supportURI;
    }

    /**
     * Sets the contact uri of this cluster for starting support processes such
     * as a hub (e.g. ssh://machine.domain.com)
     * 
     * @param supportURI
     *            the new contact uri for starting support processes such as
     *            hubs of this cluster.
     */
    public void setSupportURI(URI supportURI) {
        this.supportURI = supportURI;
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
     * Returns (copy of) map of all system properties for support processes.
     * 
     * @return all system properties of support processes, or null if unset.
     */
    public Map<String, String> getSupportSystemProperties() {
        if (supportSystemProperties == null) {
            return null;
        }
        return new HashMap<String, String>(supportSystemProperties);
    }

    /**
     * Sets map of all system properties for support processes
     * 
     * @param systemProperties
     *            new system properties for support processes, or null to unset.
     */
    public void setSupportSystemProperties(Map<String, String> systemProperties) {
        if (systemProperties == null) {
            this.supportSystemProperties = null;
        } else {
            this.supportSystemProperties = new HashMap<String, String>(
                    systemProperties);
        }
    }

    /**
     * Sets a single system property for support processes. Map will be created
     * if needed.
     * 
     * @param name
     *            name of new property
     * @param value
     *            value of new property.
     */
    public void setSupportSystemProperty(String name, String value) {
        if (supportSystemProperties == null) {
            supportSystemProperties = new HashMap<String, String>();
        }
        supportSystemProperties.put(name, value);
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get the raw properties this cluster was created from. Useful for
     * supporting custom properties in deploy based applications
     * 
     * @return the raw properties this cluster was created from.
     */
    public TypedProperties getProperties() {
        return properties;
    }

    /**
     * Checks if this cluster is suitable for deploying. If not, throws an
     * exception.
     * 
     * @param jobName
     *            name of job
     * @param supportOnly
     *            if only the necessary settings for starting a support process
     *            need to be checked
     * 
     * @throws Exception
     *             if this cluster is incomplete or incorrect.
     */
    public void checkSettings(String jobName, boolean supportOnly)
            throws Exception {
        String prefix = "Cannot run job \"" + jobName + "\": Cluster ";

        if (name == null) {
            throw new Exception(prefix + "name not specified");
        }

        if (supportURI == null) {
            throw new Exception(prefix + "support URI not specified");
        }

        if (!supportOnly) {
            if (jobURI == null) {
                throw new Exception(prefix + "job URI not specified");
            }

        }

        if (fileAdaptors == null || fileAdaptors.size() == 0) {
            throw new Exception(prefix + "file adaptors not specified");
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
        boolean empty = true;

        if (prefix == null) {
            throw new Exception("cannot print cluster to file,"
                    + " prefix is not specified");
        }

        String dotPrefix = prefix + ".";

        if (supportAdaptor != null) {
            out.println(dotPrefix + "support.adaptor = " + supportAdaptor);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "support.adaptor = ");
        }

        if (supportURI != null) {
            out.println(dotPrefix + "support.uri = "
                    + supportURI.toASCIIString());
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "support.uri = ");
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
                    + DeployProperties.strings2CSS(fileAdaptors));
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

        if (supportSystemProperties != null) {
            out.println(dotPrefix + "support.system.properties = "
                    + DeployProperties.toCSString(supportSystemProperties));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "support.system.properties =");
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

        if (color != null) {
            out.println(dotPrefix + "color = " + Colors.color2colorCode(color));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "color = ");
        }

        if (empty && printComments) {
            out.println("#Dummy property to make sure cluster is actually defined");
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

        result += " Support adaptor = " + getSupportAdaptor() + "\n";
        result += " Support URI = " + getSupportURI() + "\n";
        result += " Job adaptor = " + getJobAdaptor() + "\n";
        result += " Job URI = " + getJobURI() + "\n";
        result += " File adaptors = "
                + DeployProperties.strings2CSS(fileAdaptors) + "\n";
        result += " Java path = " + getJavaPath() + "\n";
        result += " Wrapper Script = " + getJobWrapperScript() + "\n";
        result += " User name = " + getUserName() + "\n";
        result += " User keyfile = " + getKeyFile() + "\n";
        result += " Support system properties = "
                + DeployProperties.toCSString(getSupportSystemProperties())
                + "\n";
        result += " Latitude = " + getLatitude() + "\n";
        result += " Longitude = " + getLongitude() + "\n";
        result += " Color = " + Colors.color2colorCode(color) + "\n";

        return result;
    }

    public boolean isVisibleOnMap() {
        return visibleOnMap;
    }

    public void setVisibleOnMap(boolean visible) {
        visibleOnMap = visible;
    }

}
