package ibis.deploy;

import java.util.List;

import org.gridlab.gat.URI;

public class Cluster {
	
	//name of this cluster
	private String name;
	
	//resource broker used to start server
	private String serverBrokerAdaptors;
	
	//uri of server resource broker
	private URI serverBrokerURI;

	//resource broker used to start jobs
	private String jobBrokerAdaptor;
	
	//uri of job broker
	private URI jobBrokerURI;
	
	//adaptor(s) used for files
	private List<String> fileAdaptors;
	
	//path of java on cluster (simply "java" if not specified)
	private String javaPath;

	public void startHub(Hub rootServer) {
		// TODO Auto-generated method stub
		
	}
}
