package ibis.deploy;

import java.io.File;
import java.util.List;

/**
 * Group of applications, only relation is they live in one file :)
 * 
 * @author ndrost
 *
 */
public class ApplicationGroup {
	
	//application object holding defaults
	private Application defaults;
	
	//list of files required for server (mostly jar files)
	//also supports directories.
	private File[] serverLibs;
	
	private List<Application> applications;
	
	//load applications group from file
	public ApplicationGroup(File file) {
		// TODO Auto-generated constructor stub
		
		
		if (file != null) {
			//load
		}
	}


	public Application getApplication(String applicationName) {
		// TODO Auto-generated method stub
		return null;
	}



	public File[] getServerLibs() {
		return serverLibs;
	}

}
