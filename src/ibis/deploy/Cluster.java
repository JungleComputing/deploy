package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gridlab.gat.URI;

public class Cluster {

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

    // adaptor(s) used for files
    private List<String> fileAdaptors;

    // path of java on cluster (simply "java" if not specified)
    private String javaPath;

    // wrapper to use when starting a job
    private File wrapperScript;

    // user name to authenticate user with
    private String userName;

    // number of nodes of this cluster
    private int nodes;

    // number of cores of this cluster (in total, not per node)
    private int cores;

    // latitue position of this cluster
    private double latitude;

    // longitude position of this cluster
    private double longitude;

    /**
     * Creates a new cluster with a given name. Clusters cannot be created
     * directly, but are constructed by a parent Grid object.
     * 
     * @param name
     *            the name of the cluster
     * @throws Exception
     *             if the name given is <code>null</code>
     */

    Cluster(String name, Grid parent) throws Exception {
        this.parent = parent;

        if (name == null) {
            throw new Exception("no name specified for cluster");
        }
        this.name = name;
        serverAdaptor = null;
        serverURI = null;
        jobAdaptor = null;
        jobURI = null;
        fileAdaptors = null;
        javaPath = null;
        wrapperScript = null;
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
     * @param object
     *            name of this cluster, or null to load "defaults" cluster
     * @throws Exception
     *             if cluster cannot be read properly
     */
    Cluster(TypedProperties properties, String name, Grid parent)
            throws Exception {
        this.parent = parent;

        String prefix;
        if (name == null) {
            prefix = "";
        } else {
            prefix = name + ".";
        }

        this.name = name;
        serverAdaptor = properties.getProperty(prefix + "server.adaptor");
        serverURI = Util.getURIProperty(properties, prefix + "server.uri");
        jobAdaptor = properties.getProperty(prefix + "job.adaptor");
        jobURI = Util.getURIProperty(properties, prefix + "job.uri");

        // get adaptors as list of string, defaults to "null"
        fileAdaptors = Util.getStringListProperty(properties, prefix + "file.adaptors");
        
        javaPath = properties.getProperty(prefix + "java.path");
        wrapperScript = Util.getFileProperty(properties, prefix
                + "wrapper.script");

        userName = properties.getProperty(prefix + "user.name");
        nodes = properties.getIntProperty(prefix + "nodes", 0);
        cores = properties.getIntProperty(prefix + "cores", 0);
        latitude = properties.getDoubleProperty(prefix + "latitude", 0);
        longitude = properties.getDoubleProperty(prefix + "longitude", 0);

    }

    public String[] getFileAdaptors() {
        if (fileAdaptors == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getFileAdaptors();
        }
        return fileAdaptors.toArray(new String[0]);
    }

    public void setFileAdaptors(String[] fileAdaptors) {
        if (fileAdaptors == null) {
            this.fileAdaptors = null;
        } else {
            this.fileAdaptors = Arrays.asList(fileAdaptors.clone());
        }
    }
    
    public void addFileAdaptor(String fileAdaptor) {
        if (fileAdaptors == null) {
            fileAdaptors = new ArrayList<String>();
        }
        fileAdaptors.add(fileAdaptor);
    }

    public String getJavaPath() {
        if (javaPath == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getJavaPath();
        }
        return javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public File getWrapperScript() {
        if (wrapperScript == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getWrapperScript();
        }
        return wrapperScript;
    }

    public void setWrapperScript(File wrapperScript) {
        this.wrapperScript = wrapperScript;
    }

    public String getJobAdaptor() {
        if (jobAdaptor == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getJobAdaptor();
        }
        return jobAdaptor;
    }

    public void setJobAdaptor(String jobAdaptor) {
        this.jobAdaptor = jobAdaptor;
    }

    public URI getJobURI() {
        if (jobURI == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getJobURI();
        }
        return jobURI;
    }

    public void setJobURI(URI jobURI) {
        this.jobURI = jobURI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerAdaptor() {
        if (serverAdaptor == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getServerAdaptor();
        }
        return serverAdaptor;
    }

    public void setServerAdaptor(String serverAdaptor) {
        this.serverAdaptor = serverAdaptor;
    }

    public URI getServerURI() {
        if (serverURI == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getServerURI();
        }
        return serverURI;
    }

    public void setServerURI(URI serverURI) {
        this.serverURI = serverURI;
    }

    public String getUserName() {
        if (userName == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getUserName();
        }
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Total number of nodes in this cluster
     * 
     * @return Total number of nodes in this cluster. Returns 0 if unknown
     */
    public int getNodes() {
        if (nodes <= 0) {
            if (parent == null) {
                return 0;
            }
            return parent.getDefaults().getNodes();
        }
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    /**
     * Total number of cores in this cluster
     * 
     * @return Total number of cores in this cluster. Returns 0 if unknown
     */
    public int getCores() {
        if (cores <= 0) {
            if (parent == null) {
                return 0;
            }
            return parent.getDefaults().getCores();
        }
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    /**
     * Latitude position of this cluster
     * 
     * @return Latitude position of this cluster. Returns 0 if unknown
     */
    public double getLatitude() {
        if (latitude == 0) {
            if (parent == null) {
                return 0;
            }
            return parent.getDefaults().getLatitude();
        }
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Longitude position of this cluster
     * 
     * @return Longitude position of this cluster. Returns 0 if unknown
     */
    public double getLongitude() {
        if (longitude == 0) {
            if (parent == null) {
                return 0;
            }
            return parent.getDefaults().getLongitude();
        }
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Print the settings of this application to a (properties) file
     * 
     * @param out
     *            stream to write this file to
     * @param prependName
     *            if true, key/value lines prepended with the application name
     * @throws Exception
     *             if this cluster has no name
     */
    public void print(PrintWriter out, boolean prependName) throws Exception {
        String prefix;

        if (prependName) {
            if (name == null || name.length() == 0) {
                throw new Exception("cannot print cluster to file,"
                        + " name is not specified");
            }
            prefix = name + ".";
        } else {
            prefix = "";
        }

        if (serverAdaptor == null) {
            out.println("#" + prefix + "server.adaptor = ");
        } else {
            out.println(prefix + "server.adaptor = " + serverAdaptor);
        }

        if (serverURI == null) {
            out.println("#" + prefix + "server.uri = ");
        } else {
            out.println(prefix + "server.uri = " + serverURI.toASCIIString());
        }

        if (jobAdaptor == null) {
            out.println("#" + prefix + "job.adaptor = ");
        } else {
            out.println(prefix + "job.adaptor = " + jobAdaptor);
        }

        if (jobURI == null) {
            out.println("#" + prefix + "job.uri = ");
        } else {
            out.println(prefix + "job.uri = " + jobURI.toASCIIString());
        }

        if (fileAdaptors == null) {
            out.println("#" + prefix + "file.adaptors = ");
        } else {
            out.println(prefix + "file.adaptors = "
                    + Util.strings2CSS(fileAdaptors));
        }

        if (javaPath == null) {
            out.println("#" + prefix + "java.path = ");
        } else {
            out.println(prefix + "java.path = " + javaPath);
        }

        if (userName == null) {
            out.println("#" + prefix + "user.name = ");
        } else {
            out.println(prefix + "user.name = " + userName);
        }

        if (nodes <= 0) {
            out.println("#" + prefix + "nodes = ");
        } else {
            out.println(prefix + "nodes = " + nodes);
        }
        
        if (cores <= 0) {
            out.println("#" + prefix + "cores = ");
        } else {
            out.println(prefix + "cores = " + cores);
        }

        if (latitude == 0) {
            out.println("#" + prefix + "latitude = ");
        } else {
            out.println(prefix + "latitude = " + latitude);
        }

        if (longitude == 0) {
            out.println("#" + prefix + "longitude = ");
        } else {
            out.println(prefix + "longitude = " + longitude);
        }

    }

    public String toString() {
        return name;
    }

    public String toPrintString() {
        String result = "Cluster " + getName() + "\n";
        result += " Server adaptor = " + getServerAdaptor() + "\n";
        result += " Server URI = " + getServerURI() + "\n";
        result += " Job adaptor = " + getJobAdaptor() + "\n";
        result += " Job URI = " + getJobURI() + "\n";
        result += " File adaptors = " + Util.strings2CSS(fileAdaptors) + "\n";
        result += " Java path = " + getJavaPath() + "\n";
        result += " Wrapper Script = " + getWrapperScript() + "\n";
        result += " User name = " + getUserName() + "\n";
        result += " Nodes = " + getNodes() + "\n";
        result += " Cores = " + getCores() + "\n";
        result += " Latitude = " + getLatitude() + "\n";
        result += " Longitude = " + getLongitude() + "\n";

        return result;
    }

}
