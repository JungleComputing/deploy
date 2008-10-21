package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.URI;

public class Cluster {

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

	/**
	 * Creates a new cluster with a given name
	 * 
	 * @param name
	 *            the name of the cluster
	 * @throws Exception
	 *             if the name given is <code>null</code>
	 */

	public Cluster(String name) throws Exception {
		if (name == null) {
			throw new Exception("no name specified for cluster");
		}
		this.name = name;
		serverAdaptor = null;
		serverURI = null;
		jobAdaptor = null;
		jobURI = null;
		fileAdaptors = new ArrayList<String>();
		javaPath = null;
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
	public Cluster(TypedProperties properties, String name) throws Exception {
		String prefix;
		if (name == null) {
			prefix = "";
		} else {
			prefix = name + ".";
		}

		this.name = name;
		serverAdaptor = properties.getProperty(prefix + "server.adaptor");
		serverURI = new URI(properties.getProperty(prefix + "server.uri"));
		jobAdaptor = properties.getProperty(prefix + "job.adaptor");
		jobURI = new URI(properties.getProperty(prefix + "job.uri"));

		// get property, convert to String[], convert to List<String>;
		fileAdaptors = Arrays.asList(properties.getStringList(properties
				.getProperty(prefix + "file.adaptors")));

		javaPath = properties.getProperty(prefix + "java.path");
	}

	public List<String> getFileAdaptors() {
		return fileAdaptors;
	}

	public void setFileAdaptors(List<String> fileAdaptors) {
		this.fileAdaptors = fileAdaptors;
	}

	public String getJavaPath() {
		return javaPath;
	}

	public void setJavaPath(String javaPath) {
		this.javaPath = javaPath;
	}

	public String getJobAdaptor() {
		return jobAdaptor;
	}

	public void setJobAdaptor(String jobAdaptor) {
		this.jobAdaptor = jobAdaptor;
	}

	public URI getJobURI() {
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
		return serverAdaptor;
	}

	public void setServerAdaptor(String serverAdaptor) {
		this.serverAdaptor = serverAdaptor;
	}

	public URI getServerURI() {
		return serverURI;
	}

	public void setServerURI(URI serverURI) {
		this.serverURI = serverURI;
	}

	// convert a list of Strings to a single comma seperated String
	private String printStringList(List<String> list) {
		if (list.size() == 0) {
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
	 * @param if
	 *            true, key/value lines prepended with the application name
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

		out.println(prefix + "server.adaptor=" + serverAdaptor);
		out.println(prefix + "server.uri=" + serverURI.toASCIIString());
		out.println(prefix + "job.adaptor=" + jobAdaptor);
		out.println(prefix + "job.uri=" + jobURI.toASCIIString());
		out.println(prefix + "file.adaptors=" + printStringList(fileAdaptors));
		out.println(prefix + "java.path=" + javaPath);
	}

}
