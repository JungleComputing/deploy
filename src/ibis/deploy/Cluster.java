package ibis.deploy;

import java.util.List;

import org.gridlab.gat.URI;

public class Cluster {
	
	//name of this cluster
	private String name;
	
	//resource broker adaptor used to start server
	private String serverAdaptor;
	
	//uri of server resource broker
	private URI serverURI;

	//resource broker used to start jobs
	private String jobAdaptor;
	
	//uri of job broker
	private URI jobURI;
	
	//adaptor(s) used for files
	private List<String> fileAdaptors;
	
	//path of java on cluster (simply "java" if not specified)
	private String javaPath;

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

}
