package ibis.deploy;

import ibis.util.TypedProperties;

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
	private String[] fileAdaptors;

	// path of java on cluster (simply "java" if not specified)
	private String javaPath;

	// user name to authenticate user with
	private String userName;

	private int nodes;

	private double latitude;

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
		userName = null;
		nodes = 0;
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

		String serverURIString = properties.getProperty(prefix + "server.uri");
		if (serverURIString == null) {
			serverURI = null;
		} else {
			serverURI = new URI(serverURIString);
		}
		jobAdaptor = properties.getProperty(prefix + "job.adaptor");

		String jobURIString = properties.getProperty(prefix + "job.uri");
		if (jobURIString == null) {
			jobURI = null;
		} else {
			jobURI = new URI(jobURIString);
		}

		// get adaptors as list of string, defaults to "null"
		fileAdaptors = properties.getStringList(prefix + "file.adaptors", ",",
				null);

		javaPath = properties.getProperty(prefix + "java.path");
		userName = properties.getProperty(prefix + "user.name");
		nodes = properties.getIntProperty(prefix + "nodes", 0);
		latitude = properties.getDoubleProperty(prefix + "latitude", 0);
		longitude = properties.getDoubleProperty(prefix + "latitude", 0);

	}

	public String[] getFileAdaptors() {
		if (fileAdaptors == null) {
			if (parent == null) {
				return null;
			}
			return parent.getDefaults().getFileAdaptors();
		}
		return fileAdaptors.clone();
	}

	public void setFileAdaptors(String[] fileAdaptors) {
		if (fileAdaptors == null) {
			this.fileAdaptors = null;
		} else {
			this.fileAdaptors = fileAdaptors.clone();
		}
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

	// convert a list of Strings to a single comma seperated String
	private static String printStringList(String[] list) {
		if (list == null || list.length == 0) {
			return "";
		}
		String result = "";
		for (String object : list) {
			result = result + object.toString() + ",";
		}
		return result.substring(0, result.length() - 1);
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
					+ printStringList(fileAdaptors));
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

	public String getFileAdaptorString() {
		return printStringList(fileAdaptors);
	}

	public String toPrintString() {
		String result = "Cluster " + getName() + "\n";
		result += " Server adaptor = " + getServerAdaptor() + "\n";
		result += " Server URI = " + getServerURI() + "\n";
		result += " Job adaptor = " + getJobAdaptor() + "\n";
		result += " Job URI = " + getJobURI() + "\n";
		result += " File adaptors = " + Arrays.deepToString(fileAdaptors)
				+ "\n";
		result += " Server adaptor = " + getServerAdaptor() + "\n";
		result += " Java path = " + getJavaPath() + "\n";
		result += " User name = " + getUserName() + "\n";
		result += " Nodes = " + getNodes() + "\n";

		return result;
	}

}
